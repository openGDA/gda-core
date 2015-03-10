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

package gda.device.detector.frelon;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceAttribute;
import gda.device.DeviceException;
import gda.device.TangoDeviceProxy;
import gda.device.Timer;
import gda.device.TimerStatus;
import gda.device.detector.corba.impl.DetectorAdapter;
import gda.device.detector.corba.impl.DetectorImpl;
import gda.device.detector.pilatus.TangoLimaDetector;
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
public class TangoFrelonDetector extends TangoLimaDetector implements InitializingBean, IObserver {

	private static final Logger logger = LoggerFactory.getLogger(TangoFrelonDetector.class);
	private TangoDeviceProxy dev = null;
	private Tfg timer;
	// Frelon attributes
	private String e2v_correction = null;
	private Short espia_dev_nb = null;
	private String image_mode = null;
	private String input_channel = null;
	private Integer roi_bin_offset = null;
	private String roi_mode = null;
	private String spb2_config = null;
	
	private static int detectorCount = 0;
	private static int ready = 0;
	private static int scanPoint;
	
	@Override
	public void configure() throws FactoryException {
		super.configure();
		try {
			init();
			if (e2v_correction != null)
				writeE2vCorrection(e2v_correction);
			if (image_mode != null)
				writeImageMode(image_mode);
			if (input_channel != null)
				writeInputChannel(input_channel);
			if (roi_bin_offset != null)
				writeRoiBinOffset(roi_bin_offset);
			if (roi_mode != null)
				writeRoiMode(roi_mode);
			if (spb2_config != null)
				writeSpb2Config(spb2_config);
			if (timer != null)
				timer.addIObserver(this);
			configured = true;
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
		System.out.println("************************************Frelon close() deleting timer IOBserver");
		timer.deleteIObserver(this);
	}
	
	@Override
	public void atScanStart() throws DeviceException {
		scanPoint = 0;
		writeSavingOverwritePolicy("OVERWRITE");
		writeSavingPrefix(getSavingPrefix());
		writeSavingSuffix(getSavingSuffix());
		writeSavingDirectory(getSavingDirectory());
	}
	
	@Override
	public void collectData() throws DeviceException {
		detectorCount++;
		writeNbFrames(timer.getCurrentFrames(1));
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
	public TangoDeviceProxy getFrelonTangoDeviceProxy() {
		return dev;
	}

	/**
	 * @param dev The Tango device proxy to set.
	 */
	public void setFrelonTangoDeviceProxy(TangoDeviceProxy dev) {
		this.dev = dev;
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		this.timer = (Tfg) timer;
	}

	public String getE2v_correction() {
		return e2v_correction;
	}

	public void setE2v_correction(String e2v_correction) {
		this.e2v_correction = e2v_correction;
	}

	public Short getEspia_dev_nb() {
		return espia_dev_nb;
	}

	public String getImage_mode() {
		return image_mode;
	}

	public void setImage_mode(String image_mode) {
		this.image_mode = image_mode;
	}

	public String getInput_channel() {
		return input_channel;
	}

	public void setInput_channel(String input_channel) {
		this.input_channel = input_channel;
	}

	public Integer getRoi_bin_offset() {
		return roi_bin_offset;
	}

	public void setRoi_bin_offset(Integer roi_bin_offset) {
		this.roi_bin_offset = roi_bin_offset;
	}

	public String getRoi_mode() {
		return roi_mode;
	}

	public void setRoi_mode(String roi_mode) {
		this.roi_mode = roi_mode;
	}

	public String getSpb2_config() {
		return spb2_config;
	}

	public void setSpb2_config(String spb2_config) {
		this.spb2_config = spb2_config;
	}

	public String readE2vCorrection() throws DeviceException {
		isAvailable();
		try {
			e2v_correction = dev.read_attribute("e2v_correction").extractString();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get e2v_correction", e);
		}		
		return e2v_correction;
	}
	
	public Short readEspiaDevNb() throws DeviceException {
		isAvailable();
		try {
			espia_dev_nb = dev.read_attribute("espia_dev_nb").extractShort();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get espia_dev_nb", e);
		}		
		return espia_dev_nb;
	}
	
	public String readImageMode() throws DeviceException {
		isAvailable();
		try {
			image_mode = dev.read_attribute("image_mode").extractString();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get image_mode", e);
		}		
		return image_mode;
	}
	
	public String readInputChannel() throws DeviceException {
		isAvailable();
		try {
			input_channel = dev.read_attribute("input_channel").extractString();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get input_channel", e);
		}		
		return input_channel;
	}
	
	public Integer readRoiBinOffset() throws DeviceException {
		isAvailable();
		try {
			roi_bin_offset = dev.read_attribute("roi_bin_offset").extractLong();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get roi_bin_offset", e);
		}		
		return roi_bin_offset;
	}
	
	public String readRoiMode() throws DeviceException {
		isAvailable();
		try {
			roi_mode = dev.read_attribute("roi_mode").extractString();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get roi_mode", e);
		}		
		return roi_mode;
	}
	
	public String readSpb2Config() throws DeviceException {
		isAvailable();
		try {
			spb2_config = dev.read_attribute("spb2_config").extractString();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get spb2_config", e);
		}		
		return spb2_config;
	}
	
	public void writeE2vCorrection(String e2v_correction) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("e2v_correction", e2v_correction));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set e2v_correction", e);
		}
	}
	
	public void writeImageMode(String image_mode) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("image_mode", image_mode));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set image_mode", e);
		}
	}
	
	public void writeInputChannel(String input_channel) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("input_channel", input_channel));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set input_channel", e);
		}
	}
	
	public void writeRoiBinOffset(Integer roi_bin_offset) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("roi_bin_offset", roi_bin_offset));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set roi_bin_offset", e);
		}
	}
	
	public void writeRoiMode(String roi_mode) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("roi_mode", roi_mode));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set roi_mode", e);
		}
	}
	
	public void writeSpb2Config(String spb2_config) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("spb2_config", spb2_config));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set spb2_config", e);
		}
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		if ("e2v_correction".equalsIgnoreCase(attributeName)) {
			writeE2vCorrection(e2v_correction);
		} else if ("image_mode".equalsIgnoreCase(attributeName)) {
			writeImageMode(image_mode);
		} else if ("input_channel".equalsIgnoreCase(attributeName)) {
			writeInputChannel(input_channel);
		} else if ("roi_bin_offset".equalsIgnoreCase(attributeName)) {
			writeRoiBinOffset(roi_bin_offset);
		} else if ("roi_mode".equalsIgnoreCase(attributeName)) {
			writeRoiMode(roi_mode);
		} else if ("spb2_config".equalsIgnoreCase(attributeName)) {
			writeSpb2Config(spb2_config);
		}
		super.setAttribute(attributeName, value);
	}
	
	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		Object object = null;
		if ("e2v_correction".equalsIgnoreCase(attributeName)) {
			object = readE2vCorrection();
		} else if ("espia_dev_nb".equalsIgnoreCase(attributeName)) {
			object = readEspiaDevNb();
		} else if ("image_mode".equalsIgnoreCase(attributeName)) {
			object = readImageMode();
		} else if ("input_channel".equalsIgnoreCase(attributeName)) {
			object = readInputChannel();
		} else if ("roi_bin_offset".equalsIgnoreCase(attributeName)) {
			object = readRoiBinOffset();
		} else if ("roi_mode".equalsIgnoreCase(attributeName)) {
			object = readRoiMode();
		} else if ("spb2_config".equalsIgnoreCase(attributeName)) {
			object = readSpb2Config();
		} else if ("TotalFrames".equalsIgnoreCase(attributeName)) {
			object = timer.getAttribute("TotalFrames");
		} else if ("Cycles".equalsIgnoreCase(attributeName)) {
			object = timer.getAttribute("Cycles");
		} else {
			object = super.getAttribute(attributeName);
		}
		return object;
	}

	@Override
	public void update(Object source, Object arg) {
		if (arg != null && arg instanceof TimerStatus) {
			TimerStatus ts = (TimerStatus) arg;
			if ("DEAD PAUSE".equals(ts.getCurrentStatus())) {
				try {
					writeNbFrames(timer.getCurrentFrames(ts.getCurrentFrame()));
					writeExposureTime(timer.getCurrentLiveTime(ts.getCurrentFrame()));
					writeLatencyTime(timer.getCurrentDeadTime(ts.getCurrentFrame()));
					writeSavingOverwritePolicy("ABORT");
					writeSavingPrefix(getSavingPrefix());
					super.collectData();
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
					logger.error("Unable to restart the timer from frelon" + e.getMessage());
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
