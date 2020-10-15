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

package uk.ac.gda.api.acquisition.parameters;

import static gda.TestHelpers.deserialiseDocument;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.gda.api.exception.GDAException;

public class ScannablePositionDocumentTest {

	private static final String MOTOR_X = "motor_x";
	private static final String CLOSE = "CLOSE";

	@Test
	public void deserialiseScannablePositionDocumentOnlyType() throws GDAException {
		ScannablePositionDocument document = deserialiseDocument(
				"test/resources/ScannablePositionDocumentTypeOnly.json", ScannablePositionDocument.class);

		assertEquals(0.0, document.getPosition(), 0);
		assertNull(document.getScannable());
		assertNull(document.getAxis());
		assertNull(document.getLabelledPosition());
		assertEquals(ScannablePositionDocument.ValueType.NUMERIC, document.getValueType());
	}

	@Test(expected = GDAException.class)
	public void deserialiseScannablePositionDocumentWrongType() throws GDAException {
		deserialiseDocument(
				"test/resources/ScannablePositionDocumentWrongType.json", ScannablePositionDocument.class);
	}

	@Test
	public void deserialiseScannablePositionDocumentNoType() throws GDAException {
		ScannablePositionDocument document = deserialiseDocument(
				"test/resources/ScannablePositionDocumentNoType.json", ScannablePositionDocument.class);

		assertEquals(2.0, document.getPosition(), 0);
		assertEquals("motor_y", document.getScannable());
		assertEquals("Y", document.getAxis());
		assertEquals(CLOSE, document.getLabelledPosition());
		assertNull(document.getValueType());
	}

	@Test
	public void serialiseScannablePositionDocumentWithPosition() throws IOException {

		ScannablePositionDocument.Builder builder = new ScannablePositionDocument.Builder();
		builder.withScannable(MOTOR_X);
		builder.withAxis("X");
		builder.withPosition(2.0);
		builder.withLabelledPosition(CLOSE);

		String json = new ObjectMapper().writeValueAsString(builder.build());

		assertThat(json, containsString("\"scannable\":\"motor_x\""));
		assertThat(json, containsString("\"axis\":\"X\""));
		assertThat(json, containsString("\"position\":2.0"));
		assertThat(json, containsString("\"labelledPosition\":\"CLOSE\""));
	}
}
