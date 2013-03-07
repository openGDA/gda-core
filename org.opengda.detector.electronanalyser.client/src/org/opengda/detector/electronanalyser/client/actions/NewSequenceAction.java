package org.opengda.detector.electronanalyser.client.actions;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.opengda.detector.electronanalyser.client.sequenceeditor.IRegionDefinitionView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewSequenceAction extends AbstractHandler implements IHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(NewSequenceAction.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart instanceof IRegionDefinitionView) {
			IRegionDefinitionView regionDefView = (IRegionDefinitionView) activePart;
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getShell();
			FileDialog fileDialog = new FileDialog(shell);
			String fileName = fileDialog.open();
			File file=null;
			if (fileName != null) file = new File(fileName);
			try {
				if (file != null && !file.exists()) {
					regionDefView.getRegionDefinitionResourceUtil()
							.setFileName(fileName);
					regionDefView.getRegionDefinitionResourceUtil()
							.createSequence();
					regionDefView.getRegionDefinitionResourceUtil().setFileChanged(true);
				} else {
					MessageDialog msgd = new MessageDialog(
							shell,
							"Create a new sequence file",
							null,
							"file: "
									+ fileName
									+ " already exists. Do you want to open it?",
							MessageDialog.WARNING, new String[] { "OK",
									"Cancel" }, 0);
					int returncode = msgd.open();
					if (returncode == 0) {
						regionDefView.getRegionDefinitionResourceUtil()
								.setFileName(fileName);
					}
				}
			} catch (IOException e1) {
				logger.error("Cannot create a new sequence file.", e1);
			} catch (Exception e) {
				logger.error("Cannot create a new sequence", e);
			}

			regionDefView.refreshTable();
		}
		return null;
	}

}
