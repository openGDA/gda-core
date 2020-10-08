/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.data.scan.datawriter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.january.dataset.PositionIterator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Streams;

import gda.TestHelpers;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.StoredMetadataEntry;
import gda.device.Detector;
import gda.device.Monitor;
import gda.device.Scannable;
import gda.device.detector.countertimer.DummyCounterTimer;
import gda.device.monitor.DummyMonitor;
import gda.device.scannable.DummyScannable;
import gda.jython.InterfaceProvider;
import gda.scan.ConcurrentScan;
import gda.scan.IScanDataPoint;

public abstract class AbstractNexusDataWriterScanTest {

	public enum DetectorType {
		NONE, NEXUS_DEVICE, COUNTER_TIMER;
	}

	protected static final String METADATA_KEY_FEDERAL_ID = "federalid";

	protected static final int MAX_SCAN_RANK = 3; // larger scans take too long


	protected static final int[] EMPTY_SHAPE = new int[0];
	protected static final int[] SINGLE_VALUE_SHAPE = new int[] { 1 };

	protected static final int[] GRID_SHAPE = { 8, 5 };
	private static final int DEFAULT_NUM_AXIS_POINTS = 2;

	protected static final String ATTRIBUTE_NAME_LOCAL_NAME = "local_name";
	protected static final String ATTRIBUTE_NAME_TARGET = "target";

	protected static final String INSTRUMENT_NAME = "instrument";
	protected static final String SCANNABLE_NAME_PREFIX = "scannable";
	protected static final String MONITOR_NAME = "mon01";

	protected static final int EXPECTED_SCAN_NUMBER = 1;
	protected static final String EXPECTED_ENTRY_IDENTIFER = "1";
	protected static final String EXPECTED_PROGRAM_NAME = "GDA 7.11.0";
	protected static final String EXPECTED_USER_GROUP_NAME = "user01";
	protected static final String EXPECTED_INSTRUMENT_NAME = "base";
	protected static final String EXPECTED_USER_NAME = "abc12345";

	protected static final double START_VALUE = 0.0;
	protected static final double STEP_SIZE = 1.0;
	protected static final double SCANNABLE_LOWER_BOUND = -123.456;
	protected static final double SCANNABLE_UPPER_BOUND = 987.654;

	protected static final double MONITOR_VALUE = 2.5;

	private static INexusFileFactory nexusFileFactory;

	private String testDir;

	protected final int scanRank;
	protected final int[] scanDimensions;
	protected Scannable[] scannables;
	protected Monitor monitor;

	protected Detector detector;

	protected DetectorType detectorType; // the type of detector we're testing, in terms of nexus writing, e.g. counter timer

	private Object[] scanArguments;

	public AbstractNexusDataWriterScanTest(int scanRank) {
		this.scanRank = scanRank;
		final int[] scanDimensions = new int[scanRank];
		// dimensions 0 and 1 use GRID_SHAPE, any remaining use DEFAULT_NUM_AXIS_POINTS to keep the
		// scan size small for higher dimension scans
		for (int i = 0; i < scanRank; i++) {
			scanDimensions[i] = i < GRID_SHAPE.length ? GRID_SHAPE[i] : DEFAULT_NUM_AXIS_POINTS;
		}
		this.scanDimensions = scanDimensions;
	}

	public static void setUpServices() {
		// must be called from @BeforeClass method of subclasses
		nexusFileFactory = new NexusFileFactoryHDF5();

		GDAMetadataProvider.getInstance().addMetadataEntry(new StoredMetadataEntry(
				METADATA_KEY_FEDERAL_ID, EXPECTED_USER_NAME));
	}

	@AfterClass
	public static void tearDownServices() {
		GDAMetadataProvider.setInstanceForTesting(null);
	}

	@Before
	public void setUp() throws Exception {
		// setup devices
		this.scannables = new Scannable[scanRank];
		for (int i = 0; i < scanRank; i++) {
			final DummyScannable dummyScannable = new DummyScannable();
			dummyScannable.setName(SCANNABLE_NAME_PREFIX + i);
			dummyScannable.setLowerGdaLimits(SCANNABLE_LOWER_BOUND);
			dummyScannable.setUpperGdaLimits(SCANNABLE_UPPER_BOUND);
			this.scannables[i] = dummyScannable;
		}

		final DummyMonitor dummyMonitor = new DummyMonitor();
		dummyMonitor.setConstantValue(MONITOR_VALUE);
		dummyMonitor.setName(MONITOR_NAME);
		this.monitor = dummyMonitor;
	}

	@After
	public void tearDown() {
		scannables = null;
		monitor = null;
	}

	protected void setUpTest() throws Exception {
		testDir = TestHelpers.setUpTest(this.getClass(), "concurrentScan", true);
	}

	@Test
	public void concurrentScanNoDetector() throws Exception {
		concurrentScan(null, DetectorType.NONE);
	}

	@Test
	public void concurrentScanCounterTimer() throws Exception {
		final DummyCounterTimer detector = new DummyCounterTimer();
		detector.setName("counterTimer");
		detector.setDataDecimalPlaces(3);
		detector.setUseGaussian(true);
		detector.setInputNames(new String[0]);
		final String[] names = new String[] { "one", "two", "three", "four" };
		detector.setExtraNames(names);
		detector.setTotalChans(names.length);
		detector.configure();
		detector.setCollectionTime(10.0);

		concurrentScan(detector, DetectorType.COUNTER_TIMER);
	}

	protected void concurrentScan(Detector detector, DetectorType detectorType) throws Exception {
		this.detector = detector;
		this.detectorType = detectorType;

		setUpTest(); // create test dir and initialize properties

		// create the scan
		scanArguments = createScanArguments(detector);
		final ConcurrentScan scan = new ConcurrentScan(scanArguments);
		assertEquals(-1, scan.getScanNumber());

		// run the scan
		scan.runScan();

		final File expectedNexusFile = new File(testDir + "/Data/1.nxs");
		final String expectedNexusFilePath = expectedNexusFile.getAbsolutePath();

		assertThat(expectedNexusFile.exists(), is(true));
		assertThat(scan.getScanNumber(), is(EXPECTED_SCAN_NUMBER));
		final IScanDataPoint lastPoint = InterfaceProvider.getScanDataPointProvider().getLastScanDataPoint();
		assertThat(lastPoint.getScanIdentifier(), is(EXPECTED_SCAN_NUMBER));

		assertThat(lastPoint.getCurrentFilename(), is(equalTo(expectedNexusFilePath)));
		assertThat(scan.getDataWriter().getCurrentFileName(), is(equalTo(expectedNexusFilePath)));

		try (final NexusFile nexusFile = openNexusFile(expectedNexusFilePath)) {
			checkNexusFile(nexusFile);
		}
	}

	private Object[] createScanArguments(Detector detector) {
		final List<Object> scanArgs = new ArrayList<>();

		// add scannables
		for (int i = 0; i < scanRank; i++) {
			scanArgs.add(scannables[i]);
			scanArgs.add(START_VALUE);
			scanArgs.add(getStopValue(i));
			scanArgs.add(STEP_SIZE);
		}

		// add monitor
		scanArgs.add(monitor);

		// add detector if present
		if (detector != null) {
			scanArgs.add(detector);
		}

		return scanArgs.toArray();
	}

	private double getStopValue(int i) {
		return START_VALUE + STEP_SIZE * (scanDimensions[i] - 1);
	}

	protected String getExpectedScanCommand() {
		return "scan " + String.join(" ", Arrays.stream(scanArguments)
			.map(arg -> arg instanceof Scannable ? ((Scannable) arg).getName() : arg.toString())
			.toArray(String[]::new));
	}

	protected String[] getScannableAndMonitorNames() {
		return Streams.concat(Arrays.stream(scannables), Stream.of(monitor))
			.map(Scannable::getName)
			.toArray(String[]::new);
	}

	protected int getNumDevices() {
		return scanRank + (monitor != null ? 1 : 0) + (detector != null ? 1 : 0);
	}

	private NexusFile openNexusFile(String filePath) throws NexusException {
		final NexusFile nexusFile = nexusFileFactory.newNexusFile(filePath);
		nexusFile.openToRead();
		return nexusFile;
	}

	private void checkNexusFile(NexusFile nexusFile) throws Exception {
		final TreeFile nexusTree = NexusUtils.loadNexusTree(nexusFile);
		final NXroot nexusRoot = (NXroot) nexusTree.getGroupNode();
		assertThat(nexusRoot, is(notNullValue()));
		final NXentry entry = nexusRoot.getEntry(getEntryName());
		assertThat(entry, is (notNullValue()));

		checkNexusMetadata(entry);
		checkDevices(entry);
		checkDataGroups(entry);
	}

	protected void checkDevices(NXentry entry) throws Exception {
		final NXinstrument instrument = entry.getInstrument();
		assertThat(instrument, is(notNullValue()));

		checkInstrumentGroupMetadata(instrument); // check no of nodes, etc
		checkScannablesAndMonitors(instrument);
		checkDetector(instrument);
	}

	private void checkScannablesAndMonitors(final NXinstrument instrument) throws Exception {
		final Map<String, NXpositioner> positioners = instrument.getAllPositioner();
		assertThat(positioners.size(), is(scannables.length + 1));
		assertThat(positioners.keySet(), containsInAnyOrder(getScannableAndMonitorNames()));

		// check the scannables have been written correctly
		for (int i = 0; i < scanRank; i++) {
			final String scannableName = scannables[i].getName();
			final NXpositioner scannablePos = positioners.get(scannableName);
			assertThat(scannablePos, is(notNullValue()));

			checkScannablePositioner(scannablePos, i);
		}

		checkMonitor(instrument);
	}

	private void checkDetector(NXinstrument instrument) throws Exception {
		final Set<String> detectorGroupNames = instrument.getAllDetector().keySet();

		if (detector == null) {
			assertThat(detectorGroupNames, is(empty()));
			return; // no detector in this scan, so no NXdetector group to check
		}

		assertThat(detectorGroupNames, is(not(empty())));
		assertThat(detectorGroupNames, contains(detector.getName()));

		// check that the NXdetector group for the detector is as expected
		final NXdetector detectorGroup = instrument.getDetector(detector.getName());
		assertThat(detectorGroup, is(notNullValue()));

		switch (detectorType) {
			case NONE:
				break; // detector == null, so this case is not reached
			case NEXUS_DEVICE:
				checkNexusDeviceDetector(detectorGroup);
				break;
			case COUNTER_TIMER:
				checkCounterTimerDetector(detectorGroup);
				break;
			default:
				throw new IllegalArgumentException("Unknown detector type: " + detectorType);
		}
	}

	private void checkNexusDeviceDetector(final NXdetector detGroup) throws DatasetException {
		final DataNode dataNode = detGroup.getDataNode(NXdata.NX_DATA);
		assertThat(dataNode, is(notNullValue()));

		final ILazyDataset dataset = dataNode.getDataset();
		final int[] shape = dataset.getShape();
		assertThat(shape.length, is(scanRank + 2));
		assertThat(Arrays.copyOfRange(shape, 0, scanRank), is(equalTo(scanDimensions)));
		checkDatasetWritten(dataset);
	}

	private void checkCounterTimerDetector(NXdetector detGroup) throws DatasetException {
		final String[] extraNames = detector.getExtraNames();
		assertThat(detGroup.getNumberOfDataNodes(), is(extraNames.length + 3));

		assertThat(detGroup.getDescriptionScalar(), is(equalTo("Dummy Counter Timer")));
		assertThat(detGroup.getTypeScalar(), is(equalTo("DUMMY")));
		assertThat(detGroup.getDataNode("id").getString(), is(equalTo("dumdum-2")));

		for (String name : detector.getExtraNames()) {
			final DataNode dataNode = detGroup.getDataNode(name);
			assertThat(dataNode, is(notNullValue()));
			checkDatasetWritten(dataNode.getDataset());
		}
	}

	private void checkMonitor(NXinstrument instrument) throws Exception {
		// check the monitor has been written correctly
		final NXpositioner monitorPos = instrument.getPositioner(monitor.getName());
		assertThat(monitorPos, is(notNullValue()));
		checkMonitorPositioner(monitorPos);
	}

	protected abstract void checkScannablePositioner(NXpositioner positioner, int scanIndex) throws Exception;

	protected abstract void checkMonitorPositioner(NXpositioner positioner) throws Exception;

	protected abstract void checkInstrumentGroupMetadata(NXinstrument instrument);

	protected abstract void checkDataGroups(NXentry entry) throws Exception;

	protected void checkNexusMetadata(NXentry entry) {
		// start_time
		checkDateTime(entry.getStart_time());
		// end_time
		checkDateTime(entry.getEnd_time());

		checkUsers(entry);
	}

	protected abstract void checkUsers(NXentry entry);

	private void checkDateTime(IDataset dataset) {
		assertThat(dataset, is(notNullValue()));
		assertThat(dataset.getRank(), either(is(0)).or(is(1)));
		assertThat(dataset.getElementClass(), is(equalTo(String.class)));
		final String dateTimeString;
		if (dataset.getRank() == 0) {
			assertThat(dataset.getShape(), is(equalTo(EMPTY_SHAPE)));
			dateTimeString = dataset.getString();
		} else {
			assertThat(dataset.getShape(), is(equalTo(SINGLE_VALUE_SHAPE)));
			dateTimeString = dataset.getString(0);
		}
		final LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
		assertThat(dateTime, is(lessThan(LocalDateTime.now())));
		assertThat(dateTime, is(greaterThan(LocalDateTime.now().minus(5, ChronoUnit.MINUTES))));
	}

	protected void checkDatasetWritten(ILazyDataset dataset) throws DatasetException {
		checkDatasetWritten(dataset.getSlice());
	}

	protected void checkDatasetWritten(IDataset dataset) {
		// Make sure none of the numbers are NaNs. The detector
		// is expected to fill this scan with non-nulls.
		final PositionIterator posIter = new PositionIterator(dataset.getShape());
		while (posIter.hasNext()) {
			int[] pos = posIter.getPos();
			assertThat(Double.isNaN(dataset.getDouble(pos)), is(false));
		}
	}

	protected abstract String getEntryName();

	protected Dataset getExpectedScannableDataset(int i) {
		final DoubleDataset dataset = DatasetFactory.zeros(scanDimensions);
		final Dataset scannableValues = DatasetFactory.createRange(START_VALUE, getStopValue(i) + 1.0, STEP_SIZE);
		final IndexIterator iter = dataset.getIterator(true);
		while (iter.hasNext()) {
			final int[] scanPosition = iter.getPos();
			final int scannableIndex = scanPosition[i]; // the index of the scannable value in its range
			final double expectedPos = scannableValues.getDouble(scannableIndex);
			dataset.setItem(expectedPos, scanPosition);
		}

		return dataset;
	}

}
