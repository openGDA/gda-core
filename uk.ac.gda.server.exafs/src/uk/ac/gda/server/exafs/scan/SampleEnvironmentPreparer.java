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

package uk.ac.gda.server.exafs.scan;

import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.server.exafs.scan.iterators.SampleEnvironmentIterator;

/**
 * Sets up the beamline-specific sample environment options. Each implementation should be beamline specific as each
 * beamline has a unique combinations of sample environments and scan types.
 */
public interface SampleEnvironmentPreparer {

	/**
	 * Gives the preparer the parameters for the next experiment and do any preparation for the whole experiment.
	 */
	public void configure(ISampleParameters sampleParameters) throws Exception;

	/**
	 * Returns an Iterator object which loops over the different sample environment setings for this experiment e.g.
	 * motor positions, temperatures, or a combination of these.
	 * 
	 * @param experimentType
	 * @return SampleEnvironmentIterator
	 */
	public SampleEnvironmentIterator createIterator(String experimentType);

}
