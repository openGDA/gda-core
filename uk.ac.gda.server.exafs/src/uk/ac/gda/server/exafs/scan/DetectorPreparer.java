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

import gda.device.Detector;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.IScanParameters;

/**
 * Configures the detectors for ExafsScans. Each implementation should be beamline specific as each beamline has a
 * unique combinations of detectors and scan types.
 */
public interface DetectorPreparer {

	/**
	 * Gives the preparer the parameters for the next experiment and do any preparation for the whole experiment.
	 */
	void configure(IScanParameters scanBean, IDetectorParameters detectorBean, IOutputParameters outputBean,
			String experimentFullPath) throws Exception;

	/**
	 * Perform any beamline-specific set up before data collection.
	 */
	void beforeEachRepetition() throws Exception;

	/**
	 * Perform any beamline-specific work after data collection. To 'reset' the beamline after the experiment.
	 */
	void completeCollection();

	/**
	 * @return any beamline specific detectors to add to ones in use based on the beans given by the configure method
	 */
	Detector[] getExtraDetectors();

}
