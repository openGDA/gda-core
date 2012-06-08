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

import java.io.Serializable;

/**
 * EpicsCAStatus Class
 */
public class EpicsCAStatus implements Serializable {
	final static long serialVersionUID = 1;
	final String msg, name, severity_name;
	final int severity_value, value;

	EpicsCAStatus(CAStatus status) {
		if (status != null) {
			msg = status.getMessage();
			name = status.getName();
			severity_value = status.getSeverity().getValue();
			severity_name = status.getSeverity().getName();
			value = status.getValue();
		} else {
			msg = "Unknown";
			name = "Unknown";
			severity_value = 0;
			severity_name = "Unknown";
			value = 0;
		}

	}

}
