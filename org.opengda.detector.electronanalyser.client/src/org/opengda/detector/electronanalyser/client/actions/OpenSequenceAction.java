package org.opengda.detector.electronanalyser.client.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.opengda.detector.electronanalyser.client.sequenceeditor.IRegionDefinitionView;
import org.opengda.detector.electronanalyser.client.sequenceeditor.SequenceView;

public class OpenSequenceAction extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart instanceof IRegionDefinitionView) {
			IRegionDefinitionView regionDefView = (IRegionDefinitionView) activePart;
			if (regionDefView instanceof SequenceView) {
				if (((SequenceView) regionDefView).isDirty()) {
					MessageDialog msgDialog = new MessageDialog(
							HandlerUtil.getActiveShell(event),
							"Unsaved Data",
							null,
							"Current sequence contains unsaved data. Do you want to save them first?",
							MessageDialog.WARNING,
							new String[] { "Yes", "No" }, 0);
					int result = msgDialog.open();
					if (result == 0) {
						((SequenceView) regionDefView).doSave(new NullProgressMonitor());
					}
				}
			}

			FileDialog fileDialog = new FileDialog(
					HandlerUtil.getActiveShell(event));
			String fileName = fileDialog.open();
			if (fileName != null) {
				regionDefView.refreshTable(fileName, false);
			}
		}
		return null;
	}

}
