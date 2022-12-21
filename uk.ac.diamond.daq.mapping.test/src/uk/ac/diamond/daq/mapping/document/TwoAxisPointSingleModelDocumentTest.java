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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.points.models.IBoundsToFit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import gda.mscan.element.Mutator;
import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplate;
import uk.ac.diamond.daq.mapping.api.document.model.AcquisitionTemplateFactory;
import uk.ac.diamond.daq.mapping.api.document.model.AxialStepModelDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.gda.api.acquisition.AcquisitionTemplateType;
import uk.ac.gda.common.exception.GDAException;

/**
 * Tests for the {@link AxialStepModelDocument}
 *
 * @author Maurizio Nagni
 */
public class TwoAxisPointSingleModelDocumentTest extends DocumentTestBase {

	@Before
	public void before() {
	}

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


		Map<Mutator, List<Number>> mutators = new EnumMap<>(Mutator.class);
		mutators.put(Mutator.ALTERNATING, Arrays.asList(1, 2));
		ScanpathDocument modelDocument = new ScanpathDocument(AcquisitionTemplateType.TWO_DIMENSION_POINT, scannableTrackDocuments);
		String document = serialiseDocument(modelDocument);
		assertThat(document, containsString("motor_x"));
		assertThat(document, containsString("motor_y"));
		assertThat(document, containsString("\"alternating\" : true"));
	}

	@Test
	public void serialiseDocumentTestWithBoundsFit() {
		try {
			System.setProperty(IBoundsToFit.PROPERTY_NAME_BOUNDS_TO_FIT, "true");
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
			System.clearProperty(IBoundsToFit.PROPERTY_NAME_BOUNDS_TO_FIT);
		}
	}

	@Test
	public void deserialiseDocumentTest() throws GDAException {
		ScanpathDocument modelDocument = deserialiseDocument("test/resources/TwoAxisPointSingleModelDocument.json",
				ScanpathDocument.class);

		ScannableTrackDocument std = modelDocument.getScannableTrackDocuments().get(0);
		Assert.assertEquals("motor_x", std.getScannable());
		Assert.assertEquals(2.0, std.getStart(), 0.0);
		Assert.assertEquals(2.0, std.getStop(), 0.0);
		Assert.assertEquals(0, std.getPoints(), 0.0);

		std = modelDocument.getScannableTrackDocuments().get(1);
		Assert.assertEquals("motor_y", std.getScannable());
		Assert.assertEquals(5.0, std.getStart(), 0.0);
		Assert.assertEquals(5.0, std.getStop(), 0.0);
		Assert.assertEquals(0, std.getPoints(), 0.0);
	}

	@Test
	public void validDocumentTest() throws GDAException {
		ScanpathDocument modelDocument = deserialiseDocument("test/resources/TwoAxisPointSingleModelDocument.json",
				ScanpathDocument.class);
		AcquisitionTemplate acquisitionTemplate = AcquisitionTemplateFactory.buildModelDocument(modelDocument);

		acquisitionTemplate.validate();
	}

	@Test(expected = GDAException.class)
	public void invalidDocumentTest() throws GDAException {
		ScanpathDocument modelDocument = deserialiseDocument("test/resources/TwoAxisPointSingleModelInvalidDocument.json",
				ScanpathDocument.class);
		AcquisitionTemplate acquisitionTemplate = AcquisitionTemplateFactory.buildModelDocument(modelDocument);

		acquisitionTemplate.validate();
	}
}