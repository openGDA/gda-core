package uk.ac.gda.devices.hplc.ui.actions;

import java.util.Properties;

import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.intro.config.IIntroAction;

public class SwitchToHplcSetupPerspectiveAction implements IIntroAction {
	@Override
	public void run(IIntroSite site, Properties params) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();

		IIntroManager iMan = workbench.getIntroManager();
		iMan.closeIntro(iMan.getIntro());

		// close all other perspectives that might be open
		window.getActivePage().closeAllPerspectives(true, false);

		// open the BioSAXS setup perspective
		try {
			workbench.showPerspective("uk.ac.gda.devices.hplc.hplcsetupperspective", window);

			IPerspectiveRegistry iPerspectiveRegistry = PlatformUI.getWorkbench().getPerspectiveRegistry();
			iPerspectiveRegistry.setDefaultPerspective("uk.ac.gda.devices.hplc.hplcsetupperspective");

		} catch (WorkbenchException e) {
		}
	}
}