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

import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.SCANNABLE_NAME_SOLSTICE_SCAN_MONITOR;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertAxes;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertIndices;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertNXentryMetadata;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertScanNotFinished;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertSolsticeScanGroup;
import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertTarget;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NXslit;
import org.eclipse.dawnsci.nexus.NXuser;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.device.NexusMetadataDevice;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.scanning.api.AbstractScannable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.scannable.MockNeXusSlit;
import org.eclipse.scanning.example.scannable.MockScannableConfiguration;
import org.eclipse.scanning.example.scannable.MockScannableConnector;
import org.eclipse.scanning.sequencer.ServiceHolder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PerScanMonitorTest extends NexusTest {

	@Before
	public void beforeTest() throws Exception {
		// Make a few detectors and models...
		MockScannableConfiguration dcsModel = new MockScannableConfiguration();
		dcsModel.setXGapName("s1gapX");
		dcsModel.setYGapName("s1gapY");
		dcsModel.setXCentreName("s1cenX");
		dcsModel.setYCentreName("s1cenY");
		MockNeXusSlit dummyConfiguredScannable = new MockNeXusSlit();
		dummyConfiguredScannable.setName("dcs");
		dummyConfiguredScannable.setModel(dcsModel);
		dummyConfiguredScannable.setActivated(true);
		dummyConfiguredScannable.register();

		((MockScannableConnector) connector).setCreateIfNotThere(false);
		// TODO See this scannable which is a MockNeXusSlit
		// Use NexusNodeFactory to create children as correct for http://confluence.diamond.ac.uk/pages/viewpage.action?pageId=37814632
		IScannable<Number> dcs = connector.getScannable("dcs"); // Scannable created by spring with a model.
		dcs.setPosition(10.0);
		((MockScannableConnector) connector).setGlobalPerScanMonitorNames();

		final NexusMetadataDevice<NXuser> userNexusDevice = new NexusMetadataDevice("user", NexusBaseClass.NX_USER);
		final Map<String, Object> userData = new HashMap<>();
		userData.put(NXuser.NX_NAME, "John Smith");
		userData.put(NXuser.NX_ROLE, "Beamline Scientist");
		userData.put(NXuser.NX_ADDRESS, "Diamond Light Source, Didcot, Oxfordshire, OX11 0DE");
		userData.put(NXuser.NX_EMAIL, "john.smith@diamond.ac.uk");
		userData.put(NXuser.NX_FACILITY_USER_ID, "wgp76868");
		userNexusDevice.setNexusMetadata(userData);
		ServiceHolder.getNexusDeviceService().register(userNexusDevice);
	}

	@Test
	public void modelCheck() throws Exception {
		MockScannableConfiguration conf = new MockScannableConfiguration("s1gapX", "s1gapY", "s1cenX", "s1cenY");
		assertEquals(conf, ((AbstractScannable<?>)connector.getScannable("dcs")).getModel());
	}

	@Test
	public void testBasicScanWithPerPointAndPerScanMonitors() throws Exception {
		test("monitor1", "perScanMonitor1");
	}

	@Test
	public void testBasicScanWithPerScanMonitor() throws Exception {
		test(null, "perScanMonitor1");
	}

	@Test
	public void testBasicScanWithStringPerScanMonitor() throws Exception {
		test(null, "stringPerScanMonitor");
	}

	@Test
	public void testBasicScanWithLegacyPerScanMonitor() throws Exception {
		((MockScannableConnector) connector).setGlobalPerScanMonitorNames("perScanMonitor2");
		test("monitor1", "perScanMonitor1", "perScanMonitor2");
	}

	@Test
	public void testBasicScanWithLegacyAndPrerequisitePerScanMonitors() throws Exception {
		((MockScannableConnector) connector).setGlobalPerScanMonitorPrerequisiteNames("perScanMonitor1", "perScanMonitor2");
		((MockScannableConnector) connector).setGlobalPerScanMonitorNames("perScanMonitor3");
		((MockScannableConnector) connector).setGlobalPerScanMonitorPrerequisiteNames("perScanMonitor3", "perScanMonitor4", "perScanMonitor5");
		((MockScannableConnector) connector).setGlobalPerScanMonitorPrerequisiteNames("perScanMonitor5", "perScanMonitor6");
		test("monitor1", "perScanMonitor1", "perScanMonitor2",
				"perScanMonitor3", "perScanMonitor4", "perScanMonitor5", "perScanMonitor6");
	}

	@Test
	public void testScanWithConfiguredScannable() throws Exception {
		test("monitor1", "dcs");
	}

	@Test
	public void testBasicScanWithNexusDevice() throws Exception {
		test(null, "user");
	}

	private void test(String perPointMonitorName, String perScanMonitorName,
			String... additionalExpectedPerScanMonitorNames) throws Exception {
		int[] shape = new int[] { 8, 5 };
		long before = System.currentTimeMillis();
		// Tell configure detector to write 1 image into a 2D scan
		IRunnableDevice<ScanModel> scanner = createStepScan(perPointMonitorName, perScanMonitorName, shape);
		assertScanNotFinished(getNexusRoot(scanner).getEntry());
		scanner.run(null);
		long after = System.currentTimeMillis();
		System.out.println("Running "+product(shape)+" points took "+(after-before)+" ms");

		final String[] expectedPerScanMonitorNames;
		if (additionalExpectedPerScanMonitorNames.length == 0) {
			expectedPerScanMonitorNames = new String[] { perScanMonitorName };
		} else {
			expectedPerScanMonitorNames = new String[additionalExpectedPerScanMonitorNames.length + 1];
			expectedPerScanMonitorNames[0] = perScanMonitorName;
			System.arraycopy(additionalExpectedPerScanMonitorNames, 0, expectedPerScanMonitorNames, 1, additionalExpectedPerScanMonitorNames.length);
		}

		checkNexusFile(scanner, shape, expectedPerScanMonitorNames);
	}

	private int product(int[] shape) {
		int total = 1;
		for (int i : shape) total*=i;
		return total;
	}

	private void checkNexusFile(IRunnableDevice<ScanModel> scanner, int[] sizes,
			String[] expectedPerScanMonitorNames) throws Exception {

		final ScanModel scanModel = ((AbstractRunnableDevice<ScanModel>) scanner).getModel();

		final NXroot rootNode = getNexusRoot(scanner);
		final NXentry entry = rootNode.getEntry();
		final NXinstrument instrument = entry.getInstrument();

		// check the scan points have been written correctly
		assertNXentryMetadata(entry);
		assertSolsticeScanGroup(entry, false, false, sizes);

		DataNode dataNode = null;
		IDataset dataset = null;
		int[] shape = null;

		// check metadata scannables
		checkPerScanMonitors(scanModel, entry, new HashSet<>(Arrays.asList(expectedPerScanMonitorNames)));

		final IPosition pos = scanModel.getPointGenerator().iterator().next();
		final List<String> scannableNames = pos.getNames();

		List<IScannable<?>> perPoint  = scanModel.getMonitorsPerPoint().stream()
				.filter(scannable -> !scannable.getName().equals(SCANNABLE_NAME_SOLSTICE_SCAN_MONITOR))
				.collect(Collectors.toList());
        final boolean hasMonitor = perPoint != null && !perPoint.isEmpty();

		final String dataGroupName = hasMonitor ? perPoint.get(0).getName() : pos.getNames().get(0);
		final NXdata nxData = entry.getData(dataGroupName);
		assertNotNull(nxData);

		// Check axes
		final String[] expectedAxesNames = scannableNames.stream().map(x -> x + "_value_set").toArray(String[]::new);
		assertAxes(nxData, expectedAxesNames);

		int[] defaultDimensionMappings = IntStream.range(0, sizes.length).toArray();
		for (int i = 0; i < scannableNames.size(); i++) {
			final String scannableName = scannableNames.get(i);
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

	private void checkPerScanMonitors(final ScanModel scanModel, NXentry entry,
			Set<String> expectedPerScanMonitorNames) throws Exception {
		final Set<String> perScanMonitorNames = scanModel.getMonitorsPerScan().stream()
				.map(scannable -> scannable.getName()).collect(Collectors.toSet());
		final Set<String> nexusDeviceNames = scanModel.getAdditionalScanObjects().stream()
				.filter(INexusDevice.class::isInstance).map(INexusDevice.class::cast)
				.map(INexusDevice::getName).collect(Collectors.toSet());

		final Set<String> perScanAndNexusDeviceNames = new HashSet<>(perScanMonitorNames);
		perScanAndNexusDeviceNames.addAll(nexusDeviceNames);

		assertEquals(expectedPerScanMonitorNames, perScanAndNexusDeviceNames);

		for (String perScanMonitorName : perScanAndNexusDeviceNames) {
			final NXobject parentGroup = nexusDeviceNames.contains(perScanMonitorName) ?
					entry : entry.getInstrument(); // NXuser is added to the entry, other devices to instrument
			final NXobject nexusObjectForDevice = (NXobject) parentGroup.getGroupNode(perScanMonitorName);
			assertNotNull(nexusObjectForDevice);
			switch (nexusObjectForDevice.getNexusBaseClass()) {
			case NX_POSITIONER:
				checkMetadataPositioner((NXpositioner) nexusObjectForDevice, perScanMonitorName);
				break;
			case NX_SLIT:
				checkSlit((NXslit) nexusObjectForDevice, perScanMonitorName);
				break;
			case NX_USER:
				checkUser((NXuser) nexusObjectForDevice, perScanMonitorName);
				break;
			default:
				Assert.fail("Unexpected nexus base class: " + nexusObjectForDevice.getNexusBaseClass());
			}
		}
	}

	private void checkMetadataPositioner(final NXpositioner positioner, String perScanMonitorName)
			throws ScanningException, DatasetException {
		assertNotNull(positioner);
		assertEquals(perScanMonitorName, positioner.getNameScalar());

		if (perScanMonitorName.startsWith("string")) {
			final String expectedValue = (String) connector.getScannable(perScanMonitorName).getPosition();
			final DataNode dataNode = positioner.getDataNode("value");
			assertNotNull(dataNode);
			assertEquals(expectedValue, dataNode.getString());
		} else {
			int num = Integer.parseInt(perScanMonitorName.substring("perScanMonitor".length()));
			double expectedValue = num * 10.0;

			DataNode dataNode = positioner.getDataNode("value_set"); // TODO should not be here for per scan monitor
			assertNotNull(dataNode);
			Dataset dataset = DatasetUtils.sliceAndConvertLazyDataset(dataNode.getDataset());
			assertEquals(1, dataset.getSize());
			assertTrue(dataset instanceof DoubleDataset);
			assertEquals(expectedValue, dataset.getElementDoubleAbs(0), 1e-15);

			dataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
			assertNotNull(dataNode);
			dataset = DatasetUtils.sliceAndConvertLazyDataset(dataNode.getDataset());
			assertEquals(1, dataset.getSize());
			assertTrue(dataset instanceof DoubleDataset);
			assertEquals(expectedValue, dataset.getElementDoubleAbs(0), 1e-15);
		}
	}

	private void checkSlit(NXslit slit, String perScanMonitorName) throws DatasetException {
		assertNotNull(slit);
		assertEquals(perScanMonitorName, slit.getString("name")); // There is no NXslit.getNameScaler() or NXslit.NX_NAME

		final double expectedValue = 10.0;
		DataNode dataNode = slit.getDataNode(NXslit.NX_X_GAP);
		assertNotNull(dataNode);
		Dataset dataset = DatasetUtils.sliceAndConvertLazyDataset(dataNode.getDataset());
		assertEquals(1, dataset.getSize());
		assertTrue(dataset instanceof DoubleDataset);
		assertEquals(expectedValue, dataset.getElementDoubleAbs(0), 1e-15);

		dataNode = slit.getDataNode(NXslit.NX_Y_GAP);
		assertNotNull(dataNode);
		dataset = DatasetUtils.sliceAndConvertLazyDataset(dataNode.getDataset());
		assertEquals(1, dataset.getSize());
		assertTrue(dataset instanceof DoubleDataset);
		assertEquals(expectedValue, dataset.getElementDoubleAbs(0), 1e-15);
	}

	private void checkUser(NXuser user, String perScanMonitorName) {
		final NexusMetadataDevice<NXuser> metadataDevice =
				(NexusMetadataDevice<NXuser>) ServiceHolder.getNexusDeviceService().<NXuser>getNexusDevice(perScanMonitorName);
		final Map<String, Object> metadata = metadataDevice.getNexusMetadata();
		assertEquals(metadata.size(), user.getNumberOfDataNodes());
		for (Map.Entry<String, Object> metadataEntry : metadata.entrySet()) {
			assertEquals(metadataEntry.getValue(), user.getString(metadataEntry.getKey()));
		}
	}

	private IRunnableDevice<ScanModel> createStepScan(String perPointMonitorName,
			String perScanMonitorName, int... size) throws Exception {

		final IScannable<?> perPointMonitor = perPointMonitorName == null ? null : connector.getScannable(perPointMonitorName);
		final IScannable<?> perScanMonitor = perScanMonitorName == null ? null : connector.getScannable(perScanMonitorName);
		// if there's no scannable for the perScanMonitorName, see if there's a nexus device
		final INexusDevice<?> nexusDevice = (perScanMonitorName == null || perScanMonitor != null) ?
				null : ServiceHolder.getNexusDeviceService().getNexusDevice(perScanMonitorName);

		final CompoundModel compoundModel = new CompoundModel();

		// We add the outer scans, if any
		for (int dim = 0; dim < size.length; dim ++) {
			if (size[dim] > 1) {
				compoundModel.addModel(new AxialStepModel("neXusScannable"+(dim+1), 10,20,9.9d/(size[dim]-1)));
			} else {
				compoundModel.addModel(new AxialStepModel("neXusScannable"+(dim+1), 10,20,30)); // Will generate one value at 10
			}
		}

		final IPointGenerator<CompoundModel> pointGen = pointGenService.createCompoundGenerator(compoundModel);

		// Create the model for a scan.
		final ScanModel scanModel = new ScanModel();
		scanModel.setPointGenerator(pointGen);
		scanModel.setScanPathModel(compoundModel);
		if (perScanMonitor != null) {
			perScanMonitor.setActivated(true);
		}
		scanModel.setMonitorsPerPoint(perPointMonitor);
		scanModel.setMonitorsPerScan(perScanMonitor);
		scanModel.setAdditionalScanObjects(nexusDevice == null ? null : Arrays.asList(nexusDevice));

		// Create a file to scan into.
		scanModel.setFilePath(output.getAbsolutePath());
		System.out.println("File writing to " + scanModel.getFilePath());

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = runnableDeviceService.createRunnableDevice(scanModel, null);

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
