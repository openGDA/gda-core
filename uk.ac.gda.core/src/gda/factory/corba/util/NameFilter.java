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

import gda.configuration.properties.LocalProperties;
import gda.observable.IIsBeingObserved;

/**
 * NameFilter Class
 */
public final class NameFilter implements Filter {
	
	private final String shortName;
	
	private final String name;

	private static String eventChannelName = LocalProperties.get("gda.eventChannelName", "local.eventChannel");

	private final IIsBeingObserved isBeingObserved;

	/**
	 * @param shortName
	 * @param isBeingObserved
	 */
	public NameFilter(String shortName, IIsBeingObserved isBeingObserved) {

		if (shortName == null || shortName.length() == 0)
			throw new IllegalArgumentException("FilterBase.FilterBase: Error - name is null or zero length");
		this.shortName = shortName;
		this.name = MakeEventChannelName(shortName);
		this.isBeingObserved = isBeingObserved;
	}

	@Override
	public ACCEPTANCE apply(TimedStructuredEvent event) {
		if (isBeingObserved != null && !isBeingObserved.IsBeingObserved())
			return ACCEPTANCE.NOT;
		final String eventName = event.getHeader().eventName;
		return (eventName != null) && name.equals(eventName) ? ACCEPTANCE.EXCLUSIVE : ACCEPTANCE.NOT;
	}

	/**
	 * Returns the object name used by this name filter.
	 * 
	 * @return the object name
	 */
	public String getName() {
		return shortName;
	}
	
	/**
	 * @param shortName
	 * @return short name plus event channel name
	 */
	static public String MakeEventChannelName(String shortName) {
		return shortName + "." + eventChannelName;
	}

	/**
	 * @return event channel name
	 */
	static public String getEventChannelName() {
		return eventChannelName;
	}
	
	@Override
	public String toString() {
		return String.format("NameFilter(name=%s)", getName());
	}

}
