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

package gda.device.enumpositioner;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.epics.connection.EpicsController.MonitorType;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EpicsBinaryFromBits extends EnumPositionerBase implements EnumPositioner, InitializationListener, MonitorListener {

	private static final Logger logger = LoggerFactory.getLogger(EpicsBinaryFromBits.class);

	private String recordName;
	
	private String[] positionNames;
	
	private int bitToCheck;
	
	private EpicsController controller;

	private EpicsChannelManager channelManager;
	private Channel controlChnl;
	
	/**
	 * Constructor
	 */
	public EpicsBinaryFromBits() {
		super();
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
	}
	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			if (recordName != null) {
				createChannelAccess(recordName);
				channelManager.tryInitialize(100);
			}
		}
		configured = true;
	}
	
	private void createChannelAccess(String pv) throws FactoryException {
		try {
			controlChnl = channelManager.createChannel(pv, this,MonitorType.STS, false);
			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();

		} catch (Throwable th) {
			throw new FactoryException("failed to connect to all channels", th);
		}
	}
	
	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		logger.error("this class does not implement a moveTo method");
	}
	
	/**
	 * @see gda.device.Scannable#getPosition()
	 */
	@Override
	public Object getPosition() throws DeviceException {
		checkConfigured();
		int intValue;
		try {
			intValue = controller.cagetInt(controlChnl);
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(), e);
		}
		
		//TODO the procedure below is not optimal
		String intString = String.format("%16d",Integer.valueOf(Integer.toBinaryString(intValue)));
		assert(intString.length()>=bitToCheck);
		//in a string, the first character corresponds to the highest bit - need to check from end
		boolean returnIsTrue = intString.charAt(intString.length() - bitToCheck - 1)=='1';
		if (returnIsTrue) {
			return positionNames[1];
		}
		return positionNames[0];

	}
	
	void checkConfigured() throws DeviceException {
		if (!configured)
			throw new DeviceException(getName() + " is not yet configured");
	}
	public String getRecordName() {
		return recordName;
	}


	public void setRecordName(String recordName) {
		this.recordName = recordName;
	}


	public String[] getPositionNames() {
		return positionNames;
	}


	public void setPositionNames(String[] positionNames) {
		this.positionNames = positionNames;
	}


	public int getBitToCheck() {
		return bitToCheck;
	}
	public void setBitToCheck(int bitToCheck) {
		this.bitToCheck = bitToCheck;
	}
	@Override
	public void initializationCompleted() throws InterruptedException, DeviceException, TimeoutException, CAException {
		// TODO Auto-generated method stub
		
	}
	/**
	 * @see gov.aps.jca.event.MonitorListener#monitorChanged(gov.aps.jca.event.MonitorEvent)
	 */
	@Override
	public void monitorChanged(MonitorEvent arg0) {
		DBR dbr = arg0.getDBR();
		if (dbr.isINT()) {
			int dmovValue = ((DBR_Int) dbr).getIntValue()[0];
			this.notifyIObservers(this, dmovValue);
		} else {
			logger.error("error with MonitorEvent from" + recordName + "should return INT type value.");
		}
	}
	
}