/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import org.dawnsci.mapping.ui.IMapClickEvent;
import org.eclipse.dawnsci.plotting.api.axis.ClickEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.JythonServerFacade;

public class StageMoveHandler implements EventHandler {

	private static final Logger logger = LoggerFactory.getLogger(StageMoveHandler.class);

	@Override
	public void handleEvent(Event event) {
		final IMapClickEvent mapClickEvent = (IMapClickEvent) event.getProperty("event");
		// moveTo is only handled for double-clicks
		if (mapClickEvent.isDoubleClick()) {
			final ClickEvent clickEvent = mapClickEvent.getClickEvent();

			// moveTo needs to run in UI thread as it displays a dialog asking for confirmation
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					moveTo(clickEvent.getxValue(), clickEvent.getyValue());
				}
			});
		}
	}

	// FIXME This should be replaced by a method using the messaging to ask the server for a move.
	@Deprecated
	private void moveTo(final double xLocation, final double yLocation) {
		logger.debug("moveTo({}, {})", xLocation, yLocation);
		// Dialog to confirm move
		// TODO Should be able to get this via injection in e4
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

		// create a dialog with ok and cancel buttons and a question icon
		MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
		dialog.setText("Go Here?");
		dialog.setMessage("Do you want to move the stage to:\n"
				+ MappingScanRequestHandler.X_AXIS_NAME + " = " + xLocation +"\n"
				+ MappingScanRequestHandler.Y_AXIS_NAME + " = " + yLocation);

		// Open dialog and await user selection
		int returnCode = dialog.open();
		// If user chose to cancel return without moving
		if (returnCode == SWT.CANCEL) return;

		// Get the Jython Server facade to do the move
		// FIXME This should be replaced by a activeMQ message to move once that is available
		JythonServerFacade jsf = JythonServerFacade.getInstance();

		// Do move
		// Move x
		String command = MappingScanRequestHandler.X_AXIS_NAME + ".asynchronousMoveTo(" + xLocation + ")";
		jsf.runCommand(command);
		// Move y
		command = MappingScanRequestHandler.Y_AXIS_NAME + ".asynchronousMoveTo(" + yLocation + ")";
		jsf.runCommand(command);
	}

}
