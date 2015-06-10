package de.catma.ui.analyzer;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.catma.repository.db.maintenance.UserManager;

public class PhraseResultHelpWindow extends Window {
		
	public PhraseResultHelpWindow() {
		super("Help");
		initComponents();
		setHeight("500px");
		setWidth("400px");
		center();
		setStyleName("help-windows");
	}

	private void initComponents() {
		VerticalLayout content = new VerticalLayout();
		content.setMargin(true);
		content.setSpacing(true);
		
		Label helpText = new Label("<h4>Tagging search results</h4>" +
				"You can tag the search results in the Kwic-view: " +
				"<p>First select one or more rows and then drag the desired " +
				"Tag from the Tag Type Manager over the Kwic-results.</p>" +
				"<h4>Take a closer look</h4>" +
				"You can jump to the location in the full text by double " +
				"clicking on a row in the Kwic-view." +
				"<h4>Untag search results</h4>" +
				"The \"Results by markup\" tab gives you the opportunity " +
				"to untag markup for selected search results in the Kwic-view.", ContentMode.HTML);
		content.addComponent( helpText);
		setContent(content);
		
	}

	
}