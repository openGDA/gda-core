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
package org.eclipse.scanning.test.command;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolylineROI;
import org.eclipse.scanning.api.device.models.ClusterProcessingModel;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.OneAxisPointRepeatedModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsRandomOffsetModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;
import org.eclipse.scanning.command.factory.PyExpressionFactory;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.junit.Before;
import org.junit.Test;


public class PyExpresserTest {

	private PyExpressionFactory factory;

	@Before
	public void services() {
		ServiceTestHelper.setupServices();
		this.factory = new PyExpressionFactory();
	}

	@Test
	public void testScanRequestWithMonitor_Step() throws Exception {

		AxialStepModel smodel = new AxialStepModel();
		smodel.setStart(0);
		smodel.setStop(10);
		smodel.setStep(1);
		smodel.setName("fred");


		Collection<String> monitors = new ArrayList<>();
		monitors.add("someMonitor");

		ScanRequest request = new ScanRequest();
		request.setCompoundModel(new CompoundModel(smodel));
		request.setMonitorNamesPerPoint(monitors);

		assertEquals(  // Concise.
				"mscan(step('fred', 0.0, 10.0, 1.0, False, True), 'someMonitor')",
				factory.pyExpress(request, false));
		assertEquals(  // Verbose.
				"mscan(path=[step(axis='fred', start=0.0, stop=10.0, step=1.0, alternating=False, continuous=True)], monitorsPerPoint=['someMonitor'])",
				factory.pyExpress(request, true));
	}

	@Test
	public void testGridModelWithPolygonalROI() throws Exception {

		BoundingBox bbox = new BoundingBox();
		bbox.setxAxisStart(0);
		bbox.setyAxisStart(1);
		bbox.setxAxisLength(10);
		bbox.setyAxisLength(11);

		TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel();
		gmodel.setxAxisName("myFast");
		gmodel.setyAxisName("mySlow");
		gmodel.setBoundingBox(bbox);
		gmodel.setxAxisPoints(3);
		gmodel.setyAxisPoints(4);
		gmodel.setAlternating(true);
		gmodel.setContinuous(true);

		List<PointROI> points = new ArrayList<>();
		points.add(new PointROI(0, 0));
		points.add(new PointROI(0, 1));
		points.add(new PointROI(1, 0));
		PolygonalROI roi = new PolygonalROI(new PolylineROI(points));

		assertEquals(  // Concise.
				"grid(('myFast', 'mySlow'), (0, 1), (10, 12), count=(3, 4), True, True, False, poly((0, 0), (0, 1), (1, 0)))",
				factory.pyExpress(gmodel, Arrays.asList(roi), false));
		assertEquals(  // Verbose.
				"grid(axes=('myFast', 'mySlow'), start=(0, 1), stop=(10, 12), count=(3, 4), alternating=True, continuous=True, verticalOrientation=False, roi=[poly((0, 0), (0, 1), (1, 0))])",
				factory.pyExpress(gmodel, Arrays.asList(roi), true));
	}

	@Test
	public void testRandomOffsetGridModel() throws Exception {
		BoundingBox bbox = new BoundingBox();
		bbox.setxAxisStart(0);
		bbox.setyAxisStart(1);
		bbox.setxAxisLength(10);
		bbox.setyAxisLength(11);

		TwoAxisGridPointsRandomOffsetModel model = new TwoAxisGridPointsRandomOffsetModel();
		model.setxAxisName("myFast");
		model.setyAxisName("mySlow");
		model.setBoundingBox(bbox);
		model.setxAxisPoints(3);
		model.setyAxisPoints(4);
		model.setAlternating(true);
		model.setContinuous(true);
		model.setSeed(5);
		model.setOffset(10.0);

		String expectedConcise = "random_offset_grid(('myFast', 'mySlow'), (0, 1), (10, 12), (3, 4), True, True, False)";
		String expectedVerbose = "random_offset_grid(axes=('myFast', 'mySlow'), start=(0, 1), stop=(10, 12), count=(3, 4), alternating=True, continuous=True, verticalOrientation=False)";
		assertEquals(expectedConcise, factory.pyExpress(model, false));
		assertEquals(expectedVerbose, factory.pyExpress(model, true));
	}

	@Test
	public void testSpiralModel() throws Exception {
		BoundingBox bbox = new BoundingBox();
		bbox.setxAxisStart(0);
		bbox.setyAxisStart(1);
		bbox.setxAxisLength(10);
		bbox.setyAxisLength(11);

		TwoAxisSpiralModel model = new TwoAxisSpiralModel();
		model.setxAxisName("myFast");
		model.setyAxisName("mySlow");
		model.setBoundingBox(bbox);
		model.setScale(1.5);
		model.setContinuous(false);

		CircularROI croi = new CircularROI();
		ScanRequest request = new ScanRequest();

		CompoundModel cmodel = new CompoundModel();
		cmodel.setData(model, croi);
		request.setCompoundModel(cmodel);

		String expectedConcise = "spiral(('myFast', 'mySlow'), (0, 1), (10, 12), 1.5, False, False, circ((0, 0), 1))";
		assertEquals(expectedConcise, factory.pyExpress(model, Arrays.asList(croi), false));

		String expectedVerbose = "spiral(axes=('myFast', 'mySlow'), start=(0, 1), stop=(10, 12), scale=1.5, alternating=False, continuous=False, roi=[circ(origin=(0, 0), radius=1)])";
		assertEquals(expectedVerbose, factory.pyExpress(model, Arrays.asList(croi), true));
	}

	@Test
	public void testLissajousModel() throws Exception {
		BoundingBox bbox = new BoundingBox();
		bbox.setxAxisStart(0);
		bbox.setyAxisStart(1);
		bbox.setxAxisLength(10);
		bbox.setyAxisLength(11);

		TwoAxisLissajousModel model = new TwoAxisLissajousModel();
		model.setxAxisName("myFast");
		model.setyAxisName("mySlow");
		model.setBoundingBox(bbox);
		model.setA(1);
		model.setB(0.25);
		model.setPoints(100);
		model.setContinuous(false);

		String expectedConcise = "lissajous(('myFast', 'mySlow'), (0, 1), (10, 12), 1.0, 0.25, 100, False, False)";
		String expectedVerbose = "lissajous(axes=('myFast', 'mySlow'), start=(0, 1), stop=(10, 12), a=1.0, b=0.25, points=100, alternating=False, continuous=False)";
		assertEquals(expectedConcise, factory.pyExpress(model, false));
		assertEquals(expectedVerbose, factory.pyExpress(model, true));
	}

	@Test
	public void testScanRequestWithMonitor_Repeat()
			throws Exception {

		OneAxisPointRepeatedModel rmodel = new OneAxisPointRepeatedModel();
		rmodel.setCount(10);
		rmodel.setValue(2.2);
		rmodel.setSleep(25);
		rmodel.setName("fred");

		Collection<String> monitors = new ArrayList<>();
		monitors.add("someMonitor");

		ScanRequest request = new ScanRequest();
		request.setCompoundModel(new CompoundModel(rmodel));
		request.setMonitorNamesPerPoint(monitors);

		assertEquals(  // Concise.
				"mscan(repeat('fred', 10, 2.2, 25), 'someMonitor')",
				factory.pyExpress(request, false));
		assertEquals(  // Verbose.
				"mscan(path=[repeat(axis='fred', count=10, value=2.2, sleep=25)], monitorsPerPoint=['someMonitor'])",
				factory.pyExpress(request, true));
	}


	@Test
	public void testScanRequestWithROI()
			throws Exception {

		BoundingBox bbox = new BoundingBox();
		bbox.setxAxisStart(0);
		bbox.setyAxisStart(1);
		bbox.setxAxisLength(10);
		bbox.setyAxisLength(11);

		TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel();
		gmodel.setxAxisName("myFast");
		gmodel.setyAxisName("mySlow");
		gmodel.setBoundingBox(bbox);
		gmodel.setxAxisPoints(3);
		gmodel.setyAxisPoints(4);
		gmodel.setAlternating(false);
		gmodel.setContinuous(true);

		CircularROI croi = new CircularROI();
		ScanRequest request = new ScanRequest();

		CompoundModel cmodel = new CompoundModel();
		cmodel.setData(gmodel, croi);
		request.setCompoundModel(cmodel);

		assertEquals(  // Concise.
				"mscan(grid(('myFast', 'mySlow'), (0, 1), (10, 12), count=(3, 4), False, True, False, circ((0, 0), 1)))",
				factory.pyExpress(request, false));
		assertEquals(  // Verbose.
				"mscan(path=[grid(axes=('myFast', 'mySlow'), start=(0, 1), stop=(10, 12), count=(3, 4), alternating=False, continuous=True, verticalOrientation=False, roi=[circ(origin=(0, 0), radius=1)])])",
				factory.pyExpress(request, true));
	}

	@Test
	public void testCompoundScanRequest()
			throws Exception {

		AxialStepModel smodel = new AxialStepModel();
		smodel.setStart(0);
		smodel.setStop(10);
		smodel.setStep(1);
		smodel.setName("fred");

		AxialArrayModel amodel = new AxialArrayModel();
		amodel.setName("fred");
		amodel.setPositions(0.1);

		ScanRequest request = new ScanRequest();
		request.setCompoundModel(new CompoundModel(smodel, amodel));

		assertEquals(  // Concise.
				"mscan([step('fred', 0.0, 10.0, 1.0, False, True), val('fred', 0.1)])",
				factory.pyExpress(request, false));
		assertEquals(  // Verbose.
				"mscan(path=[step(axis='fred', start=0.0, stop=10.0, step=1.0, alternating=False, continuous=True), array(axis='fred', values=[0.1])])",
				factory.pyExpress(request, true));
	}

	@Test
	public void testStepModel() throws Exception{

		AxialStepModel smodel = new AxialStepModel();
		smodel.setStart(0);
		smodel.setStop(10);
		smodel.setStep(1);
		smodel.setName("fred");

		assertEquals(  // Concise.
				"step('fred', 0.0, 10.0, 1.0, False, True)",
				factory.pyExpress(smodel, false));
		assertEquals(  // Verbose.
				"step(axis='fred', start=0.0, stop=10.0, step=1.0, alternating=False, continuous=True)",
				factory.pyExpress(smodel, true));
	}

	@Test
	public void testMultiStepModelNoSteps() throws Exception{

		AxialMultiStepModel mmodel = new AxialMultiStepModel();
		mmodel.setName("fred");

		assertEquals(  // Concise.
				"mstep('fred', [], False, False)",
				factory.pyExpress(mmodel, false));
		assertEquals(  // Verbose.
				"mstep(axis='fred', stepModels=[], alternating=False, continuous=False)",
				factory.pyExpress(mmodel, true));
	}

	@Test
	public void testMultiStepModelOneStep() throws Exception{

		AxialMultiStepModel mmodel = new AxialMultiStepModel();
		mmodel.setName("fred");

		AxialStepModel smodel = new AxialStepModel();
		smodel.setStart(0);
		smodel.setStop(10);
		smodel.setStep(1);
		smodel.setName("fred");
		mmodel.addModel(smodel);
		mmodel.setContinuous(true);

		assertEquals(  // Concise.
				"mstep('fred', [StepModel('fred', 0.0, 10.0, 1.0, False, True)], False, True)",
				factory.pyExpress(mmodel, false));
		assertEquals(  // Verbose.
				"mstep(axis='fred', stepModels=[StepModel(axis='fred', start=0.0, stop=10.0, step=1.0, alternating=False, continuous=True)], alternating=False, continuous=True)",
				factory.pyExpress(mmodel, true));
	}

	@Test
	public void testMultiStepModelTwoSteps() throws Exception{

		AxialMultiStepModel mmodel = new AxialMultiStepModel();
		mmodel.setName("fred");

		AxialStepModel smodel = new AxialStepModel();
		smodel.setStart(0);
		smodel.setStop(10);
		smodel.setStep(1);
		smodel.setName("fred");
		mmodel.addModel(smodel);

		smodel = new AxialStepModel();
		smodel.setStart(20);
		smodel.setStop(30);
		smodel.setStep(2);
		smodel.setName("bill");
		mmodel.addModel(smodel);

		assertEquals(  // Concise.
				"mstep('fred', [StepModel('fred', 0.0, 10.0, 1.0, False, True), StepModel('bill', 20.0, 30.0, 2.0, False, True)], False, False)",
				factory.pyExpress(mmodel, false));

		String expected = "mstep(axis='fred', stepModels=[StepModel(axis='fred', start=0.0, stop=10.0, step=1.0, alternating=False, continuous=True), StepModel(axis='bill', start=20.0, stop=30.0, step=2.0, alternating=False, continuous=True)], alternating=False, continuous=False)";
		String actual   = factory.pyExpress(mmodel, true);
		assertEquals(expected, actual);
	}



	@Test
	public void testGridModel() throws Exception {

		BoundingBox bbox = new BoundingBox();
		bbox.setxAxisStart(0);
		bbox.setyAxisStart(1);
		bbox.setxAxisLength(10);
		bbox.setyAxisLength(11);

		TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel();
		gmodel.setxAxisName("myFast");
		gmodel.setyAxisName("mySlow");
		gmodel.setBoundingBox(bbox);
		gmodel.setxAxisPoints(3);
		gmodel.setyAxisPoints(4);
		gmodel.setAlternating(true);
		gmodel.setContinuous(true);

		assertEquals(  // Concise.
				"grid(('myFast', 'mySlow'), (0, 1), (10, 12), count=(3, 4), True, True, False)",
				factory.pyExpress(gmodel, new ArrayList<>(), false));
		assertEquals(  // Verbose.
				"grid(axes=('myFast', 'mySlow'), start=(0, 1), stop=(10, 12), count=(3, 4), alternating=True, continuous=True, verticalOrientation=False)",
				factory.pyExpress(gmodel, new ArrayList<>(), true));
	}

	@Test
	public void testScanRequestWithProcessing() throws Exception {

		BoundingBox bbox = new BoundingBox();
		bbox.setxAxisStart(0);
		bbox.setyAxisStart(1);
		bbox.setxAxisLength(10);
		bbox.setyAxisLength(11);

		TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel();
		gmodel.setxAxisName("p");
		gmodel.setyAxisName("q");
		gmodel.setBoundingBox(bbox);
		gmodel.setxAxisPoints(2);
		gmodel.setyAxisPoints(2);
		gmodel.setContinuous(false);
		gmodel.setAlternating(false);

		ScanRequest request = new ScanRequest();
		request.setCompoundModel(new CompoundModel(Arrays.asList(gmodel)));
		Map<String,Object> detectors = new LinkedHashMap<>();
		detectors.put("mandelbrot", new MandelbrotModel("p", "q"));
		detectors.put("processing", new ClusterProcessingModel("processing", "mandelbrot", "/tmp/something.nxs"));
		request.setDetectors(detectors);

		String mscan = factory.pyExpress(request, false);
		String expected = "mscan(grid(('p', 'q'), (0, 1), (10, 12), count=(2, 2), False, False, False), [detector('mandelbrot', 0.1), detector('processing', -1.0)])";
		assertEquals(expected, mscan);

		mscan = factory.pyExpress(request, true);
		expected = "mscan(path=[grid(axes=('p', 'q'), start=(0, 1), stop=(10, 12), count=(2, 2), alternating=False, continuous=False, verticalOrientation=False)], det=[detector('mandelbrot', 0.1, maxIterations=500, escapeRadius=10.0, columns=301, rows=241, points=1000, maxRealCoordinate=1.5, maxImaginaryCoordinate=1.2, realAxisName='p', imaginaryAxisName='q', enableNoise=False, noiseFreeExposureTime=5.0, saveImage=True, saveSpectrum=True, saveValue=True), detector('processing', -1.0, detectorName='mandelbrot', processingFilePath='/tmp/something.nxs', xmx='1024m', timeOut=600000, numberOfCores=1, monitorForOverwrite=False)])";
		assertEquals(expected, mscan);
	}

	@Test
	public void testClusterProcessingModel() throws Exception {

		ClusterProcessingModel cmodel = new ClusterProcessingModel("processing", "mandelbrot", "/tmp/something.nxs");

		String detector = factory.pyExpress(cmodel, true);
		String expected = "detector('processing', -1.0, detectorName='mandelbrot', processingFilePath='/tmp/something.nxs', xmx='1024m', timeOut=600000, numberOfCores=1, monitorForOverwrite=False)";
		assertEquals(expected, detector);
	}

	@Test
	public void testMandelbrotModel() throws Exception {

		MandelbrotModel mmodel = new MandelbrotModel("p", "q");

		String detector = factory.pyExpress(mmodel, false);
		String expected = "detector('mandelbrot', 0.1)";
		assertEquals(expected, detector);
	}

	@Test
	public void testDummyMalcolmModel() throws Exception {

		DummyMalcolmModel mmodel = new DummyMalcolmModel();
		mmodel.setName("malcolm");
		mmodel.setExposureTime(0.1);
		mmodel.setAxesToMove(Arrays.asList("p", "q"));

		String detector = factory.pyExpress(mmodel, false);
		String expected = "detector('malcolm', 0.1)";
		assertEquals(expected, detector);
	}


	@Test
	public void testRasterModel() throws Exception {

		BoundingBox bbox = new BoundingBox();
		bbox.setxAxisStart(0);
		bbox.setyAxisStart(1);
		bbox.setxAxisLength(10);
		bbox.setyAxisLength(11);

		TwoAxisGridStepModel rmodel = new TwoAxisGridStepModel();
		rmodel.setxAxisName("myFast");
		rmodel.setyAxisName("mySlow");
		rmodel.setBoundingBox(bbox);
		rmodel.setxAxisStep(3);
		rmodel.setyAxisStep(4);
		rmodel.setAlternating(true);
		rmodel.setContinuous(true);

		assertEquals(  // Concise.
				"grid(('myFast', 'mySlow'), (0, 1), (10, 12), (3.0, 4.0), True, True, False)",
				factory.pyExpress(rmodel, null, false));
		assertEquals(  // Verbose.
				"grid(axes=('myFast', 'mySlow'), start=(0, 1), stop=(10, 12), step=(3.0, 4.0), alternating=True, continuous=True, verticalOrientation=False)",
				factory.pyExpress(rmodel, null, true));
	}

	@Test
	public void testArrayModel() throws Exception {

		AxialArrayModel amodel = new AxialArrayModel();
		amodel.setName("fred");
		amodel.setPositions(0.1);

		assertEquals(  // Concise.
				"val('fred', 0.1)",
				factory.pyExpress(amodel, false));
		assertEquals(  // Verbose.
				"array(axis='fred', values=[0.1])",
				factory.pyExpress(amodel, true));

		amodel.setPositions(0.1, 0.2);
		assertEquals(  // Concise but with n>1 array values.
				"array('fred', [0.1, 0.2], False, True)",
				factory.pyExpress(amodel, false));
	}

}