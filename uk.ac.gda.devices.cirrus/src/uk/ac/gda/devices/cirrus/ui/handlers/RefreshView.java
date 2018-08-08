package uk.ac.gda.devices.cirrus.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

import uk.ac.gda.devices.cirrus.ui.views.CirrusSetup;

public class RefreshView extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// find the view
		IViewPart rgaSetupView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.findView(CirrusSetup.ID);
		// call refresh
		if (rgaSetupView != null) {
			((CirrusSetup) rgaSetupView).refresh();
		}
		return null;
	}
}