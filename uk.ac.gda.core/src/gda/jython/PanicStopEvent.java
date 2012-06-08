/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
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

import java.io.Serializable;
import java.util.EventObject;

/**
 * This object is sent as an event when the panic stop button is pressed.
 */
public class PanicStopEvent extends EventObject implements Serializable {

	public PanicStopEvent(Object source) {
		super(source);
	}

	public PanicStopEvent() {
		// Could send more complex serilizable here but leave
		// as simple lightweight string for now.
		this("Panic Stop Pressed");
	}
	
	@Override
	public String toString() {
		return getClass().getName();
	}
}
