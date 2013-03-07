package org.opengda.detector.electronanalyser.client.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.opengda.detector.electronanalyser.client.sequenceeditor.IRegionDefinitionView;

public class OpenSequenceAction extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart instanceof IRegionDefinitionView) {
			IRegionDefinitionView regionDefView = (IRegionDefinitionView) activePart;

			FileDialog fileDialog = new FileDialog(
					HandlerUtil.getActiveShell(event));
			String fileName = fileDialog.open();
			if (fileName != null) {
				regionDefView.getRegionDefinitionResourceUtil().setFileName(
						fileName);
				regionDefView.getRegionDefinitionResourceUtil().setFileChanged(true);
				regionDefView.refreshTable();
				
			}
		}
		return null;
	}

}
