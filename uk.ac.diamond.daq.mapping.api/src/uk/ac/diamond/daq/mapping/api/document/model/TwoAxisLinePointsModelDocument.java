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
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.springframework.util.Assert;

import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplate;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.Trajectory;
import uk.ac.gda.common.exception.GDAException;

/**
 * Describes an acquisition on a 2D line.
 *
 * @author Maurizio Nagni
 */
public class TwoAxisLinePointsModelDocument implements AcquisitionTemplate {

	private final Trajectory trajectory;

	private IScanPointGeneratorModel pathModel;
	private IROI roi;

	TwoAxisLinePointsModelDocument(Trajectory trajectory) {
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
		// Has to define two axes
		var numberOfScannables = trajectory.getAxes().size();
		Assert.isTrue(numberOfScannables == 2, "Two scannables expected; found " + numberOfScannables);

		// Different axes
		ScannableTrackDocument std1 = trajectory.getAxes().get(0);
		ScannableTrackDocument std2 = trajectory.getAxes().get(1);
		Assert.isTrue(!std1.getScannable().equals(std2.getScannable()), "Distinct scannables expected");
	}

	private IScanPointGeneratorModel createPathModel() {
		ScannableTrackDocument scannableOne = trajectory.getAxes().get(0);
		ScannableTrackDocument scannableTwo = trajectory.getAxes().get(1);

		TwoAxisLinePointsModel model = new TwoAxisLinePointsModel();
		model.setPoints(scannableOne.getPoints());
		model.setxAxisName(scannableOne.getScannable());
		model.setyAxisName(scannableTwo.getScannable());
		model.setBoundingLine(new BoundingLine(scannableOne.getStart(), scannableTwo.getStart(),
				scannableOne.getStop() - scannableOne.getStart(), scannableTwo.getStop() - scannableTwo.getStart()));
		model.setAlternating(isAlternating());
		model.setContinuous(isContinuous());
		return model;
	}

	private boolean isAlternating() {
		return trajectory.getAxes().stream().anyMatch(ScannableTrackDocument::isAlternating);
	}

	private boolean isContinuous() {
		return trajectory.getAxes().stream().anyMatch(ScannableTrackDocument::isContinuous);
	}

	private IROI createROI() {
		var scannableOne = trajectory.getAxes().get(0);
		var scannableTwo = trajectory.getAxes().get(1);
		return new LinearROI(new double[] { scannableOne.getStart(), scannableTwo.getStart() },
				new double[] { scannableOne.getStop(), scannableTwo.getStop() });
	}
}
