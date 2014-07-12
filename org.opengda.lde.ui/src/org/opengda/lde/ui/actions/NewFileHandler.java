package org.opengda.lde.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.handlers.HandlerUtil;
import org.opengda.lde.ui.dialogs.NewFileWizard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NewFileHandler extends AbstractHandler implements IHandler {

	private Logger logger = LoggerFactory.getLogger(NewFileHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		NewFileWizard newWizard = new NewFileWizard();
		IWorkbench workbench = HandlerUtil.getActiveWorkbenchWindow(event).getWorkbench();
		// must initialise because this handler does not go through newWizard extension point.
		newWizard.init(workbench, StructuredSelection.EMPTY);
		WizardDialog wizardDialog = new WizardDialog(shell, newWizard);
		if (wizardDialog.open() == Window.OK) {
			logger.debug("New LDE file is created.");
		} else {
			logger.debug("New LDE file creation is concelled.");
		}
		return null;
	}
}
