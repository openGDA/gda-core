package org.opengda.detector.electronanalyser.client.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.opengda.detector.electronanalyser.client.ElectronAnalyserClientPlugin;

public class ChangeLayoutHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		String commandParameter = event.getParameter("org.opengda.detector.electronanalyser.client.plotLayoutPreference");
		if (commandParameter != null) {
			ElectronAnalyserClientPlugin.getDefault().getPreferenceStore().setValue(ElectronAnalyserClientPlugin.PLOT_LAYOUT, commandParameter);

			IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
			if (activeWorkbenchWindow != null) {
				IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
				if (page != null) {
					page.resetPerspective();
				}
			}
			// IHandlerService handlerService = (IHandlerService)
			// HandlerUtil.getActivePart(event).getSite().getService(IHandlerService.class);
			// try {
			// handlerService.executeCommand("org.eclipse.ui.window.resetPerspective",
			// null);
			// } catch (Exception e) {
			// e.printStackTrace();
			// }

			// IPerspectiveRegistry perspectiveRegistry =
			// PlatformUI.getWorkbench().getPerspectiveRegistry();
			//
			// IPerspectiveDescriptor sesPerspective =
			// perspectiveRegistry.findPerspectiveWithId(SESPerspective.ID);
			// perspectiveRegistry.revertPerspective(sesPerspective);
		}
		return null;
	}

}
