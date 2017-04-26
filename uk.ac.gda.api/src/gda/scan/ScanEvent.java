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

import java.io.File;
import java.io.Serializable;

import gda.scan.Scan.ScanStatus;

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

	private final ScanInformation latestInformation;
	private final ScanStatus latestStatus;
	private final int currentPointNumber;
	private final EventType type;

	public ScanEvent(EventType type, ScanInformation latestInformation, ScanStatus latestStatus,
			int currentPointNumber) {
		this.type = type; // why are we sending this event
		this.latestInformation = latestInformation; // static info about the scan
		this.latestStatus = latestStatus; // current status
		this.currentPointNumber = currentPointNumber; // current point number
	}

	@Override
	public String toString() {
		return type.toString() + " event from scan " + latestInformation.getScanNumber();
	}

	/**
	 * For the ApplicationActionBarAdvisor
	 *
	 * @return String
	 */
	public String toProgressString() {
		final String output;
		switch (latestStatus) {
		case COMPLETED_AFTER_FAILURE:
			output = getScanCompletedMessagePrefix() + "failed.";
			break;
		case COMPLETED_AFTER_STOP:
			output = getScanCompletedMessagePrefix() + " was aborted.";
			break;
		case COMPLETED_EARLY:
			output = getScanCompletedMessagePrefix() + " was fast forwarded.";
			break;
		case COMPLETED_OKAY:
			output = getScanCompletedMessagePrefix() + " complete.";
			break;
		case FINISHING_EARLY:
			output = addScanRunningOutput() + " FINISHING";
			break;
		case PAUSED:
			output = addScanRunningOutput() + " PAUSED";
			break;
		case TIDYING_UP_AFTER_FAILURE:
			output = addScanRunningOutput() + " ERROR";
			break;
		case TIDYING_UP_AFTER_STOP:
			output = addScanRunningOutput() + " ABORTING";
			break;
		default:
			output = addScanRunningOutput();
			break;
		}
		return output;
	}

	private String getScanCompletedMessagePrefix() {
		String output = "No scan running. ";
		String fileName = latestInformation.getFilename();
		if (fileName != null && !fileName.isEmpty()) {
			File file = new File(fileName);
			output += "Last scan '" + file.getName() + "'";
		}
		return output;
	}

	private String addScanRunningOutput() {
		String output = "Scan ";
		String fileName = latestInformation.getFilename();
		if (fileName != null && !fileName.isEmpty()) {
			File file = new File(fileName);
			output += "'" + file.getName() + "' ";
		}
		output += "running (" + (currentPointNumber + 1) + "/" + latestInformation.getNumberOfPoints() + ")";
		output = addDimensionToProgressString(output);
		return output;
	}

	private String addDimensionToProgressString(String output) {
		if (latestInformation.getDimensions().length > 1) {
			output += " " + latestInformation.getDimensions().length + "D";
		}
		return output;
	}

	/**
	 * For the Command Queue progress bar
	 *
	 * @return String
	 */
	public String toShortProgressString() {
		return "Scan " + latestInformation.getScanNumber() + " " + latestStatus.toString();
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
