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

package uk.ac.diamond.daq.mapping.ui.document;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import gda.mscan.element.Mutator;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.ui.document.scanpath.AxialStepModelDocument;

/**
 * Tests for the {@link AxialStepModelDocument}
 *
 * @author Maurizio Nagni
 */
public class AxialStepModelDocumentTest {

	private final ObjectMapper objectMapper = DocumentMapper.getObjectMapper();

	@Before
	public void before() {
	}

	@Test
	public void serialiseDocumentTest() {
		ScannableTrackDocument scannableDocument = new ScannableTrackDocument("motor_x", 2.0, 5.0, 1.0);
		Map<Mutator, List<Number>> mutators = new EnumMap<>(Mutator.class);
		mutators.put(Mutator.ALTERNATING, Arrays.asList(1, 2));
		AxialStepModelDocument axialStepModelDocument = new AxialStepModelDocument(scannableDocument, mutators);
		String document = serialiseDocument(axialStepModelDocument);
		assertThat(document, containsString("motor_x"));
		assertThat(document, containsString("\"alternating\":[1,2]"));
	}

	@Test
	public void deserialiseDocumentTest() {
		AxialStepModelDocument axialStepModelDocument = deserialiseDocument(getJsonDocument());
		Assert.assertEquals("motor_x", axialStepModelDocument.getScannable().getScannable());
		Assert.assertTrue(axialStepModelDocument.getMutators().containsKey(Mutator.ALTERNATING));
		Assert.assertTrue(axialStepModelDocument.getMutators().containsValue(Arrays.asList(1, 2)));
	}

	private AxialStepModelDocument deserialiseDocument(String json) {
		try {
			return objectMapper.readValue(json, AxialStepModelDocument.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String serialiseDocument(AxialStepModelDocument axialStepModelDocument) {
		try {
			return new ObjectMapper().writeValueAsString(axialStepModelDocument);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getJsonDocument() {

		return "{\"type\":\"AxialStep\","
				+ "\"scannable\":{\"scannable\":\"motor_x\",\"start\":2.0,\"stop\":5.0,\"step\":1.0},"
				+ "\"mutators\":{\"alternating\":[1,2]}}";
	}

}
