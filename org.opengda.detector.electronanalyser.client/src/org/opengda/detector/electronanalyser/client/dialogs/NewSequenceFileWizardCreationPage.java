package org.opengda.detector.electronanalyser.client.dialogs;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

public class NewSequenceFileWizardCreationPage extends WizardNewFileCreationPage {

	public NewSequenceFileWizardCreationPage(String pageName,
			IStructuredSelection selection) {
		super(pageName, selection);
		setTitle("Sequence File");
		setDescription("Creates a new sequence file");
		setFileExtension("seq");
	}

	public NewSequenceFileWizardCreationPage(IStructuredSelection selection) {
		this("New Sequence File", selection);
		
	}
}
