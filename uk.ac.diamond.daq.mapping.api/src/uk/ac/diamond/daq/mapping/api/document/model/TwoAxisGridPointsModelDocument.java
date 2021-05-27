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

import static gda.mscan.element.Mutator.RANDOM_OFFSET;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsRandomOffsetModel;
import org.springframework.util.Assert;

import gda.mscan.element.Mutator;
import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplate;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.gda.common.exception.GDAException;

/**
 * Describes an acquisition on a 2D rectangle.
 *
 * @author Maurizio Nagni
 */
public class TwoAxisGridPointsModelDocument implements AcquisitionTemplate {

	private final ScanpathDocument scanpathDocument;

	private IScanPointGeneratorModel pathModel;
	private IROI roi;

	TwoAxisGridPointsModelDocument(ScanpathDocument scanpathDocument) {
		this.scanpathDocument = scanpathDocument;
	}

	@Override
	public IScanPointGeneratorModel getIScanPointGeneratorModel() {
		return Optional.ofNullable(pathModel).orElseGet(createPathModel);
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
		Assert.isTrue(scanpathDocument.getScannableTrackDocuments().size() == 2);
		ScannableTrackDocument std1 = scanpathDocument.getScannableTrackDocuments().get(0);
		ScannableTrackDocument std2 = scanpathDocument.getScannableTrackDocuments().get(1);

		// Different axes
		Assert.isTrue(!std1.getScannable().equals(std2.getScannable()));

		// Ignores any other property
	}

	private Supplier<IScanPointGeneratorModel> createPathModel = () -> {
		// Temporary trick to support line until a multi dimentional approach is defined
		ScannableTrackDocument scannableOne = getScanpathDocument().getScannableTrackDocuments().get(0);
		ScannableTrackDocument scannableTwo = getScanpathDocument().getScannableTrackDocuments().get(1);

		TwoAxisGridPointsModel model;
		if (getScanpathDocument().getMutators().containsKey(RANDOM_OFFSET)) {
			model = createRandomModel();
		} else {
			model = createGridPointModel();
		}
		model.setBoundingBox(getBoundingBox());
		model.setxAxisName(scannableOne.getScannable());
		model.setyAxisName(scannableTwo.getScannable());

		model.setxAxisPoints(scannableOne.getPoints());
		model.setyAxisPoints(scannableTwo.getPoints());
		model.setAlternating(getScanpathDocument().getMutators().containsKey(Mutator.ALTERNATING));
		model.setContinuous(getScanpathDocument().getMutators().containsKey(Mutator.CONTINUOUS));
		this.pathModel = model;
		return this.pathModel;
	};


	private TwoAxisGridPointsModel createGridPointModel() {
		return new TwoAxisGridPointsModel();
	}

	private TwoAxisGridPointsModel createRandomModel() {
		TwoAxisGridPointsRandomOffsetModel model = new TwoAxisGridPointsRandomOffsetModel();

		List<Number> params = getScanpathDocument().getMutators().get(RANDOM_OFFSET);
		model.setOffset(params.get(AcquisitionTemplateFactory.OFFSET_INDEX).doubleValue());
		if (params.size() > 1) {
			model.setSeed(getScanpathDocument().getMutators().get(RANDOM_OFFSET).get(AcquisitionTemplateFactory.SEED_INDEX).intValue());
		}
		return model;
	}

	private Supplier<IROI> createROI = () -> {
		// Temporary trick to support line until a multi dimensional approach is defined
		ScannableTrackDocument scannableOne = getScanpathDocument().getScannableTrackDocuments().get(0);
		ScannableTrackDocument scannableTwo = getScanpathDocument().getScannableTrackDocuments().get(1);
		this.roi = new RectangularROI(scannableOne.getStart(), scannableTwo.getStart(), scannableOne.length(),
				scannableTwo.length(), 0.0);
		return this.roi;
	};

	private BoundingBox getBoundingBox() {
		ScannableTrackDocument scannableOne = getScanpathDocument().getScannableTrackDocuments().get(0);
		ScannableTrackDocument scannableTwo = getScanpathDocument().getScannableTrackDocuments().get(1);
		return new BoundingBox(scannableOne.getStart(), scannableTwo.getStart(),
				scannableOne.length(),	scannableTwo.length());
	}

	@Override
	public ScanpathDocument getScanpathDocument() {
		return scanpathDocument;
	}
}
