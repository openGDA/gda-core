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

package gda.device.scannable;

import gda.device.Detector;
import gda.device.DeviceException;

/**
 * VariableCollectionTimeDetectors can be prepared with a profile of collection times, such that each successive call to
 * collectData can result in a different collection time.
 * <p>
 * The index pointing to the 'current' collection time is reset to zero at {@link #atScanLineStart()} and incremented
 * after each {@link #collectData()}. {@link #getCollectionTime()} should read out this 'current' time.
 * <p>
 * Calls to {@link #setCollectionTime(double)} should fail if a collection profile is set. Setting the profile to null
 * will remove the profile and {@link #getCollectionTime()} should again return the time configured with {@link #setCollectionTime(double)}.
 */
public interface VariableCollectionTimeDetector extends Detector {

	/**
	 * Set a profile of collection times used for successive calls to {@link #collectData()} or in a continuous scan.
	 * 
	 * @param times times in seconds or null
	 * @throws DeviceException
	 */
	public void setCollectionTimeProfile(double[] times) throws DeviceException;
	
	/**
	 * Get the profile of collection times used for successive calls to {@link #collectData()} or in a continuous scan.
	 * 
	 * @throws DeviceException times in seconds or null
	 */
	public double[] getCollectionTimeProfile() throws DeviceException;

}

