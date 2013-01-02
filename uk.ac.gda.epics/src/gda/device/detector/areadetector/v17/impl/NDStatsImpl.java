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
import gda.device.detector.areadetector.IPVProvider;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.areadetector.v17.NDStats;
import gda.epics.connection.EpicsController;
import gda.epics.interfaces.NDStatsType;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class NDStatsImpl implements InitializingBean, NDStats {

	private final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	private String basePVName;

	private IPVProvider pvProvider;

	private NDStatsType config;
	
	private String deviceName;

	private NDPluginBase pluginBase;

	static final Logger logger = LoggerFactory.getLogger(NDStatsImpl.class);

	@Override
	public NDPluginBase getPluginBase() {
		return pluginBase;
	}

	public void setPluginBase(NDPluginBase pluginBase) {
		this.pluginBase = pluginBase;
	}

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
				config = Configurator.getConfiguration(getDeviceName(), NDStatsType.class);
			} catch (ConfigurationNotFoundException e) {
				logger.error("EPICS configuration for device {} not found", getDeviceName());
				throw new FactoryException("EPICS configuration for device " + getDeviceName() + " not found.", e);
			}
		}
	}

	/**
	 * Map that stores the channel against the PV name
	 */
	private Map<String, Channel> channelMap = new HashMap<String, Channel>();

	/**
	*
	*/
	@Override
	public short getComputeStatistics() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getComputeStatistics().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(ComputeStatistics));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getComputeStatistics", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setComputeStatistics(int computestatistics) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getComputeStatistics().getPv()), computestatistics);
			} else {
				EPICS_CONTROLLER.caput(getChannel(ComputeStatistics), computestatistics);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot setComputeStatistics", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getComputeStatistics_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getComputeStatistics_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(ComputeStatistics_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getComputeStatistics_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBgdWidth() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBgdWidth().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(BgdWidth));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getBgdWidth", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setBgdWidth(int bgdwidth) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getBgdWidth().getPv()), bgdwidth);
			} else {
				EPICS_CONTROLLER.caput(getChannel(BgdWidth), bgdwidth);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot setBgdWidth", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBgdWidth_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBgdWidth_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(BgdWidth_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getBgdWidth_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getMinValue_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getMinValue_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(MinValue_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getMinValue_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getMaxValue_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getMaxValue_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(MaxValue_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getMaxValue_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getMeanValue_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getMeanValue_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(MeanValue_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getMeanValue_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getSigma_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getSigma_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(Sigma_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getSigma_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getTotal_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getTotal_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(Total_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getTotal_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getNet_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getNet_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(Net_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getNet_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getComputeCentroid() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getComputeCentroid().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(ComputeCentroid));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getComputeCentroid", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setComputeCentroid(int computecentroid) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getComputeCentroid().getPv()), computecentroid);
			} else {
				EPICS_CONTROLLER.caput(getChannel(ComputeCentroid), computecentroid);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot setComputeCentroid", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getComputeCentroid_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getComputeCentroid_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(ComputeCentroid_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getComputeCentroid_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getCentroidThreshold() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getCentroidThreshold().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(CentroidThreshold));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getCentroidThreshold", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setCentroidThreshold(double centroidthreshold) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getCentroidThreshold().getPv()), centroidthreshold);
			} else {
				EPICS_CONTROLLER.caput(getChannel(CentroidThreshold), centroidthreshold);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot setCentroidThreshold", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getCentroidThreshold_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getCentroidThreshold_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(CentroidThreshold_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getCentroidThreshold_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getCentroidX_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getCentroidX_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(CentroidX_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getCentroidX_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getCentroidY_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getCentroidY_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(CentroidY_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getCentroidY_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getSigmaX_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getSigmaX_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(SigmaX_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getSigmaX_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getSigmaY_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getSigmaY_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(SigmaY_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getSigmaY_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getSigmaXY_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getSigmaXY_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(SigmaXY_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getSigmaXY_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getComputeProfiles() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getComputeProfiles().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(ComputeProfiles));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getComputeProfiles", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setComputeProfiles(int computeprofiles) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getComputeProfiles().getPv()), computeprofiles);
			} else {
				EPICS_CONTROLLER.caput(getChannel(ComputeProfiles), computeprofiles);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot setComputeProfiles", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getComputeProfiles_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getComputeProfiles_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(ComputeProfiles_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getComputeProfiles_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getProfileSizeX_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getProfileSizeX_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(ProfileSizeX_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getProfileSizeX_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getProfileSizeY_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getProfileSizeY_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(ProfileSizeY_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getProfileSizeY_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getCursorX() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getCursorX().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(CursorX));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getCursorX", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setCursorX(int cursorx) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getCursorX().getPv()), cursorx);
			} else {
				EPICS_CONTROLLER.caput(getChannel(CursorX), cursorx);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot setCursorX", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getCursorX_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getCursorX_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(CursorX_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getCursorX_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getCursorY() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getCursorY().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(CursorY));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getCursorY", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setCursorY(int cursory) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getCursorY().getPv()), cursory);
			} else {
				EPICS_CONTROLLER.caput(getChannel(CursorY), cursory);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot setCursorY", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getCursorY_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getCursorY_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(CursorY_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getCursorY_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double[] getProfileAverageX_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDoubleArray(createChannel(config.getProfileAverageX_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDoubleArray(getChannel(ProfileAverageX_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getProfileAverageX_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double[] getProfileAverageY_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDoubleArray(createChannel(config.getProfileAverageY_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDoubleArray(getChannel(ProfileAverageY_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getProfileAverageY_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double[] getProfileThresholdX_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDoubleArray(createChannel(config.getProfileThresholdX_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDoubleArray(getChannel(ProfileThresholdX_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getProfileThresholdX_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double[] getProfileThresholdY_RBV() throws Exception {
		try {

			if (config != null) {
				return EPICS_CONTROLLER.cagetDoubleArray(createChannel(config.getProfileThresholdY_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDoubleArray(getChannel(ProfileThresholdY_RBV));

		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getProfileThresholdY_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double[] getProfileCentroidX_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDoubleArray(createChannel(config.getProfileCentroidX_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDoubleArray(getChannel(ProfileCentroidX_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getProfileCentroidX_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double[] getProfileCentroidY_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDoubleArray(createChannel(config.getProfileCentroidY_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDoubleArray(getChannel(ProfileCentroidY_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getProfileCentroidY_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double[] getProfileCursorX_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDoubleArray(createChannel(config.getProfileCursorX_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDoubleArray(getChannel(ProfileCursorX_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getProfileCursorX_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double[] getProfileCursorY_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDoubleArray(createChannel(config.getProfileCursorY_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDoubleArray(getChannel(ProfileCursorY_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getProfileCursorY_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getComputeHistogram() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getComputeHistogram().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(ComputeHistogram));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getComputeHistogram", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setComputeHistogram(int computehistogram) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getComputeHistogram().getPv()), computehistogram);
			} else {
				EPICS_CONTROLLER.caput(getChannel(ComputeHistogram), computehistogram);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot setComputeHistogram", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getComputeHistogram_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getComputeHistogram_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(ComputeHistogram_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getComputeHistogram_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getHistSize() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getHistSize().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(HistSize));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getHistSize", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setHistSize(int histsize) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getHistSize().getPv()), histsize);
			} else {
				EPICS_CONTROLLER.caput(getChannel(HistSize), histsize);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot setHistSize", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getHistSize_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getHistSize_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(HistSize_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getHistSize_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getHistMin() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getHistMin().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(HistMin));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getHistMin", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setHistMin(double histmin) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getHistMin().getPv()), histmin);
			} else {
				EPICS_CONTROLLER.caput(getChannel(HistMin), histmin);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot setHistMin", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getHistMin_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getHistMin_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(HistMin_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getHistMin_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getHistMax() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getHistMax().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(HistMax));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getHistMax", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setHistMax(double histmax) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getHistMax().getPv()), histmax);
			} else {
				EPICS_CONTROLLER.caput(getChannel(HistMax), histmax);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot setHistMax", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getHistMax_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getHistMax_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(HistMax_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getHistMax_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getHistEntropy_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getHistEntropy_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(HistEntropy_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getHistEntropy_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double[] getHistogram_RBV() throws Exception {
		Channel channel = config != null ? createChannel(config.getHistogram_RBV().getPv()) : getChannel(Histogram_RBV);
		return EPICS_CONTROLLER.cagetDoubleArray(channel);
	}

	@Override
	public double[] getHistogram_RBV(int numberOfElements) throws Exception {
		Channel channel = config != null ? createChannel(config.getHistogram_RBV().getPv()) : getChannel(Histogram_RBV);
		return EPICS_CONTROLLER.cagetDoubleArray(channel, numberOfElements);
	}

	/**
	*
	*/
	@Override
	public int getMaxSizeX() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getMaxSizeX().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(MaxSizeX));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getMaxSizeX", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setMaxSizeX(int maxsizex) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getMaxSizeX().getPv()), maxsizex);
			} else {
				EPICS_CONTROLLER.caput(getChannel(MaxSizeX), maxsizex);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot setMaxSizeX", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getSetXHOPR() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getSetXHOPR().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(SetXHOPR));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getSetXHOPR", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSetXHOPR(double setxhopr) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSetXHOPR().getPv()), setxhopr);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SetXHOPR), setxhopr);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot setSetXHOPR", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getMaxSizeY() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getMaxSizeY().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(MaxSizeY));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getMaxSizeY", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setMaxSizeY(int maxsizey) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getMaxSizeY().getPv()), maxsizey);
			} else {
				EPICS_CONTROLLER.caput(getChannel(MaxSizeY), maxsizey);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot setMaxSizeY", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getSetYHOPR() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getSetYHOPR().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(SetYHOPR));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot getSetYHOPR", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSetYHOPR(double setyhopr) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSetYHOPR().getPv()), setyhopr);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SetYHOPR), setyhopr);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl-> Cannot setSetYHOPR", ex);
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
	}

	/**
	 * @param basePVName
	 *            The basePVName to set.
	 */
	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
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
			return createChannel(getChannelName(pvElementName, args));
		} catch (Exception exception) {
			logger.warn("g.d.d.a.v.i.NDStatsImpl -> Problem getting channel", exception);
			throw exception;
		}
	}

	public Channel createChannel(String fullPvName) throws CAException, TimeoutException {
		Channel channel = channelMap.get(fullPvName);
		if (channel == null) {
			try {
				channel = EPICS_CONTROLLER.createChannel(fullPvName);
			} catch (CAException cae) {
				logger.warn("g.d.d.a.v.i.NDStatsImpl-> Problem creating channel", cae);
				throw cae;
			} catch (TimeoutException te) {
				logger.warn("g.d.d.a.v.i.NDStatsImpl-> Problem creating channel", te);
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
	public void reset() throws Exception {
		pluginBase.reset();
	}
}