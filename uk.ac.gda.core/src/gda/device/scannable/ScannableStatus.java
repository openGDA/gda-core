/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.device.scannable;

import java.io.Serializable;

/**
 * An object used by Scannables to inform objects IObserving them that the status has changed.
 * <p>
 * To ensure the correct status objects are sent out by Scannables, this status class must not be used by any Scannable
 * abstract base classes, but should only be used by concrete Scabnbnable classes wishing to be IObserved.
 */
public class ScannableStatus implements Serializable {

	/**
	 * The Scannable is not operating. A call to isBusy would return false;
	 */
	public static int IDLE = 0;

	/**
	 * The Scannable is operating by having its asynchronousMoveTo method being called (or one of the methods which
	 * indirectly call it). A call to isBusy would return true;
	 */
	public static int BUSY = IDLE + 1;

	/**
	 * An error occurred the last time the asynchronousMoveTo method (or one of the methods which indirectly call it)
	 * was called.
	 */
	public static int FAULT = BUSY + 1;

	/**
	 * The status value this object represents
	 */
	public int status = 0;

	/**
	 * The name of the Scannable which this status value represents
	 */
	public String scannable = "";

	/**
	 * Constructor.
	 * 
	 * @param name
	 * @param status
	 */
	public ScannableStatus(String name, int status) {
		this.scannable = name;
		this.status = status;
	}

	/**
	 * The status value this object represents
	 * 
	 * @return int
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * The name of the Scannable this object represents
	 * 
	 * @return String
	 */
	public String getScannableName() {
		return scannable;
	}
	
	@Override
	public String toString() {
		return String.format("ScannableStatus(%s, %d)", scannable, status);
	}
	
}
