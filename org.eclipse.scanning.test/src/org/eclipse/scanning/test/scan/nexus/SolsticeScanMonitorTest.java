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

import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.FIELD_NAME_END_TIME;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.FIELD_NAME_POINT_END_TIME;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.FIELD_NAME_POINT_START_TIME;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.FIELD_NAME_SCAN_DEAD_TIME;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.FIELD_NAME_SCAN_DEAD_TIME_PERCENT;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.FIELD_NAME_SCAN_DURATION;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.FIELD_NAME_SCAN_ESTIMATED_DURATION;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.FIELD_NAME_SCAN_FINISHED;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.FIELD_NAME_SCAN_RANK;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.FIELD_NAME_SCAN_SHAPE;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.FIELD_NAME_START_TIME;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.FIELD_NAME_UNIQUE_KEYS;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.GROUP_NAME_KEYS;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.PROPERTY_NAME_SUPPRESS_GLOBAL_UNIQUE_KEYS;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.PROPERTY_NAME_UNIQUE_KEYS_PATH;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.SymbolicNode;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.AbstractNexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.dataset.StringDataset;
import org.eclipse.january.io.ILazySaver;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.sequencer.nexus.SolsticeScanMonitor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class SolsticeScanMonitorTest {

	public static class MockLazySaver implements ILazySaver {

		private static final long serialVersionUID = 1L;

		private IDataset lastWrittenData = null;

		private SliceND lastSlice = null;

		private int numWrites = 0;

		@Override
		public boolean isFileReadable() {
			return true;
		}

		@Override
		public IDataset getDataset(IMonitor mon, SliceND slice) throws IOException {
			return null;
		}

		@Override
		public void initialize() throws IOException {
			// do nothing
		}

		@Override
		public boolean isFileWriteable() {
			return true;
		}

		@Override
		public void setSlice(IMonitor mon, IDataset data, SliceND slice) throws IOException {
			// TODO could write to a dataset? SliceIterator may be useful here
			lastWrittenData = data;
			lastSlice = slice;
			numWrites++;
		}

		public IDataset getLastWrittenData() {
			return lastWrittenData;
		}

		public SliceND getLastSlice() {
			return lastSlice;
		}

		public int getNumberOfWrites() {
			return numWrites;
		}

	}

	public static class ExternalFileWritingPositioner extends AbstractNexusObjectProvider<NXpositioner> {

		public ExternalFileWritingPositioner(String name) {
			super(name, NexusBaseClass.NX_POSITIONER, NXpositioner.NX_VALUE);
			addExternalFileName("panda.nxs");
			setPropertyValue("uniqueKeys", MALCOLM_UNIQUE_KEYS_PATH);
		}

		@Override
		protected NXpositioner createNexusObject() {
			final NXpositioner positioner = NexusNodeFactory.createNXpositioner();
			addExternalLink(positioner, NXpositioner.NX_VALUE, "/entry/data", 2);

			return positioner;
		}
	}

	public static class ExternalFileWritingDetector extends AbstractNexusObjectProvider<NXdetector> {

		public static final String EXTERNAL_FILE_NAME = "detector.nxs";

		public ExternalFileWritingDetector() {
			this("detector", EXTERNAL_FILE_NAME);
		}

		public ExternalFileWritingDetector(String name, String externalFileName) {
			super(name, NexusBaseClass.NX_DETECTOR);
			addExternalFileName(externalFileName);
			setPropertyValue(PROPERTY_NAME_UNIQUE_KEYS_PATH, MALCOLM_UNIQUE_KEYS_PATH);
		}

		@Override
		protected NXdetector createNexusObject() {
			final NXdetector detector = NexusNodeFactory.createNXdetector();
			addExternalLink(detector, NXdetector.NX_DATA, "/entry/data", 4);

			return detector;
		}

	}

	public static class InternalUniqueKeysWritingDetector extends AbstractNexusObjectProvider<NXdetector> {

		public InternalUniqueKeysWritingDetector() {
			super("internal", NexusBaseClass.NX_DETECTOR);
			setPropertyValue(PROPERTY_NAME_UNIQUE_KEYS_PATH, INTERNAL_UNIQUE_KEYS_PATH);
		}

		@Override
		protected NXdetector createNexusObject() {
			final NXdetector detector = NexusNodeFactory.createNXdetector();
			detector.initializeLazyDataset(INTERNAL_UNIQUE_KEYS_PATH, 2, Integer.class);

			return detector;
		}

	}

	public static class SuppressGlobalUniqueKeysDetector extends ExternalFileWritingDetector {

		private static final String EXTERNAL_FILE_NAME = "suppress.nxs";

		public SuppressGlobalUniqueKeysDetector() {
			super("suppress", EXTERNAL_FILE_NAME);
			setPropertyValue(PROPERTY_NAME_SUPPRESS_GLOBAL_UNIQUE_KEYS, true);
		}

	}


	private static final String MALCOLM_UNIQUE_KEYS_PATH = "/entry/NDAttributes/NDArrayUniqueId";

	private static final String INTERNAL_UNIQUE_KEYS_PATH = "uniqueKeys";

	private IPointGeneratorService pointGenService;

	@Before
	public void setUp() {
		pointGenService = new PointGeneratorService();
	}

	@Test
	public void testCreateNexusObject() throws Exception {
		testCreateNexusObject(false);
	}

	@Test
	public void testSuppressGlobalUniqueKeys() throws Exception {
		testCreateNexusObject(true);
	}

	private void testCreateNexusObject(boolean suppressGlobalUniqueKeys) throws Exception {
		// Arrange
		List<NexusObjectProvider<?>> nexusObjectProviders = new ArrayList<>();
		nexusObjectProviders.add(new ExternalFileWritingDetector());
		nexusObjectProviders.add(new InternalUniqueKeysWritingDetector());
		if (suppressGlobalUniqueKeys) {
			nexusObjectProviders.add(new SuppressGlobalUniqueKeysDetector());
		}
		String[] positionerNames = new String[] { "xPos", "yPos" };
		for (String positionerName : positionerNames) {
			nexusObjectProviders.add(new ExternalFileWritingPositioner(positionerName));
		}

		ScanModel scanModel = new ScanModel();
		StaticModel model = new StaticModel(25);
		IPointGenerator<StaticModel> gen = pointGenService.createGenerator(model);

		IDetectorModel detModel = new MandelbrotModel();
		detModel.setExposureTime(0.1);
		scanModel.setScanInformation(new ScanInformation(gen, Arrays.asList(detModel), null));
		SolsticeScanMonitor solsticeScanMonitor = new SolsticeScanMonitor(scanModel);
		solsticeScanMonitor.setNexusObjectProviders(nexusObjectProviders);

		final int[] scanShape = new int[] { 8, 5 };
		final int scanRank = scanShape.length;
		NexusScanInfo scanInfo = new NexusScanInfo();
		scanInfo.setRank(scanRank);
		scanInfo.setShape(scanShape);
		int[] expectedChunking = new int[scanInfo.getRank()];
		Arrays.fill(expectedChunking, 1);
		expectedChunking[expectedChunking.length-1] = 8;

		// Act
		NXcollection solsticeScanCollection = solsticeScanMonitor.createNexusObject(scanInfo);

		// Assert
		assertNotNull(solsticeScanCollection);

		// assert scan finished dataset created correctly - value must be false
		DataNode scanFinishedDataNode = solsticeScanCollection.getDataNode(FIELD_NAME_SCAN_FINISHED);
		assertTrue(scanFinishedDataNode!=null);
		assertTrue(scanFinishedDataNode.getDataset()!=null && scanFinishedDataNode.getDataset() instanceof ILazyWriteableDataset);
		ILazyWriteableDataset scanFinishedDataset = (ILazyWriteableDataset) scanFinishedDataNode.getDataset();
		assertTrue(scanFinishedDataset.getRank()==1);
		assertTrue(Arrays.equals(scanFinishedDataset.getShape(), new int[] { 1 }));
		MockLazySaver scanFinishedSaver = new MockLazySaver();
		scanFinishedDataset.setSaver(scanFinishedSaver);

		// assert scan rank set correctly
		DataNode scanRankDataNode = solsticeScanCollection.getDataNode(FIELD_NAME_SCAN_RANK);
		assertEquals(scanRank, scanRankDataNode.getDataset().getSlice().getInt());

		// assert scan shape set correctly
		DataNode scanShapeDataNode = solsticeScanCollection.getDataNode(FIELD_NAME_SCAN_SHAPE);
		assertNotNull(scanFinishedDataNode);
		IDataset shapeDataset = scanShapeDataNode.getDataset().getSlice();
		assertNotNull(shapeDataset);
		assertEquals(1, shapeDataset.getRank());
		assertEquals(Integer.class, shapeDataset.getElementClass());
		assertArrayEquals(new int[] { scanRank }, shapeDataset.getShape());
		for (int i = 0; i < scanShape.length; i++) {
			assertEquals(scanShape[i], shapeDataset.getInt(i));
		}

		// assert that the estimated time has been written
		DataNode estimatedTimeDataNode = solsticeScanCollection.getDataNode(FIELD_NAME_SCAN_ESTIMATED_DURATION);
		assertNotNull(estimatedTimeDataNode);
		IDataset estimatedTimeDataset;
		try {
			estimatedTimeDataset = estimatedTimeDataNode.getDataset().getSlice();
		} catch (DatasetException e) {
			throw new AssertionError("Could not get data from lazy dataset", e);
		}

		assertEquals(String.class, estimatedTimeDataset.getElementClass());
		assertEquals(0, estimatedTimeDataset.getRank());
		assertArrayEquals(new int[]{}, estimatedTimeDataset.getShape());
		String estimatedTime = estimatedTimeDataset.getString();
		assertNotNull(estimatedTime);
		assertEquals("00:00:02.500", estimatedTime);

		// assert the actual time dataset has been created - note it hasn't been written to yet
		DataNode actualTimeDataNode = solsticeScanCollection.getDataNode(FIELD_NAME_SCAN_DURATION);
		assertNotNull(actualTimeDataNode);
		ILazyDataset actualTimeDataset = actualTimeDataNode.getDataset();
		assertNotNull(actualTimeDataset);
		assertEquals(String.class, actualTimeDataset.getElementClass());

		// assert the dead time dataset has been created - note it hasn't been written to yet
		DataNode deadTimeDataNode = solsticeScanCollection.getDataNode(FIELD_NAME_SCAN_DEAD_TIME);
		assertNotNull(deadTimeDataNode);
		ILazyDataset deadTimeDataset = deadTimeDataNode.getDataset();
		assertNotNull(deadTimeDataset);
		assertEquals(String.class, deadTimeDataset.getElementClass());

		// assert the dead time percent dataset has been created - again note it hasn't been written to yet
		DataNode deadTimePercentDataNode = solsticeScanCollection.getDataNode(FIELD_NAME_SCAN_DEAD_TIME_PERCENT);
		assertNotNull(deadTimePercentDataNode);
		ILazyDataset deadTimePercentDataset = deadTimePercentDataNode.getDataset();
		assertNotNull(deadTimePercentDataset);
		assertEquals(String.class, deadTimePercentDataset.getElementClass());

		// assert unique keys dataset created correctly
		NXcollection keysCollection = (NXcollection) solsticeScanCollection.getGroupNode(GROUP_NAME_KEYS);
		assertNotNull(keysCollection);

		final DataNode uniqueKeysDataNode = keysCollection.getDataNode(FIELD_NAME_UNIQUE_KEYS);
		if (suppressGlobalUniqueKeys) {
			assertTrue(uniqueKeysDataNode == null);
		} else {
			assertTrue(uniqueKeysDataNode != null);
			assertTrue(uniqueKeysDataNode.getDataset()!=null && uniqueKeysDataNode.getDataset() instanceof ILazyWriteableDataset);
			ILazyWriteableDataset uniqueKeysDataset = (ILazyWriteableDataset) uniqueKeysDataNode.getDataset();
			assertTrue(uniqueKeysDataset.getRank()==scanRank);
			assertTrue(uniqueKeysDataset.getElementClass().equals(Integer.class));
			assertTrue(Arrays.equals(uniqueKeysDataset.getChunking(), expectedChunking));
			MockLazySaver uniqueKeysSaver = new MockLazySaver();
			uniqueKeysDataset.setSaver(uniqueKeysSaver);
		}

		// assert links to unique keys for devices that write their own
		final int expectedNumUniqueKeys = (nexusObjectProviders.size() - 1) + (uniqueKeysDataNode == null ? 0 : 1);
		assertEquals(expectedNumUniqueKeys, keysCollection.getNumberOfNodelinks());
		for (NexusObjectProvider<?> objectProvider : nexusObjectProviders) {
			final String deviceName = objectProvider.getName();
			final String uniqueKeysPath = (String) objectProvider.getPropertyValue(PROPERTY_NAME_UNIQUE_KEYS_PATH);
			final Set<String> externalFileNames = objectProvider.getExternalFileNames();
			if (externalFileNames.isEmpty()) {
				assertEquals("internal", deviceName);
				final NXobject nexusObject = objectProvider.getNexusObject();
				final NodeLink uniqueKeysNodeLink = nexusObject.findNodeLink(INTERNAL_UNIQUE_KEYS_PATH);
				assertTrue(uniqueKeysNodeLink != null && uniqueKeysNodeLink.isDestinationData());
				final DataNode uniqueKeysNode = (DataNode) nexusObject.findNodeLink(INTERNAL_UNIQUE_KEYS_PATH).getDestination();
				assertSame(uniqueKeysNode, keysCollection.getDataNode(deviceName));
			} else {
				final String externalFileName = externalFileNames.iterator().next();
				String datasetName = externalFileName.replace("/", "__");
				SymbolicNode symbolicNode = keysCollection.getSymbolicNode(datasetName);
				assertNotNull(symbolicNode);
				assertEquals(uniqueKeysPath, symbolicNode.getPath());
			}
		}

		checkTimeStampDataset(solsticeScanCollection.getDataNode(FIELD_NAME_POINT_START_TIME), true, scanRank);
		checkTimeStampDataset(solsticeScanCollection.getDataNode(FIELD_NAME_POINT_END_TIME), true, scanRank);
		checkTimeStampDataset(solsticeScanCollection.getDataNode(FIELD_NAME_START_TIME), false, scanRank);
		checkTimeStampDataset(solsticeScanCollection.getDataNode(FIELD_NAME_END_TIME), false, scanRank);
	}

	private void checkTimeStampDataset(DataNode node, boolean perPoint, int scanRank) {
		assertNotNull(node);
		ILazyDataset startTimeStampsDataset = node.getDataset();
		assertNotNull(startTimeStampsDataset);
		assertEquals(String.class, startTimeStampsDataset.getElementClass());
		if (perPoint) {
			assertEquals(scanRank, startTimeStampsDataset.getRank());
		} else {
			assertArrayEquals(new int[] { 1 }, startTimeStampsDataset.getShape());
		}
	}

	@Test
	public void testWriteScanPoints() throws Exception {
		// Arrange - we have to create the nexus object first
		List<NexusObjectProvider<?>> nexusObjectProviders = new ArrayList<>();
		nexusObjectProviders.add(new ExternalFileWritingDetector());
		nexusObjectProviders.add(new InternalUniqueKeysWritingDetector());
		String[] positionerNames = new String[] { "xPos", "yPos" };
		for (String positionerName : positionerNames) {
			nexusObjectProviders.add(new ExternalFileWritingPositioner(positionerName));
		}

		ScanModel scanModel = new ScanModel();
		StaticModel model = new StaticModel(25);
		IPointGenerator<?> pointGen = pointGenService.createGenerator(model);

		IDetectorModel detModel = new MandelbrotModel();
		detModel.setExposureTime(0.1);
		scanModel.setScanInformation(new ScanInformation(pointGen, Arrays.asList(detModel), null));
		SolsticeScanMonitor solsticeScanMonitor = new SolsticeScanMonitor(scanModel);
		solsticeScanMonitor.setNexusObjectProviders(nexusObjectProviders);

		final int[] scanShape = new int[] { 8, 5 };
		final int scanRank = scanShape.length;
		NexusScanInfo scanInfo = new NexusScanInfo();
		scanInfo.setRank(scanRank);
		scanInfo.setShape(scanShape);
		int[] expectedChunking = new int[scanInfo.getRank()];
		Arrays.fill(expectedChunking, 1);

		// Act
		NXcollection solsticeScanCollection = solsticeScanMonitor.createNexusObject(scanInfo);

		// Assert
		assertNotNull(solsticeScanCollection);
		DataNode scanFinishedDataNode = solsticeScanCollection.getDataNode(FIELD_NAME_SCAN_FINISHED);
		ILazyWriteableDataset scanFinishedDataset = (ILazyWriteableDataset) scanFinishedDataNode.getDataset();
		MockLazySaver scanFinishedSaver = new MockLazySaver();
		scanFinishedDataset.setSaver(scanFinishedSaver);
		DataNode actualTimeDataNode = solsticeScanCollection.getDataNode(FIELD_NAME_SCAN_DURATION);
		ILazyWriteableDataset actualTimeDataset = (ILazyWriteableDataset) actualTimeDataNode.getDataset();
		MockLazySaver actualTimeSaver = new MockLazySaver();
		actualTimeDataset.setSaver(actualTimeSaver);
		DataNode deadTimeDataNode = solsticeScanCollection.getDataNode(FIELD_NAME_SCAN_DEAD_TIME);
		ILazyWriteableDataset deadTimeDataset = (ILazyWriteableDataset) deadTimeDataNode.getDataset();
		MockLazySaver deadTimeSaver = new MockLazySaver();
		deadTimeDataset.setSaver(deadTimeSaver);
		DataNode deadTimePercentDataNode = solsticeScanCollection.getDataNode(FIELD_NAME_SCAN_DEAD_TIME_PERCENT);
		ILazyWriteableDataset deadTimePercentDataset = (ILazyWriteableDataset) deadTimePercentDataNode.getDataset();
		MockLazySaver deadTimePercentSaver = new MockLazySaver();
		deadTimePercentDataset.setSaver(deadTimePercentSaver);

		DataNode startTimeNode = solsticeScanCollection.getDataNode(FIELD_NAME_START_TIME);
		ILazyWriteableDataset startTimeDataset = (ILazyWriteableDataset) startTimeNode.getDataset();
		startTimeDataset.setSaver(new MockLazySaver());

		DataNode stopTimeNode = solsticeScanCollection.getDataNode(FIELD_NAME_END_TIME);
		ILazyWriteableDataset stopTimeDataset = (ILazyWriteableDataset) stopTimeNode.getDataset();
		stopTimeDataset.setSaver(new MockLazySaver());

		// assert scan shape set correctly
		DataNode scanShapeDataNode = solsticeScanCollection.getDataNode(FIELD_NAME_SCAN_SHAPE);
		assertNotNull(scanFinishedDataNode);
		IDataset shapeDataset = scanShapeDataNode.getDataset().getSlice();
		assertNotNull(shapeDataset);
		assertEquals(1, shapeDataset.getRank());
		assertEquals(Integer.class, shapeDataset.getElementClass());
		assertArrayEquals(new int[] { scanRank }, shapeDataset.getShape());
		for (int i = 0; i < scanShape.length; i++) {
			assertEquals(scanShape[i], shapeDataset.getInt(i));
		}

		DataNode estimatedTimeDataNode = solsticeScanCollection.getDataNode(FIELD_NAME_SCAN_ESTIMATED_DURATION);
		assertNotNull(estimatedTimeDataNode);
		IDataset estimatedTimeDataset;
		try {
			estimatedTimeDataset = estimatedTimeDataNode.getDataset().getSlice();
		} catch (DatasetException e) {
			throw new AssertionError("Could not get data from lazy dataset", e);
		}

		// assert that the estimated time has been written
		assertEquals(String.class, estimatedTimeDataset.getElementClass());
		assertEquals(0, estimatedTimeDataset.getRank());
		assertArrayEquals(new int[]{}, estimatedTimeDataset.getShape());
		String estimatedTime = estimatedTimeDataset.getString();
		assertNotNull(estimatedTime);
		assertEquals("00:00:02.500", estimatedTime);

		// assert the actual time dataset has been created - note it hasn't been written to yet
		assertNotNull(actualTimeDataset);
		assertEquals(String.class, actualTimeDataset.getElementClass());

		assertNotNull(deadTimeDataset);
		assertEquals(String.class, deadTimePercentDataset.getElementClass());

		// TODO what can we assert about the value
		// assert unique keys dataset created correctly
		NXcollection keysCollection = (NXcollection) solsticeScanCollection.getGroupNode(GROUP_NAME_KEYS);
		assertNotNull(keysCollection);

		DataNode uniqueKeysDataNode = keysCollection.getDataNode(FIELD_NAME_UNIQUE_KEYS);
		ILazyWriteableDataset uniqueKeysDataset = (ILazyWriteableDataset) uniqueKeysDataNode.getDataset();
		MockLazySaver uniqueKeysSaver = new MockLazySaver();
		uniqueKeysDataset.setSaver(uniqueKeysSaver);

		// assert links to unique keys for devices that write their own
		assertEquals(4, keysCollection.getNumberOfNodelinks());
		for (NexusObjectProvider<?> objectProvider : nexusObjectProviders) {
			final String deviceName = objectProvider.getName();
			final String uniqueKeysPath = (String) objectProvider.getPropertyValue(PROPERTY_NAME_UNIQUE_KEYS_PATH);
			final Set<String> externalFileNames = objectProvider.getExternalFileNames();
			if (externalFileNames.isEmpty()) {
				assertEquals("internal", deviceName);
				final NXobject nexusObject = objectProvider.getNexusObject();
				final NodeLink uniqueKeysNodeLink = nexusObject.findNodeLink(INTERNAL_UNIQUE_KEYS_PATH);
				assertTrue(uniqueKeysNodeLink != null && uniqueKeysNodeLink.isDestinationData());
				final DataNode uniqueKeysNode = (DataNode) nexusObject.findNodeLink(INTERNAL_UNIQUE_KEYS_PATH).getDestination();
				assertSame(uniqueKeysNode, keysCollection.getDataNode(deviceName));
			} else {
				final String externalFileName = externalFileNames.iterator().next();
				String datasetName = externalFileName.replace("/", "__");
				SymbolicNode symbolicNode = keysCollection.getSymbolicNode(datasetName);
				assertNotNull(symbolicNode);
				assertEquals(uniqueKeysPath, symbolicNode.getPath());
			}
		}

		// test calling setPosition
		// arrange
		double[] pos = new double[] { 172.5, 56.3 };
		int[] indices = new int[] { 8, 3 };
		int stepIndex = 23;
		MapPosition position = new MapPosition();
		position.setStepIndex(stepIndex);
		List<Collection<String>> names = new ArrayList<>( positionerNames.length);
		for (int i = 0; i < positionerNames.length; i++) {
			position.put(positionerNames[i], pos[i]);
			position.putIndex(positionerNames[i], indices[i]);
			names.add(Arrays.asList(positionerNames[i]));
		}
		position.setDimensionNames(names);

		// act
		solsticeScanMonitor.setPosition(null, position);
		solsticeScanMonitor.scanFinished();

		// assert
		// check data written to scan finished dataset
		IDataset writtenToScanFinishedData = scanFinishedSaver.getLastWrittenData();
		assertNotNull(writtenToScanFinishedData);
		assertEquals(0, writtenToScanFinishedData.getRank());
		assertArrayEquals(new int[0], writtenToScanFinishedData.getShape());
		assertTrue(writtenToScanFinishedData instanceof IntegerDataset);
		assertEquals(1, writtenToScanFinishedData.getInt());

		// check data written to actual time dataset
		IDataset writtenToActualTimeDataset = actualTimeSaver.getLastWrittenData();
		assertNotNull(writtenToActualTimeDataset);
		assertEquals(0, writtenToActualTimeDataset.getRank());
		assertArrayEquals(new int[0], writtenToActualTimeDataset.getShape());
		assertTrue(writtenToActualTimeDataset instanceof StringDataset);

		DateTimeFormatter formatter = new DateTimeFormatterBuilder().
				appendPattern("HH:mm:ss").appendFraction(ChronoField.NANO_OF_SECOND, 3, 3, true).toFormatter();
		String actualTime = writtenToActualTimeDataset.getString();
		formatter.parse(actualTime); // throws exception if not valid time

		// check data written to dead time dataset
		IDataset writtenToDeadTimeDataset = deadTimeSaver.getLastWrittenData();
		assertNotNull(writtenToActualTimeDataset);
		assertEquals(0, writtenToActualTimeDataset.getRank());
		assertArrayEquals(new int[0], writtenToDeadTimeDataset.getShape());
		assertTrue(writtenToDeadTimeDataset instanceof StringDataset);

		// check data written to dead time percent dataset
		IDataset writtenToDeadTimePercentDataset = deadTimePercentSaver.getLastWrittenData();
		assertNotNull(writtenToDeadTimePercentDataset);
		assertEquals(0, writtenToDeadTimePercentDataset.getRank());
		assertArrayEquals(new int[0], writtenToDeadTimePercentDataset.getShape());
		assertTrue(writtenToDeadTimePercentDataset instanceof StringDataset);

		// check data written to unique keys dataset
		IDataset writtenToUniqueKeysData = uniqueKeysSaver.getLastWrittenData();
		assertNotNull(writtenToUniqueKeysData);
		int[] expectedShape = new int[scanInfo.getRank()];
		Arrays.fill(expectedShape, 1);
		assertArrayEquals(writtenToUniqueKeysData.getShape(), expectedShape);
		assertTrue(writtenToUniqueKeysData instanceof IntegerDataset);
		int[] valuePos = new int[scanRank]; // all zeros
		assertEquals(stepIndex + 1, writtenToUniqueKeysData.getInt(valuePos));

		SliceND uniqueKeysSlice = uniqueKeysSaver.getLastSlice();
		Assert.assertNotNull(uniqueKeysSlice);
		assertArrayEquals(uniqueKeysSlice.getShape(), expectedShape);
		assertArrayEquals(uniqueKeysSlice.getStart(), indices);
		assertArrayEquals(uniqueKeysSlice.getStep(), expectedShape); // all ones
		int[] stopIndices = Arrays.stream(indices).map(x -> x + 1).toArray();
		assertArrayEquals(uniqueKeysSlice.getStop(), stopIndices);
	}

}