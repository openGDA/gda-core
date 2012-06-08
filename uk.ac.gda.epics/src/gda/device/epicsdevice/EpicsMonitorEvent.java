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

import gov.aps.jca.CAStatus;
import gov.aps.jca.event.MonitorEvent;

import java.io.Serializable;

/**
 * EpicsMonitorEvent Class
 */
public class EpicsMonitorEvent implements Serializable {
	final static long serialVersionUID = 1;
	/**
	 * 
	 */
	final public Object caStatus;
	/**
	 * 
	 */
	final public Object epicsDbr;

	EpicsMonitorEvent(MonitorEvent event, boolean toWrap) {
		if (toWrap) {
			CAStatus _caStatus = event.getStatus();
			caStatus = _caStatus != null ? new EpicsCAStatus(_caStatus) : null;
			epicsDbr = EpicsDevice.WrapEpicsDBR(event.getDBR());
		} else {
			caStatus = event.getStatus();
			epicsDbr = event.getDBR();
		}
	}

	EpicsMonitorEvent(Object object) // used for dummy events
	{
		caStatus = null;
		epicsDbr = object;
	}
}
