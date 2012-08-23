/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device;

/**
 * An interface to control detectors from outside a scan. The file template used to determine the path for detector
 * files is determined by the implementation. AreaDetector's NDFilePlugin
 * (http://cars.uchicago.edu/software/epics/NDPluginFile.html) will always apply arguments to the template in the order
 * filepath, filename, filenumber. Here the filepath and filename are configurable, and the filenumber is determined by
 * the implementation.
 */
public interface DetectorSnapper {


	/**
	 * Sets the collection time for the next collection (implicitly a single image collection).
	 * 
	 * @param collectionTime
	 * @throws Exception 
	 */
	public void prepareForAcquisition(double collectionTime) throws Exception;

	/**
	 * Return the acquisition time resulting from the last configured collection time. This will depend on the
	 * collection strategy.
	 * 
	 * @return acquisition time in seconds
	 * @throws Exception 
	 */
	public double getAcquireTime() throws Exception;

	/**
	 * Return the acquisition period resulting from the last configured collection time. This will depend on the
	 * collection strategy.
	 * 
	 * @return acquisition time in seconds
	 * @throws Exception 
	 */
	public double getAcquirePeriod() throws Exception;

	/**
	 * Trigger an acquisition and block until complete.
	 * 
	 * @return list of all resulting files.
	 * @throws InterruptedException
	 * @throws IllegalStateException
	 *             if the detector was already acquiring
	 * @throws Exception 
	 */
	public String[] acquire() throws InterruptedException, IllegalStateException, Exception;

}
