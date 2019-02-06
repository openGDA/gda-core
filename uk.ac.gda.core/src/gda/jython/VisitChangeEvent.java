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
 * This object is sent as an event when a client wants to change visit
 */
public class VisitChangeEvent extends EventObject {

	private String visit;

	public VisitChangeEvent(Object source, String visit) {
		super(source);
		setVisit(visit);
	}

	public String getVisit() {
		return visit;
	}
	public void setVisit(String visit) {
		this.visit = visit;
	}

	@Override
	public String toString() {
		return getClass().getName();
	}
}
