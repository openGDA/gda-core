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

import gda.device.detector.BufferedDetector;

/**
 * For QEXAFS, override the detectors listed in the XML file to remove the need to have two detector XML files.
 * <p>
 * The normal paradigm for the XAS Experiment perspective is that the same XML files can be used for all scan types,
 * with only the scan XML file changing. But this breaks down for qexafs where different detector objects are needed as
 * they need to fulfil the BufferedDetector interface. So instead override the list of detectors using the DetectorPreparer.
 */
public interface QexafsDetectorPreparer extends DetectorPreparer {

	public BufferedDetector[] getQEXAFSDetectors() throws Exception;
}
