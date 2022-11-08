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

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.springframework.util.Assert;

import gda.mscan.element.Mutator;
import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplate;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.gda.common.exception.GDAException;

/**
 * Describes an acquisition on a 2D line.
 *
 * @author Maurizio Nagni
 */
public class TwoAxisLinePointsModelDocument implements AcquisitionTemplate {

	private final ScanpathDocument scanpathDocument;

	private IScanPointGeneratorModel pathModel;
	private IROI roi;

	TwoAxisLinePointsModelDocument(ScanpathDocument scanpathDocument) {
		this.scanpathDocument = scanpathDocument;
	}

	@Override
	public List<IScanPointGeneratorModel> getIScanPointGeneratorModels() {
		if (pathModel == null) pathModel = createPathModel();
		return List.of(pathModel);
	}

	@Override
	public IROI getROI() {
		return Optional.ofNullable(roi).orElseGet(createROI);
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
		var numberOfScannables = scanpathDocument.getScannableTrackDocuments().size();
		Assert.isTrue(numberOfScannables == 2, "Two scannables expected; found " + numberOfScannables);

		// Different axes
		ScannableTrackDocument std1 = getScanpathDocument().getScannableTrackDocuments().get(0);
		ScannableTrackDocument std2 = getScanpathDocument().getScannableTrackDocuments().get(1);
		Assert.isTrue(!std1.getScannable().equals(std2.getScannable()), "Distinct scannables expected");

		// Ignores any other property
	}

	private IScanPointGeneratorModel createPathModel() {
		// Temporary trick to support line until a multi dimentional approach is defined
		ScannableTrackDocument scannableOne = getScanpathDocument().getScannableTrackDocuments().get(0);
		ScannableTrackDocument scannableTwo = getScanpathDocument().getScannableTrackDocuments().get(1);

		TwoAxisLinePointsModel model = new TwoAxisLinePointsModel();
		model.setPoints(scannableOne.getPoints());
		model.setxAxisName(scannableOne.getScannable());
		model.setyAxisName(scannableTwo.getScannable());
		model.setBoundingLine(new BoundingLine(scannableOne.getStart(), scannableTwo.getStart(),
				scannableOne.getStop() - scannableOne.getStart(), scannableTwo.getStop() - scannableTwo.getStart()));
		model.setAlternating(getScanpathDocument().getMutators().containsKey(Mutator.ALTERNATING));
		model.setContinuous(getScanpathDocument().getMutators().containsKey(Mutator.CONTINUOUS));
		return model;
	}

	private Supplier<IROI> createROI = () -> {
		// Temporary trick to support line until a multi dimentional approach is defined
		ScannableTrackDocument scannableOne = getScanpathDocument().getScannableTrackDocuments().get(0);
		ScannableTrackDocument scannableTwo = getScanpathDocument().getScannableTrackDocuments().get(1);
		this.roi = new LinearROI(new double[] { scannableOne.getStart(), scannableTwo.getStart() },
				new double[] { scannableOne.getStop(), scannableTwo.getStop() });
		return this.roi;
	};

	@Override
	public ScanpathDocument getScanpathDocument() {
		return scanpathDocument;
	}
}
