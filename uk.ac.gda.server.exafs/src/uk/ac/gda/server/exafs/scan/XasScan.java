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

import uk.ac.gda.beans.exafs.IDetectorConfigurationParameters;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.IScanParameters;

public interface XasScan {

	/**
	 * For the database behind the LoggingScriptController which keeps a list of data collections and for log messages
	 * to the user.
	 *
	 * @return String name of scan type e.g. XANES
	 */
	public abstract String getScanType();

	public abstract void doCollection(ISampleParameters sampleBean, IScanParameters scanBean,
			IDetectorParameters detectorBean, IOutputParameters outputBean,
			IDetectorConfigurationParameters detectorConfigurationBean, String experimentFullPath, int numRepetitions)
			throws Exception;

}