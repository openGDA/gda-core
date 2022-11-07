/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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
import static gda.data.scan.datawriter.NexusScanDataWriter.PROPERTY_VALUE_DATA_FORMAT_NEXUS_SCAN;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_GDA_FIELD_NAME;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_GDA_SCANNABLE_NAME;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_GDA_SCAN_ROLE;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_LOCAL_NAME;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_UNITS;
import static org.eclipse.dawnsci.nexus.NexusConstants.DATA_AXES;
import static org.eclipse.dawnsci.nexus.NexusConstants.DATA_INDICES_SUFFIX;
import static org.eclipse.dawnsci.nexus.NexusConstants.DATA_SIGNAL;
import static org.eclipse.dawnsci.nexus.NexusConstants.NXCLASS;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertAxes;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertIndices;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertSignal;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.measure.quantity.Length;

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
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.builder.impl.DefaultNexusBuilderFactory;
import org.eclipse.dawnsci.nexus.device.impl.NexusDeviceService;
import org.eclipse.dawnsci.nexus.scan.impl.NexusScanFileServiceImpl;
import org.eclipse.dawnsci.nexus.template.impl.NexusTemplateServiceImpl;
import org.eclipse.dawnsci.nexus.test.utilities.NexusTestUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.scanning.device.Services;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.data.ServiceHolder;
import gda.data.scan.nexus.device.GDADeviceNexusConstants;
import gda.data.scan.nexus.device.GDANexusDeviceAdapterFactory;
import gda.device.Detector;
import gda.device.Scannable;
import gda.device.detector.DummyDetector;
import gda.device.scannable.DummyMultiFieldUnitsScannable;
import gda.scan.ConcurrentScan;
import uk.ac.diamond.daq.scanning.ScannableDeviceConnectorService;
import uk.ac.diamond.daq.scanning.ScannableNexusWrapperScanTest;

public class MultiFieldScannableNexusScanTest {

	private static final String SCANNABLE_NAME = "scan1";
	private static final String DETECTOR_NAME = "det";
	private static final String EXPECTED_UNITS = "mm";
	private static final String ATTR_NAME_TARGET = "target";

	private String outputDir;

	private Detector detector;
	private Scannable scannable;

	private String[] inputNames;
	private String[] extraNames;
	private Double[] lowerLimits;
	private Double[] upperLimits;


	@BeforeAll
	public static void setUpServices() {
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

	@AfterEach
	public void tearDown() {
		LocalProperties.clearProperty(GDA_DATA_SCAN_DATAWRITER_DATAFORMAT);
	}

	@Test
	public void testScanSingleInputNoExtra() throws Exception {
		testMultiFieldScannableNexusTest(1, 0);
	}

	@Test
	public void testScanSingleInputSingleExtra() throws Exception {
		testMultiFieldScannableNexusTest(1, 1);
	}

	@Test
	public void testScanSingleInputMultiExtra() throws Exception {
		testMultiFieldScannableNexusTest(1, 3);
	}

	@Test
	public void testScanMultiInputNoExtra() throws Exception {
		testMultiFieldScannableNexusTest(3, 0);
	}

	@Test
	public void testScanMultiInputSingleExtra() throws Exception {
		testMultiFieldScannableNexusTest(3, 1);
	}

	@Test
	public void testScanMultiInputMultiExtra() throws Exception {
		testMultiFieldScannableNexusTest(3, 3);
	}

	private void testMultiFieldScannableNexusTest(int numInputFields, int numExtraFields) throws Exception {
		setUpTest("testMultiFieldScannableNexusTest" + numInputFields + "input" + numExtraFields + "extra");

		scannable = createScannable(numInputFields, numExtraFields);
		detector = createDetector();
		runScan();
		checkNexusFile();
	}

	private void setUpTest(String testName) throws Exception {
		final String testDir = TestHelpers.setUpTest(ScannableNexusWrapperScanTest.class, testName, true);
		outputDir = testDir + "/Data/";
		// need to set property after call to setUpTest above
		LocalProperties.set(GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, PROPERTY_VALUE_DATA_FORMAT_NEXUS_SCAN);
	}

	private Scannable createScannable(int numInputNames, int numExtraNames) throws Exception {
		final DummyMultiFieldUnitsScannable<Length> scannable = new DummyMultiFieldUnitsScannable<>(SCANNABLE_NAME);
		scannable.setHardwareUnitString(EXPECTED_UNITS);
		scannable.setUserUnits(EXPECTED_UNITS);

		inputNames = IntStream.rangeClosed(1, numInputNames).mapToObj(i -> "input" + i).toArray(String[]::new);
		scannable.setInputNames(inputNames);
		extraNames = IntStream.rangeClosed(1, numExtraNames).mapToObj(i -> "extra" + i).toArray(String[]::new);
		scannable.setExtraNames(extraNames);

		lowerLimits = IntStream.rangeClosed(1, numInputNames) // -100, -200, etc.
				.map(i -> -i * 100).mapToObj(Double::valueOf).toArray(Double[]::new);
		scannable.setLowerGdaLimits(lowerLimits);
		upperLimits = IntStream.rangeClosed(1, numInputNames) // 100, 200, etc.
				.map(i -> i * 100).mapToObj(Double::valueOf).toArray(Double[]::new);
		scannable.setUpperGdaLimits(upperLimits);

		return scannable;
	}

	private Detector createDetector() {
		final DummyDetector detector = new DummyDetector(DETECTOR_NAME);
		detector.setExtraNames(new String[] { NXdetector.NX_DATA });
		return detector;
	}

	private void runScan() throws Exception {
		final Object[] scanArguments = createScanArguments();
		final ConcurrentScan scan = new ConcurrentScan(scanArguments);
		scan.runScan();
	}

	private Object[] createScanArguments() {
		final Object startPos;
		final Object stopPos;
		final Object stepSize;

		final int numInputFields = inputNames.length;
		if (numInputFields == 1) {
			startPos = 0.0;
			stopPos = 10.0;
			stepSize = 1.0;
		} else {
			startPos = IntStream.range(0, numInputFields).mapToObj(Double::valueOf).toArray(Double[]::new);
			stopPos = IntStream.range(0, numInputFields).map(i -> i + 10).mapToObj(Double::valueOf).toArray(Double[]::new);
			stepSize = Collections.nCopies(numInputFields, 1.0).stream().toArray(Double[]::new);
		}

		return new Object[] { scannable, startPos, stopPos, stepSize, detector };
	}

	private void checkNexusFile() throws NexusException, DatasetException {
		final String nexusFilePath = outputDir + "1.nxs";
		assertThat(new File(nexusFilePath).exists(), is(true));

		final TreeFile nexusTree = NexusTestUtils.loadNexusFile(nexusFilePath, true);
		final NXroot nexusRoot = (NXroot) nexusTree.getGroupNode();
		assertThat(nexusRoot, is(notNullValue()));
		final NXentry entry = nexusRoot.getEntry();
		assertThat(entry, is(notNullValue()));
		final NXinstrument instrument = entry.getInstrument();
		assertThat(instrument, is(notNullValue()));

		final String[] deviceNames = new String[] { SCANNABLE_NAME, DETECTOR_NAME }; // SCANNABLE_NAME is NXpositioner in single field case, NXcollection in multi field case
		final String[] expectedGroupNodeNames = inputNames.length == 1 ? deviceNames :
				Stream.concat(Arrays.stream(deviceNames), Arrays.stream(scannable.getInputNames()).map(name -> SCANNABLE_NAME + "." + name)).toArray(String[]::new);
		assertThat(instrument.getGroupNodeNames(), containsInAnyOrder(expectedGroupNodeNames));

		final NXdetector detector = instrument.getDetector(DETECTOR_NAME);
		assertThat(detector, is(notNullValue()));
		assertThat(detector.getDataNodeNames(), containsInAnyOrder(NXdetector.NX_DATA,
				NXdetector.NX_TYPE, NXdetector.NX_DESCRIPTION, "id"));
		final DataNode detectorData = detector.getDataNode(NXdetector.NX_DATA);
		assertThat(detectorData, is(notNullValue()));

		for (int inputFieldIndex = 0; inputFieldIndex < inputNames.length; inputFieldIndex++) {
			checkPositioner(instrument, inputFieldIndex);
		}

		// in the case of multiple input fields, an NXcollection is created with links to the input fields and any extra fields
		if (inputNames.length > 1) {
			checkNXcollection(instrument);
		}

		checkDataGroup(entry);
	}

	private void checkPositioner(final NXinstrument instrument, int inputFieldIndex) throws DatasetException {
		final boolean singleInputField = inputNames.length == 1;
		final String inputName = inputNames[inputFieldIndex];
		final String positionerName = singleInputField ? SCANNABLE_NAME : SCANNABLE_NAME + "." + inputName;
		final NXpositioner positioner = instrument.getPositioner(positionerName);
		assertThat(positioner, is(notNullValue()));
		assertThat(positioner.getAttributeNames(), containsInAnyOrder(NXCLASS, ATTRIBUTE_NAME_GDA_SCANNABLE_NAME, ATTRIBUTE_NAME_GDA_SCAN_ROLE));
		assertThat(positioner.getAttrString(null, ATTRIBUTE_NAME_GDA_SCANNABLE_NAME), is(equalTo(SCANNABLE_NAME)));
		assertThat(positioner.getAttrString(null, ATTRIBUTE_NAME_GDA_SCAN_ROLE), is(equalTo(ScanRole.SCANNABLE.toString().toLowerCase())));

		final String[] standardFieldNames = new String[] { NXpositioner.NX_VALUE, NXpositioner.NX_NAME, NXpositioner.NX_SOFT_LIMIT_MIN, NXpositioner.NX_SOFT_LIMIT_MAX };
		final String[] expectedDataNodeNames = singleInputField ?
				Stream.of(standardFieldNames, extraNames).flatMap(Stream::of).toArray(String[]::new) :
				standardFieldNames;
		assertThat(positioner.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames));
		assertThat(positioner.getNameScalar(), is(equalTo(SCANNABLE_NAME + (singleInputField ? "" : "." + inputName))));

		final DataNode valueDataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
		assertThat(valueDataNode, is(notNullValue()));
		assertThat(valueDataNode.getDataset(), is(notNullValue()));
		assertThat(valueDataNode.getAttributeNames(), containsInAnyOrder(ATTRIBUTE_NAME_LOCAL_NAME, ATTRIBUTE_NAME_GDA_FIELD_NAME, ATTRIBUTE_NAME_UNITS, ATTR_NAME_TARGET));
		assertThat(positioner.getAttrString(NXpositioner.NX_VALUE, ATTRIBUTE_NAME_GDA_FIELD_NAME), is(equalTo(inputName)));
		assertThat(positioner.getAttrString(NXpositioner.NX_VALUE, ATTRIBUTE_NAME_LOCAL_NAME), is(equalTo(SCANNABLE_NAME + "." + inputName)));
		assertThat(positioner.getAttrString(NXpositioner.NX_VALUE, ATTRIBUTE_NAME_UNITS), is(equalTo(EXPECTED_UNITS)));

		if (singleInputField) {
			for (String extraName : extraNames) {
				final DataNode extraFieldDataNode = positioner.getDataNode(extraName);
				assertThat(extraFieldDataNode, notNullValue());
				assertThat(extraFieldDataNode.getDataset(), is(notNullValue()));
				assertThat(extraFieldDataNode.getAttributeNames(), containsInAnyOrder(GDADeviceNexusConstants.ATTRIBUTE_NAME_LOCAL_NAME, GDADeviceNexusConstants.ATTRIBUTE_NAME_GDA_FIELD_NAME, GDADeviceNexusConstants.ATTRIBUTE_NAME_UNITS));
				assertThat(positioner.getAttrString(extraName, GDADeviceNexusConstants.ATTRIBUTE_NAME_LOCAL_NAME), is(equalTo(SCANNABLE_NAME + "." + extraName)));
				assertThat(positioner.getAttrString(extraName, GDADeviceNexusConstants.ATTRIBUTE_NAME_GDA_FIELD_NAME), is(equalTo(extraName)));
				assertThat(positioner.getAttrString(extraName, GDADeviceNexusConstants.ATTRIBUTE_NAME_UNITS), is(equalTo(EXPECTED_UNITS)));
			}
		}

		final DataNode softLimitMin = positioner.getDataNode(NXpositioner.NX_SOFT_LIMIT_MIN);
		assertThat(softLimitMin, is(notNullValue()));
		final IDataset softLimitMinDataset = softLimitMin.getDataset().getSlice();
		assertThat(softLimitMinDataset, is(equalTo(DatasetFactory.createFromObject(lowerLimits[inputFieldIndex]))));

		final DataNode softLimitMax = positioner.getDataNode(NXpositioner.NX_SOFT_LIMIT_MAX);
		assertThat(softLimitMax, is(notNullValue()));
		final IDataset softLimitMaxDataset = softLimitMax.getDataset().getSlice();
		assertThat(softLimitMaxDataset, is(equalTo(DatasetFactory.createFromObject(upperLimits[inputFieldIndex]))));
	}

	private void checkNXcollection(final NXinstrument instrument) {
		final NXcollection collection = instrument.getCollection(SCANNABLE_NAME);
		assertThat(collection, is(notNullValue()));

		assertThat(collection.getAttributeNames(), containsInAnyOrder(NXCLASS, GDADeviceNexusConstants.ATTRIBUTE_NAME_GDA_SCANNABLE_NAME, GDADeviceNexusConstants.ATTRIBUTE_NAME_GDA_SCAN_ROLE));
		assertThat(collection.getGroupNodeNames(), is(empty()));
		final String[] expectedDataNodeNames = Stream.of(inputNames, extraNames, new String[] { GDADeviceNexusConstants.FIELD_NAME_NAME })
				.flatMap(Stream::of).toArray(String[]::new);
		assertThat(collection.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames));
		assertThat(collection.getAttrString(null, GDADeviceNexusConstants.ATTRIBUTE_NAME_GDA_SCANNABLE_NAME), is(equalTo(SCANNABLE_NAME)));
		assertThat(collection.getAttrString(null, GDADeviceNexusConstants.ATTRIBUTE_NAME_GDA_SCAN_ROLE), is(equalTo(ScanRole.SCANNABLE.toString().toLowerCase())));

		for (int i = 0; i < inputNames.length; i++) {
			final String inputName = inputNames[i];
			final DataNode inputFieldDataNode = collection.getDataNode(inputName);
			assertThat(inputFieldDataNode, is(notNullValue()));
			assertThat(inputFieldDataNode, is(sameInstance(
					instrument.getPositioner(SCANNABLE_NAME + "." + inputName).getDataNode(NXpositioner.NX_VALUE))));
		}

		for (int i= 0; i < extraNames.length; i++) {
			final String extraName = extraNames[i];
			final DataNode extraFieldDataNode = collection.getDataNode(extraName);
			assertThat(extraFieldDataNode, is(notNullValue()));
			assertThat(extraFieldDataNode.getAttributeNames(), containsInAnyOrder(GDADeviceNexusConstants.ATTRIBUTE_NAME_GDA_FIELD_NAME, GDADeviceNexusConstants.ATTRIBUTE_NAME_LOCAL_NAME, GDADeviceNexusConstants.ATTRIBUTE_NAME_UNITS));
			assertThat(collection.getAttrString(extraName, GDADeviceNexusConstants.ATTRIBUTE_NAME_GDA_FIELD_NAME), is(equalTo(extraName)));
			assertThat(collection.getAttrString(extraName, GDADeviceNexusConstants.ATTRIBUTE_NAME_LOCAL_NAME), is(equalTo(SCANNABLE_NAME + "." + extraName)));
			assertThat(collection.getAttrString(extraName, GDADeviceNexusConstants.ATTRIBUTE_NAME_UNITS), is(equalTo(EXPECTED_UNITS)));
		}
	}

	private void checkDataGroup(final NXentry entry) {
		final Map<String, NXdata> dataGroups = entry.getAllData();
		assertThat(dataGroups.size(), is(1));
		assertThat(dataGroups.keySet(), contains(DETECTOR_NAME));

		final NXdata dataGroup = entry.getData(DETECTOR_NAME);
		assertThat(dataGroup, is(notNullValue()));

		final boolean singleInputField = inputNames.length == 1;
		final String[] inputFieldDataNodeNames = singleInputField ? new String[] { SCANNABLE_NAME } :
				Arrays.stream(inputNames).map(inputName -> SCANNABLE_NAME + "_" + inputName).toArray(String[]::new);
		final String[] expectedDataNodeNames = ArrayUtils.add(inputFieldDataNodeNames, NXdetector.NX_DATA);
		assertThat(dataGroup.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames));

		final DataNode detectorDataNode = dataGroup.getDataNode(NXdata.NX_DATA);
		assertThat(detectorDataNode, is(notNullValue()));
		assertThat(detectorDataNode, is(sameInstance(entry.getInstrument().getDetector(DETECTOR_NAME).getDataNode(NXdetector.NX_DATA))));

		final String[] standardAttributeNames = new String[] { NXCLASS, DATA_SIGNAL, DATA_AXES };
		final String[] indicesAttributeNames = Arrays.stream(inputFieldDataNodeNames).
				map(name -> name + DATA_INDICES_SUFFIX).toArray(String[]::new);
		final String[] expectedIndicesNames = Stream.of(standardAttributeNames, indicesAttributeNames).flatMap(Stream::of).toArray(String[]::new);
		assertThat(dataGroup.getAttributeNames(), containsInAnyOrder(expectedIndicesNames));

		assertSignal(dataGroup, NXdata.NX_DATA);
		assertAxes(dataGroup, inputFieldDataNodeNames[0]); // the first input field is the default axis
		for (int i = 0; i < inputNames.length; i++) {
			// the data nodes for each input field have the same name in the NXdata as the NXpositioner for that field
			final String axisDataNodeName = inputFieldDataNodeNames[i];
			final DataNode inputNameDataNode = dataGroup.getDataNode(inputFieldDataNodeNames[i]);
			assertThat(inputNameDataNode, is(notNullValue()));
			final String positionerName = singleInputField ? SCANNABLE_NAME : SCANNABLE_NAME + "." + inputNames[i];
			assertThat(inputNameDataNode, is(sameInstance(entry.getInstrument().getPositioner(positionerName).getDataNode(NXpositioner.NX_VALUE))));
			assertIndices(dataGroup, axisDataNodeName, 0);
		}
	}

}
