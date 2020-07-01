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

package uk.ac.diamond.daq.mapping.document;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import gda.mscan.element.Mutator;
import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplate;
import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplateType;
import uk.ac.diamond.daq.mapping.api.document.model.AxialStepModelDocument;
import uk.ac.diamond.daq.mapping.api.document.model.AcquisitionTemplateFactory;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.gda.api.exception.GDAException;

/**
 * Tests for the {@link AxialStepModelDocument}
 *
 * @author Maurizio Nagni
 */
public class TwoAxisPointSingleModelDocumentTest extends DocumentTestBase {

	@Before
	public void before() {
	}

	@Test
	public void serialiseDocumentTest() throws GDAException {
		List<ScannableTrackDocument> scannableTrackDocuments = new ArrayList<>();

		ScannableTrackDocument.Builder builder = new ScannableTrackDocument.Builder();
		builder.withScannable("motor_x");
		builder.withStart(2.0);
		builder.withStop(2.0);
		builder.withPoints(1);
		scannableTrackDocuments.add(builder.build());

		builder = new ScannableTrackDocument.Builder();
		builder.withScannable("motor_y");
		builder.withStart(1.0);
		builder.withStop(1.0);
		builder.withPoints(1);
		scannableTrackDocuments.add(builder.build());

		Map<Mutator, List<Number>> mutators = new EnumMap<>(Mutator.class);
		mutators.put(Mutator.ALTERNATING, Arrays.asList(1, 2));
		ScanpathDocument modelDocument = new ScanpathDocument(AcquisitionTemplateType.TWO_DIMENSION_POINT,
				scannableTrackDocuments, mutators);
		String document = serialiseDocument(modelDocument);
		assertThat(document, containsString("motor_x"));
		assertThat(document, containsString("motor_y"));
		assertThat(document, containsString("\"alternating\" : [ 1, 2 ]"));
	}

	@Test
	public void deserialiseDocumentTest() throws GDAException {
		ScanpathDocument modelDocument = deserialiseDocument("/resources/TwoAxisPointSingleModelDocument.json",
				ScanpathDocument.class);

		ScannableTrackDocument std = modelDocument.getScannableTrackDocuments().get(0);
		Assert.assertEquals("motor_x", std.getScannable());
		Assert.assertEquals(2.0, std.getStart(), 0.0);
		Assert.assertEquals(2.0, std.getStop(), 0.0);
		Assert.assertEquals(Integer.MIN_VALUE, std.getPoints(), 0.0);

		std = modelDocument.getScannableTrackDocuments().get(1);
		Assert.assertEquals("motor_y", std.getScannable());
		Assert.assertEquals(5.0, std.getStart(), 0.0);
		Assert.assertEquals(5.0, std.getStop(), 0.0);
		Assert.assertEquals(Integer.MIN_VALUE, std.getPoints(), 0.0);
	}

	@Test
	public void validDocumentTest() throws GDAException {
		ScanpathDocument modelDocument = deserialiseDocument("/resources/TwoAxisPointSingleModelDocument.json",
				ScanpathDocument.class);
		AcquisitionTemplate acquisitionTemplate = AcquisitionTemplateFactory.buildModelDocument(modelDocument);

		acquisitionTemplate.validate();
	}

	@Test(expected = GDAException.class)
	public void invalidDocumentTest() throws GDAException {
		ScanpathDocument modelDocument = deserialiseDocument("/resources/TwoAxisPointSingleModelInvalidDocument.json",
				ScanpathDocument.class);
		AcquisitionTemplate acquisitionTemplate = AcquisitionTemplateFactory.buildModelDocument(modelDocument);

		acquisitionTemplate.validate();
	}
}