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
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import org.eclipse.scanning.api.points.models.IBoundsToFit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument.Axis;
import uk.ac.gda.common.exception.GDAException;

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
		var trajectory = new ScannableTrackDocument();
		trajectory.setScannable("motor_x");
		trajectory.setAxis(Axis.X);
		trajectory.setStart(2.0);
		trajectory.setStop(5.0);
		trajectory.setPoints(5);
		String document = serialiseDocument(trajectory);
		assertThat(document, containsString("motor_x"));
		assertThat(document, containsString("\"axis\" : \"X\""));
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
		Assert.assertEquals(0, scannableDocument.getPoints(), 0.0);
	}

	@Test
	public void deserialiseDocumentTestWithPoints() throws GDAException {
		ScannableTrackDocument scannableDocument = deserialiseDocument("test/resources/ScannableTrackDocumentWithPoints.json",
				ScannableTrackDocument.class);
		Assert.assertEquals("motor_y", scannableDocument.getScannable());
		Assert.assertEquals(0.1, scannableDocument.getStart(), 0.0);
		Assert.assertEquals(0.2, scannableDocument.getStop(), 0.0);
		Assert.assertEquals(0, scannableDocument.getStep(), 0.0);
		Assert.assertEquals(10, scannableDocument.getPoints(), 0.0);
	}

	@Test
	public void axisPointsTest() {
		var trajectory = new ScannableTrackDocument();
		trajectory.setScannable("motor_x");
		trajectory.setStart(1.0);
		trajectory.setStop(5.0);
		trajectory.setStep(1.0);
		Assert.assertEquals(0, trajectory.getPoints(), 0.0);
	}

	@Test
	public void pointsWithinLimitsTest() {
		var trajectory = new ScannableTrackDocument();
		trajectory.setScannable("gts_theta");
		trajectory.setStart(0);
		trajectory.setStop(180);
		trajectory.setStep(0.36);
		assertThat(trajectory.calculatedPoints(), is(equalTo(501)));
	}

	@Test
	public void pointsWithinLimitsBoundsFitTest() {
		try {
			System.setProperty(IBoundsToFit.PROPERTY_DEFAULT_BOUNDS_FIT, "true");
			var trajectory = new ScannableTrackDocument();
			trajectory.setScannable("gts_theta");
			trajectory.setStart(0);
			trajectory.setStop(180);
			trajectory.setStep(0.36);
			assertThat(trajectory.calculatedPoints(), is(equalTo(500)));
		} finally {
			System.clearProperty(IBoundsToFit.PROPERTY_DEFAULT_BOUNDS_FIT);
		}
	}
}
