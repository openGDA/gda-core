package org.eclipse.scanning.test.scan.nexus;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.scanning.api.annotation.scan.FileDeclared;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MalcolmGridScanTest extends AbstractMalcolmScanTest {

	static Stream<Arguments> parameters() {
		return Stream.of(
				arguments(false, new int[] { 8, 5 }), // 2D grid scan
				arguments(true, new int[] { 8, 5 }), // 2D snake scan
				arguments(true, new int[] { 7, 5 }), // 2D snake with odd number of lines
				arguments(false, new int[] { 3, 2, 5 }), // 3D grid scan
				arguments(true, new int[] { 3, 2, 5 }), // 3D snake scan
				arguments(false, new int[] { 3, 3, 2, 2 }), // 4D malcolm scan
				arguments(false, new int[] { 2, 2, 2, 2, 2 }), // 5D malcolm scan
				arguments(false, new int[] { 2, 2, 2, 2, 2, 2, 2, 2 }) // 8D malcolm scan
		);
	}

	@Override
	protected DummyMalcolmModel createMalcolmModel() {
		final DummyMalcolmModel model = createMalcolmModelTwoDetectors();
		model.setAxesToMove(List.of("stage_x", "stage_y" ));
		model.setPositionerNames(List.of("stage_x", "j1", "j2", "j3"));
		model.setMonitorNames(List.of("i0"));

		return model;
	}

	@ParameterizedTest
	@MethodSource("parameters")
	void testMalcolmScan(boolean snake, int[] shape) throws Exception {
		IRunnableDevice<ScanModel> scanner = createMalcolmGridScan(malcolmDevice, output, snake, shape); // Outer scan of another scannable, for instance temp.
		scanner.run(null);

		checkSize(scanner, shape);
		checkFiles();

		// Check we reached armed (it will normally throw an exception on error)
		assertThat(scanner.getDeviceState(), is(DeviceState.ARMED));
		checkNexusFile(scanner, snake, shape); // Step model is +1 on the size
	}

	private void checkFiles() {
		assertThat(participant.getCount(FileDeclared.class), is(4));

		final List<String> fileNames = participant.getPaths().stream().map(File::new).map(File::getName).collect(toList());
		assertThat(fileNames, containsInAnyOrder(output.getName(), "detector.h5", "detector2.h5", "panda.h5"));
	}

}
