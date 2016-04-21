/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.mapping.impl.MandelbrotPreprocessor;

public class MandelbrotPreprocessorTest {

	private static final String CONFIGURED_REAL_AXIS_NAME = "x_motor";
	private static final String CONFIGURED_IMAGINARY_AXIS_NAME = "y_motor";

	private MandelbrotPreprocessor preprocessor;

	@Before
	public void setUp() throws Exception {
		preprocessor = new MandelbrotPreprocessor();
		preprocessor.setRealAxisName(CONFIGURED_REAL_AXIS_NAME);
		preprocessor.setImaginaryAxisName(CONFIGURED_IMAGINARY_AXIS_NAME);
	}

	@After
	public void tearDown() throws Exception {
		preprocessor = null;
	}

	@Test
	public void axisNamesShouldBeReplaced() throws Exception {
		ScanRequest<?> scanRequest = new ScanRequest<>();
		MandelbrotModel mandelbrotModel = new MandelbrotModel();
		mandelbrotModel.setRealAxisName("stage_x");
		mandelbrotModel.setImaginaryAxisName("stage_y");
		scanRequest.putDetector("mandelbrot_detector", mandelbrotModel);

		ScanRequest<?> expectedScanRequest = new ScanRequest<>();
		MandelbrotModel expectedModel = new MandelbrotModel();
		expectedModel.setRealAxisName(CONFIGURED_REAL_AXIS_NAME);
		expectedModel.setImaginaryAxisName(CONFIGURED_IMAGINARY_AXIS_NAME);
		expectedScanRequest.putDetector("mandelbrot_detector", expectedModel);

		// Before preprocessing the scan requests should be different
		assertNotEquals(expectedScanRequest, scanRequest);

		preprocessor.preprocess(scanRequest);

		// After preprocessing they should be the same
		assertEquals(expectedScanRequest, scanRequest);
	}
}
