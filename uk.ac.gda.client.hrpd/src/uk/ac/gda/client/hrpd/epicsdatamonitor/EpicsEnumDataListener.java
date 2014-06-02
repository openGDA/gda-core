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

package uk.ac.gda.client.hrpd.epicsdatamonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
/** 
 * A named Spring-configurable {@link MonitorListener} for an EPICS PV of type {@link DBRType#DOUBLE}.
 * This listener stores a double data array which updated via {@link MonitorEvent} from the EPICS PV by default,
 * unless its {@link #poll} property is set to true, in which case, it will poll data from EPICS PV every time 
 * when {@link #getValue()} method is called.
 * <li>{@link #name} and {@link #pvName} must be specified for an instance.</li>
 * <li>{@link #disablePoll()} and {@link #enablePoll()} can be used to switch monitoring on and off dynamically.</li>
 * <li>The default mode is monitoring on.</li>
 * 
 */
public class EpicsEnumDataListener extends EpicsPVListener {
	private short value;
	private Logger logger=LoggerFactory.getLogger(EpicsEnumDataListener.class);
	private String[] positions;
	@Override
	public void disablePoll() {
		if (pvchannel != null) {
			try {
				pvmonitor = pvchannel.addMonitor(DBRType.DOUBLE, pvchannel.getElementCount(), Monitor.VALUE, this);
				setPoll(false);
			} catch (IllegalStateException e) {
				logger.error("Fail to add monitor to channel " + pvchannel.getName(), e);
			} catch (CAException e) {
				logger.error("Fail to add monitor to channel " + pvchannel.getName(), e);
			}
		}
	}

	@Override
	public Short getValue() {
		if (isPoll()) {
			try {
				return controller.cagetEnum(pvchannel);
			} catch (TimeoutException | CAException | InterruptedException e) {
				logger.error(getName() + ": failed to get values from PV " + pvchannel.getName(), e);
			}
		}
		return this.value;
	}

	@Override
	public void monitorChanged(MonitorEvent ev) {
		Channel ch = (Channel) ev.getSource();
		if (first) {
			first = false;
			logger.debug("Data listener is added to channel {}.", ch.getName());
		}
		DBR dbr = ev.getDBR();
		if (dbr.isENUM()) {
			value = ((DBR_Enum) dbr).getEnumValue()[0];
			if (observers.IsBeingObserved()) {
				observers.notifyIObservers(this, Short.valueOf(value));
			}
		}
	}
	/**
	 * return current enum position name in String.
	 * @return current position name
	 */
	public String getPosition() {
		return positions[getValue()];
	}
	
	private String[] getEnumPositions() throws DeviceException {
		try {
			return controller.cagetLabels(pvchannel);
		} catch (TimeoutException | CAException | InterruptedException e) {
			logger.error(getName()+": failed to initialise Enum Positions from "+pvchannel.getName(), e);
			throw new DeviceException(getName()+": failed to initialise Enum Positions from "+pvchannel.getName(), e);
		}
	}
	public String[] getPositions() {
		return this.positions;
	}
	@Override
	public void initializationCompleted() throws InterruptedException, DeviceException, TimeoutException, CAException {
		positions=getEnumPositions();
		super.initializationCompleted();
	}

}
