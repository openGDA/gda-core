/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

import gda.epics.connection.EpicsController;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 *
 */
public class XBufferEPICsPlugin implements InitializingBean {
	protected final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();
	private String basePVName;
	static final Logger logger = LoggerFactory.getLogger(XBufferEPICsPlugin.class);
	
	public enum EnableCallbacks {
		DISABLE, ENABLE
	}
	/**
	 * 
	 */
	public XBufferEPICsPlugin() {
		
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
	
	
	public Channel createChannel(String fullPvName) throws CAException, TimeoutException {
		Channel channel;
		try {
			channel = EPICS_CONTROLLER.createChannel(fullPvName);
		} catch (CAException cae) {
			logger.warn("g.d.d.a.v.i.XBufferEPICsPlugin-> Problem creating channel", cae);
			throw cae;
		} catch (TimeoutException te) {
			logger.warn("g.d.d.a.v.i.XBufferEPICsPlugin-> Problem creating channel", te);
			throw te;
		}
		return channel;
	}
	
	protected Channel getChannel() throws Exception {
		try {
			String fullPvName = basePVName+"EnableCallbacks_RBV";
			return createChannel(fullPvName);
		} catch (Exception exception) {
			logger.warn("Problem getting channel", exception);
			throw exception;
		}
	}
	
	public int getEnableCallbacks_RBV() throws Exception{
		try{
			return EPICS_CONTROLLER.cagetEnum(getChannel());
		} catch (Exception ex){
			logger.warn("g.d.d.a.v.i.XBufferEPICsPlugin-> Cannot get EnableCallbacks_RBV",ex);
			throw ex;
		}
	}
	
	public void setEnableCallbacks_RBV(int enableCallbacks) throws Exception{
		try{
			EPICS_CONTROLLER.caput(getChannel(),enableCallbacks);
		} catch (Exception ex){
			logger.warn("g.d.d.a.v.i.XBufferEPICsPlugin-> Cannot get EnableCallbacks_RBV",ex);
			throw ex;
		}
	}
		
	@Override
	public void afterPropertiesSet() throws Exception {
		if (basePVName == null) {
			throw new IllegalArgumentException("'basePVName'needs to be declared");
		}
	}

}
