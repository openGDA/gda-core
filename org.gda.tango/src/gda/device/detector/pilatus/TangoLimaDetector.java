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
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceData;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.TangoDeviceProxy;
import gda.device.detector.DetectorBase;
import gda.device.detector.corba.impl.DetectorAdapter;
import gda.device.detector.corba.impl.DetectorImpl;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

@CorbaAdapterClass(DetectorAdapter.class)
@CorbaImplClass(DetectorImpl.class)
public class TangoLimaDetector extends DetectorBase implements Detector, Scannable, Configurable, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(TangoLimaDetector.class);
	private TangoDeviceProxy dev = null;
	
	private int width = 1;
	private int height = 1;
	private double exposureTime = 0.0;
	private int frames = 1;
	private boolean createsOwnFile = false;
	private String detectorType = null;
	private String detectorID = "0";
	private String detectorDescription = null;
	private String imageType = null;
	private int lastImageReady = -1;
	private int lastImageSaved = -1;
	private int lastImageAcquired = -1;
	private boolean readyForNextAcq = false;
	private boolean readyForNextImage = false;
	private String savingFormat = "EDF";
	private String savingSuffix = null;
	private String savingPrefix = "img_";
	private String savingDirectory = "/tmp";
	private String savingMode = "MANUAL";
	private String savingIndexFormat = "%04d";
	private int nextNumber;
		
	private String triggerMode = null;
	private String acqMode = null;
	private Double latencyTime = null;

	@Override
	public void configure() throws FactoryException {
		try {
			init();
			getWidth();
			getHeight();
			readDetectorType();
			readDetectorDescription();
			readImageType();
			configured = true;
		} catch (Exception e) {
			logger.error("TangoLimaDetector {} configure: {}", getName(), e);
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
		logger.debug("TangoLimaDetector reconfiguring {}", getName());
		configure();
	}

	/**
	 * @return Returns the Tango device proxy.
	 */
	public TangoDeviceProxy getLimaTangoDeviceProxy() {
		return dev;
	}

	/**
	 * @param dev The Tango device proxy to set.
	 */
	public void setLimaTangoDeviceProxy(TangoDeviceProxy dev) {
		this.dev = dev;
	}

	public String getTriggerMode() {
		return triggerMode;
	}

	public void setTriggerMode(String triggerMode) {
		this.triggerMode = triggerMode;
	}

	public String getAcqMode() {
		return acqMode;
	}

	public void setAcqMode(String acqMode) {
		this.acqMode = acqMode;
	}

	public Double getLatencyTime() {
		return latencyTime;
	}

	public void setLatencyTime(Double latencyTime) {
		this.latencyTime = latencyTime;
	}

	public String getSavingFormat() {
		return savingFormat;
	}

	public void setSavingFormat(String savingFormat) {
		this.savingFormat = savingFormat;
	}

	public String getSavingIndexFormat() {
		return savingIndexFormat;
	}

	public void setSavingIndexFormat(String savingIndexFormat) {
		this.savingIndexFormat = savingIndexFormat;
	}

	public String getSavingSuffix() {
		return savingSuffix;
	}

	public void setSavingSuffix(String savingSuffix) {
		this.savingSuffix = savingSuffix;
	}

	public String getSavingPrefix() {
		return savingPrefix;
	}

	public void setSavingPrefix(String savingPrefix) {
		this.savingPrefix = savingPrefix;
	}

	public String getSavingDirectory() {
		return savingDirectory;
	}

	public void setSavingDirectory(String savingDirectory) {
		this.savingDirectory = savingDirectory;
	}

	public String getSavingMode() {
		return savingMode;
	}

	public void setSavingMode(String savingMode) {
		this.savingMode = savingMode;
	}

	public int getSavingNextNumber() {
		return nextNumber;
	}

	public void setSavingNextNumber(int nextNumber) {
		this.nextNumber = nextNumber;
	}

	public int getWidth() throws DeviceException {
		isAvailable();
		try {
			width = (int) dev.read_attribute("image_width").extractULong();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get image width for " + getName(), e);
		}
		return width;
	}

	public int getHeight() throws DeviceException {
		isAvailable();
		try {
			height = (int) dev.read_attribute("image_height").extractULong();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get image height for "+ getName(), e);
		}		
		return height;
	}

	@Override
	public void setCollectionTime(double seconds) {
		try {
			writeExposureTime(seconds);
		} catch (DeviceException e) {
			e.printStackTrace();
		}
	}
	
	public void init() throws DeviceException {
		isAvailable();
	}

	@Override
	public void stop() throws DeviceException {
		isAvailable();
		try {
			dev.command_inout("StopAcq");
		} catch (DevFailed e) {
			throw new DeviceException("failed to stop " + getName(), e);
		}		
	}
		
	public void reset() throws DeviceException
	{
		try {
			dev.command_inout("Reset");
		} catch (DevFailed e) {
			throw new DeviceException("failed to reset "+ getName(), e);
		}
	}
	
	@Override
	public void collectData() throws DeviceException {
		isAvailable();
		try {
			if (acqMode != null)
				writeAcqMode(acqMode);
			if (savingFormat != null)
				writeSavingFormat(savingFormat);
			if (savingIndexFormat != null)
				writeSavingIndexFormat(savingIndexFormat);
			if (savingSuffix != null)
				writeSavingSuffix(savingSuffix);
			if (savingPrefix != null)
				writeSavingPrefix(savingPrefix);
			if (savingDirectory != null)
				writeSavingDirectory(savingDirectory);
			if (savingMode != null)
				writeSavingMode(savingMode);
			if (triggerMode != null)
				writeTriggerMode(triggerMode);

			writeSavingNextNumber(nextNumber);

			System.out.println("Check for readiness **************************");
			for (int i=0; i<100; i++) {
				if (readReadyForNextAcq()) {
					break;
				}
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					// ignore
				}
			}
			if (readReadyForNextAcq()) {
				System.out.println("Preparing acq **************************");
				dev.command_inout("PrepareAcq");
				System.out.println("Prepared **************************");
				// Add small sleep time for slower detectors to respond before start arrives.
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// ignore
				}
			} else {
				//Lets throw until we figure out why its not ready!
				throw new DeviceException(getName() + " not ready for acquisition ");
			}
		} catch (DevFailed e) {
			throw new DeviceException("failed to prepare acq for " + getName(), e);
		}		
		try {
			dev.command_inout("StartAcq");
		} catch (DevFailed e) {
			throw new DeviceException("failed to start acq for " + getName(), e);
		}		
	}


	public void restart() throws DeviceException {
		isAvailable();
		writeSavingNextNumber(nextNumber);
		boolean ready = false;
		for (int i = 0; i < 100; i++) {
			ready = readReadyForNextAcq();
			if (ready) {
				try {
					dev.command_inout("PrepareAcq");
					break;
				} catch (DevFailed e) {
					// ignore
				}
			} else {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
		if (!ready) {
			// Lets throw until we figure out why its not ready!
			throw new DeviceException(getName() + " not ready for acquisition ");
		}
		try {
			dev.command_inout("StartAcq");
		} catch (DevFailed e) {
			throw new DeviceException("failed to start acq for " + getName(), e);
		}
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return createsOwnFile;
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return detectorType;
	}
	
	@Override
	public String getDescription() throws DeviceException {
		return detectorDescription;
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return detectorID;
	}

	public String getDetectorDescription() {
		return detectorDescription;
	}

	public void setDetectorID(String detectorID) {
		this.detectorID = detectorID;
	}

	public void setDetectorType(String detectorType) {
		this.detectorType = detectorType;
	}

	@Override
	public int getStatus() throws DeviceException {
		int istat = Detector.FAULT;
		isAvailable();
		try {
			String status = dev.read_attribute("acq_status").extractString();
			if ("ready".equalsIgnoreCase(status)) {
				istat = Detector.IDLE;
			} else if ("fault".equalsIgnoreCase(status)) {
				istat = Detector.FAULT;
			} else { // "running", "readout"
				istat = Detector.BUSY;
			}
		} catch (DevFailed e) {
			throw new DeviceException("failed to get status from " + getName(), e);
		}		
		return istat;
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return new int[]{height, width};
	}

	@Override
	public Object readout() throws DeviceException {
		byte[] byteData = null;	
		int[] intData = new int[width*height*frames];
		isAvailable();
		try {
			int n = 0;
			if (readLastImageReady() != frames-1)
			{
				logger.error("TangoLimaDetector: readout: image not ready {} should be ", readLastImageReady(), frames-1);
				throw new DeviceException("TangoLimaDetector: readout: image not ready");				
			}
			for (int k = 0; k < frames; k++) {
				DeviceData argin = new DeviceData();
				argin.insert(k);
				DeviceData argout = dev.command_inout("getImage", argin);
				byteData = argout.extractByteArray();
				
				if ("Bpp32S".equalsIgnoreCase(imageType)) {
					if (byteData.length !=  width*height*4) {
						logger.error("TangoLimaDetector.readout failed: expected {} bytes, got {}", width*height*4, byteData.length);
						throw new DeviceException("TangoLimaDetector.readout failed to read all the image data");
					}
					for (int j = 0; j < byteData.length; j += 4, n++) {
						intData[n] = ((byteData[j + 3] & 0xff) << 24) | ((byteData[j + 2] & 0xff) << 16)
								| ((byteData[j + 1] & 0xff) << 8) | (byteData[j] & 0xff);
					}
				} else if ("Bpp16".equalsIgnoreCase(imageType)) {
					if (byteData.length !=  width*height*2) {
						logger.error("TangoLimaDetector.readout failed: expected {} bytes, got {}", width*height*2, byteData.length);
						throw new DeviceException("TangoLimaDetector.readout failed to read all the image data");
					}
					for (int j = 0; j < byteData.length; j += 2, n++) {
						intData[n] = ((byteData[j + 1] & 0xff) << 8) | (byteData[j] & 0xff);
					}
				} else {
					logger.error("TangoLimaDetector: readout: Unsupported image type {}", imageType);
					throw new DeviceException("TangoLimaDetector: readout: Unsupported image type");
				}
			}
		} catch (DevFailed e) {
			logger.error("TangoLimaDetector.readout failed {}", e.errors[0].desc);
			throw new DeviceException(e.errors[0].desc);
		}
		return intData;
	}

	public Object readLastImage() throws DeviceException {
		byte[] byteData = null;	
		double[] data = new double[width*height];
		try {
			dev.isAvailable();
			int n = 0;
			int last = readLastImageAcquired();
			logger.debug("TangoLimaDetector is reading last image nos: {}", last);
			if (last >= 0) {
				DeviceData argin = new DeviceData();
				argin.insert(last);
				DeviceData argout = dev.command_inout("getImage", argin);
				byteData = argout.extractByteArray();
				
				if ("Bpp32S".equalsIgnoreCase(imageType)) {
					if (byteData.length !=  width*height*4) {
						logger.error("TangoLimaDetector.readout failed: expected {} bytes, got {}", width*height*4, byteData.length);
						throw new DeviceException("TangoLimaDetector.readout failed to read all the image data");
					}
					for (int j = 0; j < byteData.length; j += 4, n++) {
						data[n] = ((byteData[j + 3] & 0xff) << 24) | ((byteData[j + 2] & 0xff) << 16)
								| ((byteData[j + 1] & 0xff) << 8) | (byteData[j] & 0xff);
					}
				} else if ("Bpp16".equalsIgnoreCase(imageType)) {
					if (byteData.length !=  width*height*2) {
						logger.error("TangoLimaDetector.readout failed: expected {} bytes, got {}", width*height*2, byteData.length);
						throw new DeviceException("TangoLimaDetector.readout failed to read all the image data");
					}
					for (int j = 0; j < byteData.length; j += 2, n++) {
						data[n] = ((byteData[j + 1] & 0xff) << 8) | (byteData[j] & 0xff);
					}
				} else {
					logger.error("TangoLimaDetector: readout: Unsupported image type {}", imageType);
					throw new DeviceException("TangoLimaDetector: readout: Unsupported image type");
				}
			}
		} catch (DevFailed e) {
			logger.error("TangoLimaDetector.readout failed {}", e.errors[0].desc);
			throw new DeviceException(e.errors[0].desc);
		}
		return data;
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		Object object = null;
		if ("ReadLastImage".equalsIgnoreCase(attributeName)) {
			object = readLastImage();
		}
		return object;
	}

	protected void isAvailable() throws DeviceException {
		try {
			if (dev.state().value() == DevState._FAULT) {
				throw new DeviceException("Tango device server " + dev.get_name() + " shows fault");
			}
			// Is the device still connected or just started or at fault
			if (dev.read_attribute("acq_status").extractString().equalsIgnoreCase("Fault")) {
				logger.info("TangoLimaDetector {} status at fault: Sending reset", getName());
				reset();
			}
			setConfigured(true);
		} catch (DevFailed e) {
			// device has lost connection
			setConfigured(false);
			throw new DeviceException("Tango device server " + getName() + " failed");
		} catch (Exception e) {
			throw new DeviceException("Tango device server stuffed");			
		}
	}

	public double readExposureTime() throws DeviceException {
		isAvailable();
		try {
			exposureTime = dev.read_attribute("acq_expo_time").extractDouble();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get exposure time", e);
		}		
		return exposureTime;
	}

	public void writeExposureTime(double exposureTime) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("acq_expo_time", exposureTime));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set exposure time for " + getName(), e);
		}
		this.exposureTime = exposureTime;
	}

	public int readNbFrames() throws DeviceException {
		isAvailable();
		try {
			frames = dev.read_attribute("acq_nb_frames").extractLong();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get nbFrames", e);
		}		
		return frames;
	}

	public void writeNbFrames(int nbFrames) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("acq_nb_frames", nbFrames));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set nbFrames", e);
		}
		this.frames = nbFrames;
	}

	public double readLatencyTime() throws DeviceException {
		isAvailable();
		try {
			 latencyTime = dev.read_attribute("latency_time").extractDouble();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get latency time", e);
		}		
		return latencyTime;
	}

	public void writeLatencyTime(double latencyTime) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("latency_time", latencyTime));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set latency Time", e);
		}	
		this.latencyTime = latencyTime;
	}

	public String readTriggerMode() throws DeviceException {
		isAvailable();
		try {
			triggerMode = dev.read_attribute("acq_trigger_mode").extractString();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get trigger mode", e);
		}		
		return triggerMode;
	}

	public void writeTriggerMode(String triggerMode) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("acq_trigger_mode", triggerMode));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set trigger mode", e);
		}		
	}

	public String readAcqMode() throws DeviceException {
		isAvailable();
		try {
			acqMode = dev.read_attribute("acq_mode").extractString();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get acquisition mode", e);
		}		
		return acqMode;
	}

	public void writeAcqMode(String acqMode) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("acq_mode", acqMode));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set acquisition mode", e);
		}
		this.acqMode = acqMode;
	}

	public String readDetectorType() throws DeviceException {
		isAvailable();
		try {
			detectorType = dev.read_attribute("camera_type").extractString();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get camera type", e);
		}
		return detectorType;
	}

	public String readDetectorDescription() throws DeviceException {
		isAvailable();
		try {
			detectorDescription = dev.read_attribute("camera_model").extractString();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get camera model", e);
		}
		return detectorDescription;
	}

	public String readImageType() throws DeviceException {
		isAvailable();
		try {
			imageType = dev.read_attribute("image_type").extractString();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get image type", e);
		}
		return imageType;
	}

	public int readLastImageReady() throws DeviceException {
		isAvailable();
		try {
			lastImageReady = dev.read_attribute("last_image_ready").extractLong();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get last image ready", e);
		}		
		return lastImageReady;
	}

	public int readLastImageSaved() throws DeviceException {
		isAvailable();
		try {
			lastImageSaved = dev.read_attribute("last_image_saved").extractLong();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get last image saved", e);
		}		
		return lastImageSaved;
	}

	public int readLastImageAcquired() throws DeviceException {
		isAvailable();
		try {
			lastImageAcquired = dev.read_attribute("last_image_acquired").extractLong();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get last image acquired", e);
		}		
		return lastImageAcquired;
	}

	public boolean readReadyForNextAcq() throws DeviceException {
		isAvailable();
		try {
			readyForNextAcq = dev.read_attribute("ready_for_next_acq").extractBoolean();
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$ ready " + readyForNextAcq);
		} catch (DevFailed e) {
			throw new DeviceException("failed to get ready for next acq", e);
		}		
		return readyForNextAcq;
	}

	public boolean readReadyForNextImage() throws DeviceException {
		isAvailable();
		try {
			readyForNextImage = dev.read_attribute("ready_for_next_image").extractBoolean();
		} catch (DevFailed e) {
			throw new DeviceException("failed to get ReadyForNextAcqlast image saved", e);
		}		
		return readyForNextImage;
	}

	public void writeSavingOverwritePolicy(String policy) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("saving_overwrite_policy", policy));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set saving overwrite policy " + policy, e);
		}		
	}

	public void writeSavingFormat(String format) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("saving_format", format));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set saving format " + format, e);
		}		
	}

	public void writeSavingIndexFormat(String format) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("saving_index_format", format));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set saving index format " + format, e);
		}		
	}

	public void writeSavingNextNumber(int nextNumber) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("saving_next_number", nextNumber));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set saving next number "+ nextNumber, e);
		}		
	}

	public void writeSavingSuffix(String suffix) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("saving_suffix", suffix));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set saving suffix " + suffix, e);
		}
	}

	public void writeSavingPrefix(String prefix) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("saving_prefix", prefix));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set saving prefix " + prefix, e);
		}
	}

	public void writeSavingDirectory(String directory) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("saving_directory", directory));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set saving directory " + directory, e);
		}
	}

	public void writeSavingMode(String mode) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("saving_mode", mode));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set saving mode " + mode, e);
		}
	}

	public void writeSavingFramePerFile(int frames) throws DeviceException {
		isAvailable();
		try {
			dev.write_attribute(new DeviceAttribute("saving_frame_per_file", frames));
		} catch (DevFailed e) {
			throw new DeviceException("failed to set saving frame per file ", e);
		}
	}
}
