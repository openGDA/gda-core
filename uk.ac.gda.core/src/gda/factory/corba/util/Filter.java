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

package gda.factory.corba.util;

/**
 * A class can implement the Filter interface in order to filter events to a Subscriber
 */
public interface Filter {
	/**
	 * Type to indicate whether the event is to be processed by the object associated with this filter
	 */
	enum ACCEPTANCE {
	/**
	 * Event is not to be processed by this filter
	 */
	NOT, 
	/**
	 *  Event is to be processed by this filter only
	 */
	EXCLUSIVE}
	/**
	 * Apply the filter to the event
	 * 
	 * @param event
	 *            the event to be tested
	 * @return true if the event is desired, false otherwise
	 */
	public ACCEPTANCE apply(TimedStructuredEvent event);
}
