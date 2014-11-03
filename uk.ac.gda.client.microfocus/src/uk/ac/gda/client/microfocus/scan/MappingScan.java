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

package uk.ac.gda.client.microfocus.scan;

import uk.ac.gda.client.microfocus.scan.datawriter.MicroFocusWriterExtender;
import uk.ac.gda.server.exafs.scan.XasScan;

public interface MappingScan extends XasScan{

	public MicroFocusWriterExtender getMFD();

//	public void doCollection(ISampleParameters sampleParams, IScanParameters scanBean,
//			IDetectorParameters detParams, IOutputParameters outputParams,
//			IDetectorConfigurationParameters xspressConfigurationParameters, String experimentalFullPath, int i) throws Exception;
}
