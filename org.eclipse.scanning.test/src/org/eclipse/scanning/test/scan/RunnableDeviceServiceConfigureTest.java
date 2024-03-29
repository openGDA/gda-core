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
package org.eclipse.scanning.test.scan;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.IScanService;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.MandelbrotDetector;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import uk.ac.diamond.osgi.services.ServiceProvider;

public class RunnableDeviceServiceConfigureTest {

	@BeforeAll
	public static void setup() throws Exception {
		ServiceTestHelper.setupServices();

		registerFive();
	}

	@AfterAll
	public static void tearDown() {
		ServiceProvider.reset();
	}

	public static void registerFive() throws Exception {

		for (int i = 0; i < 5; i++) {

			final MandelbrotModel model = new MandelbrotModel("x", "y");
			model.setName("mandelbrot"+i);
			model.setExposureTime(0.000001);

			final MandelbrotDetector det = new MandelbrotDetector();
			det.setModel(model);
			det.setName("mandelbrot"+i);

			DeviceInformation<MandelbrotModel> info = new DeviceInformation<>("mandelbrot"+i);
			info.setId("org.eclipse.scanning.example.mandelbrotDetectorInfo"+i);
			info.setLabel("Mandelbrot Detector "+i);
			info.setDescription("A Test Detector");
			info.setIcon("org.eclipse.scanning.example/icons/alarm-clock-select.png");
			det.setDeviceInformation(info);

			ServiceProvider.getService(IRunnableDeviceService.class).register(det);

		}
	}

	@Test
	public void testScanMandelbrot1() throws Exception {
		IRunnableDevice<ScanModel> scan = createTestScanner("mandelbrot1");
		scan.run(null);
		checkRun(scan, 25);
	}

	@Test
	public void testScanMandelbrot4() throws Exception {
		IRunnableDevice<ScanModel> scan = createTestScanner("mandelbrot4");
		scan.run(null);
		checkRun(scan, 25);
	}

	@Test
	public void testScanAFewMandelbrots() throws Exception {
		IRunnableDevice<ScanModel> scan = createTestScanner("mandelbrot0", "mandelbrot1", "mandelbrot2", "mandelbrot3", "mandelbrot4");
		scan.run(null);
		checkRun(scan, 25);
	}


	private IRunnableDevice<ScanModel> createTestScanner(String... names) throws Exception {
		final IScanService sservice = ServiceProvider.getService(IScanService.class);
		final List<IRunnableDevice<? extends IDetectorModel>> detectors = new ArrayList<>(names.length);
		for (String name : names) {
			detectors.add(sservice.getRunnableDevice(name));
		}

		// If none passed, create scan points for a grid.
		final TwoAxisGridPointsModel gridPointsModel = new TwoAxisGridPointsModel("x", "y");
		gridPointsModel.setyAxisPoints(5);
		gridPointsModel.setxAxisPoints(5);
		gridPointsModel.setBoundingBox(new BoundingBox(0,0,3,3));

		final IPointGenerator<? extends IScanPointGeneratorModel> pointGen =
				ServiceProvider.getService(IPointGeneratorService.class).createGenerator(gridPointsModel);

		// Create the model for a scan.
		final ScanModel scanModel = new ScanModel();
		scanModel.setPointGenerator(pointGen);
		scanModel.setScanPathModel(gridPointsModel);
		scanModel.setDetectors(detectors);

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = sservice.createScanDevice(scanModel);
		return scanner;
	}

	private void checkRun(IRunnableDevice<ScanModel> scanner, int size) {
		// Bit of a hack to get the generator from the model - should this be easier?
		// Do not copy this code
		ScanModel smodel = scanner.getModel();
		IPointGenerator<?> gen = smodel.getPointGenerator();
		assertEquals(gen.size(), size);
	}

}
