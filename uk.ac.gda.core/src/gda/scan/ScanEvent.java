/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.scan;

import gda.scan.Scan.ScanStatus;

import java.io.Serializable;

/**
 * The latest information about the current scan, to be broadcast via JythonServer (i.e. InterfaceProvider) to
 * registered IObservers.
 */
public class ScanEvent implements Serializable {

	/**
	 * The event type gives the reason for the event being sent. Use the latestStatus attribute to get more details.
	 */
	public enum EventType {
		STARTED("started"), UPDATED("updated"), FINISHED("finished");

		private final String string;

		private EventType(final String text) {
			this.string = text;
		}

		@Override
		public String toString() {
			return string;
		}
	}

	private ScanInformation latestInformation;
	private ScanStatus latestStatus;
	private int currentPointNumber;
	private EventType type;
	
	public ScanEvent(EventType type, ScanInformation latestInformation, ScanStatus latestStatus, int currentPointNumber) {
		super();
		this.type = type;								// why are we sending this event
		this.latestInformation = latestInformation;		// static info about the scan
		this.latestStatus = latestStatus;				// current status
		this.currentPointNumber = currentPointNumber;	// current point number
	}

	public ScanInformation getLatestInformation() {
		return latestInformation;
	}

	public ScanStatus getLatestStatus() {
		return latestStatus;
	}

	public int getCurrentPointNumber() {
		return currentPointNumber;
	}
	
	public EventType getType() {
		return type;
	}
}
