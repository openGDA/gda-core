/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.configuration.view.handlers.impl;

import gda.jython.IScanDataPointObserver;
import gda.jython.InterfaceProvider;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gda.observable.IObservable;
import gda.scan.IScanDataPoint;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.alignment.view.TomoAlignmentCommands;
import uk.ac.gda.client.tomo.configuration.view.handlers.IScanControllerUpdateListener;
import uk.ac.gda.client.tomo.configuration.view.handlers.ITomoScanController;

public class TomoScanController implements ITomoScanController {

	private static final String NONE = "None";

	private static final String DOUBLE_REG_EXP = "(\\d)*.?(\\d)*";

	private static final String EXPOSURE_TIME = "ExposureTime:";

	private ArrayList<IScanControllerUpdateListener> controllerUpdates = new ArrayList<IScanControllerUpdateListener>();

	private static final Logger logger = LoggerFactory.getLogger(TomoScanController.class);

	private IObservable tomoScriptController;

	private final Pattern DOUBLE_REG_EXP_PATTERN = Pattern.compile(DOUBLE_REG_EXP);

	private static final String ID_GET_RUNNING_CONFIG = "RunningConfig#";

	private boolean isInitialised = false;

	public void setTomoScriptController(IObservable tomoScriptController) {
		this.tomoScriptController = tomoScriptController;
	}

	@Override
	public void addControllerUpdateListener(IScanControllerUpdateListener scanControllerUpdateListener) {
		controllerUpdates.add(scanControllerUpdateListener);
	}

	@Override
	public void removeControllerUpdateListener(IScanControllerUpdateListener scanControllerUpdateListener) {
		controllerUpdates.remove(scanControllerUpdateListener);
	}

	@Override
	public void runScan(String configFilePath) {

		String setupTomoScanCmd = String.format("tomoAlignment.tomographyConfigurationManager.setupTomoScan('%1$s')",
				configFilePath);
		logger.debug("Setup:{}", setupTomoScanCmd);
		JythonServerFacade.getInstance().runCommand(setupTomoScanCmd);
	}

	private IScanDataPointObserver tomoScriptControllerObserver = new IScanDataPointObserver() {

		@Override
		public void update(Object source, final Object arg) {
			if (arg instanceof IScanDataPoint) {
				final IScanDataPoint scanDataPoint = (IScanDataPoint) arg;
				double currentPoint = scanDataPoint.getCurrentPointNumber();
				double numberOfPoints = scanDataPoint.getNumberOfPoints();
				double progress = currentPoint / numberOfPoints * 100;
				for (IScanControllerUpdateListener lis : controllerUpdates) {
					lis.updateScanProgress(progress);
				}
			} else if (source.equals(tomoScriptController)) {
				if (arg instanceof Exception) {
					Exception exception = (Exception) arg;
					logger.error("Problem with scan {}", exception);
					for (IScanControllerUpdateListener lis : controllerUpdates) {
						lis.updateError(exception);
					}
				} else {
					if (arg instanceof String) {
						String message = (String) arg;
						if (message.startsWith(EXPOSURE_TIME)) {
							String expInDouble = message.substring(EXPOSURE_TIME.length());
							if (DOUBLE_REG_EXP_PATTERN.matcher(expInDouble).matches()) {
								for (IScanControllerUpdateListener lis : controllerUpdates) {
									lis.updateExposureTime(Double.parseDouble(expInDouble));
								}
							}
						} else {

							if (message.startsWith(ID_GET_RUNNING_CONFIG)) {
								final String runningConfigId = message.substring(ID_GET_RUNNING_CONFIG.length());
								if (NONE.equals(runningConfigId)) {
									logger.debug("No configs are running");
									for (IScanControllerUpdateListener lis : controllerUpdates) {
										lis.isScanRunning(false || isJythonScanRunning(), runningConfigId);
									}
								} else {
									for (IScanControllerUpdateListener lis : controllerUpdates) {
										lis.isScanRunning(true, runningConfigId);
									}
								}
							} else {
								for (IScanControllerUpdateListener lis : controllerUpdates) {
									lis.updateMessage(message);
								}
							}
						}
					}
				}
			}
		}
	};

	private boolean isJythonScanRunning() {
		boolean isJythonScanRunning = false;
		try {
			isJythonScanRunning = Jython.RUNNING == JythonServerFacade.getCurrentInstance().getScanStatus();
		} catch (Exception ex) {
			logger.error("Problem extracting jython scan running status", ex);
		}
		return isJythonScanRunning;
	}

	@Override
	public void dispose() {
		if (controllerUpdates.size() < 1) {
			if (tomoScriptController != null) {
				tomoScriptController.deleteIObserver(tomoScriptControllerObserver);
			}
			InterfaceProvider.getScanDataPointProvider().deleteIScanDataPointObserver(tomoScriptControllerObserver);
			isInitialised = false;
		}
	}

	@Override
	public void isScanRunning() {
		InterfaceProvider.getCommandRunner().evaluateCommand(TomoAlignmentCommands.TOMOGRAPHY_IS_RUNNING_CONFIG_CMD);
	}

	@Override
	public void initialize() {
		if (!isInitialised) {
			if (this.tomoScriptController != null) {
				this.tomoScriptController.addIObserver(tomoScriptControllerObserver);
			}
			InterfaceProvider.getScanDataPointProvider().addIScanDataPointObserver(tomoScriptControllerObserver);
			isInitialised = true;
		}
	}

	@Override
	public void stopScan() {
		InterfaceProvider.getCommandRunner().evaluateCommand(TomoAlignmentCommands.TOMOGRAPHY_STOP_SCAN);
	}

}
