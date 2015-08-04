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
import gda.device.detector.areadetector.v17.NDProcess;
import gda.epics.LazyPVFactory;
import gda.epics.connection.EpicsController;
import gda.epics.interfaces.NDProcessType;
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

public class NDProcessImpl extends NDBaseImpl implements InitializingBean, NDProcess {

	private final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	private String basePVName;

	private IPVProvider pvProvider;
	private Integer initialEnableBackground;

	public int getInitialEnableBackground() {
		return initialEnableBackground;
	}

	public void setInitialEnableBackground(int initialEnableBackground) {
		this.initialEnableBackground = initialEnableBackground;
	}

	private Integer initialEnableFlatField;
	private Integer initialEnableOffsetScale;

	public int getInitialEnableOffsetScale() {
		return initialEnableOffsetScale;
	}

	public void setInitialEnableOffsetScale(int initialEnableOffsetScale) {
		this.initialEnableOffsetScale = initialEnableOffsetScale;
	}

	public int getInitialEnableHighClip() {
		return initialEnableHighClip;
	}

	public void setInitialEnableHighClip(int initialEnableHighClip) {
		this.initialEnableHighClip = initialEnableHighClip;
	}

	public int getInitialEnableLowClip() {
		return initialEnableLowClip;
	}

	public void setInitialEnableLowClip(int initialEnableLowClip) {
		this.initialEnableLowClip = initialEnableLowClip;
	}

	public int getInitialEnableFilter() {
		return initialEnableFilter;
	}

	public void setInitialEnableFilter(int initialEnableFilter) {
		this.initialEnableFilter = initialEnableFilter;
	}

	private Integer initialEnableHighClip;
	private Integer initialEnableLowClip;
	private Integer initialEnableFilter;

	public int getInitialEnableFlatField() {
		return initialEnableFlatField;
	}

	public void setInitialEnableFlatField(int initialEnableFlatField) {
		this.initialEnableFlatField = initialEnableFlatField;
	}

	private NDProcessType config;

	private String deviceName;

	static final Logger logger = LoggerFactory.getLogger(NDProcessImpl.class);

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
				config = Configurator.getConfiguration(getDeviceName(), NDProcessType.class);
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
	public short getDataTypeOut() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getDataTypeOut().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(DataTypeOut));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getDataTypeOut", ex);
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
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setDataTypeOut", ex);
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
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getDataTypeOut_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getSaveBackground() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getSaveBackground().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(SaveBackground));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getSaveBackground", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSaveBackground(int savebackground) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSaveBackground().getPv()), savebackground);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SaveBackground), savebackground);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setSaveBackground", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getSaveBackground_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getSaveBackground_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(SaveBackground_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getSaveBackground_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getEnableBackground() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getEnableBackground().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableBackground));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getEnableBackground", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setEnableBackground(int enablebackground) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getEnableBackground().getPv()), enablebackground);
			} else {
				EPICS_CONTROLLER.caput(getChannel(EnableBackground), enablebackground);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setEnableBackground", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getEnableBackground_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getEnableBackground_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableBackground_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getEnableBackground_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getValidBackground_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getValidBackground_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(ValidBackground_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getValidBackground_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getSaveFlatField() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getSaveFlatField().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(SaveFlatField));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getSaveFlatField", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSaveFlatField(int saveflatfield) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSaveFlatField().getPv()), saveflatfield);
			} else {
				EPICS_CONTROLLER.caput(getChannel(SaveFlatField), saveflatfield);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setSaveFlatField", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getSaveFlatField_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getSaveFlatField_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(SaveFlatField_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getSaveFlatField_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getEnableFlatField() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getEnableFlatField().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableFlatField));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getEnableFlatField", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setEnableFlatField(int enableflatfield) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getEnableFlatField().getPv()), enableflatfield);
			} else {
				EPICS_CONTROLLER.caput(getChannel(EnableFlatField), enableflatfield);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setEnableFlatField", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getEnableFlatField_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getEnableFlatField_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableFlatField_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getEnableFlatField_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getValidFlatField_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getValidFlatField_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(ValidFlatField_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getValidFlatField_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getScaleFlatField() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getScaleFlatField().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(ScaleFlatField));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getScaleFlatField", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setScaleFlatField(double scaleflatfield) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getScaleFlatField().getPv()), scaleflatfield);
			} else {
				EPICS_CONTROLLER.caput(getChannel(ScaleFlatField), scaleflatfield);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setScaleFlatField", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getScaleFlatField_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getScaleFlatField_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(ScaleFlatField_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getScaleFlatField_RBV", ex);
			throw ex;
		}
	}

	@Override
	public short getAutoOffsetScale() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getAutoOffsetScale().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(AutoOffsetScale));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getAutoOffsetScale", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setAutoOffsetScale(int autooffsetscale) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getAutoOffsetScale().getPv()), autooffsetscale);
			} else {
				EPICS_CONTROLLER.caput(getChannel(AutoOffsetScale), autooffsetscale);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setAutoOffsetScale", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getEnableOffsetScale() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getEnableOffsetScale().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableOffsetScale));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getEnableOffsetScale", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setEnableOffsetScale(int enableoffsetscale) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getEnableOffsetScale().getPv()), enableoffsetscale);
			} else {
				EPICS_CONTROLLER.caput(getChannel(EnableOffsetScale), enableoffsetscale);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setEnableOffsetScale", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getEnableOffsetScale_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getEnableOffsetScale_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableOffsetScale_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getEnableOffsetScale_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOffset() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getOffset().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(Offset));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getOffset", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setOffset(double offset) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getOffset().getPv()), offset);
			} else {
				EPICS_CONTROLLER.caput(getChannel(Offset), offset);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setOffset", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOffset_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getOffset_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Offset_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getOffset_RBV", ex);
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
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getScale", ex);
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
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setScale", ex);
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
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getScale_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getEnableLowClip() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getEnableLowClip().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableLowClip));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getEnableLowClip", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setEnableLowClip(int enablelowclip) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getEnableLowClip().getPv()), enablelowclip);
			} else {
				EPICS_CONTROLLER.caput(getChannel(EnableLowClip), enablelowclip);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setEnableLowClip", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getEnableLowClip_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getEnableLowClip_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableLowClip_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getEnableLowClip_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getLowClip() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getLowClip().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(LowClip));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getLowClip", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setLowClip(double lowclip) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getLowClip().getPv()), lowclip);
			} else {
				EPICS_CONTROLLER.caput(getChannel(LowClip), lowclip);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setLowClip", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getLowClip_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getLowClip_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(LowClip_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getLowClip_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getEnableHighClip() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getEnableHighClip().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableHighClip));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getEnableHighClip", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setEnableHighClip(int enablehighclip) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getEnableHighClip().getPv()), enablehighclip);
			} else {
				EPICS_CONTROLLER.caput(getChannel(EnableHighClip), enablehighclip);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setEnableHighClip", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getEnableHighClip_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getEnableHighClip_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableHighClip_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getEnableHighClip_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getHighClip() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getHighClip().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(HighClip));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getHighClip", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setHighClip(double highclip) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getHighClip().getPv()), highclip);
			} else {
				EPICS_CONTROLLER.caput(getChannel(HighClip), highclip);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setHighClip", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getHighClip_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getHighClip_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(HighClip_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getHighClip_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getEnableFilter() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getEnableFilter().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableFilter));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getEnableFilter", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setEnableFilter(int enablefilter) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getEnableFilter().getPv()), enablefilter);
			} else {
				EPICS_CONTROLLER.caput(getChannel(EnableFilter), enablefilter);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setEnableFilter", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getEnableFilter_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getEnableFilter_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableFilter_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getEnableFilter_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getResetFilter() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getResetFilter().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(ResetFilter));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getResetFilter", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setResetFilter(int resetfilter) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caputWait(createChannel(config.getResetFilter().getPv()), resetfilter);
			} else {
				EPICS_CONTROLLER.caputWait(getChannel(ResetFilter), resetfilter);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setResetFilter", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getResetFilter_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getResetFilter_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(ResetFilter_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getResetFilter_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getNumFilter() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getNumFilter().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(NumFilter));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getNumFilter", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setNumFilter(int numfilter) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getNumFilter().getPv()), numfilter);
			} else {
				EPICS_CONTROLLER.caput(getChannel(NumFilter), numfilter);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setNumFilter", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getNumFilter_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNumFilter_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(NumFilter_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getNumFilter_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getNumFilterRecip() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNumFilterRecip().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(NumFilterRecip));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getNumFilterRecip", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setNumFilterRecip(int numfilterrecip) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getNumFilterRecip().getPv()), numfilterrecip);
			} else {
				EPICS_CONTROLLER.caput(getChannel(NumFilterRecip), numfilterrecip);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setNumFilterRecip", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getNumFiltered_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNumFiltered_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(NumFiltered_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getNumFiltered_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOOffset() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getOOffset().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(OOffset));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getOOffset", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setOOffset(double ooffset) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getOOffset().getPv()), ooffset);
			} else {
				EPICS_CONTROLLER.caput(getChannel(OOffset), ooffset);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setOOffset", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOOffset_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getOOffset_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(OOffset_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getOOffset_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOScale() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getOScale().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(OScale));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getOScale", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setOScale(double oscale) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getOScale().getPv()), oscale);
			} else {
				EPICS_CONTROLLER.caput(getChannel(OScale), oscale);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setOScale", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOScale_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getOScale_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(OScale_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getOScale_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOC1() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getOC1().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(OC1));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getOC1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setOC1(double oc1) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getOC1().getPv()), oc1);
			} else {
				EPICS_CONTROLLER.caput(getChannel(OC1), oc1);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setOC1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOC1_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getOC1_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(OC1_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getOC1_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOC2() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getOC2().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(OC2));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getOC2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setOC2(double oc2) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getOC2().getPv()), oc2);
			} else {
				EPICS_CONTROLLER.caput(getChannel(OC2), oc2);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setOC2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOC2_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getOC2_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(OC2_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getOC2_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOC3() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getOC3().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(OC3));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getOC3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setOC3(double oc3) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getOC3().getPv()), oc3);
			} else {
				EPICS_CONTROLLER.caput(getChannel(OC3), oc3);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setOC3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOC3_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getOC3_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(OC3_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getOC3_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOC4() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getOC4().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(OC4));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getOC4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setOC4(double oc4) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getOC4().getPv()), oc4);
			} else {
				EPICS_CONTROLLER.caput(getChannel(OC4), oc4);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setOC4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOC4_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getOC4_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(OC4_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getOC4_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getFOffset() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getFOffset().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(FOffset));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getFOffset", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setFOffset(double foffset) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getFOffset().getPv()), foffset);
			} else {
				EPICS_CONTROLLER.caput(getChannel(FOffset), foffset);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setFOffset", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getFOffset_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getFOffset_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(FOffset_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getFOffset_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getFScale() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getFScale().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(FScale));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getFScale", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setFScale(double fscale) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getFScale().getPv()), fscale);
			} else {
				EPICS_CONTROLLER.caput(getChannel(FScale), fscale);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setFScale", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getFScale_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getFScale_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(FScale_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getFScale_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getFC1() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getFC1().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(FC1));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getFC1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setFC1(double fc1) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getFC1().getPv()), fc1);
			} else {
				EPICS_CONTROLLER.caput(getChannel(FC1), fc1);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setFC1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getFC1_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getFC1_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(FC1_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getFC1_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getFC2() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getFC2().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(FC2));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getFC2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setFC2(double fc2) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getFC2().getPv()), fc2);
			} else {
				EPICS_CONTROLLER.caput(getChannel(FC2), fc2);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setFC2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getFC2_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getFC2_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(FC2_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getFC2_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getFC3() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getFC3().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(FC3));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getFC3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setFC3(double fc3) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getFC3().getPv()), fc3);
			} else {
				EPICS_CONTROLLER.caput(getChannel(FC3), fc3);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setFC3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getFC3_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getFC3_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(FC3_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getFC3_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getFC4() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getFC4().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(FC4));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getFC4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setFC4(double fc4) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getFC4().getPv()), fc4);
			} else {
				EPICS_CONTROLLER.caput(getChannel(FC4), fc4);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setFC4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getFC4_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getFC4_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(FC4_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getFC4_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getROffset() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getROffset().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(ROffset));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getROffset", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setROffset(double roffset) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getROffset().getPv()), roffset);
			} else {
				EPICS_CONTROLLER.caput(getChannel(ROffset), roffset);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setROffset", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getROffset_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getROffset_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(ROffset_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getROffset_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getRC1() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getRC1().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(RC1));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getRC1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setRC1(double rc1) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getRC1().getPv()), rc1);
			} else {
				EPICS_CONTROLLER.caput(getChannel(RC1), rc1);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setRC1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getRC1_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getRC1_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(RC1_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getRC1_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getRC2() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getRC2().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(RC2));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getRC2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setRC2(double rc2) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getRC2().getPv()), rc2);
			} else {
				EPICS_CONTROLLER.caput(getChannel(RC2), rc2);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setRC2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getRC2_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getRC2_RBV().getPv()));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(RC2_RBV));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getRC2_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getFilterType() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getFilterType().getPv()));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(FilterType));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getFilterType", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setFilterType(int filtertype) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getFilterType().getPv()), filtertype);
			} else {
				EPICS_CONTROLLER.caput(getChannel(FilterType), filtertype);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setFilterType", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getFilterTypeSeq() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getFilterTypeSeq().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(FilterTypeSeq));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getFilterTypeSeq", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setFilterTypeSeq(int filtertypeseq) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getFilterTypeSeq().getPv()), filtertypeseq);
			} else {
				EPICS_CONTROLLER.caput(getChannel(FilterTypeSeq), filtertypeseq);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setFilterTypeSeq", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getRecursiveAveSeq() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getRecursiveAveSeq().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(RecursiveAveSeq));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getRecursiveAveSeq", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setRecursiveAveSeq(int recursiveaveseq) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getRecursiveAveSeq().getPv()), recursiveaveseq);
			} else {
				EPICS_CONTROLLER.caput(getChannel(RecursiveAveSeq), recursiveaveseq);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setRecursiveAveSeq", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getRecursiveSumSeq() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getRecursiveSumSeq().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(RecursiveSumSeq));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getRecursiveSumSeq", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setRecursiveSumSeq(int recursivesumseq) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getRecursiveSumSeq().getPv()), recursivesumseq);
			} else {
				EPICS_CONTROLLER.caput(getChannel(RecursiveSumSeq), recursivesumseq);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setRecursiveSumSeq", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getDifferenceSeq() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getDifferenceSeq().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(DifferenceSeq));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getDifferenceSeq", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setDifferenceSeq(int differenceseq) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getDifferenceSeq().getPv()), differenceseq);
			} else {
				EPICS_CONTROLLER.caput(getChannel(DifferenceSeq), differenceseq);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setDifferenceSeq", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getRecursiveAveDiffSeq() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getRecursiveAveDiffSeq().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(RecursiveAveDiffSeq));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getRecursiveAveDiffSeq", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setRecursiveAveDiffSeq(int recursiveavediffseq) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getRecursiveAveDiffSeq().getPv()), recursiveavediffseq);
			} else {
				EPICS_CONTROLLER.caput(getChannel(RecursiveAveDiffSeq), recursiveavediffseq);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setRecursiveAveDiffSeq", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getCopyToFilterSeq() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getCopyToFilterSeq().getPv()));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(CopyToFilterSeq));
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot getCopyToFilterSeq", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setCopyToFilterSeq(int copytofilterseq) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getCopyToFilterSeq().getPv()), copytofilterseq);
			} else {
				EPICS_CONTROLLER.caput(getChannel(CopyToFilterSeq), copytofilterseq);
			}
		} catch (Exception ex) {
			logger.warn("g.d.d.a.v.i.NDProcessImpl-> Cannot setCopyToFilterSeq", ex);
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
			logger.warn("g.d.d.a.v.i.NDProcessImpl -> Problem getting channel", exception);
			throw exception;
		}
	}

	public Channel createChannel(String fullPvName) throws CAException, TimeoutException {
		Channel channel = channelMap.get(fullPvName);
		if (channel == null) {
			try {
				channel = EPICS_CONTROLLER.createChannel(fullPvName);
			} catch (CAException cae) {
				logger.warn("g.d.d.a.v.i.NDProcessImpl-> Problem creating channel", cae);
				throw cae;
			} catch (TimeoutException te) {
				logger.warn("g.d.d.a.v.i.NDProcessImpl-> Problem creating channel", te);
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
		getPluginBase().reset();
		if (initialEnableBackground != null) {
			setEnableBackground(initialEnableBackground.shortValue());
		}
		if (initialEnableFlatField != null) {
			setEnableFlatField(initialEnableFlatField.shortValue());
		}
		if (initialEnableOffsetScale != null) {
			setEnableOffsetScale(initialEnableOffsetScale.shortValue());
		}
		if (initialEnableHighClip != null) {
			setEnableHighClip(initialEnableHighClip.shortValue());
		}
		if (initialEnableLowClip != null) {
			setEnableLowClip(initialEnableLowClip.shortValue());
		}
		if (initialEnableFilter != null) {
			setEnableFilter(initialEnableFilter.shortValue());
		}
	}

	@Override
	public void setFilterCallbacks(int filterCallback) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(FilterCallbacks), filterCallback);
	}

	@Override
	public int getFilterCallbacks() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(FilterCallbacks_RBV));
	}

	@Override
	public void setAutoResetFilter(int enable) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(AutoResetFilter), enable);
	}

	@Override
	public int getAutoResetFilter() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(AutoResetFilter_RBV));
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
	public Observable<Double> createScaleObservable() throws Exception {
		return LazyPVFactory.newReadOnlyDoublePV(getChannelName(Scale_RBV));
	}

	@Override
	public Observable<Double> createOffsetObservable() throws Exception {
		return LazyPVFactory.newReadOnlyDoublePV(getChannelName(Offset_RBV));
	}

}
