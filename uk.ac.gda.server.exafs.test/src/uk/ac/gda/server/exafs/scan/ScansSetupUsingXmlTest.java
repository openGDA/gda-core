/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.server.exafs.scan;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileHDF5;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.StringDataset;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.data.metadata.NXMetaDataProvider;
import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import gda.data.scan.datawriter.NexusDataWriter;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.BufferedDetector;
import gda.device.detector.DUMMY_XSPRESS2_MODE;
import gda.device.detector.DummyDAServer;
import gda.device.detector.QexafsFFoverIO;
import gda.device.detector.TfgFFoverI0;
import gda.device.detector.countertimer.BufferedScaler;
import gda.device.detector.countertimer.TfgScaler;
import gda.device.detector.countertimer.TfgScalerWithFrames;
import gda.device.detector.countertimer.TfgScalerWithLogValues;
import gda.device.detector.xspress.Xspress2BufferedDetector;
import gda.device.detector.xspress.Xspress2Detector;
import gda.device.detector.xspress.xspress2data.Xspress2DAServerController;
import gda.device.memory.Scaler;
import gda.device.motor.DummyMotor;
import gda.device.timer.Etfg;
import gda.factory.Factory;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.jython.scriptcontroller.logging.LoggingScriptController;
import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.OutputParameters;
import uk.ac.gda.beans.exafs.QEXAFSParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.beans.exafs.b18.B18SampleParameters;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.Xspress3Parameters;
import uk.ac.gda.beans.xspress.ResGrades;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.server.exafs.epics.device.scannable.QexafsTestingScannable;
import uk.ac.gda.server.exafs.scan.iterators.SampleEnvironmentIterator;
import uk.ac.gda.server.exafs.scan.preparers.OutputPreparerBase;
import uk.ac.gda.util.beans.BeansFactory;
import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * Unit tests that try to setup suitable environment for running realistic scans.
 * The scans here are setup using xml files - i.e. the same as scans setup and run from the gda client,
 * or from Jython scripts using pre-prepared xml files..
 * @since 25/5/2017
 */
public class ScansSetupUsingXmlTest {
	private static final Logger logger = LoggerFactory.getLogger(ScansSetupUsingXmlTest.class);

	private final String testFileFolder = "testfiles/gda/scan-xml-files/";
	private String xspress2ConfigFilename = testFileFolder+"Xspress_Parameters.xml";
	private String xspress2DtcConfigFilename = testFileFolder+"Xspress_Deadtime_Parameters.xml";

	protected ISampleParameters sampleBean;
	protected IScanParameters scanBean;
	protected IDetectorParameters detectorBean;
	protected IOutputParameters outputBean;
	protected XspressParameters detectorConfigurationBean;

	private XasScanFactory xasScanFactory;

	private QexafsTestingScannable qexafsScannable;

	// Detector related objects
	private Xspress2Detector xspress2Detector;
	private DummyDAServer daserver;
	private Etfg tfg;
	private TfgScalerWithFrames counterTimer01;
	private TfgFFoverI0 ffi0;
	private Scaler memory;
	// Qexfas
	private BufferedScaler qexafs_counterTimer01;
	private Xspress2BufferedDetector qexafs_xspress;
	private QexafsFFoverIO qexafsFfI0;

	protected String testDir;

	private NXMetaDataProvider metashop;
	private String beforeScanEntryName = "before_scan";

	protected void setupForTest(Class<?> classType, String testName) throws Exception {
		/* String testFolder = */TestHelpers.setUpTest(classType, testName, true);
		LocalProperties.setScanSetsScanNumber(true);
		LocalProperties.set("gda.scan.sets.scannumber", "true");
		LocalProperties.set("gda.scanbase.firstScanNumber", "-1");
		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "XasNexusDataWriter");
		LocalProperties.set("gda.nexus.createSRS", "false");
		testDir = LocalProperties.getBaseDataDir();
		LocalProperties.set(LocalProperties.GDA_VISIT_DIR, testDir);

		// Findables the server needs to know about
		Findable[] findables = new Findable[] { xspress2Detector, counterTimer01, ffi0,
												qexafs_xspress, qexafs_counterTimer01, qexafsFfI0,
												qexafsScannable,
												metashop};

		final Factory factory = TestHelpers.createTestFactory();
		for(Findable f : findables) {
			factory.addFindable(f);
			InterfaceProvider.getJythonNamespace().placeInJythonNamespace(f.getName(), f);
		}

		// Need to add objectfactory to Finder if using Finder.getIntance().find(...) to get at scannables.
		Finder.addFactory(factory);
	}

	/**
	 * Setup DAServer and Tfg (used by scalers, XSpress2)
	 */
	public void setupDaserverTfg() {
		daserver = new DummyDAServer();
		daserver.setName("DummyDAServer");
		daserver.setXspressMode(DUMMY_XSPRESS2_MODE.XSPRESS2_FULL_MCA);
		daserver.connect();
		daserver.setNonRandomTestData(false);

		tfg = new Etfg();
		tfg.setName("tfg");
		tfg.configure();
	}

	/**
	 * Setup XSpress2 detector, qexafs buffered xspress2
	 * @throws FactoryException
	 * @throws DeviceException
	 */
	public void setupXspress2Detector() throws FactoryException, DeviceException {
		Xspress2DAServerController controller = new Xspress2DAServerController();
		controller.setDaServer(daserver);
		controller.setTfg(tfg);
		controller.setDaServer(daserver);
		controller.setMcaOpenCommand("xspress open-mca");
		controller.setScalerOpenCommand("xspress open-scalers");
		controller.setStartupScript("xspress2 format-run 'xsp1' res-none");
		controller.setXspressSystemName("xsp1");

		xspress2Detector = new Xspress2Detector();
		xspress2Detector.setName("xspress2Detector");
		xspress2Detector.setController(controller);
		xspress2Detector.setConfigFileName(xspress2ConfigFilename);
		xspress2Detector.setDtcConfigFileName(xspress2DtcConfigFilename);

		// xspress2Detector.setName("xspress2Detector");

		xspress2Detector.setFullMCABits(8);
		xspress2Detector.configure();

		qexafs_xspress = new Xspress2BufferedDetector();
		qexafs_xspress.setName("qexafs_xspress");
		qexafs_xspress.setXspress2system(xspress2Detector);
		qexafs_xspress.setDaserver(daserver);
		qexafs_xspress.setInputNames(new String[] {"qexafs_energy"});
	}

	private void setupAndConfigureScaler(TfgScalerWithLogValues tfgScalerWithFrames) throws FactoryException {
		tfgScalerWithFrames.setScaler(memory);
		tfgScalerWithFrames.setTimer(tfg);
		tfgScalerWithFrames.setTFGv2(true);
		tfgScalerWithFrames.setOutputLogValues(true);
		tfgScalerWithFrames.setTimeChannelRequired(true);
		tfgScalerWithFrames.setExtraNames(new String[] { "I0", "It", "Iref", "lnI0It", "lnItIref" });
		tfgScalerWithFrames.setFirstDataChannel(0);
		tfgScalerWithFrames.setNumChannelsToRead(3);
		tfgScalerWithFrames.setOutputFormat(new String[] { "%.6,5g", "%9d", "%9d", "%9d", "%.5g", "%.5g" });
		tfgScalerWithFrames.setDarkCurrentRequired(false); // set to false, or otherwise need to also setup shutter
		tfgScalerWithFrames.configure();
	}

	/**
	 * Setup scalers used for Xas, Qexafs scans
	 * @throws FactoryException
	 * @throws DeviceException
	 */
	public void setupScalers() throws FactoryException, DeviceException {
		memory = new Scaler();
		memory.setName("memory");
		memory.setDaServer(daserver);
		memory.setHeight(1);
		memory.setWidth(4);
		memory.setOpenCommand("tfg open-cc");
		memory.configure();

		counterTimer01 = new TfgScalerWithFrames();
		counterTimer01.setName("counterTimer01");
		setupAndConfigureScaler(counterTimer01);

		qexafs_counterTimer01 = new BufferedScaler();
		qexafs_counterTimer01.setName("qexafs_counterTimer01");
		qexafs_counterTimer01.setDaserver(daserver);
		setupAndConfigureScaler(qexafs_counterTimer01);

		ffi0 = new TfgFFoverI0();
		ffi0.setName("FFI0");
		ffi0.setXspress(xspress2Detector);
		ffi0.setCounterTimer(counterTimer01);
		ffi0.configure();

		qexafsFfI0 = new QexafsFFoverIO();
		qexafsFfI0.setName("qexafsFfI0");
		qexafsFfI0.setQexafsScaler(qexafs_counterTimer01);
		qexafsFfI0.setQexafsXspress(qexafs_xspress);
		qexafsFfI0.configure();
	}

	/**
	 * Make energy scannable that will be moved during scans
	 * @throws Exception
	 */
	protected void setupEnergyScannable() throws Exception {
		DummyMotor dummyMotor = new DummyMotor();
		dummyMotor.setName("dummyMotor");
		dummyMotor.setMinPosition(0);
		dummyMotor.setMaxPosition(100000);
		dummyMotor.setPosition(0);
		dummyMotor.setSpeed(1000000);
		dummyMotor.configure();

		qexafsScannable = new QexafsTestingScannable();
		qexafsScannable.setName("qexafsScannable");
		qexafsScannable.setMotor(dummyMotor);
		qexafsScannable.setLowerGdaLimits(dummyMotor.getMinPosition());
		qexafsScannable.setUpperGdaLimits(dummyMotor.getMaxPosition());
		qexafsScannable.setOutputFormat(new String[]{"%.4f"});
	}

	/**
	 * Simple detector preparer that returns list of (dummy) detectors to use for QExafs scans
	 */
	QexafsDetectorPreparer detectorPreparer = new QexafsDetectorPreparer() {

		private IDetectorParameters detectorBean;

		@Override
		public void configure(IScanParameters scanBean, IDetectorParameters detectorBean, IOutputParameters outputBean,
				String experimentFullPath) throws Exception {
			if (detectorConfigurationBean!=null) {
				xspress2Detector.applyConfigurationParameters(detectorConfigurationBean);
			} else {
				xspress2Detector.setConfigFileName(xspress2ConfigFilename);
				xspress2Detector.configure();
			}
			this.detectorBean = detectorBean;
		}

		@Override
		public Detector[] getExtraDetectors() {
			return null;
		}

		@Override
		public void completeCollection() {
		}

		@Override
		public void beforeEachRepetition() throws Exception {
		}

		@Override
		public BufferedDetector[] getQEXAFSDetectors() throws Exception {
			String expType = detectorBean.getExperimentType();
			if (expType.equals(DetectorParameters.FLUORESCENCE_TYPE)) {
				return new BufferedDetector[] {qexafs_counterTimer01, qexafs_xspress, qexafsFfI0 };
			} else {
				return new BufferedDetector[] {qexafs_counterTimer01 };
			}
		}
	};

	public class OutputPreparer extends OutputPreparerBase {
		public OutputPreparer(AsciiDataWriterConfiguration datawriterconfig, NXMetaDataProvider metashop) {
			super(datawriterconfig, metashop);
		}
	}

	/** Simple sample environment iterator to use in place of beamline specific one */
	SampleEnvironmentIterator sampleEnvironmentIterator = new SampleEnvironmentIterator() {

		@Override
		public void resetIterator() {
		}

		@Override
		public void next() throws DeviceException, InterruptedException {
		}

		@Override
		public int getNumberOfRepeats() {
			return 1;
		}

		@Override
		public String getNextSampleName() {
			return "Sample_name";
		}

		@Override
		public List<String> getNextSampleDescriptions() {
			return Arrays.asList("Sample description part 1", "Sample description part2");
		}
	};


	private void setupScanFactory() {

		BeamlinePreparer beamlinePreparer = mock(BeamlinePreparer.class);
		SampleEnvironmentPreparer samplePreparer = mock(SampleEnvironmentPreparer.class);
		LoggingScriptController loggingScriptController = mock(LoggingScriptController.class);

		// Mock the sample environment preparer method would normally return the sample environment iterator
		Mockito.when(samplePreparer.createIterator(DetectorParameters.FLUORESCENCE_TYPE)).thenReturn(sampleEnvironmentIterator);
		Mockito.when(samplePreparer.createIterator(DetectorParameters.TRANSMISSION_TYPE)).thenReturn(sampleEnvironmentIterator);


		AsciiDataWriterConfiguration datawriterconfig = new AsciiDataWriterConfiguration();
		metashop = new NXMetaDataProvider();
		metashop.setName("metashop");
		LocalProperties.set(NexusDataWriter.GDA_NEXUS_METADATAPROVIDER_NAME, metashop.getName());

		OutputPreparer outputPreparer = new OutputPreparer(datawriterconfig, metashop);

		xasScanFactory = new XasScanFactory();
		xasScanFactory.setBeamlinePreparer(beamlinePreparer);
		xasScanFactory.setDetectorPreparer(detectorPreparer);
		xasScanFactory.setSamplePreparer(samplePreparer);
		xasScanFactory.setOutputPreparer(outputPreparer);
		xasScanFactory.setLoggingScriptController(loggingScriptController);
		xasScanFactory.setMetashop(metashop);
		xasScanFactory.setIncludeSampleNameInNexusName(true);
		xasScanFactory.setEnergyScannable(qexafsScannable);
		xasScanFactory.setScanName("energyScan");

		xasScanFactory.setQexafsEnergyScannable(qexafsScannable);
		xasScanFactory.setQexafsDetectorPreparer(detectorPreparer);
	}

	/** Set BeansFactory class list - this is so that xml serialisation knows which classes to it can deal with... */
	private void setupBeansFactory() {
		Class<?>[] classes = new Class<?>[] { B18SampleParameters.class, QEXAFSParameters.class, XasScanParameters.class,
				DetectorParameters.class, OutputParameters.class, XspressParameters.class, Xspress3Parameters.class };
		BeansFactory.setClasses(classes);
	}

	@Before
	public void prepareScannables() throws Exception {
		setupDaserverTfg();
		setupXspress2Detector();
		setupScalers();
		setupEnergyScannable();
		setupScanFactory();
		setupBeansFactory();
	}

	@After
	public void tearDown() {
		// Remove factories from Finder so they do not affect other tests
		Finder.removeAllFactories();
	}

	/**
	 * Load dataset from nexus file; file should be closed initially (it will be opened to read the data, and closed afterwards).
	 * @param nexusFilename
	 * @param groupName
	 * @param dataName
	 * @return dataset
	 * @throws NexusException
	 * @throws DatasetException
	 */
	public IDataset getDataset(String nexusFilename, String groupName, String dataName) throws NexusException, DatasetException {
		NexusFile file = NexusFileHDF5.openNexusFileReadOnly(nexusFilename);
		try {
			GroupNode group = file.getGroup("/entry1/"+groupName, false);
			DataNode d = file.getData(group, dataName);
			return d.getDataset().getSlice(null, null, null);
		}catch(NexusException e){
			String msg = "Problem opening nexus data group "+groupName+" data "+dataName;
			throw new NexusException(msg+e);
		}finally {
			file.close();
		}
	}

	private Collection<String> getEntryNames(String nexusFilename, String groupName) throws NexusException {
		try(var file = NexusFileHDF5.openNexusFileReadOnly(nexusFilename)) {
			GroupNode g = file.getGroup("/entry1/"+groupName, false);
			return g.getDataNodeNames();
		}
	}

	private void checkDatasetShape(IDataset dataset, int[] expectedShape) {
		assertArrayEquals(expectedShape, dataset.getShape());
	}

	private void testNexusFileDetectorDataShape() throws NexusException, DeviceException, DatasetException, IOException {
		// Get basic information on scan just run (it would be nice to get this from EnergyScan object after running 'doCollection' but
		// it doesn't provide much useful information....)
		String nexusName = getCurrentScanFileName();
		int numPoints = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation().getNumberOfPoints();
		var detectors = InterfaceProvider.getScanDataPointProvider().getLastScanDataPoint().getDetectors();

		for(Detector det : detectors) {
			if (det instanceof Xspress2Detector || det instanceof Xspress2BufferedDetector) {
				checkXSpress2DataShape(nexusName, det.getName(), numPoints);
			} else if (det instanceof BufferedDetector || det instanceof TfgScaler || det instanceof TfgFFoverI0) {
				checkDetectorDataShape(nexusName, det, numPoints);
			}
		}

		testParameterMetadata(nexusName);
	}

	private String getCurrentScanFileName() {
		return InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation().getFilename();
	}

	/**
	 * Check shape of detector data (may have several datasets, each one is 1-dimensional)
	 * @param nexusFilename
	 * @param bufDetector
	 * @param numPoints
	 * @throws NexusException
	 * @throws DatasetException
	 */
	private void checkDetectorDataShape(String nexusFilename, Detector bufDetector, int numPoints) throws NexusException, DatasetException {
		String groupName = bufDetector.getName();
		for(String datasetName : bufDetector.getExtraNames()) {
			checkDatasetShape(getDataset(nexusFilename, groupName, datasetName), new int[]{numPoints});
		}
	}

	/**
	 * @param detectorElements
	 * @return Map from ROI name to number of detector elements with that ROI name
	 */
	private Map<String, Integer> getPeakMap(List<DetectorElement> detectorElements) {
		Map<String, Integer> peakMap = new HashMap<String, Integer>(); // peakname, number of elements
		for(DetectorElement element : detectorElements) {
			for(DetectorROI region : element.getRegionList()) {
				String roiName = region.getRoiName();
				if( !peakMap.containsKey(roiName) ) {
					peakMap.put(roiName, 1);
				} else {
					// increment count for this peak name
					peakMap.put(roiName, peakMap.get(roiName)+1);
				}
			}
		}
		return peakMap;
	}

	private void checkRoiDatashapes(String nexusFilename, String groupName, int numPoints) throws NexusException, DatasetException{
		Map<String,Integer> peakMap = getPeakMap(xspress2Detector.getDetectorList());
		String resGrade = xspress2Detector.getResGrade();
		for(String roiName : peakMap.keySet()) {
			int numElementsWithRoi = peakMap.get(roiName);
			if (resGrade.startsWith(ResGrades.ALLGRADES)) {
				checkDatasetShape( getDataset(nexusFilename, groupName, roiName), new int[]{numPoints, numElementsWithRoi, 16} );
			} else {
				int[] expectedShape = new int[]{numPoints};
				if (numElementsWithRoi>1) {
					expectedShape = ArrayUtils.add(expectedShape, numElementsWithRoi);
				}
				if (resGrade.startsWith(ResGrades.NONE)) {
					checkDatasetShape(getDataset(nexusFilename, groupName, roiName), expectedShape);
				} else if (resGrade.startsWith(ResGrades.THRESHOLD)) {
					checkDatasetShape(getDataset(nexusFilename, groupName, roiName), expectedShape);
					checkDatasetShape(getDataset(nexusFilename, groupName, roiName + "_bad"), expectedShape);
				}
			}
		}
	}

	private void checkXSpress2DataShape(String nexusFilename, String groupName, int numPoints) throws NexusException, DeviceException, DatasetException {
		int numElements = xspress2Detector.getNumberOfElements();
		int numChannels = xspress2Detector.getCurrentSettings().getFullMCASize();
		String readoutMode = xspress2Detector.getReadoutMode();
		checkDatasetShape(getDataset(nexusFilename, groupName, "FF"), new int[]{numPoints});
		checkDatasetShape(getDataset(nexusFilename, groupName, "raw scaler in-window"), new int[]{numPoints, numElements});
		checkDatasetShape(getDataset(nexusFilename, groupName, "raw scaler total"), new int[]{numPoints, numElements});
		if (readoutMode.equals(XspressParameters.READOUT_MODE_REGIONSOFINTEREST)) {
			checkRoiDatashapes(nexusFilename, groupName, numPoints);
		} else {
			checkDatasetShape(getDataset(nexusFilename, groupName, "scalers"), new int[]{numPoints, numElements});
		}

		if (readoutMode.equals(XspressParameters.READOUT_MODE_SCALERS_AND_MCA)) {
			checkDatasetShape(getDataset(nexusFilename, groupName, "MCAs"), new int[]{numPoints, numElements, numChannels});
		}
		checkDatasetShape(getDataset(nexusFilename, groupName, "tfg clock cycles"), new int[]{numPoints, numElements});
		checkDatasetShape(getDataset(nexusFilename, groupName, "tfg resets"), new int[]{numPoints, numElements});
	}

	private void testRepetitionsMetadata(String filename, String repetitionEntryName, int numRepetitions) throws NexusException, DatasetException {
		logger.info("Checking repetition metadata entries in {}", beforeScanEntryName);

		Collection<String> beforeScanEntries = getEntryNames(filename, beforeScanEntryName);

		// Check the names of the nexus files run as part of the sequence of repetitions is correct
		// e.g. 3.nxs should have 1.nxs, 2.nxs as repetition filenames.
		logger.info("Checking file names in {}", repetitionEntryName);
		assertTrue(beforeScanEntryName+" does not contain repetition entry "+repetitionEntryName, beforeScanEntries.contains(repetitionEntryName));
		IDataset repetitionDset = getDataset(filename, beforeScanEntryName, repetitionEntryName);
		String[] repetitionFiles = DatasetUtils.cast(StringDataset.class, repetitionDset).get().split("\\s+");

		// There should be numRepetitions-1 filenames
		assertEquals(numRepetitions-1, repetitionFiles.length);
		for(int i=0; i<repetitionFiles.length; i++) {
			String expectedName = filename.replace(numRepetitions+".nxs", (i+1)+".nxs");
			assertEquals(expectedName, repetitionFiles[i]);
			logger.info("Filename {} is ok", repetitionFiles[i]);
		}
	}

	private void testParameterMetadata(String filename) throws NexusException, DatasetException, IOException {
		logger.info("Checking parameter Metadata entries in {}", beforeScanEntryName);

		// Check the XML parameter files in before_scan metadata match the original files.
		for(String entryName : getEntryNames(filename, beforeScanEntryName)) {
			if (entryName.endsWith(".xml")) {
				IDataset paramDataset = getDataset(filename, beforeScanEntryName, entryName);
				String paramString = DatasetUtils.cast(StringDataset.class, paramDataset).get();
				String paramStringFromFile = FileUtils.readFileToString(Paths.get(testFileFolder).resolve(entryName).toFile(), Charset.defaultCharset());
				assertEquals(paramStringFromFile, paramString);
				logger.info("{} is ok", entryName);
			}
		}
	}

	/**
	 * Load settings from XML file and create new bean objects (used to configure the scans).
	 * @param sampleFileName
	 * @param scanFileName
	 * @param detectorFileName
	 * @param outputFileName
	 * @throws Exception
	 */
	private void loadBeans(String sampleFileName, String scanFileName, String detectorFileName, String outputFileName) throws Exception {
		sampleBean = (ISampleParameters) XMLHelpers.getBeanObject(testFileFolder, sampleFileName);
		scanBean = (IScanParameters) XMLHelpers.getBeanObject(testFileFolder, scanFileName);
		detectorBean = (IDetectorParameters) XMLHelpers.getBeanObject(testFileFolder, detectorFileName);
		outputBean = (IOutputParameters) XMLHelpers.getBeanObject(testFileFolder, outputFileName);
		detectorConfigurationBean = (XspressParameters) XMLHelpers.getBeanObject(null, xspress2ConfigFilename);
	}

	@Test
	public void testXasScan() throws Exception {
		setupForTest(ScansSetupUsingXmlTest.class, "testXasScan");
		EnergyScan energyScan = xasScanFactory.createEnergyScan();
		energyScan.configureCollection("Sample_Parameters.xml", "XAS_Parameters.xml", "Detector_Parameters.xml", "Output_Parameters.xml", testFileFolder, 1);
		energyScan.doCollection();
		testNexusFileDetectorDataShape();
	}

	@Test
	public void testXasScanFromBeans() throws Exception {
		setupForTest(ScansSetupUsingXmlTest.class, "testXasScanFromBeans");
		loadBeans("Sample_Parameters.xml", "XAS_Parameters.xml", "Detector_Parameters.xml", "Output_Parameters.xml");
		EnergyScan energyScan = xasScanFactory.createEnergyScan();
		energyScan.configureCollection(sampleBean, scanBean, detectorBean, outputBean, detectorConfigurationBean, testFileFolder, 1);
		energyScan.doCollection();
		testNexusFileDetectorDataShape();
	}

	@Test
	public void testQExafsScan() throws Exception {
		setupForTest(ScansSetupUsingXmlTest.class, "testQExafsScan");
		EnergyScan energyScan = xasScanFactory.createQexafsScan();
		energyScan.configureCollection("Sample_Parameters.xml", "QEXAFS_Parameters.xml", "Detector_Parameters.xml", "Output_Parameters.xml", testFileFolder, 1);
		energyScan.doCollection();
		testNexusFileDetectorDataShape();
	}

	@Test
	public void testQExafsScanFromBeans() throws Exception {
		setupForTest(ScansSetupUsingXmlTest.class, "testQExafsScanFromBeans");
		loadBeans("Sample_Parameters.xml", "QEXAFS_Parameters.xml", "Detector_Parameters.xml", "Output_Parameters.xml");
		EnergyScan energyScan = xasScanFactory.createQexafsScan();
		energyScan.configureCollection(sampleBean, scanBean, detectorBean, outputBean, detectorConfigurationBean, testFileFolder, 1);
		energyScan.doCollection();
		testNexusFileDetectorDataShape();
	}

	@Test
	public void testQExafsScanFromBeansXspress2TreeWriter() throws Exception {
		setupForTest(ScansSetupUsingXmlTest.class, "testQExafsScanFromBeansXspress2TreeWriter");
		qexafs_xspress.setUseNexusTreeWriter(true);
		qexafs_xspress.setDetectorNexusFilename(Paths.get(testDir, "xspress2.nxs").toAbsolutePath().toString());
		loadBeans("Sample_Parameters.xml", "QEXAFS_Parameters.xml", "Detector_Parameters.xml", "Output_Parameters.xml");
		EnergyScan energyScan = xasScanFactory.createQexafsScan();
		energyScan.configureCollection(sampleBean, scanBean, detectorBean, outputBean, detectorConfigurationBean, testFileFolder, 1);
		energyScan.doCollection();
		testNexusFileDetectorDataShape();
	}

	@Test
	public void testQExafsScanRepetitions() throws Exception {
		setupForTest(ScansSetupUsingXmlTest.class, "testQExafsScanRepetitions");
		EnergyScan energyScan = xasScanFactory.createQexafsScan();
		int numRepetitions = 5;
		energyScan.configureCollection("Sample_Parameters.xml", "QEXAFS_Parameters.xml", "Detector_Parameters.xml", "Output_Parameters.xml", testFileFolder, numRepetitions);
		energyScan.doCollection();
		testNexusFileDetectorDataShape();
		testRepetitionsMetadata(getCurrentScanFileName(), energyScan.getFilesInRepetitionEntry(), 5);
	}
}
