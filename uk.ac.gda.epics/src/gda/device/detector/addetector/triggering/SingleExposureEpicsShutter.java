/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.addetector.triggering;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ADBase.StandardTriggerMode;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.device.detector.areadetector.v17.impl.ADBaseImpl;
import gda.epics.connection.EpicsController;
import gda.scan.ScanInformation;
import gda.util.Sleep;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleExposureEpicsShutter extends SimpleAcquire {

	public SingleExposureEpicsShutter(ADBase adBase, double readoutTime,
			String shutterPV, String shutterOpenValue, double shutterOpenDelay,
			String shutterCloseValue, double shutterCloseDelay) {
		super(adBase, readoutTime);
		setShutterPV(shutterPV);
		setShutterOpenValue(shutterOpenValue);
		setShutterOpenDelay(shutterOpenDelay);
		setShutterCloseValue(shutterCloseValue);
		setShutterCloseDelay(shutterCloseDelay);
	}

	// Setup the logging facilities
	static final Logger logger = LoggerFactory.getLogger(ADBaseImpl.class);

	private final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	public String getShutterPV() {
		return shutterPV;
	}

	public void setShutterPV(String shutterPV) {
		this.shutterPV = shutterPV;
	}

	public String getShutterOpenValue() {
		return shutterOpenValue;
	}

	public void setShutterOpenValue(String shutterOpenValue) {
		this.shutterOpenValue = shutterOpenValue;
	}

	public double getShutterOpenDelay() {
		return shutterOpenDelay;
	}

	public void setShutterOpenDelay(double shutterOpenDelay) {
		this.shutterOpenDelay = shutterOpenDelay;
	}

	public String getShutterCloseValue() {
		return shutterCloseValue;
	}

	public void setShutterCloseValue(String shutterCloseValue) {
		this.shutterCloseValue = shutterCloseValue;
	}

	public double getShutterCloseDelay() {
		return shutterCloseDelay;
	}

	public void setShutterCloseDelay(double shutterCloseDelay) {
		this.shutterCloseDelay = shutterCloseDelay;
	}

	private String shutterPV;
	private String shutterOpenValue;
	private double shutterOpenDelay;
	private String shutterCloseValue;
	private double shutterCloseDelay;

	private Channel shutter;
	private double collectionTime;

	FutureTask<Void> closeShutterTask;

	private void startCloseShutterTask() {
		class CloseShutterAfterCollectionTime implements Callable<Void> {
			@Override
			public Void call() throws Exception {
				try {
					logger.info("CloseShutterAfterCollectionTime.call() start");
					Thread.sleep((int)(collectionTime*1000));
					logger.info("CloseShutterAfterCollectionTime.call() after sleep {} = {}", shutterPV,
							EPICS_CONTROLLER.caget(shutter));
				} catch (Exception e) {
					throw new Exception("Problem sleeping", e);
				} finally {
					EPICS_CONTROLLER.caput(shutter, shutterCloseValue);
					logger.info("CloseShutterAfterCollectionTime.call() after close caput {} = {}", shutterPV,
							EPICS_CONTROLLER.caget(shutter));
				}
				return null;
			}
		}
		closeShutterTask = new FutureTask<Void>(new CloseShutterAfterCollectionTime());

		(new Thread(closeShutterTask, "SingleExposureEpicsShutter."+
			"CloseShutterAfterCollectionTime-" + shutterPV)).start();
	}

	private void checkForCloseShutterTaskErrors() throws DeviceException, InterruptedException {
		if (closeShutterTask == null) return;
		if (!closeShutterTask.isDone()) {
			logger.error("closeShutterTask still running, cancelling...");
			closeShutterTask.cancel(true);
		}
		try {
			closeShutterTask.get();
		} catch (CancellationException e) {
			logger.error(e.getClass() + " cancelled", e);
		} catch (ExecutionException e) {
			logger.error(e.getClass() + " caused an exception:", e.getCause());
			throw new DeviceException(e.getClass() + " problem: " + e.getMessage(), e.getCause());
		} catch (InterruptedException e) {
			logger.error(e.getClass() + " interrupted", e);
			throw e;
		}
	}

	@Override
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		this.collectionTime = collectionTime;
		if (shutterCloseDelay > 0) {
			getAdBase().setAcquireTime(collectionTime+shutterCloseDelay);
		} else {
			getAdBase().setAcquireTime(collectionTime);
		}
		if (getReadoutTime() < 0) {
			getAdBase().setAcquirePeriod(0.0);
		} else {
			getAdBase().setAcquirePeriod(collectionTime + getReadoutTime() +
					getShutterOpenDelay() + getShutterCloseDelay());
		}
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		if (numImages != 1) {
			throw new IllegalArgumentException("This single exposure " +
					"triggering strategy expects to expose only 1 image");
		}
		super.prepareForCollection(collectionTime, 1, scanInfo);
		configureTriggerMode();
		getAdBase().setImageModeWait(ImageMode.SINGLE);
		getAdBase().setNumImages(1);
	}

	protected void configureTriggerMode() throws Exception {
		getAdBase().setTriggerMode(StandardTriggerMode.INTERNAL.ordinal());

		if (shutter == null) {
			try {
				shutter = EPICS_CONTROLLER.createChannel(shutterPV);
			} catch (CAException cae) {
				logger.warn("Problem creating channel", cae);
				throw cae;
			} catch (TimeoutException te) {
				logger.warn("Problem creating channel", te);
				throw te;
			}
		}
	}

	@Override
	public void collectData() throws Exception {
		logger.info("SingleExposureEpicsShutter:collectData() start " +
			"collectionTime {}s  shutterOpenDelay {}s shutterCloseDelay {}s",
			new Object[] { collectionTime, shutterOpenDelay, shutterCloseDelay });

		checkForCloseShutterTaskErrors();

		logger.info("collectData() before open caput {} = {}",
				shutterPV, EPICS_CONTROLLER.caget(shutter));
		// Open the shutter
		EPICS_CONTROLLER.caput(shutter, shutterOpenValue);

		logger.info("collectData() after open caput {} = {}",
				shutterPV, EPICS_CONTROLLER.caget(shutter));

		if (shutterCloseDelay > 0) {
			// Start close task after start of acquisition.
			Sleep.sleep((int) (shutterOpenDelay*1000));

			logger.info("collectData() after {}s sleep {} = {}",
					new Object[] { shutterOpenDelay, shutterPV,
						EPICS_CONTROLLER.caget(shutter) });

			getAdBase().startAcquiring();

			logger.info("collectData() after startAcquiring {} = {}",
					shutterPV, EPICS_CONTROLLER.caget(shutter));

			startCloseShutterTask();

			logger.info("collectData() after startCloseShutterTask {} = {}",
					shutterPV, EPICS_CONTROLLER.caget(shutter));
		} else {
			// Start close BEFORE start of acquisition.!!!
			double sleepFor = shutterOpenDelay+shutterCloseDelay;
			Sleep.sleep((int) (sleepFor*1000));

			logger.info("collectData() after {}s sleep {} = {}",
					new Object[] { sleepFor, shutterPV, EPICS_CONTROLLER.caget(shutter) });

			EPICS_CONTROLLER.caput(shutter, shutterCloseValue);

			logger.info("CloseShutterAfterCollectionTime.call() after close caput {} = {}",
				shutterPV, EPICS_CONTROLLER.caget(shutter));

			sleepFor = -shutterCloseDelay;
			Sleep.sleep((int) (sleepFor*1000));

			logger.info("collectData() after {}s sleep {} = {}",
					new Object[] { sleepFor, shutterPV, EPICS_CONTROLLER.caget(shutter) });

			getAdBase().startAcquiring();

			logger.info("collectData() after startAcquiring {} = {}", shutterPV,
					EPICS_CONTROLLER.caget(shutter));
		}
	}

	@Override
	public void completeCollection() throws Exception {
		logger.info("SingleExposureEpicsShutter:endCollection() start");

		getAdBase().stopAcquiring();

		logger.info("getAdBase().endCollection() after stopAcquiring {} = {}", shutterPV,
				EPICS_CONTROLLER.caget(shutter));

		checkForCloseShutterTaskErrors();

		logger.info("getAdBase().endCollection() after checkForCloseShutterTaskErrors get{} = {}", shutterPV,
				EPICS_CONTROLLER.caget(shutter));
	}
}
