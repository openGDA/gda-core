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

package uk.ac.diamond.daq.microfocus.api;

import gda.device.detector.BufferedDetector;
import uk.ac.gda.server.exafs.scan.DetectorPreparer;

public interface RasterMapDetectorPreparer extends DetectorPreparer {

	/**
	 * @return BufferedDetector[] - based on the parameters given to the object in its
	 *         {@link #configure(uk.ac.gda.beans.exafs.IScanParameters, uk.ac.gda.beans.exafs.IDetectorParameters, uk.ac.gda.beans.exafs.IOutputParameters, String)}
	 *         method
	 * @throws Exception
	 */
	public BufferedDetector[] getRasterMapDetectors() throws Exception;
}
