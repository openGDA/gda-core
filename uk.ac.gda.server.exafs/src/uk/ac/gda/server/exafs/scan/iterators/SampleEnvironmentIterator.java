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

package uk.ac.gda.server.exafs.scan.iterators;

import java.util.List;

import gda.device.DeviceException;

/**
 * To loop through a series of sample positions. At each node it is expected to perform the energy-scan data collection.
 * <p>
 * The change to the sample environment is performed within this iterator. So the {@link #next()} method would be called
 * before each energy scan.
 */
public interface SampleEnvironmentIterator {

	/**
	 * @return the total number of scans defined by the bean held by this object
	 */
	public int getNumberOfRepeats();

	/**
	 * Move the sample environment to the next position in its array. Does not return until the sample environment
	 * change has completed.
	 *
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	public void next/* moveToNext */() throws DeviceException, InterruptedException;

	/**
	 * Do not operate hardware, but move the iteration back to the start. To be used when there is an external loop to
	 * the sample environment.
	 */
	public void resetIterator();

	/**
	 * Based on the current position of the iterator AFTER a call to moveToNext, what is the name of the next sample to
	 * be run
	 */
	public String getNextSampleName();

	/**
	 * Based on the current position of the iterator AFTER a call to moveToNext, what are the same details
	 * (descriptions) of the next sample to be run
	 *
	 * @return String[]
	 */
	public List<String> getNextSampleDescriptions();
}
