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
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertIndices;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertNXentryMetadata;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertScanNotFinished;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertSolsticeScanGroup;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertTarget;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.SCANNABLE_NAME_SOLSTICE_SCAN_MONITOR;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
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
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.points.ServiceHolder;
import org.eclipse.scanning.points.validation.ValidatorService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BasicScanTest extends NexusTest {

    private IScannable<?> monitor;

    @BeforeClass
	public static void beforeClass() {
		final ServiceHolder serviceHolder = new ServiceHolder();
		serviceHolder.setValidatorService(new ValidatorService());
		serviceHolder.setPointGeneratorService(new PointGeneratorService());
	}

    @Before
	public void beforeTest() throws Exception {
		monitor = connector.getScannable("monitor1");
	}

	@Test
	public void testBasicScan1D() throws Exception {
		test(null, null, 5);
	}

	@Test
	public void testBasicScan2D() throws Exception {
		test(null, null, 8, 5);
	}

	@Test
	public void testBasicScan3D() throws Exception {
		test(null, null, 5, 8, 5);
	}

	@Test
	public void testBasicScan1DWithMonitor() throws Exception {
		test(monitor, null, 5);
	}

	/**
	 * This is an important test, it showed a race condition
	 * in the multi-threaded positioning, do not ignore :)
	 *
	 * @throws Exception
	 */
	@Test
	public void testBasicScan1DWithMonitorMultipleTimes() throws Exception {

		for (int i = 0; i < 5; i++) {
			//System.out.println("Iteration "+i+":");
			test(monitor, null, 5);
		}
	}

	@Test
	public void testBasicScan2DWithMonitor() throws Exception {
		test(monitor, null, 8, 5);
	}

	@Test
	public void testBasicScan3DWithMonitor() throws Exception {
		test(monitor, null, 5, 8, 5);
	}

	private void test(IScannable<?> monitorPerPoint, IScannable<?> monitorPerScan, int... shape) throws Exception {

		long before = System.currentTimeMillis();
		// Tell configure detector to write 1 image into a 2D scan
		IRunnableDevice<ScanModel> scanner = createStepScan(monitorPerPoint, monitorPerScan, shape);
		assertScanNotFinished(getNexusRoot(scanner).getEntry());
		scanner.run(null);
		long after = System.currentTimeMillis();
		System.out.println("Running "+product(shape)+" points took "+(after-before)+" ms");

		checkNexusFile(scanner, shape);
	}

	private int product(int[] shape) {
		int total = 1;
		for (int i : shape) total*=i;
		return total;
	}

	protected void checkNexusFile(IRunnableDevice<ScanModel> scanner, int... sizes) throws Exception {

		final ScanModel scanModel = ((AbstractRunnableDevice<ScanModel>) scanner).getModel();

		NXroot rootNode = getNexusRoot(scanner);
		NXentry entry = rootNode.getEntry();
		NXinstrument instrument = entry.getInstrument();

		// check the scan points have been written correctly
		assertNXentryMetadata(entry);
		assertSolsticeScanGroup(entry, false, false, sizes);

		DataNode dataNode = null;
		IDataset dataset = null;
		int[] shape = null;

		// check metadata scannables
		checkMetadataScannables(scanModel, instrument);

		final IPosition pos = scanModel.getPointGenerator().iterator().next();
		final Collection<String> scannableNames = pos.getNames();

		List<IScannable<?>> perPoint  = scanModel.getMonitorsPerPoint() != null
                ? scanModel.getMonitorsPerPoint().stream()
				.filter(scannable -> !scannable.getName().equals(SCANNABLE_NAME_SOLSTICE_SCAN_MONITOR)).collect(Collectors.toList())
                : null;
        final boolean hasMonitor = perPoint != null && !perPoint.isEmpty();

		String dataGroupName = hasMonitor ? perPoint.get(0).getName() : pos.getNames().get(0);
		NXdata nxData = entry.getData(dataGroupName);
		assertNotNull(nxData);

		// Check axes
		String[] expectedAxesNames = scannableNames.stream().map(x -> x + "_value_set").toArray(String[]::new);
		assertAxes(nxData, expectedAxesNames);

		int[] defaultDimensionMappings = IntStream.range(0, sizes.length).toArray();
		int i = -1;
		for (String  scannableName : scannableNames) {
		    i++;
			NXpositioner positioner = instrument.getPositioner(scannableName);
			assertNotNull(positioner);

			dataNode = positioner.getDataNode("value_set");
			dataset = dataNode.getDataset().getSlice();
			shape = dataset.getShape();
			assertEquals(1, shape.length);
			assertEquals(sizes[i], shape[0]);

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
		DataNode dataNode;
		Dataset dataset;

		if (scanModel.getMonitorsPerScan() == null) return;

        for (IScannable<?> metadataScannable : scanModel.getMonitorsPerScan()) {
			NXpositioner positioner = instrument.getPositioner(metadataScannable.getName());
			assertNotNull(positioner);
			assertEquals(metadataScannable.getName(), positioner.getNameScalar());

			dataNode = positioner.getDataNode("value_set"); // TODO should not be here for metadata scannable
			assertNotNull(dataNode);
			dataset = DatasetUtils.sliceAndConvertLazyDataset(dataNode.getDataset());
			assertEquals(1, dataset.getSize());
			assertTrue(dataset instanceof DoubleDataset);
			assertEquals(10.0, dataset.getElementDoubleAbs(0), 1e-15);

			dataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
			assertNotNull(dataNode);
			dataset = DatasetUtils.sliceAndConvertLazyDataset(dataNode.getDataset());
			assertEquals(1, dataset.getSize());
			assertTrue(dataset instanceof DoubleDataset);
			assertEquals(10.0, dataset.getElementDoubleAbs(0), 1e-15);
		}
	}

	private IRunnableDevice<ScanModel> createStepScan(IScannable<?> monitorPerPoint,
			                                          IScannable<?> monitorPerScan,
			                                          int... size) throws Exception {

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
		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(new IRunListener() {
			@Override
			public void runWillPerform(RunEvent evt) throws ScanningException{
				System.out.println("Running acquisition scan of size "+fgen.size());
			}
		});

		return scanner;
	}

	public static INexusFileFactory getFileFactory() {
		return fileFactory;
	}

	public static void setFileFactory(INexusFileFactory fileFactory) {
		NexusTest.fileFactory = fileFactory;
	}

}
