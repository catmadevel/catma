package de.catma.ui.repository;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

import de.catma.CleaApplication;
import de.catma.core.ExceptionHandler;
import de.catma.core.document.repository.Repository;
import de.catma.core.document.repository.RepositoryManager;

public class RepositoryListView extends VerticalLayout {

	private RepositoryManager repositoryManager;
	private Table repositoryTable;
	private Button openBt;
	
	public RepositoryListView(RepositoryManager repositoryManager) {
		this.repositoryManager = repositoryManager;
		initComponents();
		initActions();
	}

	private void initActions() {
		openBt.addListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				Repository repository = (Repository)repositoryTable.getValue();
				try {
					repository.open();
					((CleaApplication)getApplication()).openRepository(repository);
				} catch (Exception e) {
					ExceptionHandler.log(e);
				}
			}
		});
		
		repositoryTable.addListener(new Table.ValueChangeListener() {
            public void valueChange(ValueChangeEvent event) {
            	openBt.setEnabled(event.getProperty().getValue() != null);
            }
		});
	}

	private void initComponents() {
		repositoryTable = new Table("Available Repositories");
		BeanItemContainer<Repository> container = 
				new BeanItemContainer<Repository>(Repository.class);
		
		for (Repository r : repositoryManager.getRepositories()) {
			container.addBean(r);
		}
		
		repositoryTable.setContainerDataSource(container);

		repositoryTable.setVisibleColumns(new Object[] {"name"});
		repositoryTable.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
		repositoryTable.setSelectable(true);
		repositoryTable.setMultiSelect(false);
		repositoryTable.setPageLength(3);
		repositoryTable.setImmediate(true);
		
		addComponent(repositoryTable);
		setMargin(true);
		setSpacing(true);
		
		
		openBt = new Button("Open");
		openBt.setImmediate(true);
		
		
		addComponent(openBt);
		setComponentAlignment(openBt, Alignment.TOP_RIGHT);
		
		if (container.size() > 0) {
			repositoryTable.setValue(container.getIdByIndex(0));
		}
		else {
			openBt.setEnabled(false);
		}
		
		

	}
	
}