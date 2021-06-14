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
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
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
public class AxialStepModelDocumentTest extends DocumentTestBase {

	private static final String MOTOR_X = "motor_x";

	@Test
	public void serialiseDocumentTest() throws GDAException {
		List<ScannableTrackDocument> scannableTrackDocuments = new ArrayList<>();
		var builder = new ScannableTrackDocument.Builder();
		builder.withScannable(MOTOR_X);
		builder.withAxis("x");
		builder.withStart(2.0);
		builder.withStop(2.0);
		builder.withPoints(5);
		scannableTrackDocuments.add(builder.build());
		Map<Mutator, List<Number>> mutators = new EnumMap<>(Mutator.class);
		mutators.put(Mutator.ALTERNATING, Arrays.asList(1, 2));
		var modelDocument = new ScanpathDocument(AcquisitionTemplateType.ONE_DIMENSION_LINE,
				scannableTrackDocuments, mutators);
		String document = serialiseDocument(modelDocument);
		assertThat(document, containsString(MOTOR_X));
		assertThat(document, containsString("\"axis\" : \"x\""));
		assertThat(document, containsString("\"ALTERNATING\" : [ 1, 2 ]"));
	}

	@Test
	public void deserialiseDocumentTest() throws GDAException {
		ScanpathDocument modelDocument = deserialiseDocument("test/resources/AxialStepModelDocument.json",
				ScanpathDocument.class);
		Assert.assertEquals(MOTOR_X, modelDocument.getScannableTrackDocuments().get(0).getScannable());
		Assert.assertTrue(modelDocument.getMutators().containsKey(Mutator.ALTERNATING));
		Assert.assertTrue(modelDocument.getMutators().containsValue(Arrays.asList(1, 2)));
	}
}
