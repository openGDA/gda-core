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

package gda.device.epicsdevice;

import gda.device.DeviceException;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.device.scannable.ScannableUtils;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * An object that provides read/write access to an EPICS DOUBLE.
 */
@CorbaImplClass(ScannableImpl.class)
@CorbaAdapterClass(ScannableAdapter.class)
public class EpicsDouble extends ScannableBase implements Configurable, InitializationListener {
	
	private static final Logger logger = LoggerFactory.getLogger(EpicsDouble.class);
	
	private String pvName;
	
	private EpicsChannelManager channelManager;
	
	private EpicsController controller;
	
	private Channel valChannel;
	
	private MonitorListener valMonitor;
	
	private double value;

	/**
	 * Creates a new EpicsDouble.
	 */
	public EpicsDouble() {
		channelManager = new EpicsChannelManager(this);
		controller = EpicsController.getInstance();
		valMonitor = new ValMonitorListener();
	}
	
	/**
	 * Sets the PV name that this object will connect to.
	 * 
	 * @param pvName the PV name
	 */
	public void setPvName(String pvName) {
		this.pvName = pvName;
	}
	
	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			createChannels();
			channelManager.tryInitialize(100);
			configured = true;
		}
	}
	
	private void createChannels() throws FactoryException {
		try {
			valChannel = channelManager.createChannel(pvName + ".VAL", valMonitor, false);
			channelManager.creationPhaseCompleted();
		} catch (Throwable t) {
			throw new FactoryException("Could not connect channels for " + StringUtils.quote(getName()));
		}
	}

	@Override
	public void initializationCompleted() {
		logger.info(getClass().getSimpleName() + " " + StringUtils.quote(getName()) + " is connected");
	}
	
	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		final double target = ScannableUtils.objectToArray(position)[0];
		setValue(target);
	}
	
	private void setValue(double d) throws DeviceException {
		try {
			controller.caput(valChannel, d);
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in setValue", e);
		}
	}

	@Override
	public Object getPosition() throws DeviceException {
		return value;
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	private class ValMonitorListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent event) {
			DBR dbr = event.getDBR();
			if (dbr.isDOUBLE()) {
				final DBR_Double d = (DBR_Double) dbr;
				value = d.getDoubleValue()[0];
				notifyIObservers(this, new ScannablePositionChangeEvent(value));
			}
		}
	}
	
}
