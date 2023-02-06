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

import uk.ac.diamond.daq.mapping.api.document.scanpath.Trajectory;
import uk.ac.gda.common.exception.GDAException;

/**
 * Describes a trajectory in ScanPointGenerator terms
 */
public interface AcquisitionTemplate {

	/**
	 * Defines how the space is traversed
	 */
	IScanPointGeneratorModel getIScanPointGeneratorModel();

	/**
	 * Defines the trajectory geometry
	 */
	IROI getROI();

	/**
	 * Validates the internal {@link Trajectory}
	 *
	 * @throws GDAException is the document contains invalid data
	 */
	void validate() throws GDAException;
}
