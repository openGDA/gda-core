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
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.points.models.IBoundsToFit;
import org.junit.Assert;
import org.junit.Test;

import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplate;
import uk.ac.diamond.daq.mapping.api.document.model.AcquisitionTemplateFactory;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.Trajectory;
import uk.ac.gda.api.acquisition.TrajectoryShape;
import uk.ac.gda.common.exception.GDAException;

public class TwoDimensionPointScanpathDocumentTest extends DocumentTestBase {


	@Test
	public void serialiseDocumentTest() throws GDAException {
		List<ScannableTrackDocument> scannableTrackDocuments = new ArrayList<>();

		var trajectory = new ScannableTrackDocument();
		trajectory.setScannable("motor_x");
		trajectory.setStart(2.0);
		trajectory.setStop(2.0);
		trajectory.setPoints(1);
		trajectory.setAlternating(true);
		scannableTrackDocuments.add(trajectory);

		trajectory = new ScannableTrackDocument();
		trajectory.setScannable("motor_y");
		trajectory.setStart(1.0);
		trajectory.setStop(1.0);
		trajectory.setPoints(1);
		scannableTrackDocuments.add(trajectory);

		assertThat(scannableTrackDocuments.get(0).calculatedStep(), is(equalTo(0.0)));
		assertThat(scannableTrackDocuments.get(1).calculatedStep(), is(equalTo(0.0)));
		assertThat(scannableTrackDocuments.get(0).calculatedPoints(), is(equalTo(1)));
		assertThat(scannableTrackDocuments.get(0).calculatedPoints(), is(equalTo(1)));

		ScanpathDocument modelDocument = new ScanpathDocument(List.of(new Trajectory(scannableTrackDocuments, TrajectoryShape.TWO_DIMENSION_POINT)));
		String document = serialiseDocument(modelDocument);
		assertThat(document, containsString("motor_x"));
		assertThat(document, containsString("motor_y"));
		assertThat(document, containsString("\"alternating\" : true"));
	}

	@Test
	public void serialiseDocumentTestWithBoundsFit() {
		try {
			System.setProperty(IBoundsToFit.PROPERTY_DEFAULT_BOUNDS_FIT, "true");
			List<ScannableTrackDocument> scannableTrackDocuments = new ArrayList<>();

			var trajectory = new ScannableTrackDocument();
			trajectory.setScannable("motor_x");
			trajectory.setStart(2.0);
			trajectory.setStop(2.0);
			trajectory.setPoints(1);
			trajectory.setAlternating(true);
			scannableTrackDocuments.add(trajectory);

			trajectory = new ScannableTrackDocument();
			trajectory.setScannable("motor_y");
			trajectory.setStart(1.0);
			trajectory.setStop(1.0);
			trajectory.setPoints(1);
			scannableTrackDocuments.add(trajectory);

			assertThat(scannableTrackDocuments.get(0).calculatedStep(), is(equalTo(0.0)));
			assertThat(scannableTrackDocuments.get(1).calculatedStep(), is(equalTo(0.0)));
			assertThat(scannableTrackDocuments.get(0).calculatedPoints(), is(equalTo(1)));
			assertThat(scannableTrackDocuments.get(0).calculatedPoints(), is(equalTo(1)));
		} finally {
			System.clearProperty(IBoundsToFit.PROPERTY_DEFAULT_BOUNDS_FIT);
		}
	}

	@Test
	public void deserialiseDocumentTest() throws GDAException {
		ScanpathDocument scan = deserialiseDocument("test/resources/TwoAxisPointSingleModelDocument.json", ScanpathDocument.class);

		var trajectories = scan.getTrajectories();
		Assert.assertEquals(1, trajectories.size());

		var trajectory = trajectories.get(0);
		Assert.assertEquals(2, trajectory.getAxes().size());

		var axis = trajectory.getAxes().get(0);
		Assert.assertEquals("motor_x", axis.getScannable());
		Assert.assertEquals(2.0, axis.getStart(), 0.0);
		Assert.assertEquals(2.0, axis.getStop(), 0.0);
		Assert.assertEquals(0, axis.getPoints(), 0.0);

		axis = trajectory.getAxes().get(1);
		Assert.assertEquals("motor_y", axis.getScannable());
		Assert.assertEquals(5.0, axis.getStart(), 0.0);
		Assert.assertEquals(5.0, axis.getStop(), 0.0);
		Assert.assertEquals(0, axis.getPoints(), 0.0);
	}

	@Test
	public void validDocumentTest() throws GDAException {
		ScanpathDocument modelDocument = deserialiseDocument("test/resources/TwoAxisPointSingleModelDocument.json", ScanpathDocument.class);
		AcquisitionTemplate acquisitionTemplate = AcquisitionTemplateFactory.buildModelDocument(modelDocument).get(0);

		acquisitionTemplate.validate();
	}

	@Test
	public void invalidDocumentTest() throws GDAException {
		ScanpathDocument modelDocument = deserialiseDocument("test/resources/TwoAxisPointSingleModelInvalidDocument.json", ScanpathDocument.class);
		assertThrows(GDAException.class, () -> AcquisitionTemplateFactory.buildModelDocument(modelDocument));
	}
}