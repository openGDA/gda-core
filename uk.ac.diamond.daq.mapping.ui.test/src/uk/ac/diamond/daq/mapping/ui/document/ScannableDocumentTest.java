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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;

/**
 * Tests for the {@link ScannableTrackDocument}
 *
 * @author Maurizio Nagni
 */
public class ScannableDocumentTest {

	private final ObjectMapper objectMapper = DocumentMapper.getObjectMapper();

	@Before
	public void before() {
	}

	@Test
	public void serialiseDocumentTest() {
		ScannableTrackDocument scannableDocument = new ScannableTrackDocument("motor_x", 2.0, 5.0, 1.0);
		String document = serialiseDocument(scannableDocument);
		assertThat(document, containsString("motor_x"));
	    assertThat(document, containsString("\"start\":2.0"));
	    assertThat(document, containsString("\"stop\":5.0"));
	    assertThat(document, containsString("\"step\":1.0"));
	}

	@Test
	public void deserialiseDocumentTest() {
		ScannableTrackDocument scannableDocument = deserialiseDocument(getJsonDocument());
		Assert.assertEquals("motor_y", scannableDocument.getScannable());
		Assert.assertEquals(0.1, scannableDocument.getStart(), 0.0);
		Assert.assertEquals(0.2, scannableDocument.getStop(), 0.0);
		Assert.assertEquals(0.3, scannableDocument.getStep(), 0.0);
	}

	@Test
	public void axisPointsTest() {
		ScannableTrackDocument scannableDocument = new ScannableTrackDocument("motor_x", 2.0, 5.0, 1.0);
		Assert.assertEquals(3.0, scannableDocument.getAxisPoints(), 0.0);
	}

	private ScannableTrackDocument deserialiseDocument(String json) {
		try {
			return objectMapper.readValue(json, ScannableTrackDocument.class);
		} catch (IOException e) {

		}
		return null;
	}

	private String serialiseDocument(ScannableTrackDocument scannableDocument) {
		try {
			return new ObjectMapper().writeValueAsString(scannableDocument);
		} catch (IOException e) {

		}
		return null;
	}

	private String getJsonDocument() {
		return "{\"scannable\": \"motor_y\", "
				+ "\"start\": 0.1,"
				+ "\"stop\": 0.2,"
				+ "\"step\": 0.3 }";
	}

}
