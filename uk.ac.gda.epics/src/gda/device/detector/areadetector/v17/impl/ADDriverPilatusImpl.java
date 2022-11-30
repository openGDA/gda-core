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

package gda.device.detector.areadetector.v17.impl;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADDriverPilatus;
import gda.epics.LazyPVFactory;
import gda.epics.NoCallbackPV;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;
import gda.epics.util.EpicsGlobals;
import gda.factory.FactoryException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

public class ADDriverPilatusImpl implements ADDriverPilatus, InitializingBean {

	public class SoftTriggerCallbackListener implements PutListener {
		@Override
		public void putCompleted(PutEvent event) {
			if (event.getStatus() != CAStatus.NORMAL) {
				logger.error("Put failed. Channel {} : Status {}",
						((Channel) event.getSource()).getName(),
						event.getStatus());
				setSoftTriggerStatus(Detector.FAULT);
				return;
			}
			logger.info("Soft Trigger request completed: {} called back", ((Channel) event.getSource()).getName());
			setSoftTriggerStatus(Detector.IDLE);
		}
	}

	static final Logger logger = LoggerFactory.getLogger(ADDriverPilatusImpl.class);

	private String basePVName;

	private volatile int softTriggerStatus = Detector.IDLE;

	private Object softTriggerStatusMonitor = new Object();

	/**
	 * Some operations e.g. set threshold energy can take upwards of 20 seconds so a
	 * longer time out can be specified for these caputs
	 */
	private double longCaputTimeout = EpicsGlobals.getTimeout();

	// PVs

	private ReadOnlyPV<Boolean> pvArmed;

	private PV<Float> pvDelayTime;

	private ReadOnlyPV<Float> pvDelayTime_RBV;

	private PV<Float> pvThresholdEnergy;

	private ReadOnlyPV<Float> pvThresholdEnergy_RBV;

	private PV<Gain> pvGain;

	private PV<Float> pvImageFileTmot;

	private PV<String> pvBadPixelFile;

	private ReadOnlyPV<Integer> pvNumBadPixels;

	private NoCallbackPV<String> pvFlatFieldFile;

	private PV<Integer> pvMinFlatField;

	private ReadOnlyPV<Integer> pvMinFlatField_RBV;

	private ReadOnlyPV<Boolean> pvFlatFieldValid;

	private PV<Integer> pvSoftTrigger;

	private SoftTriggerCallbackListener softTriggerCallbackListener = new SoftTriggerCallbackListener();

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	public String getBasePVName() {
		return basePVName;
	}

	public double getLongCaputTimeout() {
		return longCaputTimeout;
	}

	public void setLongCaputTimeout(double longCaputTimeout) {
		this.longCaputTimeout = longCaputTimeout;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (basePVName == null) {
			throw new IllegalArgumentException("basePVName needs to be declared");
		}
		try {
			createLazyPvs();
		} catch (Exception e) {
			throw new FactoryException("Problem configuring PVs", e);
		}
	}

	private void createLazyPvs() {

		pvArmed = LazyPVFactory.newReadOnlyBooleanFromIntegerPV(fullname("Armed"));

		pvDelayTime = LazyPVFactory.newFloatPV(fullname("DelayTime"));

		pvDelayTime_RBV = LazyPVFactory.newReadOnlyFloatPV(fullname("DelayTime_RBV"));

		pvThresholdEnergy = LazyPVFactory.newFloatPV(fullname("ThresholdEnergy"));

		pvThresholdEnergy_RBV = LazyPVFactory.newReadOnlyFloatPV(fullname("ThresholdEnergy_RBV"));

		pvGain = LazyPVFactory.newEnumPV(fullname("GainMenu"), Gain.class);

		pvImageFileTmot = LazyPVFactory.newFloatPV(fullname("ImageFileTmot"));

		pvBadPixelFile = LazyPVFactory.newStringFromWaveformPV(fullname("BadPixelFile"));

		pvNumBadPixels = LazyPVFactory.newReadOnlyIntegerPV(fullname("NumBadPixels"));

		pvFlatFieldFile = LazyPVFactory.newStringFromWaveformPV(fullname("FlatFieldFile"));

		pvMinFlatField = LazyPVFactory.newIntegerPV(fullname("MinFlatField"));

		pvMinFlatField_RBV = LazyPVFactory.newReadOnlyIntegerPV(fullname("MinFlatField_RBV"));

		pvFlatFieldValid = LazyPVFactory.newReadOnlyBooleanFromIntegerPV(fullname("FlatFieldValid"));

		//TODO: handle ADPilatusType (the config the other pvs use)
		pvSoftTrigger = LazyPVFactory.newIntegerPV(fullname(SoftTrigger));
	}

	private String fullname(String pvElementName, String... args) {
		// untried - RobW
		return basePVName + ((args.length > 0) ? args[0] : pvElementName);
	}

	@Override
	public void reset() throws DeviceException {
	}

	@Override
	public boolean isArmed() throws DeviceException {
		try {
			return pvArmed.get();
		} catch (IOException e) {
			throw new DeviceException(e);
		}
	}


	@Override
	public void waitForArmed(double timeoutS) throws DeviceException {
		try {
			pvArmed.setValueMonitoring(true);
		} catch (IOException e) {
			throw new DeviceException(e);
		}
		logger.info("Waiting for pilatus detector to arm");
		try {
			pvArmed.waitForValue(armed -> armed, timeoutS);
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public void setDelayTime(float delayTimeSeconds) throws DeviceException {
		try {
			pvDelayTime.putWait(delayTimeSeconds);
		} catch (IOException e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public float getDelayTime_RBV() throws DeviceException {
		try {
			return pvDelayTime_RBV.get();
		} catch (IOException e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public void setThresholdEnergy(float thresholdEnergy) throws DeviceException {
		try {
			pvThresholdEnergy.putWait(thresholdEnergy, longCaputTimeout);
		} catch (IOException e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public float getThresholdEnergy_RBV() throws DeviceException {
		try {
			return pvThresholdEnergy_RBV.get();
		} catch (IOException e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public void setGain(Gain gain) throws DeviceException {
		try {
			pvGain.putWait(gain, longCaputTimeout);
		} catch (IOException e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public Gain getGain() throws DeviceException {
		try {
			return pvGain.get();
		} catch (IOException e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public void setImageFileTmot(float timeoutSeconds) throws DeviceException {
		try {
			pvImageFileTmot.putWait(timeoutSeconds);
		} catch (IOException e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public void setBadPixelFile(String filename) throws DeviceException {
		try {
			pvBadPixelFile.putWait(filename);
		} catch (IOException e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public int getNumBadPixels() throws DeviceException {
		try {
			return pvNumBadPixels.get();
		} catch (IOException e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public void setFlatFieldFile(String filename) throws DeviceException {
		try {
			pvFlatFieldFile.putNoWait(filename);
		} catch (IOException e) {
			throw new DeviceException(e);
		}
		if (!getFlatFieldValid()) {
			throw new IllegalArgumentException("The flatfied file '" + filename + "' was not loaded or was not valid");
		}
	}

	@Override
	public void setMinFlatField(int minIntensity) throws DeviceException {
		try {
			pvMinFlatField.putWait(minIntensity);
		} catch (IOException e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public int getMinFlatField_RBV() throws DeviceException {
		try {
			return pvMinFlatField_RBV.get();
		} catch (IOException e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public boolean getFlatFieldValid() throws DeviceException {
		try {
			return pvFlatFieldValid.get();
		} catch (IOException e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public void sendSoftTrigger() throws Exception {
		logger.trace("Sending soft trigger: {} called.", SoftTrigger);
		try {
			setSoftTriggerStatus(Detector.BUSY);
			pvSoftTrigger.putNoWait(1, softTriggerCallbackListener);
		} catch (Exception e) {
			setSoftTriggerStatus(Detector.IDLE);
			logger.error("Exception sending soft trigger");
			throw e;
		}
	}

	@Override
	public void waitForSoftTriggerCallback() throws InterruptedException {
		synchronized (softTriggerStatusMonitor) {
			try {
				while (softTriggerStatus == Detector.BUSY) {
					softTriggerStatusMonitor.wait(1000);
				}
			} finally {
				if (softTriggerStatus != Detector.IDLE) {
					setSoftTriggerStatus(Detector.IDLE);
				}
			}
		}
	}

	public int getSoftTriggerStatus() {
		return softTriggerStatus;
	}

	public void setSoftTriggerStatus(int softTriggerStatus) {
		synchronized (softTriggerStatusMonitor) {
			this.softTriggerStatus = softTriggerStatus;
			softTriggerStatusMonitor.notifyAll();
		}
	}
}
