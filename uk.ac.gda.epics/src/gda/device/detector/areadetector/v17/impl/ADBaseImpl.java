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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import gda.device.Detector;
import gda.device.detector.areadetector.AreaDetectorBin;
import gda.device.detector.areadetector.AreaDetectorROI;
import gda.device.detector.areadetector.impl.AreaDetectorBinImpl;
import gda.device.detector.areadetector.impl.AreaDetectorROIImpl;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;
import gda.epics.connection.EpicsController;
import gda.observable.Observable;
import gda.observable.Predicate;
import gov.aps.jca.CAException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

public class ADBaseImpl implements InitializingBean, ADBase {

	public class StartPutListener implements PutListener {
		@Override
		public void putCompleted(PutEvent event) {
			if (event.getStatus() != CAStatus.NORMAL) {
				logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(),
						event.getStatus());
				setStatus(Detector.FAULT);
				return;
			}
			logger.info("Acquisition request completed: {} called back.", ((Channel) event.getSource()).getName());
			setStatus(Detector.IDLE);
		}
	}

	// Setup the logging facilities
	private static final Logger logger = LoggerFactory.getLogger(ADBaseImpl.class);

	protected final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	private String basePVName;

	/**
	 * Map that stores the channel against the PV name
	 */
	private Map<String, Channel> channelMap = new HashMap<String, Channel>();

	private Integer initialMinX;

	private Integer initialMinY;

	private Integer initialSizeX;

	private Integer initialSizeY;

	private String initialDataType;

	private Integer initialBinX;

	private Integer initialBinY;

	private String initialNDAttributesFile;

	private StartPutListener startputlistener = new StartPutListener();

	private volatile int status = Detector.IDLE;

	private Object statusMonitor = new Object();

	private PV<Integer> pvArrayCounter_RBV;

	private PV<Integer> pvDetectorState_RBV;

	private Map<Short, NDPluginBase.DataType> dataTypeRBV_Map;

	private ReadOnlyPV<Short> detectorStatePV = null;

	/**
	*
	*/
	@Override
	public String getPortName_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.caget(getChannel(PortName_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getPortName_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getManufacturer_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.caget(getChannel(Manufacturer_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getManufacturer_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getModel_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.caget(getChannel(Model_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getModel_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getMaxSizeX_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(MaxSizeX_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getMaxSizeX_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getMaxSizeY_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(MaxSizeY_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getMaxSizeY_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getDataType() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(DataType));
		} catch (Exception ex) {
			logger.warn("Cannot getDataType", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setDataType(String datatype) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(DataType), datatype);
		} catch (Exception ex) {
			logger.warn("Cannot setDataType", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getDataType_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(DataType_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getDataType_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public NDPluginBase.DataType getDataType_RBV2() throws Exception {
		try {
			Channel channel = getChannel(DataType_RBV);
			short val = EPICS_CONTROLLER.cagetEnum(channel);
			if (dataTypeRBV_Map == null) {
				dataTypeRBV_Map = createDataTypeMap(channel);
			}
			return dataTypeRBV_Map.get(val);
		} catch (Exception ex) {
			logger.warn("Cannot getDataType_RBV", ex);
			throw ex;
		}
	}

	private Map<Short, NDPluginBase.DataType> createDataTypeMap(Channel channel) throws TimeoutException, CAException,
			InterruptedException,
			Exception {
		Map<Short, NDPluginBase.DataType> map = new HashMap<Short, NDPluginBase.DataType>();
		String [] labels = EPICS_CONTROLLER.cagetLabels(channel);
		for(int i=0; i< labels.length; i++){
			String label = labels[i].toUpperCase();
			short key = (short) i;
			switch(label){
			case "INT8":
				map.put(key,NDPluginBase.DataType.INT8);
				break;
			case "INT16":
				map.put(key,NDPluginBase.DataType.INT16);
				break;
			case "INT32":
				map.put(key,NDPluginBase.DataType.INT32);
				break;
			case "UINT8":
				map.put(key,NDPluginBase.DataType.UINT8);
				break;
			case "UINT16":
				map.put(key,NDPluginBase.DataType.UINT16);
				break;
			case "UINT32":
				map.put(key,NDPluginBase.DataType.UINT32);
				break;
			case "FLOAT32":
				map.put(key,NDPluginBase.DataType.FLOAT32);
				break;
			case "FLOAT64":
				map.put(key,NDPluginBase.DataType.FLOAT64);
				break;
			default:
				throw new Exception("Inavalid data type label " + StringUtils.quote(label));
			}
		}
		return map;
	}

	/**
	*
	*/
	@Override
	public short getColorMode() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ColorMode));
		} catch (Exception ex) {
			logger.warn("Cannot getColorMode", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setColorMode(int colormode) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(ColorMode), colormode);
		} catch (Exception ex) {
			logger.warn("Cannot setColorMode", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getColorMode_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ColorMode_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getColorMode_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBinX() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(BinX));
		} catch (Exception ex) {
			logger.warn("Cannot getBinX", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setBinX(int binx) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(BinX), binx);
		} catch (Exception ex) {
			logger.warn("Cannot setBinX", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBinX_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(BinX_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getBinX_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBinY() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(BinY));
		} catch (Exception ex) {
			logger.warn("Cannot getBinY", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setBinY(int biny) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(BinY), biny);
		} catch (Exception ex) {
			logger.warn("Cannot setBinY", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBinY_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(BinY_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getBinY_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getMinX() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(MinX));
		} catch (Exception ex) {
			logger.warn("Cannot getMinX", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setMinX(int minx) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(MinX), minx);
		} catch (Exception ex) {
			logger.warn("Cannot setMinX", ex);
			throw ex;
		}
	}

	@Override
	public void setMinXWait(int minx, double timeout) throws Exception {
		try {
			EPICS_CONTROLLER.caputWait(getChannel(MinX), minx, timeout);
		} catch (Exception ex) {
			logger.warn("Cannot setMinX", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getMinX_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(MinX_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getMinX_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getMinY() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(MinY));
		} catch (Exception ex) {
			logger.warn("Cannot getMinY", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setMinY(int miny) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(MinY), miny);
		} catch (Exception ex) {
			logger.warn("Cannot setMinY", ex);
			throw ex;
		}
	}
	@Override
	public void setMinYWait(int miny, double timeout) throws Exception {
		try {
			EPICS_CONTROLLER.caputWait(getChannel(MinY), miny,timeout);
		} catch (Exception ex) {
			logger.warn("Cannot setMinY", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getMinY_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(MinY_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getMinY_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeX() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeX));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeX", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeX(int sizex) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(SizeX), sizex);
		} catch (Exception ex) {
			logger.warn("Cannot setSizeX", ex);
			throw ex;
		}
	}
	/**
	*
	*/
	@Override
	public void setSizeXWait(int sizex, double timeout) throws Exception {
		try {
			EPICS_CONTROLLER.caputWait(getChannel(SizeX), sizex, timeout);
		} catch (Exception ex) {
			logger.warn("Cannot setSizeX", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeX_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeX_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeX_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeY() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeY));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeY", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeY(int sizey) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(SizeY), sizey);
		} catch (Exception ex) {
			logger.warn("Cannot setSizeY", ex);
			throw ex;
		}
	}
	/**
	*
	*/
	@Override
	public void setSizeYWait(int sizey, double timeout) throws Exception {
		try {
			EPICS_CONTROLLER.caputWait(getChannel(SizeY), sizey, timeout);
		} catch (Exception ex) {
			logger.warn("Cannot setSizeY", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeY_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeY_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeY_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getReverseX() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ReverseX));
		} catch (Exception ex) {
			logger.warn("Cannot getReverseX", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setReverseX(int reversex) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(ReverseX), reversex);
		} catch (Exception ex) {
			logger.warn("Cannot setReverseX", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getReverseX_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ReverseX_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getReverseX_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getReverseY() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ReverseY));
		} catch (Exception ex) {
			logger.warn("Cannot getReverseY", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setReverseY(int reversey) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(ReverseY), reversey);
		} catch (Exception ex) {
			logger.warn("Cannot setReverseY", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getReverseY_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ReverseY_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getReverseY_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getArraySizeX_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(ArraySizeX_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getArraySizeX_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getArraySizeY_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(ArraySizeY_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getArraySizeY_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getArraySizeZ_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(ArraySizeZ_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getArraySizeZ_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getArraySize_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(ArraySize_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getArraySize_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getAcquireTime() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(AcquireTime));
		} catch (Exception ex) {
			logger.warn("Cannot getAcquireTime", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setAcquireTime(double acquiretime) throws Exception {
		logger.debug("Setting Acquire time to {} (for '{}')", acquiretime, Acquire);

		try {
			EPICS_CONTROLLER.caputWait(getChannel(AcquireTime), acquiretime);
		} catch (Exception ex) {
			logger.warn("Cannot setAcquireTime", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getAcquireTime_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(AcquireTime_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getAcquireTime_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getAcquirePeriod() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(AcquirePeriod));
		} catch (Exception ex) {
			logger.warn("Cannot getAcquirePeriod", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setAcquirePeriod(double acquireperiod) throws Exception {
		logger.debug("Setting Acquire period to {} (for '{}')", acquireperiod, Acquire);
		try {
			EPICS_CONTROLLER.caputWait(getChannel(AcquirePeriod), acquireperiod);
		} catch (Exception ex) {
			logger.warn("Cannot setAcquirePeriod", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getAcquirePeriod_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(AcquirePeriod_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getAcquirePeriod_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getTimeRemaining_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(TimeRemaining_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getTimeRemaining_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getGain() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(Gain));
		} catch (Exception ex) {
			logger.warn("Cannot getGain", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setGain(double gain) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(Gain), gain);
		} catch (Exception ex) {
			logger.warn("Cannot setGain", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getGain_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(Gain_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getGain_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getFrameType() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(FrameType));
		} catch (Exception ex) {
			logger.warn("Cannot getFrameType", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setFrameType(int frametype) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(FrameType), frametype);
		} catch (Exception ex) {
			logger.warn("Cannot setFrameType", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getFrameType_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(FrameType_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getFrameType_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getImageMode() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ImageMode));
		} catch (Exception ex) {
			logger.warn("Cannot getImageMode", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setImageMode(int imagemode) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(ImageMode), imagemode);
		} catch (Exception ex) {
			logger.warn("Cannot setImageMode", ex);
			throw ex;
		}
	}

	@Override
	public void setImageModeWait(ImageMode imagemode) throws Exception {
		try {
			EPICS_CONTROLLER.caputWait(getChannel(ImageMode), imagemode.ordinal());
		} catch (Exception ex) {
			logger.warn("Cannot setImageMode", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getImageMode_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ImageMode_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getImageMode_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getTriggerMode() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(TriggerMode));
		} catch (Exception ex) {
			logger.warn("Cannot getTriggerMode", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setTriggerMode(int triggermode) throws Exception {
		try {
			EPICS_CONTROLLER.caputWait(getChannel(TriggerMode), triggermode);
		} catch (Exception ex) {
			logger.warn("Cannot setTriggerMode", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getTriggerMode_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(TriggerMode_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getTriggerMode_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getNumExposures() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(NumExposures));
		} catch (Exception ex) {
			logger.warn("Cannot getNumExposures", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setNumExposures(int numexposures) throws Exception {
		try {
			EPICS_CONTROLLER.caputWait(getChannel(NumExposures), numexposures);
		} catch (Exception ex) {
			logger.warn("Cannot setNumExposures", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setNumExposures(int numexposures, double timeout) throws Exception {
		try {
			EPICS_CONTROLLER.caputWait(getChannel(NumExposures), numexposures, timeout);
		} catch (Exception ex) {
			logger.warn("Cannot setNumExposures", ex);
			throw ex;
		}
	}
	/**
	*
	*/
	@Override
	public int getNumExposures_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(NumExposures_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getNumExposures_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getNumExposuresCounter_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(NumExposuresCounter_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getNumExposuresCounter_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getNumImages() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(NumImages));
		} catch (Exception ex) {
			logger.warn("Cannot getNumImages", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setNumImages(int numimages) throws Exception {
		try {
			EPICS_CONTROLLER.caputWait(getChannel(NumImages), numimages);
		} catch (Exception ex) {
			logger.warn("Cannot setNumImages", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getNumImages_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(NumImages_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getNumImages_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getNumImagesCounter_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(NumImagesCounter_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getNumImagesCounter_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getAcquireState() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(Acquire));
		} catch (Exception ex) {
			logger.warn("Cannot getAcquireState", ex);
			throw ex;
		}
	}

	@Override
	public void startAcquiring() throws Exception {
		logger.debug("Acquisition started: {} called.", Acquire);
		try {
			if (getAcquireState() == 1) {
				return; // if continuing acquiring already started,do not put another call again as EPICS will not
						// callback at all in this case.
			}
			setStatus(Detector.BUSY);
			EPICS_CONTROLLER.caput(getChannel(Acquire), 1, startputlistener);
		} catch (Exception e) {
			setStatus(Detector.IDLE);
			logger.error("Exception on start Acquirig", e);
			throw e;
		}
	}
	@Override
	public void startAcquiringWait() throws Exception {
		startAcquiring();
		while (getStatus() != Detector.IDLE && getStatus() != Detector.FAULT) {
			Thread.sleep(100);
		}
		if (getStatus() == Detector.FAULT) {
			logger.debug("detector in a fault state");
		}
	}

	@Override
	public void startAcquiringSynchronously() throws Exception {
		int countBefore = getArrayCounter_RBV();
		startAcquiring();
		while (getStatus() != Detector.IDLE && getStatus() != Detector.FAULT) {
			Thread.sleep(100);
			if( getAcquireState()==0)
				throw new Exception("Camera is not acquiring but putListener has not been called");
		}
		if (getStatus() == Detector.FAULT) {
			logger.debug("detector in a fault state");
		}
		int countAfter = getArrayCounter_RBV();
		if( countAfter==countBefore)
			throw new Exception("Acquire completed but counter did not increment");
	}

	@Override
	public void stopAcquiring() throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(Acquire), 0);
		} catch (Exception e) {
			logger.error("Exception on stop Acquiring", e);
			throw e;
		} finally {
			// If the acquisition state is busy then wait for it to complete.
			while (getAcquireState() == 1) {
				Thread.sleep(25);
			}
			setStatus(Detector.IDLE);
			logger.info("Stopping detector acquisition");
		}
	}

	/**
	*
	*/
	@Override
	public String getAcquire_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.caget(getChannel(Acquire_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getAcquire_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getArrayCounter() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(ArrayCounter));
		} catch (Exception ex) {
			logger.warn("Cannot getArrayCounter", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setArrayCounter(int arraycounter) throws Exception {
		try {
			EPICS_CONTROLLER.caputWait(getChannel(ArrayCounter), arraycounter);
		} catch (Exception ex) {
			logger.warn("Cannot setArrayCounter", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getArrayCounter_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(ArrayCounter_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getArrayCounter_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getArrayRate_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(ArrayRate_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getArrayRate_RBV", ex);
			throw ex;
		}
	}

	/**
	 * @throws InterruptedException
	 * @throws CAException
	 * @throws TimeoutException
	 */
	@Override
	public short getDetectorState_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(DetectorState_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getDetectorState_RBV", ex);
			throw ex;
		}
	}

	@Override
	public short getDetectorStateLastMonitoredValue() throws Exception {
		if (detectorStatePV == null) {
			final String fullPVName = genenerateFullPvName(DetectorState_RBV);
			detectorStatePV = LazyPVFactory.newReadOnlyShortPV(fullPVName);
			detectorStatePV.setValueMonitoring(true);
		}
		return detectorStatePV.getLast();
	}

	/**
	*
	*/
	@Override
	public short getArrayCallbacks() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ArrayCallbacks));
		} catch (Exception ex) {
			logger.warn("Cannot getArrayCallbacks", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setArrayCallbacks(int arraycallbacks) throws Exception {
		try {
			EPICS_CONTROLLER.caputWait(getChannel(ArrayCallbacks), arraycallbacks);
		} catch (Exception ex) {
			logger.warn("Cannot setArrayCallbacks", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getArrayCallbacks_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ArrayCallbacks_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getArrayCallbacks_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getNDAttributesFile() throws Exception {
		try {
			return new String(EPICS_CONTROLLER.cagetByteArray(getChannel(NDAttributesFile))).trim();
		} catch (Exception ex) {
			logger.warn("Cannot getNDAttributesFile", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setNDAttributesFile(String ndattributesfile) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(NDAttributesFile), (ndattributesfile + '\0').getBytes());
		} catch (Exception ex) {
			logger.warn("Cannot setNDAttributesFile", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getStatusMessage_RBV() throws Exception {
		try {
			return new String(EPICS_CONTROLLER.cagetByteArray(getChannel(StatusMessage_RBV))).trim();
		} catch (Exception ex) {
			logger.warn("Cannot getStatusMessage_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getStringToServer_RBV() throws Exception {
		try {
			return new String(EPICS_CONTROLLER.cagetByteArray(getChannel(StringToServer_RBV))).trim();
		} catch (Exception ex) {
			logger.warn("Cannot getStringToServer_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getStringFromServer_RBV() throws Exception {
		try {
			return new String(EPICS_CONTROLLER.cagetByteArray(getChannel(StringFromServer_RBV))).trim();
		} catch (Exception ex) {
			logger.warn("Cannot getStringFromServer_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getReadStatus() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ReadStatus));
		} catch (Exception ex) {
			logger.warn("Cannot getReadStatus", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setReadStatus(int readstatus) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(ReadStatus), readstatus);
		} catch (Exception ex) {
			logger.warn("Cannot setReadStatus", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getShutterMode() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ShutterMode));
		} catch (Exception ex) {
			logger.warn("Cannot getShutterMode", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setShutterMode(int shuttermode) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(ShutterMode), shuttermode);
		} catch (Exception ex) {
			logger.warn("Cannot setShutterMode", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getShutterMode_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ShutterMode_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getShutterMode_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getShutterControl() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ShutterControl));
		} catch (Exception ex) {
			logger.warn("Cannot getShutterControl", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setShutterControl(int shuttercontrol) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(ShutterControl), shuttercontrol);
		} catch (Exception ex) {
			logger.warn("Cannot setShutterControl", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getShutterControl_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ShutterControl_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getShutterControl_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getShutterStatus_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ShutterStatus_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getShutterStatus_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getShutterOpenDelay() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(ShutterOpenDelay));
		} catch (Exception ex) {
			logger.warn("Cannot getShutterOpenDelay", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setShutterOpenDelay(double shutteropendelay) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(ShutterOpenDelay), shutteropendelay);
		} catch (Exception ex) {
			logger.warn("Cannot setShutterOpenDelay", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getShutterOpenDelay_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(ShutterOpenDelay_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getShutterOpenDelay_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getShutterCloseDelay() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(ShutterCloseDelay));
		} catch (Exception ex) {
			logger.warn("Cannot getShutterCloseDelay", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setShutterCloseDelay(double shutterclosedelay) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(ShutterCloseDelay), shutterclosedelay);
		} catch (Exception ex) {
			logger.warn("Cannot setShutterCloseDelay", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getShutterCloseDelay_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(ShutterCloseDelay_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getShutterCloseDelay_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getShutterOpenEPICSPV() throws Exception {
		try {
			return EPICS_CONTROLLER.caget(getChannel(ShutterOpenEPICSPV_ELEMENTNAME, ShutterOpenEPICSPV_PVPOSTFIX));
		} catch (Exception ex) {
			logger.warn("Cannot getShutterOpenEPICSPV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setShutterOpenEPICSPV(String shutteropenepicspv) throws Exception {
		logger.warn("Problem with PV Name - ShutterOpenEPICSPV -> ShutterOpenEPICS.OUT");
		try {
			EPICS_CONTROLLER.caput(getChannel(ShutterOpenEPICSPV_ELEMENTNAME, ShutterOpenEPICSPV_PVPOSTFIX),
					shutteropenepicspv);
		} catch (Exception ex) {
			logger.warn("Cannot setShutterOpenEPICSPV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getShutterOpenEPICSCmd() throws Exception {
		try {
			return EPICS_CONTROLLER.caget(getChannel(ShutterOpenEPICSCmd_ElEMENTNAME, ShutterOpenEPICSCmd_PVPOSTFIX));
		} catch (Exception ex) {
			logger.warn("Cannot getShutterOpenEPICSCmd", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setShutterOpenEPICSCmd(String shutteropenepicscmd) throws Exception {
		logger.warn("Problem with PV Name - ShutterOpenEPICSCmd -> ShutterOpenEPICSCmd.OCAL");
		try {
			EPICS_CONTROLLER.caput(getChannel(ShutterOpenEPICSCmd_ElEMENTNAME, ShutterOpenEPICSCmd_PVPOSTFIX),
					shutteropenepicscmd);
		} catch (Exception ex) {
			logger.warn("Cannot setShutterOpenEPICSCmd", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getShutterCloseEPICSPV() throws Exception {
		try {
			return EPICS_CONTROLLER.caget(getChannel(ShutterCloseEPICSPV_ELEMENTNAME, ShutterCloseEPICSPV_PVPOSTFIX));
		} catch (Exception ex) {
			logger.warn("Cannot getShutterCloseEPICSPV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setShutterCloseEPICSPV(String shuttercloseepicspv) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(ShutterCloseEPICSPV_ELEMENTNAME, ShutterCloseEPICSPV_PVPOSTFIX),
					shuttercloseepicspv);
		} catch (Exception ex) {
			logger.warn("Cannot setShutterCloseEPICSPV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getShutterCloseEPICSCmd() throws Exception {
		try {
			return EPICS_CONTROLLER.caget(getChannel(ShutterCloseEPICSCmd_ELEMENTNAME, ShutterCloseEPICSCmd_PVPOSTFIX));
		} catch (Exception ex) {
			logger.warn("Cannot getShutterCloseEPICSCmd", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setShutterCloseEPICSCmd(String shuttercloseepicscmd) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(ShutterCloseEPICSCmd_ELEMENTNAME, ShutterCloseEPICSCmd_PVPOSTFIX),
					shuttercloseepicscmd);
		} catch (Exception ex) {
			logger.warn("Cannot setShutterCloseEPICSCmd", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getShutterStatusEPICS_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ShutterStatusEPICS_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getShutterStatusEPICS_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getShutterStatusEPICSPV() throws Exception {
		try {
			return EPICS_CONTROLLER.caget(getChannel(ShutterStatusEPICSPV_ELEMENTNAME, ShutterStatusEPICSPV_PVPOSTFIX));
		} catch (Exception ex) {
			logger.warn("Cannot getShutterStatusEPICSPV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getShutterStatusEPICSCloseVal() throws Exception {
		try {
			return EPICS_CONTROLLER.caget(getChannel(ShutterStatusEPICSCloseVal_ELEMENTNAME,
					ShutterStatusEPICSCloseVal_PVPOSTFIX));
		} catch (Exception ex) {
			logger.warn("Cannot getShutterStatusEPICSCloseVal", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getShutterStatusEPICSOpenVal() throws Exception {
		try {
			return EPICS_CONTROLLER.caget(getChannel(ShutterStatusEPICSOpenVal_ELEMENTNAME,
					ShutterStatusEPICSOpenVal_PVPOSTFIX));
		} catch (Exception ex) {
			logger.warn("Cannot getShutterStatusEPICSOpenVal", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getTemperature() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(Temperature));
		} catch (Exception ex) {
			logger.warn("Cannot getTemperature", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setTemperature(double temperature) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(Temperature), temperature);
		} catch (Exception ex) {
			logger.warn("Cannot setTemperature", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getTemperature_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(Temperature_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getTemperature_RBV", ex);
			throw ex;
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (basePVName == null) {
			throw new IllegalArgumentException("'basePVName' needs to be declared");
		}

		pvArrayCounter_RBV = LazyPVFactory.newIntegerPV(genenerateFullPvName(ArrayCounter_RBV));
		pvDetectorState_RBV = LazyPVFactory.newIntegerPV(genenerateFullPvName(DetectorState_RBV));
	}

	/**
	 * @return Returns the basePVName.
	 */
	public String getBasePVName() {
		return basePVName;
	}

	/**
	 * @param basePVName
	 *            The basePVName to set.
	 */
	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	/**
	 * This method allows to toggle between the method in which the PV is acquired.
	 *
	 * @param pvElementName
	 * @param args
	 * @return {@link Channel} to talk to the relevant PV.
	 * @throws Exception
	 */
	protected Channel getChannel(String pvElementName, String... args) throws Exception {
		try {
			String fullPvName = genenerateFullPvName(pvElementName, args);
			return createChannel(fullPvName);
		} catch (Exception exception) {
			logger.warn("Problem getting channel", exception);
			throw exception;
		}
	}

	protected String genenerateFullPvName(String pvElementName, String... args) {
		String pvPostFix = null;
		if (args.length > 0) {
			// PV element name is different from the pvPostFix
			pvPostFix = args[0];
		} else {
			pvPostFix = pvElementName;
		}

		return basePVName + pvPostFix;
	}

	public Channel createChannel(String fullPvName) throws CAException, TimeoutException {
		Channel channel = channelMap.get(fullPvName);
		if (channel == null) {
			try {
				channel = EPICS_CONTROLLER.createChannel(fullPvName);
			} catch (CAException cae) {
				logger.warn("Problem creating channel", cae);
				throw cae;
			} catch (TimeoutException te) {
				logger.warn("Problem creating channel", te);
				throw te;

			}
			channelMap.put(fullPvName, channel);
		}
		return channel;
	}

	@Override
	public int getInitialMinX() {
		return initialMinX;
	}

	@Override
	public int getInitialMinY() {
		return initialMinY;
	}

	@Override
	public int getInitialSizeX() {
		return initialSizeX;
	}

	@Override
	public int getInitialSizeY() {
		return initialSizeY;
	}

	/**
	 * @param initialMinX
	 *            The initialMinX to set.
	 */
	public void setInitialMinX(int initialMinX) {
		this.initialMinX = initialMinX;
	}

	/**
	 * @param initialMinY
	 *            The initialMinY to set.
	 */
	public void setInitialMinY(int initialMinY) {
		this.initialMinY = initialMinY;
	}

	/**
	 * @param initialSizeX
	 *            The initialSizeX to set.
	 */
	public void setInitialSizeX(int initialSizeX) {
		this.initialSizeX = initialSizeX;
	}

	/**
	 * @param initialSizeY
	 *            The initialSizeY to set.
	 */
	public void setInitialSizeY(int initialSizeY) {
		this.initialSizeY = initialSizeY;
	}

	@Override
	public AreaDetectorROI getAreaDetectorROI() throws Exception {
		return new AreaDetectorROIImpl(getMinX(), getMinY(), getSizeX(), getSizeY());
	}

	@Override
	public AreaDetectorBin getBinning() throws Exception {
		return new AreaDetectorBinImpl(getBinX_RBV(), getBinY_RBV());
	}

	@Override
	public String getInitialDataType() {
		return initialDataType;
	}

	/**
	 * @param initialDataType
	 *            The initialDataType to set.
	 */
	public void setInitialDataType(String initialDataType) {
		this.initialDataType = initialDataType;
	}

	public String getInitialNDAttributesFile() {
		return initialNDAttributesFile;
	}

	/**
	 * @param initialNDAttributesFile
	 *            The initialNDAttributesFile to set.
	 */
	public void setInitialNDAttributesFile(String initialNDAttributesFile) {
		this.initialNDAttributesFile = initialNDAttributesFile;
	}

	@Override
	public void reset() throws Exception {
		if (initialDataType != null)
			setDataType(initialDataType);
		if ((initialMinX != null) && (initialMinY != null) && (initialSizeX != null) && (initialSizeY != null)) {
			setAreaDetectorROI(initialMinX, initialMinY, initialSizeX, initialSizeY);
		}
		if ((initialBinX != null) && (initialBinY != null)) {
			setBinning(initialBinX, initialBinY);
		}
		if (initialNDAttributesFile != null) {
			setNDAttributesFile(initialNDAttributesFile);
		}
		setStatus(Detector.IDLE);
	}

	/**
	 * @param binX
	 * @param binY
	 */
	private void setBinning(Integer binX, Integer binY) throws Exception {
		setBinX(binX);
		setBinY(binY);
	}

	/**
	 * @param minX
	 * @param minY
	 * @param sizeX
	 * @param sizeY
	 */
	private void setAreaDetectorROI(Integer minX, Integer minY, Integer sizeX, Integer sizeY) throws Exception {
		setMinX(minX);
		setMinY(minY);
		setSizeX(sizeX);
		setSizeY(sizeY);
	}

	/**
	 * @param initialBinX
	 *            The initialBinX to set.
	 */
	public void setInitialBinX(Integer initialBinX) {
		this.initialBinX = initialBinX;
	}

	/**
	 * @param initialBinY
	 *            The initialBinY to set.
	 */
	public void setInitialBinY(Integer initialBinY) {
		this.initialBinY = initialBinY;
	}

	@Override
	public void setStatus(int status) {
		synchronized (statusMonitor) {
			this.status = status;
			this.statusMonitor.notifyAll();
		}
	}

	@Override
	public int waitWhileStatusBusy() throws InterruptedException {
		synchronized (statusMonitor) {
			try{
				while (status == Detector.BUSY) {
					statusMonitor.wait(1000);
				}
			}  finally{
				//if interrupted clear the status state as the IOC may have crashed
				if ( status != 0)
					setStatus(0);
			}
			return status;
		}
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public void getEPICSStatus() throws Exception {
		this.status = getAcquireState();
	}

	class GreaterThanOrEqualTo implements Predicate<Integer> {

		private final int value;

		public GreaterThanOrEqualTo(int value) {
			this.value = value;
		}

		@Override
		public boolean apply(Integer object) {
			return (object >= value);
		}

	}

	@Override
	public void waitForArrayCounterToReach(final int exposureNumber, double timeoutS) throws InterruptedException,
			Exception, java.util.concurrent.TimeoutException {

		pvArrayCounter_RBV.setValueMonitoring(true);
		pvArrayCounter_RBV.waitForValue(new Predicate<Integer>() {
			@Override
			public boolean apply(Integer object) {
				return (object >= exposureNumber);
			}
		}, timeoutS);
	}

	@Override
	public void waitForDetectorStateIDLE(double timeoutS) throws InterruptedException,
			Exception, java.util.concurrent.TimeoutException {

		pvDetectorState_RBV.setValueMonitoring(true);
		pvDetectorState_RBV.waitForValue(new Predicate<Integer>() {
			@Override
			public boolean apply(Integer object) {
				return (object == 0);
			}
		}, timeoutS);
	}

	private String getChannelName(String pvElementName, String... args) {
		return genenerateFullPvName(pvElementName, args);
	}

	@Override
	public Observable<Short> createAcquireStateObservable() throws Exception {
		return LazyPVFactory.newReadOnlyEnumPV(getChannelName(Acquire_RBV), Short.class);
	}

	@Override
	public Observable<Double> createAcquireTimeObservable() throws Exception {
		return LazyPVFactory.newReadOnlyDoublePV(getChannelName(AcquireTime_RBV));
	}

	@Override
	public void setImageMode(ImageMode imagemode) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(ImageMode), imagemode.ordinal());
		} catch (Exception ex) {
			logger.warn("Cannot setImageMode", ex);
			throw ex;
		}

	}
	@Override
	public void setImageModeWait(ImageMode imagemode, double timeout) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(ImageMode), imagemode.ordinal(), timeout);
		} catch (Exception ex) {
			logger.warn("Cannot setImageMode", ex);
			throw ex;
		}

	}

	// General purpose getters and setters for arbitrary PV suffixes. Primarily for prototyping.

	@Override
	public int getIntBySuffix(String suffix) throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(suffix));
	}

	@Override
	public void setIntBySuffix(String suffix, int value) throws Exception {
		Channel channel = getChannel(suffix);
		logger.info("Setting channel {} to int value {}", channel.getName(), value);
		EPICS_CONTROLLER.caput(channel, value);
	}

	@Override
	public double getDoubleBySuffix(String suffix) throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(suffix));
	}

	@Override
	public void setDoubleBySuffix(String suffix, double value) throws Exception {
		Channel channel = getChannel(suffix);
		logger.info("Setting channel {} to double value {}", channel.getName(), value);
		EPICS_CONTROLLER.caput(channel, value);
	}

	@Override
	public String getStringBySuffix(String suffix) throws Exception {
		return EPICS_CONTROLLER.caget(getChannel(suffix));
	}

	@Override
	public void setStringBySuffix(String suffix, String value) throws Exception {
		Channel channel = getChannel(suffix);
		logger.info("Setting channel {} to string value {}", channel.getName(), value);
		EPICS_CONTROLLER.caput(channel, value);
	}
}
