/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

import static gda.configuration.properties.LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertAxes;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertIndices;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertSignal;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertTarget;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusTestUtils.openNexusFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.builder.impl.DefaultNexusBuilderFactory;
import org.eclipse.dawnsci.nexus.device.impl.NexusDeviceService;
import org.eclipse.dawnsci.nexus.scan.impl.NexusScanFileServiceImpl;
import org.eclipse.dawnsci.nexus.template.impl.NexusTemplateServiceImpl;
import org.eclipse.scanning.device.Services;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.python.core.Py;
import org.python.core.PyTuple;

import gda.MockFactory;
import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.data.ServiceHolder;
import gda.data.scan.nexus.device.GDANexusDeviceAdapterFactory;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.scan.ConcurrentScan;
import uk.ac.diamond.daq.scanning.ScannableDeviceConnectorService;

public class ScannableWithStringInputFieldTest {

	private static final String DETECTOR_NAME = "det1";
	private static final String SCANNABLE_NAME = "s1";

	private static final String[] FIELD_NAMES = new String[] { "numVal", "strVal" };

	private static Object[][] SCAN_POSITIONS = new Object[][] {
		{ 700.0, "pc" },
		{ 700.0, "nc" },
		{ 705.0, "nc" },
		{ 705.0, "pc" }
	};

	private Detector detector;

	private Scannable scannable;

	private String outputDir;

	@Before
	public void setUp() throws Exception {
		detector = createDetector();
	}

	private Detector createDetector() throws DeviceException {
		final Detector detector = mock(Detector.class, DETECTOR_NAME);
		when(detector.getName()).thenReturn(DETECTOR_NAME);
		when(detector.getInputNames()).thenReturn(new String[0]);
		when(detector.getExtraNames()).thenReturn(new String[] { NXdetector.NX_DATA });
		when(detector.getOutputFormat()).thenReturn(new String[] { "%5.2g" });
		when(detector.readout()).thenReturn(new Double[] { 0.0 });
		when(detector.isBusy()).thenReturn(false);
		return detector;
	}

	protected void setUpTest(String testName) throws Exception {
		final String testDir = TestHelpers.setUpTest(this.getClass(), testName, true);
		outputDir = testDir + "/Data/";
		// note, setting this property in @BeforeScan doesn't work, it must be reset somewhere before the scan
		LocalProperties.set(GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, NexusScanDataWriter.class.getSimpleName());

		// TODO, this is copied from NexusScanDataWriterScanTest. Move to common location?
		final NexusDeviceService nexusDeviceService = new NexusDeviceService();

		final ServiceHolder gdaDataServiceHolder = new ServiceHolder();
		gdaDataServiceHolder.setNexusScanFileService(new NexusScanFileServiceImpl());
		gdaDataServiceHolder.setNexusDeviceService(nexusDeviceService);

		final org.eclipse.dawnsci.nexus.scan.ServiceHolder oednsServiceHolder = new org.eclipse.dawnsci.nexus.scan.ServiceHolder();
		oednsServiceHolder.setNexusDeviceService(nexusDeviceService);
		oednsServiceHolder.setNexusBuilderFactory(new DefaultNexusBuilderFactory());
		oednsServiceHolder.setTemplateService(new NexusTemplateServiceImpl());

		final org.eclipse.dawnsci.nexus.ServiceHolder oednServiceHolder = new org.eclipse.dawnsci.nexus.ServiceHolder();
		oednServiceHolder.setNexusFileFactory(new NexusFileFactoryHDF5());
		oednServiceHolder.setNexusDeviceAdapterFactory(new GDANexusDeviceAdapterFactory());

		new Services().setScannableDeviceService(new ScannableDeviceConnectorService());
	}

	@Test
	public void testScannableWithNumericAndStringInputField() throws Exception {
		setUpTest("testScannableWithNumericAndStringInputField");

		scannable = createScannable();
		final Object[] scanArguments = createScanArguments();
		final ConcurrentScan scan = new ConcurrentScan(scanArguments);

		scan.runScan();

		final File expectedNexusFile = new File(outputDir + "1.nxs");
		final String expectedNexusFilePath = expectedNexusFile.getAbsolutePath();
		assertThat(expectedNexusFile.exists(), is(true));

		// check the contents of the nexus file
		try (final NexusFile nexusFile = openNexusFile(expectedNexusFilePath)) {
			checkNexusFile(nexusFile);
		}
	}

	private Scannable createScannable() throws Exception {
		final Scannable numScannable = MockFactory.createMockScannable(FIELD_NAMES[0],
				new String[] { FIELD_NAMES[0] }, new String[0], new Double[] { 0.0 });
		final Scannable strScannable = MockFactory.createMockScannable(FIELD_NAMES[1],
				new String[] { FIELD_NAMES[1] }, new String[0], new String[] { "%s" }, 5, "pc");

		return new ScannableGroup(SCANNABLE_NAME, new Scannable[] { numScannable, strScannable });
	}

	private Object[] createScanArguments() {
		final List<Object> arguments = new ArrayList<>();
		arguments.add(scannable);
		arguments.add(new PyTuple(Py.javas2pys((Object[]) SCAN_POSITIONS)));
		arguments.add(detector);

		return arguments.toArray();
	}

	private void checkNexusFile(NexusFile nexusFile) throws NexusException {
		final TreeFile nexusTree = NexusUtils.loadNexusTree(nexusFile);
		final NXroot nexusRoot = (NXroot) nexusTree.getGroupNode();
		assertThat(nexusRoot, is(notNullValue()));
		final NXentry entry = nexusRoot.getEntry();
		assertThat(entry, is(notNullValue()));

		final NXinstrument instrument = entry.getInstrument();
		assertThat(instrument, is(notNullValue()));

		final NXcollection scannableCollection = instrument.getCollection(SCANNABLE_NAME);
		assertThat(scannableCollection, is(notNullValue()));

		final int[] scanShape = { SCAN_POSITIONS.length };
		for (int i = 0; i < FIELD_NAMES.length; i++) {
			final String fieldName = FIELD_NAMES[i];
			final DataNode dataNode = scannableCollection.getDataNode(fieldName);
			assertThat(dataNode, is(notNullValue()));
			assertThat(dataNode.getDataset().getShape(), is(scanShape));

			final String positionerName = SCANNABLE_NAME + "." + FIELD_NAMES[i];
			final NXpositioner positioner = instrument.getPositioner(positionerName);
			assertThat(positioner, is(notNullValue()));
			assertThat(positioner.getDataNodeNames(), contains(NXpositioner.NX_NAME, NXpositioner.NX_VALUE));
			assertThat(positioner.getNameScalar(), is(equalTo(positionerName)));
			assertThat(positioner.getDataNode(NXpositioner.NX_VALUE), is(Matchers.sameInstance(dataNode)));
		}

		// check the NXdata group
		assertThat(entry.getAllData().keySet(), contains(DETECTOR_NAME));
		final NXdata dataGroup = entry.getData(DETECTOR_NAME);
		assertThat(dataGroup, is(notNullValue()));
		final String[] scannableLinkedFieldNames = Arrays.stream(FIELD_NAMES).map(fieldName -> SCANNABLE_NAME + "_" + fieldName).toArray(String[]::new);
		final String[] expectedDataGroupFieldNames = ArrayUtils.add(scannableLinkedFieldNames, NXdetector.NX_DATA);
		assertThat(dataGroup.getDataNodeNames(), containsInAnyOrder(expectedDataGroupFieldNames));

		assertSignal(dataGroup, NXdetector.NX_DATA);
		assertAxes(dataGroup, SCANNABLE_NAME + "_" + FIELD_NAMES[0]);
		for (int i = 0; i < FIELD_NAMES.length; i++) {
			final String fieldName = FIELD_NAMES[i];
			final String dataFieldName = scannableLinkedFieldNames[i];
			final DataNode dataNode = dataGroup.getDataNode(dataFieldName);
			assertThat(dataNode, is(notNullValue()));
			assertThat(dataNode, is(sameInstance(scannableCollection.getDataNode(fieldName))));
			assertIndices(dataGroup, dataFieldName, 0);
			assertTarget(dataGroup, dataFieldName, nexusRoot, "/entry/instrument/" + SCANNABLE_NAME + "/" + fieldName);
		}
	}

}
