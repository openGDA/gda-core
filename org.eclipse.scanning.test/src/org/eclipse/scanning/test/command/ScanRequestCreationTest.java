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
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.OneAxisPointRepeatedModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsRandomOffsetModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLineStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;
import org.eclipse.scanning.api.points.models.TwoAxisPointSingleModel;
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import uk.ac.diamond.osgi.services.ServiceProvider;


public class ScanRequestCreationTest extends AbstractJythonTest {

	public ScanRequestCreationTest() {
		super(false);
	}

	@BeforeEach
	public void before() throws Exception {
		ServiceTestHelper.registerTestDevices();
	}

	@Test
	public void testGridContinuous() {
		pi.exec("sr = "
			  + "scan_request("
			  + "	path=grid("
			  + "		axes=('x', 'y'),"
			  + "		start=(0, 0),"
			  + "		stop=(1, 1),"
			  + "		count=(5, 5)," // this is what makes the model a grid
			  + "		alternating=False,"
			  + "		continuous=True,"
			  + "		roi=rect(origin=(0, 0), size=(1, 1))),"
			  + "	det=[detector('mandelbrot', 0.5)]"
			  + ")"
		);

		ScanRequest request = pi.get("sr", ScanRequest.class);

		Collection<IScanPointGeneratorModel> models = request.getCompoundModel().getModels();
		assertEquals(1, models.size());  // I.e. this is not a compound scan.

		TwoAxisGridPointsModel model = (TwoAxisGridPointsModel) models.iterator().next();
		assertEquals(true, model.isContinuous());
	}

	@Test
	public void testRasterContinuous() {
		pi.exec("sr = "
			  + "scan_request("
			  + "	path=grid("
			  + "		axes=('x', 'y'),"
			  + "		start=(0, 0),"
			  + "		stop=(1, 1),"
			  + "		step=(0.01, 0.01)," // this is what makes the model a raster
			  + "		alternating=False,"
			  + "		continuous=True,"
			  + "		roi=rect(origin=(0, 0), size=(1, 1))),"
			  + "	det=[detector('mandelbrot', 0.5)]"
			  + ")"
		);

		ScanRequest request = pi.get("sr", ScanRequest.class);

		Collection<IScanPointGeneratorModel> models = request.getCompoundModel().getModels();
		assertEquals(1, models.size());  // I.e. this is not a compound scan.

		TwoAxisGridStepModel model = (TwoAxisGridStepModel) models.iterator().next();
		assertEquals(true, model.isContinuous());
	}

	@Test
	public void testGridCommandWithROI() throws Exception {
		pi.exec("sr =                             "
			+	"scan_request(                    "
			+	"    grid(                        "
			+	"        axes=(my_scannable, 'y'),"  // Can use Scannable objects or strings.
			+	"        start=(0, 2),            "
			+	"        stop=(10, 11),           "
			+	"        count=(5, 6),            "
			+	"        roi=circ((4, 6), 5)      "
			+	"    ),                           "
			+	"    det=detector('mandelbrot', 0.1),         "
			+	")                                ");
		ScanRequest request = pi.get("sr", ScanRequest.class);

		Collection<IScanPointGeneratorModel> models = request.getCompoundModel().getModels();
		assertEquals(1, models.size());  // I.e. this is not a compound scan.

		IScanPointGeneratorModel model = models.iterator().next();
		assertEquals(TwoAxisGridPointsModel.class, model.getClass());

		TwoAxisGridPointsModel gmodel = (TwoAxisGridPointsModel) model;
		assertEquals("fred", gmodel.getxAxisName());
		assertEquals("y", gmodel.getyAxisName());
		assertEquals(5, gmodel.getxAxisPoints());
		assertEquals(6, gmodel.getyAxisPoints());
		assertEquals(true, gmodel.isAlternating());

		BoundingBox bbox = gmodel.getBoundingBox();
		assertEquals(0, bbox.getxAxisStart(), 1e-8);
		assertEquals(2, bbox.getyAxisStart(), 1e-8);
		assertEquals(10, bbox.getxAxisLength(), 1e-8);
		assertEquals(9, bbox.getyAxisLength(), 1e-8);

		Collection<IROI> regions = ServiceProvider.getService(IPointGeneratorService.class)
				.findRegions(gmodel, request.getCompoundModel().getRegions());
		assertEquals(1, regions.size());

		IROI region = regions.iterator().next();
		assertEquals(CircularROI.class, region.getClass());

		CircularROI cregion = (CircularROI) region;
		assertEquals(4, cregion.getCentre()[0], 1e-8);
		assertEquals(6, cregion.getCentre()[1], 1e-8);
		assertEquals(5, cregion.getRadius(), 1e-8);

		Map<String, IDetectorModel> detectors = request.getDetectors();
		assertTrue(detectors.keySet().contains("mandelbrot"));

		Object dmodel = detectors.get("mandelbrot");
		assertEquals(MandelbrotModel.class, dmodel.getClass());

		MandelbrotModel mmodel = (MandelbrotModel) dmodel;
		assertEquals(0.1, mmodel.getExposureTime(), 1e-8);
	}

	@Test
	public void testGridCommandWithROINoDetector() throws Exception {
		pi.exec("sr =                             "
			+	"scan_request(                    "
			+	"    grid(                        "
			+	"        axes=(my_scannable, 'y'),"  // Can use Scannable objects or strings.
			+	"        start=(0, 2),            "
			+	"        stop=(10, 11),           "
			+	"        count=(5, 6),            "
			+	"        roi=circ((4, 6), 5)      "
			+	"    ),                           "
			+	")                                ");
		ScanRequest request = pi.get("sr", ScanRequest.class);

		Collection<IScanPointGeneratorModel> models = request.getCompoundModel().getModels();
		assertEquals(1, models.size());  // I.e. this is not a compound scan.

		IScanPointGeneratorModel model = models.iterator().next();
		assertEquals(TwoAxisGridPointsModel.class, model.getClass());

		TwoAxisGridPointsModel gmodel = (TwoAxisGridPointsModel) model;
		assertEquals("fred", gmodel.getxAxisName());
		assertEquals("y", gmodel.getyAxisName());
		assertEquals(5, gmodel.getxAxisPoints());
		assertEquals(6, gmodel.getyAxisPoints());
		assertEquals(true, gmodel.isAlternating());

		BoundingBox bbox = gmodel.getBoundingBox();
		assertEquals(0, bbox.getxAxisStart(), 1e-8);
		assertEquals(2, bbox.getyAxisStart(), 1e-8);
		assertEquals(10, bbox.getxAxisLength(), 1e-8);
		assertEquals(9, bbox.getyAxisLength(), 1e-8);

		Collection<IROI> regions = ServiceProvider.getService(IPointGeneratorService.class)
				.findRegions(gmodel, request.getCompoundModel().getRegions());
		assertEquals(1, regions.size());

		IROI region = regions.iterator().next();
		assertEquals(CircularROI.class, region.getClass());

		CircularROI cregion = (CircularROI) region;
		assertEquals(4, cregion.getCentre()[0], 1e-8);
		assertEquals(6, cregion.getCentre()[1], 1e-8);
		assertEquals(5, cregion.getRadius(), 1e-8);
	}

	@Test
	public void testGridWithPolygonROI() throws GeneratorException {
		pi.exec("sr =                             "
			+	"scan_request(                    "
			+	"    grid(                        "
			+	"        axes=(my_scannable, 'y'),"  // Can use Scannable objects or strings.
			+	"        start=(0, 2),            "
			+	"        stop=(10, 11),           "
			+	"        count=(5, 6),            "
			+	"        roi=poly((0, 0),(1, 5),(1, -2))"
			+	"    ),                           "
			+	")                                ");
		ScanRequest request = pi.get("sr", ScanRequest.class);

		Collection<IScanPointGeneratorModel> models = request.getCompoundModel().getModels();
		assertEquals(1, models.size());  // I.e. this is not a compound scan.

		IScanPointGeneratorModel model = models.iterator().next();

		Collection<IROI> regions = ServiceProvider.getService(IPointGeneratorService.class)
				.findRegions(model, request.getCompoundModel().getRegions());
		assertEquals(1, regions.size());

		IROI region = regions.iterator().next();
		assertEquals(PolygonalROI.class, region.getClass());

		PolygonalROI poly = (PolygonalROI) region;
		assertEquals(3, poly.getNumberOfPoints());

		assertEquals(0., poly.getPoint(0).getPointX(), 1e-8);
		assertEquals(0., poly.getPoint(0).getPointY(), 1e-8);
		assertEquals(1., poly.getPoint(1).getPointX(), 1e-8);
		assertEquals(5., poly.getPoint(1).getPointY(), 1e-8);
		assertEquals(1., poly.getPoint(2).getPointX(), 1e-8);
		assertEquals(-2., poly.getPoint(2).getPointY(), 1e-8);
	}

	@Test
	public void testRandomOffsetGridCommand() {
		pi.exec("sr =                             "
				+	"scan_request(                    "
				+	"    random_offset_grid(			"
				+	"        axes=(my_scannable, 'y'),"
				+	"        start=(0, 2),            "
				+	"        stop=(10, 11),           "
				+	"        count=(5, 6),            "
				+	"		 offset=5,				  "
				+	"        roi=circ((4, 6), 5)      "
				+	"    ),                           "
				+	"    det=detector('mandelbrot', 0.1),         "
				+	")                                ");
			ScanRequest request = pi.get("sr", ScanRequest.class);

			Collection<IScanPointGeneratorModel> models = request.getCompoundModel().getModels();
			assertEquals(1, models.size());  // I.e. this is not a compound scan.

			IScanPointGeneratorModel model = models.iterator().next();
			assertEquals(TwoAxisGridPointsRandomOffsetModel.class, model.getClass());

			TwoAxisGridPointsRandomOffsetModel rogmodel = (TwoAxisGridPointsRandomOffsetModel) model;
			assertEquals("fred", rogmodel.getxAxisName());
			assertEquals("y", rogmodel.getyAxisName());
			assertEquals(5, rogmodel.getxAxisPoints());
			assertEquals(6, rogmodel.getyAxisPoints());
			assertEquals(true, rogmodel.isAlternating());
			assertEquals(5, rogmodel.getOffset(), 1e-8);
			assertEquals(0, rogmodel.getSeed());

	}

	@Test
	public void testSpiralCommand() {
		pi.exec("sr =                             	  "
				+	"scan_request(                    "
				+	"    spiral(			          "
				+	"        axes=(my_scannable, 'y'),"
				+	"        start=(0, 2),            "
				+	"        stop=(10, 11),           "
				+   "		 scale=(0.5),			  "
				+	"        roi=circ((4, 6), 5)      "
				+	"    )                            "
				+	")                                ");
			ScanRequest request = pi.get("sr", ScanRequest.class);

			Collection<IScanPointGeneratorModel> models = request.getCompoundModel().getModels();
			assertEquals(1, models.size());  // I.e. this is not a compound scan.

			IScanPointGeneratorModel model = models.iterator().next();
			assertEquals(TwoAxisSpiralModel.class, model.getClass());

			TwoAxisSpiralModel spiral = (TwoAxisSpiralModel) model;
			assertEquals(0.5, spiral.getScale(), 1e-8);
	}

	@Test
	public void testLissajousCommand() {
		pi.exec("sr =                             	  "
				+	"scan_request(                    "
				+	"    lissajous(			          "
				+	"        axes=(my_scannable, 'y'),"
				+	"        start=(0, 2),            "
				+	"        stop=(10, 11),           "
				+   "		 a=(0.5),			 	  "
				+   "		 b=(0.1),			 	  "
				+   "		 points=(40),		 	  "
				+	"        roi=circ((4, 6), 5)      "
				+	"    )                            "
				+	")                                ");
			ScanRequest request = pi.get("sr", ScanRequest.class);

			Collection<IScanPointGeneratorModel> models = request.getCompoundModel().getModels();
			assertEquals(1, models.size());  // I.e. this is not a compound scan.

			IScanPointGeneratorModel model = models.iterator().next();
			assertEquals(TwoAxisLissajousModel.class, model.getClass());

			TwoAxisLissajousModel lissajous = (TwoAxisLissajousModel) model;
			assertEquals(0.5, lissajous.getA(), 1e-8);
			assertEquals(0.1, lissajous.getB(), 1e-8);
			assertEquals(40, lissajous.getPoints());
	}

	@Test
	public void testStepCommandWithMonitors() {
		pi.exec("sr =                               "
			+	"scan_request(                      "
			+	"    step(my_scannable, -2, 5, 0.5),"
			+	"    monitorsPerPoint=['x', another_scannable],  "  // Monitor two scannables.
			+	"    det=detector('mandelbrot', 0.1),           "
			+	")                                  ");
		ScanRequest request = pi.get("sr", ScanRequest.class);

		Object model = request.getCompoundModel().getModels().get(0);
		assertEquals(AxialStepModel.class, model.getClass());

		AxialStepModel smodel = (AxialStepModel) model;
		assertEquals("fred", smodel.getName());
		assertEquals(-2, smodel.getStart(), 1e-8);
		assertEquals(5, smodel.getStop(), 1e-8);
		assertEquals(0.5, smodel.getStep(), 1e-8);

		Collection<String> monitors = request.getMonitorNamesPerPoint();
		assertEquals(2, monitors.size());
		Iterator<String> monitorIterator = monitors.iterator();
		assertEquals("x", monitorIterator.next());
		assertEquals("bill", monitorIterator.next());
	}

	@Test
	public void testStepCommandWithMonitorsNoDetector() {
		pi.exec("sr =                               "
			+	"scan_request(                      "
			+	"    step(my_scannable, -2, 5, 0.5),"
			+	"    monitorsPerPoint=['x', another_scannable],  "  // Monitor two scannables.
			+	")                                  ");
		ScanRequest request = pi.get("sr", ScanRequest.class);

		Object model = request.getCompoundModel().getModels().get(0);
		assertEquals(AxialStepModel.class, model.getClass());

		AxialStepModel smodel = (AxialStepModel) model;
		assertEquals("fred", smodel.getName());
		assertEquals(-2, smodel.getStart(), 1e-8);
		assertEquals(5, smodel.getStop(), 1e-8);
		assertEquals(0.5, smodel.getStep(), 1e-8);

		Collection<String> monitors = request.getMonitorNamesPerPoint();
		assertEquals(2, monitors.size());
		Iterator<String> monitorIterator = monitors.iterator();
		assertEquals("x", monitorIterator.next());
		assertEquals("bill", monitorIterator.next());
	}

	@Test
	public void testRepeatCommandWithMonitors() {
		pi.exec("sr =                               "
			+	"scan_request(                      "
			+	"    repeat(my_scannable, 10, 2.2, 25),"
			+	"    monitorsPerPoint=['x', another_scannable],  "  // Monitor two scannables.
			+	"    det=detector('mandelbrot', 0.1),           "
			+	")                                  ");
		ScanRequest request = pi.get("sr", ScanRequest.class);

		Object model = request.getCompoundModel().getModels().get(0);
		assertEquals(OneAxisPointRepeatedModel.class, model.getClass());

		OneAxisPointRepeatedModel smodel = (OneAxisPointRepeatedModel) model;
		assertEquals("fred", smodel.getName());
		assertEquals(10, smodel.getCount(), 1e-8);
		assertEquals(2.2, smodel.getValue(), 1e-8);
		assertEquals(25, smodel.getSleep(), 1e-8);

		Collection<String> monitors = request.getMonitorNamesPerPoint();
		assertEquals(2, monitors.size());
		Iterator<String> monitorIterator = monitors.iterator();
		assertEquals("x", monitorIterator.next());
		assertEquals("bill", monitorIterator.next());
	}

	@Test
	public void testRepeatCommandWithMonitorsNoDetector() {
		pi.exec("sr =                               "
			+	"scan_request(                      "
			+	"    repeat(my_scannable, 10, 2.2, 25),"
			+	"    monitorsPerPoint=['x', another_scannable]  "  // Monitor two scannables.
			+	")                                  ");
		ScanRequest request = pi.get("sr", ScanRequest.class);

		Object model = request.getCompoundModel().getModels().get(0);
		assertEquals(OneAxisPointRepeatedModel.class, model.getClass());

		OneAxisPointRepeatedModel smodel = (OneAxisPointRepeatedModel) model;
		assertEquals("fred", smodel.getName());
		assertEquals(10, smodel.getCount(), 1e-8);
		assertEquals(2.2, smodel.getValue(), 1e-8);
		assertEquals(25, smodel.getSleep(), 1e-8);

		Collection<String> monitors = request.getMonitorNamesPerPoint();
		assertEquals(2, monitors.size());
		Iterator<String> monitorIterator = monitors.iterator();
		assertEquals("x", monitorIterator.next());
		assertEquals("bill", monitorIterator.next());
	}

	@Test
	public void testRasterCommandWithROIs() throws Exception {
		pi.exec("sr =                                 "
			+	"scan_request(                        "
			+	"    grid(                            "
			+	"        axes=('x', 'y'),             "
			+	"        start=(1, 2),                "
			+	"        stop=(8, 10),                "
			+	"        step=(0.5, 0.6),             "
			+	"        alternating=True,                  "
			+	"        roi=[                        "
			+	"            circ((4, 4), 5),         "
			+	"            rect((3, 4), (3, 3), 0.1)"
			+	"        ]                            "
			+	"    ),                               "
			+	"    det=detector('mandelbrot', 0.1),             "
			+	")                                    ");
		ScanRequest request = pi.get("sr", ScanRequest.class);

		Object model = request.getCompoundModel().getModels().iterator().next();
		assertEquals(TwoAxisGridStepModel.class, model.getClass());

		TwoAxisGridStepModel rmodel = (TwoAxisGridStepModel) model;
		assertEquals(0.5, rmodel.getxAxisStep(), 1e-8);

		Collection<IROI> regions = ServiceProvider.getService(IPointGeneratorService.class)
				.findRegions(rmodel, request.getCompoundModel().getRegions());
		assertEquals(2, regions.size());

		Iterator<IROI> regionIterator = regions.iterator();
		CircularROI cregion = (CircularROI) regionIterator.next();
		assertEquals(4, cregion.getCentre()[0], 1e-8);
		assertEquals(4, cregion.getCentre()[1], 1e-8);
		assertEquals(5, cregion.getRadius(), 1e-8);

		RectangularROI rregion = (RectangularROI) regionIterator.next();
		assertEquals(3, rregion.getPoint()[0], 1e-8);
		assertEquals(4, rregion.getPoint()[1], 1e-8);
		assertEquals(3, rregion.getLengths()[0], 1e-8);
		assertEquals(3, rregion.getLengths()[0], 1e-8);
		assertEquals(0.1, rregion.getAngle(), 0.1);  // Radians.
	}

	@Test
	public void testRasterCommandWithROIsNoDetector() throws Exception {
		pi.exec("sr =                                 "
			+	"scan_request(                        "
			+	"    grid(                            "
			+	"        axes=('x', 'y'),             "
			+	"        start=(1, 2),                "
			+	"        stop=(8, 10),                "
			+	"        step=(0.5, 0.6),             "
			+	"        alternating=True,                  "
			+	"        roi=[                        "
			+	"            circ((4, 4), 5),         "
			+	"            rect((3, 4), (3, 3), 0.1)"
			+	"        ]                            "
			+	"    )                                "
			+	")                                    ");
		ScanRequest request = pi.get("sr", ScanRequest.class);

		Object model = request.getCompoundModel().getModels().iterator().next();
		assertEquals(TwoAxisGridStepModel.class, model.getClass());

		TwoAxisGridStepModel rmodel = (TwoAxisGridStepModel) model;
		assertEquals(0.5, rmodel.getxAxisStep(), 1e-8);

		Collection<IROI> regions = ServiceProvider.getService(IPointGeneratorService.class)
				.findRegions(rmodel, request.getCompoundModel().getRegions());
		assertEquals(2, regions.size());

		Iterator<IROI> regionIterator = regions.iterator();
		CircularROI cregion = (CircularROI) regionIterator.next();
		assertEquals(4, cregion.getCentre()[0], 1e-8);
		assertEquals(4, cregion.getCentre()[1], 1e-8);
		assertEquals(5, cregion.getRadius(), 1e-8);

		RectangularROI rregion = (RectangularROI) regionIterator.next();
		assertEquals(3, rregion.getPoint()[0], 1e-8);
		assertEquals(4, rregion.getPoint()[1], 1e-8);
		assertEquals(3, rregion.getLengths()[0], 1e-8);
		assertEquals(3, rregion.getLengths()[0], 1e-8);
		assertEquals(0.1, rregion.getAngle(), 0.1);  // Radians.
	}

	@Test
	public void testArrayCommand() {
		pi.exec("sr =                                 "
			+	"scan_request(                        "
			+	"    array('qty', [-3, 1, 1.5, 1e10]),"
			+	"    det=detector('mandelbrot', 0.1),             "
			+	")                                    ");
		ScanRequest request = pi.get("sr", ScanRequest.class);

		Object model = request.getCompoundModel().getModels().iterator().next();
		assertEquals(AxialArrayModel.class, model.getClass());

		AxialArrayModel amodel = (AxialArrayModel) model;
		assertEquals("qty", amodel.getName());
		assertEquals(-3, amodel.getPositions()[0], 1e-8);
		assertEquals(1, amodel.getPositions()[1], 1e-8);
		assertEquals(1.5, amodel.getPositions()[2], 1e-8);
		assertEquals(1e10, amodel.getPositions()[3], 1);
	}

	@Test
	public void testOneDEqualSpacingCommand() {
		pi.exec("sr =                                                    "
			+	"scan_request(                                           "
			+	"    line(origin=(0, 4), length=10, angle=0.1, count=10),"
			+	"    det=[detector('mandelbrot', 0.1)],                              "
			+	")                                                       ");
		ScanRequest request = pi.get("sr", ScanRequest.class);

		Object model = request.getCompoundModel().getModels().iterator().next();
		assertEquals(TwoAxisLinePointsModel.class, model.getClass());

		TwoAxisLinePointsModel omodel = (TwoAxisLinePointsModel) model;
		assertEquals(0, omodel.getBoundingLine().getxStart(), 1e-8);
		assertEquals(4, omodel.getBoundingLine().getyStart(), 1e-8);
		assertEquals(0.1, omodel.getBoundingLine().getAngle(), 1e-8);
		assertEquals(10, omodel.getBoundingLine().getLength(), 1e-8);
		assertEquals(10, omodel.getPoints());
	}

	@Test
	public void testOneDStepCommand() {
		pi.exec("sr =                                                       "
			+	"scan_request(                                              "
			+	"    line(origin=(-2, 1.3), length=10, angle=0.1, step=0.5),"
			+	"    det=detector('mandelbrot', 0.1),                                   "
			+	")                                                          ");
		ScanRequest request = pi.get("sr", ScanRequest.class);

		Object model = request.getCompoundModel().getModels().iterator().next();
		assertEquals(TwoAxisLineStepModel.class, model.getClass());

		TwoAxisLineStepModel omodel = (TwoAxisLineStepModel) model;
		assertEquals(-2, omodel.getBoundingLine().getxStart(), 1e-8);
		assertEquals(1.3, omodel.getBoundingLine().getyStart(), 1e-8);
		assertEquals(0.1, omodel.getBoundingLine().getAngle(), 1e-8);
		assertEquals(10, omodel.getBoundingLine().getLength(), 1e-8);
		assertEquals(0.5, omodel.getStep(), 1e-8);
	}

	@Test
	public void testSinglePointCommand() {
		pi.exec("sr = scan_request(point(4, 5), det=mandelbrot(0.1))");
		ScanRequest request = pi.get("sr", ScanRequest.class);

		Object model = request.getCompoundModel().getModels().iterator().next();
		assertEquals(TwoAxisPointSingleModel.class, model.getClass());

		TwoAxisPointSingleModel spmodel = (TwoAxisPointSingleModel) model;
		assertEquals(4, spmodel.getX(), 1e-8);
		assertEquals(5, spmodel.getY(), 1e-8);

		Map<String, IDetectorModel> detectors = request.getDetectors();
		assertTrue(detectors.keySet().contains("mandelbrot"));

		Object dmodel = detectors.get("mandelbrot");
		assertEquals(MandelbrotModel.class, dmodel.getClass());

		MandelbrotModel mmodel = (MandelbrotModel) dmodel;
		assertEquals(0.1, mmodel.getExposureTime(), 1e-8);
	}

	@Test
	public void testSquareBracketCombinations() {
		pi.exec("sr0 = scan_request(point(4, 5), det=detector('mandelbrot', 0.1))");
		pi.exec("sr1 = scan_request([point(4, 5)], det=detector('mandelbrot', 0.1))");
		pi.exec("sr2 = scan_request(point(4, 5), det=[detector('mandelbrot', 0.1)])");
		pi.exec("sr3 = scan_request([point(4, 5)], det=[detector('mandelbrot', 0.1)])");

		ScanRequest request1 = pi.get("sr0", ScanRequest.class);
		ScanRequest request2 = pi.get("sr1", ScanRequest.class);
		ScanRequest request3 = pi.get("sr2", ScanRequest.class);
		ScanRequest request4 = pi.get("sr3", ScanRequest.class);

		assertEquals(4, ((TwoAxisPointSingleModel) request1.getCompoundModel().getModels().iterator().next()).getX(), 1e-8);
		assertEquals(0.1, ((MandelbrotModel) request1.getDetectors().get("mandelbrot")).getExposureTime(), 1e-8);

		assertEquals(4, ((TwoAxisPointSingleModel) request2.getCompoundModel().getModels().iterator().next()).getX(), 1e-8);
		assertEquals(0.1, ((MandelbrotModel) request2.getDetectors().get("mandelbrot")).getExposureTime(), 1e-8);

		assertEquals(4, ((TwoAxisPointSingleModel) request3.getCompoundModel().getModels().iterator().next()).getX(), 1e-8);
		assertEquals(0.1, ((MandelbrotModel) request3.getDetectors().get("mandelbrot")).getExposureTime(), 1e-8);

		assertEquals(4, ((TwoAxisPointSingleModel) request4.getCompoundModel().getModels().iterator().next()).getX(), 1e-8);
		assertEquals(0.1, ((MandelbrotModel) request4.getDetectors().get("mandelbrot")).getExposureTime(), 1e-8);
	}

	@Test
	public void testCompoundCommand() {
		pi.exec("sr =                                                                                 "
			+	"scan_request(                                                                        "
			+	"    path=[                                                                           "
			+	"        grid(axes=('x', 'y'), start=(0, 0), stop=(10, 10), count=(5, 5), alternating=True),"
			+	"        step('qty', 0, 10, 1),                                                       "
			+	"    ],                                                                               "
			+	"    det=detector('mandelbrot', 0.1),                                                             "
			+	")                                                                                    ");
		ScanRequest request = pi.get("sr", ScanRequest.class);

		Collection<IScanPointGeneratorModel> models = request.getCompoundModel().getModels();
		assertEquals(2, models.size());  // I.e. this is a compound scan with two components.

		Iterator<IScanPointGeneratorModel> modelIterator = models.iterator();
		TwoAxisGridPointsModel gmodel = (TwoAxisGridPointsModel) modelIterator.next();
		assertEquals(5, gmodel.getyAxisPoints());

		AxialStepModel smodel = (AxialStepModel) modelIterator.next();
		assertEquals(10, smodel.getStop(), 1e-8);
	}

	@Test
	public void testMoveToKeepStillCommand() {
		pi.exec("sr =                                              "
			+	"scan_request(                                     "
			+	"    [step(my_scannable, -2, 5, 0.5), val('y', 5)],"
			+	"    det=detector('mandelbrot', 0.1),                          "
			+	")                                                 ");
		ScanRequest request = pi.get("sr", ScanRequest.class);

		Collection<IScanPointGeneratorModel> models = request.getCompoundModel().getModels();
		assertEquals(2, models.size());

		Iterator<IScanPointGeneratorModel> modelIterator = models.iterator();
		modelIterator.next();  // Throw away the step model.

		AxialArrayModel amodel = (AxialArrayModel) modelIterator.next();
		assertEquals(1, amodel.getPositions().length);
		assertEquals(5, amodel.getPositions()[0], 1e-8);
	}

	@Test
	public void testMoveToKeepStillCommandNoDetector() {
		pi.exec("sr =                                              "
			+	"scan_request(                                     "
			+	"    [step(my_scannable, -2, 5, 0.5), val('y', 5)]"
			+	")                                                 ");
		ScanRequest request = pi.get("sr", ScanRequest.class);

		Collection<IScanPointGeneratorModel> models = request.getCompoundModel().getModels();
		assertEquals(2, models.size());

		Iterator<IScanPointGeneratorModel> modelIterator = models.iterator();
		modelIterator.next();  // Throw away the step model.

		AxialArrayModel amodel = (AxialArrayModel) modelIterator.next();
		assertEquals(1, amodel.getPositions().length);
		assertEquals(5, amodel.getPositions()[0], 1e-8);
	}


	@Disabled("ScanRequest<?>.equals() doesn't allow this test to work.")
	@Test
	public void testArgStyleInvariance() {
		pi.exec("sr_full =                     "
			+	"scan_request(                 "
			+	"    path=grid(                "
			+	"        axes=('x', 'y'),      "
			+	"        start=(1, 2),         "
			+	"        stop=(8, 10),         "
			+	"        step=(0.5, 0.6),      "
			+	"        roi=[                 "
			+	"            circ(             "
			+	"                origin=(4, 4),"
			+	"                radius=5      "
			+	"            ),                "
			+	"            rect(             "
			+	"                origin=(3, 4),"
			+	"                size=(3, 3),  "
			+	"                angle=0.1     "
			+	"            ),                "
			+	"        ]                     "
			+	"    ),                        "
			+	"    det=mandelbrot(0.1),      "
			+	")                             ");
		pi.exec("sr_minimal =                                    "
			+	"scan_request(                                   "
			+	"    grid(                                       "
			+	"        ('x', 'y'), (1, 2), (8, 10), (0.5, 0.6),"
			+	"        roi=[                                   "
			+	"            circ((4, 4), 5),                    "
			+	"            rect((3, 4), (3, 3), 0.1),          "
			+	"        ]                                       "
			+	"    ),                                          "
			+	"    mandelbrot(0.1),                            "
			+	")                                               ");

		ScanRequest requestFullKeywords = pi.get("sr_full", ScanRequest.class);
		ScanRequest requestMinimalKeywords = pi.get("sr_minimal", ScanRequest.class);

		assertTrue(requestMinimalKeywords.equals(requestFullKeywords));
	}
}
