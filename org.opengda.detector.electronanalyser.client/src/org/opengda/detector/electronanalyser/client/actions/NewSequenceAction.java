package org.opengda.detector.electronanalyser.client.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.opengda.detector.electronanalyser.client.dialogs.NewSequenceFileWizard;
import org.opengda.detector.electronanalyser.client.sequenceeditor.IRegionDefinitionView;
import org.opengda.detector.electronanalyser.client.views.SequenceView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a handler for creating new sequence file command. It uses an INewWizard to specify the filename to create.
 * @author fy65
 *
 */
public class NewSequenceAction extends AbstractHandler implements IHandler {

	private Logger logger = LoggerFactory.getLogger(NewSequenceAction.class);

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
		} 
		Shell shell = HandlerUtil.getActiveShell(event);
		NewSequenceFileWizard newWizard = new NewSequenceFileWizard();
		IWorkbench workbench = HandlerUtil.getActiveWorkbenchWindow(event)
				.getWorkbench();
		// must initialise because this handler does not go through newWizard extension point.
		newWizard.init(workbench, StructuredSelection.EMPTY);
		WizardDialog wizardDialog = new WizardDialog(shell, newWizard);
		if (wizardDialog.open() == Window.OK) {
			logger.debug("New sequence file is created.");
		} else {
			logger.debug("New sequence file is concelled.");
		}
		return null;
	}
}
