package uk.ac.gda.beamline.synoptics.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;
import gda.jython.PanicStopEvent;
import gda.observable.IObserver;
/**
 * StopAll command implementation that displays a blocking dialog to user until this command process on GDA server is
 * finished. It supports
 *
 */
public class StopAllHandlerAllowExemption extends AbstractHandler {

	public static final String ID = "uk.ac.gda.beamline.synoptics.StopAllCommand";
	private static final Logger logger = LoggerFactory.getLogger(StopAllHandlerAllowExemption.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		logger.debug("Stop All button pressed");

		// latch to block until stop all is complete
		final CountDownLatch latch = new CountDownLatch(2);
		final IObserver anIObserver = (source, arg) -> {
			if (arg instanceof PanicStopEvent) {
				// When the stop finishes decrement the latch
				latch.countDown();
			}
		};
		JythonServerFacade.getInstance().addIObserver(anIObserver);

		try {
			new StopAllProgressMonitorDialog(HandlerUtil.getActiveShell(event)).run(true, false, m -> {
				m.beginTask("Stopping server processes. Please wait...",
						IProgressMonitor.UNKNOWN);

				// Call halt: current scan, queue, motors in finder, not Jython Scannables (see command_server bean definition)
				m.subTask("Stop active scan and queued processes if any ......");
				JythonServerFacade.getInstance().abortCommands();
				m.subTask("Stop Jython scannables ......");
				stopJythonScannablesExceptExcluded(latch);
				// Block until finished or timeout.
				if (!latch.await(2, TimeUnit.MINUTES)) {
					MessageBox dialog=new MessageBox(HandlerUtil.getActiveShell(event), SWT.ICON_ERROR | SWT.OK);
					dialog.setText("Stop All Failed!");
					dialog.setMessage("GDA server is broken, you need to restart!");
					dialog.open();
				}
				InterfaceProvider.getTerminalPrinter().print("!!! Stop-all complete");
				logger.info("... Stop complete");

			});
		} catch (InvocationTargetException | InterruptedException e) {
			logger.error("Stop all command failed to complete.", e);
			throw new ExecutionException("Stop all command failed to complete.", e);
		} finally {
			JythonServerFacade.getInstance().deleteIObserver(anIObserver);
		}

		// Must return null
		return null;
	}

	//run script to workaround issue DAQ-1173
	//expecting a stopJythonScannables.py script to be provided by beamline for customisation on excluded scannables.
	//please see example in i21-config for stopJythonScannables.py
	private void stopJythonScannablesExceptExcluded(CountDownLatch latch) {
		String locateScript = JythonServerFacade.getInstance().locateScript("stopJythonScannables.py");
		if (locateScript != null) {
			JythonServerFacade.getInstance().runScript(locateScript);
		} else {
			logger.info("'stopJythonScannables.py' script file is not provided in this beamline to customise 'Stop All'" );
		}
		latch.countDown();
	}

	private static class StopAllProgressMonitorDialog extends ProgressMonitorDialog {

		public StopAllProgressMonitorDialog(Shell parent) {
			super(parent);
		}

		@Override
		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			shell.setText("Stop All Operation in Progress");
		}
	}

}
