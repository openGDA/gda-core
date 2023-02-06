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
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.springframework.util.Assert;

import gda.mscan.element.Mutator;
import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplate;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.Trajectory;
import uk.ac.gda.common.exception.GDAException;

/**
 * Describes a model where an acquisition is performed on a single {@code Scannable}, that is on a single axis, and a
 * {@link Mutator} map defines the movement type
 *
 * @author Maurizio Nagni
 */
public class AxialStepModelDocument implements AcquisitionTemplate {

	private final Trajectory trajectory;

	private IScanPointGeneratorModel pathModel;
	private IROI roi;

	public AxialStepModelDocument(Trajectory trajectory) {
		this.trajectory = trajectory;
	}

	@Override
	public IScanPointGeneratorModel getIScanPointGeneratorModel() {
		if (pathModel == null) pathModel = createPathModel();
		return pathModel;
	}

	@Override
	public IROI getROI() {
		if (roi == null) roi = createROI();
		return roi;
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
		var numberOfScannables = trajectory.getAxes().size();
		Assert.isTrue(numberOfScannables == 1, "Only one scannable expected in model; found " + numberOfScannables);
	}

	private IScanPointGeneratorModel createPathModel() {
		ScannableTrackDocument axis = trajectory.getAxes().get(0);

		if (axis.getStep() < 0.0) {
			return null;
		}

		double step = axis.calculatedStep();

		AxialStepModel model = new AxialStepModel(axis.getScannable(),
				axis.getStart(),
				axis.getStop(), step);
		model.setAlternating(axis.isAlternating());
		model.setContinuous(axis.isContinuous());
		model.setName(axis.getScannable());

		return model;
	}

	private IROI createROI() {
		ScannableTrackDocument scannableTrackDocument = trajectory.getAxes().get(0);
		// start point, because the movement is one a single axis the second is set to zero
		double[] spt = { scannableTrackDocument.getStart(), 0.0 };
		// end point, because the movement is one a single axis the second is set to zero
		double[] ept = { scannableTrackDocument.getStop(), 0.0 };

		// LinearROI defines a line in a 2D space
		var line = new LinearROI(spt, ept);
		line.setName(scannableTrackDocument.getScannable());
		return line;
	}
}
