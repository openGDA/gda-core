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

import gda.jython.InterfaceProvider;
import gda.jython.Jython;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PauseScanHandler extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(PauseScanHandler.class);

	/**
	 * Returns if the button should be checked (ie something was pause), true or if there was nothing to pause or a
	 * resume happened then false.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			int   scanStatus = InterfaceProvider.getScanStatusHolder().getScanStatus();
			int scriptStatus = InterfaceProvider.getScriptController().getScriptStatus();

			logger.debug("Pause/Resume Scan button pressed, scan status={}, scriptStatus={}", scanStatus, scriptStatus);

			boolean somethingPaused = false;

			switch (scanStatus) {
				case Jython.IDLE: // then we are only thinking about a script here
					if (scriptStatus == Jython.PAUSED) {
						InterfaceProvider.getScriptController().resumeCurrentScript();
					} else
					if (scriptStatus == Jython.RUNNING) {
						InterfaceProvider.getScriptController().pauseCurrentScript();
						somethingPaused = true;
					}
					break;

				case Jython.PAUSED:
					InterfaceProvider.getCurrentScanController().resumeCurrentScan();
					if (scriptStatus == Jython.PAUSED) {
						InterfaceProvider.getScriptController().resumeCurrentScript();
					}
					break;

				case Jython.RUNNING:
					InterfaceProvider.getCurrentScanController().pauseCurrentScan();
					// TODO: Investigate whether script should always be paused, or only paused when running.
					InterfaceProvider.getScriptController().pauseCurrentScript();
					somethingPaused = true;
					break;

				default:
					logger.warn("PauseScanHandler:execute, scanStatus neither IDLE, PAUSED nor RUNNING!");
			}

			return somethingPaused;
		} catch (Exception ne) {
			throw new ExecutionException("Error pausing scan", ne);
		}
	}
}
