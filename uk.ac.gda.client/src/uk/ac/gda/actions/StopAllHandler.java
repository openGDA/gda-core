/*-
 * Copyright © 2017 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.actions;

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

import gda.jython.JythonServerFacade;
import gda.jython.PanicStopEvent;
import gda.observable.IObserver;

/**
 * StopAll command implementation that displays a blocking dialog to user until this command process on GDA server is
 * finished.
 *
 * It intended to replace {@link BeamlineHaltHandler} which returns before server process completed, thus allow users to
 * multiple click this.
 */
public class StopAllHandler extends AbstractHandler {

	public static final String ID = "uk.ac.gda.client.StopAllCommand";
	private static final Logger logger = LoggerFactory.getLogger(StopAllHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		logger.debug("Stop All button pressed");

		// latch to block until stop all is complete
		final CountDownLatch latch = new CountDownLatch(1);
		final IObserver anIObserver = (source, arg) -> {
			if (arg instanceof PanicStopEvent) {
				// When the stop finishes decrement the latch
				latch.countDown();
			}
		};
		JythonServerFacade.getInstance().addIObserver(anIObserver);

		try {
			new StopAllProgressMonitorDialog(HandlerUtil.getActiveShell(event)).run(true, false, m -> {
				m.beginTask("Stopping all motors and Jython scannables on the server. Please wait...",
						IProgressMonitor.UNKNOWN);

				// Call halt
				JythonServerFacade.getInstance().beamlineHalt();
				// Block until finished or timeout.
				if (!latch.await(2, TimeUnit.MINUTES)) {
					MessageBox dialog=new MessageBox(HandlerUtil.getActiveShell(event), SWT.ICON_ERROR | SWT.OK);
					dialog.setText("Stop All Failed!");
					dialog.setMessage("GDA server is broken, you need to restart!");
					dialog.open();
				}
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
