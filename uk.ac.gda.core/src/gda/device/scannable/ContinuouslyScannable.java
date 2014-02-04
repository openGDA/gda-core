/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.ScannableMotionUnits;

/**
 * This operates a device whose operation sends out trigger signals to HistogramDetectors to collect data.
 * <p>
 * Such Scannables will be used in ContinuousScans.
 */
public interface ContinuouslyScannable extends ScannableMotionUnits {

	/**
	 * Sets the parameters which define the continuous movement to use
	 * 
	 * @param parameters
	 */
	public void setContinuousParameters(ContinuousParameters parameters);

	/**
	 * @return ContinuousParameters
	 */
	public ContinuousParameters getContinuousParameters();

	/**
	 * Prepare hardware for the continuous move. This assumes that continuous parameters have been supplied.

	 * @throws DeviceException
	 */
	public void prepareForContinuousMove() throws DeviceException;

	/**
	 * The actual hardware might not be able to return exactly the numbner of points requested. This method returns the
	 * actaul number the scan should expect.
	 * 
	 * @return int - the number of data points which would be actually returned based on the given ContinuousParameters
	 */
	public int getNumberOfDataPoints();
	
	/**
	 * Perform the move based on the supplied continuous parameters.
	 * 
	 * @throws DeviceException
	 */
	public void performContinuousMove() throws DeviceException;

	/**
	 * Once move and data collection complete, reverts any hardware settings etc. set for the move.
	 * 
	 * @throws DeviceException
	 */
	public void continuousMoveComplete() throws DeviceException;
	
	/**
	 * As this scannable controls the motion, it is responsible for knowing the energy at each frame
	 * 
	 * @param frameIndex
	 * @return double energy in eV of the given frame
	 */
	public double calculateEnergy(int frameIndex) throws DeviceException;
}
