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
 * This object is sent when a UDC client does not enter the event loop,
 * because the preconditions were not initially met.
 */
public class UDCFailedToStartEvent extends EventObject {

	private final String explanation;

	public UDCFailedToStartEvent(Object source, String preconditionsMessage) {
		super(source);
		explanation = preconditionsMessage;
	}

	@Override
	public String toString() {
		return explanation;
	}
}
