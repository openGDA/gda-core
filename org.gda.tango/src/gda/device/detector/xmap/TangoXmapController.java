/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.detector.xmap;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevState;
import fr.esrf.TangoApi.DeviceData;
import gda.device.Detector;
import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.TangoDeviceProxy;
import gda.factory.FactoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class TangoXmapController extends DeviceBase implements XmapController, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(TangoXmapController.class);

	private TangoDeviceProxy tangoDeviceProxy = null;
	private int numberOfElements; // Also known as the number of channels not to be confused with MCA channels
	private int numberOfScas; // Also known as regions of interest
	private String filePrefix;
	private String fileSuffix;
	private String filePath = null;
	private int fileIndex;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (tangoDeviceProxy == null) {
			throw new IllegalArgumentException("Tango xmap device proxy needs to be set");
		}
	}

	public TangoDeviceProxy getTangoDeviceProxy() {
		return tangoDeviceProxy;
	}

	public void setTangoDeviceProxy(TangoDeviceProxy tangoDeviceProxy) {
		this.tangoDeviceProxy = tangoDeviceProxy;
	}

	@Override
	public void configure() throws FactoryException {
		try {
			tangoDeviceProxy.isAvailable();
			numberOfElements = tangoDeviceProxy.getAttributeAsInt("numberOfElements");
			numberOfScas = tangoDeviceProxy.getAttributeAsInt("numberOfScas");
			tangoDeviceProxy.setAttribute("filePrefix", filePrefix);
			tangoDeviceProxy.setAttribute("fileSuffix", fileSuffix);
			tangoDeviceProxy.setAttribute("filePath", filePath);
			setConfigured(true);
		} catch (DeviceException e) {
			logger.error("xmap device server is not available", e.getMessage());
			setConfigured(false);
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
			setConfigured(false);
		}
	}

	@Override
	public void clearAndStart() throws DeviceException {
		try {
			tangoDeviceProxy.isAvailable();
			tangoDeviceProxy.command_inout("Clear");
			tangoDeviceProxy.command_inout("Start");
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
			throw new DeviceException(e.errors[0].desc);
		}
	}

	@Override
	public void start() throws DeviceException {
		try {
			tangoDeviceProxy.isAvailable();
			tangoDeviceProxy.command_inout("Start");
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
			throw new DeviceException(e.errors[0].desc);
		}
	}

	@Override
	public void stop() throws DeviceException {
		try {
			tangoDeviceProxy.isAvailable();
			tangoDeviceProxy.command_inout("Stop");
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
			throw new DeviceException(e.errors[0].desc);
		}
	}

	public void setFileIndex(int fileIndex) throws DeviceException {
		try {
			tangoDeviceProxy.isAvailable();
			tangoDeviceProxy.setAttribute("fileIndex", fileIndex);
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
			throw new DeviceException(e.errors[0].desc);
		}
	}

	public double[] readStats() throws DeviceException {
		try {
			tangoDeviceProxy.isAvailable();
			DeviceData argout = tangoDeviceProxy.command_inout("ReadStats");
			// return argout.extractDoubleArray();
			// testing
			double[] d = { 0., 1., 2., 3., 4., 5., 0., 1., 2., 3., 4., 5., 0., 1., 2., 3., 4., 5., 0., 1., 2., 3., 4., 5. };
			return d;
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
			throw new DeviceException(e.errors[0].desc);
		}
	}

	public String getFilePrefix() {
		return filePrefix;
	}

	public void setFilePrefix(String filePrefix) {
		this.filePrefix = filePrefix;
	}

	public String getFileSuffix() {
		return fileSuffix;
	}

	public void setFileSuffix(String fileSuffix) {
		this.fileSuffix = fileSuffix;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public void setAllROIs(int[] roiList) throws DeviceException {
		try {
			DeviceData args = new DeviceData();
			args.insert(roiList);
			tangoDeviceProxy.isAvailable();
			tangoDeviceProxy.command_inout("SetScas", args);
			numberOfScas = tangoDeviceProxy.getAttributeAsInt("numberOfScas");
			// testing
			numberOfScas = roiList.length / (3 * numberOfElements);
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
			throw new DeviceException(e.errors[0].desc);
		}
	}

	@Override
	public void setNumberOfBins(int numberOfBins) throws DeviceException {
		try {
			tangoDeviceProxy.isAvailable();
			tangoDeviceProxy.setAttribute("mcaBins", numberOfBins);
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
			throw new DeviceException(e.errors[0].desc);
		}
	}

	public int getMaxScas() {
		return 32; // this should come from the device server
	}

	@Override
	public int getNumberOfROIs() {
		return numberOfScas;
	}

	@Override
	public int getNumberOfElements() throws DeviceException {
		return numberOfElements;
	}

	@Override
	public int getNumberOfBins() throws DeviceException {
		try {
			tangoDeviceProxy.isAvailable();
			return tangoDeviceProxy.getAttributeAsInt("mcaBins");
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
			throw new DeviceException(e.errors[0].desc);
		}
	}

	public int getMCASize() {
		int size = 0;
		try {
			size = tangoDeviceProxy.getAttributeAsInt("mcaLength");
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
		}
		return size;
	}

	@Override
	public double[] getROIsSum() throws DeviceException {
		double[] roiSum = new double[numberOfScas];
		for (int j = 0; j < numberOfElements; j++) {
			double roi[] = this.getROIs(j);
			for (int i = 0; i < numberOfScas; i++) {
				roiSum[i] += roi[i];
			}
		}
		return roiSum;
	}

	@Override
	public double[] getROICounts(int roiIndex) throws DeviceException {
		double[] roiCounts = new double[numberOfElements];
		for (int j = 0; j < numberOfElements; j++) {
			double roi[] = this.getROIs(j);
			roiCounts[j] = roi[roiIndex];
		}
		return roiCounts;
	}

	@Override
	public double[] getROIs(int elementNumber) throws DeviceException {
		try {
			tangoDeviceProxy.isAvailable();
			DeviceData args = new DeviceData();
			args.insert(elementNumber);
			// DeviceData argout = tangoDeviceProxy.command_inout("ReadScas", args);
			// return argout.extractDoubleArray();
			// for testing only
			double[] rawData = new double[numberOfScas];
			for (int i = 0; i < numberOfScas; i++) {
				// rawData[i] = (double) ((Math.random() + 1) * 10);
				rawData[i] = (double) (elementNumber + 2) * (i + 2);
			}
			return rawData;
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
			throw new DeviceException(e.errors[0].desc);
		}
	}

	@Override
	public int getStatus() throws DeviceException {
		int status;
		try {
			DevState state = tangoDeviceProxy.state();
			if (state == DevState.MOVING) {
				status = Detector.BUSY;
			} else if (state == DevState.ON) {
				status = Detector.IDLE;
			} else {
				status = Detector.FAULT;
			}
		} catch (DevFailed e) {
			throw new DeviceException("Tango device server " + tangoDeviceProxy.get_name() + " failed");
		} catch (Exception e) {
			throw new DeviceException("Tango device server stuffed");
		}
		return status;
	}

	public double[][] getMCAData(double time) throws DeviceException {
		double[] data = null;
		int total = 0;
		int mcaSize = getMCASize();
		double[][] output = new double[numberOfElements][mcaSize];
		// testing
		// try {
		// tangoDeviceProxy.isAvailable();
		// tangoDeviceProxy.setAttribute("PresetTime", time);
		// tangoDeviceProxy.command_inout("Start");
		// while (getStatus() == Detector.BUSY) {
		// try {
		// Thread.sleep(100);
		// total += 100;
		// if (total >= (time + time / 10))
		// break;
		// } catch (InterruptedException e) {
		// logger.error("Sleep interrupted", e);
		// }
		// }
		// DeviceData argout = tangoDeviceProxy.command_inout("ReadMca");
		// data = argout.extractDoubleArray();
		// } catch (DevFailed e) {
		// logger.error(e.errors[0].desc);
		// throw new DeviceException(e.errors[0].desc);
		// }
		// if (data != null) {
		int k = 1; // for testing
		for (int element = 0; element < numberOfElements; element++) {
			for (int energy = 0; energy < mcaSize; energy++) {
				// testing output[element][energy] = data[energy];
				output[element][energy] = k++;
				}
			}
		// }
		return output;
	}

	@Override
	public void setAcquisitionTime(double time) throws DeviceException {
		// xmap is under the control of the tfg
	}

	// Implemented but not used for here down

	@Override
	public double getICR(int element) throws DeviceException {
		try {
			return tangoDeviceProxy.getAttributeAsDouble("ICR");
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
			throw new DeviceException(e.errors[0].desc);
		}
	}

	@Override
	public double getOCR(int element) throws DeviceException {
		try {
			return tangoDeviceProxy.getAttributeAsDouble("OCR");
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
			throw new DeviceException(e.errors[0].desc);
		}
	}

	@Override
	public double getRealTime() throws DeviceException {
		try {
			return tangoDeviceProxy.getAttributeAsInt("realTime");
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
			throw new DeviceException(e.errors[0].desc);
		}
	}

	@Override
	public int getEvents(int element) throws DeviceException {
		try {
			return tangoDeviceProxy.getAttributeAsInt("events");
		} catch (DevFailed e) {
			logger.error(e.errors[0].desc);
			throw new DeviceException(e.errors[0].desc);
		}
	}

	@Override
	public void setNumberOfElements(int numberOfElements) throws DeviceException {
		logger.warn("setNumberOfElements(int) not implemented in the tango xmap interface");
	}

	@Override
	public void setStatusRate(double statusRate) throws DeviceException {
		logger.warn("setStatusRate(double) not implemented in the tango xmap interface");
	}

	@Override
	public double getStatusRate() throws DeviceException {
		logger.warn("getStatusRate() not implemented in the tango xmap interface");
		return 0;
	}

	@Override
	public void setReadRate(double readRate) throws DeviceException {
		logger.warn("setReadRate(double) not implemented in the tango xmap interface");
	}

	@Override
	public double getReadRate() throws DeviceException {
		logger.warn("getReadRate() not implemented in the tango xmap interface");
		return 0;
	}

	@Override
	public void setStatusRate(String statusRate) throws DeviceException {
		logger.warn("setStatusRate(String) not implemented in the tango xmap interface");
	}

	@Override
	public void setReadRate(String readRate) throws DeviceException {
		logger.warn("setReadRate() not implemented in the tango xmap interface");
	}

	@Override
	public void deleteROIs(int mcaIndex) throws DeviceException {
		logger.warn("deleteROIs(int) not implemented in the tango xmap interface");
	}

	@Override
	public void setNthROI(double[][] rois, int roiIndex) throws DeviceException {
		logger.warn("setNthROI() not implemented in the tango xmap interface");
	}

	@Override
	public void setROI(double[][] rois, int mcaIndex) throws DeviceException {
		logger.warn("setROI(double[][],int) not implemented in the tango xmap interface");
	}

	@Override
	public void setROIs(double[][] rois) throws DeviceException {
		logger.warn("setROIs(double[][]) not implemented in the tango xmap interface");
	}

	@Override
	public int[] getData(int elementNumber) throws DeviceException {
		logger.warn("getData(int) not implemented in the tango xmap interface");
		return null;
	}

	@Override
	public int[][] getData() throws DeviceException {
		logger.warn("getData() not implemented in the tango xmap interface");
		return null;
	}

	@Override
	public double getAcquisitionTime() throws DeviceException {
		logger.warn("getAcquisitionTime() not implemented in the tango xmap interface");
		return 0;
	}

	@Override
	public void setNumberOfROIs(int numberOfROIs) {
		logger.warn("setNumberOfROIs(int) not implemented in the tango xmap interface");
	}

	@Override
	public double[] getROIs(int mcaNumber, int[][] data) throws DeviceException {
		logger.warn("getROIs(int, int[][]) not implemented in the tango xmap interface");
		return null;
	}

}