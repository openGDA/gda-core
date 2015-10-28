package org.opengda.lde.ui.actions;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.opengda.lde.ui.views.SampleGroupView;


public class OpenFileHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart instanceof SampleGroupView) {
			SampleGroupView view = (SampleGroupView) activePart;
			FileDialog fileDialog = new FileDialog(HandlerUtil.getActiveShell(event));
			String fileName = fileDialog.open();
			if (fileName != null) {
				view.refreshTable(fileName);
			}
		}
		return null;
	}

}
