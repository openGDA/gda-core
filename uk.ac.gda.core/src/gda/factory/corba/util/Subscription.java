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

import gda.factory.corba.util.Filter.ACCEPTANCE;

/**
 * An holder class that contains the EventSubscriber and filter.
 */
final class Subscription {

	private final EventSubscriber subscriber;
	private final Filter filter;

	/**
	 * Create the subscription holder
	 *
	 * @param subscriber
	 *            the subscriber to the event channel
	 * @param filter
	 *            the filter to be used
	 */
	public Subscription(EventSubscriber subscriber, Filter filter) {
		if( filter == null)
			throw new IllegalArgumentException("filter is null");
		this.subscriber = subscriber;
		this.filter = filter;
	}


	/**
	 * Filter incoming event and inform the subscriber.
	 *
	 * @param event
	 *            the incoming event
	 * @return Type to indicate whether the event is to be processed by the object associated with this filer
	 */
	public ACCEPTANCE accept(TimedStructuredEvent event) {
		ACCEPTANCE processed = ACCEPTANCE.NOT;
		if (event != null) {
			processed = filter.apply(event);
			if(processed != ACCEPTANCE.NOT){
				Object payload = event.getPayload();
				if(payload instanceof EventCollection){
					for( Object obj : ((EventCollection)payload)){
						subscriber.inform(obj);
					}
				} else {
					subscriber.inform(payload);
				}
			}
		}
		return processed;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((filter == null) ? 0 : filter.hashCode());
		result = prime * result + ((subscriber == null) ? 0 : subscriber.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Subscription other = (Subscription) obj;
		if (filter == null) {
			if (other.filter != null)
				return false;
		} else if (!filter.equals(other.filter))
			return false;
		if (subscriber == null) {
			if (other.subscriber != null)
				return false;
		} else if (!subscriber.equals(other.subscriber))
			return false;
		return true;
	}

}
