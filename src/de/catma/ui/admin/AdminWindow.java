package de.catma.ui.admin;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import de.catma.repository.db.maintenance.UserManager;

public class AdminWindow extends Window {
	
	private TextField numberOfUsers;
	private Button btRefresh;
	private UserManager userManager;
	
	public AdminWindow() {
		super("Administration");
		userManager = new UserManager();
		initComponents();
		initActions();
	}

	private void initActions() {
		btRefresh.addClickListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				numberOfUsers.setReadOnly(false);
				numberOfUsers.setValue(String.valueOf(userManager.getUserCount()));
				numberOfUsers.setReadOnly(true);
			}
		});
		
	}

	private void initComponents() {
		VerticalLayout content = new VerticalLayout();
		content.setMargin(true);
		content.setSpacing(true);
		numberOfUsers = new TextField("Current users online");
		numberOfUsers.setValue(String.valueOf(userManager.getUserCount()));
		numberOfUsers.setReadOnly(true);
		content.addComponent(numberOfUsers);
		btRefresh = new Button("Refresh");
		content.addComponent(btRefresh);
		setContent(content);
	}

	
}
