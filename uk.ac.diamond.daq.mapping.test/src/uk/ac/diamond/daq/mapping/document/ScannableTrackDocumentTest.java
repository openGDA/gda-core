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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.gda.api.exception.GDAException;

/**
 * Tests for the {@link ScannableTrackDocument}
 *
 * @author Maurizio Nagni
 */
public class ScannableTrackDocumentTest extends DocumentTestBase {

	@Before
	public void before() {
	}

	@Test
	public void serialiseDocumentTest() throws GDAException {
		ScannableTrackDocument.Builder builder = new ScannableTrackDocument.Builder();
		builder.withScannable("motor_x");
		builder.withAxis("x");
		builder.withStart(2.0);
		builder.withStop(5.0);
		builder.withPoints(5);
		ScannableTrackDocument scannableDocument = builder.build();
		String document = serialiseDocument(scannableDocument);
		assertThat(document, containsString("motor_x"));
		assertThat(document, containsString("\"axis\" : \"x\""));
		assertThat(document, containsString("\"start\" : 2.0"));
		assertThat(document, containsString("\"stop\" : 5.0"));
		assertThat(document, containsString("\"points\" : 5"));
	}

	@Test
	public void deserialiseDocumentWithStepTest() throws GDAException {
		ScannableTrackDocument scannableDocument = deserialiseDocument("test/resources/ScannableTrackDocumentWithStep.json",
				ScannableTrackDocument.class);
		Assert.assertEquals("motor_y", scannableDocument.getScannable());
		Assert.assertEquals(0.1, scannableDocument.getStart(), 0.0);
		Assert.assertEquals(0.2, scannableDocument.getStop(), 0.0);
		Assert.assertEquals(0.5, scannableDocument.getStep(), 0.0);
		Assert.assertEquals(Integer.MIN_VALUE, scannableDocument.getPoints(), 0.0);
	}

	@Test
	public void deserialiseDocumentTestWithPoints() throws GDAException {
		ScannableTrackDocument scannableDocument = deserialiseDocument("test/resources/ScannableTrackDocumentWithPoints.json",
				ScannableTrackDocument.class);
		Assert.assertEquals("motor_y", scannableDocument.getScannable());
		Assert.assertEquals(0.1, scannableDocument.getStart(), 0.0);
		Assert.assertEquals(0.2, scannableDocument.getStop(), 0.0);
		Assert.assertEquals(Double.MIN_VALUE, scannableDocument.getStep(), 0.0);
		Assert.assertEquals(10, scannableDocument.getPoints(), 0.0);
	}

	@Test
	public void axisPointsTest() {
		ScannableTrackDocument.Builder builder = new ScannableTrackDocument.Builder();
		builder.withScannable("motor_x");
		builder.withStart(1.0);
		builder.withStop(5.0);
		builder.withStep(1.0);
		ScannableTrackDocument scannableDocument = builder.build();
		Assert.assertEquals(Integer.MIN_VALUE, scannableDocument.getPoints(), 0.0);
	}
}
