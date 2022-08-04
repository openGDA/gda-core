package org.eclipse.scanning.test.scan.nexus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.RandomLineDevice;
import org.eclipse.scanning.example.detector.RandomLineModel;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScanTimeoutTest extends NexusTest {

	private RandomLineDevice linedetector;

	@BeforeEach
	void before() throws ScanningException {
		final RandomLineModel rlModel = new RandomLineModel();
		rlModel.setTimeout(10);
		linedetector = (RandomLineDevice) TestDetectorHelpers.createAndConfigureRandomLineDetector(rlModel);
		assertThat(linedetector, is(notNullValue()));
	}

	@AfterEach
	void after() throws Exception {
		linedetector.reset();
	}

	@Test
	void testLine() throws Exception {
		final IRunnableDevice<ScanModel> scanner = createScanner(linedetector,  2, 2);
		scanner.run(null);

		assertThat(linedetector.getCount("configure"), is(1));
		assertThat(linedetector.getCount("run"), is(4));
		assertThat(linedetector.getCount("write"), is(4));
	}

	@Test
	void testLineTimeoutThrowsException() throws Exception {
		try {
			linedetector.getModel().setExposureTime(2); // Sleeps for 2 seconds.
			linedetector.getModel().setTimeout(1);      // Only 1 second allowed.

			// All scannables should have their name set ok
			final IRunnableDevice<ScanModel> scanner = createScanner(linedetector, 2, 2);
			assertThrows(ScanningException.class, () -> scanner.run(null));
		} finally {
			linedetector.getModel().setExposureTime(0);
			linedetector.getModel().setTimeout(-1);
		}
	}

	@Test
	void testLineThrowsWriteException() throws Exception {
		try {
			linedetector.setThrowWriteExceptions(true);

			// All scannables should have their name set ok
			final IRunnableDevice<ScanModel> scanner = createScanner(linedetector, 2, 2);
			assertThrows(ScanningException.class, () -> scanner.run(null));
		} finally {
			linedetector.setThrowWriteExceptions(false);
		}
	}


	@Test
	void testMultiStep() throws Exception {
		final IRunnableDevice<ScanModel> scanner = createMultiStepScanner(linedetector);
		final long before = System.currentTimeMillis();
		scanner.run();
		final long after = System.currentTimeMillis();
		final long time  = after - before;

		assertThat("The time to run the scan must be less than 2000 but it was "+time+"ms", time, is(lessThan(2000l)));

		assertThat(linedetector.getCount("configure"), is(1));
		assertThat(((RandomLineModel)linedetector.getValue("configure", 0)).getExposureTime(), is(closeTo(0.001, 1e-5)));

		assertThat(linedetector.getCount("run"), is(21));
		assertThat(linedetector.getCount("write"), is(21));
	}

	private IRunnableDevice<ScanModel> createScanner(IRunnableDevice<RandomLineModel> device, int... shape) throws Exception {
		final ScanModel smodel = createGridScanModel(device, output, true, shape);
		return scanService.createScanDevice(smodel);
	}

	private IRunnableDevice<ScanModel> createMultiStepScanner(IRunnableDevice<RandomLineModel> device) throws Exception {
		final AxialMultiStepModel multiStepModel = new AxialMultiStepModel();
		multiStepModel.setName("x");
		multiStepModel.addRange(10, 20, 2); // Te = 0.0015
		multiStepModel.addRange(25, 50, 5);  // Te = 0.002
		multiStepModel.addRange(100, 500, 50);  // Te = 0.003
		multiStepModel.setContinuous(false);

		final IPointGenerator<? extends IScanPointGeneratorModel> pointGen = pointGenService.createGenerator(multiStepModel);
		assertThat(pointGen.size(), is(21));

		final ScanModel scanModel = new ScanModel();
		scanModel.setPointGenerator(pointGen);
		scanModel.setScanPathModel(multiStepModel);
		scanModel.setDetector(device);
		scanModel.setFilePath(output.getCanonicalPath());

		return scanService.createScanDevice(scanModel);
	}

}
