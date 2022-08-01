/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.server.services.monitor;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableStatus;
import uk.ac.diamond.daq.jms.monitor.Monitor;
import uk.ac.diamond.daq.jms.monitor.MonitorStatus;

public class MonitorAdapter {

	public Monitor createMonitor(Scannable scannable) throws MonitorServiceException  {
		var monitor = new Monitor(scannable.getName(),"No Units");
			monitor.setValue(getValue(scannable));
			return monitor;

	}

	public String getValue(Scannable scannable) throws MonitorServiceException {
		try {
			return scannable.getPosition().toString();
		} catch (DeviceException e) {
			throw new MonitorServiceException("Could not get monitor value for " + scannable.getName(), e);
		}
	}

	public MonitorStatus convertStatus(Scannable scannable, Object event) {
		if (event instanceof ScannableStatus) {
			ScannableStatus status = (ScannableStatus) event;
			if (status == ScannableStatus.BUSY) {
				return MonitorStatus.AVAILABLE;
			} else if (status == ScannableStatus.IDLE) {
				return MonitorStatus.AVAILABLE;
			} else if (status == ScannableStatus.FAULT) {
				return MonitorStatus.ERROR;
			}
		}
		return MonitorStatus.UNKNOWN;
	}

	public MonitorStatus getStatus(Scannable scannable)  {
		try {
		scannable.getPosition();
		} catch (DeviceException e) {
			return MonitorStatus.ERROR;
		}
		return MonitorStatus.AVAILABLE;
	}

}
