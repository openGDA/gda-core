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

package gda.jython.scriptcontroller;

import java.io.Serializable;

/**
 * Serializable object which is passed to IObservers of scripts to be notified of script progress
 */
public class ScriptEvent implements Serializable {
	/**
	 * Enumeration of the type of event described
	 */
	public enum Event {
		
		/**
		 * Script has started
		 */
		Start,
		
		/**
		 * Script has finished successfully
		 */
		End,
		
		/**
		 * Script has ended with an exception
		 */
		Exception
	}

	/**
	 * Unique identifier of the script.
	 */
	final public String id;

	/**
	 * the type of event
	 */
	final public Event event;
	
	/**
	 * some further information for the script Observers
	 */
	final public Object data;

	public ScriptEvent(String id, Event event, Object data) {
		this.id = id;
		this.event = event;
		this.data = data;
	}

	public boolean isStart() {
		return event == Event.Start;
	}

	public boolean isEnd() {
		return event == Event.End;
	}

	public boolean isException() {
		return event == Event.Exception;
	}

	public String getExceptionString() {
		if (isException()) {
			if (data instanceof Exception) {
				return ((Exception) data).getMessage();
			} else if (data instanceof String) {
				return (String) data;
			}
			return "Exception not specified.";
		}
		return null;
	}
	
	@Override
	public String toString() {
		return String.format("ScriptEvent[id=%s, event=%s, data=%s]", id, event, data);
	}
}
