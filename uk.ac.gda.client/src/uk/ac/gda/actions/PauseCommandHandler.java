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
			JythonStatus scanStatus = InterfaceProvider.getScanStatusHolder().getScanStatus();
			JythonStatus scriptStatus = InterfaceProvider.getScriptController().getScriptStatus();

			logger.debug("Pause/Resume button pressed, scanStatus={}, scriptStatus={}", scanStatus, scriptStatus);

			switch (scanStatus) {
				case IDLE:
					// Only a script could be running - set it to whatever it currently isn't
					if (scriptStatus == JythonStatus.PAUSED) {
						logger.trace("Resuming script");
						InterfaceProvider.getScriptController().resumeCurrentScript();
					} else if (scriptStatus == JythonStatus.RUNNING) {
						logger.trace("Pausing script");
						InterfaceProvider.getScriptController().pauseCurrentScript();
					}
					break;
				case PAUSED:
					// Resume scan. If script is paused, leave it paused as the current scan could be being run independently
					// while the script is paused in the background.
					logger.trace("Resuming scan");
					InterfaceProvider.getCurrentScanController().resumeCurrentScan();
					break;
				case RUNNING:
					// Only pause scan - if it's part of a script, the script will wait anyway
					logger.trace("Pausing scan");
					InterfaceProvider.getCurrentScanController().pauseCurrentScan();
					break;
				default:
					logger.warn("PauseScanHandler:execute, unexpected scanStatus {}", scanStatus);
			}
			return null; // as per AbstractHandler#execute javadoc
		} catch (Exception e) {
			// log and throw because the exception is caught somewhere in the RCP framework
			// and the error is lost.
			logger.error("Error pausing current scan/script", e);
			throw new ExecutionException("Error pausing scan", e);
		}
	}
}
