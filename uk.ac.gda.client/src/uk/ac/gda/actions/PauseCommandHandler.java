/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.InterfaceProvider;
import gda.jython.JythonServerStatus;
import gda.jython.JythonStatus;

/**
 * Handler to respond to the GUI pause button being pressed.
 *
 * Pauses current scan or script depending on what is running.
 */
public class PauseCommandHandler extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(PauseCommandHandler.class);

	/**
	 * Pauses or resumes the scan or script depending on current state of Jython Server.
	 *
	 * @return null as specified by {@link AbstractHandler#execute(ExecutionEvent)}.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			JythonServerStatus status = InterfaceProvider.getJythonServerStatusProvider().getJythonServerStatus();
			logger.debug("Pause/Resume button pressed, scan status={}, scriptStatus={}", status.scanStatus, status.scriptStatus);

			switch (status.scanStatus) {
				case IDLE:
					// Only a script could be running - set it to whatever it currently isn't
					if (status.scriptStatus == JythonStatus.PAUSED) {
						InterfaceProvider.getScriptController().resumeCurrentScript();
					} else if (status.scriptStatus == JythonStatus.RUNNING) {
						InterfaceProvider.getScriptController().pauseCurrentScript();
					}
					break;
				case PAUSED:
					// Resume scan. If script is paused, leave it paused as the current scan could be being run independently
					// while the script is paused in the background.
					InterfaceProvider.getCurrentScanController().resumeCurrentScan();
					break;
				case RUNNING:
					// Only pause scan - if it's part of a script, the script will wait anyway
					InterfaceProvider.getCurrentScanController().pauseCurrentScan();
					break;
				default:
					logger.warn("PauseScanHandler:execute, unexpected scanStatus {}", status.scanStatus);
			}
			return null; // as per AbstractHandler#execute javadoc
		} catch (Exception ne) {
			throw new ExecutionException("Error pausing scan", ne);
		}
	}
}
