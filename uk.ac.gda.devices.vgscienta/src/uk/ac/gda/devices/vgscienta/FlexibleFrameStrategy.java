/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.vgscienta;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.detector.addetector.triggering.SimpleAcquire;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDProcess;
import gda.device.detector.areadetector.v17.impl.ADBaseImpl;
import gda.epics.connection.EpicsController;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

public class FlexibleFrameStrategy extends SimpleAcquire implements MonitorListener, IObservable {
	private static final long PROCESSING_WAIT_TIME_MS = 25;
	private static final int PROCESSING_TIMEOUT_MS = 15000; // 15 secs

	private static final Logger logger = LoggerFactory.getLogger(FlexibleFrameStrategy.class);

	private ObservableComponent oc = new ObservableComponent();

	private volatile int maxNumberOfFrames = 1;
	private volatile int currentFrame = -1;
	private volatile int highestFrame = 0;
	private volatile boolean wethinkweareincharge = false;

	private final NDProcess proc;
	private final NDArray array;
	private EpicsController epicsController;

	public FlexibleFrameStrategy(ADBase base, double time, NDProcess ndProcess, NDArray ndArray) throws CAException, InterruptedException, TimeoutException {
		super(base, time);
		setReadoutTime(-1);
		proc = ndProcess;
		array = ndArray;
		epicsController = EpicsController.getInstance();
		epicsController.setMonitor(epicsController.createChannel(((ADBaseImpl) getAdBase()).getBasePVName() + ADBase.ArrayCounter_RBV), this);
	}

	@Override
	public void collectData() throws Exception {
		logger.trace("collectData called");
		getAdBase().setArrayCounter(0);
		array.getPluginBase().setArrayCounter(0);
		proc.getPluginBase().setArrayCounter(0);
		proc.setResetFilter(1);
		logger.debug("Reset IOC counters and recursive filter");
		currentFrame = 0;
		highestFrame = 0;
		wethinkweareincharge = true;
		interactWithDeviceIfRequired();
		logger.debug("Start acquiring");
		getAdBase().startAcquiring();
	}

	@Override
	public synchronized void monitorChanged(MonitorEvent arg0) {
		if (wethinkweareincharge) {
			if (arg0.getDBR() instanceof DBR_Int) {
				currentFrame = ((DBR_Int) arg0.getDBR()).getIntValue()[0];
				interactWithDeviceIfRequired();
				logger.debug("Processed updates for frame {}.", currentFrame);
			}
		}
	}

	private void interactWithDeviceIfRequired() {
		if (!wethinkweareincharge)
			return;

		try {
			if (currentFrame == (maxNumberOfFrames - 1)) {
					getAdBase().setImageMode(0);
			} else {
					getAdBase().setImageMode(2);
			}
		} catch (Exception e) {
			logger.error("Exception received controlling analyser exposure, sweeps out of control!", e);
		}

		notifyObservers();
	}

	@Override
	public void completeCollection() throws Exception {
		logger.trace("completeCollection called");
		if (!wethinkweareincharge)
			return;
		wethinkweareincharge = false;
		highestFrame = currentFrame;
		currentFrame = -1;
		// versions of SES=ses1.3.1r5 and IOC=R3.14.12.3v2-11 subsume zeroing of power supplies within acquire stop,
		// so separate zeroing action is no longer exposed. Call to super.completeCollection was
		// removed from here avoid toggling the power supplies by invoking stop (via getAdBase().stopAcquire)
		// after every datapoint in a step scan
		notifyObservers();
	}

	public int getMaxNumberOfFrames() {
		return maxNumberOfFrames;
	}

	public void setMaxNumberOfFrames(int maxNumberOfFrames) {
		logger.trace("setMaxNumberOfFrames called with {} frames", maxNumberOfFrames);
		if (maxNumberOfFrames < 1)
			throw new IllegalArgumentException("must collect at least one frame");
		// Catch the equal case as it can cause problems if a frame completes while changing iterations
		if (maxNumberOfFrames <= currentFrame)
			throw new IllegalArgumentException("cannot reduce number of frames when I already collected more");
		this.maxNumberOfFrames = maxNumberOfFrames;
		interactWithDeviceIfRequired();
	}

	public double getFrameTime() throws Exception {
		return super.getAcquireTime();
	}

	@Override
	public double getAcquireTime() throws Exception {
		return super.getAcquireTime() * proc.getNumFiltered_RBV();
	}

	public int getLastAcquired() throws Exception {
		return proc.getNumFiltered_RBV();
	}

	public int getCurrentFrame() {
		return currentFrame;
	}

	@Override
	public void addIObserver(IObserver observer) {
		oc.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		oc.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		oc.deleteIObservers();
	}

	private void notifyObservers() {
		oc.notifyIObservers(this, new FrameUpdate(currentFrame, maxNumberOfFrames));
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, DeviceException {
		logger.trace("waitWhileBusy called");
		// Wait for IOC acquire to call back
		super.waitWhileBusy();

		// If we are not in charge at this point just return. Means collectData() hasn't been called yet
		// This is called during moving to the next point and if we are not in charge we don't want to wait for processing as were not sure it will ever happen.
		// This fixes the configure only bug I05-29
		if (!wethinkweareincharge) {
			logger.trace("waitWhileBusy called while we are not in charge so return");
			return;
		}

		logger.trace("Starting to wait for processing...");
		// at this point we should be stopped, but might not have processed the last frame/sweep
		if (currentFrame > highestFrame)
			highestFrame = currentFrame;
		try {
			int timeoutCounter = 0;
			// Check if the expected number of frames have been processed yet and reached array plugin
			while (proc.getNumFiltered_RBV() < highestFrame || array.getPluginBase().getArrayCounter_RBV() < highestFrame) {
				// Log the current status so we can try to debug why we are here
				logger.debug("Waiting for IOC processing: highestFrame={},  numFilter={}, arrayCounterRBV={}", highestFrame, proc.getNumFiltered_RBV(),
						array.getPluginBase().getArrayCounter_RBV());
				logger.debug("Waiting for IOC processing: number={}, time={} ms", timeoutCounter, timeoutCounter * PROCESSING_WAIT_TIME_MS);
				// Check if timeout is exceeded
				if (timeoutCounter * PROCESSING_WAIT_TIME_MS > PROCESSING_TIMEOUT_MS) {
					logger.error("Timout ({} ms) waiting for IOC processing", PROCESSING_TIMEOUT_MS);
					throw new DeviceException(String.format("Timout (%d ms) waiting for IOC processing", PROCESSING_TIMEOUT_MS));
				}
				// Increment timeout counter
				timeoutCounter++;
				// Wait for some time before checking again
				Thread.sleep(PROCESSING_WAIT_TIME_MS);
			}
			logger.debug("At the end of waiting we have: highestFrame={},  numFilter={}, arrayCounterRBV={}", highestFrame, proc.getNumFiltered_RBV(),
					array.getPluginBase().getArrayCounter_RBV());
			logger.info("IOC Processing complete. (Waited for {} ms)", PROCESSING_WAIT_TIME_MS * timeoutCounter);
		} catch (Exception e) {
			throw new DeviceException("Error waiting for IOC processing", e);
		}
	}
}