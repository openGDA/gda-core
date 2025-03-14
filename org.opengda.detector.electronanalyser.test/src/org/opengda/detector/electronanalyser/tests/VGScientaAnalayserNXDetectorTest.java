package org.opengda.detector.electronanalyser.tests;

import static gda.data.scan.datawriter.NexusScanDataWriter.PROPERTY_NAME_ENTRY_NAME;
import static org.eclipse.dawnsci.nexus.NexusConstants.DATA_AXES;
import static org.eclipse.dawnsci.nexus.NexusConstants.DATA_INDICES_SUFFIX;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.test.utilities.NexusTestUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.opengda.detector.electronanalyser.api.SESExcitationEnergySource;
import org.opengda.detector.electronanalyser.api.SESRegion;
import org.opengda.detector.electronanalyser.api.SESSequence;
import org.opengda.detector.electronanalyser.api.SESSequenceHelper;
import org.opengda.detector.electronanalyser.nxdetector.VGScientaAnalyserCollectionStrategy;
import org.opengda.detector.electronanalyser.nxdetector.VGScientaAnalyserNXDetector;
import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;
import org.opengda.detector.electronanalyser.utils.AnalyserRegionConstants;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.data.scan.datawriter.NexusScanDataWriter;
import gda.data.scan.nexus.NexusScanDataWriterTestSetup;
import gda.device.DeviceException;
import gda.device.scannable.DummyScannable;
import gda.jython.ICurrentScanInformationHolder;
import gda.jython.InterfaceProvider;
import gda.scan.ConcurrentScan;
import gda.scan.ScanInformation;

public class VGScientaAnalayserNXDetectorTest {

	private static final String ENTRY_NAME = "entry1";

	private static VGScientaAnalyserNXDetector detector;
	private static VGScientaAnalyserCollectionStrategy collectionStrategy;
	private static VGScientaAnalyser mockAnalyser;

	private static String outputDir;

	@BeforeEach
	public void setup() throws Exception {
		SetupSESSettingsTestHelper.setupFinderAndSESSettingsService();
		setupScan();
		setupDetector();
	}

	public static void setupScan() throws Exception {
		NexusScanDataWriterTestSetup.setUp();
		final String testDir = TestHelpers.setUpTest(VGScientaAnalayserNXDetectorTest.class, "test", true, NexusScanDataWriter.class);
		outputDir = testDir + File.separator + "Data";

		LocalProperties.set(PROPERTY_NAME_ENTRY_NAME, ENTRY_NAME);
	}

	public static void setupDetector() {
		mockAnalyser = mock(VGScientaAnalyser.class);

		collectionStrategy = new VGScientaAnalyserCollectionStrategy();
		collectionStrategy.setName("collectionStrategy");
		collectionStrategy.setAnalyser(mockAnalyser);

		detector = new VGScientaAnalyserNXDetector();
		detector.setName("detector");
		detector.setCollectionStrategy(collectionStrategy);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace(detector.getName(), detector);
	}

	@AfterEach
	public void tearDown() {
		SetupSESSettingsTestHelper.tearDown();
		NexusScanDataWriterTestSetup.tearDown();
		Mockito.reset(mockAnalyser);
	}

	public static String[] getTestSequenceFiles() {
		return new String[] {
			VGScientaAnalayserNXDetectorTest.class.getSimpleName() + File.separator + "test1.seq",
			VGScientaAnalayserNXDetectorTest.class.getSimpleName() + File.separator +"test2.seq",
			VGScientaAnalayserNXDetectorTest.class.getSimpleName() + File.separator +"test3.seq"
		};
	}

	@ParameterizedTest
	@MethodSource("getTestSequenceFiles")
	public void sequenceFileDetectorScanTests(String sequenceFile) throws Exception {
		final String sequenceFilePath = SESSequenceHelper.getDefaultFilePath() + File.separator + sequenceFile;
		final SESSequence sequence = SESSequenceHelper.loadSequence(sequenceFilePath);

		//Setup each excitation energy source scannable to be in sync with the sequence file.
		for (SESExcitationEnergySource e : sequence.getExcitationEnergySources()) {
			e.getScannable().asynchronousMoveTo(e.getValue());
		}
		final String nexusFilePath = runDetectorScan(sequence);

		// check the content of the nexus file
		try (final NexusFile nexusFile = NexusTestUtils.openNexusFile(nexusFilePath)) {
			final TreeFile nexusTree = NexusUtils.loadNexusTree(nexusFile);
			final NXroot nexusRoot = (NXroot) nexusTree.getGroupNode();
			assertThat(nexusRoot, is(notNullValue()));
			final NXentry entry = nexusRoot.getEntry(ENTRY_NAME);
			assertThat(entry, is(notNullValue()));
			for (final SESRegion region : sequence.getEnabledRegions()) {
				final NXdata regionNode = entry.getData(region.getName());
				checkNXdataRegion(regionNode);

				if (region.isEnergyModeBinding()) {
					final double excitationEnergy = sequence.getExcitationEnergySourceByRegion(region).getValue();
					checkNXdataRegionBindingEnergy(regionNode, excitationEnergy);
				}
			}
			checkNXInstrumentDetectorRegionList(entry.getData(detector.getName()), sequence.getEnabledRegionNames());
		}
	}

	private String runDetectorScan(SESSequence sequence) throws Exception {
		final DummyScannable x = new DummyScannable("x");
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace(x.getName(), x);
		final DummyScannable y = new DummyScannable("y");
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace(y.getName(), y);

		when(mockAnalyser.getPsuMode()).thenReturn(sequence.getElementSet());

		//Must use Answer so that mock analyser can dynamically get values based on region running in scan.
		when(mockAnalyser.getEnergyAxis()).thenAnswer(i -> {
			return new double[collectionStrategy.calculateEnergyAxisSize(detector.getCurrentRegion())];
		});
		when(mockAnalyser.getAngleAxis()).thenAnswer(i -> {
			return new double[collectionStrategy.calculateAngleAxisSize(detector.getCurrentRegion())];
		});
		when(mockAnalyser.getExternalIODataFormatted()).thenAnswer(i -> {
			return new double[collectionStrategy.calculateExternalIOSize(detector.getCurrentRegion())];
		});
		when(mockAnalyser.getSpectrum()).thenAnswer(i -> {
			return new double[collectionStrategy.calculateEnergyAxisSize(detector.getCurrentRegion())];
		});
		when(mockAnalyser.getImage(anyInt())).thenAnswer(i -> {
			return new double[collectionStrategy.calculateAngleAxisSize(detector.getCurrentRegion()) * collectionStrategy.calculateEnergyAxisSize(detector.getCurrentRegion())];
		});
		when(mockAnalyser.getExcitationEnergy()).thenAnswer(i -> {
			return sequence.getExcitationEnergySourceByRegion(detector.getCurrentRegion()).getValue();
		});

		detector.setSequence(sequence);
		final ConcurrentScan scan = new ConcurrentScan(new Object[] {x, 1, 2, 1, y, 1, 2, 1, detector});
		scan.runScan();

		final File expectedNexusFile = new File(outputDir + File.separator + "1.nxs");
		final String expectedNexusFilePath = expectedNexusFile.getAbsolutePath();
		assertThat(expectedNexusFile.exists(), is(true));
		assertThat(scan.getDataWriter().getCurrentFileName(), is(equalTo(expectedNexusFilePath)));

		return expectedNexusFilePath;
	}

	private void checkNXdataRegion(NXdata regionData) {
		final int[] scanDimensions = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation().getDimensions();

		final ILazyDataset imageData = regionData.getDataNode(AnalyserRegionConstants.IMAGE_DATA).getDataset();
		assertNotNull(imageData);
		assertThat(imageData.getShape().length, is(equalTo(scanDimensions.length + 2)));
		final ILazyDataset spectrumData = regionData.getDataNode(AnalyserRegionConstants.SPECTRUM_DATA).getDataset();
		assertNotNull(spectrumData);
		assertThat(spectrumData.getShape().length, is(equalTo(scanDimensions.length + 1)));
		final ILazyDataset externalIOData = regionData.getDataNode(AnalyserRegionConstants.EXTERNAL_IO_DATA).getDataset();
		assertNotNull(externalIOData);
		assertThat(externalIOData.getShape().length, is(equalTo(scanDimensions.length + 1)));
		final ILazyDataset energiesData = regionData.getDataNode(AnalyserRegionConstants.ENERGIES).getDataset();
		assertNotNull(energiesData);
		assertThat(energiesData.getShape().length, is(equalTo(1)));
		final ILazyDataset anglesData = regionData.getDataNode(AnalyserRegionConstants.ANGLES).getDataset();
		assertNotNull(anglesData);
		assertThat(anglesData.getShape().length, is(equalTo(1)));

		assertThat(regionData.getAttributeSignal(), is(equalTo(AnalyserRegionConstants.IMAGE_DATA)));

		final Attribute axes = regionData.getAttribute(DATA_AXES);
		assertNotNull(axes);
		final int axesSize = axes.getSize();

		final int anglesAxisIndex = axesSize - 2;
		final int energiesAxisIndex = axesSize - 1;
		assertThat(axes.getValue().getString(anglesAxisIndex), is(equalTo(AnalyserRegionConstants.ANGLES)));
		assertThat(axes.getValue().getString(energiesAxisIndex), is(equalTo(AnalyserRegionConstants.ENERGIES)));
		checkRegionDataIndices(regionData, AnalyserRegionConstants.SPECTRUM_DATA, energiesAxisIndex);
		checkRegionDataIndices(regionData, AnalyserRegionConstants.EXTERNAL_IO_DATA, energiesAxisIndex);
	}

	protected void checkRegionDataIndices(NXdata regionData, String name, int axisIndexToTestAgainst) {
		final Attribute dataIndices = regionData.getAttribute(name + DATA_INDICES_SUFFIX);
		assertNotNull(dataIndices);
		final int dataIndicesSize = dataIndices.getSize();
		assertThat(dataIndices.getValue().getInt(dataIndicesSize -1), is(equalTo(axisIndexToTestAgainst)));
	}

	protected void checkNXdataRegionBindingEnergy(final NXdata regionData, final double excitationEnergy) throws Exception {
		final IDataset bindingEnergyData = regionData.getData(AnalyserRegionConstants.ENERGIES);
		assertNotNull(bindingEnergyData);
		final double[] originalEnergyData = mockAnalyser.getEnergyAxis();
		assertNotNull(originalEnergyData);

		assertThat(bindingEnergyData.getSize(), is(equalTo(originalEnergyData.length)));
		for (int i = 0; i < bindingEnergyData.getSize(); i++) {
			final double bindingEnergyPoint = bindingEnergyData.getDouble(i);
			final double originalEnergyPoint = originalEnergyData[i];
			final double bindingEnergyPointToMatch = excitationEnergy - originalEnergyPoint;
			assertThat(bindingEnergyPoint, is(bindingEnergyPointToMatch));
		}
	}

	protected void checkNXInstrumentDetectorRegionList(final NXdata detectorData, final List<String> enabledRegionNames) {
		final IDataset regionListData = detectorData.getData(AnalyserRegionConstants.REGION_LIST);
		assertThat(regionListData.getSize(), is(enabledRegionNames.size()));
		for (int i = 0; i < enabledRegionNames.size(); i++) {
			assertThat(regionListData.getString(i), is(enabledRegionNames.get(i)));
		}
	}

	@Test
	public void testPropertyCreateFileAtScanStartIsSetAtScanStartAndScanEnd() throws DeviceException {
		final boolean propertyBeforeScan = LocalProperties.check(NexusScanDataWriter.PROPERTY_NAME_CREATE_FILE_AT_SCAN_START, false);

		final ICurrentScanInformationHolder scanInfoHolder= mock(ICurrentScanInformationHolder.class);
		InterfaceProvider.setCurrentScanInformationHolderForTesting(scanInfoHolder);
		when(scanInfoHolder.getCurrentScanInformation()).thenReturn(mock(ScanInformation.class));

		detector.atScanStart();
		final boolean propertyAfterAtScanStart = LocalProperties.check(NexusScanDataWriter.PROPERTY_NAME_CREATE_FILE_AT_SCAN_START, false);
		assertThat(propertyAfterAtScanStart, is(equalTo(true)));

		detector.scanEnd();
		final boolean propertyAfterAtScanEnd = LocalProperties.check(NexusScanDataWriter.PROPERTY_NAME_CREATE_FILE_AT_SCAN_START, false);
		assertThat(propertyAfterAtScanEnd, is(equalTo(propertyBeforeScan)));
	}
}