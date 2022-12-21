/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;

import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplate;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument.Axis;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.gda.api.acquisition.AcquisitionTemplateType;
import uk.ac.gda.common.exception.GDAException;

public class DiffractionTomographyDocument implements AcquisitionTemplate {

	private ScanpathDocument document;
	private AcquisitionTemplate innerScan;
	private AcquisitionTemplate outerScan;

	public DiffractionTomographyDocument(ScanpathDocument document) {
		this.document = document;
		this.innerScan = getInnerScan();
		this.outerScan = getOuterScan();
	}

	private AcquisitionTemplate getInnerScan() {
		var innerAxes = List.of(getAxis(Axis.X), getAxis(Axis.THETA));
		var innerDocument = new ScanpathDocument(AcquisitionTemplateType.TWO_DIMENSION_GRID, innerAxes);
		return new TwoAxisGridPointsModelDocument(innerDocument);
	}

	private AcquisitionTemplate getOuterScan() {
		var outerAxis = List.of(getAxis(Axis.Y));
		var outerDocument = new ScanpathDocument(AcquisitionTemplateType.ONE_DIMENSION_LINE, outerAxis);
		return new AxialStepModelDocument(outerDocument);
	}

	private ScannableTrackDocument getAxis(Axis axis) {
		return document.getScannableTrackDocuments().stream()
				.filter(doc -> doc.getAxis().equals(axis))
				.findFirst().orElseThrow(() -> new NoSuchElementException(axis + " axis not found!"));
	}

	@Override
	public ScanpathDocument getScanpathDocument() {
		return document;
	}

	@Override
	public List<IScanPointGeneratorModel> getIScanPointGeneratorModels() {
		var generators = new ArrayList<>(outerScan.getIScanPointGeneratorModels());
		generators.addAll(innerScan.getIScanPointGeneratorModels());
		return generators;
	}

	@Override
	public IROI getROI() {
		return innerScan.getROI();
	}

	@Override
	public void validate() throws GDAException {
		innerScan.validate();
		outerScan.validate();
	}



}
