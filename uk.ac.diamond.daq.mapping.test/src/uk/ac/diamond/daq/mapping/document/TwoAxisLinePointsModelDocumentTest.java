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
import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplateType;
import uk.ac.diamond.daq.mapping.api.document.model.AxialStepModelDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.gda.common.exception.GDAException;

/**
 * Tests for the {@link AxialStepModelDocument}
 *
 * @author Maurizio Nagni
 */
public class TwoAxisLinePointsModelDocumentTest extends DocumentTestBase {

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
		builder.withPoints(5);
		scannableTrackDocuments.add(builder.build());

		builder = new ScannableTrackDocument.Builder();
		builder.withScannable("motor_y");
		builder.withStart(1.0);
		builder.withStop(1.0);
		builder.withPoints(10);
		scannableTrackDocuments.add(builder.build());

		Map<Mutator, List<Number>> mutators = new EnumMap<>(Mutator.class);
		mutators.put(Mutator.ALTERNATING, Arrays.asList(1, 2));
		ScanpathDocument modelDocument = new ScanpathDocument(AcquisitionTemplateType.TWO_DIMENSION_LINE,
				scannableTrackDocuments, mutators);
		String document = serialiseDocument(modelDocument);
		assertThat(document, containsString("motor_x"));
		assertThat(document, containsString("motor_y"));
		assertThat(document, containsString("\"ALTERNATING\" : [ 1, 2 ]"));
	}

	@Test
	public void deserialiseDocumentTest() throws GDAException {
		ScanpathDocument modelDocument = deserialiseDocument("test/resources/TwoAxisLinePointsModelDocument.json",
				ScanpathDocument.class);
		Assert.assertEquals(2, modelDocument.getScannableTrackDocuments().size());
		ScannableTrackDocument std = modelDocument.getScannableTrackDocuments().get(0);
		Assert.assertEquals("motor_x", std.getScannable());
		Assert.assertEquals(1.0, std.getStep(), 0.0);
		Assert.assertEquals(Integer.MIN_VALUE, std.getPoints(), 0.0);

		std = modelDocument.getScannableTrackDocuments().get(1);
		Assert.assertEquals("motor_y", std.getScannable());
		Assert.assertEquals(Double.MIN_VALUE, std.getStep(), 0.0);
		Assert.assertEquals(2, std.getPoints(), 0.0);


		Assert.assertTrue(modelDocument.getMutators().containsKey(Mutator.ALTERNATING));
		Assert.assertTrue(modelDocument.getMutators().containsValue(Arrays.asList(1, 2)));
	}
}