package de.catma.ui.tabbedview;

import java.util.Iterator;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.CloseHandler;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;

public class TabbedView extends VerticalLayout implements CloseHandler, Iterable<Component> {
	
	private TabSheet tabSheet;
	private Label noOpenTabsLabel;
	
	public TabbedView(String noOpenTabsText) {
		initComponents(noOpenTabsText);
	}

	private void initComponents(String noOpenTabsText) {
		tabSheet = new TabSheet();
		tabSheet.setSizeFull();
		noOpenTabsLabel = 
				new Label(noOpenTabsText);
		
		noOpenTabsLabel.setSizeFull();
		setMargin(true);
		addComponent(noOpenTabsLabel);
		setComponentAlignment(noOpenTabsLabel, Alignment.MIDDLE_CENTER);
		
		addComponent(tabSheet);
		tabSheet.hideTabs(true);
		tabSheet.setHeight("0px");	
		tabSheet.setCloseHandler(this);
		
		setSizeFull();
	}
	
	public void onTabClose(TabSheet tabsheet, Component tabContent) {
		
		tabsheet.removeComponent(tabContent);
		((ClosableTab)tabContent).close();
		
		// workaround for http://dev.vaadin.com/ticket/7686
		try {
			Thread.sleep(5);
		} catch (InterruptedException ex) {
	            //do nothing 
	    }
		
		if (tabsheet.getComponentCount() == 0) {
			 //setVisible(false) doesn't work here because of out of sync errors
			tabSheet.hideTabs(true);
			tabSheet.setHeight("0px");
			addComponent(noOpenTabsLabel, 0);
			noOpenTabsLabel.setVisible(true);
			setMargin(true);
		}
	}
	
	protected Tab addTab(Component component, String caption) {
		
		Tab tab = tabSheet.addTab(component, caption);
		
		tab.setClosable(true);
		tabSheet.setSelectedTab(tab.getComponent());
		
		if (tabSheet.getComponentCount() != 0) {
			noOpenTabsLabel.setVisible(false);
			removeComponent(noOpenTabsLabel);
			setMargin(false);
			tabSheet.hideTabs(false);
			tabSheet.setSizeFull();
		}
		
		return tab;
	}
	
	protected void addClosableTab(ClosableTab closableTab, String caption) {
		addTab(closableTab, caption).setClosable(true);
	}

	public Iterator<Component> iterator() {
		return tabSheet.getComponentIterator();
	}
	
	protected void setSelectedTab(Component tabContent) {
		tabSheet.setSelectedTab(tabContent);
	}
}