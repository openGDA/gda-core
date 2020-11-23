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

import static org.eclipse.scanning.test.utilities.scan.nexus.NexusAssert.assertUnits;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.util.Map;
import java.util.stream.IntStream;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinsertion_device;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXmonochromator;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXsource;
import org.eclipse.dawnsci.nexus.NXuser;
import org.eclipse.dawnsci.nexus.template.impl.NexusTemplateServiceImpl;
import org.eclipse.january.dataset.DatasetFactory;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import gda.configuration.properties.LocalProperties;
import gda.data.ServiceHolder;

@RunWith(value=Parameterized.class)
public class NexusDataWriterScanTest extends AbstractNexusDataWriterScanTest {

	private static final int[] METADATA_DATASET_SHAPE = new int[] { 1 };

	private static final String FIELD_NAME_SCAN_COMMAND = "scan_command";
	private static final String FIELD_NAME_SCAN_DIMENSIONS = "scan_dimensions";
	private static final String FIELD_NAME_USER_NAME = "username";
	private static final String GROUP_NAME_DEFAULT = "default";

	private static final String ATTRIBUTE_NAME_AXIS = "axis";
	private static final String ATTRIBUTE_NAME_LABEL = "label";
	private static final String ATTRIBUTE_NAME_PRIMARY = "primary";

	private static final String ENTRY_NAME = "entry1";

	private static final String METADATA_KEY_MONOCHROMATOR_NAME = "instrument.monochromator.name";
	private static final String METADATA_KEY_MONOCHROMATOR_ENERGY = "instrument.monochromator.energy";
	private static final String METADATA_KEY_MONOCHROMATOR_WAVELENGTH = "instrument.monochromator.wavelength";
	private static final String METADATA_KEY_INSERTION_DEVICE_GAP = "instrument.insertion_device.gap";
	private static final String METADATA_KEY_INSTRUMENT_SOURCE_ENERGY = "instrument.source.energy";
	private static final String METADATA_KEY_INSTRUMENT_SOURCE_CURRENT = "instrument.source.current";

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

		final ServiceHolder gdaDataServiceHolder = new ServiceHolder();
		gdaDataServiceHolder.setNexusTemplateService(new NexusTemplateServiceImpl());
	}

	@Override
	protected void setUpTest(String testName) throws Exception {
		super.setUpTest(testName);
		LocalProperties.set(NexusDataWriter.GDA_NEXUS_CREATE_SRS, "true");
	}

	@Override
	protected void setUpMetadata() throws Exception {
		super.setUpMetadata();

		// Note: I tried using ScannableMetadataEntries here, but they don't work with no JythonServiceFacade set up, which is hard in a test.
		// Additional note: for some reason metadata values have to be strings.

		// metadata entries for NXmonochromator
		addMetadataEntry(METADATA_KEY_MONOCHROMATOR_NAME, EXPECTED_MONOCHROMATOR_NAME);
		addMetadataEntry(METADATA_KEY_MONOCHROMATOR_ENERGY, EXPECTED_MONOCHROMATOR_ENERGY);
		addMetadataEntry(METADATA_KEY_MONOCHROMATOR_WAVELENGTH, EXPECTED_MONOCHROMATOR_WAVELENGTH);

		// metadata entries for NXinsertion_device - this is the only field that NeXusUtils.writeNXinsertionDevice actually writes)
		addMetadataEntry(METADATA_KEY_INSERTION_DEVICE_GAP, EXPECTED_INSERTION_DEVICE_GAP);

		// metadata entries for NXsource
		addMetadataEntry(METADATA_KEY_INSTRUMENT_SOURCE_ENERGY, EXPECTED_SOURCE_ENERGY);
		addMetadataEntry(METADATA_KEY_INSTRUMENT_SOURCE_CURRENT, EXPECTED_SOURCE_CURRENT);
	}

	@Override
	protected void checkNexusMetadata(NXentry entry) {
		super.checkNexusMetadata(entry);

		// entry_identifier
		assertThat(entry.getEntry_identifierScalar(), is(equalTo(EXPECTED_ENTRY_IDENTIFER)));
		// program_name
		assertThat(entry.getProgram_nameScalar(), is(equalTo(EXPECTED_PROGRAM_NAME)));
		// scan_command
		assertThat(entry.getDataset(FIELD_NAME_SCAN_COMMAND).getString(), is(equalTo(getExpectedScanCommand())));
		// scan_dimensions
		assertThat(entry.getDataset(FIELD_NAME_SCAN_DIMENSIONS), is(equalTo(DatasetFactory.createFromObject(scanDimensions))));
		// title
		assertThat(entry.getTitleScalar(), is(equalTo(getExpectedScanCommand()))); // title seems to be same as scan command(!)
	}

	@Override
	protected void checkInstrumentGroupMetadata(final NXinstrument instrument) {
		assertThat(instrument.getDataNodeNames(), contains(NXinstrument.NX_NAME));
		assertThat(instrument.getNumberOfDataNodes(), is(1));
		assertThat(instrument.getNameScalar(), is(equalTo(EXPECTED_INSTRUMENT_NAME)));

		// group for each device, plus metadata groups: source, monochromator, insertion_device
		final int expectedGroupNodes = getNumDevices() + 3;
		assertThat(instrument.getNumberOfGroupNodes(), is(expectedGroupNodes));
		checkSource(instrument);
	}

	private void checkSource(NXinstrument instrument) {
		final NXsource source = instrument.getSource();
		assertThat(source, is(notNullValue()));

		final String[] expectedDataNodeNames = { NXsource.NX_NAME, NXsource.NX_PROBE,
				NXsource.NX_TYPE, NXsource.NX_CURRENT, NXsource.NX_ENERGY };
		assertThat(source.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames));
		assertThat(source.getNumberOfDataNodes(), is(expectedDataNodeNames.length));
		assertThat(source.getNumberOfGroupNodes(), is(0));

		assertThat(source.getNameScalar(), is(equalTo("DLS")));
		assertThat(source.getProbeScalar(), is(equalTo("x-ray")));
		assertThat(source.getTypeScalar(), is(equalTo("Synchrotron X-ray Source")));
		assertThat(source.getEnergyScalar(), is(equalTo(3.0)));
		assertThat(source.getCurrentScalar(), is(equalTo(25.5)));
	}

	@Override
	protected void checkDefaultScannablePositioner(NXpositioner scannablePos, int i) throws Exception {
		final String scannableName = scannables[i].getName();

		final String[] expectedDataNodeNames = { scannableName, NXpositioner.NX_SOFT_LIMIT_MIN, NXpositioner.NX_SOFT_LIMIT_MAX };
		assertThat(scannablePos.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames));
		assertThat(scannablePos.getNumberOfDataNodes(), is(expectedDataNodeNames.length));

		final DataNode scannableValueDataNode = scannablePos.getDataNode(scannableName);
		final String expectedAxes = String.join(",", IntStream.range(0, scanRank).map(j->j+1).mapToObj(Integer::toString).toArray(String[]::new));
		assertThat(scannableValueDataNode, is(notNullValue()));

		final String[] expectedDataNodeAttrNames = { ATTRIBUTE_NAME_AXIS, ATTRIBUTE_NAME_LABEL,
				ATTRIBUTE_NAME_LOCAL_NAME, ATTRIBUTE_NAME_PRIMARY, ATTRIBUTE_NAME_TARGET };
		assertThat(scannableValueDataNode.getAttributeNames(), containsInAnyOrder(expectedDataNodeAttrNames));
		assertThat(scannableValueDataNode.getNumberOfAttributes(), is(expectedDataNodeAttrNames.length));

		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_AXIS).getFirstElement(), is(equalTo(expectedAxes)));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_LABEL).getFirstElement(), is(Integer.toString(i)));
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
	protected void checkConfiguredScannablePositioner(String scannableName, NXpositioner positioner)
			throws Exception {
		assertThat(positioner.getDataNodeNames(), contains(scannableName));
		assertThat(positioner.getNumberOfDataNodes(), is(1));

		final DataNode scannableValueDataNode = positioner.getDataNode(scannableName);
		assertThat(scannableValueDataNode, is(notNullValue()));

		final String[] expectedDataNodeAttrNames = { ATTRIBUTE_NAME_AXIS, ATTRIBUTE_NAME_LOCAL_NAME, ATTRIBUTE_NAME_PRIMARY,
				ATTRIBUTE_NAME_TARGET, ATTRIBUTE_NAME_UNITS };
		assertThat(scannableValueDataNode.getAttributeNames(), containsInAnyOrder(expectedDataNodeAttrNames));
		assertThat(scannableValueDataNode.getNumberOfAttributes(), is(expectedDataNodeAttrNames.length));

		final String expectedAxes = String.join(",", IntStream.range(0, scanRank).map(j->j+1).mapToObj(Integer::toString).toArray(String[]::new));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_AXIS).getFirstElement(), is(equalTo(expectedAxes)));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_LOCAL_NAME).getFirstElement(),
				is(equalTo(scannableName + "." + scannableName)));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_TARGET).getFirstElement(), is(equalTo(
				"/" + ENTRY_NAME + "/" + INSTRUMENT_NAME + "/" + scannableName + "/" + scannableName)));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_PRIMARY).getFirstElement(), is(equalTo("1")));
		assertThat(scannableValueDataNode.getAttribute(ATTRIBUTE_NAME_UNITS).getFirstElement(),
				is(equalTo("mm")));

		assertThat(scannableValueDataNode.getDataset().getSlice(),
				is(equalTo(DatasetFactory.createFromObject(getExpectedScannableDataset(0))))); // check values
	}

	@Override
	protected void checkMonitorPositioner(NXpositioner monitorPos) throws Exception {
		assertThat(monitorPos.getDataNodeNames(), contains(NXpositioner.NX_VALUE));
		assertThat(monitorPos.getNumberOfDataNodes(), is(1));

		final DataNode monitorValueDataNode = monitorPos.getDataNode(NXpositioner.NX_VALUE);
		assertThat(monitorValueDataNode, is(notNullValue()));

		final String[] expectedDataNodeAttrNames = { ATTRIBUTE_NAME_AXIS, ATTRIBUTE_NAME_LOCAL_NAME, ATTRIBUTE_NAME_PRIMARY,
				ATTRIBUTE_NAME_TARGET, ATTRIBUTE_NAME_LABEL };
		assertThat(monitorValueDataNode.getAttributeNames(), containsInAnyOrder(expectedDataNodeAttrNames));
		assertThat(monitorValueDataNode.getNumberOfAttributes(), is(expectedDataNodeAttrNames.length));

		final String expectedAxes = String.join(",", IntStream.range(0, scanRank).map(j->j+1).mapToObj(Integer::toString).toArray(String[]::new));
		assertThat(monitorValueDataNode.getAttribute(ATTRIBUTE_NAME_AXIS).getFirstElement(), is(equalTo(expectedAxes)));
		assertThat(monitorValueDataNode.getAttribute(ATTRIBUTE_NAME_LOCAL_NAME).getFirstElement(),
				is(equalTo(MONITOR_NAME + "." + NXpositioner.NX_VALUE)));
		assertThat(monitorValueDataNode.getAttribute(ATTRIBUTE_NAME_TARGET).getFirstElement(),
				is(equalTo("/" + ENTRY_NAME + "/" + INSTRUMENT_NAME + "/" + MONITOR_NAME + "/" + NXpositioner.NX_VALUE)));
		assertThat(monitorValueDataNode.getAttribute(ATTRIBUTE_NAME_LABEL).getFirstElement(), is(equalTo(Integer.toString(scanRank))));
		assertThat(monitorValueDataNode.getAttribute(ATTRIBUTE_NAME_PRIMARY).getFirstElement(), is(equalTo("1")));
		assertThat(monitorValueDataNode.getDataset().getSlice(),
				is(equalTo(DatasetFactory.zeros(scanDimensions).fill(MONITOR_VALUE)))); // check values
	}

	@Override
	protected void checkMetadataScannablePositioner(NXpositioner positioner, int index) throws Exception {
		final String scannableName = METADATA_SCANNABLE_NAMES[index];
		assertThat(positioner.getNumberOfDataNodes(), is(1));

		final DataNode valueDataNode = positioner.getDataNode(scannableName);
		assertThat(valueDataNode, is(notNullValue()));

		final String[] expectedDataNodeAttrNames = { ATTRIBUTE_NAME_AXIS, ATTRIBUTE_NAME_LOCAL_NAME, ATTRIBUTE_NAME_UNITS };
		assertThat(valueDataNode.getNumberOfAttributes(), is(expectedDataNodeAttrNames.length));

		assertThat(valueDataNode.getAttribute(ATTRIBUTE_NAME_AXIS).getFirstElement(), is(equalTo("1")));
		assertThat(valueDataNode.getAttribute(ATTRIBUTE_NAME_LOCAL_NAME).getFirstElement(),
				is(equalTo(scannableName + "." + scannableName)));
		assertThat(valueDataNode.getAttribute(ATTRIBUTE_NAME_UNITS).getFirstElement(), is(equalTo("mm")));

		assertThat(valueDataNode.getDataset().getShape(), is(equalTo(METADATA_DATASET_SHAPE)));
		assertThat(valueDataNode.getDataset().getSlice().getDouble(0), is(equalTo((double) index)));
	}

	@Override
	protected void checkDataGroups(NXentry entry) {
		// NexusDataWriter creates a single NXdata group
		final String expectedDataGroupName = detectorType == DetectorType.NONE ? GROUP_NAME_DEFAULT : detector.getName();
		final Map<String, NXdata> dataGroups = entry.getAllData();
		assertThat(dataGroups.keySet(), contains(expectedDataGroupName));
		final NXdata data = dataGroups.get(expectedDataGroupName);
		assertThat(data, is(notNullValue()));

		final String[] scannableAndMonitorNames = getScannableAndMonitorNames();
		int expectedNumDataNodes = scannableAndMonitorNames.length;
		switch (detectorType) {
			case NONE: break;
			case NEXUS_DEVICE: break;
			case COUNTER_TIMER:
				final String[] extraNames = detector.getExtraNames();
				expectedNumDataNodes += extraNames.length;
				for (String name : extraNames) {
					assertThat(data.getDataNode(name), is(both(notNullValue()).and(sameInstance(
							entry.getInstrument().getDetector(detector.getName()).getDataNode(name)))));
				}
				break;
			default:
				throw new IllegalArgumentException("Unknown detector type " + detectorType);
		}

		assertThat(data.getNumberOfDataNodes(), is(expectedNumDataNodes));
		for (int i = 0; i < scannableAndMonitorNames.length; i++) {
			final String scannableName = scannableAndMonitorNames[i];
			if (i < scanRank) {
				assertThat(data.getDataNode(scannableName), is(both(notNullValue()).and(sameInstance(
						entry.getInstrument().getPositioner(scannableName).getDataNode(scannableName)))));
			} else {
				assertThat(data.getDataNode(NXpositioner.NX_VALUE), is(both(notNullValue()).and(sameInstance(
						entry.getInstrument().getPositioner(MONITOR_NAME).getDataNode(NXpositioner.NX_VALUE)))));
			}
		}
	}

	@Override
	protected void checkMonochromatorGroup(NXinstrument instrument) {
		final NXmonochromator monochromator = instrument.getMonochromator();
		assertThat(monochromator, is(notNullValue()));

		assertThat(monochromator.getDataNode("name").getString(), is(equalTo(EXPECTED_MONOCHROMATOR_NAME)));
		assertThat(monochromator.getEnergyScalar(), is(equalTo(EXPECTED_MONOCHROMATOR_ENERGY)));
		assertUnits(monochromator.getDataNode(NXmonochromator.NX_ENERGY), "keV");
		assertThat(monochromator.getWavelengthScalar(), is(equalTo(EXPECTED_MONOCHROMATOR_WAVELENGTH)));
		assertUnits(monochromator.getDataNode(NXmonochromator.NX_WAVELENGTH), "Angstrom");
	}

	@Override
	protected void checkInsertionDeviceGroup(NXinstrument instrument) {
		final NXinsertion_device insertionDevice = instrument.getInsertion_device();
		assertThat(insertionDevice, is(notNullValue()));

		assertThat(insertionDevice.getGapScalar(), is(equalTo(EXPECTED_INSERTION_DEVICE_GAP)));
		assertUnits(insertionDevice.getDataNode(NXinsertion_device.NX_GAP), "mm");
	}

	@Override
	protected void checkSourceGroup(NXinstrument instrument) {
		final NXsource source = instrument.getSource();
		assertThat(source, is(notNullValue()));

		assertThat(source.getNumberOfDataNodes(), is(5));
		assertThat(source.getNumberOfGroupNodes(), is(0));

		assertThat(source.getNameScalar(), is(equalTo("DLS")));
		assertThat(source.getProbeScalar(), is(equalTo("x-ray")));
		assertThat(source.getTypeScalar(), is(equalTo("Synchrotron X-ray Source")));

		assertThat(source.getEnergyScalar(), is(equalTo(EXPECTED_SOURCE_ENERGY)));
		assertUnits(source.getDataNode(NXsource.NX_ENERGY), "GeV");
		assertThat(source.getCurrentScalar(), is(equalTo(EXPECTED_SOURCE_CURRENT)));
		assertUnits(source.getDataNode(NXsource.NX_CURRENT), "mA");
	}

	@Override
	protected void checkSampleGroup(NXentry entry) {
		// NeXusUtil.write_NXsample is not called and only creates an empty NXsample group
		assertThat(entry.getAllSample().size(), is(0));
	}

	@Override
	protected void checkUsers(NXentry entry) {
		// user group
		final Map<String, NXuser> users = entry.getAllUser();
		assertThat(users.keySet(), Matchers.contains(EXPECTED_USER_GROUP_NAME));
		final NXuser user = users.get(EXPECTED_USER_GROUP_NAME);
		assertThat(user, is(notNullValue()));
		assertThat(user.getNumberOfNodelinks(), is(1));
		assertThat(user.getString(FIELD_NAME_USER_NAME), is(equalTo(EXPECTED_USER_NAME)));
	}

}
