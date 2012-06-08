/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import gda.factory.corba.EventHeader;
import gda.factory.corba.StructuredEvent;
import gda.factory.corba.StructuredEventHelper;
import gda.util.Serializer;

class TimedStructuredEvent {
	
	private long timeReceivedMs;
	private StructuredEvent event;
	private Object payload;

	public TimedStructuredEvent(TimedAny event) {
		this.timeReceivedMs = event.timeReceivedMs;
		this.event = StructuredEventHelper.extract(event.event);
	}
	

	public EventHeader getHeader() {
		return event.eventHeader;
	}
	
	public synchronized Object getPayload() {
		if (payload == null) {
			payload = Serializer.toObject(event.byteData);
		}
		return payload;
	}
	
	public long getTimeReceivedMs() {
		return timeReceivedMs;
	}

	public StructuredEvent getEvent() {
		return event;
	}

	@Override
	public String toString() {
		return String.format("TimedStructuredEvent(time=%d, source=%s, type=%s)", timeReceivedMs, getHeader().eventName, getHeader().typeName);
	}

}