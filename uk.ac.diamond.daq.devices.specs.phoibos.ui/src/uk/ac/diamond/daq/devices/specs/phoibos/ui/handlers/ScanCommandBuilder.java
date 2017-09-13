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

package uk.ac.diamond.daq.devices.specs.phoibos.ui.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.ICommandRunner;
import gda.jython.InterfaceProvider;

public final class ScanCommandBuilder {
	private static final Logger logger = LoggerFactory.getLogger(ScanCommandBuilder.class);

	// TODO Would be nice to replace this with a static scan so we don't need the dummy scannable
	private static final String SCAN_BASE_COMMAND = "scan dummy_a 0 0 1 analyser";

	private final ICommandRunner commandRunner = InterfaceProvider.getCommandRunner();

	String buildScanCommand() {
		// Find out if any extraDetectors are configured, should exist and return a empty string if none are required
		String extraDetectors = commandRunner.evaluateCommand("extraDetectors");
		if (extraDetectors == null) {
			logger.warn("extraDetectors was not in the Jython namespace, no extraDetectors will be used");
			extraDetectors = "";
		}
		logger.debug("Extra detectors configured: {}", extraDetectors);

		// Add the extra detectors to the scan command
		String scanCommand = SCAN_BASE_COMMAND + " " + extraDetectors;
		// Clean trailing white space if extra detectors is empty
		scanCommand = scanCommand.trim();

		logger.debug("Scan command is: {}", scanCommand);
		return scanCommand;
	}
}