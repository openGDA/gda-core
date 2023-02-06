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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.Trajectory;
import uk.ac.gda.api.acquisition.TrajectoryShape;
import uk.ac.gda.common.exception.GDAException;


public class TwoDimensionLineScanpathDocumentTest extends DocumentTestBase {

	@Test
	public void serialiseDocumentTest() throws GDAException {
		List<ScannableTrackDocument> scannableTrackDocuments = new ArrayList<>();

		var path = new ScannableTrackDocument();
		path.setScannable("motor_x");
		path.setStart(2.0);
		path.setStop(2.0);
		path.setPoints(5);
		path.setAlternating(true);
		scannableTrackDocuments.add(path);

		path = new ScannableTrackDocument();
		path.setScannable("motor_y");
		path.setStart(1.0);
		path.setStop(1.0);
		path.setPoints(10);
		scannableTrackDocuments.add(path);

		ScanpathDocument modelDocument = new ScanpathDocument(List.of(new Trajectory(scannableTrackDocuments, TrajectoryShape.TWO_DIMENSION_LINE)));
		String document = serialiseDocument(modelDocument);
		assertThat(document, containsString("motor_x"));
		assertThat(document, containsString("motor_y"));
		assertThat(document, containsString("\"alternating\" : true"));
	}

	@Test
	public void deserialiseDocumentTest() throws GDAException {
		ScanpathDocument modelDocument = deserialiseDocument("test/resources/TwoAxisLinePointsModelDocument.json", ScanpathDocument.class);

		var trajectory = modelDocument.getTrajectories().iterator().next();
		assertThat(trajectory.getAxes().size(), equalTo(2));

		var axis = trajectory.getAxes().get(0);
		Assert.assertEquals("motor_x", axis.getScannable());
		Assert.assertEquals(1.0, axis.getStep(), 0.0);
		Assert.assertEquals(0, axis.getPoints(), 0.0);

		Assert.assertTrue(axis.isAlternating());

		axis = trajectory.getAxes().get(1);
		Assert.assertEquals("motor_y", axis.getScannable());
		Assert.assertEquals(0, axis.getStep(), 0.0);
		Assert.assertEquals(2, axis.getPoints(), 0.0);
	}
}