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
import gda.device.detector.areadetector.v17.NDOverlay;
import gda.epics.connection.EpicsController;
import gda.epics.interfaces.NDOverlayType;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class NDOverlayImpl extends NDBaseImpl implements InitializingBean, NDOverlay {

	private static final String OVERLAY_7 = "8:";

	private static final String OVERLAY_6 = "7:";

	private static final String OVERLAY_5 = "6:";

	private static final String OVERLAY_4 = "5:";

	private static final String OVERLAY_3 = "4:";

	private static final String OVERLAY_2 = "3:";

	private static final String OVERLAY_1 = "2:";

	private static final String OVERLAY_0 = "1:";

	private final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	private String basePVName;

	private IPVProvider pvProvider;

	private NDOverlayType config;

	private String deviceName;

	static final Logger logger = LoggerFactory.getLogger(NDOverlayImpl.class);

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
				config = Configurator.getConfiguration(getDeviceName(), NDOverlayType.class);
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
	 * List all the PVs
	 */

	private static final String Name0 = OVERLAY_0 + "Name";

	private static final String Name_RBV0 = OVERLAY_0 + "Name_RBV";

	private static final String Use0 = OVERLAY_0 + "Use";

	private static final String Use0_RBV = OVERLAY_0 + "Use_RBV";

	private static final String PositionXLink0 = OVERLAY_0 + "PositionXLink";

	private static final String PositionX0 = OVERLAY_0 + "PositionX";

	private static final String PositionX0_RBV = OVERLAY_0 + "PositionX_RBV";

	private static final String PositionYLink0 = OVERLAY_0 + "PositionYLink";

	private static final String PositionY0 = OVERLAY_0 + "PositionY";

	private static final String PositionY0_RBV = OVERLAY_0 + "PositionY_RBV";

	private static final String SizeXLink0 = OVERLAY_0 + "SizeXLink";

	private static final String SizeX0 = OVERLAY_0 + "SizeX";

	private static final String SizeX0_RBV = OVERLAY_0 + "SizeX_RBV";

	private static final String SizeYLink0 = OVERLAY_0 + "SizeYLink";

	private static final String SizeY0 = OVERLAY_0 + "SizeY";

	private static final String SizeY0_RBV = OVERLAY_0 + "SizeY_RBV";

	private static final String Shape0 = OVERLAY_0 + "Shape";

	private static final String Shape0_RBV = OVERLAY_0 + "Shape_RBV";

	private static final String DrawMode0 = OVERLAY_0 + "DrawMode";

	private static final String DrawMode0_RBV = OVERLAY_0 + "DrawMode_RBV";

	private static final String Red0 = OVERLAY_0 + "Red";

	private static final String Red0_RBV = OVERLAY_0 + "Red_RBV";

	private static final String Green0 = OVERLAY_0 + "Green";

	private static final String Green0_RBV = OVERLAY_0 + "Green_RBV";

	private static final String Blue0 = OVERLAY_0 + "Blue";

	private static final String Blue0_RBV = OVERLAY_0 + "Blue_RBV";

	private static final String Name1 = OVERLAY_1 + "Name";

	private static final String Name_RBV1 = OVERLAY_1 + "Name_RBV";

	private static final String Use1 = OVERLAY_1 + "Use";

	private static final String Use1_RBV = OVERLAY_1 + "Use_RBV";

	private static final String PositionXLink1 = OVERLAY_1 + "PositionXLink";

	private static final String PositionX1 = OVERLAY_1 + "PositionX";

	private static final String PositionX1_RBV = OVERLAY_1 + "PositionX_RBV";

	private static final String PositionYLink1 = OVERLAY_1 + "PositionYLink";

	private static final String PositionY1 = OVERLAY_1 + "PositionY";

	private static final String PositionY1_RBV = OVERLAY_1 + "PositionY_RBV";

	private static final String SizeXLink1 = OVERLAY_1 + "SizeXLink";

	private static final String SizeX1 = OVERLAY_1 + "SizeX";

	private static final String SizeX1_RBV = OVERLAY_1 + "SizeX_RBV";

	private static final String SizeYLink1 = OVERLAY_1 + "SizeYLink";

	private static final String SizeY1 = OVERLAY_1 + "SizeY";

	private static final String SizeY1_RBV = OVERLAY_1 + "SizeY_RBV";

	private static final String Shape1 = OVERLAY_1 + "Shape";

	private static final String Shape1_RBV = OVERLAY_1 + "Shape_RBV";

	private static final String DrawMode1 = OVERLAY_1 + "DrawMode";

	private static final String DrawMode1_RBV = OVERLAY_1 + "DrawMode_RBV";

	private static final String Red1 = OVERLAY_1 + "Red";

	private static final String Red1_RBV = OVERLAY_1 + "Red_RBV";

	private static final String Green1 = OVERLAY_1 + "Green";

	private static final String Green1_RBV = OVERLAY_1 + "Green_RBV";

	private static final String Blue1 = OVERLAY_1 + "Blue";

	private static final String Blue1_RBV = OVERLAY_1 + "Blue_RBV";

	private static final String Name2 = OVERLAY_2 + "Name";

	private static final String Name_RBV2 = OVERLAY_2 + "Name_RBV";

	private static final String Use2 = OVERLAY_2 + "Use";

	private static final String Use2_RBV = OVERLAY_2 + "Use_RBV";

	private static final String PositionXLink2 = OVERLAY_2 + "PositionXLink";

	private static final String PositionX2 = OVERLAY_2 + "PositionX";

	private static final String PositionX2_RBV = OVERLAY_2 + "PositionX_RBV";

	private static final String PositionYLink2 = OVERLAY_2 + "PositionYLink";

	private static final String PositionY2 = OVERLAY_2 + "PositionY";

	private static final String PositionY2_RBV = OVERLAY_2 + "PositionY_RBV";

	private static final String SizeXLink2 = OVERLAY_2 + "SizeXLink";

	private static final String SizeX2 = OVERLAY_2 + "SizeX";

	private static final String SizeX2_RBV = OVERLAY_2 + "SizeX_RBV";

	private static final String SizeYLink2 = OVERLAY_2 + "SizeYLink";

	private static final String SizeY2 = OVERLAY_2 + "SizeY";

	private static final String SizeY2_RBV = OVERLAY_2 + "SizeY_RBV";

	private static final String Shape2 = OVERLAY_2 + "Shape";

	private static final String Shape2_RBV = OVERLAY_2 + "Shape_RBV";

	private static final String DrawMode2 = OVERLAY_2 + "DrawMode";

	private static final String DrawMode2_RBV = OVERLAY_2 + "DrawMode_RBV";

	private static final String Red2 = OVERLAY_2 + "Red";

	private static final String Red2_RBV = OVERLAY_2 + "Red_RBV";

	private static final String Green2 = OVERLAY_2 + "Green";

	private static final String Green2_RBV = OVERLAY_2 + "Green_RBV";

	private static final String Blue2 = OVERLAY_2 + "Blue";

	private static final String Blue2_RBV = OVERLAY_2 + "Blue_RBV";

	private static final String Name3 = OVERLAY_3 + "Name";

	private static final String Name_RBV3 = OVERLAY_3 + "Name_RBV";

	private static final String Use3 = OVERLAY_3 + "Use";

	private static final String Use3_RBV = OVERLAY_3 + "Use_RBV";

	private static final String PositionXLink3 = OVERLAY_3 + "PositionXLink";

	private static final String PositionX3 = OVERLAY_3 + "PositionX";

	private static final String PositionX3_RBV = OVERLAY_3 + "PositionX_RBV";

	private static final String PositionYLink3 = OVERLAY_3 + "PositionYLink";

	private static final String PositionY3 = OVERLAY_3 + "PositionY";

	private static final String PositionY3_RBV = OVERLAY_3 + "PositionY_RBV";

	private static final String SizeXLink3 = OVERLAY_3 + "SizeXLink";

	private static final String SizeX3 = OVERLAY_3 + "SizeX";

	private static final String SizeX3_RBV = OVERLAY_3 + "SizeX_RBV";

	private static final String SizeYLink3 = OVERLAY_3 + "SizeYLink";

	private static final String SizeY3 = OVERLAY_3 + "SizeY";

	private static final String SizeY3_RBV = OVERLAY_3 + "SizeY_RBV";

	private static final String Shape3 = OVERLAY_3 + "Shape";

	private static final String Shape3_RBV = OVERLAY_3 + "Shape_RBV";

	private static final String DrawMode3 = OVERLAY_3 + "DrawMode";

	private static final String DrawMode3_RBV = OVERLAY_3 + "DrawMode_RBV";

	private static final String Red3 = OVERLAY_3 + "Red";

	private static final String Red3_RBV = OVERLAY_3 + "Red_RBV";

	private static final String Green3 = OVERLAY_3 + "Green";

	private static final String Green3_RBV = OVERLAY_3 + "Green_RBV";

	private static final String Blue3 = OVERLAY_3 + "Blue";

	private static final String Blue3_RBV = OVERLAY_3 + "Blue_RBV";

	private static final String Name4 = OVERLAY_4 + "Name";

	private static final String Name_RBV4 = OVERLAY_4 + "Name_RBV";

	private static final String Use4 = OVERLAY_4 + "Use";

	private static final String Use4_RBV = OVERLAY_4 + "Use_RBV";

	private static final String PositionXLink4 = OVERLAY_4 + "PositionXLink";

	private static final String PositionX4 = OVERLAY_4 + "PositionX";

	private static final String PositionX4_RBV = OVERLAY_4 + "PositionX_RBV";

	private static final String PositionYLink4 = OVERLAY_4 + "PositionYLink";

	private static final String PositionY4 = OVERLAY_4 + "PositionY";

	private static final String PositionY4_RBV = OVERLAY_4 + "PositionY_RBV";

	private static final String SizeXLink4 = OVERLAY_4 + "SizeXLink";

	private static final String SizeX4 = OVERLAY_4 + "SizeX";

	private static final String SizeX4_RBV = OVERLAY_4 + "SizeX_RBV";

	private static final String SizeYLink4 = OVERLAY_4 + "SizeYLink";

	private static final String SizeY4 = OVERLAY_4 + "SizeY";

	private static final String SizeY4_RBV = OVERLAY_4 + "SizeY_RBV";

	private static final String Shape4 = OVERLAY_4 + "Shape";

	private static final String Shape4_RBV = OVERLAY_4 + "Shape_RBV";

	private static final String DrawMode4 = OVERLAY_4 + "DrawMode";

	private static final String DrawMode4_RBV = OVERLAY_4 + "DrawMode_RBV";

	private static final String Red4 = OVERLAY_4 + "Red";

	private static final String Red4_RBV = OVERLAY_4 + "Red_RBV";

	private static final String Green4 = OVERLAY_4 + "Green";

	private static final String Green4_RBV = OVERLAY_4 + "Green_RBV";

	private static final String Blue4 = OVERLAY_4 + "Blue";

	private static final String Blue4_RBV = OVERLAY_4 + "Blue_RBV";

	private static final String Name5 = OVERLAY_5 + "Name";

	private static final String Name_RBV5 = OVERLAY_5 + "Name_RBV";

	private static final String Use5 = OVERLAY_5 + "Use";

	private static final String Use5_RBV = OVERLAY_5 + "Use_RBV";

	private static final String PositionXLink5 = OVERLAY_5 + "PositionXLink";

	private static final String PositionX5 = OVERLAY_5 + "PositionX";

	private static final String PositionX5_RBV = OVERLAY_5 + "PositionX_RBV";

	private static final String PositionYLink5 = OVERLAY_5 + "PositionYLink";

	private static final String PositionY5 = OVERLAY_5 + "PositionY";

	private static final String PositionY5_RBV = OVERLAY_5 + "PositionY_RBV";

	private static final String SizeXLink5 = OVERLAY_5 + "SizeXLink";

	private static final String SizeX5 = OVERLAY_5 + "SizeX";

	private static final String SizeX5_RBV = OVERLAY_5 + "SizeX_RBV";

	private static final String SizeYLink5 = OVERLAY_5 + "SizeYLink";

	private static final String SizeY5 = OVERLAY_5 + "SizeY";

	private static final String SizeY5_RBV = OVERLAY_5 + "SizeY_RBV";

	private static final String Shape5 = OVERLAY_5 + "Shape";

	private static final String Shape5_RBV = OVERLAY_5 + "Shape_RBV";

	private static final String DrawMode5 = OVERLAY_5 + "DrawMode";

	private static final String DrawMode5_RBV = OVERLAY_5 + "DrawMode_RBV";

	private static final String Red5 = OVERLAY_5 + "Red";

	private static final String Red5_RBV = OVERLAY_5 + "Red_RBV";

	private static final String Green5 = OVERLAY_5 + "Green";

	private static final String Green5_RBV = OVERLAY_5 + "Green_RBV";

	private static final String Blue5 = OVERLAY_5 + "Blue";

	private static final String Blue5_RBV = OVERLAY_5 + "Blue_RBV";

	private static final String Name6 = OVERLAY_6 + "Name";

	private static final String Name_RBV6 = OVERLAY_6 + "Name_RBV";

	private static final String Use6 = OVERLAY_6 + "Use";

	private static final String Use6_RBV = OVERLAY_6 + "Use_RBV";

	private static final String PositionXLink6 = OVERLAY_6 + "PositionXLink";

	private static final String PositionX6 = OVERLAY_6 + "PositionX";

	private static final String PositionX6_RBV = OVERLAY_6 + "PositionX_RBV";

	private static final String PositionYLink6 = OVERLAY_6 + "PositionYLink";

	private static final String PositionY6 = OVERLAY_6 + "PositionY";

	private static final String PositionY6_RBV = OVERLAY_6 + "PositionY_RBV";

	private static final String SizeXLink6 = OVERLAY_6 + "SizeXLink";

	private static final String SizeX6 = OVERLAY_6 + "SizeX";

	private static final String SizeX6_RBV = OVERLAY_6 + "SizeX_RBV";

	private static final String SizeYLink6 = OVERLAY_6 + "SizeYLink";

	private static final String SizeY6 = OVERLAY_6 + "SizeY";

	private static final String SizeY6_RBV = OVERLAY_6 + "SizeY_RBV";

	private static final String Shape6 = OVERLAY_6 + "Shape";

	private static final String Shape6_RBV = OVERLAY_6 + "Shape_RBV";

	private static final String DrawMode6 = OVERLAY_6 + "DrawMode";

	private static final String DrawMode6_RBV = OVERLAY_6 + "DrawMode_RBV";

	private static final String Red6 = OVERLAY_6 + "Red";

	private static final String Red6_RBV = OVERLAY_6 + "Red_RBV";

	private static final String Green6 = OVERLAY_6 + "Green";

	private static final String Green6_RBV = OVERLAY_6 + "Green_RBV";

	private static final String Blue6 = OVERLAY_6 + "Blue";

	private static final String Blue6_RBV = OVERLAY_6 + "Blue_RBV";

	private static final String Name7 = OVERLAY_7 + "Name";

	private static final String Name_RBV7 = OVERLAY_7 + "Name_RBV";

	private static final String Use7 = OVERLAY_7 + "Use";

	private static final String Use7_RBV = OVERLAY_7 + "Use_RBV";

	private static final String PositionXLink7 = OVERLAY_7 + "PositionXLink";

	private static final String PositionX7 = OVERLAY_7 + "PositionX";

	private static final String PositionX7_RBV = OVERLAY_7 + "PositionX_RBV";

	private static final String PositionYLink7 = OVERLAY_7 + "PositionYLink";

	private static final String PositionY7 = OVERLAY_7 + "PositionY";

	private static final String PositionY7_RBV = OVERLAY_7 + "PositionY_RBV";

	private static final String SizeXLink7 = OVERLAY_7 + "SizeXLink";

	private static final String SizeX7 = OVERLAY_7 + "SizeX";

	private static final String SizeX7_RBV = OVERLAY_7 + "SizeX_RBV";

	private static final String SizeYLink7 = OVERLAY_7 + "SizeYLink";

	private static final String SizeY7 = OVERLAY_7 + "SizeY";

	private static final String SizeY7_RBV = OVERLAY_7 + "SizeY_RBV";

	private static final String Shape7 = OVERLAY_7 + "Shape";

	private static final String Shape7_RBV = OVERLAY_7 + "Shape_RBV";

	private static final String DrawMode7 = OVERLAY_7 + "DrawMode";

	private static final String DrawMode7_RBV = OVERLAY_7 + "DrawMode_RBV";

	private static final String Red7 = OVERLAY_7 + "Red";

	private static final String Red7_RBV = OVERLAY_7 + "Red_RBV";

	private static final String Green7 = OVERLAY_7 + "Green";

	private static final String Green7_RBV = OVERLAY_7 + "Green_RBV";

	private static final String Blue7 = OVERLAY_7 + "Blue";

	private static final String Blue7_RBV = OVERLAY_7 + "Blue_RBV";

	/**
	*
	*/
	@Override
	public String getName0() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.caget(createChannel(config.getName0().getPv()));
			}
			return EPICS_CONTROLLER.caget(getChannel(Name0));
		} catch (Exception ex) {
			logger.warn("Cannot getName0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setName0(String name0) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getName0().getPv()), name0);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Name0), name0);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setName0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getName_RBV0() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.caget(createChannel(config.getName_RBV0().getPv()));
			}
			return EPICS_CONTROLLER.caget(getChannel(Name_RBV0));
		} catch (Exception ex) {
			logger.warn("Cannot getName_RBV0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setName_RBV0(String name_rbv0) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getName_RBV0().getPv()), name_rbv0);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Name_RBV0), name_rbv0);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setName_RBV0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getUse0() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getUse0().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Use0));
		} catch (Exception ex) {
			logger.warn("Cannot getUse0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setUse0(int use0) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getUse0().getPv()), use0);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Use0), use0);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setUse0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getUse0_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getUse0_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Use0_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getUse0_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionXLink0() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionXLink0().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionXLink0));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionXLink0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionXLink0(int positionxlink0) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionXLink0().getPv()), positionxlink0);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionXLink0), positionxlink0);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionXLink0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionX0() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionX0().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionX0));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionX0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionX0(int positionx0) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionX0().getPv()), positionx0);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionX0), positionx0);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionX0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionX0_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionX0_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionX0_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionX0_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionYLink0() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionYLink0().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionYLink0));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionYLink0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionYLink0(int positionylink0) throws Exception {
		if (config != null) {
			EPICS_CONTROLLER.caput(createChannel(config.getPositionYLink0().getPv()), positionylink0);
		} else {
			EPICS_CONTROLLER.caput(getChannel(PositionYLink0), positionylink0);

		}
	}

	/**
	*
	*/
	@Override
	public int getPositionY0() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionY0().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionY0));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionY0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionY0(int positiony0) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionY0().getPv()), positiony0);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionY0), positiony0);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionY0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionY0_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionY0_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionY0_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionY0_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeXLink0() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeXLink0().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeXLink0));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeXLink0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeXLink0(int sizexlink0) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeXLink0().getPv()), sizexlink0);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeXLink0), sizexlink0);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeXLink0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeX0() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeX0().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeX0));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeX0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeX0(int sizex0) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeX0().getPv()), sizex0);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeX0), sizex0);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeX0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeX0_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeX0_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeX0_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeX0_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeYLink0() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeYLink0().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeYLink0));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeYLink0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeYLink0(int sizeylink0) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeYLink0().getPv()), sizeylink0);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeYLink0), sizeylink0);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeYLink0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeY0() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeY0().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeY0));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeY0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeY0(int sizey0) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeY0().getPv()), sizey0);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeY0), sizey0);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeY0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeY0_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeY0_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeY0_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeY0_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getShape0() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getShape0().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Shape0));
		} catch (Exception ex) {
			logger.warn("Cannot getShape0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setShape0(int shape0) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getShape0().getPv()), shape0);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Shape0), shape0);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setShape0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getShape0_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getShape0_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Shape0_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getShape0_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getDrawMode0() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getDrawMode0().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(DrawMode0));
		} catch (Exception ex) {
			logger.warn("Cannot getDrawMode0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setDrawMode0(int drawmode0) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getDrawMode0().getPv()), drawmode0);
			} else {
				EPICS_CONTROLLER.caput(getChannel(DrawMode0), drawmode0);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setDrawMode0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getDrawMode0_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getDrawMode0_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(DrawMode0_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getDrawMode0_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getRed0() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getRed0().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Red0));
		} catch (Exception ex) {
			logger.warn("Cannot getRed0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setRed0(int red0) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getRed0().getPv()), red0);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Red0), red0);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setRed0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getRed0_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getRed0_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Red0_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getRed0_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getGreen0() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getGreen0().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Green0));
		} catch (Exception ex) {
			logger.warn("Cannot getGreen0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setGreen0(int green0) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getGreen0().getPv()), green0);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Green0), green0);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setGreen0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getGreen0_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getGreen0_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Green0_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getGreen0_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBlue0() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBlue0().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Blue0));
		} catch (Exception ex) {
			logger.warn("Cannot getBlue0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setBlue0(int blue0) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getBlue0().getPv()), blue0);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Blue0), blue0);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setBlue0", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBlue0_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBlue0_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Blue0_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getBlue0_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getName1() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.caget(createChannel(config.getName1().getPv()));
			}
			return EPICS_CONTROLLER.caget(getChannel(Name1));
		} catch (Exception ex) {
			logger.warn("Cannot getName1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setName1(String name1) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getName1().getPv()), name1);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Name1), name1);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setName1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getName_RBV1() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.caget(createChannel(config.getName_RBV1().getPv()));
			}
			return EPICS_CONTROLLER.caget(getChannel(Name_RBV1));
		} catch (Exception ex) {
			logger.warn("Cannot getName_RBV1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setName_RBV1(String name_rbv1) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getName_RBV1().getPv()), name_rbv1);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Name_RBV1), name_rbv1);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setName_RBV1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getUse1() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getUse1().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Use1));
		} catch (Exception ex) {
			logger.warn("Cannot getUse1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setUse1(int use1) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getUse1().getPv()), use1);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Use1), use1);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setUse1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getUse1_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getUse1_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Use1_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getUse1_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionXLink1() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionXLink1().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionXLink1));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionXLink1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionXLink1(int positionxlink1) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionXLink1().getPv()), positionxlink1);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionXLink1), positionxlink1);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionXLink1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionX1() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionX1().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionX1));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionX1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionX1(int positionx1) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionX1().getPv()), positionx1);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionX1), positionx1);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionX1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionX1_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionX1_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionX1_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionX1_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionYLink1() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionYLink1().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionYLink1));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionYLink1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionYLink1(int positionylink1) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionYLink1().getPv()), positionylink1);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionYLink1), positionylink1);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionYLink1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionY1() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionY1().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionY1));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionY1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionY1(int positiony1) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionY1().getPv()), positiony1);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionY1), positiony1);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionY1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionY1_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionY1_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionY1_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionY1_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeXLink1() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeXLink1().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeXLink1));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeXLink1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeXLink1(int sizexlink1) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeXLink1().getPv()), sizexlink1);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeXLink1), sizexlink1);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeXLink1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeX1() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeX1().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeX1));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeX1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeX1(int sizex1) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeX1().getPv()), sizex1);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeX1), sizex1);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeX1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeX1_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeX1_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeX1_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeX1_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeYLink1() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeYLink1().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeYLink1));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeYLink1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeYLink1(int sizeylink1) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeYLink1().getPv()), sizeylink1);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeYLink1), sizeylink1);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeYLink1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeY1() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeY1().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeY1));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeY1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeY1(int sizey1) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeY1().getPv()), sizey1);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeY1), sizey1);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeY1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeY1_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeY1_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeY1_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeY1_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getShape1() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getShape1().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Shape1));
		} catch (Exception ex) {
			logger.warn("Cannot getShape1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setShape1(int shape1) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getShape1().getPv()), shape1);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Shape1), shape1);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setShape1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getShape1_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getShape1_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Shape1_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getShape1_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getDrawMode1() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getDrawMode1().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(DrawMode1));
		} catch (Exception ex) {
			logger.warn("Cannot getDrawMode1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setDrawMode1(int drawmode1) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getDrawMode1().getPv()), drawmode1);
			} else {
				EPICS_CONTROLLER.caput(getChannel(DrawMode1), drawmode1);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setDrawMode1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getDrawMode1_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getDrawMode1_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(DrawMode1_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getDrawMode1_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getRed1() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getRed1().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Red1));
		} catch (Exception ex) {
			logger.warn("Cannot getRed1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setRed1(int red1) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getRed1().getPv()), red1);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Red1), red1);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setRed1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getRed1_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getRed1_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Red1_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getRed1_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getGreen1() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getGreen1().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Green1));
		} catch (Exception ex) {
			logger.warn("Cannot getGreen1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setGreen1(int green1) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getGreen1().getPv()), green1);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Green1), green1);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setGreen1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getGreen1_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getGreen1_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Green1_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getGreen1_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBlue1() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBlue1().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Blue1));
		} catch (Exception ex) {
			logger.warn("Cannot getBlue1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setBlue1(int blue1) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getBlue1().getPv()), blue1);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Blue1), blue1);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setBlue1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBlue1_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBlue1_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Blue1_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getBlue1_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getName2() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.caget(createChannel(config.getName2().getPv()));
			}
			return EPICS_CONTROLLER.caget(getChannel(Name2));
		} catch (Exception ex) {
			logger.warn("Cannot getName2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setName2(String name2) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getName2().getPv()), name2);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Name2), name2);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setName2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getName_RBV2() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.caget(createChannel(config.getName_RBV2().getPv()));
			}
			return EPICS_CONTROLLER.caget(getChannel(Name_RBV2));
		} catch (Exception ex) {
			logger.warn("Cannot getName_RBV2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setName_RBV2(String name_rbv2) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getName_RBV2().getPv()), name_rbv2);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Name_RBV2), name_rbv2);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setName_RBV2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getUse2() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getUse2().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Use2));
		} catch (Exception ex) {
			logger.warn("Cannot getUse2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setUse2(int use2) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getUse2().getPv()), use2);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Use2), use2);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setUse2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getUse2_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getUse2_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Use2_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getUse2_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionXLink2() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionXLink2().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionXLink2));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionXLink2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionXLink2(int positionxlink2) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionXLink2().getPv()), positionxlink2);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionXLink2), positionxlink2);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionXLink2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionX2() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionX2().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionX2));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionX2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionX2(int positionx2) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionX2().getPv()), positionx2);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionX2), positionx2);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionX2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionX2_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionX2_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionX2_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionX2_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionYLink2() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionYLink2().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionYLink2));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionYLink2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionYLink2(int positionylink2) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionYLink2().getPv()), positionylink2);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionYLink2), positionylink2);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionYLink2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionY2() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionY2().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionY2));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionY2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionY2(int positiony2) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionY2().getPv()), positiony2);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionY2), positiony2);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionY2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionY2_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionY2_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionY2_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionY2_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeXLink2() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeXLink2().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeXLink2));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeXLink2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeXLink2(int sizexlink2) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeXLink2().getPv()), sizexlink2);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeXLink2), sizexlink2);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeXLink2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeX2() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeX2().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeX2));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeX2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeX2(int sizex2) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeX2().getPv()), sizex2);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeX2), sizex2);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeX2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeX2_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeX2_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeX2_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeX2_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeYLink2() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeYLink2().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeYLink2));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeYLink2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeYLink2(int sizeylink2) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeYLink2().getPv()), sizeylink2);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeYLink2), sizeylink2);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeYLink2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeY2() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeY2().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeY2));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeY2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeY2(int sizey2) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeY2().getPv()), sizey2);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeY2), sizey2);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeY2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeY2_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeY2_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeY2_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeY2_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getShape2() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getShape2().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Shape2));
		} catch (Exception ex) {
			logger.warn("Cannot getShape2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setShape2(int shape2) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getShape2().getPv()), shape2);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Shape2), shape2);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setShape2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getShape2_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getShape2_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Shape2_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getShape2_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getDrawMode2() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getDrawMode2().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(DrawMode2));
		} catch (Exception ex) {
			logger.warn("Cannot getDrawMode2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setDrawMode2(int drawmode2) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getDrawMode2().getPv()), drawmode2);
			} else {
				EPICS_CONTROLLER.caput(getChannel(DrawMode2), drawmode2);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setDrawMode2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getDrawMode2_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getDrawMode2_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(DrawMode2_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getDrawMode2_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getRed2() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getRed2().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Red2));
		} catch (Exception ex) {
			logger.warn("Cannot getRed2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setRed2(int red2) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getRed2().getPv()), red2);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Red2), red2);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setRed2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getRed2_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getRed2_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Red2_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getRed2_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getGreen2() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getGreen2().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Green2));
		} catch (Exception ex) {
			logger.warn("Cannot getGreen2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setGreen2(int green2) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getGreen2().getPv()), green2);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Green2), green2);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setGreen2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getGreen2_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getGreen2_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Green2_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getGreen2_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBlue2() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBlue2().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Blue2));
		} catch (Exception ex) {
			logger.warn("Cannot getBlue2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setBlue2(int blue2) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getBlue2().getPv()), blue2);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Blue2), blue2);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setBlue2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBlue2_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBlue2_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Blue2_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getBlue2_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getName3() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.caget(createChannel(config.getName3().getPv()));
			}
			return EPICS_CONTROLLER.caget(getChannel(Name3));
		} catch (Exception ex) {
			logger.warn("Cannot getName3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setName3(String name3) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getName3().getPv()), name3);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Name3), name3);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setName3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getName_RBV3() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.caget(createChannel(config.getName_RBV3().getPv()));
			}
			return EPICS_CONTROLLER.caget(getChannel(Name_RBV3));
		} catch (Exception ex) {
			logger.warn("Cannot getName_RBV3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setName_RBV3(String name_rbv3) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getName_RBV3().getPv()), name_rbv3);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Name_RBV3), name_rbv3);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setName_RBV3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getUse3() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getUse3().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Use3));
		} catch (Exception ex) {
			logger.warn("Cannot getUse3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setUse3(int use3) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getUse3().getPv()), use3);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Use3), use3);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setUse3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getUse3_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getUse3_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Use3_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getUse3_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionXLink3() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionXLink3().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionXLink3));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionXLink3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionXLink3(int positionxlink3) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionXLink3().getPv()), positionxlink3);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionXLink3), positionxlink3);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionXLink3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionX3() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionX3().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionX3));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionX3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionX3(int positionx3) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionX3().getPv()), positionx3);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionX3), positionx3);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionX3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionX3_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionX3_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionX3_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionX3_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionYLink3() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionYLink3().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionYLink3));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionYLink3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionYLink3(int positionylink3) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionYLink3().getPv()), positionylink3);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionYLink3), positionylink3);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionYLink3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionY3() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionY3().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionY3));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionY3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionY3(int positiony3) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionY3().getPv()), positiony3);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionY3), positiony3);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionY3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionY3_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionY3_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionY3_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionY3_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeXLink3() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeXLink3().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeXLink3));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeXLink3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeXLink3(int sizexlink3) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeXLink3().getPv()), sizexlink3);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeXLink3), sizexlink3);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeXLink3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeX3() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeX3().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeX3));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeX3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeX3(int sizex3) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeX3().getPv()), sizex3);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeX3), sizex3);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeX3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeX3_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeX3_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeX3_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeX3_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeYLink3() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeYLink3().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeYLink3));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeYLink3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeYLink3(int sizeylink3) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeYLink3().getPv()), sizeylink3);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeYLink3), sizeylink3);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeYLink3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeY3() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeY3().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeY3));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeY3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeY3(int sizey3) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeY3().getPv()), sizey3);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeY3), sizey3);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeY3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeY3_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeY3_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeY3_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeY3_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getShape3() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getShape3().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Shape3));
		} catch (Exception ex) {
			logger.warn("Cannot getShape3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setShape3(int shape3) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getShape3().getPv()), shape3);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Shape3), shape3);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setShape3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getShape3_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getShape3_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Shape3_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getShape3_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getDrawMode3() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getDrawMode3().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(DrawMode3));
		} catch (Exception ex) {
			logger.warn("Cannot getDrawMode3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setDrawMode3(int drawmode3) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getDrawMode3().getPv()), drawmode3);
			} else {
				EPICS_CONTROLLER.caput(getChannel(DrawMode3), drawmode3);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setDrawMode3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getDrawMode3_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getDrawMode3_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(DrawMode3_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getDrawMode3_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getRed3() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getRed3().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Red3));
		} catch (Exception ex) {
			logger.warn("Cannot getRed3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setRed3(int red3) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getRed3().getPv()), red3);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Red3), red3);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setRed3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getRed3_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getRed3_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Red3_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getRed3_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getGreen3() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getGreen3().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Green3));
		} catch (Exception ex) {
			logger.warn("Cannot getGreen3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setGreen3(int green3) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getGreen3().getPv()), green3);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Green3), green3);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setGreen3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getGreen3_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getGreen3_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Green3_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getGreen3_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBlue3() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBlue3().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Blue3));
		} catch (Exception ex) {
			logger.warn("Cannot getBlue3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setBlue3(int blue3) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getBlue3().getPv()), blue3);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Blue3), blue3);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setBlue3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBlue3_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBlue3_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Blue3_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getBlue3_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getName4() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.caget(createChannel(config.getName4().getPv()));
			}
			return EPICS_CONTROLLER.caget(getChannel(Name4));
		} catch (Exception ex) {
			logger.warn("Cannot getName4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setName4(String name4) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getName4().getPv()), name4);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Name4), name4);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setName4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getName_RBV4() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.caget(createChannel(config.getName_RBV4().getPv()));
			}
			return EPICS_CONTROLLER.caget(getChannel(Name_RBV4));
		} catch (Exception ex) {
			logger.warn("Cannot getName_RBV4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setName_RBV4(String name_rbv4) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getName_RBV4().getPv()), name_rbv4);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Name_RBV4), name_rbv4);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setName_RBV4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getUse4() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getUse4().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Use4));
		} catch (Exception ex) {
			logger.warn("Cannot getUse4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setUse4(int use4) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getUse4().getPv()), use4);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Use4), use4);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setUse4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getUse4_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getUse4_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Use4_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getUse4_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionXLink4() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionXLink4().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionXLink4));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionXLink4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionXLink4(int positionxlink4) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionXLink4().getPv()), positionxlink4);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionXLink4), positionxlink4);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionXLink4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionX4() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionX4().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionX4));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionX4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionX4(int positionx4) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionX4().getPv()), positionx4);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionX4), positionx4);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionX4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionX4_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionX4_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionX4_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionX4_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionYLink4() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionYLink4().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionYLink4));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionYLink4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionYLink4(int positionylink4) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionYLink4().getPv()), positionylink4);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionYLink4), positionylink4);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionYLink4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionY4() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionY4().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionY4));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionY4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionY4(int positiony4) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionY4().getPv()), positiony4);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionY4), positiony4);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionY4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionY4_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionY4_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionY4_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionY4_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeXLink4() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeXLink4().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeXLink4));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeXLink4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeXLink4(int sizexlink4) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeXLink4().getPv()), sizexlink4);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeXLink4), sizexlink4);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeXLink4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeX4() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeX4().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeX4));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeX4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeX4(int sizex4) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeX4().getPv()), sizex4);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeX4), sizex4);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeX4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeX4_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeX4_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeX4_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeX4_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeYLink4() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeYLink4().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeYLink4));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeYLink4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeYLink4(int sizeylink4) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeYLink4().getPv()), sizeylink4);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeYLink4), sizeylink4);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeYLink4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeY4() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeY4().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeY4));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeY4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeY4(int sizey4) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeY4().getPv()), sizey4);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeY4), sizey4);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeY4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeY4_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeY4_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeY4_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeY4_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getShape4() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getShape4().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Shape4));
		} catch (Exception ex) {
			logger.warn("Cannot getShape4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setShape4(int shape4) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getShape4().getPv()), shape4);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Shape4), shape4);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setShape4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getShape4_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getShape4_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Shape4_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getShape4_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getDrawMode4() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getDrawMode4().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(DrawMode4));
		} catch (Exception ex) {
			logger.warn("Cannot getDrawMode4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setDrawMode4(int drawmode4) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getDrawMode4().getPv()), drawmode4);
			} else {
				EPICS_CONTROLLER.caput(getChannel(DrawMode4), drawmode4);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setDrawMode4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getDrawMode4_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getDrawMode4_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(DrawMode4_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getDrawMode4_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getRed4() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getRed4().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Red4));
		} catch (Exception ex) {
			logger.warn("Cannot getRed4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setRed4(int red4) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getRed4().getPv()), red4);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Red4), red4);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setRed4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getRed4_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getRed4_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Red4_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getRed4_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getGreen4() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getGreen4().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Green4));
		} catch (Exception ex) {
			logger.warn("Cannot getGreen4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setGreen4(int green4) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getGreen4().getPv()), green4);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Green4), green4);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setGreen4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getGreen4_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getGreen4_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Green4_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getGreen4_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBlue4() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBlue4().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Blue4));
		} catch (Exception ex) {
			logger.warn("Cannot getBlue4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setBlue4(int blue4) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getBlue4().getPv()), blue4);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Blue4), blue4);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setBlue4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBlue4_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBlue4_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Blue4_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getBlue4_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getName5() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.caget(createChannel(config.getName5().getPv()));
			}
			return EPICS_CONTROLLER.caget(getChannel(Name5));
		} catch (Exception ex) {
			logger.warn("Cannot getName5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setName5(String name5) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getName5().getPv()), name5);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Name5), name5);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setName5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getName_RBV5() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.caget(createChannel(config.getName_RBV5().getPv()));
			}
			return EPICS_CONTROLLER.caget(getChannel(Name_RBV5));
		} catch (Exception ex) {
			logger.warn("Cannot getName_RBV5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setName_RBV5(String name_rbv5) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getName_RBV5().getPv()), name_rbv5);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Name_RBV5), name_rbv5);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setName_RBV5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getUse5() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getUse5().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Use5));
		} catch (Exception ex) {
			logger.warn("Cannot getUse5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setUse5(int use5) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getUse5().getPv()), use5);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Use5), use5);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setUse5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getUse5_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getUse5_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Use5_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getUse5_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionXLink5() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionXLink5().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionXLink5));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionXLink5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionXLink5(int positionxlink5) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionXLink5().getPv()), positionxlink5);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionXLink5), positionxlink5);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionXLink5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionX5() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionX5().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionX5));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionX5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionX5(int positionx5) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionX5().getPv()), positionx5);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionX5), positionx5);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionX5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionX5_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionX5_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionX5_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionX5_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionYLink5() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionYLink5().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionYLink5));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionYLink5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionYLink5(int positionylink5) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionYLink5().getPv()), positionylink5);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionYLink5), positionylink5);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionYLink5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionY5() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionY5().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionY5));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionY5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionY5(int positiony5) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionY5().getPv()), positiony5);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionY5), positiony5);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionY5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionY5_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionY5_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionY5_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionY5_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeXLink5() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeXLink5().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeXLink5));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeXLink5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeXLink5(int sizexlink5) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeXLink5().getPv()), sizexlink5);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeXLink5), sizexlink5);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeXLink5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeX5() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeX5().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeX5));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeX5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeX5(int sizex5) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeX5().getPv()), sizex5);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeX5), sizex5);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeX5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeX5_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeX5_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeX5_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeX5_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeYLink5() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeYLink5().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeYLink5));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeYLink5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeYLink5(int sizeylink5) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeYLink5().getPv()), sizeylink5);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeYLink5), sizeylink5);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeYLink5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeY5() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeY5().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeY5));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeY5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeY5(int sizey5) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeY5().getPv()), sizey5);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeY5), sizey5);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeY5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeY5_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeY5_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeY5_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeY5_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getShape5() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getShape5().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Shape5));
		} catch (Exception ex) {
			logger.warn("Cannot getShape5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setShape5(int shape5) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getShape5().getPv()), shape5);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Shape5), shape5);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setShape5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getShape5_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getShape5_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Shape5_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getShape5_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getDrawMode5() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getDrawMode5().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(DrawMode5));
		} catch (Exception ex) {
			logger.warn("Cannot getDrawMode5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setDrawMode5(int drawmode5) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getDrawMode5().getPv()), drawmode5);
			} else {
				EPICS_CONTROLLER.caput(getChannel(DrawMode5), drawmode5);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setDrawMode5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getDrawMode5_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getDrawMode5_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(DrawMode5_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getDrawMode5_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getRed5() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getRed5().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Red5));
		} catch (Exception ex) {
			logger.warn("Cannot getRed5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setRed5(int red5) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getRed5().getPv()), red5);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Red5), red5);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setRed5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getRed5_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getRed5_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Red5_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getRed5_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getGreen5() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getGreen5().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Green5));
		} catch (Exception ex) {
			logger.warn("Cannot getGreen5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setGreen5(int green5) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getGreen5().getPv()), green5);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Green5), green5);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setGreen5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getGreen5_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getGreen5_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Green5_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getGreen5_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBlue5() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBlue5().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Blue5));
		} catch (Exception ex) {
			logger.warn("Cannot getBlue5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setBlue5(int blue5) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getBlue5().getPv()), blue5);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Blue5), blue5);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setBlue5", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBlue5_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBlue5_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Blue5_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getBlue5_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getName6() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.caget(createChannel(config.getName6().getPv()));
			}
			return EPICS_CONTROLLER.caget(getChannel(Name6));
		} catch (Exception ex) {
			logger.warn("Cannot getName6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setName6(String name6) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getName6().getPv()), name6);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Name6), name6);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setName6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getName_RBV6() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.caget(createChannel(config.getName_RBV6().getPv()));
			}
			return EPICS_CONTROLLER.caget(getChannel(Name_RBV6));
		} catch (Exception ex) {
			logger.warn("Cannot getName_RBV6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setName_RBV6(String name_rbv6) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getName_RBV6().getPv()), name_rbv6);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Name_RBV6), name_rbv6);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setName_RBV6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getUse6() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getUse6().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Use6));
		} catch (Exception ex) {
			logger.warn("Cannot getUse6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setUse6(int use6) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getUse6().getPv()), use6);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Use6), use6);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setUse6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getUse6_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getUse6_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Use6_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getUse6_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionXLink6() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionXLink6().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionXLink6));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionXLink6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionXLink6(int positionxlink6) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionXLink6().getPv()), positionxlink6);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionXLink6), positionxlink6);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionXLink6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionX6() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionX6().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionX6));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionX6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionX6(int positionx6) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionX6().getPv()), positionx6);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionX6), positionx6);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionX6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionX6_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionX6_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionX6_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionX6_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionYLink6() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionYLink6().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionYLink6));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionYLink6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionYLink6(int positionylink6) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionYLink6().getPv()), positionylink6);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionYLink6), positionylink6);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionYLink6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionY6() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionY6().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionY6));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionY6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionY6(int positiony6) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionY6().getPv()), positiony6);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionY6), positiony6);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionY6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionY6_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionY6_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionY6_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionY6_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeXLink6() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeXLink6().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeXLink6));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeXLink6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeXLink6(int sizexlink6) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeXLink6().getPv()), sizexlink6);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeXLink6), sizexlink6);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeXLink6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeX6() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeX6().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeX6));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeX6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeX6(int sizex6) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeX6().getPv()), sizex6);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeX6), sizex6);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeX6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeX6_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeX6_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeX6_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeX6_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeYLink6() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeYLink6().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeYLink6));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeYLink6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeYLink6(int sizeylink6) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeYLink6().getPv()), sizeylink6);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeYLink6), sizeylink6);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeYLink6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeY6() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeY6().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeY6));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeY6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeY6(int sizey6) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeY6().getPv()), sizey6);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeY6), sizey6);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeY6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeY6_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeY6_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeY6_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeY6_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getShape6() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getShape6().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Shape6));
		} catch (Exception ex) {
			logger.warn("Cannot getShape6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setShape6(int shape6) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getShape6().getPv()), shape6);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Shape6), shape6);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setShape6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getShape6_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getShape6_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Shape6_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getShape6_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getDrawMode6() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getDrawMode6().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(DrawMode6));
		} catch (Exception ex) {
			logger.warn("Cannot getDrawMode6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setDrawMode6(int drawmode6) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getDrawMode6().getPv()), drawmode6);
			} else {
				EPICS_CONTROLLER.caput(getChannel(DrawMode6), drawmode6);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setDrawMode6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getDrawMode6_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getDrawMode6_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(DrawMode6_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getDrawMode6_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getRed6() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getRed6().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Red6));
		} catch (Exception ex) {
			logger.warn("Cannot getRed6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setRed6(int red6) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getRed6().getPv()), red6);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Red6), red6);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setRed6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getRed6_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getRed6_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Red6_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getRed6_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getGreen6() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getGreen6().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Green6));
		} catch (Exception ex) {
			logger.warn("Cannot getGreen6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setGreen6(int green6) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getGreen6().getPv()), green6);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Green6), green6);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setGreen6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getGreen6_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getGreen6_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Green6_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getGreen6_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBlue6() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBlue6().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Blue6));
		} catch (Exception ex) {
			logger.warn("Cannot getBlue6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setBlue6(int blue6) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getBlue6().getPv()), blue6);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Blue6), blue6);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setBlue6", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBlue6_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBlue6_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Blue6_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getBlue6_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getName7() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.caget(createChannel(config.getName7().getPv()));
			}
			return EPICS_CONTROLLER.caget(getChannel(Name7));
		} catch (Exception ex) {
			logger.warn("Cannot getName7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setName7(String name7) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getName7().getPv()), name7);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Name7), name7);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setName7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public String getName_RBV7() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.caget(createChannel(config.getName_RBV7().getPv()));
			}
			return EPICS_CONTROLLER.caget(getChannel(Name_RBV7));
		} catch (Exception ex) {
			logger.warn("Cannot getName_RBV7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setName_RBV7(String name_rbv7) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getName_RBV7().getPv()), name_rbv7);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Name_RBV7), name_rbv7);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setName_RBV7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getUse7() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getUse7().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Use7));
		} catch (Exception ex) {
			logger.warn("Cannot getUse7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setUse7(int use7) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getUse7().getPv()), use7);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Use7), use7);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setUse7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getUse7_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getUse7_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Use7_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getUse7_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionXLink7() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionXLink7().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionXLink7));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionXLink7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionXLink7(int positionxlink7) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionXLink7().getPv()), positionxlink7);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionXLink7), positionxlink7);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionXLink7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionX7() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionX7().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionX7));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionX7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionX7(int positionx7) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionX7().getPv()), positionx7);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionX7), positionx7);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionX7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionX7_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionX7_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionX7_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionX7_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionYLink7() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionYLink7().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionYLink7));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionYLink7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionYLink7(int positionylink7) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionYLink7().getPv()), positionylink7);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionYLink7), positionylink7);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionYLink7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionY7() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionY7().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionY7));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionY7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setPositionY7(int positiony7) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getPositionY7().getPv()), positiony7);
			} else {
				EPICS_CONTROLLER.caput(getChannel(PositionY7), positiony7);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setPositionY7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getPositionY7_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getPositionY7_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(PositionY7_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getPositionY7_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeXLink7() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeXLink7().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeXLink7));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeXLink7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeXLink7(int sizexlink7) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeXLink7().getPv()), sizexlink7);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeXLink7), sizexlink7);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeXLink7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeX7() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeX7().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeX7));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeX7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeX7(int sizex7) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeX7().getPv()), sizex7);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeX7), sizex7);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeX7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeX7_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeX7_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeX7_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeX7_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeYLink7() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeYLink7().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeYLink7));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeYLink7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeYLink7(int sizeylink7) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeYLink7().getPv()), sizeylink7);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeYLink7), sizeylink7);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeYLink7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeY7() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeY7().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeY7));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeY7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSizeY7(int sizey7) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeY7().getPv()), sizey7);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SizeY7), sizey7);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setSizeY7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getSizeY7_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeY7_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(SizeY7_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSizeY7_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getShape7() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getShape7().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Shape7));
		} catch (Exception ex) {
			logger.warn("Cannot getShape7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setShape7(int shape7) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getShape7().getPv()), shape7);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Shape7), shape7);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setShape7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getShape7_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getShape7_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Shape7_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getShape7_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getDrawMode7() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getDrawMode7().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(DrawMode7));
		} catch (Exception ex) {
			logger.warn("Cannot getDrawMode7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setDrawMode7(int drawmode7) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getDrawMode7().getPv()), drawmode7);
			} else {
				EPICS_CONTROLLER.caput(getChannel(DrawMode7), drawmode7);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setDrawMode7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getDrawMode7_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getDrawMode7_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(DrawMode7_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getDrawMode7_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getRed7() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getRed7().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Red7));
		} catch (Exception ex) {
			logger.warn("Cannot getRed7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setRed7(int red7) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getRed7().getPv()), red7);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Red7), red7);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setRed7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getRed7_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getRed7_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Red7_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getRed7_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getGreen7() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getGreen7().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Green7));
		} catch (Exception ex) {
			logger.warn("Cannot getGreen7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setGreen7(int green7) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getGreen7().getPv()), green7);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Green7), green7);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setGreen7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getGreen7_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getGreen7_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Green7_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getGreen7_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBlue7() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBlue7().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Blue7));
		} catch (Exception ex) {
			logger.warn("Cannot getBlue7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setBlue7(int blue7) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getBlue7().getPv()), blue7);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Blue7), blue7);
			}
		} catch (Exception ex) {
			logger.warn("Cannot setBlue7", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getBlue7_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBlue7_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(Blue7_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getBlue7_RBV", ex);
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

	public void setPvProvider(IPVProvider pvProvider) {
		this.pvProvider = pvProvider;
	}

	@Override
	public void reset() throws Exception {
		getPluginBase().reset();
	}

}
