package org.eclipse.scanning.test.scan.nexus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.scanning.api.annotation.scan.FileDeclared;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.eclipse.scanning.malcolm.core.AbstractMalcolmDevice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class MalcolmGridScanTest extends AbstractMalcolmScanTest {

	public int numDims;

	public boolean snake;

	public int[] shape;

	@Parameters(name="{0}D scan, snake={1}, shape={2}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{ 2, false, Arrays.asList( 8, 5 )}, // 2D grid scan
			{ 2, true, Arrays.asList( 8, 5 )},  // 2D snake scan
			{ 2, true, Arrays.asList( 7, 5 )}, // 2D snake with odd number of lines
			{ 3, false, Arrays.asList( 3, 2, 5 )}, // 3D grid scan
			{ 3, true, Arrays.asList( 3, 2, 5 )}, // 3D snake scan
			{ 4, false, Arrays.asList( 3, 3, 2, 2 )}, // 4D malcolm scan
			{ 5, false, Arrays.asList( 1, 1, 1, 2, 2 )}, // 5D malcolm scan
			{ 8, false, Arrays.asList( 1, 1, 1, 1, 1, 1, 2, 2 )} // 8D malcolm scan
		});
	}

	public MalcolmGridScanTest(int numDims, boolean snake, List<Integer> shape) {
		this.numDims = numDims;
		this.snake = snake;
		this.shape = shape.stream().mapToInt(i -> i).toArray(); // param is list so that method name is correct
	}

	@Override
	protected DummyMalcolmModel createMalcolmModel() {
		final DummyMalcolmModel model = createMalcolmModelTwoDetectors();
		model.setAxesToMove(Arrays.asList("stage_x", "stage_y" ));
		model.setPositionerNames(Arrays.asList("stage_x", "j1", "j2", "j3"));
		model.setMonitorNames(Arrays.asList("i0"));

		return model;
	}


	@Test
	public void testMalcolmScan() throws Exception {
		IRunnableDevice<ScanModel> scanner = createMalcolmGridScan(malcolmDevice, output, snake, shape); // Outer scan of another scannable, for instance temp.
		scanner.run(null);

		checkSize(scanner, shape);
		checkFiles();

		// Check we reached armed (it will normally throw an exception on error)
		assertEquals(DeviceState.ARMED, scanner.getDeviceState());
		checkNexusFile(scanner, snake, shape); // Step model is +1 on the size
	}

	private IRunnableDevice<ScanModel> createMalcolmGridScan(final IRunnableDevice<?> malcolmDevice, File file, boolean snake, int... size) throws Exception {

		// Create scan points for a grid and make a generator
		GridModel gmodel = new GridModel(); // Note stage_x and stage_y scannables controlled by malcolm
		gmodel.setxAxisName("stage_x");
		gmodel.setxAxisPoints(size[size.length-1]);
		gmodel.setyAxisName("stage_y");
		gmodel.setyAxisPoints(size[size.length-2]);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));
		gmodel.setSnake(snake);

		IPointGenerator<?> gen = pointGenService.createGenerator(gmodel);

		IPointGenerator<?>[] gens = new IPointGenerator<?>[size.length - 1];
		if (size.length > 2) {
			for (int dim = size.length - 3; dim > -1; dim--) {
				final StepModel model;
				if (size[dim]-1>0) {
					model = new StepModel("neXusScannable"+(dim+1), 10,20,9.99d/(size[dim]-1));
				} else {
					model = new StepModel("neXusScannable"+(dim+1), 10,20,30);
				}
				final IPointGenerator<?> step = pointGenService.createGenerator(model);
				gens[dim] = step;
			}
		}
		gens[size.length - 2] = gen;

		gen = pointGenService.createCompoundGenerator(gens);

		// Create the model for a scan.
		final ScanModel smodel = new ScanModel();
		smodel.setPointGenerator(gen);
		smodel.setDetectors(malcolmDevice);
		// Cannot set the generator from @PreConfigure in this unit test.
		((AbstractMalcolmDevice<?>)malcolmDevice).setPointGenerator(gen);

		// Create a file to scan into.
		smodel.setFilePath(file.getAbsolutePath());
		System.out.println("File writing to " + smodel.getFilePath());

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = runnableDeviceService.createRunnableDevice(smodel, null);

		final IPointGenerator<?> fgen = gen;
		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(new IRunListener() {
			@Override
			public void runWillPerform(RunEvent evt) throws ScanningException {
				try {
					System.out.println("Running acquisition size of scan "+fgen.size());
				} catch (GeneratorException e) {
					throw new ScanningException(e);
				}
			}
		});

		return scanner;
	}

	private void checkFiles() {
		assertEquals(4, participant.getCount(FileDeclared.class));
		List<String> paths = participant.getPaths();
		assertTrue(paths.stream().anyMatch(path -> path.endsWith("detector.h5")));
		assertTrue(paths.stream().anyMatch(path -> path.endsWith("detector2.h5")));
		assertTrue(paths.stream().anyMatch(path -> path.endsWith("panda.h5")));
	}
}
