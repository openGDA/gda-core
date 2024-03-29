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

import static java.util.stream.Collectors.toList;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertAxes;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertDiamondScanGroup;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertIndices;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertNXentryMetadata;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertScanNotFinished;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertSignal;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertTarget;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.DarkImageModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DarkCurrentTest extends NexusTest {

	private IWritableDetector<MandelbrotModel> detector;
	private IWritableDetector<DarkImageModel>  dark;

	@BeforeEach
	void before() throws Exception {
		final MandelbrotModel model = createMandelbrotModel();
		detector = TestDetectorHelpers.createAndConfigureMandelbrotDetector(model);
		assertThat(detector, is(notNullValue()));

		final DarkImageModel dmodel = new DarkImageModel();
		dark = TestDetectorHelpers.createAndConfigureDarkImageDetector(dmodel);
		assertThat(dark, is(notNullValue()));
	}

	/**
	 * This test fails if the chunking is not done by the detector.
	 *
	 * @throws Exception
	 */
	@Test
	void testDarkImage() throws Exception {
		// Tell configure detector to write 1 image into a 2D scan
		final IRunnableDevice<ScanModel> scanner = createGridScan(8, 5);
		assertScanNotFinished(getNexusRoot(scanner).getEntry());
		scanner.run(null);

		checkNexusFile(scanner, 8, 5);
	}

	private void checkNexusFile(IRunnableDevice<ScanModel> scanner, int... sizes) throws Exception {
		final ScanModel scanModel = ((AbstractRunnableDevice<ScanModel>) scanner).getModel();
		assertEquals(DeviceState.ARMED, scanner.getDeviceState());

		final NXroot rootNode = getNexusRoot(scanner);
		final NXentry entry = rootNode.getEntry();

		// check that the scan points have been written correctly
		assertNXentryMetadata(entry);
		assertDiamondScanGroup(entry, false, false, sizes);
		assertSolsticeScanMetadata(entry, scanModel.getScanPathModel());

		final Collection<String> positionerNames = scanModel.getPointGenerator().iterator().next().getNames();

		// check the data for the dark detector
		checkDark(rootNode, positionerNames, sizes);

		// check the images from the mandelbrot detector
		checkImages(rootNode, positionerNames, sizes);
	}

	private void checkDark(NXroot rootNode, Collection<String> positionerNames, int... sizes) throws Exception {
		final String detectorName = dark.getName();
		final NXentry entry = rootNode.getEntry();
		final NXdetector detector = entry.getInstrument().getDetector(detectorName);
		assertThat(detector, is(notNullValue()));
		final IDataset ds = detector.getDataNode(NXdetector.NX_DATA).getDataset().getSlice();
		int[] shape = ds.getShape();

		final int size = getScanSize(sizes);

		@SuppressWarnings("unchecked")
		final int frequency = (((AbstractRunnableDevice<DarkImageModel>) dark).getModel().getFrequency());
		assertThat(shape[0], is(equalTo(size / frequency)));

		checkNXdata(rootNode, detectorName, positionerNames);
	}

	private void checkImages(NXroot rootNode, Collection<String> positionerNames, int... sizes) throws Exception {
		final String detectorName = detector.getName();
		final NXentry entry = rootNode.getEntry();
		final NXinstrument instrument = entry.getInstrument();
		final NXdetector detector = instrument.getDetector(detectorName);
		assertThat(detector, is(notNullValue()));

		IDataset ds = detector.getDataNode(NXdetector.NX_DATA).getDataset().getSlice();

		int[] shape = ds.getShape();
		for (int i = 0; i < sizes.length; i++)
			assertThat(shape[i], is(equalTo(sizes[i])));

		// Make sure none of the numbers are NaNs. The detector
		// is expected to fill this scan with non-nulls.
		final PositionIterator it = new PositionIterator(shape);
		while (it.hasNext()) {
			int[] next = it.getPos();
			assertThat(Double.isNaN(ds.getDouble(next)), is(false));
		}

		// Check axes
		// Demand values should be 1D
		int i = -1;
		for (String positionerName : positionerNames) {
			i++;
			final NXpositioner positioner = instrument.getPositioner(positionerName);
			assertThat(positioner, is(notNullValue()));
			ds = positioner.getDataNode(NXpositioner.NX_VALUE + "_set").getDataset().getSlice();
			shape = ds.getShape();
			assertThat(shape, is(equalTo(new int[] { sizes[i] })));

			// Actual values should be scanD
			ds = positioner.getDataNode(NXpositioner.NX_VALUE).getDataset().getSlice();
			shape = ds.getShape();
			assertArrayEquals(sizes, shape);
		}

		checkNXdata(rootNode, detectorName, positionerNames);
	}

	private void checkNXdata(NXroot rootNode, String detectorName, Collection<String> scannableNames) {
		final NXentry entry = rootNode.getEntry();

		final LinkedHashMap<String, List<String>> detectorDataFields = new LinkedHashMap<>();
		if (detectorName.equals("dkExmpl")) {
			detectorDataFields.put(NXdetector.NX_DATA, Arrays.asList("."));
		} else if (detectorName.equals("mandelbrot")) {
			detectorDataFields.put(NXdetector.NX_DATA, Arrays.asList("real", "imaginary"));
			detectorDataFields.put("spectrum", Arrays.asList("spectrum_axis"));
			detectorDataFields.put("value", Collections.emptyList());
		}

		final Map<String, String> expectedDataGroupNamesForDevice =
				detectorDataFields.keySet().stream().collect(Collectors.toMap(Function.identity(),
						x -> detectorName + (x.equals(NXdetector.NX_DATA) ? "" : "_" + x)));

		final Map<String, NXdata> nxDataGroups = entry.getChildren(NXdata.class);
		final List<String> dataGroupNamesForDevice = nxDataGroups.keySet().stream()
				.filter(name -> name.startsWith(detectorName)).collect(toList());
		assertThat(dataGroupNamesForDevice.size(), is(equalTo(detectorDataFields.size())));
		assertThat(dataGroupNamesForDevice, containsInAnyOrder(expectedDataGroupNamesForDevice.values().toArray(String[]::new)));

		for (String dataFieldName : expectedDataGroupNamesForDevice.keySet()) {
			final String nxDataGroupName = expectedDataGroupNamesForDevice.get(dataFieldName);
			final NXdata nxData = nxDataGroups.get(nxDataGroupName);
			assertThat(nxData, is(notNullValue()));

			// check the default data field for the NXdata group
			final String sourceFieldName = nxDataGroupName.equals(detectorName) ? NXdetector.NX_DATA :
				nxDataGroupName.substring(nxDataGroupName.indexOf('_') + 1);
			assertSignal(nxData, sourceFieldName);

			assertSame(nxData.getDataNode(sourceFieldName),
					entry.getInstrument().getDetector(detectorName).getDataNode(sourceFieldName));
			assertTarget(nxData, sourceFieldName, rootNode, "/entry/instrument/" + detectorName
					+ "/" + sourceFieldName);

			// append _value_demand to each name in list
			final List<String> expectedAxesNames = Stream.concat(
					scannableNames.stream().map(x -> x + "_value_set"),
					detectorDataFields.get(sourceFieldName).stream()).collect(Collectors.toList());
			// add placeholder value "." for each additional dimension

			assertAxes(nxData, expectedAxesNames.toArray(new String[expectedAxesNames.size()]));
			int[] defaultDimensionMappings = IntStream.range(0, scannableNames.size()).toArray();

			// check the value_demand and value fields for each scannable
			int i = -1;
			for (String  positionerName : scannableNames) {
			    i++;
				final NXpositioner positioner = entry.getInstrument().getPositioner(positionerName);

				// check value_demand data node
				final String demandFieldName = positionerName + "_" + NXpositioner.NX_VALUE + "_set";
				assertSame(nxData.getDataNode(demandFieldName), positioner.getDataNode("value_set"));
				assertIndices(nxData, demandFieldName, i);
				assertTarget(nxData, demandFieldName, rootNode, "/entry/instrument/" + positionerName
						+ "/value_set");

				// check value data node
				final String valueFieldName = positionerName + "_" + NXpositioner.NX_VALUE;
				assertSame(nxData.getDataNode(valueFieldName),
						positioner.getDataNode(NXpositioner.NX_VALUE));
				assertIndices(nxData, valueFieldName, defaultDimensionMappings);
				assertTarget(nxData, valueFieldName, rootNode, "/entry/instrument/" + positionerName
						+ "/" + NXpositioner.NX_VALUE);
			}
		}
	}

	private IRunnableDevice<ScanModel> createGridScan(int... size) throws Exception {

		// Create scan points for a grid and make a generator
		final TwoAxisGridPointsModel gridModel = new TwoAxisGridPointsModel();
		gridModel.setxAxisName("xNex");
		gridModel.setxAxisPoints(size[size.length-1]);
		gridModel.setyAxisName("yNex");
		gridModel.setyAxisPoints(size[size.length-2]);
		gridModel.setBoundingBox(new BoundingBox(0,0,3,3));

		final CompoundModel compoundModel = createNestedStepScans(2, size);
		compoundModel.addModel(gridModel);

		final IPointGenerator<CompoundModel> pointGen = pointGenService.createCompoundGenerator(compoundModel);

		// Create the model for a scan.
		final ScanModel scanModel = new ScanModel();
		scanModel.setPointGenerator(pointGen);
		scanModel.setScanPathModel(compoundModel);
		scanModel.setDetectors(Arrays.asList(detector, dark));

		// Create a file to scan into.
		scanModel.setFilePath(output.getAbsolutePath());
		System.out.println("File writing to "+scanModel.getFilePath());

		// Create a scan and run it without publishing events
		final IRunnableDevice<ScanModel> scanner = scanService.createScanDevice(scanModel);

		final IPointGenerator<?> fgen = pointGen;
		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(IRunListener.createRunWillPerformListener(
				event -> System.out.println("Running acquisition scan of size "+fgen.size())));

		return scanner;
	}

}
