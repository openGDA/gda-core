package org.opengda.detector.electronanalyser.client.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.opengda.detector.electronanalyser.client.sequenceeditor.IRegionDefinitionView;
import org.opengda.detector.electronanalyser.client.viewextensionfactories.SequenceViewExtensionFactory;

public class OpenAction extends Action {
	private IWorkbenchPage page;
	private ISelectionProvider selectionProvider;
	private String seqFileName;

	public OpenAction(IWorkbenchPage page, ISelectionProvider iSelectionProvider) {
		setText("Open");
		this.page = page;
		this.selectionProvider = iSelectionProvider;
	}

	@Override
	public boolean isEnabled() {
		ISelection selection=selectionProvider.getSelection();
		if (!selection.isEmpty()){
			IStructuredSelection sSelection=(IStructuredSelection)selection;
			if (sSelection.size()==1 && sSelection.getFirstElement() instanceof IFile){
				IFile seqFile = (IFile) sSelection.getFirstElement();
				seqFileName = seqFile.getLocation().toString();
				return true;
			}
		}
		return false;
	}

	@Override
	public void run() {
		if (isEnabled()) {
			IViewPart seqViewPart = null;
			try {
				seqViewPart = page.showView(SequenceViewExtensionFactory.ID);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
			if (seqViewPart != null && seqFileName != null) {
				if (seqViewPart instanceof IRegionDefinitionView) {
					IRegionDefinitionView regionDefinitionView = (IRegionDefinitionView) seqViewPart;
					regionDefinitionView.refreshTable(seqFileName, false);
				}
			}
		}
	}
}
