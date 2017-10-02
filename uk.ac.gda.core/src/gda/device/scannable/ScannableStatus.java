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

import gda.device.Scannable;

/**
 * An enum used by {@link Scannable}s to inform objects IObserving them that the status has changed.
 * <p>
 * To ensure the correct status objects are sent out by Scannables, this status class must not be used by any Scannable
 * abstract base classes, but should only be used by concrete Scannable classes wishing to be IObserved.
 */
public enum ScannableStatus {

	/**
	 * The Scannable is not operating. A call to isBusy would return false;
	 */
	IDLE,

	/**
	 * The Scannable is operating by having its asynchronousMoveTo method being called (or one of the methods which
	 * indirectly call it). A call to isBusy would return true;
	 */
	BUSY,

	/**
	 * An error occurred the last time the asynchronousMoveTo method (or one of the methods which indirectly call it)
	 * was called.
	 */
	FAULT;

}
