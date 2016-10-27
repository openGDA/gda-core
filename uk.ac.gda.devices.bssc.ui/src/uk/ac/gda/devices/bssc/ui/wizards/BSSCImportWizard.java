package uk.ac.gda.devices.bssc.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class BSSCImportWizard extends Wizard implements IImportWizard {

	BSSCImportWizardPage mainPage;

	public BSSCImportWizard() {
		super();
	}

	@Override
	public boolean performFinish() {
		IFile file = mainPage.createNewFile();
		if (file == null) {
			return false;
		}
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("File Import Wizard"); // NON-NLS-1
		setNeedsProgressMonitor(true);
		mainPage = new BSSCImportWizardPage("BioSAXS Experiment Import", selection); // NON-NLS-1
	}

	@Override
	public void addPages() {
		super.addPages();
		addPage(mainPage);
	}
}