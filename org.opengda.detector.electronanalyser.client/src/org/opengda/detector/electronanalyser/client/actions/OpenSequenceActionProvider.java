package org.opengda.detector.electronanalyser.client.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.OpenSystemEditorAction;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.opengda.detector.electronanalyser.client.sequenceeditor.IRegionDefinitionView;
import org.opengda.detector.electronanalyser.client.viewextensionfactories.SequenceViewExtensionFactory;

public class OpenSequenceActionProvider extends CommonActionProvider {

	private OpenAction openAction;
	public OpenSequenceActionProvider() {
	}

	@Override
	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		openAction = new OpenAction(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage());
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN,
				openAction);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, openAction);
	}

	public class OpenAction extends OpenSystemEditorAction {
		private IWorkbenchPage page;

		public OpenAction(IWorkbenchPage page) {
			super(page);
			this.page = page;
		}

		@Override
		public void run() {
			String seqFileName = null;
			ISelection selection2 = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage().getSelection();
			if (selection2 instanceof IStructuredSelection) {
				IStructuredSelection structSel = (IStructuredSelection) selection2;
				if (structSel.getFirstElement() instanceof IFile) {
					IFile seqFile = (IFile) structSel.getFirstElement();
					seqFileName = seqFile.getLocation().toString();
				}
			}
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
