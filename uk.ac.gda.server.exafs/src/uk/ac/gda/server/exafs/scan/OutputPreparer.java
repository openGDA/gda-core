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

import java.util.Set;

import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.scan.ScanPlotSettings;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.IScanParameters;

/**
 * Sets up the experiment-specific options for how the output is configured e.g. file names, file headers
 */
public interface OutputPreparer {

	/**
	 * Gives the preparer the parameters for the next experiment and do any preparation for the whole experiment.
	 */
	void configure(IOutputParameters outputParameters, IScanParameters scanBean, IDetectorParameters detectorBean, ISampleParameters sampleParameters)
			throws DeviceException;

	/**
	 * Perform any beamline-specific set up before data collection.
	 */
	void beforeEachRepetition() throws Exception;

	ScanPlotSettings getPlotSettings();

	void resetStaticMetadataList();

	AsciiDataWriterConfiguration getAsciiDataWriterConfig(IScanParameters scanBean);

	Set<Scannable> getScannablesToBeAddedAsColumnInDataFile();

}
