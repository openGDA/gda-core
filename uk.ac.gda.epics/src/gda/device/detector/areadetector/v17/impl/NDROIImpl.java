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

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.detector.areadetector.AreaDetectorROI;
import gda.device.detector.areadetector.IPVProvider;
import gda.device.detector.areadetector.impl.AreaDetectorROIImpl;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.areadetector.v17.NDROI;
import gda.epics.LazyPVFactory;
import gda.epics.connection.EpicsController;
import gda.epics.interfaces.NDROIType;
import gda.factory.FactoryException;
import gda.observable.Observable;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class NDROIImpl extends NDBaseImpl implements InitializingBean, NDROI {

	private final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	private String basePVName;

	private IPVProvider pvProvider;
	/**
	 * Map that stores the channel against the PV name
	 */
	private Map<String, Channel> channelMap = new HashMap<String, Channel>();
	private NDROIType config;
	private String deviceName;

	// Setup the logging facilities
	static final Logger logger = LoggerFactory.getLogger(NDROIImpl.class);

	private Integer initialDataType;

	private Integer initialMinX;

	private Integer initialMinY;

	private Integer initialSizeX;

	private Integer initialSizeY;

	private Integer initialBinX;

	private Integer initialBinY;

	private boolean initialEnableScale;

	private Integer initialScale;

	private boolean initialEnableX;

	private boolean initialEnableY;

	private boolean initialEnableZ;

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) throws FactoryException {
		this.deviceName = deviceName;
		initializeConfig();
	}

	private void initializeConfig() throws FactoryException {
		if (deviceName != null) {
			try {
				config = Configurator.getConfiguration(getDeviceName(), NDROIType.class);
			} catch (ConfigurationNotFoundException e) {
				logger.error("EPICS configuration for device {} not found", getDeviceName());
				throw new FactoryException("EPICS configuration for device " + getDeviceName() + " not found.", e);
			}
		}
	}

	/**
	*
	*/
	@Override
	public String getLabel() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.caget(createChannel(config.getLabel().getPv()));
			}
			return EPICS_CONTROLLER.caget(getChannel(Label));
		} catch (Exception ex) {
			logger.warn("Cannot getLabel", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setLabel(String label) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getLabel().getPv()), label);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Label), label);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setLabel", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getLabel_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.caget(createChannel(config.getLabel_RBV().getPv()));
			}
			return EPICS_CONTROLLER.caget(getChannel(Label_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getLabel_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBinX() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBinX().getPv()));
			}
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
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getBinX().getPv()), binx);
			} else {
				EPICS_CONTROLLER.caput(getChannel(BinX), binx);
			}
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
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBinX_RBV().getPv()));
			}
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
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBinY().getPv()));
			}
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
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getBinY().getPv()), biny);
			} else {
				EPICS_CONTROLLER.caput(getChannel(BinY), biny);
			}
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
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBinY_RBV().getPv()));
			}
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
	public int getBinZ() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBinZ().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(BinZ));
		} catch (Exception ex) {
			logger.warn("Cannot getBinZ", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setBinZ(int binz) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getBinZ().getPv()), binz);
			} else {
				EPICS_CONTROLLER.caput(getChannel(BinZ), binz);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setBinZ", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBinZ_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBinZ_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(BinZ_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getBinZ_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getMinX() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getMinX().getPv()));
			}
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
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getMinX().getPv()), minx);
			} else {
				EPICS_CONTROLLER.caput(getChannel(MinX), minx);
			}
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
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getMinX_RBV().getPv()));
			}
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
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getMinY().getPv()));
			}
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
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getMinY().getPv()), miny);
			} else {
				EPICS_CONTROLLER.caput(getChannel(MinY), miny);
			}
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
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getMinY_RBV().getPv()));
			}
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
	public int getMinZ() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getMinZ().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(MinZ));
		} catch (Exception ex) {
			logger.warn("Cannot getMinZ", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setMinZ(int minz) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getMinZ().getPv()), minz);
			} else {
				EPICS_CONTROLLER.caput(getChannel(MinZ), minz);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setMinZ", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getMinZ_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getMinZ_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(MinZ_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getMinZ_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeX() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeX().getPv()));
			}
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
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeX().getPv()), sizex);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeX), sizex);
			}
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
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeX_RBV().getPv()));
			}
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
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeY().getPv()));
			}
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
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeY().getPv()), sizey);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeY), sizey);
			}
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
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeY_RBV().getPv()));
			}
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
	public int getSizeZ() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeZ().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeZ));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeZ", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeZ(int sizez) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeZ().getPv()), sizez);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeZ), sizez);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeZ", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeZ_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeZ_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeZ_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeZ_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getMaxSizeX_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getMaxSizeX_RBV().getPv()));
			}
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
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getMaxSizeY_RBV().getPv()));
			}
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
	public int getMaxSizeZ_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getMaxSizeZ_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(MaxSizeZ_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getMaxSizeZ_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getReverseX() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getReverseX().getPv()));
			}
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
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getReverseX().getPv()), reversex);
			} else {
				EPICS_CONTROLLER.caput(getChannel(ReverseX), reversex);
			}
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
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getReverseX_RBV().getPv()));
			}
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
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getReverseY().getPv()));
			}
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
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getReverseY().getPv()), reversey);
			} else {
				EPICS_CONTROLLER.caput(getChannel(ReverseY), reversey);
			}
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
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getReverseY_RBV().getPv()));
			}
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
	public short getReverseZ() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getReverseZ().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(ReverseZ));
		} catch (Exception ex) {
			logger.warn("Cannot getReverseZ", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setReverseZ(int reversez) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getReverseZ().getPv()), reversez);
			} else {
				EPICS_CONTROLLER.caput(getChannel(ReverseZ), reversez);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setReverseZ", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getReverseZ_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getReverseZ_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(ReverseZ_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getReverseZ_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getArraySizeX_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getArraySizeX_RBV().getPv()));
			}
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
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getArraySizeY_RBV().getPv()));
			}
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
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getArraySizeZ_RBV().getPv()));
			}
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
	public boolean isScalingEnabled() throws Exception {
		int scalingEnabled = EPICS_CONTROLLER.cagetInt(getChannel(EnableScale));
		if (scalingEnabled == 1) {
			return true;
		}
		return false;
	}

	/**
	*
	*/
	@Override
	public void enableScaling() throws Exception {
		if (config != null) {
			EPICS_CONTROLLER.caput(createChannel(config.getEnableScale().getPv()), 1);
		} else {
			EPICS_CONTROLLER.caput(getChannel(EnableScale), 1);
		}
	}

	@Override
	public void disableScaling() throws Exception {
		if (config != null) {
			EPICS_CONTROLLER.caput(createChannel(config.getEnableScale().getPv()), 0);
		} else {
			EPICS_CONTROLLER.caput(getChannel(EnableScale), 0);
		}
	}

	/**
	*
	*/
	@Override
	public short isScalingEnabled_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getEnableScale_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableScale_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getEnableScale_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getScale() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getScale().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(Scale));
		} catch (Exception ex) {
			logger.warn("Cannot getScale", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setScale(double scale) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getScale().getPv()), scale);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Scale), scale);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setScale", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getScale_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getScale_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(Scale_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getScale_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getDataTypeOut() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getDataTypeOut().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(DataTypeOut));
		} catch (Exception ex) {
			logger.warn("Cannot getDataTypeOut", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setDataTypeOut(int datatypeout) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getDataTypeOut().getPv()), datatypeout);
			} else {
				EPICS_CONTROLLER.caput(getChannel(DataTypeOut), datatypeout);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setDataTypeOut", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getDataTypeOut_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getDataTypeOut_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(DataTypeOut_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getDataTypeOut_RBV", ex);
			throw ex;
		}
	}

	/**
	 * @return Returns the basePVName.
	 */
	public String getBasePVName() {
		return basePVName;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (deviceName == null && basePVName == null && pvProvider == null) {
			throw new IllegalArgumentException("'deviceName','basePVName' or 'pvProvider' needs to be declared");
		}
		/*
		 * Previously a check was made on the initialBinX/Y, initialMinX/Y, initialSizeX/Y However there is already a
		 * checks in the reset method where they are used. Checking at reset rather than server startup is better as the
		 * values to be used for reset may not be known are server restart.
		 */
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
	private Channel getChannel(String pvElementName, String... args) throws Exception {
		try {
			String pvPostFix = null;
			if (args.length > 0) {
				// PV element name is different from the pvPostFix
				pvPostFix = args[0];
			} else {
				pvPostFix = pvElementName;
			}

			String fullPvName;
			if (pvProvider != null) {
				fullPvName = pvProvider.getPV(pvElementName);
			} else {
				fullPvName = basePVName + pvPostFix;
			}
			return createChannel(fullPvName);
		} catch (Exception exception) {
			logger.warn("Problem getting channel", exception);
			throw exception;
		}
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

	/**
	 * @return Returns the pvProvider.
	 */
	public IPVProvider getPvProvider() {
		return pvProvider;
	}

	/**
	 * @param pvProvider
	 *            The pvProvider to set.
	 */
	public void setPvProvider(IPVProvider pvProvider) {
		this.pvProvider = pvProvider;
	}

	@Override
	public void setAreaDetectorROI(AreaDetectorROI areaDetectorROI) throws Exception {
		setMinX(areaDetectorROI.getMinX());
		setMinY(areaDetectorROI.getMinY());
		setSizeX(areaDetectorROI.getSizeX());
		setSizeY(areaDetectorROI.getSizeY());
	}

	@Override
	public AreaDetectorROI getAreaDetectorROI() throws Exception {
		return new AreaDetectorROIImpl(getMinX(), getMinY(), getSizeX(), getSizeY());
	}

	@Override
	public void reset() throws Exception {
		getPluginBase().reset();
		if (initialDataType != null)
			setDataTypeOut((short) initialDataType.intValue());
		if (initialEnableScale)
			enableScaling();
		if (initialScale != null) {
			setScale(initialScale);
		}
		if (initialDataType != null)
			setDataTypeOut((short) initialDataType.intValue());
		if ((initialMinX != null) && (initialMinY != null) && (initialSizeX != null) && (initialSizeY != null)) {
			setROI(initialMinX, initialMinY, initialSizeX, initialSizeY);
		}
		if ((initialBinX != null) && (initialBinY != null)) {
			setBinning(initialBinX, initialBinY);
		}

		if (initialEnableX) {
			enableX();
		} else {
			disableX();
		}

		if (initialEnableY) {
			enableY();
		} else {
			disableY();
		}

		if (initialEnableZ) {
			enableZ();
		} else {
			disableZ();
		}

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
	private void setROI(Integer minX, Integer minY, Integer sizeX, Integer sizeY) throws Exception {
		setMinX(minX);
		setMinY(minY);
		setSizeX(sizeX);
		setSizeY(sizeY);
	}

	public void setInitialEnableScale(boolean value) {
		this.initialEnableScale = value;
	}

	public void setInitialEnableX(boolean initialEnableX) {
		this.initialEnableX = initialEnableX;
	}

	public boolean isInitialEnableX() {
		return initialEnableX;
	}

	public void setInitialEnableY(boolean initialEnableY) {
		this.initialEnableY = initialEnableY;
	}

	public boolean isInitialEnableY() {
		return initialEnableY;
	}

	public void setInitialEnableZ(boolean initialEnableZ) {
		this.initialEnableZ = initialEnableZ;
	}

	public boolean isInitialEnableZ() {
		return initialEnableZ;
	}

	public boolean isInitialEnableScale() {
		return initialEnableScale;
	}

	public void setInitialScale(Integer value) {
		this.initialScale = value;
	}

	public Integer getInitialScale() {
		return initialScale;
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

	/**
	 * @param initialDataType
	 *            The initialDataType to set.
	 */
	public void setInitialDataType(Integer initialDataType) {
		this.initialDataType = initialDataType;
	}

	public int getInitialDataTypeOrd() {
		return initialDataType;
	}

	public void setInitialDataTypeOrd(int ordinal) {
		initialDataType = ordinal;
	}

	public void setInitialDatatype(NDPluginBase.DataType datatype) {
		this.initialDataType = datatype.ordinal();
	}

	/**
	 * @param initialMinX
	 *            The initialMinX to set.
	 */
	public void setInitialMinX(Integer initialMinX) {
		this.initialMinX = initialMinX;
	}

	/**
	 * @param initialMinY
	 *            The initialMinY to set.
	 */
	public void setInitialMinY(Integer initialMinY) {
		this.initialMinY = initialMinY;
	}

	/**
	 * @param initialSizeX
	 *            The initialSizeX to set.
	 */
	public void setInitialSizeX(Integer initialSizeX) {
		this.initialSizeX = initialSizeX;
	}

	/**
	 * @param initialSizeY
	 *            The initialSizeY to set.
	 */
	public void setInitialSizeY(Integer initialSizeY) {
		this.initialSizeY = initialSizeY;
	}

	@Override
	public boolean isEnableX() throws Exception {
		short shortVal = 0;
		try {
			if (config != null) {
				shortVal = EPICS_CONTROLLER.cagetShort(createChannel(config.getEnableX().getPv()));
			} else {
				shortVal = EPICS_CONTROLLER.cagetShort(getChannel(EnableX));
			}
		} catch (Exception ex) {
			throw ex;
		}
		return shortVal == 1 ? true : false;
	}

	@Override
	public boolean isEnableY() throws Exception {
		short shortVal = 0;
		try {
			if (config != null) {
				shortVal = EPICS_CONTROLLER.cagetShort(createChannel(config.getEnableY().getPv()));
			} else {
				shortVal = EPICS_CONTROLLER.cagetShort(getChannel(EnableY));
			}
		} catch (Exception ex) {
			throw ex;
		}
		return shortVal == 1 ? true : false;
	}

	@Override
	public boolean isEnableZ() throws Exception {
		short shortVal = 0;
		try {
			if (config != null) {
				shortVal = EPICS_CONTROLLER.cagetShort(createChannel(config.getEnableZ().getPv()));
			} else {
				shortVal = EPICS_CONTROLLER.cagetShort(getChannel(EnableZ));
			}
		} catch (Exception ex) {
			throw ex;
		}
		return shortVal == 1 ? true : false;
	}

	@Override
	public void enableX() throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getEnableX().getPv()), 1);
			} else {
				EPICS_CONTROLLER.caput(getChannel(EnableX), 1);
			}
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public void disableX() throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getEnableX().getPv()), 0);
			} else {
				EPICS_CONTROLLER.caput(getChannel(EnableX), 0);
			}
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public void enableY() throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getEnableY().getPv()), 1);
			} else {
				EPICS_CONTROLLER.caput(getChannel(EnableY), 1);
			}
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public void disableY() throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getEnableY().getPv()), 0);
			} else {
				EPICS_CONTROLLER.caput(getChannel(EnableY), 0);
			}
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public void enableZ() throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getEnableZ().getPv()), 1);
			} else {
				EPICS_CONTROLLER.caput(getChannel(EnableZ), 1);
			}
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public void disableZ() throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getEnableZ().getPv()), 0);
			} else {
				EPICS_CONTROLLER.caput(getChannel(EnableZ), 0);
			}
		} catch (Exception ex) {
			throw ex;
		}
	}


	private String getChannelName(String pvElementName, String... args)throws Exception{
		String pvPostFix = null;
		if (args.length > 0) {
			// PV element name is different from the pvPostFix
			pvPostFix = args[0];
		} else {
			pvPostFix = pvElementName;
		}

		String fullPvName;
		if (pvProvider != null) {
			fullPvName = pvProvider.getPV(pvElementName);
		} else {
			fullPvName = basePVName + pvPostFix;
		}
		return fullPvName;
	}


	@Override
	public Observable<Integer> createMinXObservable() throws Exception {
		return LazyPVFactory.newReadOnlyIntegerPV(getChannelName(MinX_RBV));
	}

	@Override
	public Observable<Integer> createMinYObservable() throws Exception {
		return LazyPVFactory.newReadOnlyIntegerPV(getChannelName(MinY_RBV));
	}
	@Override
	public Observable<Integer> createSizeXObservable() throws Exception {
		return LazyPVFactory.newReadOnlyIntegerPV(getChannelName(SizeX_RBV));
	}

	@Override
	public Observable<Integer> createSizeYObservable() throws Exception {
		return LazyPVFactory.newReadOnlyIntegerPV(getChannelName(SizeY_RBV));
	}

	@Override
	public Observable<String> createEnableXObservable() throws Exception {
		return LazyPVFactory.newReadOnlyEnumPV(getChannelName(EnableX), String.class);
	}

	@Override
	public Observable<String> createEnableYObservable() throws Exception {
		return LazyPVFactory.newReadOnlyEnumPV(getChannelName(EnableY), String.class);
	}


}
