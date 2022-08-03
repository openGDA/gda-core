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
import static org.eclipse.scanning.example.scannable.MockNeXusScannable.FIELD_NAME_SET_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.PositionIterator;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.ConstantVelocityModel;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.junit.Before;
import org.junit.Test;

public class MonitorTest extends NexusTest {

	private enum MonitorScanRole {
		PER_SCAN, PER_POINT
	}

	private static IWritableDetector<ConstantVelocityModel> detector;

	@Before
	public void before() throws Exception {
		final ConstantVelocityModel model = new ConstantVelocityModel("cv scan", 100, 200, 25);
		model.setName("cv device");

		detector = TestDetectorHelpers.createAndConfigureConstantVelocityDetector(model);
		assertNotNull(detector);

		detector.addRunListener(IRunListener.createRunPerformedListener(evt -> System.out.println("Ran cv device detector @ "+evt.getPosition())));
	}

	@Test
	public void test1DOuter() throws Exception {
		testScan(8);
	}

	@Test
	public void testPerPoint() throws Exception {
		testScan(MonitorScanRole.PER_POINT, MonitorScanRole.PER_POINT, 2); // They all are anyway
	}
	@Test
	public void testPerPointIsPerScanToo() throws Exception {
		testScan(MonitorScanRole.PER_POINT, MonitorScanRole.PER_SCAN, 2); // They all are anyway
	}

	@Test
	public void testPerScan() throws Exception {
		testScan(MonitorScanRole.PER_SCAN, MonitorScanRole.PER_SCAN, 2);
	}
	@Test(expected=AssertionError.class)
	public void testPerScanIsNotPerPoint() throws Exception {
		testScan(MonitorScanRole.PER_SCAN, MonitorScanRole.PER_POINT, 2);
	}

	@Test
	public void testMixture() throws Exception {
		// NOTE That they must be MockNeXusScannables which we test.
		final List<String> perPoint = Arrays.asList("monitor0", "monitor3");
		final List<String> perScan = Arrays.asList("z", "monitor1");

		final IRunnableDevice<ScanModel> scanner = runScan(perPoint, perScan, 5);

		assertPerPointMonitors(scanner, perPoint, 5);
		assertPerScanMonitors(scanner, perScan);
	}


	@Test
	public void test2DOuter() throws Exception {
		testScan(5, 8);
	}

	@Test
	public void test3DOuter() throws Exception {
		testScan(2, 2, 2);
	}

	@Test
	public void test8DOuter() throws Exception {
		testScan(2, 2, 2, 2, 2, 2, 2, 2);
	}

	private void testScan(int... shape) throws Exception {
		testScan(MonitorScanRole.PER_POINT, MonitorScanRole.PER_POINT, shape);
	}


	private void testScan(MonitorScanRole mrole, MonitorScanRole testedRole, int... shape) throws Exception {
		final List<String> monitors = Arrays.asList("monitor1", "monitor2");
		final IRunnableDevice<ScanModel> scanner;
		if (mrole == MonitorScanRole.PER_POINT) {
			scanner = runScan(monitors, null, shape);
		} else {
			scanner = runScan(null, monitors, shape);
		}
		// Check we reached ready (it will normally throw an exception on error)
		checkNexusFile(scanner, monitors, testedRole, shape); // Step model is +1 on the size
	}

	private IRunnableDevice<ScanModel>runScan(List<String> monitorsPerPoint, List<String> monitorsPerScan, int... shape) throws Exception {
		final IRunnableDevice<ScanModel> scanner = createNestedStepScanWithMonitors(detector,
				monitorsPerPoint, monitorsPerScan, shape); // Outer scan of another scannable, for instance temp.
		assertScanNotFinished(getNexusRoot(scanner).getEntry());

		scanner.run(null);
		return scanner;
	}

	private void checkNexusFile(IRunnableDevice<ScanModel> scanner, List<String> monitorNames, MonitorScanRole role,
			int... sizes) throws Exception {
		final ScanModel scanModel = ((AbstractRunnableDevice<ScanModel>) scanner).getModel();
		assertThat(scanner.getDeviceState(), is(DeviceState.ARMED));
		final NXroot rootNode = getNexusRoot(scanner);
		final NXentry entry = rootNode.getEntry();
		final NXinstrument instrument = entry.getInstrument();

		// check that the scan points have been written correctly
		assertNXentryMetadata(entry);
		assertDiamondScanGroup(entry, false, false, sizes);

		final String detectorName = scanModel.getDetectors().get(0).getName();
		final NXdetector detector = instrument.getDetector(detectorName);
		final DataNode dataNode = detector.getDataNode(NXdetector.NX_DATA);
		final IDataset dataset = dataNode.getDataset().getSlice();
		final int[] shape = dataset.getShape();

		// validate the NXdata generated by the NexusDataBuilder
		final NXdata nxData = entry.getData(detectorName);
		assertNotNull(nxData);
		assertSignal(nxData, NXdetector.NX_DATA);
		assertThat(nxData.getDataNode(NXdetector.NX_DATA), is(sameInstance(dataNode)));

		for (int i = 0; i < sizes.length; i++)
			assertEquals(sizes[i], shape[i]);

		// Make sure none of the numbers are NaNs. The detector
		// is expected to fill this scan with non-nulls.
		final PositionIterator it = new PositionIterator(shape);
		while (it.hasNext()) {
			int[] next = it.getPos();
			assertThat(Double.isNaN(dataset.getDouble(next)), is(false));
		}

		// Check axes
		final IPosition pos = scanModel.getPointGenerator().iterator().next();

		// Append _value_demand to each name in scannable names list, and appends
		// the item "." 3 times to the resulting list
		final String[] expectedAxesNames = Stream
				.concat(pos.getNames().stream().map(x -> x + "_value_set"), Collections.nCopies(3, ".").stream())
				.toArray(String[]::new);
		assertAxes(nxData, expectedAxesNames);

		if (role == MonitorScanRole.PER_POINT) {
			assertPerPointMonitors(scanner, monitorNames, sizes);
		} else if (role == MonitorScanRole.PER_SCAN) {
			assertPerScanMonitors(scanner, monitorNames);
		}
	}

	private void assertPerPointMonitors(IRunnableDevice<ScanModel> scanner,
			final List<String> monitorNames, int... sizes) throws Exception {

		final IPosition pos = scanner.getModel().getPointGenerator().iterator().next();
		final NXroot rootNode = getNexusRoot(scanner);
		final NXentry entry = rootNode.getEntry();
		final NXinstrument instrument = entry.getInstrument();
		final String detectorName = scanner.getModel().getDetectors().get(0).getName();
		final NXdata nxData = entry.getData(detectorName);

		final Collection<String> scannableNames = pos.getNames();
		final List<String> allNames = new ArrayList<>(scannableNames);
		allNames.addAll(monitorNames);

		final int[] defaultDimensionMappings = IntStream.range(0, sizes.length).toArray();
		for (int i = 0; i < allNames.size(); i++) {
			final String deviceName = allNames.get(i);
			// This test uses NXpositioner for all scannables and monitors
			final NXpositioner positioner = instrument.getPositioner(deviceName);
			assertThat(positioner, is(notNullValue()));

			DataNode dataNode = positioner.getDataNode(FIELD_NAME_SET_VALUE);
			IDataset dataset = dataNode.getDataset().getSlice();
			int[] shape = dataset.getShape();
			assertThat(shape.length, is(1));
			String nxDataFieldName;
			if (i < scannableNames.size()) {
				// in practise monitors wouldn't have the 'demand' field
				assertThat(shape[0], is(sizes[i]));
				nxDataFieldName = deviceName + "_" + FIELD_NAME_SET_VALUE;
				assertThat(nxData.getDataNode(nxDataFieldName), is(sameInstance(dataNode)));
				assertIndices(nxData, nxDataFieldName, i);
				assertTarget(nxData, nxDataFieldName, rootNode, "/entry/instrument/" + deviceName + "/value_set");
			}

			// Actual values should be scanD
			dataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
			dataset = dataNode.getDataset().getSlice();
			shape = dataset.getShape();
			assertThat("The value of monitor, '" + deviceName + "' is incorrect", shape, is(equalTo(sizes)));

			nxDataFieldName = deviceName + "_" + NXpositioner.NX_VALUE;
			assertThat(nxData.getDataNode(nxDataFieldName), is(sameInstance(dataNode)));
			assertIndices(nxData, nxDataFieldName, defaultDimensionMappings);
			assertTarget(nxData, nxDataFieldName, rootNode,
					"/entry/instrument/" + deviceName + "/" + NXpositioner.NX_VALUE);
		}
	}

	private void assertPerScanMonitors(IRunnableDevice<ScanModel> scanner, List<String> perScanNames) throws Exception {
		final NXroot rootNode = getNexusRoot(scanner);
		final NXentry entry = rootNode.getEntry();
		final NXinstrument instrument = entry.getInstrument();

		final Collection<IScannable<?>> perScanMonitors = scanner.getModel().getMonitorsPerScan();

		// check each metadata scannable has been written correctly
		for (IScannable<?> scannable : perScanMonitors) {
			final String name = scannable.getName();
			assertThat(perScanNames.contains(name), is(true));

			final NXobject nexusObject = (NXobject) instrument.getGroupNode(name);

			// Check that the nexus object is of the expected base class
			assertThat("The scannable '"+name+"' could not be found.", nexusObject, is(notNullValue()));
			assertThat(nexusObject.getString("name"), is(equalTo(name)));
			assertThat(nexusObject.getNumberOfGroupNodes(), is(0));
			assertThat(nexusObject.getNumberOfAttributes(), is(1));

			final DataNode dataNode = nexusObject.getDataNode("value");
			final IDataset dataset = dataNode.getDataset().getSlice();

			assertThat(dataset.getRank(), is(0)); // A scalar value not the shape of the scan.
		}
	}

	private IRunnableDevice<ScanModel> createNestedStepScanWithMonitors(final IRunnableDevice<ConstantVelocityModel> detector, List<String> monitorNamesPerPoint, List<String> monitorNamesPerScan, int... size) throws Exception {

		// Create scan points for a grid and make a generator
		final int ySize = size[size.length-1];
		final AxialStepModel stepModel = new AxialStepModel("yNex", 10,20, ySize > 1 ? 11d/ySize : 30); // N points or 1
		final IPointGenerator<AxialStepModel> stepGen = pointGenService.createGenerator(stepModel);
		assertEquals(ySize, stepGen.size());

		final CompoundModel compoundModel = createNestedStepScans(1, size);
		compoundModel.addModel(stepModel);

		final IPointGenerator<CompoundModel> pointGen = pointGenService.createCompoundGenerator(compoundModel);

		// Create the model for a scan.
		final ScanModel scanModel = new ScanModel();
		scanModel.setPointGenerator(pointGen);
		scanModel.setScanPathModel(compoundModel);
		scanModel.setDetector(detector);
		scanModel.setMonitorsPerPoint(createMonitors(monitorNamesPerPoint));
		scanModel.setMonitorsPerScan(createMonitors(monitorNamesPerScan));

		// Create a file to scan into.
		scanModel.setFilePath(output.getAbsolutePath());
		System.out.println("File writing to "+scanModel.getFilePath());

		// Create a scan and run it without publishing events
		final IRunnableDevice<ScanModel> scanner = scanService.createScanDevice(scanModel);
		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(IRunListener.createRunWillPerformListener(
				event -> System.out.println("Running acquisition scan of size " + pointGen.size())));

		return scanner;
	}

	private List<IScannable<?>> createMonitors(List<String> monitorNames) throws ScanningException {
		if (monitorNames == null) return null;
		final List<IScannable<?>> ret = new ArrayList<IScannable<?>>(monitorNames.size());
		for (String name : monitorNames) ret.add(connector.getScannable(name));
		return ret;
	}


	public static INexusFileFactory getFileFactory() {
		return fileFactory;
	}

	public static void setFileFactory(INexusFileFactory fileFactory) {
		NexusTest.fileFactory = fileFactory;
	}

}
