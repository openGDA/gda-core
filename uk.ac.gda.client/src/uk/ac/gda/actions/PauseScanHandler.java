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

			logger.debug("Pause/Resume Scan button pressed");

			boolean scanRunning = InterfaceProvider.getScanStatusHolder().getScanStatus() == Jython.RUNNING;
			boolean scriptRunning = InterfaceProvider.getScriptController().getScriptStatus() == Jython.RUNNING;
			boolean scanPaused = InterfaceProvider.getScanStatusHolder().getScanStatus() == Jython.PAUSED;
			boolean scriptPaused = InterfaceProvider.getScriptController().getScriptStatus() == Jython.PAUSED;
			boolean scanIdle = InterfaceProvider.getScanStatusHolder().getScanStatus() == Jython.PAUSED;

			boolean somethingPaused = false;

			if (scanIdle) {
				// then we are only thinking about a script here
				if (scriptPaused) {
					InterfaceProvider.getScriptController().resumeCurrentScript();
				} else if (scriptRunning && !somethingPaused) {
					InterfaceProvider.getScriptController().pauseCurrentScript();
					somethingPaused = true;
				}
			} else {
				// look at the scan status first
				if (scanPaused) {
					InterfaceProvider.getCurrentScanController().resumeCurrentScan();
					if (scriptPaused) {
						InterfaceProvider.getScriptController().resumeCurrentScript();
					}
				} else if (scanRunning) {
					InterfaceProvider.getCurrentScanController().pauseCurrentScan();
					InterfaceProvider.getScriptController().pauseCurrentScript();
					somethingPaused = true;
				}

			}

			return !somethingPaused;
		} catch (Exception ne) {
			throw new ExecutionException(ne.getMessage(), ne);
		}
	}
}
