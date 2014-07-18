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
package de.catma.ui.tagger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutAction.ModifierKey;
import com.vaadin.server.ClassResource;
import com.vaadin.server.Resource;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.catma.tag.Property;
import de.catma.tag.PropertyDefinition;
import de.catma.tag.PropertyValueList;
import de.catma.tag.TagInstance;
import de.catma.ui.dialog.SaveCancelListener;

public class PropertyEditDialog extends Window {

	private static enum TreePropertyName {
		property,
		icon,
		value,
		assigned, 
		;
	}
	
	private TreeTable propertyTree;
	private AdhocPropertyValuesBuffer propertyValuesBuffer;
	private ComboBox cbAddNewValue;
	private Button btAdd;
	private Button btSave;
	private Button btCancel;
	private TagInstance tagInstance;
	private Set<Property> changedProperties;
	private Label hintText;
	private boolean init = false;

	public PropertyEditDialog(TagInstance tagInstance,
			SaveCancelListener<Set<Property>> saveCancelListener,
			AdhocPropertyValuesBuffer propertyValuesBuffer) {
		super("Edit Properties for Tag "
				+tagInstance.getTagDefinition().getName());
		this.tagInstance = tagInstance;
		this.propertyValuesBuffer = propertyValuesBuffer;
		changedProperties = new HashSet<Property>();
		initComponents();
		initActions(saveCancelListener);
	}
	
	@Override
	public void attach() {
		super.attach();
		
		if (!init ) {
			initData();
			init = true;
		}
	}
	

	private void initData() {
		for (Property p : tagInstance.getUserDefinedProperties()) {
			PropertyDefinition propertyDefinition = p.getPropertyDefinition();
			ClassResource pIcon = new ClassResource("ui/tagmanager/resources/ylwdiamd.gif");
			
			propertyTree.addItem(
					new Object[] {
							propertyDefinition.getName(),
							null,
							null},
					p);
			propertyTree.getContainerProperty(
					p, TreePropertyName.icon).setValue(pIcon);
			propertyTree.setChildrenAllowed(p, true);
			
			Set<String> values = new HashSet<String>();
			
			values.addAll(propertyDefinition.getPossibleValueList().getPropertyValueList().getValues());
			values.addAll(p.getPropertyValueList().getValues());
			
			for (String pValue : values) {
				String pValueItemId = propertyDefinition.getUuid() + "_" + pValue;
				CheckBox cb = createCheckBox(p, pValue);
				
				propertyTree.addItem(
					new Object[] {
							null,
							pValue,
							cb
					},
					pValueItemId);
				
				propertyTree.setParent(pValueItemId, p);
				propertyTree.setChildrenAllowed(pValueItemId, false);
			}
			propertyTree.setCollapsed(p, false);
			
//			Alex: this is part of my attempt to set key shortcuts for check boxes.
//			It's still here because I want to keep it a little while.
//			Set<Object> idSet = new HashSet<Object>();
//			for (Object id : propertyTree.getItemIds()){
//				idSet.add(id);
//			}
//			cb.setClickShortcut(KeyCode.NUM0, ModifierKey.ALT);
		}
		
		if (tagInstance.getUserDefinedProperties().size() == 1){
			propertyTree.setValue(
					tagInstance.getUserDefinedProperties().iterator().next());
		}
//		Object selection = propertyTree.getValue();
//		for (String value : getValues(selection)){
//			selectNewValue.addItem(value);
//		}
		
		// Populate the combo box with values.
		for (String value : propertyValuesBuffer.getValues()){
			cbAddNewValue.addItem(value);
		}
		
	}

	private CheckBox createCheckBox(final Property p, final String pValue) {
		final CheckBox cb = new CheckBox(
			null, p.getPropertyValueList().getValues().contains(pValue));
		
		cb.addValueChangeListener(new ValueChangeListener() {
			
			@Override
			public void valueChange(ValueChangeEvent event) {
				propertyValueChanged(p, pValue, cb.getValue());
			}
		});
		return cb;
	}
	
	private void propertyValueChanged(Property p, String pValue, boolean add){
		List<String> valueList = new ArrayList<String>();
		valueList.addAll(p.getPropertyValueList().getValues());
		
		if (add) {
			valueList.add(pValue);
		}
		else {
			valueList.remove(pValue);
		}
		
		p.setPropertyValueList(new PropertyValueList(valueList));
		
		changedProperties.add(
				tagInstance.getProperty(p.getPropertyDefinition().getUuid()));
		
	}

	private void initActions(
			final SaveCancelListener<Set<Property>> saveCancelListener) {

		btCancel.addClickListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				UI.getCurrent().removeWindow(PropertyEditDialog.this);
				saveCancelListener.cancelPressed();
			}
		});
		
		btSave.addClickListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				UI.getCurrent().removeWindow(PropertyEditDialog.this);
				saveCancelListener.savePressed(changedProperties);
			}
		});
		
		
		btAdd.addClickListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				Object selection = propertyTree.getValue();
				final Property property = getProperty(selection);
				final String pValue = (String)cbAddNewValue.getValue();
				
				if ((pValue == null)||(pValue.isEmpty())) {
					Notification.show(
						"Info", "The value can not be empty!", 
						Type.TRAY_NOTIFICATION);
				}
				else {
							
					if (property == null) {
						Notification.show(
							"Info", 
							"Please select exactly one Property from the list first!",
							Type.TRAY_NOTIFICATION);
					}
					else {
						if (property.getPropertyValueList().getValues().contains(
								pValue) ||
							property.getPropertyDefinition()
								.getPossibleValueList().getPropertyValueList()
								.getValues().contains(pValue)) {
								Notification.show(
									"Info",
									"This value already exists. Please choose another name!", 
									Type.TRAY_NOTIFICATION);
						}
						else {
							propertyValuesBuffer.addValue(pValue);
	
							propertyValueChanged(property, pValue, true);
							String pValueItemId = 
									property.getPropertyDefinition().getUuid() 
									+ "_" + pValue;
							propertyTree.addItem(
									new Object[] {
											null,
											pValue,
											createCheckBox(property, pValue)
									},
									pValueItemId);	
							
							propertyTree.setParent(
									pValueItemId, property.getPropertyDefinition());
							propertyTree.setChildrenAllowed(pValueItemId, false);
							cbAddNewValue.setValue("");
						}
					}
					
				}
			}
		});
	}

	private void initComponents(){
		VerticalLayout mainLayout= new VerticalLayout();
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);
		
		hintText = new Label("Please use the check boxes to select values.");
		mainLayout.addComponent(hintText);
		
		propertyTree = new TreeTable();
		propertyTree.setSelectable(true);

		propertyTree.setSizeFull();
		propertyTree.setPageLength(10);
		propertyTree.setImmediate(true);
		
		propertyTree.focus();
		propertyTree.addShortcutListener(new AbstractField.FocusShortcut(
				propertyTree, KeyCode.ARROW_UP, ModifierKey.CTRL));
		
		propertyTree.addContainerProperty(TreePropertyName.property, String.class, "");
		propertyTree.setColumnHeader(TreePropertyName.property, "Property");
		
		propertyTree.addContainerProperty(TreePropertyName.icon, Resource.class, "");
		
		propertyTree.addContainerProperty(TreePropertyName.value, String.class, "");
		propertyTree.setColumnHeader(TreePropertyName.value, "Value");

		propertyTree.addContainerProperty(TreePropertyName.assigned, CheckBox.class, "");
		propertyTree.setColumnHeader(TreePropertyName.assigned, "Assigned");
		
		propertyTree.setItemCaptionPropertyId(TreePropertyName.property);
		propertyTree.setItemIconPropertyId(TreePropertyName.icon);
		
		propertyTree.setVisibleColumns(
				new Object[] {
						TreePropertyName.property,
						TreePropertyName.value,
						TreePropertyName.assigned
				});

		mainLayout.addComponent(propertyTree);
		
		HorizontalLayout comboBox = new HorizontalLayout();
		comboBox.setSpacing(true);
		
		cbAddNewValue = new ComboBox("Add value");
		cbAddNewValue.setTextInputAllowed(true);
		cbAddNewValue.setNewItemsAllowed(true);
		cbAddNewValue.setImmediate(true);
		cbAddNewValue.addShortcutListener(new AbstractField.FocusShortcut(
				cbAddNewValue, KeyCode.ARROW_DOWN, ModifierKey.CTRL));
		
		comboBox.addComponent(cbAddNewValue);
		
		btAdd = new Button("+");
		btAdd.setClickShortcut(KeyCode.INSERT);
		comboBox.addComponent(btAdd);
		comboBox.setComponentAlignment(btAdd, Alignment.BOTTOM_RIGHT);
		
		mainLayout.addComponent(comboBox);
		
		hintText = new Label("New property values created here exist only for this tag instance! "
				+ "For the creation of new systematic values use the Tag Manager.");
		mainLayout.addComponent(hintText);
		
		HorizontalLayout buttonPanel = new HorizontalLayout();
		buttonPanel.setSpacing(true);
		
		btSave = new Button("Save");
		btSave.setClickShortcut(KeyCode.ENTER, ModifierKey.ALT);
		buttonPanel.addComponent(btSave);
		buttonPanel.setComponentAlignment(btSave, Alignment.MIDDLE_RIGHT);
		
		btCancel = new Button("Cancel");
		btCancel.setClickShortcut(KeyCode.ESCAPE);
		buttonPanel.addComponent(btCancel);
		buttonPanel.setComponentAlignment(btCancel, Alignment.MIDDLE_RIGHT);
		
		mainLayout.addComponent(buttonPanel);
		mainLayout.setComponentAlignment(buttonPanel, Alignment.MIDDLE_RIGHT);
			
		setContent(mainLayout);
		setWidth("30%");
		setHeight("90%");
		setModal(true);
		center();
	}
	
	private Property getProperty(Object selection) {
		while ((selection != null) && !(selection instanceof Property)) {
			selection = propertyTree.getParent(selection);
		}
		return (Property)selection;
	}
	
//	private Set<String> getValues(Object selection){
//
//		while ((selection != null) && !(selection instanceof Property)) {
//			selection = propertyTree.getParent(selection);
//			values.addAll(getProperty(selection).getPropertyDefinition().getPossibleValueList().getPropertyValueList().getValues());
//			values.addAll(getProperty(selection).getPropertyValueList().getValues());
//		}
//		
//		return values;
//	}
	

	public void show() {
		UI.getCurrent().addWindow(this);
	}
	
}