package org.opengda.detector.electronanalyser.client.actions;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
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
			File file = new File(fileName);
			try {
				if (!file.exists()) {
					regionDefView.getRegionDefinitionResourceUtil()
							.setFileName(fileName);
					regionDefView.getRegionDefinitionResourceUtil()
							.createSequence();
				} else {
					MessageBox msgbox = new MessageBox(
							HandlerUtil.getActiveShell(event), SWT.ICON_WARNING
									| SWT.OK | SWT.CANCEL);
					msgbox.setText("Create a new sequence file");
					msgbox.setMessage("file: " + fileName
							+ " already exists. Do you want to open it?");
					int returncode = msgbox.open();
					if (returncode == SWT.OK) {
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
