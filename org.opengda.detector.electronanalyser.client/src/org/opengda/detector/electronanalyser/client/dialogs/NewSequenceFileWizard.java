package org.opengda.detector.electronanalyser.client.dialogs;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.opengda.detector.electronanalyser.client.sequenceeditor.IRegionDefinitionView;
import org.opengda.detector.electronanalyser.client.sequenceeditor.SequenceViewExtensionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewSequenceFileWizard extends Wizard implements INewWizard {
	private NewSequenceFileWizardCreationPage newSequenceFilePage;
	private IStructuredSelection selection;
	private IWorkbench workbench;
	private Logger logger = LoggerFactory.getLogger(NewSequenceFileWizard.class);

	public NewSequenceFileWizard() {
		setWindowTitle("New Sequence File");
	}
	@Override
	public void addPages() {
		newSequenceFilePage=new NewSequenceFileWizardCreationPage(selection);
		addPage(newSequenceFilePage);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench=workbench;
		this.selection=selection;
	}

	@Override
	public boolean performFinish() {
		IWorkbenchPart activePart = workbench.getActiveWorkbenchWindow().getActivePage().findView(SequenceViewExtensionFactory.ID);
		IRegionDefinitionView regionDefView = null;
		if (activePart instanceof IRegionDefinitionView) {
			regionDefView = (IRegionDefinitionView) activePart;
		}
		IFile file = newSequenceFilePage.createNewFile();
		if (file != null ) {
			if (regionDefView != null) {
				regionDefView.refreshTable(file.getLocation().toString(), true);
			} else {
				logger.error("Cannot find the active part in new sequence file wizard.");
			}
			return true;
		} else {
			return false;
		}
	}
}
