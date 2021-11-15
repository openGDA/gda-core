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
import java.util.function.DoubleFunction;
import java.util.stream.IntStream;

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
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.builder.impl.DefaultNexusBuilderFactory;
import org.eclipse.dawnsci.nexus.device.impl.NexusDeviceService;
import org.eclipse.dawnsci.nexus.scan.impl.NexusScanFileServiceImpl;
import org.eclipse.dawnsci.nexus.template.impl.NexusTemplateServiceImpl;
import org.eclipse.scanning.device.Services;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.python.core.PyArray;
import org.python.core.PyFloat;
import org.python.core.PyTuple;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.data.ServiceHolder;
import gda.data.scan.nexus.device.GDANexusDeviceAdapterFactory;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.DummyScannable;
import gda.device.scannable.ScannableUtils;
import gda.device.scannable.scannablegroup.DummyScannableFieldScannableMotion;
import gda.scan.ConcurrentScan;
import uk.ac.diamond.daq.scanning.ScannableDeviceConnectorService;

public class HKLScanTest {

	private static class HKLScan {
		public Double[] s1ScannableArgs;
		public Scannable scannable;
		public Double[] start;
		public Double[] stop;
		public Double[] step;
		public PyTuple points;
		public String expectedDefaultAxisName;

		public int getExpectedNumPoints() throws Exception {
			if (s1ScannableArgs != null) {
				return ScannableUtils.getNumberSteps(s1ScannableArgs[0], s1ScannableArgs[1], s1ScannableArgs[2]) + 1;
			}
			if (points != null) return points.size();
			return ScannableUtils.getNumberSteps(scannable, start, stop, step) + 1;
		}

		public int[] getExpectedShape() throws Exception {
			return new int[] { getExpectedNumPoints() };
		}

	}

	private static final String ATTR_NAME_GDA_FIELD_NAME = "gda_field_name";
	private static final String ATTR_NAME_LOCAL_NAME = "local_name";
	private static final String ATTR_NAME_TARGET = "target";

	private static final String HKL_SCANNABLE_NAME = "hkl";
	private static final String S1_SCANNABLE_NAME = "s1";
	private static final String DETECTOR_NAME = "det1";

	private static final String FIELD_NAME_H  = "h";
	private static final String FIELD_NAME_K  = "k";
	private static final String FIELD_NAME_L = "l";

	private static final String[] INPUT_NAMES = { FIELD_NAME_H, FIELD_NAME_K, FIELD_NAME_L };
	private static final String[] EXTRA_NAMES = { "p", "q", "r" };

	private String outputDir;

	private Scannable hklScannable;
	private Scannable s1Scannable;

	private Detector detector;

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

	@Before
	public void setUp() throws Exception {
		hklScannable = new DummyScannableFieldScannableMotion(
				HKL_SCANNABLE_NAME, INPUT_NAMES, EXTRA_NAMES);
		s1Scannable = new DummyScannable(S1_SCANNABLE_NAME);

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

	@After
	public void tearDown() {
		LocalProperties.clearProperty(GDA_DATA_SCAN_DATAWRITER_DATAFORMAT);
	}

	@Test
	public void testHKLScan_h() throws Exception {
		setUpTest("testHKLScan_h");
		testHKLScan(FIELD_NAME_H, 0, 5, 1);
	}

	@Test
	public void testHKLScan_k() throws Exception {
		setUpTest("testHKLScan_k");
		testHKLScan(FIELD_NAME_K, 0, 5, 1);
	}

	@Test
	public void testHKLScan_l() throws Exception {
		setUpTest("testHKLScan_l");
		testHKLScan(FIELD_NAME_L, 0, 5, 1);
	}

	@Test
	public void testHKLScan_k_backward() throws Exception {
		setUpTest("testHKLScan_k");
		testHKLScan(FIELD_NAME_K, 5, 0, 1);
	}

	@Test
	public void testHKLScan_moveMultiple() throws Exception {
		setUpTest("testHKLScan_moveMultiple");
		final HKLScan hklScan = new HKLScan();
		hklScan.scannable = hklScannable;
		hklScan.start = new Double[] { 0.0, 0.0, 0.0 };
		hklScan.stop = new Double[] { 5.0, 25.0, 10.0 };
		hklScan.step = new Double[] { 1.0, 5.0, 2.0};
		hklScan.expectedDefaultAxisName = FIELD_NAME_K;
		testHKLScan(hklScan);
	}

	@Test
	public void testHKLScan_moveMultipleBackwards() throws Exception {
		setUpTest("testHKLScan_moveMultipleBackwards");
		final HKLScan hklScan = new HKLScan();
		hklScan.scannable = hklScannable;
		hklScan.stop = new Double[] { 0.0, 0.0, 0.0 };
		hklScan.start = new Double[] { -5.0, -25.0, -10.0 };
		hklScan.step = new Double[] { -1.0, -5.0, -2.0};
		hklScan.expectedDefaultAxisName = FIELD_NAME_K;
		testHKLScan(hklScan);
	}

	private void testHKLScan(String fieldToMove, double start, double stop, double step) throws Exception {
		final HKLScan hklScan = createHKLScan(fieldToMove, start, stop, step);
		testHKLScan(hklScan);
	}

	@Test
	public void testHKLScanStartStep() throws Exception {
		// since step is not specified, we add another scannable, s1Scannable as the first
		// this means that s1Scannable is the default axis in the NXdata group rather than
		// any of the fields of the hkl scannable, so only one test for this case is required
		setUpTest("testHKLScanStartStep");
		final HKLScan hklScan = createHKLScan(FIELD_NAME_H, 0, null, 0.1);
		testHKLScan(hklScan);
	}

	@Test
	public void testHKLScanPoints_h() throws Exception {
		setUpTest("testHKLScanPoints_h");
		testHKLScanPoints(FIELD_NAME_H, new double[] { 1.0, 4.0, 6.0 });
	}

	@Test
	public void testHKLScanPoints_k() throws Exception {
		setUpTest("testHKLScanPoints_k");
		testHKLScanPoints(FIELD_NAME_K, new double[] { 1.0, 2.0, 3.5, 7.0, 2.3 });
	}

	@Test
	public void testHKLScanPoints_l() throws Exception {
		setUpTest("testHKLScanPoints_l");
		testHKLScanPoints(FIELD_NAME_L, new double[] { 1.0, 2.0, 3.0, 4.0 });
	}

	@Test
	public void testHKLScanPoints_moveMultiple() throws Exception {
		setUpTest("testHKLScanPoints_moveMultiple");
		final double[][] points = new double[3][];
		points[0] = new double[] { 0.0, 0.0, 0.0 };
		points[1] = new double[] { 0.0, 1.0, 3.5 };
		points[2] = new double[] { 0.0, 2.0, 7.0 };

		testHKLScanPoints(FIELD_NAME_L, points);
	}

	@Test
	public void testHKLScanPoints_moveMultipleBackwards() throws Exception {
		setUpTest("testHKLScanPoints_moveMultipleBackwards");
		final double[][] points = new double[3][];
		points[0] = new double[] { 0.0, 6.0, 8.0 };
		points[1] = new double[] { 0.0, 1.0, 7.0 };
		points[2] = new double[] { 0.0, -4.0, 6.0 };

		testHKLScanPoints(FIELD_NAME_K, points);
	}

	private void testHKLScanPoints(String fieldToMove, double[] points) throws Exception {
		final DoubleFunction<double[]> toPosition = point -> IntStream.range(0, INPUT_NAMES.length)
				.mapToDouble(i -> INPUT_NAMES[i].equals(fieldToMove) ? point : 0.0)
				.toArray(); // converts a point to position array

		final double[][] positions = Arrays.stream(points)
				.mapToObj(toPosition::apply)
				.toArray(double[][]::new);

		testHKLScanPoints(fieldToMove, positions);
	}

	private void testHKLScanPoints(String expectedDefaultAxisName, double[][] positions) throws Exception {
		final HKLScan hklScan = new HKLScan();
		hklScan.expectedDefaultAxisName = expectedDefaultAxisName;
		hklScan.scannable = hklScannable;
		hklScan.points = scanPointsToPyTuple(positions);
		testHKLScan(hklScan);
	}

	private PyTuple scanPointsToPyTuple(double[][] positions) {
		final PyArray[] pyPositions = Arrays.stream(positions).map(position -> {
			final PyArray pyArray = new PyArray(PyFloat.class, position.length);
			IntStream.range(0, position.length).forEach(i -> pyArray.__setitem__(i, new PyFloat(position[i])));
			return pyArray;
		}).toArray(PyArray[]::new);

		return new PyTuple(pyPositions);
	}

	private void testHKLScan(HKLScan hklScan) throws Exception {
		final Object[] scanArguments = createScanArguments(hklScan);
		final ConcurrentScan scan = new ConcurrentScan(scanArguments);
		final int numSteps = hklScan.getExpectedNumPoints();
		assertThat(scan.getScanInformation().getNumberOfPoints(), is(numSteps));
		assertThat(scan.getScanInformation().getDimensions(), is(equalTo(new int[] { numSteps })));

		scan.runScan();

		final File expectedNexusFile = new File(outputDir + "1.nxs");
		final String expectedNexusFilePath = expectedNexusFile.getAbsolutePath();
		assertThat(expectedNexusFile.exists(), is(true));

		// check the content of the nexus file
		try (final NexusFile nexusFile = openNexusFile(expectedNexusFilePath)) {
			checkNexusFile(nexusFile, hklScan);
		}
	}

	private HKLScan createHKLScan(String fieldToMove, double start, Double stop, double step) {
		// Note, ConcurrentScan can only deal with Double[] or PyList, not double[], Object[] or List<?>
		// TODO: try to fix this? Or create JIRA ticket. See ConcurrentScanTest.testMultielementScannables
		final HKLScan hklScan = new HKLScan();
		hklScan.scannable = hklScannable;
		hklScan.start = new Double[INPUT_NAMES.length];
		hklScan.stop = stop == null ? null : new Double[INPUT_NAMES.length];
		hklScan.step = new Double[INPUT_NAMES.length];
		hklScan.expectedDefaultAxisName = fieldToMove;

		for (int i = 0; i < INPUT_NAMES.length; i++) {
			final boolean isFieldToMove = INPUT_NAMES[i].equals(fieldToMove);
			hklScan.start[i] = isFieldToMove ? start : 0.0;
			if (stop != null) hklScan.stop[i] = isFieldToMove ? stop : 0.0;
			hklScan.step[i] = isFieldToMove ? step : 0.0;
		}

		if (stop == null) {
			hklScan.s1ScannableArgs = new Double[] { 0.0, 1.0, 0.1 };
		}

		return hklScan;
	}

	private Object[] createScanArguments(HKLScan hklScan) {
		final List<Object> arguments = new ArrayList<>();

		if (hklScan.s1ScannableArgs != null) {
			// when the hkl scannable doesn't have stop value and isn't a list of points,
			// we need another scannable first, creating an ImplicitScanObject with a fixed sized
			// so that the ImplicitScanObject for the hklScannable can be set to that size
			arguments.add(s1Scannable);
			arguments.addAll(Arrays.asList(hklScan.s1ScannableArgs));
		}

		arguments.add(hklScannable);

		if (hklScan.points != null) {
			arguments.add(hklScan.points);
		} else {
			arguments.add(hklScan.start);
			if (hklScan.stop != null) arguments.add(hklScan.stop);
			arguments.add(hklScan.step);
		}

		// use a monitor instead of a detector for simplicity. This will be the @signal field of the NXdata
		arguments.add(detector);

		return arguments.toArray();
	}

	private void checkNexusFile(NexusFile nexusFile, HKLScan hklScan) throws Exception {
		final TreeFile nexusTree = NexusUtils.loadNexusTree(nexusFile);
		final NXroot nexusRoot = (NXroot) nexusTree.getGroupNode();
		assertThat(nexusRoot, is(notNullValue()));
		final NXentry entry = nexusRoot.getEntry();
		assertThat(entry, is(notNullValue()));

		final NXinstrument instrument = entry.getInstrument();
		assertThat(instrument, is(notNullValue()));

		// check the NXcollection for the hkl scannable
		final NXcollection hklCollection = instrument.getCollection(HKL_SCANNABLE_NAME);
		assertThat(hklCollection, is(notNullValue()));

		final String[] allFieldNames = ArrayUtils.addAll(INPUT_NAMES, EXTRA_NAMES);
		final String[] expectedDataNodeNames = ArrayUtils.add(allFieldNames, NXpositioner.NX_NAME);
		assertThat(hklCollection.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames));
		for (int i = 0; i < allFieldNames.length; i++) {
			final String fieldName = allFieldNames[i];
			final boolean isInputField = i < INPUT_NAMES.length;

			final DataNode dataNode = hklCollection.getDataNode(fieldName);
			assertThat(dataNode, is(notNullValue()));
			assertThat(dataNode.getDataset().getShape(), is(equalTo(hklScan.getExpectedShape())));

			String[] expectedAttributeNames = { ATTR_NAME_GDA_FIELD_NAME, ATTR_NAME_LOCAL_NAME };
			if (isInputField) expectedAttributeNames = ArrayUtils.add(expectedAttributeNames, ATTR_NAME_TARGET);
			assertThat(dataNode.getAttributeNames(), containsInAnyOrder(expectedAttributeNames));
			assertThat(dataNode.getAttribute(ATTR_NAME_GDA_FIELD_NAME).getFirstElement(), is(equalTo(fieldName)));
			assertThat(dataNode.getAttribute(ATTR_NAME_LOCAL_NAME).getFirstElement(),
					is(equalTo(HKL_SCANNABLE_NAME + NexusConstants.FIELD_SEPERATOR + fieldName)));

			if (isInputField) {
				final String positionerName = HKL_SCANNABLE_NAME + NexusConstants.FIELD_SEPERATOR + fieldName;
				final NXpositioner positioner = instrument.getPositioner(positionerName);
				assertThat(positioner, is(notNullValue()));
				assertThat(positioner.getDataNodeNames(), contains(NXpositioner.NX_NAME, NXpositioner.NX_VALUE));
				assertThat(positioner.getNameScalar(), is(equalTo(positionerName)));
				assertThat(positioner.getDataNode(NXpositioner.NX_VALUE), is(sameInstance(dataNode)));
			}
		}

		// check the NXdata group
		assertThat(entry.getAllData().keySet(), contains(DETECTOR_NAME));
		final NXdata dataGroup = entry.getData(DETECTOR_NAME);
		assertThat(dataGroup, is(notNullValue()));
		final String[] hklLinkedFieldNames = Arrays.stream(INPUT_NAMES).map(fieldName -> HKL_SCANNABLE_NAME + NexusConstants.FIELD_SEPERATOR + fieldName).toArray(String[]::new);
		String[] expectedDataGroupFieldNames = ArrayUtils.add(hklLinkedFieldNames, NXdetector.NX_DATA);
		if (hklScan.s1ScannableArgs != null) {
			expectedDataGroupFieldNames = ArrayUtils.add(expectedDataGroupFieldNames, S1_SCANNABLE_NAME);
		}

		assertThat(dataGroup.getDataNodeNames(), containsInAnyOrder(expectedDataGroupFieldNames));

		assertSignal(dataGroup, NXdetector.NX_DATA);
		final String expectedAxisName = hklScan.s1ScannableArgs != null ? S1_SCANNABLE_NAME :
				HKL_SCANNABLE_NAME + NexusConstants.FIELD_SEPERATOR + hklScan.expectedDefaultAxisName;
		assertAxes(dataGroup, expectedAxisName);
		for (int i = 0; i < INPUT_NAMES.length; i++) {
			final String inputFieldName = INPUT_NAMES[i];
			final String dataFieldName = hklLinkedFieldNames[i];
			final DataNode dataNode = dataGroup.getDataNode(dataFieldName);
			assertThat(dataNode, is(notNullValue()));
			assertThat(dataNode, is(sameInstance(hklCollection.getDataNode(inputFieldName))));
			assertIndices(dataGroup, dataFieldName, 0); // each hkl field maps to the first dimension of the signal field
			assertTarget(dataGroup, dataFieldName, nexusRoot, "/entry/instrument/" + HKL_SCANNABLE_NAME + "/" + inputFieldName);
		}
	}

}
