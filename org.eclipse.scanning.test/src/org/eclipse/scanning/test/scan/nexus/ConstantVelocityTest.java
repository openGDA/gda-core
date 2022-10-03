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
package org.eclipse.scanning.test.scan.nexus;

import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertAxes;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertDiamondScanGroup;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertIndices;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertNXentryMetadata;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertScanNotFinished;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertSignal;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertTarget;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.PositionIterator;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.ConstantVelocityModel;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ConstantVelocityTest extends NexusTest {

	private static IWritableDetector<ConstantVelocityModel> detector;

	@BeforeEach
	void before() throws Exception {
		final ConstantVelocityModel model = new ConstantVelocityModel("cv scan", 100, 200, 25);
		model.setName("cv device");

		detector = TestDetectorHelpers.createAndConfigureConstantVelocityDetector(model);
		assertNotNull(detector);

		detector.addRunListener(IRunListener.createRunPerformedListener(
				event -> System.out.println("Ran cv device detector @ " + event.getPosition())));
	}

	static Stream<List<Integer>> scanDimensions() {
		return Stream.of(
				List.of(8),
				List.of(5, 8),
				List.of(2, 2, 2),
				List.of(2, 2, 2, 2, 2, 2, 2, 2)
			);
	}

	@ParameterizedTest(name="scanDimensions={0}")
	@MethodSource("scanDimensions")
	void testScan(List<Integer> scanDimensions) throws Exception {
		final int[] shape = scanDimensions.stream().mapToInt(Integer::intValue).toArray();
		final IRunnableDevice<ScanModel> scanner = createNestedStepScan(detector, shape); // Outer scan of another scannable, for instance temp.
		assertScanNotFinished(getNexusRoot(scanner).getEntry());
		scanner.run(null);

		// Check we reached ready (it will normally throw an exception on error)
        checkNexusFile(scanner, shape); // Step model is +1 on the size
	}

	private void checkNexusFile(IRunnableDevice<ScanModel> scanner, int... sizes) throws Exception {
		final ScanModel scanModel = ((AbstractRunnableDevice<ScanModel>)scanner).getModel();
		assertThat(scanner.getDeviceState(), is(DeviceState.ARMED));
		final NXroot rootNode = getNexusRoot(scanner);
		final NXentry entry = rootNode.getEntry();
		final NXinstrument instrument = entry.getInstrument();

		// check that the scan points have been written correctly
		assertNXentryMetadata(entry);
		assertDiamondScanGroup(entry, false, false, sizes);

		final String detectorName = scanModel.getDetectors().get(0).getName();
		final NXdetector detector = instrument.getDetector(detectorName);
		DataNode dataNode = detector.getDataNode(NXdetector.NX_DATA);
		IDataset dataset = dataNode.getDataset().getSlice();
		final int[] shape = dataset.getShape();

		// validate the NXdata generated by the NexusDataBuilder
		final NXdata nxData = entry.getData(detectorName);
		assertSignal(nxData, NXdetector.NX_DATA);

		for (int i = 0; i < sizes.length; i++)
			assertEquals(sizes[i], shape[i]);

		// Make sure none of the numbers are NaNs. The detector
		// is expected to fill this scan with non-nulls.
        final PositionIterator it = new PositionIterator(shape);
        while(it.hasNext()) {
		int[] next = it.getPos();
			assertThat(Double.isNaN(dataset.getDouble(next)), is(false));
        }

		// Check axes
        final IPosition pos = scanModel.getPointGenerator().iterator().next();
        final Collection<String> scannableNames = pos.getNames();

        // Append _value_demand to each name in list, and append items ".", "." to list
        String[] expectedAxesNames = Stream.concat(scannableNames.stream().map(x -> x + "_value_set"),
			Collections.nCopies(3, ".").stream()).toArray(String[]::new);
        assertAxes(nxData, expectedAxesNames);

        int[] defaultDimensionMappings = IntStream.range(0, sizes.length).toArray();
		int i = -1;
		for (String scannableName : scannableNames) {
			i++;
			NXpositioner positioner = instrument.getPositioner(scannableName);
			assertThat(positioner, is(notNullValue()));

			dataNode = positioner.getDataNode("value_set");
			dataset = dataNode.getDataset().getSlice();
			assertThat(dataset.getShape(), is(equalTo(new int[] { sizes[i] })));

			String nxDataFieldName = scannableName + "_value_set";
			assertSame(dataNode, nxData.getDataNode(nxDataFieldName));
			assertIndices(nxData, nxDataFieldName, i);
			assertTarget(nxData, nxDataFieldName, rootNode, "/entry/instrument/" + scannableName + "/value_set");

			// Actual values should be scanD
			dataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
			dataset = dataNode.getDataset().getSlice();
			assertThat(dataset.getShape(), is(equalTo(sizes)));

			nxDataFieldName = scannableName + "_" + NXpositioner.NX_VALUE;
			assertSame(dataNode, nxData.getDataNode(nxDataFieldName));
			assertIndices(nxData, nxDataFieldName, defaultDimensionMappings);
			assertTarget(nxData, nxDataFieldName, rootNode,
					"/entry/instrument/" + scannableName + "/" + NXpositioner.NX_VALUE);
		}
	}

	private IRunnableDevice<ScanModel> createNestedStepScan(final IRunnableDevice<? extends IDetectorModel> detector, int... size) throws Exception {

		// Create scan points for a grid and make a generator
		final AxialStepModel smodel;
		final int ySize = size[size.length-1];
		smodel = new AxialStepModel("yNex", 10,20, ySize > 1 ? 11d/ySize : 30); // N many points or 1

		final IPointGenerator<AxialStepModel> stepGen = pointGenService.createGenerator(smodel);
		assertThat(stepGen.size(), is(equalTo(ySize)));

		final CompoundModel compoundModel = createNestedStepScans(1, size);
		compoundModel.addModel(smodel);

		final IPointGenerator<CompoundModel> pointGen = pointGenService.createCompoundGenerator(compoundModel);

		// Create the model for a scan.
		final ScanModel  scanModel = new ScanModel();
		scanModel.setPointGenerator(pointGen);
		scanModel.setScanPathModel(compoundModel);
		scanModel.setDetector(detector);

		// Create a file to scan into.
		scanModel.setFilePath(output.getAbsolutePath());
		System.out.println("File writing to "+scanModel.getFilePath());

		// Create a scan and run it without publishing events
		final IRunnableDevice<ScanModel> scanner = scanService.createScanDevice(scanModel);

		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(IRunListener.createRunWillPerformListener(
				event -> System.out.println("Running acquisition scan of size " + pointGen.size())));

		return scanner;
	}

}
