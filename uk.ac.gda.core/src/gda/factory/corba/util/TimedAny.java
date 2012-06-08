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

import gda.factory.corba.StructuredEventHelper;

import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;

class TimedAny {
	
	public Any event;
	
	public long timeReceivedMs;
	
	public TimedAny(Any event,long timeReceivedMS){
		this.event = event;
		this.timeReceivedMs = timeReceivedMS;
	}
	
	public boolean isStructuredEvent() {
		TypeCode typeCode = event.type();
		return typeCode != null && typeCode.equivalent(StructuredEventHelper.type());
	}
	
	@Override
	public String toString() {
		return String.format("TimedAny(time=%d)", timeReceivedMs);
	}
	
}