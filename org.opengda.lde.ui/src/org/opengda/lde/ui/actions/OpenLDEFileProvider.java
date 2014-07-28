package org.opengda.lde.ui.actions;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

public class OpenLDEFileProvider extends CommonActionProvider {

	private OpenAction openAction;

	public OpenLDEFileProvider() {
	}

	@Override
	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		ICommonViewerSite viewSite=aSite.getViewSite();
		if (viewSite instanceof ICommonViewerWorkbenchSite) {
			ICommonViewerWorkbenchSite workbenchSite=(ICommonViewerWorkbenchSite)viewSite;
			openAction = new OpenAction(workbenchSite.getPage(), workbenchSite.getSelectionProvider());
		}
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		if (openAction.isEnabled()) {
			actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN,openAction);
		}
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		if (openAction.isEnabled()) {
			menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, openAction);
		}
	}
}
