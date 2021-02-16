package org.eclipse.scanning.test.scan.nexus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.scanning.api.annotation.scan.FileDeclared;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.junit.Test;

/**
 * Test the use of Malcolm to acquire data without moving any scannables.<br>
 * This would be used for software scans using detectors that can only acquire via Malcolm.
 *
 */
public class MalcolmStaticScanTest extends AbstractMalcolmScanTest {

	@Test
	public void test0d() throws Exception {
		runMalcolmScan();
	}

	@Test
	public void test1d() throws Exception {
		runMalcolmScan(5);
	}

	@Test
	public void test3d() throws Exception {
		runMalcolmScan(5, 3, 2);
	}

	@Override
	protected DummyMalcolmModel createMalcolmModel() {
		final DummyMalcolmModel model = createMalcolmModelTwoDetectors();
		model.setAxesToMove(Collections.emptyList());
		return model;
	}

	private void runMalcolmScan(int... size) throws Exception {
		final IRunnableDevice<ScanModel> scanner = createScanner(size);
		scanner.run();

		checkSize(scanner, size);
		checkFiles();
		checkNexusFile(scanner, false, size);
		assertEquals(DeviceState.ARMED, scanner.getDeviceState());
	}

	// Create a scan where the Malcolm device just does a static scan, but there may be outer scannables
	private IRunnableDevice<ScanModel> createScanner(int... size) throws Exception {
		final IPointGenerator<? extends IScanPointGeneratorModel> pointGenerator;
		final IScanPointGeneratorModel model;
		if (size.length == 0) {
			// Static generator for the Malcolm device
			model = new StaticModel();
			pointGenerator = pointGenService.createGenerator(model);
		} else {
			// Step scan generators for any other dimensions
			final List<AxialStepModel> models = new ArrayList<>(size.length);
			for (int dim = 0; dim < size.length; dim++) {
				final double step = size[dim] - 1 > 0 ? 9.99d / (size[dim] - 1) : 30;
				models.add(new AxialStepModel("neXusScannable" + (dim + 1), 10, 20, step));
			}
			model = new CompoundModel(models);
			pointGenerator = pointGenService.createCompoundGenerator((CompoundModel) model);
		}

		// Create the model for a scan.
		final ScanModel scanModel = new ScanModel();
		scanModel.setPointGenerator(pointGenerator);
		scanModel.setDetector(malcolmDevice);
		scanModel.setScanPathModel(model);
		// Cannot set the generator from @PreConfigure in this unit test.
		malcolmDevice.setPointGenerator(pointGenerator);

		// Create a file to scan into.
		scanModel.setFilePath(output.getAbsolutePath());
		System.out.println("File writing to " + scanModel.getFilePath());

		// Create a scan and run it without publishing events
		final IRunnableDevice<ScanModel> scanner = scanService.createScanDevice(scanModel);
		((IRunnableEventDevice<ScanModel>) scanner).addRunListener(new IRunListener() {
			@Override
			public void runWillPerform(RunEvent evt) throws ScanningException {
				System.out.println("Running acquisition scan of size "+pointGenerator.size());
			}
		});

		return scanner;
	}

	private void checkFiles() {
		assertEquals(3, participant.getCount(FileDeclared.class));
		final List<String> paths = participant.getPaths();
		assertTrue(paths.stream().anyMatch(path -> path.endsWith("detector.h5")));
		assertTrue(paths.stream().anyMatch(path -> path.endsWith("detector2.h5")));
	}
}
