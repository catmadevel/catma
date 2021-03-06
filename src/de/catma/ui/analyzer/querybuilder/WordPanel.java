/*   
 *   CATMA Computer Aided Text Markup and Analysis
 *   
 *   Copyright (C) 2009-2013  University Of Hamburg
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.catma.ui.analyzer.querybuilder;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;

import de.catma.queryengine.querybuilder.WildcardBuilder;

public class WordPanel extends FormLayout {
	
	public static interface WordChangeListener {
		public void wordChanged();
	}

	private static class PositionItem {
		private int position;
		private String displayString;
		private PositionItem(int pos, String displayString) {
			this.position = pos;
			this.displayString = displayString;
		}
		
		@Override
		public String toString() {
			return displayString;
		}
		
		public int getPosition() {
			return position;
		}
	}
	private TextField startsWithField;
	private TextField containsField;
	private TextField endsWithField;
	private TextField exactField;
	private ComboBox positionBox;
	private Button btRemove;
	private WildcardBuilder wildcardBuilder;
	private boolean withPositionBox;

	public WordPanel(ValueChangeListener valueChangeListener) {
		this(false, null, valueChangeListener);
	}
	
	public WordPanel(
			boolean withPositionBox, List<WordPanel> wordPanelList, 
			ValueChangeListener valueChangeListener) {
		this.wildcardBuilder = new WildcardBuilder();
		this.withPositionBox = withPositionBox;
		initComponents();
		initActions(wordPanelList, valueChangeListener);
	}

	private void initActions(
			final List<WordPanel> wordPanelList, ValueChangeListener valueChangeListener) {
		if (withPositionBox) {
			btRemove.addClickListener(new ClickListener() {
				
				public void buttonClick(ClickEvent event) {
					((ComponentContainer)WordPanel.this.getParent()).removeComponent(
							WordPanel.this);
					wordPanelList.remove(WordPanel.this);
				}
			});
		}
		exactField.addValueChangeListener(new ValueChangeListener() {
			
			public void valueChange(ValueChangeEvent event) {
				String value = (String)event.getProperty().getValue();
				if ((value != null) && (!value.isEmpty())){
					setWildcardInputFieldsEnabled(false);
				}
				else {
					setWildcardInputFieldsEnabled(true);
				}
			}
		});
		startsWithField.addValueChangeListener(valueChangeListener);
		containsField.addValueChangeListener(valueChangeListener);
		endsWithField.addValueChangeListener(valueChangeListener);
		exactField.addValueChangeListener(valueChangeListener);
	}

	private void setWildcardInputFieldsEnabled(boolean enabled) {
		startsWithField.setEnabled(enabled);
		containsField.setEnabled(enabled);
		endsWithField.setEnabled(enabled);		
	}

	private void initComponents() {
		setSpacing(true);
		setMargin(true);
		setSizeFull();
		
		String next = " first ";
		if (withPositionBox) {
			next = " next ";
		}

		startsWithField = new TextField();
		startsWithField.setImmediate(true);
		startsWithField.setCaption("The"+next+"word starts with");
		addComponent(startsWithField);
		
		containsField = new TextField();
		containsField.setImmediate(true);
		containsField.setCaption("The"+next+"word contains");
		addComponent(containsField);
		
		endsWithField = new TextField();
		endsWithField.setImmediate(true);
		endsWithField.setCaption("The"+next+"word ends with");
		addComponent(endsWithField);
		
		exactField = new TextField();
		exactField.setImmediate(true);
		exactField.setCaption("The"+next+"word is exactly");
		addComponent(exactField);
		
		if (withPositionBox) {
			List<PositionItem> options = new ArrayList<PositionItem>();
			for (int i=1; i<=10; i++) {
				options.add(
					new PositionItem(
						i, i+" word"+(i==1?"":"s")+" after the previous word"));
			}
			
			positionBox =
					new ComboBox("The position of this word is", options);
			positionBox.setImmediate(true);
			addComponent(positionBox);
			positionBox.setNullSelectionAllowed(false);
			positionBox.setNewItemsAllowed(false);
			positionBox.setValue(options.get(0));
			
			btRemove = new Button("Remove");
			addComponent(btRemove);
			setComponentAlignment(btRemove, Alignment.MIDDLE_CENTER);
		}
	}
	
	public String getWildcardWord() {
		String exactValue = (String)exactField.getValue();
		if ((exactValue != null) && !exactValue.isEmpty()) {
			if (withPositionBox) {
				return wildcardBuilder.getWildcardFor(exactValue, ((PositionItem)positionBox.getValue()).getPosition());
			}
			return exactValue;
		}
		return wildcardBuilder.getWildcardFor(
			(String)startsWithField.getValue(), 
			(String)containsField.getValue(), 
			(String)endsWithField.getValue(), 
			(withPositionBox?((PositionItem)positionBox.getValue()).getPosition():1));
	}
}
