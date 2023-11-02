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
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertTarget;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.IntStream;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BasicScanTest extends NexusTest {

	private IScannable<?> monitor;

	@BeforeEach
	void beforeTest() throws Exception {
		monitor = connector.getScannable("monitor1");
	}

	@Test
	void testBasicScan1D() throws Exception {
		test(null, null, 5);
	}

	@Test
	void testBasicScan2D() throws Exception {
		test(null, null, 8, 5);
	}

	@Test
	void testBasicScan3D() throws Exception {
		test(null, null, 5, 8, 5);
	}

	@Test
	void testBasicScan1DWithMonitor() throws Exception {
		test(monitor, null, 5);
	}

	/**
	 * This is an important test, it showed a race condition
	 * in the multi-threaded positioning, do not ignore :)
	 *
	 * @throws Exception
	 */
	@Test
	void testBasicScan1DWithMonitorMultipleTimes() throws Exception {

		for (int i = 0; i < 5; i++) {
			//System.out.println("Iteration "+i+":");
			test(monitor, null, 5);
		}
	}

	@Test
	void testBasicScan2DWithMonitor() throws Exception {
		test(monitor, null, 8, 5);
	}

	@Test
	void testBasicScan3DWithMonitor() throws Exception {
		test(monitor, null, 5, 8, 5);
	}

	private void test(IScannable<?> monitorPerPoint, IScannable<?> monitorPerScan, int... shape) throws Exception {
		final long before = System.currentTimeMillis();
		// Tell configure detector to write 1 image into a 2D scan
		final IRunnableDevice<ScanModel> scanner = createStepScan(monitorPerPoint, monitorPerScan, shape);
		assertScanNotFinished(getNexusRoot(scanner).getEntry());
		scanner.run(null);
		final long after = System.currentTimeMillis();
		System.out.println("Running "+ getScanSize(shape)+" points took "+(after-before)+" ms");

		checkNexusFile(scanner, shape);
	}

	protected void checkNexusFile(IRunnableDevice<ScanModel> scanner, int... sizes) throws Exception {
		final ScanModel scanModel = ((AbstractRunnableDevice<ScanModel>) scanner).getModel();
		final NXroot rootNode = getNexusRoot(scanner);
		final NXentry entry = rootNode.getEntry();
		final NXinstrument instrument = entry.getInstrument();

		// check the scan points have been written correctly
		assertNXentryMetadata(entry);
		assertDiamondScanGroup(entry, false, false, sizes);
		assertSolsticeScanMetadata(entry, scanModel.getScanPathModel());

		// check metadata scannables
		checkMetadataScannables(scanModel, instrument);

		final IPosition pos = scanModel.getPointGenerator().iterator().next();
		final Collection<String> scannableNames = pos.getNames();
		final Optional<IScannable<?>> firstMonitor = scanModel.getMonitorsPerPoint().stream().findFirst();
        final String dataGroupName = firstMonitor.map(INameable::getName).orElse(pos.getNames().get(0));
		final NXdata nxData = entry.getData(dataGroupName);
		assertThat(nxData, is(notNullValue()));

		// Check axes
		final String[] expectedAxesNames = scannableNames.stream().map(x -> x + "_value_set").toArray(String[]::new);
		assertAxes(nxData, expectedAxesNames);

		final int[] defaultDimensionMappings = IntStream.range(0, sizes.length).toArray();
		int i = -1;
		DataNode dataNode = null;
		IDataset dataset = null;
		int[] shape = null;
		for (String  scannableName : scannableNames) {
		    i++;
			NXpositioner positioner = instrument.getPositioner(scannableName);
			assertThat(positioner, is(notNullValue()));

			dataNode = positioner.getDataNode("value_set");
			dataset = dataNode.getDataset().getSlice();
			shape = dataset.getShape();
			assertThat(dataset.getShape(), is(equalTo(new int[] { sizes[i] })));

			String nxDataFieldName = scannableName + "_value_set";
			assertSame(dataNode, nxData.getDataNode(nxDataFieldName));
			assertIndices(nxData, nxDataFieldName, i);
			assertTarget(nxData, nxDataFieldName, rootNode,
					"/entry/instrument/" + scannableName + "/value_set");

			// Actual values should be scanD
			dataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
			dataset = dataNode.getDataset().getSlice();
			shape = dataset.getShape();
			assertArrayEquals(sizes, shape);

			nxDataFieldName = scannableName + "_" + NXpositioner.NX_VALUE;
			assertSame(dataNode, nxData.getDataNode(nxDataFieldName));
			assertIndices(nxData, nxDataFieldName, defaultDimensionMappings);
			assertTarget(nxData, nxDataFieldName, rootNode,
					"/entry/instrument/" + scannableName + "/" + NXpositioner.NX_VALUE);
		}
	}

	private void checkMetadataScannables(final ScanModel scanModel, NXinstrument instrument) throws DatasetException {
		if (scanModel.getMonitorsPerScan() == null) return;

        for (IScannable<?> metadataScannable : scanModel.getMonitorsPerScan()) {
			NXpositioner positioner = instrument.getPositioner(metadataScannable.getName());
			assertThat(positioner, is(nullValue()));
			assertThat(positioner.getNameScalar(), is(equalTo(metadataScannable.getName())));

			DataNode dataNode = positioner.getDataNode("value_set"); // TODO should not be here for metadata scannable
			assertThat(dataNode, is(notNullValue()));
			Dataset dataset = DatasetUtils.sliceAndConvertLazyDataset(dataNode.getDataset());
			assertThat(dataset.getSize(), is(1));
			assertThat(dataset, is(instanceOf(DoubleDataset.class)));
			assertEquals(10.0, dataset.getElementDoubleAbs(0), 1e-15);

			dataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
			assertThat(dataNode, is(notNullValue()));
			dataset = DatasetUtils.sliceAndConvertLazyDataset(dataNode.getDataset());
			assertThat(dataset.getSize(), is(1));
			assertThat(dataset, is(instanceOf(DoubleDataset.class)));
			assertEquals(10.0, dataset.getElementDoubleAbs(0), 1e-15);
		}
	}

	private IRunnableDevice<ScanModel> createStepScan(
			IScannable<?> monitorPerPoint, IScannable<?> monitorPerScan, int... size) throws Exception {

		final CompoundModel compoundModel = createNestedStepScans(0, size);

		final IPointGenerator<CompoundModel> pointGen = pointGenService.createCompoundGenerator(compoundModel);

		// Create the model for a scan.
		final ScanModel  scanModel = new ScanModel();
		scanModel.setPointGenerator(pointGen);
		scanModel.setScanPathModel(compoundModel);
		if (monitorPerScan != null) {
			monitorPerScan.setActivated(true);
		}
		scanModel.setMonitorsPerPoint(monitorPerPoint);
		scanModel.setMonitorsPerScan(monitorPerScan);

		// Create a file to scan into.
		scanModel.setFilePath(output.getAbsolutePath());
		System.out.println("File writing to " + scanModel.getFilePath());

		// Create a scan and run it without publishing events
		final IRunnableDevice<ScanModel> scanner = scanService.createScanDevice(scanModel);

		final IPointGenerator<CompoundModel> fgen = pointGen;
		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(IRunListener.createRunWillPerformListener(
				event -> System.out.println("Running acquisition scan of size "+fgen.size())));

		return scanner;
	}

	public static INexusFileFactory getFileFactory() {
		return fileFactory;
	}

	public static void setFileFactory(INexusFileFactory fileFactory) {
		NexusTest.fileFactory = fileFactory;
	}

}
