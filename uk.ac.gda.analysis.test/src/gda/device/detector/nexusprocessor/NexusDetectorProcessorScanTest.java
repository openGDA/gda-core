/*-
 * Copyright © 2023 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.device.detector.nexusprocessor;

import static gda.configuration.properties.LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT;
import static gda.data.nexus.extractor.NexusExtractor.SDSClassName;
import static gda.data.scan.datawriter.NexusScanDataWriter.PROPERTY_VALUE_DATA_FORMAT_NEXUS_SCAN;
import static gda.data.scan.nexus.device.DummyNexusDetector.ARRAY_ATTR_NAME;
import static gda.data.scan.nexus.device.DummyNexusDetector.ARRAY_ATTR_VALUE;
import static gda.data.scan.nexus.device.DummyNexusDetector.DETECTOR_NAME;
import static gda.data.scan.nexus.device.DummyNexusDetector.DETECTOR_NUMBER;
import static gda.data.scan.nexus.device.DummyNexusDetector.DIAMETER;
import static gda.data.scan.nexus.device.DummyNexusDetector.DIAMETER_UNITS;
import static gda.data.scan.nexus.device.DummyNexusDetector.FIELD_NAME_EXTERNAL;
import static gda.data.scan.nexus.device.DummyNexusDetector.FIELD_NAME_IMAGE_X;
import static gda.data.scan.nexus.device.DummyNexusDetector.FIELD_NAME_IMAGE_Y;
import static gda.data.scan.nexus.device.DummyNexusDetector.FIELD_NAME_SPECTRUM;
import static gda.data.scan.nexus.device.DummyNexusDetector.FIELD_NAME_VALUE;
import static gda.data.scan.nexus.device.DummyNexusDetector.FLOAT_ATTR_NAME;
import static gda.data.scan.nexus.device.DummyNexusDetector.FLOAT_ATTR_VALUE;
import static gda.data.scan.nexus.device.DummyNexusDetector.GAIN_SETTING;
import static gda.data.scan.nexus.device.DummyNexusDetector.IMAGE_SIZE;
import static gda.data.scan.nexus.device.DummyNexusDetector.INT_ATTR_NAME;
import static gda.data.scan.nexus.device.DummyNexusDetector.INT_ATTR_VALUE;
import static gda.data.scan.nexus.device.DummyNexusDetector.SERIAL_NUMBER;
import static gda.data.scan.nexus.device.DummyNexusDetector.SPECTRUM_SIZE;
import static gda.data.scan.nexus.device.DummyNexusDetector.STRING_ATTR_NAME;
import static gda.data.scan.nexus.device.DummyNexusDetector.STRING_ATTR_VALUE;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_LOCAL_NAME;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_SCAN_ROLE;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_UNITS;
import static gda.device.detector.nexusprocessor.roistats.NormalisingRegionProcessor.FIELD_NAME_NORM;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;
import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.FIELD_NAME_SCAN_FIELDS;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertAuxiliarySignals;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertAxes;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertDataNodesEqual;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertIndices;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertSignal;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.measure.quantity.Angle;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROIList;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.test.utilities.NexusAssert;
import org.eclipse.dawnsci.nexus.test.utilities.NexusTestUtils;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.scanning.api.device.ScanRole;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.data.scan.nexus.NexusScanDataWriterTestSetup;
import gda.data.scan.nexus.device.DummyNexusDetector;
import gda.device.Scannable;
import gda.device.detector.nexusprocessor.DatasetStats.Statistic;
import gda.device.detector.nexusprocessor.roistats.NormalisingRegionProcessor;
import gda.device.detector.nexusprocessor.roistats.RoiStatsProcessor;
import gda.device.scannable.DummyScannable;
import gda.device.scannable.DummyUnitsScannable;
import gda.jython.InterfaceProvider;
import gda.scan.ConcurrentScan;
import gda.scan.IScanDataPoint;
import uk.ac.diamond.scisoft.analysis.PlotServer;
import uk.ac.diamond.scisoft.analysis.PlotServerProvider;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;

public class NexusDetectorProcessorScanTest {

	private static final int NUM_POINTS = 5;

	private static final int EXPECTED_SCAN_NUMBER = 1;

	private static final String SCANNABLE_NAME = "theta";

	private static final String ATTENUATOR_SCANNABLE_NAME = "attn";
	private static final double ATTENUTATOR_POSITION = 0.75;

	private static final double START_VALUE = 0.0;
	private static final double STOP_VALUE = NUM_POINTS - 1.0;
	private static final double STEP_VALUE = 1.0;

	private static final List<Statistic> DATA_STATISTICS = List.of(Statistic.SUM,
			Statistic.MAX_X, Statistic.MAX_Y, Statistic.MAX_VAL, Statistic.MEAN);
	private static final List<Statistic> ROI_STATS = List.of(Statistic.SUM, Statistic.STDEV);

	private static final String PLOT_NAME = "plot1"; // for finding GUIBean

	private static final List<RectangularROI> ROIS = List.of(
			createROI("topLeft", 0, 0, 4, 4),
			createROI("centre", 2, 2, 4, 4),
			createROI("bottomRight", 4, 4, 4, 4));

	private Scannable scannable;
	private Scannable attenutatorScannable;

	private DummyNexusDetector detector;
	private NexusDetectorProcessor detectorProcessor;

	private String outputDir;

	private static RectangularROI createROI(String name, double xStart, double yStart,
			double width, double height) {
		final RectangularROI roi = new RectangularROI();
		roi.setName(name);
		roi.setPlot(true);
		roi.setPoint(xStart, yStart);
		roi.setLengths(width, height);
		roi.setAngle(0);
		return roi;
	}

	@BeforeAll
	public static void setUpServices() {
		NexusScanDataWriterTestSetup.setUp();
	}

	@AfterAll
	public static void tearDownServicesAndProperties() {
		NexusScanDataWriterTestSetup.tearDown();
	}

	@BeforeEach
	void setUp() throws Exception {
		scannable = new DummyUnitsScannable<Angle>(SCANNABLE_NAME, 0.0, "deg", "deg");

		attenutatorScannable = new DummyScannable(ATTENUATOR_SCANNABLE_NAME, ATTENUTATOR_POSITION); // input field name is same as scannable name

		detector = new DummyNexusDetector();
		detector.setScanDimensions(new int[] { NUM_POINTS });

		// dataset processors for the 'data' dataset
		final DatasetFitter dataDatasetFitter = new DatasetFitter();
		dataDatasetFitter.afterPropertiesSet(); // this would normally be called by spring

		final DatasetStats dataDatasetStats = new DatasetStats(DATA_STATISTICS);
		dataDatasetStats.setUseSingleDataGroup(true);
		final RoiStatsProcessor roiStats = createRoiStatsProcessor();
		final NormalisingRegionProcessor normProcessor = createNormalisingRegionProcessor(roiStats);

		final List<DatasetProcessor> dataDatasetProcessors = List.of(dataDatasetFitter,
				dataDatasetStats, roiStats, normProcessor);
		final NexusTreeProviderProcessor dataTreeDatasetProcessor = new NexusProviderDatasetProcessor(
				DETECTOR_NAME, NXdetector.NX_DATA, SDSClassName, dataDatasetProcessors, null);

		detectorProcessor = new NexusDetectorProcessor();
		detectorProcessor.setDetector(detector);
		detectorProcessor.setName(detector.getName()); // set by FindableNamePostProcessor when defined in spring
		detectorProcessor.setProcessor(dataTreeDatasetProcessor);
	}

	private RoiStatsProcessor createRoiStatsProcessor() throws Exception {
		final RoiStatsProcessor roiStats = new RoiStatsProcessor();
		roiStats.setPlotName(PLOT_NAME);
		roiStats.setStatsProcessor(new DatasetStats(ROI_STATS));
		roiStats.setUseSingleDataGroupPerRoi(true);

		final RectangularROIList roiList = new RectangularROIList();
		roiList.addAll(ROIS.stream().map(RectangularROI::new).toList());

		final GuiBean guiBean = new GuiBean();
		guiBean.put(GuiParameters.ROIDATALIST, roiList);

		final PlotServer plotServer = mock(PlotServer.class);
		when(plotServer.getGuiState(PLOT_NAME)).thenReturn(guiBean);
		PlotServerProvider.setPlotServer(plotServer);

		roiStats.atScanStart(); // should be called by the scan, but isn't. TODO: fix or create JIRA ticket
		return roiStats;
	}

	private NormalisingRegionProcessor createNormalisingRegionProcessor(RoiStatsProcessor roiStats) {
		final NormalisingRegionProcessor normProc = new NormalisingRegionProcessor();
		normProc.setAttenuatorScannableName(ATTENUATOR_SCANNABLE_NAME);
		normProc.setTransmissionFieldName(ATTENUATOR_SCANNABLE_NAME); // field name is same as scannable name
		normProc.setRoiStats(roiStats);

		return normProc;
	}

	@AfterEach
	void tearDown() {
		PlotServerProvider.setPlotServer(null);

		detector = null;
		detectorProcessor = null;
	}

	private void setUpTest(String testName) throws Exception {
		final String testDir = TestHelpers.setUpTest(getClass(), testName, true);
		outputDir = testDir + "/Data";

		LocalProperties.set(GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, PROPERTY_VALUE_DATA_FORMAT_NEXUS_SCAN);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace(scannable.getName(), scannable); // can only be done after setting calling TestHelpers.setUpTest
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace(ATTENUATOR_SCANNABLE_NAME, attenutatorScannable);
	}

	@Test
	void concurrentScanNexusDetectorNoProcessors() throws Exception {
		setUpTest("concurrentScanNexusDetectorNoProcessors");

		detector.setOutputDir(outputDir);
		final Object[] scanArguments = { scannable, START_VALUE, STOP_VALUE, STEP_VALUE, detectorProcessor };
		final ConcurrentScan scan = new ConcurrentScan(scanArguments);

		scan.runScan();

		assertThat(scan.getScanNumber(), is(EXPECTED_SCAN_NUMBER));
		final IScanDataPoint lastPoint = InterfaceProvider.getScanDataPointProvider().getLastScanDataPoint();
		assertThat(lastPoint.getScanIdentifier(), is(EXPECTED_SCAN_NUMBER));

		// check the nexus file was written with the expected name
		final File expectedNexusFile = new File(outputDir + "/" + EXPECTED_SCAN_NUMBER + ".nxs");
		final String expectedNexusFilePath = expectedNexusFile.getAbsolutePath();
		assertThat(expectedNexusFile.exists(), is(true));
		assertThat(lastPoint.getCurrentFilename(), is(equalTo(expectedNexusFilePath)));
		assertThat(scan.getDataWriter().getCurrentFileName(), is(equalTo(expectedNexusFilePath)));

		// check the content of the nexus file
		try (final NexusFile nexusFile = NexusTestUtils.openNexusFile(expectedNexusFilePath)) {
			checkNexusFile(nexusFile);
		}
	}

	private void checkNexusFile(NexusFile nexusFile) throws Exception {
		final TreeFile nexusTree = NexusUtils.loadNexusTree(nexusFile);
		final NXroot nexusRoot = (NXroot) nexusTree.getGroupNode();
		assertThat(nexusRoot, is(notNullValue()));
		final NXentry entry = nexusRoot.getEntry();
		assertThat(entry, is(notNullValue()));
		assertThat(entry.getDataset(FIELD_NAME_SCAN_FIELDS),
				is(equalTo(DatasetFactory.createFromObject(getExpectedScanFieldNames()))));

		final NXinstrument instrument = entry.getInstrument();
		assertThat(instrument, is(notNullValue()));

		assertThat(instrument.getAllDetector().keySet(), contains(DETECTOR_NAME));
		final NXdetector detGroup = instrument.getDetector(DETECTOR_NAME);
		assertThat(detGroup, is(notNullValue()));

		checkNexusDetector(detGroup);

		checkDataGroups(entry);
	}

	private void checkNexusDetectorGroupAttributes(NXdetector detGroup) {
		assertThat(detGroup.getAttributeNames(), containsInAnyOrder(NexusConstants.NXCLASS,
				ATTRIBUTE_NAME_LOCAL_NAME, ATTRIBUTE_NAME_SCAN_ROLE,
				STRING_ATTR_NAME, INT_ATTR_NAME, FLOAT_ATTR_NAME, ARRAY_ATTR_NAME));

		assertThat(detGroup.getAttribute(NexusConstants.NXCLASS).getFirstElement(), is(equalTo(NexusBaseClass.NX_DETECTOR.toString())));
		assertThat(detGroup.getAttribute(ATTRIBUTE_NAME_LOCAL_NAME).getFirstElement(), is(equalTo(DETECTOR_NAME)));
		assertThat(detGroup.getAttribute(ATTRIBUTE_NAME_SCAN_ROLE).getFirstElement(), is(equalTo(ScanRole.DETECTOR.toString().toLowerCase())));
		assertThat(detGroup.getAttribute(STRING_ATTR_NAME).getFirstElement(), is(equalTo(STRING_ATTR_VALUE)));
		assertThat(detGroup.getAttribute(INT_ATTR_NAME).getValue(), is(equalTo(DatasetFactory.createFromObject(new int[] { INT_ATTR_VALUE }))));
		assertThat(detGroup.getAttribute(FLOAT_ATTR_NAME).getValue(), is(equalTo(DatasetFactory.createFromObject(new double[] { FLOAT_ATTR_VALUE }))));
		assertThat(detGroup.getAttribute(ARRAY_ATTR_NAME).getValue(), is(equalTo(DatasetFactory.createFromObject(ARRAY_ATTR_VALUE))));
	}

	private void checkNexusDetector(NXdetector detGroup) {
		assertThat(detGroup, is(notNullValue()));

		checkNexusDetectorGroupAttributes(detGroup);

		assertThat(detGroup.getDataNodeNames(), containsInAnyOrder(getExpectedDataNodeNames()));
		assertThat(detGroup.getLocal_nameScalar(), is(equalTo(DETECTOR_NAME)));
		assertThat(detGroup.getDetector_numberScalar(), is(equalTo(DETECTOR_NUMBER)));
		assertThat(detGroup.getSerial_numberScalar(), is(equalTo(SERIAL_NUMBER)));
		assertThat(detGroup.getDiameterScalar(), is(equalTo(DIAMETER)));
		assertThat(detGroup.getAttrString(NXdetector.NX_DIAMETER, ATTRIBUTE_NAME_UNITS), is(DIAMETER_UNITS));
		assertThat(detGroup.getGain_settingScalar(), is(equalTo(GAIN_SETTING)));

		checkDataNode(detGroup.getDataNode(FIELD_NAME_VALUE), NexusAssert.EMPTY_SHAPE);
		checkDataNode(detGroup.getDataNode(FIELD_NAME_SPECTRUM), new int[] { SPECTRUM_SIZE });
		checkDataNode(detGroup.getDataNode(NXdetector.NX_DATA), IMAGE_SIZE);
		checkDataNode(detGroup.getDataNode(FIELD_NAME_EXTERNAL), IMAGE_SIZE);

		for (int dimNum = 1; dimNum <= 2; dimNum++) {
			for (String name : DatasetFitter.NAMES_PER_DIM) {
				checkExtraNameDataNode(detGroup, dimNum + "_" + name, NXdetector.NX_DATA);
			}
		}

		for (Statistic stat : DATA_STATISTICS) {
			checkExtraNameDataNode(detGroup, stat.getDefaultName(), NXdetector.NX_DATA);
		}

		for (RectangularROI roi : ROIS) {
			for (Statistic stat : ROI_STATS) {
				checkExtraNameDataNode(detGroup, roi.getName() + "." + stat.getDefaultName(), null);
			}
		}

		checkExtraNameDataNode(detGroup, FIELD_NAME_NORM, null);
	}

	private void checkExtraNameDataNode(NXdetector detGroup, final String extraName, String prefix) {
		final String dataNodeName = (prefix == null ? "" : prefix + ".") + extraName;
		final DataNode dataNode = detGroup.getDataNode(dataNodeName);
		assertThat(dataNode, is(notNullValue()));

		final Attribute attr = dataNode.getAttribute(ATTRIBUTE_NAME_LOCAL_NAME);
		assertThat(attr, is(notNullValue()));
		assertThat(attr.getFirstElement(), is(equalTo(DETECTOR_NAME + "." + extraName)));
	}

	private String[] getExpectedDataNodeNames() {
		final List<String> dataNodeNames = new ArrayList<>();
		dataNodeNames.addAll(List.of(FIELD_NAME_EXTERNAL, NXdetector.NX_LOCAL_NAME, NXdetector.NX_DETECTOR_NUMBER,
				NXdetector.NX_DIAMETER, NXdetector.NX_SERIAL_NUMBER, NXdetector.NX_GAIN_SETTING,
				DummyNexusDetector.FIELD_NAME_IMAGE_X, DummyNexusDetector.FIELD_NAME_IMAGE_Y));

		// data nodes added directly by the NexusDetector
		dataNodeNames.add(NXdetector.NX_DATA);
		dataNodeNames.add(FIELD_NAME_SPECTRUM);
		dataNodeNames.add(FIELD_NAME_VALUE);

		// extra name fields from DatasetFitter
		dataNodeNames.addAll(DatasetFitter.NAMES_PER_DIM.stream().map(name -> NXdetector.NX_DATA + ".1_" + name).toList());
		dataNodeNames.addAll(DatasetFitter.NAMES_PER_DIM.stream().map(name -> NXdetector.NX_DATA + ".2_" + name).toList());

		// extra name fields from DatasetStats
		dataNodeNames.addAll(DATA_STATISTICS.stream()
				.map(stat -> NXdetector.NX_DATA + "." + stat.getDefaultName())
				.toList());

		// extra name fields from RoiStateProcessor
		final List<String> roiFieldNames = ROIS.stream()
				.sorted(comparing(IRectangularROI::getName))
				.map(roi -> ROI_STATS.stream().map(stat -> roi.getName() + "." + stat.getDefaultName()))
				.flatMap(Function.identity())
				.toList();
		dataNodeNames.addAll(roiFieldNames);

		// extra name field for NormalisingRegionProcessor
		dataNodeNames.add(FIELD_NAME_NORM);

		return dataNodeNames.toArray(String[]::new);
	}

	private void checkDataNode(DataNode dataNode, int[] expectedDataShape) {
		final int[] expectedShape = new int[expectedDataShape.length + 1];
		expectedShape[0] = NUM_POINTS; // prefix the data shape with the number of points, as this is a 1d scan
		System.arraycopy(expectedDataShape, 0, expectedShape, 1, expectedDataShape.length);

		assertThat(dataNode, is(notNullValue()));
		assertThat(dataNode.getDataset().getShape(), is(equalTo(expectedShape)));
	}

	private List<String> getExpectedScanFieldNames() {
		return Stream.concat(
					Stream.of(SCANNABLE_NAME + "." + SCANNABLE_NAME),
					getDetectorFieldNames().stream().map(fieldName -> DETECTOR_NAME + "." + fieldName)
				).toList();
	}

	private List<String> getDetectorFieldNames() {
		final List<String> detectorFieldNames = new ArrayList<>();
		detectorFieldNames.addAll(Arrays.asList(detector.getExtraNames()));

		// extra name fields from DatasetFitter
		detectorFieldNames.addAll(DatasetFitter.NAMES_PER_DIM.stream().map(name -> "1_" + name).toList());
		detectorFieldNames.addAll(DatasetFitter.NAMES_PER_DIM.stream().map(name -> "2_" + name).toList());

		// extra name fields from DatasetStats
		detectorFieldNames.addAll(DATA_STATISTICS.stream().map(Statistic::getDefaultName).toList());

		// extra name fields from RoiStateProcessor
		final List<String> roiFieldNames = ROIS.stream()
				.sorted(comparing(IRectangularROI::getName))
				.map(roi -> ROI_STATS.stream().map(stat -> roi.getName() + "." + stat.getDefaultName()))
				.flatMap(Function.identity())
				.toList();
		detectorFieldNames.addAll(roiFieldNames);

		// extra name field for NormalisingRegionProcessor
		detectorFieldNames.add(FIELD_NAME_NORM);

		return detectorFieldNames;
	}

	private void checkDataGroups(NXentry entry) {
		final List<String> fieldNames = List.of(NXdetector.NX_DATA,
				FIELD_NAME_SPECTRUM, FIELD_NAME_VALUE, FIELD_NAME_EXTERNAL);

		final List<String> expectedDataGroupNames = fieldNames.stream()
				.map(name -> DETECTOR_NAME + (name.equals(NXdetector.NX_DATA) ? "" : "_" + name))
				.collect(toCollection(ArrayList::new)); // ArrayList so we can add more elements
		expectedDataGroupNames.add(DETECTOR_NAME + "_" + NXdetector.NX_DATA); // from the DatasetStats group
		expectedDataGroupNames.addAll(ROIS.stream()
				.map(roi -> roi.getName())
				.map(name -> DETECTOR_NAME + "_" + name)
				.toList());
		expectedDataGroupNames.add(DETECTOR_NAME + "_" + FIELD_NAME_NORM);
		assertThat(entry.getAllData().keySet(), containsInAnyOrder(expectedDataGroupNames.toArray()));

		for (String fieldName : fieldNames) {
			checkDataGroup(entry, fieldName);
		}
		checkDataGroup(entry, FIELD_NAME_NORM);

		checkAuxiliaryDataGroup(entry, "data", DATA_STATISTICS);
		for (IROI roi : ROIS) {
			checkAuxiliaryDataGroup(entry, roi.getName(), ROI_STATS);
		}
	}

	private void checkDataGroup(NXentry entry, String signalFieldName) {
		final String dataGroupName = DETECTOR_NAME +
				(signalFieldName.equals(NXdetector.NX_DATA) ? "" : "_" + signalFieldName);
		final NXdata dataGroup = entry.getData(dataGroupName);
		assertThat(dataGroup, is(notNullValue()));

		final Set<String> expectedDataNodeNames = new HashSet<>(Set.of(signalFieldName, SCANNABLE_NAME));
		if (signalFieldName.equals(NXdetector.NX_DATA)) {
			expectedDataNodeNames.addAll(Set.of(FIELD_NAME_IMAGE_X, FIELD_NAME_IMAGE_Y));
		}

		assertThat(dataGroup.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames.toArray()));
		assertSignal(dataGroup, signalFieldName);
		assertAuxiliarySignals(dataGroup);

		final int signalFieldRank = dataGroup.getDataNode(signalFieldName).getRank();
		final String[] expectedAxes = Collections.nCopies(signalFieldRank, ".").toArray(String[]::new);
		expectedAxes[0] = SCANNABLE_NAME;
		if (signalFieldName.equals(NXdetector.NX_DATA)) {
			expectedAxes[1] = FIELD_NAME_IMAGE_X;
			expectedAxes[2] = FIELD_NAME_IMAGE_Y;
		}
		assertAxes(dataGroup, expectedAxes);

		assertIndices(dataGroup, SCANNABLE_NAME, 0);

		assertDataNodesEqual("/entry/instrument/" + DETECTOR_NAME + "/" + signalFieldName,
				entry.getInstrument().getDetector(DETECTOR_NAME).getDataNode(signalFieldName),
				dataGroup.getDataNode(signalFieldName));
		assertDataNodesEqual("/entry/instrument/" + SCANNABLE_NAME + "/" + NXpositioner.NX_VALUE,
				entry.getInstrument().getPositioner(SCANNABLE_NAME).getDataNode(NXpositioner.NX_VALUE),
				dataGroup.getDataNode(SCANNABLE_NAME));

		NXinstrument instr = entry.getInstrument();
		if (signalFieldName.equals(NXdetector.NX_DATA)) {
			assertIndices(dataGroup, FIELD_NAME_IMAGE_X, 1);
			assertDataNodesEqual("/entry/instrument/" + DETECTOR_NAME + "/" + FIELD_NAME_IMAGE_X,
					instr.getDetector(DETECTOR_NAME).getDataNode(FIELD_NAME_IMAGE_X),
					dataGroup.getDataNode(FIELD_NAME_IMAGE_X));

			assertIndices(dataGroup, FIELD_NAME_IMAGE_Y, 2);
			assertDataNodesEqual("/entry/instrument/" + DETECTOR_NAME + "/" + FIELD_NAME_IMAGE_Y,
					instr.getDetector(DETECTOR_NAME).getDataNode(FIELD_NAME_IMAGE_Y),
					dataGroup.getDataNode(FIELD_NAME_IMAGE_Y));
		}
	}

	private void checkAuxiliaryDataGroup(NXentry entry, String dataGroupSuffix,
			List<Statistic> stats) {
		final NXdata dataGroup = entry.getData(DETECTOR_NAME + "_" + dataGroupSuffix);
		assertThat(dataGroup, is(notNullValue()));

		// TODO: find way to remove prefix for auxiliary NXdata groups
		final List<String> signalFieldNames = stats.stream()
				.map(Statistic::getDefaultName)
				.map(name -> dataGroupSuffix + "." + name)
				.toList();
		assertThat(dataGroup.getDataNodeNames(), containsInAnyOrder(
				ArrayUtils.add(signalFieldNames.toArray(String[]::new), SCANNABLE_NAME)));
		assertSignal(dataGroup, signalFieldNames.get(0));
		assertAuxiliarySignals(dataGroup, signalFieldNames.stream().skip(1).toArray(String[]::new));
		assertAxes(dataGroup, SCANNABLE_NAME);

		for (String auxSignalFieldName : signalFieldNames) {
			assertDataNodesEqual("/entry/instrument/" + DETECTOR_NAME + "/" + auxSignalFieldName,
					entry.getInstrument().getDetector(DETECTOR_NAME).getDataNode(auxSignalFieldName),
					dataGroup.getDataNode(auxSignalFieldName));
		}

		assertIndices(dataGroup, SCANNABLE_NAME, 0);
		assertDataNodesEqual("/entry/instrument/" + SCANNABLE_NAME + "/" + NXpositioner.NX_VALUE,
				entry.getInstrument().getPositioner(SCANNABLE_NAME).getDataNode(NXpositioner.NX_VALUE),
				dataGroup.getDataNode(SCANNABLE_NAME));
	}

}
