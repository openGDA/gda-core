package uk.ac.gda.devices.hidenrga.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

import uk.ac.gda.devices.hidenrga.ui.views.RGASetup;

public class ApplySettingsToRGA extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// find the view
		IViewPart rgaSetupView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.findView(RGASetup.ID);
		// call apply
		if (rgaSetupView != null) {
			((RGASetup) rgaSetupView).apply();
		}
		return null;
	}
}
