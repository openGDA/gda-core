package org.opengda.detector.electronanalyser.client.actions;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.opengda.detector.electronanalyser.client.sequenceeditor.IRegionDefinitionView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * handler for creating new sequence file command. It uses FileDialog to specify filename on the file system to create.
 * @author fy65
 *
 */
public class NewSequenceFileHandler extends AbstractHandler implements IHandler {

	private Logger logger = LoggerFactory
			.getLogger(NewSequenceFileHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart instanceof IRegionDefinitionView) {
			IRegionDefinitionView regionDefView = (IRegionDefinitionView) activePart;
			Shell shell = HandlerUtil.getActiveShell(event);
			FileDialog fileDialog = new FileDialog(shell);
			String fileName = fileDialog.open();
			File file = null;
			if (fileName != null) {
				file = new File(fileName);
			}
			if (file != null && !file.exists()) {
				regionDefView.refreshTable(fileName, true);
			} else {
				MessageDialog msgd = new MessageDialog(shell,
						"Create a new sequence file", null, "file: " + fileName
								+ " already exists. Do you want to open it?",
						MessageDialog.WARNING, new String[] { "OK", "Cancel" },
						0);
				int returncode = msgd.open();
				if (returncode == 0) {
					regionDefView.refreshTable(fileName, false);
				}
			}
		}
		return null;
	}

}
