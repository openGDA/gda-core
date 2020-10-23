/*-
 * Copyright © 2018 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.jython;

import java.util.EventObject;

/**
 * This object is sent when a UDC client never enters the event loop as the conditions are not initially met.
 */
public class UDCNeverStartedEvent extends EventObject {

	public UDCNeverStartedEvent(Object source) {
		super(source);
	}

	@Override
	public String toString() {
		return getClass().getName();
	}
}
