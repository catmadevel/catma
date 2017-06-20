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
package de.catma.ui.repository;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.ClassResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;

import de.catma.document.Corpus;
import de.catma.document.repository.Repository;
import de.catma.ui.CatmaApplication;
import de.catma.ui.admin.AdminWindow;
import de.catma.ui.repository.RepositoryHelpWindow;
import de.catma.ui.tabbedview.ClosableTab;
import de.catma.user.Permission;
import de.catma.user.Role;


public class RepositoryView extends VerticalLayout implements ClosableTab {
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	private Repository repository;
	private PropertyChangeListener exceptionOccurredListener;
	private SourceDocumentPanel sourceDocumentPanel;
	private CorpusPanel corpusPanel;
	private TagLibraryPanel tagLibraryPanel;
	private boolean init = false;
	private Button btReload;
	private Button btAdmin;
	private Button btHelp;
	
	RepositoryHelpWindow repositoryHelpWindow = new RepositoryHelpWindow();
	
	public RepositoryView(Repository repository) {
		this.repository = repository;

		exceptionOccurredListener = new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent evt) {
				if (UI.getCurrent() !=null) {
					((CatmaApplication)UI.getCurrent()).showAndLogError(
						"Repository Error!", (Throwable)evt.getNewValue());
				}
				else {
					logger.log(
						Level.SEVERE, "repository error", 
						(Throwable)evt.getNewValue());
				}
			}
		};
		
		
	}

	@Override
	public void attach() {
		super.attach();
		if (!init) {
			initComponents();
			initActions();
			this.repository.addPropertyChangeListener(
					Repository.RepositoryChangeEvent.exceptionOccurred, 
					exceptionOccurredListener);
			init = true;
		}
		
	}

	private void initActions() {
		btReload.addClickListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				try {
					Corpus corpus = corpusPanel.getSelectedCorpus();
					corpusPanel.setSelectedCorpus(null);
					repository.reload();
					corpusPanel.setSelectedCorpus(corpus);
					
				} catch (IOException e) {
					((CatmaApplication)UI.getCurrent()).showAndLogError(
							"Error reloading repository!", e);
				}
			}
		});
		
		btAdmin.addClickListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
				AdminWindow adminWindow = new AdminWindow();
				
				UI.getCurrent().addWindow(adminWindow);
			}
		});
		
		
		btHelp.addClickListener(new ClickListener() {
			
			public void buttonClick(ClickEvent event) {
								
				if(repositoryHelpWindow.getParent() == null){
					UI.getCurrent().addWindow(repositoryHelpWindow);
				} else {
					UI.getCurrent().removeWindow(repositoryHelpWindow);
				}
				
			}
		});
		
		
	}

	private void initComponents() {
		setSizeFull();
		this.setMargin(new MarginInfo(false, true, true, true));
		this.setSpacing(true);
		
		Component documentsLabel = createDocumentsLabel();
		addComponent(documentsLabel);
		documentsLabel.setHeight("50");
		documentsLabel.setStyleName("help-padding-fix");
		VerticalSplitPanel splitPanel = new VerticalSplitPanel();
		splitPanel.setStyleName("repository-panels");
		splitPanel.setSplitPosition(65);
		
		Component documentsManagerPanel = createDocumentsManagerPanel();
		splitPanel.addComponent(documentsManagerPanel);
		
		tagLibraryPanel = new TagLibraryPanel(
				repository.getTagManager(), repository);
		splitPanel.addComponent(tagLibraryPanel);
		
		addComponent(splitPanel);
		setExpandRatio(splitPanel, 1f);
	}

	

	private Component createDocumentsManagerPanel() {
		
		HorizontalSplitPanel documentsManagerPanel = new HorizontalSplitPanel();
		documentsManagerPanel.setSplitPosition(30);
		documentsManagerPanel.setSizeFull();
		
		sourceDocumentPanel = new SourceDocumentPanel(repository);
		
		corpusPanel = new CorpusPanel(repository, new ValueChangeListener() {
			
			public void valueChange(ValueChangeEvent event) {
				Object value = event.getProperty().getValue();
				sourceDocumentPanel.setSourceDocumentsFilter((Corpus)value);
			}		
		});

		documentsManagerPanel.addComponent(corpusPanel);
		documentsManagerPanel.addComponent(sourceDocumentPanel);
		
		return documentsManagerPanel;
	}

	private Component createDocumentsLabel() {
		HorizontalLayout labelLayout = new HorizontalLayout();
		labelLayout.setWidth("100%");
		labelLayout.setSpacing(true);
		
		Label documentsLabel = new Label("Document Manager");
		documentsLabel.addStyleName("bold-label");
		
		labelLayout.addComponent(documentsLabel);
		labelLayout.setExpandRatio(documentsLabel, 0.5f);
		btAdmin = new Button("Admin");
		btAdmin.setVisible(repository.getUser().hasPermission(Permission.adminwindow));
		
		labelLayout.addComponent(btAdmin);
		labelLayout.setComponentAlignment(btAdmin, Alignment.MIDDLE_RIGHT);
		
		btReload = new Button(FontAwesome.REFRESH); 
		labelLayout.addComponent(btReload);
		labelLayout.setComponentAlignment(btReload, Alignment.MIDDLE_RIGHT);
				
		btHelp = new Button(FontAwesome.QUESTION_CIRCLE);
		btHelp.addStyleName("help-button");
		
		labelLayout.addComponent(btHelp);
		labelLayout.setComponentAlignment(btHelp, Alignment.MIDDLE_RIGHT);
		
		return labelLayout;
	}

	public Repository getRepository() {
		return repository;
	}
	
	public void close() {
		this.repository.removePropertyChangeListener(
				Repository.RepositoryChangeEvent.exceptionOccurred, 
				exceptionOccurredListener);
		
		this.corpusPanel.close();
		this.sourceDocumentPanel.close();
		this.tagLibraryPanel.close();
		
		// repository is closed by the RepositoryManager from RepositoryManagerView
	}
	
	public void addClickshortCuts() { /* noop*/	}
	
	public void removeClickshortCuts() { /* noop*/ }

}


