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

import gda.device.detector.areadetector.v17.NDProcess;
import gda.epics.LazyPVFactory;
import gda.epics.connection.EpicsController;
import gda.observable.Observable;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

public class NDProcessImpl extends NDBaseImpl implements InitializingBean, NDProcess {

	private final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	private String basePVName;

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

	static final Logger logger = LoggerFactory.getLogger(NDProcessImpl.class);

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
			EPICS_CONTROLLER.caput(getChannel(DataTypeOut), datatypeout);
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
			return EPICS_CONTROLLER.cagetEnum(getChannel(DataTypeOut_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getDataTypeOut_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getSaveBackground() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(SaveBackground));
		} catch (Exception ex) {
			logger.warn("Cannot getSaveBackground", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSaveBackground(int savebackground) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(SaveBackground), savebackground);
		} catch (Exception ex) {
			logger.warn("Cannot setSaveBackground", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getSaveBackground_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(SaveBackground_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSaveBackground_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getEnableBackground() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableBackground));
		} catch (Exception ex) {
			logger.warn("Cannot getEnableBackground", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setEnableBackground(int enablebackground) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(EnableBackground), enablebackground);
		} catch (Exception ex) {
			logger.warn("Cannot setEnableBackground", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getEnableBackground_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableBackground_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getEnableBackground_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getValidBackground_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ValidBackground_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getValidBackground_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getSaveFlatField() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(SaveFlatField));
		} catch (Exception ex) {
			logger.warn("Cannot getSaveFlatField", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setSaveFlatField(int saveflatfield) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(SaveFlatField), saveflatfield);
		} catch (Exception ex) {
			logger.warn("Cannot setSaveFlatField", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getSaveFlatField_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(SaveFlatField_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getSaveFlatField_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getEnableFlatField() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableFlatField));
		} catch (Exception ex) {
			logger.warn("Cannot getEnableFlatField", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setEnableFlatField(int enableflatfield) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(EnableFlatField), enableflatfield);
		} catch (Exception ex) {
			logger.warn("Cannot setEnableFlatField", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getEnableFlatField_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableFlatField_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getEnableFlatField_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getValidFlatField_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ValidFlatField_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getValidFlatField_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getScaleFlatField() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(ScaleFlatField));
		} catch (Exception ex) {
			logger.warn("Cannot getScaleFlatField", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setScaleFlatField(double scaleflatfield) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(ScaleFlatField), scaleflatfield);
		} catch (Exception ex) {
			logger.warn("Cannot setScaleFlatField", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getScaleFlatField_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(ScaleFlatField_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getScaleFlatField_RBV", ex);
			throw ex;
		}
	}

	@Override
	public short getAutoOffsetScale() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(AutoOffsetScale));
		} catch (Exception ex) {
			logger.warn("Cannot getAutoOffsetScale", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setAutoOffsetScale(int autooffsetscale) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(AutoOffsetScale), autooffsetscale);
		} catch (Exception ex) {
			logger.warn("Cannot setAutoOffsetScale", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getEnableOffsetScale() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableOffsetScale));
		} catch (Exception ex) {
			logger.warn("Cannot getEnableOffsetScale", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setEnableOffsetScale(int enableoffsetscale) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(EnableOffsetScale), enableoffsetscale);
		} catch (Exception ex) {
			logger.warn("Cannot setEnableOffsetScale", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getEnableOffsetScale_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableOffsetScale_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getEnableOffsetScale_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOffset() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(Offset));
		} catch (Exception ex) {
			logger.warn("Cannot getOffset", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setOffset(double offset) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(Offset), offset);
		} catch (Exception ex) {
			logger.warn("Cannot setOffset", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOffset_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(Offset_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getOffset_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getScale() throws Exception {
		try {
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
			EPICS_CONTROLLER.caput(getChannel(Scale), scale);
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
	public short getEnableLowClip() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableLowClip));
		} catch (Exception ex) {
			logger.warn("Cannot getEnableLowClip", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setEnableLowClip(int enablelowclip) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(EnableLowClip), enablelowclip);
		} catch (Exception ex) {
			logger.warn("Cannot setEnableLowClip", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getEnableLowClip_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableLowClip_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getEnableLowClip_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getLowClip() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(LowClip));
		} catch (Exception ex) {
			logger.warn("Cannot getLowClip", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setLowClip(double lowclip) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(LowClip), lowclip);
		} catch (Exception ex) {
			logger.warn("Cannot setLowClip", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getLowClip_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(LowClip_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getLowClip_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getEnableHighClip() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableHighClip));
		} catch (Exception ex) {
			logger.warn("Cannot getEnableHighClip", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setEnableHighClip(int enablehighclip) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(EnableHighClip), enablehighclip);
		} catch (Exception ex) {
			logger.warn("Cannot setEnableHighClip", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getEnableHighClip_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableHighClip_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getEnableHighClip_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getHighClip() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(HighClip));
		} catch (Exception ex) {
			logger.warn("Cannot getHighClip", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setHighClip(double highclip) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(HighClip), highclip);
		} catch (Exception ex) {
			logger.warn("Cannot setHighClip", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getHighClip_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(HighClip_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getHighClip_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getEnableFilter() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableFilter));
		} catch (Exception ex) {
			logger.warn("Cannot getEnableFilter", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setEnableFilter(int enablefilter) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(EnableFilter), enablefilter);
		} catch (Exception ex) {
			logger.warn("Cannot setEnableFilter", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getEnableFilter_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(EnableFilter_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getEnableFilter_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getResetFilter() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ResetFilter));
		} catch (Exception ex) {
			logger.warn("Cannot getResetFilter", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setResetFilter(int resetfilter) throws Exception {
		try {
			EPICS_CONTROLLER.caputWait(getChannel(ResetFilter), resetfilter);
		} catch (Exception ex) {
			logger.warn("Cannot setResetFilter", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getResetFilter_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ResetFilter_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getResetFilter_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getNumFilter() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(NumFilter));
		} catch (Exception ex) {
			logger.warn("Cannot getNumFilter", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setNumFilter(int numfilter) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(NumFilter), numfilter);
		} catch (Exception ex) {
			logger.warn("Cannot setNumFilter", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getNumFilter_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(NumFilter_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getNumFilter_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getNumFilterRecip() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(NumFilterRecip));
		} catch (Exception ex) {
			logger.warn("Cannot getNumFilterRecip", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setNumFilterRecip(int numfilterrecip) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(NumFilterRecip), numfilterrecip);
		} catch (Exception ex) {
			logger.warn("Cannot setNumFilterRecip", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getNumFiltered_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(NumFiltered_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getNumFiltered_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOOffset() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(OOffset));
		} catch (Exception ex) {
			logger.warn("Cannot getOOffset", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setOOffset(double ooffset) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(OOffset), ooffset);
		} catch (Exception ex) {
			logger.warn("Cannot setOOffset", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOOffset_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(OOffset_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getOOffset_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOScale() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(OScale));
		} catch (Exception ex) {
			logger.warn("Cannot getOScale", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setOScale(double oscale) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(OScale), oscale);
		} catch (Exception ex) {
			logger.warn("Cannot setOScale", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOScale_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(OScale_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getOScale_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOC1() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(OC1));
		} catch (Exception ex) {
			logger.warn("Cannot getOC1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setOC1(double oc1) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(OC1), oc1);
		} catch (Exception ex) {
			logger.warn("Cannot setOC1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOC1_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(OC1_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getOC1_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOC2() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(OC2));
		} catch (Exception ex) {
			logger.warn("Cannot getOC2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setOC2(double oc2) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(OC2), oc2);
		} catch (Exception ex) {
			logger.warn("Cannot setOC2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOC2_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(OC2_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getOC2_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOC3() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(OC3));
		} catch (Exception ex) {
			logger.warn("Cannot getOC3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setOC3(double oc3) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(OC3), oc3);
		} catch (Exception ex) {
			logger.warn("Cannot setOC3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOC3_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(OC3_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getOC3_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOC4() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(OC4));
		} catch (Exception ex) {
			logger.warn("Cannot getOC4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setOC4(double oc4) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(OC4), oc4);
		} catch (Exception ex) {
			logger.warn("Cannot setOC4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getOC4_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(OC4_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getOC4_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getFOffset() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(FOffset));
		} catch (Exception ex) {
			logger.warn("Cannot getFOffset", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setFOffset(double foffset) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(FOffset), foffset);
		} catch (Exception ex) {
			logger.warn("Cannot setFOffset", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getFOffset_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(FOffset_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getFOffset_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getFScale() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(FScale));
		} catch (Exception ex) {
			logger.warn("Cannot getFScale", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setFScale(double fscale) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(FScale), fscale);
		} catch (Exception ex) {
			logger.warn("Cannot setFScale", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getFScale_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(FScale_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getFScale_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getFC1() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(FC1));
		} catch (Exception ex) {
			logger.warn("Cannot getFC1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setFC1(double fc1) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(FC1), fc1);
		} catch (Exception ex) {
			logger.warn("Cannot setFC1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getFC1_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(FC1_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getFC1_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getFC2() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(FC2));
		} catch (Exception ex) {
			logger.warn("Cannot getFC2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setFC2(double fc2) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(FC2), fc2);
		} catch (Exception ex) {
			logger.warn("Cannot setFC2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getFC2_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(FC2_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getFC2_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getFC3() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(FC3));
		} catch (Exception ex) {
			logger.warn("Cannot getFC3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setFC3(double fc3) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(FC3), fc3);
		} catch (Exception ex) {
			logger.warn("Cannot setFC3", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getFC3_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(FC3_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getFC3_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getFC4() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(FC4));
		} catch (Exception ex) {
			logger.warn("Cannot getFC4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setFC4(double fc4) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(FC4), fc4);
		} catch (Exception ex) {
			logger.warn("Cannot setFC4", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getFC4_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(FC4_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getFC4_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getROffset() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(ROffset));
		} catch (Exception ex) {
			logger.warn("Cannot getROffset", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setROffset(double roffset) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(ROffset), roffset);
		} catch (Exception ex) {
			logger.warn("Cannot setROffset", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getROffset_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(ROffset_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getROffset_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getRC1() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(RC1));
		} catch (Exception ex) {
			logger.warn("Cannot getRC1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setRC1(double rc1) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(RC1), rc1);
		} catch (Exception ex) {
			logger.warn("Cannot setRC1", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getRC1_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(RC1_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getRC1_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getRC2() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(RC2));
		} catch (Exception ex) {
			logger.warn("Cannot getRC2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setRC2(double rc2) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(RC2), rc2);
		} catch (Exception ex) {
			logger.warn("Cannot setRC2", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getRC2_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(RC2_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getRC2_RBV", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public short getFilterType() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(FilterType));
		} catch (Exception ex) {
			logger.warn("Cannot getFilterType", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setFilterType(int filtertype) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(FilterType), filtertype);
		} catch (Exception ex) {
			logger.warn("Cannot setFilterType", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getFilterTypeSeq() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(FilterTypeSeq));
		} catch (Exception ex) {
			logger.warn("Cannot getFilterTypeSeq", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setFilterTypeSeq(int filtertypeseq) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(FilterTypeSeq), filtertypeseq);
		} catch (Exception ex) {
			logger.warn("Cannot setFilterTypeSeq", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getRecursiveAveSeq() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(RecursiveAveSeq));
		} catch (Exception ex) {
			logger.warn("Cannot getRecursiveAveSeq", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setRecursiveAveSeq(int recursiveaveseq) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(RecursiveAveSeq), recursiveaveseq);
		} catch (Exception ex) {
			logger.warn("Cannot setRecursiveAveSeq", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getRecursiveSumSeq() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(RecursiveSumSeq));
		} catch (Exception ex) {
			logger.warn("Cannot getRecursiveSumSeq", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setRecursiveSumSeq(int recursivesumseq) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(RecursiveSumSeq), recursivesumseq);
		} catch (Exception ex) {
			logger.warn("Cannot setRecursiveSumSeq", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getDifferenceSeq() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(DifferenceSeq));
		} catch (Exception ex) {
			logger.warn("Cannot getDifferenceSeq", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setDifferenceSeq(int differenceseq) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(DifferenceSeq), differenceseq);
		} catch (Exception ex) {
			logger.warn("Cannot setDifferenceSeq", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getRecursiveAveDiffSeq() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(RecursiveAveDiffSeq));
		} catch (Exception ex) {
			logger.warn("Cannot getRecursiveAveDiffSeq", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setRecursiveAveDiffSeq(int recursiveavediffseq) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(RecursiveAveDiffSeq), recursiveavediffseq);
		} catch (Exception ex) {
			logger.warn("Cannot setRecursiveAveDiffSeq", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getCopyToFilterSeq() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(CopyToFilterSeq));
		} catch (Exception ex) {
			logger.warn("Cannot getCopyToFilterSeq", ex);
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public void setCopyToFilterSeq(int copytofilterseq) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(CopyToFilterSeq), copytofilterseq);
		} catch (Exception ex) {
			logger.warn("Cannot setCopyToFilterSeq", ex);
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
		if (basePVName == null) {
			throw new IllegalArgumentException("'basePVName' needs to be declared");
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

			return createChannel(basePVName + pvPostFix);
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

	private String getChannelName(String pvElementName, String... args) {
		String pvPostFix = null;
		if (args.length > 0) {
			// PV element name is different from the pvPostFix
			pvPostFix = args[0];
		} else {
			pvPostFix = pvElementName;
		}

		return basePVName + pvPostFix;
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
