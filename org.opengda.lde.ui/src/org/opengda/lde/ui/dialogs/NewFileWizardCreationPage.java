package org.opengda.lde.ui.dialogs;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

public class NewFileWizardCreationPage extends WizardNewFileCreationPage {

	public NewFileWizardCreationPage(String pageName,IStructuredSelection selection) {
		super(pageName, selection);
		setTitle("LDE File");
		setDescription("Creates a new LDE file");
		setFileExtension("lde");
	}

	public NewFileWizardCreationPage(IStructuredSelection selection) {
		this("New File", selection);
		
	}
}
