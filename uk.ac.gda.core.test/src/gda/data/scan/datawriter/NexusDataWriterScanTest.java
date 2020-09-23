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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.util.Map;
import java.util.stream.IntStream;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXsource;
import org.eclipse.dawnsci.nexus.NXuser;
import org.eclipse.january.dataset.DatasetFactory;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value=Parameterized.class)
public class NexusDataWriterScanTest extends AbstractNexusDataWriterScanTest {

	private static final String DATASET_NAME_SCAN_COMMAND = "scan_command";
	private static final String DATASET_NAME_SCAN_DIMENSIONS = "scan_dimensions";
	private static final String DATA_GROUP_NAME = "default";

	private static final String ATTRIBUTE_NAME_AXIS = "axis";
	private static final String ATTRIBUTE_NAME_LABEL = "label";
	private static final String ATTRIBUTE_NAME_PRIMARY = "primary";

	private static final String ENTRY_NAME = "entry1";

	private static final int NUM_SCANNABLE_VALUE_ATTRIBUTES = 5;
	private static final int NUM_MONITOR_VALUE_ATTRIBUTES = 3;

	@Parameters(name="scanRank = {0}")
	public static Object[] data() {
		return IntStream.rangeClosed(1, MAX_SCAN_RANK).mapToObj(Integer::valueOf).toArray();
	}

	public NexusDataWriterScanTest(int scanRank) {
		super(scanRank);
	}

	@BeforeClass
	public static void setUpServices() {
		AbstractNexusDataWriterScanTest.setUpServices();
	}

	@Override
	protected String getEntryName() {
		return ENTRY_NAME;
	}

	@Override
	protected void checkNexusMetadata(NXentry entry) {
		super.checkNexusMetadata(entry);

		// entry_identifier
		assertThat(entry.getEntry_identifierScalar(), is(equalTo(EXPECTED_ENTRY_IDENTIFER)));
		// program_name
		assertThat(entry.getProgram_nameScalar(), is(equalTo(EXPECTED_PROGRAM_NAME)));
		// scan_command
		assertThat(entry.getDataset(DATASET_NAME_SCAN_COMMAND).getString(), is(equalTo(getExpectedScanCommand())));
		// scan_dimensions
		assertThat(entry.getDataset(DATASET_NAME_SCAN_DIMENSIONS), is(equalTo(DatasetFactory.createFromObject(scanDimensions))));
		// title
		assertThat(entry.getTitleScalar(), is(equalTo(getExpectedScanCommand()))); // title seems to be same as scan command(!)
	}

	@Override
	protected void checkUsers(NXentry entry) {
		// user group
		final Map<String, NXuser> users = entry.getAllUser();
		assertThat(users.keySet(), Matchers.contains(EXPECTED_USER_NAME));
		final NXuser user = users.get(EXPECTED_USER_NAME);
		assertThat(user, is(notNullValue()));
		assertThat(user.getNumberOfNodelinks(), is(0));  // note that the created NXuser group is empty
	}

	@Override
	protected void checkInstrumentGroupMetadata(final NXinstrument instrument) {
		assertThat(instrument.getNumberOfDataNodes(), is(1));
		assertThat(instrument.getNameScalar(), is(equalTo(EXPECTED_INSTRUMENT_NAME)));

		assertThat(instrument.getNumberOfGroupNodes(), is(scanRank + 2)); // an NXposition for each scannable and the monitor, plus the NXsource
		checkSource(instrument);
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

	@Override
	protected void checkScannablePositioner(NXpositioner scannablePos, int i) throws Exception {
		final String scannableName = scannables[i].getName();
		final DataNode scannableValueDataNode = scannablePos.getDataNode(scannableName);
		final String expectedAxes = String.join(",", IntStream.range(0, scanRank).map(j->j+1).mapToObj(Integer::toString).toArray(String[]::new));
		assertThat(scannableValueDataNode, is(notNullValue()));
		assertThat(scannableValueDataNode.getNumberOfAttributes(), is(NUM_SCANNABLE_VALUE_ATTRIBUTES));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_AXIS).getFirstElement(), is(equalTo(expectedAxes)));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_LABEL).getFirstElement(), is(Integer.toString(i+1)));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_LOCAL_NAME).getFirstElement(),
				is(equalTo(scannableName + "." + scannableName)));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_PRIMARY).getFirstElement(), is(equalTo(("1"))));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_TARGET).getFirstElement(),
				is(equalTo("/" + ENTRY_NAME + "/" + INSTRUMENT_NAME + "/" + scannableName + "/" + scannableName)));
		assertThat(scannableValueDataNode.getDataset().getSlice(),
				is(equalTo(getExpectedScannableDataset(i)))); // check values

		assertThat(scannablePos.getSoft_limit_minScalar(), is(equalTo(SCANNABLE_LOWER_BOUND)));
		assertThat(scannablePos.getSoft_limit_maxScalar(), is(equalTo(SCANNABLE_UPPER_BOUND)));
	}

	@Override
	protected void checkMonitorPositioner(NXpositioner monitorPos) throws Exception {
		final DataNode monitorValueDataNode = monitorPos.getDataNode(NXpositioner.NX_VALUE);
		assertThat(monitorValueDataNode, is(notNullValue()));

		assertThat(monitorValueDataNode.getNumberOfAttributes(), is(NUM_MONITOR_VALUE_ATTRIBUTES));
		final String expectedAxes = String.join(",", IntStream.range(0, scanRank).map(j->j+1).mapToObj(Integer::toString).toArray(String[]::new));
		assertThat(monitorValueDataNode.getAttribute(ATTRIBUTE_NAME_AXIS).getFirstElement(), is(equalTo(expectedAxes)));
		assertThat(monitorValueDataNode.getAttribute(ATTRIBUTE_NAME_LOCAL_NAME).getFirstElement(),
				is(equalTo(MONITOR_NAME + "." + NXpositioner.NX_VALUE)));
		assertThat(monitorValueDataNode.getAttribute(ATTRIBUTE_NAME_TARGET).getFirstElement(),
				is(equalTo("/" + ENTRY_NAME + "/" + INSTRUMENT_NAME + "/" + MONITOR_NAME + "/" + NXpositioner.NX_VALUE)));
		assertThat(monitorValueDataNode.getDataset().getSlice(),
				is(equalTo(DatasetFactory.zeros(scanDimensions).fill(MONITOR_VALUE)))); // check values
	}

	@Override
	protected void checkDataGroups(NXentry entry) {
		// NexusDataWriter creates a single NXdata group
		final Map<String, NXdata> dataGroups = entry.getAllData();
		assertThat(dataGroups.keySet(), contains(DATA_GROUP_NAME));
		final NXdata data = dataGroups.get(DATA_GROUP_NAME);
		assertThat(data, is(notNullValue()));

		assertThat(data.getNumberOfDataNodes(), is(scanRank + 1));
		for (int i = 0; i < scanRank; i++) {
			final String scannableName = scannables[i].getName();
			assertThat(data.getDataNode(scannableName), is(both(notNullValue()).and(sameInstance(
					entry.getInstrument().getPositioner(scannableName).getDataNode(scannableName)))));
		}
		assertThat(data.getDataNode(NXpositioner.NX_VALUE), is(both(notNullValue()).and(sameInstance(
				entry.getInstrument().getPositioner(MONITOR_NAME).getDataNode(NXpositioner.NX_VALUE)))));
	}

}
