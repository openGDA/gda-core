package org.opengda.lde.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.opengda.lde.ui.views.SampleGroupView;

public class OpenAction extends Action {
	private IWorkbenchPage page;
	private ISelectionProvider selectionProvider;
	private String fileName;

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
				fileName = seqFile.getLocation().toString();
				return true;
			}
		}
		return false;
	}

	@Override
	public void run() {
		if (isEnabled()) {
			IViewPart viewPart = null;
			try {
				viewPart = page.showView(SampleGroupView.ID);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
			if (viewPart != null && fileName != null) {
				if (viewPart instanceof SampleGroupView) {
					SampleGroupView view = (SampleGroupView) viewPart;
					view.refreshTable(fileName, false);
				}
			}
		}
	}
}
