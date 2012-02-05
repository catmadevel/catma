package de.catma.ui.repository.wizard;

import org.vaadin.teemu.wizards.Wizard;
import org.vaadin.teemu.wizards.event.WizardCancelledEvent;
import org.vaadin.teemu.wizards.event.WizardCompletedEvent;
import org.vaadin.teemu.wizards.event.WizardProgressListener;
import org.vaadin.teemu.wizards.event.WizardStepActivationEvent;
import org.vaadin.teemu.wizards.event.WizardStepSetChangedEvent;

import com.vaadin.ui.Window;

import de.catma.core.document.source.SourceDocumentInfo;

public class WizardFactory {

	public Window createWizardWindow(SourceDocumentInfo sourceDocumentInfo) {
		final Wizard wizard = new Wizard();
		wizard.addListener(new WizardProgressListener() {
			
			public void wizardCompleted(WizardCompletedEvent event) {}
			
			public void wizardCancelled(WizardCancelledEvent event) {}
			
			public void stepSetChanged(WizardStepSetChangedEvent event) {}
			
			public void activeStepChanged(WizardStepActivationEvent event) {
				wizard.getNextButton().setEnabled(event.getActivatedStep().onAdvance());
				wizard.getBackButton().setEnabled(event.getActivatedStep().onBack());
				((DynamicWizardStep)event.getActivatedStep()).stepActivated();
			}
		});
		WizardResult wizardResult = new WizardResult(sourceDocumentInfo);
		wizard.addStep(
				new LocationPanel(
						new ToggleButtonStateListener(wizard), wizardResult));
		
		wizard.addStep(
				new FileTypePanel(
						new ToggleButtonStateListener(wizard), wizardResult));
		
		wizard.addStep(
				new IndexerOptionsPanel(
						new ToggleButtonStateListener(wizard), wizardResult));
		
		Window wizardWindow = new Window("Add new Source Document");
		wizardWindow.setModal(true);
		wizardWindow.setContent(wizard);
		wizardWindow.setHeight("85%");
		wizardWindow.setWidth("75%");
		return wizardWindow;
	}
}
