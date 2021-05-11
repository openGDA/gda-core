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

import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.TwoAxisPointSingleModel;
import org.springframework.util.Assert;

import gda.mscan.element.Mutator;
import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplate;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.gda.common.exception.GDAException;

/**
 * Describes an acquisition on a point in a 2D space.
 *
 * @author Maurizio Nagni
 */
public class TwoAxisPointSingleModelDocument implements AcquisitionTemplate {

	private final ScanpathDocument scanpathDocument;

	private IScanPointGeneratorModel pathModel;
	private IROI roi;

	TwoAxisPointSingleModelDocument(ScanpathDocument scanpathDocument) {
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
		Assert.isTrue(getScanpathDocument().getScannableTrackDocuments().size() == 2);
		ScannableTrackDocument std1 = getScanpathDocument().getScannableTrackDocuments().get(0);
		ScannableTrackDocument std2 = getScanpathDocument().getScannableTrackDocuments().get(1);
		// Each axis need to have start equal stop (is a point on each line)
		Assert.isTrue(std1.getStart() == std1.getStop());
		Assert.isTrue(std2.getStart() == std2.getStop());

		// Different axes
		Assert.isTrue(!std1.getScannable().equals(std2.getScannable()));

		// Ignores any other property
	}

	private Supplier<IScanPointGeneratorModel> createPathModel = () -> {
		// Temporary trick to support line until a multi dimentional approach is defined
		ScannableTrackDocument scannableOne = getScanpathDocument().getScannableTrackDocuments().get(0);
		ScannableTrackDocument scannableTwo = getScanpathDocument().getScannableTrackDocuments().get(1);

		TwoAxisPointSingleModel model = new TwoAxisPointSingleModel();
		model.setX(scannableOne.getStart());
		model.setxAxisName(scannableOne.getScannable());
		model.setY(scannableTwo.getStart());
		model.setyAxisName(scannableTwo.getScannable());
		model.setAlternating(getScanpathDocument().getMutators().containsKey(Mutator.ALTERNATING));
		model.setContinuous(getScanpathDocument().getMutators().containsKey(Mutator.CONTINUOUS));
		return model;
	};

	private Supplier<IROI> createROI = () -> {
		// Temporary trick to support line until a multi dimentional approach is defined
		ScannableTrackDocument scannableOne = getScanpathDocument().getScannableTrackDocuments().get(0);
		ScannableTrackDocument scannableTwo = getScanpathDocument().getScannableTrackDocuments().get(1);
		this.roi = new PointROI(scannableOne.getStart(), scannableTwo.getStart());
		return this.roi;
	};

	@Override
	public ScanpathDocument getScanpathDocument() {
		return scanpathDocument;
	}
}
