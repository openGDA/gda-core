package org.opengda.detector.electronanalyser.client.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.opengda.detector.electronanalyser.client.sequenceeditor.IRegionDefinitionView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenSequenceHandler extends AbstractHandler implements IHandler {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(OpenSequenceHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart instanceof IRegionDefinitionView regionDefView) {
			FileDialog fileDialog = new FileDialog(HandlerUtil.getActiveShell(event));
			String filterPath = regionDefView.getRegionDefinitionResourceUtil().getTgtDataRootPath();
			fileDialog.setFilterPath(filterPath);
			fileDialog.setOverwrite(true);
			fileDialog.setFilterExtensions(new String[] {"*.seq"});
			String fileName = fileDialog.open();
			if (fileName != null) {
				regionDefView.refreshTable(fileName, false);
			}
		}
		return null;
	}

}
