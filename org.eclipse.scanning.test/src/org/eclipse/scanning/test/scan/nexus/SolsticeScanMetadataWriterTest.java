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

import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.ATTRIBUTE_NAME_UNITS;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.ATTRIBUTE_VALUE_MILLISECONDS;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.FIELD_NAME_POINT_END_TIME;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.FIELD_NAME_POINT_START_TIME;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.FIELD_NAME_SCAN_COMMAND;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.FIELD_NAME_SCAN_DEAD_TIME;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.FIELD_NAME_SCAN_DEAD_TIME_PERCENT;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.FIELD_NAME_SCAN_DURATION;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.FIELD_NAME_SCAN_END_TIME;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.FIELD_NAME_SCAN_ESTIMATED_DURATION;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.FIELD_NAME_SCAN_FINISHED;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.FIELD_NAME_SCAN_RANK;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.FIELD_NAME_SCAN_SHAPE;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.FIELD_NAME_SCAN_START_TIME;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.FIELD_NAME_UNIQUE_KEYS;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.GROUP_NAME_UNIQUE_KEYS;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.PROPERTY_NAME_SUPPRESS_GLOBAL_UNIQUE_KEYS;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.PROPERTY_NAME_UNIQUE_KEYS_PATH;
import static org.eclipse.dawnsci.nexus.scan.NexusScanMetadataWriter.SCALAR_SHAPE;
import static org.eclipse.dawnsci.nexus.scan.NexusScanMetadataWriter.SINGLE_SHAPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.SymbolicNode;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.AbstractNexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.CustomNexusEntryModification;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.scan.NexusScanMetadataWriter;
import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.FloatDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.LongDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.io.ILazySaver;
import org.eclipse.scanning.api.device.IScanDevice;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.points.ServiceHolder;
import org.eclipse.scanning.points.validation.ValidatorService;
import org.eclipse.scanning.sequencer.nexus.SolsticeScanMetadataWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class SolsticeScanMetadataWriterTest {

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
			addExternalFileName("p45-14-panda.nxs");
			setPropertyValue(PROPERTY_NAME_UNIQUE_KEYS_PATH, MALCOLM_UNIQUE_KEYS_PATH);
		}

		@Override
		protected NXpositioner createNexusObject() {
			final NXpositioner positioner = NexusNodeFactory.createNXpositioner();
			addExternalLink(positioner, NXpositioner.NX_VALUE, "/entry/data", 2);

			return positioner;
		}
	}

	public static class ExternalFileWritingDetector extends AbstractNexusObjectProvider<NXdetector> {

		public static final String EXTERNAL_FILE_NAME = "p45-14-detector.nxs";

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
			setPropertyValue(PROPERTY_NAME_UNIQUE_KEYS_PATH, PROPERTY_NAME_UNIQUE_KEYS_PATH);
		}

		@Override
		protected NXdetector createNexusObject() {
			final NXdetector detector = NexusNodeFactory.createNXdetector();
			detector.initializeLazyDataset(PROPERTY_NAME_UNIQUE_KEYS_PATH, 2, Integer.class);

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

	private static final int[] SCAN_SHAPE = new int[] { 8, 5 };

	private static final String SCAN_COMMAND = "mscan(grid(axes=('xNex', 'yNex'), start=(0, 0), stop=(8, 5), count=(1, 1), snake=True)";

	private static final String[] EXPECTED_ENTRY_FIELD_NAMES = {
			FIELD_NAME_SCAN_START_TIME, FIELD_NAME_SCAN_END_TIME, FIELD_NAME_SCAN_DURATION,
			FIELD_NAME_SCAN_COMMAND, FIELD_NAME_SCAN_SHAPE,
	};

	private IPointGeneratorService pointGenService;

	private SolsticeScanMetadataWriter scanMetadataWriter;

	private NXcollection diamondScanCollection;

	@BeforeEach
	public void setUp() {
		pointGenService = new PointGeneratorService();
		new ServiceHolder().setValidatorService(new ValidatorService());
	}

	@Test
	public void testCreateNexusObject() throws Exception {
		testCreateNexusObject(false);
	}

	@Test
	public void testSuppressGlobalUniqueKeys() throws Exception {
		testCreateNexusObject(true);
	}

	@Test
	public void testCustomNexusModification() throws Exception {
		testCreateNexusObject(false);

		final CustomNexusEntryModification modification = scanMetadataWriter.getCustomNexusModification();
		assertThat(modification, is(notNullValue()));

		final NXentry entry = NexusNodeFactory.createNXentry();
		modification.modifyEntry(entry);

		assertThat(entry.getDataNodeNames(), containsInAnyOrder(EXPECTED_ENTRY_FIELD_NAMES));

		for (String fieldName : EXPECTED_ENTRY_FIELD_NAMES) {
			assertThat(entry.getDataNode(fieldName), is(sameInstance(diamondScanCollection.getDataNode(fieldName))));
		}
	}

	private void testCreateNexusObject(boolean suppressGlobalUniqueKeys) throws Exception {
		// Arrange
		final List<NexusObjectProvider<?>> nexusObjectProviders = new ArrayList<>();
		nexusObjectProviders.add(new ExternalFileWritingDetector());
		nexusObjectProviders.add(new InternalUniqueKeysWritingDetector());
		if (suppressGlobalUniqueKeys) {
			nexusObjectProviders.add(new SuppressGlobalUniqueKeysDetector());
		}
		final String[] positionerNames = new String[] { "xPos", "yPos" };
		for (String positionerName : positionerNames) {
			nexusObjectProviders.add(new ExternalFileWritingPositioner(positionerName));
		}

		final int scanRank = SCAN_SHAPE.length;
		final int numPoints = SCAN_SHAPE[0] * SCAN_SHAPE[1];
		final TwoAxisGridPointsModel gridModel = createGridModel(SCAN_SHAPE);

		final IPointGenerator<TwoAxisGridPointsModel> gen = pointGenService.createGenerator(gridModel);
		final ScanModel scanModel = new ScanModel();
		scanModel.setPointGenerator(gen);

		final IDetectorModel detModel = new MandelbrotModel();
		detModel.setExposureTime(0.1);
		scanModel.setScanInformation(new ScanInformation(gen, Arrays.asList(detModel), null));

		final IScanDevice scanDevice = mock(IScanDevice.class);
		when(scanDevice.getName()).thenReturn("solstice_scan");

		scanMetadataWriter = new SolsticeScanMetadataWriter(scanDevice, scanModel);
		scanMetadataWriter.setNexusObjectProviders(nexusObjectProviders);

		final NexusScanInfo scanInfo = new NexusScanInfo();
		scanInfo.setShape(SCAN_SHAPE);
		scanInfo.setEstimatedScanTime(scanModel.getScanInformation().getEstimatedScanTime());
		scanInfo.setScanCommand(SCAN_COMMAND);

		final int[] expectedChunking = new int[scanInfo.getOuterRank()];
		Arrays.fill(expectedChunking, 1);
		expectedChunking[expectedChunking.length-1] = 8;

		diamondScanCollection = scanMetadataWriter.getNexusProvider(scanInfo).getNexusObject();

		// Assert
		assertThat(diamondScanCollection, is(notNullValue()));

		assertThat(diamondScanCollection.getDataNodeNames(), containsInAnyOrder(
				FIELD_NAME_SCAN_SHAPE, FIELD_NAME_SCAN_RANK, FIELD_NAME_SCAN_COMMAND,
				FIELD_NAME_SCAN_START_TIME, FIELD_NAME_SCAN_END_TIME, FIELD_NAME_SCAN_DURATION,
				FIELD_NAME_SCAN_ESTIMATED_DURATION, FIELD_NAME_SCAN_FINISHED,
				FIELD_NAME_SCAN_DEAD_TIME, FIELD_NAME_SCAN_DEAD_TIME_PERCENT,
				FIELD_NAME_POINT_START_TIME, FIELD_NAME_POINT_END_TIME));

		// assert scan finished dataset created correctly - value must be false
		final DataNode scanFinishedDataNode = diamondScanCollection.getDataNode(FIELD_NAME_SCAN_FINISHED);
		assertThat(scanFinishedDataNode, is(notNullValue()));
		assertThat(scanFinishedDataNode.getDataset(), is(instanceOf(ILazyWriteableDataset.class)));
		final ILazyWriteableDataset scanFinishedDataset = (ILazyWriteableDataset) scanFinishedDataNode.getDataset();
		assertThat(scanFinishedDataset.getRank(), is(1));
		assertThat(scanFinishedDataset.getShape(), is(equalTo(SINGLE_SHAPE)));
		final MockLazySaver scanFinishedSaver = new MockLazySaver();
		scanFinishedDataset.setSaver(scanFinishedSaver);

		// assert scan command set correctly
		final DataNode scanCommandDataNode = diamondScanCollection.getDataNode(FIELD_NAME_SCAN_COMMAND);
		assertThat(scanCommandDataNode, is(notNullValue()));
		assertThat(scanCommandDataNode.getDataset().getSlice().getString(), is(SCAN_COMMAND));

		// assert scan rank set correctly
		final DataNode scanRankDataNode = diamondScanCollection.getDataNode(FIELD_NAME_SCAN_RANK);
		assertThat(scanRankDataNode, is(notNullValue()));
		assertThat(scanRankDataNode.getDataset().getSlice().getInt(), is(scanRank));

		// assert scan shape set correctly
		final DataNode scanShapeDataNode = diamondScanCollection.getDataNode(FIELD_NAME_SCAN_SHAPE);
		assertThat(scanFinishedDataNode, is(notNullValue()));
		final IDataset shapeDataset = scanShapeDataNode.getDataset().getSlice();
		assertThat(shapeDataset, is(notNullValue()));
		assertThat(shapeDataset.getRank(), is(1));
		assertThat(shapeDataset.getElementClass(), is(equalTo(Integer.class)));
		assertThat(shapeDataset.getShape(), is(equalTo(new int[] { scanRank })));
		assertThat(IntStream.range(0, SCAN_SHAPE.length).map(shapeDataset::getInt).toArray(), is(equalTo(SCAN_SHAPE)));

		// assert that the estimated time has been written
		final DataNode estimatedTimeDataNode = diamondScanCollection.getDataNode(FIELD_NAME_SCAN_ESTIMATED_DURATION);
		assertThat(estimatedTimeDataNode, is(notNullValue()));
		IDataset estimatedTimeDataset;
		try {
			estimatedTimeDataset = estimatedTimeDataNode.getDataset().getSlice();
		} catch (DatasetException e) {
			throw new AssertionError("Could not get data from lazy dataset", e);
		}

		assertThat(estimatedTimeDataset.getElementClass(), is(equalTo(Long.class)));
		assertThat(estimatedTimeDataset.getRank(), is(0));
		assertThat(estimatedTimeDataset.getShape(), is(equalTo(SCALAR_SHAPE)));
		assertThat(estimatedTimeDataset.getLong(), is(numPoints * 100l));
		assertUnitsSet(estimatedTimeDataNode);

		// assert the actual time dataset has been created - note it hasn't been written to yet
		final DataNode actualTimeDataNode = diamondScanCollection.getDataNode(FIELD_NAME_SCAN_DURATION);
		assertThat(actualTimeDataNode, is(notNullValue()));
		final ILazyDataset actualTimeDataset = actualTimeDataNode.getDataset();
		assertThat(actualTimeDataset, is(notNullValue()));
		assertThat(actualTimeDataset.getElementClass(), is(equalTo(Long.class)));

		// assert the dead time dataset has been created - note it hasn't been written to yet
		final DataNode deadTimeDataNode = diamondScanCollection.getDataNode(FIELD_NAME_SCAN_DEAD_TIME);
		assertThat(deadTimeDataNode, is(notNullValue()));
		final ILazyDataset deadTimeDataset = deadTimeDataNode.getDataset();
		assertThat(deadTimeDataset, is(notNullValue()));
		assertThat(deadTimeDataset.getElementClass(), is(equalTo(Long.class)));

		// assert the dead time percent dataset has been created - again note it hasn't been written to yet
		final DataNode deadTimePercentDataNode = diamondScanCollection.getDataNode(FIELD_NAME_SCAN_DEAD_TIME_PERCENT);
		assertThat(deadTimePercentDataNode, is(notNullValue()));
		final ILazyDataset deadTimePercentDataset = deadTimePercentDataNode.getDataset();
		assertThat(deadTimePercentDataset, is(notNullValue()));
		assertThat(deadTimePercentDataset.getElementClass(), is(equalTo(Float.class)));

		// assert unique keys dataset created correctly
		final NXcollection keysCollection = (NXcollection) diamondScanCollection.getGroupNode(GROUP_NAME_UNIQUE_KEYS);
		assertThat(keysCollection, is(notNullValue()));

		final DataNode uniqueKeysDataNode = keysCollection.getDataNode(FIELD_NAME_UNIQUE_KEYS);
		if (suppressGlobalUniqueKeys) {
			assertThat(uniqueKeysDataNode, is(nullValue()));
		} else {
			assertThat(uniqueKeysDataNode, is(notNullValue()));
			assertThat(uniqueKeysDataNode.getDataset(), is(instanceOf(ILazyWriteableDataset.class)));
			final ILazyWriteableDataset uniqueKeysDataset = (ILazyWriteableDataset) uniqueKeysDataNode.getDataset();
			assertThat(uniqueKeysDataNode.getRank(), is(scanRank));
			assertThat(uniqueKeysDataset.getElementClass(), is(equalTo(Integer.class)));
			assertThat(uniqueKeysDataset.getChunking(), is(equalTo(expectedChunking)));
			final MockLazySaver uniqueKeysSaver = new MockLazySaver();
			uniqueKeysDataset.setSaver(uniqueKeysSaver);
		}

		// assert links to unique keys for devices that write their own
		final int expectedNumUniqueKeys = (nexusObjectProviders.size() - 1) + (uniqueKeysDataNode == null ? 0 : 1);
		assertThat(keysCollection.getNumberOfNodelinks(), is(expectedNumUniqueKeys));
		assertDevicesWritingKeys(nexusObjectProviders, keysCollection);

		checkTimeStampDataset(diamondScanCollection.getDataNode(FIELD_NAME_POINT_START_TIME), true, scanRank);
		checkTimeStampDataset(diamondScanCollection.getDataNode(FIELD_NAME_POINT_END_TIME), true, scanRank);
		checkTimeStampDataset(diamondScanCollection.getDataNode(FIELD_NAME_SCAN_START_TIME), false, scanRank);
		checkTimeStampDataset(diamondScanCollection.getDataNode(FIELD_NAME_SCAN_END_TIME), false, scanRank);
	}

	private TwoAxisGridPointsModel createGridModel(final int[] scanShape) {
		final TwoAxisGridPointsModel gridModel = new TwoAxisGridPointsModel();
		gridModel.setxAxisName("xNex");
		gridModel.setxAxisPoints(scanShape[1]);
		gridModel.setyAxisName("yNex");
		gridModel.setyAxisPoints(scanShape[0]);
		gridModel.setBoundingBox(new BoundingBox(0,0,3,3));
		return gridModel;
	}


	private void assertUnitsSet(DataNode node) {
		final Attribute unitsAttribute = node.getAttribute(ATTRIBUTE_NAME_UNITS);
		assertThat(unitsAttribute, is(notNullValue()));
		assertThat(unitsAttribute.getFirstElement(), is(equalTo(ATTRIBUTE_VALUE_MILLISECONDS)));
	}

	private void checkTimeStampDataset(DataNode node, boolean perPoint, int scanRank) {
		assertThat(node, is(notNullValue()));
		final ILazyDataset startTimeStampsDataset = node.getDataset();
		assertThat(startTimeStampsDataset, is(notNullValue()));
		assertThat(startTimeStampsDataset.getElementClass(), is(equalTo(String.class)));
		if (perPoint) {
			assertThat(startTimeStampsDataset.getRank(), is(scanRank));
		} else {
			assertThat(startTimeStampsDataset.getShape(), is(either(equalTo(SINGLE_SHAPE)).or(equalTo(SCALAR_SHAPE))));
		}
	}

	@Test
	public void testWriteScanPoints() throws Exception {
		// Arrange - we have to create the nexus object first
		final List<NexusObjectProvider<?>> nexusObjectProviders = new ArrayList<>();
		nexusObjectProviders.add(new ExternalFileWritingDetector());
		nexusObjectProviders.add(new InternalUniqueKeysWritingDetector());
		final String[] positionerNames = new String[] { "xPos", "yPos" };
		for (String positionerName : positionerNames) {
			nexusObjectProviders.add(new ExternalFileWritingPositioner(positionerName));
		}

		final int[] scanShape = { 8, 5 };
		final int scanRank = scanShape.length;
		final int numPoints = scanShape[0] * scanShape[1];
		final TwoAxisGridPointsModel gridModel = createGridModel(scanShape);
		final ScanModel scanModel = new ScanModel();
		final IPointGenerator<?> pointGen = pointGenService.createGenerator(gridModel);

		final IDetectorModel detModel = new MandelbrotModel();
		detModel.setExposureTime(0.1);
		scanModel.setScanInformation(new ScanInformation(pointGen, Arrays.asList(detModel), null));

		final IScanDevice scanDevice = mock(IScanDevice.class);
		when(scanDevice.getName()).thenReturn("solstice_scan");
		final SolsticeScanMetadataWriter scanMetadataWriter = new SolsticeScanMetadataWriter(scanDevice, scanModel);
		scanMetadataWriter.setNexusObjectProviders(nexusObjectProviders);

		final NexusScanInfo scanInfo = new NexusScanInfo();
		scanInfo.setShape(scanShape);
		scanInfo.setEstimatedScanTime(scanModel.getScanInformation().getEstimatedScanTime());
		final int[] expectedChunking = new int[scanInfo.getOuterRank()];
		Arrays.fill(expectedChunking, 1);

		// Act
		final NXcollection solsticeScanCollection = scanMetadataWriter.getNexusProvider(scanInfo).getNexusObject();

		// Assert
		assertThat(solsticeScanCollection, is(notNullValue()));
		final DataNode scanFinishedDataNode = solsticeScanCollection.getDataNode(FIELD_NAME_SCAN_FINISHED);
		final ILazyWriteableDataset scanFinishedDataset = (ILazyWriteableDataset) scanFinishedDataNode.getDataset();
		final MockLazySaver scanFinishedSaver = new MockLazySaver();
		scanFinishedDataset.setSaver(scanFinishedSaver);
		final DataNode actualTimeDataNode = solsticeScanCollection.getDataNode(FIELD_NAME_SCAN_DURATION);
		final ILazyWriteableDataset actualTimeDataset = (ILazyWriteableDataset) actualTimeDataNode.getDataset();
		final MockLazySaver actualTimeSaver = new MockLazySaver();
		actualTimeDataset.setSaver(actualTimeSaver);
		final DataNode deadTimeDataNode = solsticeScanCollection.getDataNode(FIELD_NAME_SCAN_DEAD_TIME);
		final ILazyWriteableDataset deadTimeDataset = (ILazyWriteableDataset) deadTimeDataNode.getDataset();
		final MockLazySaver deadTimeSaver = new MockLazySaver();
		deadTimeDataset.setSaver(deadTimeSaver);
		final DataNode deadTimePercentDataNode = solsticeScanCollection.getDataNode(FIELD_NAME_SCAN_DEAD_TIME_PERCENT);
		final ILazyWriteableDataset deadTimePercentDataset = (ILazyWriteableDataset) deadTimePercentDataNode.getDataset();
		final MockLazySaver deadTimePercentSaver = new MockLazySaver();
		deadTimePercentDataset.setSaver(deadTimePercentSaver);
		final DataNode stopTimeNode = solsticeScanCollection.getDataNode(FIELD_NAME_SCAN_END_TIME);
		final ILazyWriteableDataset stopTimeDataset = (ILazyWriteableDataset) stopTimeNode.getDataset();
		stopTimeDataset.setSaver(new MockLazySaver());
		final DataNode pointEndTimeNode = solsticeScanCollection.getDataNode(FIELD_NAME_POINT_END_TIME);
		final ILazyWriteableDataset pointEndTimeDataset = (ILazyWriteableDataset) pointEndTimeNode.getDataset();
		pointEndTimeDataset.setSaver(new MockLazySaver());

		// assert scan shape set correctly
		final DataNode scanShapeDataNode = solsticeScanCollection.getDataNode(FIELD_NAME_SCAN_SHAPE);
		assertThat(scanFinishedDataNode, is(notNullValue()));
		final IDataset shapeDataset = scanShapeDataNode.getDataset().getSlice();
		assertThat(shapeDataset, is(notNullValue()));
		assertThat(shapeDataset.getRank(), is(1));
		assertThat(shapeDataset.getElementClass(), is(equalTo(Integer.class)));
		assertThat(shapeDataset.getShape(), is(equalTo(new int[] { scanRank })));
		assertThat(IntStream.range(0, scanShape.length).map(shapeDataset::getInt).toArray(), is(equalTo(scanShape)));

		final DataNode estimatedTimeDataNode = solsticeScanCollection.getDataNode(FIELD_NAME_SCAN_ESTIMATED_DURATION);
		assertThat(estimatedTimeDataNode, is(notNullValue()));
		final IDataset estimatedTimeDataset;
		try {
			estimatedTimeDataset = estimatedTimeDataNode.getDataset().getSlice();
		} catch (DatasetException e) {
			throw new AssertionError("Could not get data from lazy dataset", e);
		}

		// assert that the estimated time has been written
		assertThat(estimatedTimeDataset.getElementClass(), is(equalTo(Long.class)));
		assertThat(estimatedTimeDataset.getRank(), is(0));
		assertThat(estimatedTimeDataset.getShape(), is(equalTo(SCALAR_SHAPE)));
		final long estimatedTime = estimatedTimeDataset.getLong();
		assertThat(estimatedTime, is(equalTo(numPoints * 100l)));
		assertUnitsSet(estimatedTimeDataNode);

		// assert the actual time dataset has been created - note it hasn't been written to yet
		assertThat(actualTimeDataset, is(notNullValue()));
		assertThat(actualTimeDataset.getElementClass(), is(equalTo(Long.class)));
		assertUnitsSet(actualTimeDataNode);
		assertThat(deadTimeDataset, is(notNullValue()));
		assertThat(deadTimePercentDataset.getElementClass(), is(equalTo(Float.class)));

		// TODO what can we assert about the value
		// assert unique keys dataset created correctly
		final NXcollection keysCollection = (NXcollection) solsticeScanCollection.getGroupNode(GROUP_NAME_UNIQUE_KEYS);
		assertThat(keysCollection, is(notNullValue()));

		final DataNode uniqueKeysDataNode = keysCollection.getDataNode(FIELD_NAME_UNIQUE_KEYS);
		final ILazyWriteableDataset uniqueKeysDataset = (ILazyWriteableDataset) uniqueKeysDataNode.getDataset();
		final MockLazySaver uniqueKeysSaver = new MockLazySaver();
		uniqueKeysDataset.setSaver(uniqueKeysSaver);

		// assert links to unique keys for devices that write their own
		assertThat(keysCollection.getNumberOfNodelinks(), is(4));
		assertDevicesWritingKeys(nexusObjectProviders, keysCollection);

		// test calling setPosition
		// arrange
		final double[] pos = new double[] { 172.5, 56.3 };
		final int[] indices = new int[] { 5, 2 };
		final int stepIndex = 23;
		final MapPosition position = new MapPosition();
		position.setStepIndex(stepIndex);
		final List<Set<String>> names = new ArrayList<>(positionerNames.length);
		for (int i = 0; i < positionerNames.length; i++) {
			position.put(positionerNames[i], pos[i]);
			position.putIndex(positionerNames[i], indices[i]);
			names.add(new LinkedHashSet<>(Arrays.asList(positionerNames[i])));
		}
		position.setDimensionNames(names);

		// act
		final PositionEvent posEvent = new PositionEvent(position, scanDevice);
		scanMetadataWriter.positionPerformed(posEvent);
		scanMetadataWriter.scanFinished();

		// assert
		// check data written to scan finished dataset
		final IDataset writtenToScanFinishedData = scanFinishedSaver.getLastWrittenData();
		assertThat(writtenToScanFinishedData, is(notNullValue()));
		assertThat(writtenToScanFinishedData.getRank(), is(1));
		assertThat(writtenToScanFinishedData.getShape(), is(equalTo(NexusScanMetadataWriter.SINGLE_SHAPE)));
		assertThat(writtenToScanFinishedData, is(instanceOf(IntegerDataset.class)));
		assertThat(writtenToScanFinishedData.getInt(0), is(1));

		// check data written to actual time dataset
		final IDataset writtenToActualTimeDataset = actualTimeSaver.getLastWrittenData();
		assertThat(writtenToActualTimeDataset, is(notNullValue()));
		assertThat(writtenToActualTimeDataset.getRank(), is(0));
		assertThat(writtenToActualTimeDataset.getShape(), is(equalTo(SCALAR_SHAPE)));
		assertThat(writtenToActualTimeDataset, is(instanceOf(LongDataset.class)));

		// check data written to dead time dataset
		final IDataset writtenToDeadTimeDataset = deadTimeSaver.getLastWrittenData();
		assertThat(writtenToDeadTimeDataset, is(notNullValue()));
		assertThat(writtenToDeadTimeDataset.getRank(), is(0));
		assertThat(writtenToDeadTimeDataset.getShape(), is(equalTo(SCALAR_SHAPE)));
		assertThat(writtenToDeadTimeDataset, is(instanceOf(LongDataset.class)));

		// check data written to dead time percent dataset
		final IDataset writtenToDeadTimePercentDataset = deadTimePercentSaver.getLastWrittenData();
		assertThat(writtenToDeadTimePercentDataset, is(notNullValue()));
		assertThat(writtenToDeadTimePercentDataset.getRank(), is(0));
		assertThat(writtenToDeadTimePercentDataset.getShape(), is(equalTo(SCALAR_SHAPE)));
		assertThat(writtenToDeadTimePercentDataset, is(instanceOf(FloatDataset.class)));

		// check data written to unique keys dataset
		final IDataset writtenToUniqueKeysData = uniqueKeysSaver.getLastWrittenData();
		assertThat(writtenToUniqueKeysData, is(notNullValue()));
		final int[] expectedShape = new int[scanRank];
		Arrays.fill(expectedShape, 1);
		assertThat(writtenToUniqueKeysData.getShape(), is(equalTo(expectedShape)));
		assertThat(writtenToUniqueKeysData, is(instanceOf(IntegerDataset.class)));
		final int[] valuePos = new int[scanRank]; // all zeros
		assertThat(writtenToUniqueKeysData.getInt(valuePos), is(stepIndex + 1));

		final SliceND uniqueKeysSlice = uniqueKeysSaver.getLastSlice();
		assertThat(uniqueKeysSlice, is(notNullValue()));
		assertThat(uniqueKeysSlice.getShape(), is(equalTo(expectedShape)));
		assertThat(uniqueKeysSlice.getStart(), is(equalTo(indices)));
		assertThat(uniqueKeysSlice.getStep(), is(equalTo(expectedShape))); // all ones
		final int[] stopIndices = Arrays.stream(indices).map(x -> x + 1).toArray();
		assertThat(uniqueKeysSlice.getStop(), is(equalTo(stopIndices)));
	}

	private void assertDevicesWritingKeys(Collection<NexusObjectProvider<?>> nexusObjectProviders, NXcollection keysCollection) {
		for (NexusObjectProvider<?> objectProvider : nexusObjectProviders) {
			final String deviceName = objectProvider.getName();
			final String uniqueKeysPath = (String) objectProvider.getPropertyValue(PROPERTY_NAME_UNIQUE_KEYS_PATH);
			final Set<String> externalFileNames = objectProvider.getExternalFileNames();
			if (externalFileNames.isEmpty()) {
				assertThat(deviceName, is(equalTo("internal")));
				final NXobject nexusObject = objectProvider.getNexusObject();
				final NodeLink uniqueKeysNodeLink = nexusObject.findNodeLink(PROPERTY_NAME_UNIQUE_KEYS_PATH);
				assertThat(uniqueKeysNodeLink, is(notNullValue()));
				assertThat(uniqueKeysNodeLink.isDestinationData(), is(true));
				final DataNode uniqueKeysNode = (DataNode) nexusObject.findNodeLink(PROPERTY_NAME_UNIQUE_KEYS_PATH).getDestination();
				assertThat(uniqueKeysNode, is(sameInstance(keysCollection.getDataNode(deviceName))));
			} else {
				final String externalFileName = externalFileNames.iterator().next();
				String datasetName = externalFileName.contains(objectProvider.getName()) ? objectProvider.getName() : "panda";
				SymbolicNode symbolicNode = keysCollection.getSymbolicNode(datasetName);
				assertThat(symbolicNode, is(notNullValue()));
				assertThat(symbolicNode.getPath(), is(equalTo(uniqueKeysPath)));
			}
		}
	}

}
