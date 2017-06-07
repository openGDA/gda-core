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

package uk.ac.diamond.daq.persistence.bcm;

import java.util.Collection;

/**
 * BCM specific exception
 */
public class BcmException extends Exception {

	private Collection<String> conflictingScannableNames = null;

	/**
	 *
	 */
	public BcmException() {
	}

	/**
	 * @param msg
	 */
	public BcmException(String msg) {
		super(msg);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public BcmException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param conflictingScannableNames
	 */
	public BcmException(String message, Collection<String> conflictingScannableNames) {
		super(message);
		this.conflictingScannableNames = conflictingScannableNames;
	}

	/**
	 * @return list of conflicting scannables - can happen on loadMode - usually null
	 */
	public Collection<String> getConflictingScannableNames() {
		return conflictingScannableNames;
	}

	/**
	 * @param conflictingScannableNames
	 */
	public void setConflictingScannableNames(Collection<String> conflictingScannableNames) {
		this.conflictingScannableNames = conflictingScannableNames;
	}

	@Override
	public String getMessage() {
		String csns = "";
		if (conflictingScannableNames != null) {
			for (String s : conflictingScannableNames) {
				csns = csns + " " + s;
			}
		}
		return super.getMessage()+csns;
	}
}
