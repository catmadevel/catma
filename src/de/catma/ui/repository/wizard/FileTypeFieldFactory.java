package de.catma.ui.repository.wizard;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.ComboBox;

import de.catma.document.source.FileType;
import de.catma.ui.field.FieldConnector;
import de.catma.ui.field.FieldConnectorFactory;
import de.catma.ui.field.FieldMapper;
import de.catma.ui.field.GeneratorFieldFactory;
import de.catma.ui.field.ReadonlyFieldGenerator;
import de.catma.ui.repository.wizard.FileTypePanel.FileTypeCharsetValueChangeListener;

public class FileTypeFieldFactory extends GeneratorFieldFactory {
	
	static class FileTypeCharsetFieldConnectorFactory implements FieldConnectorFactory<ComboBox> {
		@Override
		public FieldConnector createFieldConnector(final ComboBox target, Object itemId) {
			return new FieldConnector() {
				
				@Override
				public void valueChange(ValueChangeEvent event) {
					FileType fileType = (FileType) event.getProperty().getValue();
					target.setEnabled(fileType.isCharsetSupported());
					
				}
			};
		}
	}
	private FieldMapper fieldMapper;
	
	public FileTypeFieldFactory(FileTypeCharsetValueChangeListener fileTypeCharsetValueChangeListener) {
		this.fieldMapper = new FieldMapper();
		FileTypeCharsetFieldConnectorFactory fileTypeCharsetFieldConnectorFactory =
				new FileTypeCharsetFieldConnectorFactory();
		this.tableFieldGenerators.put("sourceDocumentInfo.techInfoSet.fileName", new ReadonlyFieldGenerator());
		this.tableFieldGenerators.put(
			"sourceDocumentInfo.techInfoSet.fileType", 
			new FileTypeFieldGenerator(fieldMapper, fileTypeCharsetFieldConnectorFactory, fileTypeCharsetValueChangeListener));
		this.tableFieldGenerators.put(
			"sourceDocumentInfo.techInfoSet.charset",
			new CharsetFieldGenerator(fieldMapper, fileTypeCharsetFieldConnectorFactory, fileTypeCharsetValueChangeListener));
	}

}
