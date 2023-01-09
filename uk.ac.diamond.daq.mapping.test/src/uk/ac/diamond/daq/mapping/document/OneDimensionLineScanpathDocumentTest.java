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

import static gda.configuration.properties.LocalProperties.GDA_CONFIG;
import static gda.configuration.properties.LocalProperties.GDA_PROPERTIES_FILE;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.List;

import org.eclipse.scanning.api.points.models.IBoundsToFit;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument.Axis;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.Trajectory;
import uk.ac.gda.common.exception.GDAException;


public class OneDimensionLineScanpathDocumentTest extends DocumentTestBase {

	private static final String MOTOR_X = "motor_x";

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(GDA_CONFIG, "test/resources/defaultContext");
        System.setProperty(GDA_PROPERTIES_FILE, "test/resources/defaultContext/properties/_common/common_instance_java.properties");
	}

	@Test
	public void serialiseDocumentTest() throws GDAException {
		var modelDocument = new ScanpathDocument(List.of(new Trajectory(getAxisDocument())));
		String document = serialiseDocument(modelDocument);
		assertThat(document, containsString(MOTOR_X));
		assertThat(document, containsString("\"axis\" : \"X\""));
		assertThat(document, containsString("\"alternating\" : true"));
	}

	@Test
	public void withBoundsNotFit() {
		final ScannableTrackDocument modelDocument = getAxisDocument();
		var expectedStep = (modelDocument.getStop() - modelDocument.getStart()) / (modelDocument.getPoints() - 1);
		assertThat(modelDocument.calculatedStep(), is(equalTo(expectedStep)));
	}

	@Test
	public void withBoundsFit() {
		try {
			System.setProperty(IBoundsToFit.PROPERTY_DEFAULT_BOUNDS_FIT, "true");
			final ScannableTrackDocument modelDocument = getAxisDocument();
			var expectedStep = (modelDocument.getStop() - modelDocument.getStart()) / modelDocument.getPoints();
			assertThat(modelDocument.calculatedStep(), is(equalTo(expectedStep)));
		} finally {
			System.clearProperty(IBoundsToFit.PROPERTY_DEFAULT_BOUNDS_FIT);
		}
	}

	private ScannableTrackDocument getAxisDocument() {
		var trajectory = new ScannableTrackDocument();
		trajectory.setAxis(Axis.X);
		trajectory.setScannable(MOTOR_X);
		trajectory.setStart(2.0);
		trajectory.setStop(10.5);
		trajectory.setPoints(5);
		trajectory.setAlternating(true);
		return trajectory;
	}

	@Test
	public void deserialiseDocumentTest() throws GDAException {
		ScanpathDocument modelDocument = deserialiseDocument("test/resources/AxialStepModelDocument.json",	ScanpathDocument.class);

		var axis = modelDocument.getTrajectories().get(0).getAxes().get(0);
		Assert.assertEquals(MOTOR_X, axis.getScannable());
		Assert.assertTrue(axis.isAlternating());
	}
}
