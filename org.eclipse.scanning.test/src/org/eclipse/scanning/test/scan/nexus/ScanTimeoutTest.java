package org.eclipse.scanning.test.scan.nexus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.RandomLineDevice;
import org.eclipse.scanning.example.detector.RandomLineModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ScanTimeoutTest  extends NexusTest {

	private RandomLineDevice linedetector;

	@Before
	public void before() throws ScanningException {
		final RandomLineModel rlModel = new RandomLineModel();
		rlModel.setTimeout(10);
		linedetector = (RandomLineDevice)runnableDeviceService.createRunnableDevice(rlModel);
		assertNotNull(linedetector);
	}

	@After
	public void after() throws Exception {
		linedetector.reset();
	}

	@Test
	public void testLine() throws Exception {

		IRunnableDevice<ScanModel> scanner = createScanner(linedetector,  2, 2);
		scanner.run(null);

		assertEquals(1, linedetector.getCount("configure"));
		assertEquals(4, linedetector.getCount("run"));
		assertEquals(4, linedetector.getCount("write"));
	}

	@Test(expected=ScanningException.class)
	public void testLineTimeoutThrowsException() throws Exception {

		try {
			linedetector.getModel().setExposureTime(2); // Sleeps for 2 seconds.
			linedetector.getModel().setTimeout(1);      // Only 1 second allowed.

			// All scannables should have their name set ok
			IRunnableDevice<ScanModel> scanner = createScanner(linedetector,  2, 2);
			scanner.run(null);

		} finally {
			linedetector.getModel().setExposureTime(0);
			linedetector.getModel().setTimeout(-1);
		}
	}

	@Test(expected=ScanningException.class)
	public void testLineThrowsWriteException() throws Exception {

		try {
			linedetector.setThrowWriteExceptions(true);

			// All scannables should have their name set ok
			IRunnableDevice<ScanModel> scanner = createScanner(linedetector,  2, 2);
			scanner.run(null);

		} finally {
			linedetector.setThrowWriteExceptions(false);
		}
	}


	@Test
	public void testMultiStep() throws Exception {

		IRunnableDevice<ScanModel> scanner = createMultiStepScanner(linedetector);
		long before = System.currentTimeMillis();
		scanner.run();
		long after = System.currentTimeMillis();
		long time  = after-before;
		assertTrue("The time to run the scan must be less than 2000 but it was "+time+"ms", time<2000);

		assertEquals(1, linedetector.getCount("configure"));
		assertEquals(0.001,  ((RandomLineModel)linedetector.getValue("configure", 0)).getExposureTime(), 0.000001);

		assertEquals(21, linedetector.getCount("run"));
		assertEquals(21, linedetector.getCount("write"));
	}


	private IRunnableDevice<ScanModel> createScanner(IRunnableDevice<RandomLineModel> device, int... shape) throws Exception {

		ScanModel smodel = createGridScanModel(device, output, true, shape);
		return runnableDeviceService.createRunnableDevice(smodel, null);
	}

	private IRunnableDevice<ScanModel> createMultiStepScanner(IRunnableDevice<RandomLineModel> device) throws Exception {

		final AxialMultiStepModel multiStepModel = new AxialMultiStepModel();
		multiStepModel.setName("x");
		multiStepModel.addRange(10, 20, 2); // Te = 0.0015
		multiStepModel.addRange(25, 50, 5);  // Te = 0.002
		multiStepModel.addRange(100, 500, 50);  // Te = 0.003
		multiStepModel.setContinuous(false);

		final IPointGenerator<? extends IScanPointGeneratorModel> pointGen = pointGenService.createGenerator(multiStepModel);
		assertEquals(21, pointGen.size());

		final ScanModel scanModel = new ScanModel();
		scanModel.setPointGenerator(pointGen);
		scanModel.setScanPathModel(multiStepModel);
		scanModel.setDetector(device);
		scanModel.setFilePath(output.getCanonicalPath());

		return runnableDeviceService.createRunnableDevice(scanModel, null);
	}

}
