/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.test.scan.preprocess;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.process.IPreprocessor;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.preprocess.ExamplePreprocessor;
import org.eclipse.scanning.test.utilities.scan.mock.MockDetectorModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PreprocessTest {

	protected IPreprocessor preprocessor;

	@BeforeEach
	public void before() {
		preprocessor = new ExamplePreprocessor();
	}

	@Test
	public void testSimplePreprocess() throws Exception {

		ScanRequest req = createStepRequest();
		req = preprocessor.preprocess(req);
		assertNotNull(req);

		AxialStepModel step = (AxialStepModel)req.getCompoundModel().getModels().toArray()[0];
		assertTrue(step.getName().equals("xfred"));
	}

	@Test
	public void testGridPreprocess() throws Exception {

		ScanRequest req = createGridRequest();
		req = preprocessor.preprocess(req);
		assertNotNull(req);

		TwoAxisGridPointsModel grid = (TwoAxisGridPointsModel)req.getCompoundModel().getModels().toArray()[0];
		assertTrue(grid.getxAxisName().equals("xfred"));
		assertTrue(grid.getyAxisName().equals("yfred"));
	}

	@Test
	public void testGridStepPreprocess() throws Exception {

		ScanRequest req = createStepGridRequest(5);
		req = preprocessor.preprocess(req);
		assertNotNull(req);

		// TODO
	}

	private ScanRequest createStepRequest() throws IOException {

		final ScanRequest req = new ScanRequest();
		req.setCompoundModel(new CompoundModel(new AxialStepModel("fred", 0, 9, 1)));
		req.setMonitorNamesPerPoint(Arrays.asList("monitor"));

		final MockDetectorModel dmodel = new MockDetectorModel();
		dmodel.setName("detector");
		dmodel.setExposureTime(0.1);
		req.putDetector("detector", dmodel);

		return req;
	}

	private ScanRequest createStepGridRequest(int outerScanNum) throws IOException {


		final ScanRequest req = new ScanRequest();
		// Create a grid scan model
		BoundingBox box = new BoundingBox();
		box.setxAxisStart(0);
		box.setyAxisStart(0);
		box.setxAxisLength(3);
		box.setyAxisLength(3);

		TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel();
		gmodel.setyAxisPoints(5);
		gmodel.setxAxisPoints(5);
		gmodel.setBoundingBox(box);
		gmodel.setxAxisName("xNex");
		gmodel.setyAxisName("yNex");

		// 2 models
		List<IScanPointGeneratorModel> models = new ArrayList<>(outerScanNum+1);
		for (int i = 0; i < outerScanNum; i++) {
			models.add(new AxialStepModel("neXusScannable"+i, 1, 2, 1));
		}
		models.add(gmodel);
		req.setCompoundModel(new CompoundModel(models));
		req.setMonitorNamesPerPoint(Arrays.asList("monitor"));

		final File tmp = File.createTempFile("scan_servlet_test", ".nxs");
		tmp.deleteOnExit();
		req.setFilePath(tmp.getAbsolutePath()); // TODO This will really come from the scan file service which is not written.

		// 2 detectors
		final MandelbrotModel mandyModel = new MandelbrotModel();
		mandyModel.setName("mandelbrot");
		mandyModel.setRealAxisName("xNex");
		mandyModel.setImaginaryAxisName("yNex");
		mandyModel.setExposureTime(0.01);
		req.putDetector("mandelbrot", mandyModel);

		final MockDetectorModel dmodel = new MockDetectorModel();
		dmodel.setName("detector");
		dmodel.setExposureTime(0.01);
		req.putDetector("detector", dmodel);

		return req;
	}

	private ScanRequest createGridRequest() throws IOException {

		final ScanRequest req = new ScanRequest();
		// Create a grid scan model
		BoundingBox box = new BoundingBox();
		box.setxAxisStart(0);
		box.setyAxisStart(0);
		box.setxAxisLength(3);
		box.setyAxisLength(3);

		TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel();
		gmodel.setyAxisPoints(5);
		gmodel.setxAxisPoints(5);
		gmodel.setBoundingBox(box);
		gmodel.setxAxisName("xNex");
		gmodel.setyAxisName("yNex");

		req.setCompoundModel(new CompoundModel(gmodel));
		req.setMonitorNamesPerPoint(Arrays.asList("monitor"));

		final File tmp = File.createTempFile("scan_servlet_test", ".nxs");
		tmp.deleteOnExit();
		req.setFilePath(tmp.getAbsolutePath()); // TODO This will really come from the scan file service which is not written.

		final MandelbrotModel mandyModel = new MandelbrotModel();
		mandyModel.setName("mandelbrot");
		mandyModel.setRealAxisName("xNex");
		mandyModel.setImaginaryAxisName("yNex");
		req.putDetector("mandelbrot", mandyModel);

		return req;
	}

	private void fillParameters(Map<String, Object> config, long configureSleep, int imageCount) throws Exception {

		// Params for driving mock mode
		config.put("nframes", imageCount); // IMAGE_COUNT images to write
		config.put("shape", new int[]{64,64});

		final File temp = File.createTempFile("testingFile", ".hdf5");
		temp.deleteOnExit();
		config.put("file", temp.getAbsolutePath());

		// The exposure is in seconds
		config.put("exposure", 0.5);

		double csleep = configureSleep/1000d;
		if (configureSleep>0) config.put("configureSleep", csleep); // Sleeps during configure

	}
}
