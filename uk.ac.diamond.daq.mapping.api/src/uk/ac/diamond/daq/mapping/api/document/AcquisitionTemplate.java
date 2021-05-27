/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api.document;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;

import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.gda.common.exception.GDAException;

/**
 * Defines how and where execute an acquisition.
 *
 * <p>
 * The space associated with an acquisition is defined in a {@link ScanpathDocument} by a set of
 * {@link ScannableTrackDocument}. Each class implementing this interface defines:
 * <ul>
 * <li><i>how the space is traversed</i> implementing {@link #getIScanPointGeneratorModel()} (i.e. sorting the axes or
 * jumping randomly)</li>
 * <li><i>what geometry is defined in the space</i> implementing {@link #getROI()} (i.e. in a 3D space, it can be a line, a
 * surface or a solid)</li>
 * </ul>
 * </p>
 *
 * @author Maurizio Nagni
 */
public interface AcquisitionTemplate {

	/**
	 * The acquisition configuration
	 * @return the acquisition configuration
	 */
	ScanpathDocument getScanpathDocument();

	/**
	 * Defines how traverse the space defined {@link ScanpathDocument}
	 * @return a point generator
	 */
	IScanPointGeneratorModel getIScanPointGeneratorModel();

	/**
	 * Defines the acquisition geometry
	 * @return the geometric shape for the acquisition
	 */
	IROI getROI();

	/**
	 * Validates the internal {@link ScanpathDocument}
	 *
	 * @throws GDAException is the document contains invalid data
	 */
	void validate() throws GDAException;
}
