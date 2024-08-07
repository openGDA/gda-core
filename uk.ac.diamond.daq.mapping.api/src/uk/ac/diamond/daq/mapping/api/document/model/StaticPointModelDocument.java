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

package uk.ac.diamond.daq.mapping.api.document.model;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.springframework.util.Assert;

import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplate;
import uk.ac.diamond.daq.mapping.api.document.scanpath.Trajectory;
import uk.ac.gda.common.exception.GDAException;

/**
 * Describes an acquisition on a static point.
 *
 * @author Maurizio Nagni
 */
public class StaticPointModelDocument implements AcquisitionTemplate {

	private final Trajectory trajectory;

	private IScanPointGeneratorModel pathModel;

	StaticPointModelDocument(Trajectory trajectory) {
		this.trajectory = trajectory;
	}

	@Override
	public IScanPointGeneratorModel getIScanPointGeneratorModel() {
		if (pathModel == null) pathModel = createPathModel();
		return pathModel;
	}

	@Override
	public IROI getROI() {
		return null;
	}

	@Override
	public void validate() throws GDAException {
		try {
			executeValidation();
		} catch (IllegalArgumentException e) {
			throw new GDAException(String.format("Invalid document for %s", this.getClass()), e);
		}
	}

	private void executeValidation() {
		// Has to define a single document: not for the axis but to define the number of acquisitions
		var numberOfScannables = trajectory.getAxes().size();
		Assert.isTrue(numberOfScannables == 1, "Only one scannable expected in model; found " + numberOfScannables);
	}

	private IScanPointGeneratorModel createPathModel() {
		var scannableOne = trajectory.getAxes().get(0);

		var model = new StaticModel();
		model.setSize(scannableOne.getPoints());
		return model;
	}
}
