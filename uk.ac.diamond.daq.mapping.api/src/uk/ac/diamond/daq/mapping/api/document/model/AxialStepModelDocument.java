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

import static gda.mscan.element.Mutator.ALTERNATING;
import static gda.mscan.element.Mutator.CONTINUOUS;

import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.springframework.util.Assert;

import gda.mscan.element.Mutator;
import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplate;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.gda.api.exception.GDAException;

/**
 * Describes a model where an acquisition is performed on a single {@code Scannable}, that is on a single axis, and a
 * {@link Mutator} map defines the movement type
 *
 * @author Maurizio Nagni
 */
public class AxialStepModelDocument implements AcquisitionTemplate {

	private final ScanpathDocument scanpathDocument;

	private IScanPointGeneratorModel pathModel;
	private IROI roi;

	public AxialStepModelDocument(ScanpathDocument scanpathDocument) {
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
		// Has to define only one axis
		Assert.isTrue(scanpathDocument.getScannableTrackDocuments().size() == 1);

		// Ignores any other property
	}

	private Supplier<IScanPointGeneratorModel> createPathModel = () -> {
		// Temporary trick to support line until a multi dimensional approach is defined
		ScannableTrackDocument scannableTrackDocument = getScanpathDocument().getScannableTrackDocuments().get(0);

		// This is actually inconsistent as the underlying AxialStepModel requires steps in the negative direction
		// to be negative (see below) but for consistency with classic scanning and the Mapping UI negative steps
		// are disallowed as part of a valid mscan string
		if (scannableTrackDocument.getStep() < 0.0) {
			pathModel = null;
			return pathModel;
		}

		// Multiplier to adjust the step value direction
		double sign = scannableTrackDocument.getStop() < scannableTrackDocument.getStart() ? -1 : 1;

		AxialStepModel model = new AxialStepModel(scannableTrackDocument.getScannable(),
				scannableTrackDocument.getStart(),
				scannableTrackDocument.getStop(), scannableTrackDocument.getStep() * sign);
		model.setAlternating(getScanpathDocument().getMutators().containsKey(ALTERNATING));
		model.setContinuous(getScanpathDocument().getMutators().containsKey(CONTINUOUS));
		pathModel = model;
		return pathModel;
	};

	private Supplier<IROI> createROI = () -> {
		// Temporary trick to support line until a multi dimentional approach is defined
		ScannableTrackDocument scannableTrackDocument = getScanpathDocument().getScannableTrackDocuments().get(0);
		// start point, because the movement is one a single axis the second is set to zero
		double[] spt = { scannableTrackDocument.getStart(), 0.0 };
		// end point, because the movement is one a single axis the second is set to zero
		double[] ept = { scannableTrackDocument.getStop(), 0.0 };

		// LinearROI defines a line in a 2D space
		roi = new LinearROI(spt, ept);
		return roi;
	};

	private ScanpathDocument getScanpathDocument() {
		return scanpathDocument;
	}
}