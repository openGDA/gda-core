package org.opengda.lde.ui.dialogs;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.opengda.lde.ui.views.SampleGroupView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewFileWizard extends Wizard implements INewWizard {
	private NewFileWizardCreationPage newFilePage;
	private IStructuredSelection selection;
	private IWorkbench workbench;
	private Logger logger = LoggerFactory.getLogger(NewFileWizard.class);

	public NewFileWizard() {
		setWindowTitle("New File");
	}
	@Override
	public void addPages() {
		newFilePage=new NewFileWizardCreationPage(selection);
		addPage(newFilePage);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench=workbench;
		this.selection=selection;
	}

	@Override
	public boolean performFinish() {
		IWorkbenchPart activePart = workbench.getActiveWorkbenchWindow().getActivePage().findView(SampleGroupView.ID);
		SampleGroupView view = null;
		if (activePart instanceof SampleGroupView) {
			view = (SampleGroupView) activePart;
		}
		IFile file = newFilePage.createNewFile();
		if (file != null ) {
			if (view != null) {
				view.refreshTable(file.getLocation().toString());
			} else {
				logger.error("Cannot find the active part in new LDE file wizard.");
			}
			return true;
		} else {
			return false;
		}
	}
}
