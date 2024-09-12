/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package gda.device.detector.addetector.collectionstrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.epics.connection.EpicsController;
import gda.scan.ScanInformation;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

/**
 * This Collection strategy can be used where detector is running in the Continuous mode (liveViewer of the detector) and data is taken from
 * the PROC plugin which has a dynamically specified PV being monitored instead of base CAM Acquire.
 *
 * This strategy does not set the Trigger Mode, so should be wrapped with a {@link TriggerModeDecorator} as appropriate (use
 * {@link InternalTriggerModeDecorator} for the equivalent of the old SingleExposureStandard with the default internal trigger mode).
 *
 * This strategy does not set the Image Mode, it is assumed that detector is running in the Continuous mode.
 *
 * This strategy does not set the acquire time, so should be wrapped with a {@link ConfigureAcquireTimeDecorator} to behave
 * like the old SimpleAcquire or SingleExposureStandard collection strategies.
 *
 */
public class ProcStartStop extends AbstractADCollectionStrategy {
	private static final Logger logger = LoggerFactory.getLogger(ProcStartStop.class);
	protected static final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();
	private Monitor monitor;
	private Channel monitorChannel;

	private boolean restoreAcquireState = false;

	private String procMonitorPV;

	private ProcChannelListener numFilterListener = new ProcChannelListener();

	private int savedAcquireState;

	private NDProcess ndProcess=null;

	private boolean disableCallbackAfterCollect = true; // Must disable callbacks for continuous mode, otherwise it will continue fetching data independent on motors move


	private class ProcChannelListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			if (arg0.getStatus() != CAStatus.NORMAL) {
				logger.error("Put failed. Channel {} : Status {}", ((Channel) arg0.getSource()).getName(),
						arg0.getStatus());
				getAdBase().setStatus(Detector.FAULT);
				return;
			}

			if (getAdBase().getStatus() == Detector.IDLE) return;

			logger.trace("Setting detector to IDLE");
			getAdBase().setStatus(Detector.IDLE);

			// need to disable callbacks otherwise it will continue sending data further
			try {
				if (isDisableCallbackAfterCollect()) {
					logger.trace("Disabling callbacks for PROC plugin");
					ndProcess.getPluginBase().disableCallbacks();
				}
			} catch (Exception e) {
				logger.error("Failed tos disable callbacks in PROC plugin", e);
			}
		}
	}

	@Override
	protected void rawPrepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		super.rawPrepareForCollection(collectionTime, numberImagesPerCollection, scanInfo);
		configureMonitors();
	}

	private void configureMonitors() throws Exception {
		if ((getAdBase().getImageMode_RBV() != 2) || (getAdBase().getAcquireState()!=1)) {
			logger.error("For ProcStartStop collectionStrategy detector must be already running in Continuous mode!");
			throw new Exception();
		}
		if (monitorChannel == null) monitorChannel = EPICS_CONTROLLER.createChannel(procMonitorPV);
		if (monitor == null) monitor = EPICS_CONTROLLER.setMonitor(monitorChannel, numFilterListener);
	}

	// NXCollectionStrategyPlugin interface
	@Override
	public void collectData() throws Exception {
		//I09-579 If via pos command, rawPrepareForCollection method is never called.
		//Add check here to create monitors if null so detector doesn't freeze on pos.
		configureMonitors();
		// Put monitor, detector is in the Continuous mode acquiring already so no need to start Acquire
		logger.debug("CollectData called - set detector to BUSY");
		getAdBase().setStatus(Detector.BUSY);
	}

	@Override
	protected void rawCompleteCollection() throws Exception {
		logger.trace("rawCompleteCollection() called, restoreAcquireState={}", restoreAcquireState);
		if(monitor != null) EPICS_CONTROLLER.clearMonitor(monitor);
		if(monitorChannel != null) EPICS_CONTROLLER.destroy(monitorChannel);
		monitor = null;
		monitorChannel = null;
		ndProcess.setResetFilter(1);
	}

	@Override
	public int getNumberImagesPerCollection(double collectionTime) throws Exception {
		logger.trace("getNumberImagesPerCollection({}) called, ignoring collectionTime & returning 1.", collectionTime);
		return 1;
	}

	@Override
	public void rawAtCommandFailure() throws Exception {
		logger.trace("rawAtCommandFailure() called, restoreAcquireState={}", restoreAcquireState);
		completeCollection();
	}

	@Override
	public void rawStop() throws Exception {
		logger.trace("rawStop() called, restoreAcquireState={}", restoreAcquireState);
		completeCollection();
	}

	// CollectionStrategyBeanInterface interface

	@Override
	public void saveState() throws Exception {
		logger.trace("saveState() called, restoreAcquireState={}", restoreAcquireState);
		if (restoreAcquireState) {
			savedAcquireState = getAdBase().getAcquireState();
			existingStateSaved=true;
			logger.debug("Saved state now savedAcquireState={}", savedAcquireState);
		}
	}

	@Override
	public void restoreState() throws Exception {
		logger.trace("restoreState() called, restoreAcquireState={}, savedAcquireState={}", restoreAcquireState, savedAcquireState);
		if (restoreAcquireState) {
			if (savedAcquireState == 1) {
				getAdBase().startAcquiring();
			} else {
				getAdBase().stopAcquiring();
			}
			existingStateSaved=false;
			logger.debug("Restored state to savedAcquireState={})", savedAcquireState);
		}
	}

	public boolean getRestoreAcquireState() {
		return restoreAcquireState;
	}

	public void setRestoreAcquireState(boolean restoreAcquireState) {
		this.restoreAcquireState = restoreAcquireState;
	}

	public String getProcMonitorPV() {
		return procMonitorPV;
	}

	public void setProcMonitorPV(String procMonitorPV) {
		this.procMonitorPV = procMonitorPV;
	}

	public NDProcess getNdProcess() {
		return ndProcess;
	}

	public void setNdProcess(NDProcess ndProcess) {
		errorIfPropertySetAfterBeanConfigured("ndProcess");
		this.ndProcess = ndProcess;
	}

	public boolean isDisableCallbackAfterCollect() {
		return disableCallbackAfterCollect;
	}

	public void setDisableCallbackAfterCollect(boolean disableCallbackAfterCollect) {
		this.disableCallbackAfterCollect = disableCallbackAfterCollect;
	}

}
