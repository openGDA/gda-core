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
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NXsource;
import org.eclipse.dawnsci.nexus.NXuser;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import gda.TestHelpers;
import gda.device.Monitor;
import gda.device.Scannable;
import gda.device.monitor.DummyMonitor;
import gda.device.scannable.DummyScannable;
import gda.jython.InterfaceProvider;
import gda.scan.ConcurrentScan;
import gda.scan.IScanDataPoint;

public class NexusDataWriterScanTest {

	private static final int[] EMPTY_SHAPE = new int[0];
	private static final int[] SINGLE_VALUE_SHAPE = new int[] { 1 };

	private static final String DATASET_NAME_SCAN_COMMAND = "scan_command";
	private static final String DATASET_NAME_SCAN_DIMENSIONS = "scan_dimensions";
	private static final String DATA_GROUP_NAME = "default";

	private static final String ATTRIBUTE_NAME_AXIS = "axis";
	private static final String ATTRIBUTE_NAME_LABEL = "label";
	private static final String ATTRIBUTE_NAME_LOCAL_NAME = "local_name";
	private static final String ATTRIBUTE_NAME_PRIMARY = "primary";
	private static final String ATTRIBUTE_NAME_TARGET = "target";

	private static final String ENTRY_NAME = "entry1";
	private static final String INSTRUMENT_NAME = "instrument";
	private static final String SCANNABLE_NAME = "theta";
	private static final String MONITOR_NAME = "mon01";

	private static final int NUM_SCANNABLE_VALUE_ATTRIBUTES = 5;
	private static final int NUM_MONITOR_VALUE_ATTRIBUTES = 3;

	private static final int EXPECTED_SCAN_NUMBER = 1;
	private static final String EXPECTED_ENTRY_IDENTIFER = "1";
	private static final String EXPECTED_PROGRAM_NAME = "GDA 7.11.0";
	private static final String EXPECTED_SCAN_COMMAND = "scan " + SCANNABLE_NAME + " 0 10 1 " + MONITOR_NAME;
	private static final String EXPECTED_USER_NAME = "user01";
	private static final String EXPECTED_INSTRUMENT_NAME = "base";

	private static final double START_VALUE = 0.0;
	private static final double STOP_VALUE = 10.0;
	private static final double STEP_SIZE = 1.0;
	private static final int SCAN_SIZE = 11;
	private static final double SCANNABLE_LOWER_BOUND = -123.456;
	private static final double SCANNABLE_UPPER_BOUND = 987.654;

	private static final IDataset EXPECTED_SCAN_DIMENSIONS = DatasetFactory.createFromObject(new int[] { SCAN_SIZE });
	private static final IDataset EXPECTED_SCANNABLE_VALUES = DatasetFactory.createRange(START_VALUE, STOP_VALUE + 1.0, STEP_SIZE); // need to add 1 to stop to get correct dataset
	private static final double MONITOR_VALUE = 2.5;
	private static final IDataset EXPECTED_MONITOR_VALUES = DatasetFactory.createFromList(Collections.nCopies(SCAN_SIZE, MONITOR_VALUE));

	private static INexusFileFactory nexusFileFactory;

	private Scannable scannable;
	private Monitor monitor;

	@BeforeClass
	public static void setUpService() {
		nexusFileFactory = new NexusFileFactoryHDF5();
	}

	@Before
	public void setUp() throws Exception {
		// setup devices
		final DummyScannable dummyScannable = new DummyScannable();
		dummyScannable.setName(SCANNABLE_NAME);
		dummyScannable.setLowerGdaLimits(SCANNABLE_LOWER_BOUND);
		dummyScannable.setUpperGdaLimits(SCANNABLE_UPPER_BOUND);
		this.scannable = dummyScannable;

		final DummyMonitor dummyMonitor = new DummyMonitor();
		dummyMonitor.setConstantValue(MONITOR_VALUE);
		dummyMonitor.setName(MONITOR_NAME);
		this.monitor = dummyMonitor;
	}

	@After
	public void tearDown() {
		scannable = null;
		monitor = null;
	}

	@Test
	public void concurrentScan() throws Exception {
		final String testDir = TestHelpers.setUpTest(this.getClass(), "concurrentScan", true);

		// create the scan
		final Object[] scanArgs = new Object[] { scannable, 0, 10, 1, monitor };
		final ConcurrentScan scan = new ConcurrentScan(scanArgs);
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

	private NexusFile openNexusFile(String filePath) throws NexusException {
		final NexusFile nexusFile = nexusFileFactory.newNexusFile(filePath);
		nexusFile.openToRead();
		return nexusFile;
	}


	private void checkNexusFile(NexusFile nexusFile) throws Exception {
		final TreeFile nexusTree = NexusUtils.loadNexusTree(nexusFile);
		final NXroot nexusRoot = (NXroot) nexusTree.getGroupNode();
		assertThat(nexusRoot, is(notNullValue()));
		final NXentry entry = nexusRoot.getEntry(ENTRY_NAME);
		assertThat(entry, is (notNullValue()));

		checkNexusMetadata(entry);
		checkDevices(entry);
		checkDataGroup(entry);
	}

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

	private void checkNexusMetadata(NXentry entry) {
		// start_time
		checkDateTime(entry.getStart_time());
		// end_time
		checkDateTime(entry.getEnd_time());
		// entry_identifier
		assertThat(entry.getEntry_identifierScalar(), is(equalTo(EXPECTED_ENTRY_IDENTIFER)));
		// program_name
		assertThat(entry.getProgram_nameScalar(), is(equalTo(EXPECTED_PROGRAM_NAME)));
		// scan_command
		assertThat(entry.getDataset(DATASET_NAME_SCAN_COMMAND).getString(), is(equalTo(EXPECTED_SCAN_COMMAND)));
		// scan_dimensions
		assertThat(entry.getDataset(DATASET_NAME_SCAN_DIMENSIONS), is(equalTo(EXPECTED_SCAN_DIMENSIONS)));
		// title
		assertThat(entry.getTitleScalar(), is(equalTo(EXPECTED_SCAN_COMMAND))); // title seems to be same as scan command(!)

		// user group
		final Map<String, NXuser> users = entry.getAllUser();
		assertThat(users.keySet(), Matchers.contains(EXPECTED_USER_NAME));
		final NXuser user = users.get(EXPECTED_USER_NAME);
		assertThat(user, is(notNullValue()));
		assertThat(user.getNumberOfNodelinks(), is(0));  // note that the created NXuser group is empty
	}

	private void checkDevices(NXentry entry) throws Exception {
		final NXinstrument instrument = entry.getInstrument();
		assertThat(instrument, is(notNullValue()));

		assertThat(instrument.getNumberOfDataNodes(), is(1));
		assertThat(instrument.getNameScalar(), is(equalTo(EXPECTED_INSTRUMENT_NAME)));

		assertThat(instrument.getNumberOfGroupNodes(), is(3)); // scannable, monitor and source

		checkSource(instrument);

		final Map<String, NXpositioner> positioners = instrument.getAllPositioner();
		assertThat(positioners.keySet(), containsInAnyOrder(SCANNABLE_NAME, MONITOR_NAME));

		final NXpositioner scannablePos = positioners.get(SCANNABLE_NAME);
		assertThat(scannablePos.getNumberOfDataNodes(), is(3));
		assertThat(scannablePos.getDataNodeMap().keySet(), containsInAnyOrder(
				SCANNABLE_NAME, NXpositioner.NX_SOFT_LIMIT_MAX, NXpositioner.NX_SOFT_LIMIT_MIN));
		final DataNode scannableValueDataNode = scannablePos.getDataNode(SCANNABLE_NAME);
		assertThat(scannableValueDataNode, is(notNullValue()));
		assertThat(scannableValueDataNode.getNumberOfAttributes(), is(NUM_SCANNABLE_VALUE_ATTRIBUTES));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_AXIS).getFirstElement(), is("1"));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_LABEL).getFirstElement(), is("1"));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_LOCAL_NAME).getFirstElement(),
				is(equalTo(SCANNABLE_NAME + "." + SCANNABLE_NAME)));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_PRIMARY).getFirstElement(), is("1"));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_TARGET).getFirstElement(),
				is(equalTo("/" + ENTRY_NAME + "/" + INSTRUMENT_NAME + "/" + SCANNABLE_NAME + "/" + SCANNABLE_NAME)));
		assertThat(scannableValueDataNode.getDataset().getSlice(),
				is(equalTo(DatasetFactory.createFromObject(EXPECTED_SCANNABLE_VALUES)))); // check values

		assertThat(scannablePos.getSoft_limit_minScalar(), is(equalTo(SCANNABLE_LOWER_BOUND)));
		assertThat(scannablePos.getSoft_limit_maxScalar(), is(equalTo(SCANNABLE_UPPER_BOUND)));

		final NXpositioner monitorPos = positioners.get(MONITOR_NAME);
		assertThat(monitorPos.getNumberOfDataNodes(), is(1));
		final DataNode monitorValueDataNode = monitorPos.getDataNode(NXpositioner.NX_VALUE);
		assertThat(monitorValueDataNode, is(notNullValue()));
		assertThat(monitorValueDataNode.getNumberOfAttributes(), is(NUM_MONITOR_VALUE_ATTRIBUTES));
		assertThat(monitorValueDataNode.getAttribute(ATTRIBUTE_NAME_AXIS).getFirstElement(), is("1"));
		assertThat(monitorValueDataNode.getAttribute(ATTRIBUTE_NAME_LOCAL_NAME).getFirstElement(),
				is(equalTo(MONITOR_NAME + "." + NXpositioner.NX_VALUE)));
		assertThat(monitorValueDataNode.getAttribute(ATTRIBUTE_NAME_TARGET).getFirstElement(),
				is(equalTo("/" + ENTRY_NAME + "/instrument/" + MONITOR_NAME + "/" + NXpositioner.NX_VALUE)));
		assertThat(monitorValueDataNode.getDataset().getSlice(), is(equalTo(EXPECTED_MONITOR_VALUES))); // check values
	}

	private void checkSource(NXinstrument instrument) {
		final NXsource source = instrument.getSource();
		assertThat(source, is(notNullValue()));

		assertThat(source.getNumberOfDataNodes(), is(3));
		assertThat(source.getNumberOfGroupNodes(), is(0));

		assertThat(source.getNameScalar(), is(equalTo("DLS")));
		assertThat(source.getProbeScalar(), is(equalTo("x-ray")));
		assertThat(source.getTypeScalar(), is(equalTo("Synchrotron X-ray Source")));
	}

	private void checkDataGroup(NXentry entry) {
		final Map<String, NXdata> dataGroups = entry.getAllData();
		assertThat(dataGroups.keySet(), contains(DATA_GROUP_NAME));
		final NXdata data = dataGroups.get(DATA_GROUP_NAME);
		assertThat(data, is(notNullValue()));

		assertThat(data.getNumberOfDataNodes(), is(2));
		assertThat(data.getDataNode(SCANNABLE_NAME), is(both(notNullValue()).and(sameInstance(
				entry.getInstrument().getPositioner(SCANNABLE_NAME).getDataNode(SCANNABLE_NAME)))));
		assertThat(data.getDataNode(NXpositioner.NX_VALUE), is(both(notNullValue()).and(sameInstance(
				entry.getInstrument().getPositioner(MONITOR_NAME).getDataNode(NXpositioner.NX_VALUE)))));
	}

}
