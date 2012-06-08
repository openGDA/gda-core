/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.detector.mar345;

import gda.configuration.properties.LocalProperties;
import gda.util.exceptionUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MAR345 Controller Class.
 */
public class Mar345Controller extends Observable implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(Mar345Controller.class);

	/*
	 * This controller class monitors the mar status using Mar345StatusReader and an internally maintained step
	 * variable.
	 */

	private Thread statusMonitor;

	private String commandFileName = "mar.com";

	private BufferedWriter commandFile = null;

	private String statusFileName = "mar.message";

	private Mar345StatusReader statusReader = null;

	/**
	 * Controller step keeps track of the state of the mar.
	 */
	private enum ControllerStep {
		/**
		 * mar is idle, so wait for new command to be sent
		 */
		IDLE,
		/**
		 * command has been sent, so wait for Mar345StatusReader to return busy
		 */
		COMMAND_SENT,
		/**
		 * mar has started executing command, so wait fixed amount of time (about 25s). This is because the status
		 * returned by Mar345StatusReader which reads mar.message is unreliable for this amount of time (flips between 0
		 * and 1)
		 */
		BUSY_1,
		/**
		 * mar has been running for about 25s, so wait for mar to become idle. This is because, after this amount of
		 * time, the status returned by Mar345StatusReader is reliable (always 1), so just wait for status of 0
		 */
		BUSY_2,
		/**
		 * unable to read status
		 */
		UNKNOWN
	}

	private ControllerStep step = ControllerStep.UNKNOWN;
	private long statusTimer = 0;
	private long busy1Delay = 30000; // 30s to be safe

	/**
	 * Constructor.
	 */
	public Mar345Controller() {

		setUp(LocalProperties.get("gda.device.detector.mar345.mxLogDir"));
	}

	/**
	 * Constructor passing local of mar log directory
	 * 
	 * @param logDir
	 *            mar log directory
	 */
	public Mar345Controller(String logDir) {
		setUp(logDir);
	}

	/**
	 * Sets up status file and command file names, and status monitor.
	 * 
	 * @param logDir
	 *            mar log directory
	 */
	private void setUp(String logDir) {
		try {
			if (!logDir.endsWith("/")) {
				logDir = logDir + "/";
			}
		} catch (NullPointerException ex) {
			exceptionUtils.logException(logger, "Error accessing mar directory path in Mar345Controller", ex);
			return;
		}
		statusFileName = logDir + statusFileName;
		commandFileName = logDir + commandFileName;

		// Create new status file reader - delete existing file first
		try{
			(new File(statusFileName)).delete();
		}
		catch(Exception ex){
			//do nothing - we don't care
		}
		statusReader = new Mar345StatusReader(statusFileName);

		// Set up a status monitor on a separate thread
		statusMonitor = uk.ac.gda.util.ThreadManager.getThread(this);
		statusMonitor.start();

		// Open command file
		try {
			commandFile = new BufferedWriter(new FileWriter(commandFileName, false));
		} catch (IOException ex) {
			exceptionUtils.logException(logger, "Error opening writer for mar command file in Mar345Controller "
					+ commandFileName, ex);
		}
	}

	/**
	 * If the controller step is idle, return status of idle, else busy.
	 * 
	 * @return Returns the detector status (as per detector interface)
	 */
	public int getDetectorStatus() {
		if (step == ControllerStep.IDLE) {
			return 0;
		} else if (step == ControllerStep.UNKNOWN) {
			return -1;
		}
		return 1;
	}

	private boolean marStatusNotFoundReported = false;

	/**
	 * Thread which checks on the status of the mar345
	 */
	@Override
	public void run() {
		do {
			try {
				Thread.sleep(200);
			} catch (InterruptedException ex) {
				exceptionUtils.logException(logger, "Error trying to sleep in mar status monitor", ex);
			}
			try {
				updateCurrentStep(statusReader.getDetectorStatus());
				marStatusNotFoundReported = false;
			} catch (FileNotFoundException ex) {
				if (!marStatusNotFoundReported) {
					//exceptionUtils.logException(logger, "File not found - " + statusReader.statusFileName, ex);
					marStatusNotFoundReported = true; // do not report future failures
					step = ControllerStep.UNKNOWN;
				}
			} catch (Exception ex) {
				exceptionUtils.logException(logger, "IOException reading - " + statusReader.statusFileName, ex);
			}
		} while (true);
	}

	private boolean statusNot0ErrorReported = false;

	/**
	 * Updates the controller step depending on the current step and the new status obtained from the
	 * Mar345StatusReader.
	 * 
	 * @param newStatus
	 *            the new mar status
	 */
	private void updateCurrentStep(int newStatus) {
		// logger.debug("controller: new status according to Mar345StatusReader: " + newStatus);
		if(step == ControllerStep.UNKNOWN)
		{
			if(newStatus != 0)
				logger.error("Invalid status value - " + newStatus);
			step = ControllerStep.IDLE;
			return;
		}
		if (step == ControllerStep.IDLE) {
			if (newStatus != 0 && !statusNot0ErrorReported) {
				statusNot0ErrorReported = true;
				logger.error("Status is " + newStatus + " but should be 0 since no command has been sent");
			}
			return;
		}
		statusNot0ErrorReported = false;

		if (step == ControllerStep.COMMAND_SENT) {
			if (newStatus == 1) {
				step = ControllerStep.BUSY_1;
				logger.debug("controller: step changed to busy1 - now wait " + busy1Delay);
				statusTimer = System.currentTimeMillis();
			}
			return;
		}

		if (step == ControllerStep.BUSY_1) {
			if (System.currentTimeMillis() - statusTimer > busy1Delay) {
				step = ControllerStep.BUSY_2;
				logger.debug("controller: step changed to busy2");
			}
			return;
		}

		if (step == ControllerStep.BUSY_2) {
			if (newStatus == 0) {
				step = ControllerStep.IDLE;
				logger.debug("controller: step changed to idle");
			}
			return;
		}
	}

	/**
	 * Send an individual string to the Mar345. The list of commands can be found in the scan345.ps doc on the mar
	 * installation cd, under the docs folder in the mar directory or online at mar research
	 * 
	 * @param keywords
	 *            mar keywords (see documentation)
	 */
	public void sendKeywords(String keywords) {

		try {
			if (step == ControllerStep.IDLE) {
				try {
					if (commandFile != null) {
						commandFile.close();
					}
					commandFile = new BufferedWriter(new FileWriter(commandFileName, false));
				} catch (IOException e) {
					logger.debug("Error in method sendString while trying to open commandFile");
				}
				step = ControllerStep.COMMAND_SENT;
				logger.debug("controller: step changed to command_sent");
				commandFile.write(keywords);
				commandFile.newLine();
				commandFile.close();
				// commandFile.flush();
			} else {
				logger.debug("Error: cannot send command as status not idle (step is " + step.toString() + ")");
			}

		} catch (IOException e) {
			logger.debug("Error in method sendString while trying to write to commandFile");
		}
	}
}