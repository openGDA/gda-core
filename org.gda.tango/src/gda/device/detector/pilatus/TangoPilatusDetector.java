/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.detector.pilatus;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceAttribute;
import gda.device.DeviceException;
import gda.device.TangoDeviceProxy;
import gda.device.Timer;
import gda.device.TimerStatus;
import gda.device.detector.corba.impl.DetectorAdapter;
import gda.device.detector.corba.impl.DetectorImpl;
import gda.device.timer.Tfg;
import gda.factory.FactoryException;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;
import gda.observable.IObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

@CorbaAdapterClass(DetectorAdapter.class)
@CorbaImplClass(DetectorImpl.class)
public class TangoPilatusDetector extends TangoLimaDetector implements InitializingBean, IObserver {

	private static final Logger logger = LoggerFactory.getLogger(TangoPilatusDetector.class);
	private TangoDeviceProxy dev = null;
	private Tfg timer;
	
	private Double triggerDelay = null;
	private Integer threshold = null;
	private String thresholdGain = null;
	private String fillMode = null;
	private Float energyThreshold = null;
	private static int detectorCount = 0;
	private static int ready = 0;
	private static int scanPoint;
	
	@Override
	public void configure() throws FactoryException {
		super.configure();
		try {
			init();
			if (energyThreshold != null)
				writeEnergyThreshold(energyThreshold);
			if (threshold != null)
				writeThreshold(threshold);
			if (triggerDelay != null)
				writeTriggerDelay(triggerDelay);
			if (fillMode != null)
				writeFillMode(fillMode);
			if (thresholdGain != null)
				writeThresholdGain(thresholdGain);
			if (timer != null)
				timer.addIObserver(this);
			configured = true;
			logger.debug("TangoPilatusDetector {} configured successfully", getName());
		} catch (Exception e) {
			configured = false;
			logger.error("TangoPilatusDetector {} configure: {}", getName(), e);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (dev == null) {
			throw new IllegalArgumentException("tango device proxy needs to be set");
		}
	}

	@Override
	public void reconfigure() throws FactoryException {
		configured = false;
		logger.debug("NcdDetector reconfiguring " + getName());
		configure();
	}
	
	@Override
	public void close() {
		configured = false;
		timer.deleteIObserver(this);
	}
	
	@Override
	public void atScanStart() throws DeviceException {
		scanPoint = 0;
		writeSavingOverwritePolicy("OVERWRITE");
	}
	
	@Override
	public void collectData() throws DeviceException {
		detectorCount++;
		int nbFrames = timer.getCurrentFrames(1);
		System.out.println("Writing nbFrames " + nbFrames);
		writeNbFrames(timer.getCurrentFrames(1));
		writeSavingFramePerFile(timer.getCurrentFrames(1));
		writeExposureTime(timer.getCurrentLiveTime(1));
		writeLatencyTime(timer.getCurrentDeadTime(1));
		if (scanPoint != 0) {
			writeSavingOverwritePolicy("ABORT");
			writeSavingPrefix(getSavingPrefix());
		}
		super.collectData();
		System.out.println("DetectorCount is now " + detectorCount);
		scanPoint++;
	}

	/**
	 * @return Returns the Tango device proxy.
	 */
	public TangoDeviceProxy getPilatusTangoDeviceProxy() {
		return dev;
	}

	/**
	 * @param dev The Tango device proxy to set.
	 */
	public void setPilatusTangoDeviceProxy(TangoDeviceProxy dev) {
		this.dev = dev;
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		this.timer = (Tfg) timer;
	}

	public double getTriggerDelay() {
		return triggerDelay;
	}

	public void setTriggerDelay(double triggerDelay) {
		this.triggerDelay = triggerDelay;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public String getThresholdGain() {
		return thresholdGain;
	}

	public void setThresholdGain(String thresholdGain) {
		this.thresholdGain = thresholdGain;
	}

	public String getFillMode() {
		return fillMode;
	}

	public void setFillMode(String fillMode) {
		this.fillMode = fillMode;
	}

	public float getEnergyThreshold() {
		return energyThreshold;
	}

	public void setEnergyThreshold(float energyThreshold) {
		this.energyThreshold = energyThreshold;
	}

	public double readTriggerDelay() throws DeviceException {
		isAvailable();
		try {
			triggerDelay = dev.read_attribute("trigger_delay").extractDouble();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get trigger delay", e);
		}		
		return triggerDelay;
	}

	public void writeTriggerDelay(double triggerDelay) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("trigger_delay", triggerDelay));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set trigger Delay", e);
		}		
	}

	public double readEnergyThreshold() throws DeviceException {
		isAvailable();
		try {
			energyThreshold = dev.read_attribute("energy_threshold").extractFloat();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get energy threshold", e);
		}		
		return energyThreshold;
	}

	public void writeEnergyThreshold(float energyThreshold) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("energy_threshold", energyThreshold));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set energy threshold", e);
		}		
	}

	public int readThreshold() throws DeviceException {
		isAvailable();
		try {
			threshold = dev.read_attribute("threshold").extractLong();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get threshold", e);
		}		
		return threshold;
	}

	public void writeThreshold(int threshold) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("threshold", threshold));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set threshold", e);
		}		
	}

	public String readThresholdGain() throws DeviceException {
		isAvailable();
		try {
			thresholdGain = dev.read_attribute("threshold_gain").extractString();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get threshold gain", e);
		}		
		return thresholdGain;
	}

	public void writeThresholdGain(String thresholdGain) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("threshold_gain", thresholdGain));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set threshold gain", e);
		}		
	}

	public String readFillMode() throws DeviceException {
		isAvailable();
		try {
			fillMode = dev.read_attribute("fill_mode").extractString();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get fill mode", e);
		}		
		return fillMode;
	}

	public void writeFillMode(String fillMode) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("fill_mode", fillMode));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set fill mode", e);
		}		
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		if ("TriggerDelay".equalsIgnoreCase(attributeName)) {
			writeTriggerDelay((Double)value);
		} else if ("Threshold".equalsIgnoreCase(attributeName)) {
			writeThreshold((Integer) value);
		} else if ("ThresholdGain".equalsIgnoreCase(attributeName)) {
			writeThresholdGain((String) value);
		} else if ("FillMode".equalsIgnoreCase(attributeName)) {
			writeFillMode((String) value);
		} else if ("EnergyThreshold".equalsIgnoreCase(attributeName)) {
			writeEnergyThreshold((Float) value);
		} else if ("ScanNumber".equalsIgnoreCase(attributeName)) {
			setSavingNextNumber((Integer) value);
		} else if ("SavingDirectory".equalsIgnoreCase(attributeName)) {
			setSavingDirectory((String)value);
		}
		super.setAttribute(attributeName, value);
	}
	
	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		Object object = null;
		if ("TriggerDelay".equalsIgnoreCase(attributeName)) {
			object = readTriggerDelay();
		} else if ("Threshold".equalsIgnoreCase(attributeName)) {
			object = readThreshold();
		} else if ("ThresholdGain".equalsIgnoreCase(attributeName)) {
			object = readThresholdGain();
		} else if ("FillMode".equalsIgnoreCase(attributeName)) {
			object = readFillMode();
		} else if ("EnergyThreshold".equalsIgnoreCase(attributeName)) {
			object = readEnergyThreshold();
		} else if ("TotalFrames".equalsIgnoreCase(attributeName)) {
			object = timer.getAttribute("TotalFrames");
		} else if ("Cycles".equalsIgnoreCase(attributeName)) {
			object = timer.getAttribute("Cycles");
		} else if ("LastFileName".equalsIgnoreCase(attributeName)) {
			String index = String.format(getSavingIndexFormat(), getSavingNextNumber());
		      object = getSavingPrefix() + index + getSavingSuffix();
		} else {
			object = super.getAttribute(attributeName);
		}
		return object;
	}

	@Override
	public void update(Object source, Object arg) {
		if (arg != null && arg instanceof TimerStatus && configured) {
			TimerStatus ts = (TimerStatus) arg;
			if ("DEAD PAUSE".equals(ts.getCurrentStatus())) {
				try {
					stop();
					int nbFrames = timer.getCurrentFrames(ts.getCurrentFrame());
					System.out.println("Writing nbFrames " + nbFrames);
					writeNbFrames(nbFrames);
					writeSavingFramePerFile(nbFrames);
					writeExposureTime(timer.getCurrentLiveTime(ts.getCurrentFrame()));
					writeLatencyTime(timer.getCurrentDeadTime(ts.getCurrentFrame()));
					writeSavingOverwritePolicy("APPEND");
					writeSavingPrefix(getSavingPrefix());
					super.restart();
					synchronized (this) {
						ready++;
						System.out.println(">> ready " + ready + " detectorCount " + detectorCount);
						if (ready == detectorCount) {
							System.out.println("ready " + ready + " detectorCount " + detectorCount);
							ready = 0;
							System.out.println("Timer starting");
							timer.restart();
						}
					}
				} catch (DeviceException e) {
					logger.error("Unable to restart the timer from Pilatus "+ e.getMessage());
				}
			} else if ("IDLE".equals(ts.getCurrentStatus())) {
				System.out.println("Setting detector count back to zero");
				detectorCount = 0;
			}
		}
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return true;
	}
}
