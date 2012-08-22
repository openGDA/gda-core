/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.addetectorprovisional;

import static org.apache.commons.lang.ArrayUtils.addAll;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.addetector.ADDetectorTest;
import gda.device.detector.addetectorprovisional.plugin.ADBasicStats;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataDoubleAppender;
import gda.device.detector.nxdata.NXDetectorDataFileAppenderForSrs;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

// TODO No tests for array readout!

public class ProvisionalADDetectorTest extends ADDetectorTest {

	private static final double ACQUIRE_TIME = 0.5;
	private static final double ACQUIRE_PERIOD = 0.55;
	@Mock
	private ADDetectorPlugin adDetectorPlugin1;
	@Mock
	private ADDetectorPlugin adDetectorPlugin2;
	@Mock
	private ADDetectorPlugin statsPlugin;
	@Mock
	private ADDetectorPlugin centroidPlugin;
	private ADBasicStats adBasicStats;

	private ProvisionalADDetector adDet;

	private static String[] PLUGIN1_NAMES = new String[] { "1a", "1b", "1c" };
	private static String[] PLUGIN2_NAMES = new String[] { "2a", "2b" };
	private static String[] PLUGIN1_FORMATS = new String[] { "%.1f", "%.2f", "%.3f" };
	private static String[] PLUGIN2_FORMATS = new String[] { "%.4f", "%.5f" };

	@Override
	public ProvisionalADDetector det() {
		return adDet;
	}

	@Override
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		adBasicStats = new ADBasicStats(ndStats);
		when(ndStats.getPluginBase()).thenReturn(ndStatsBase);
		adDet = new ProvisionalADDetector("testdet", adBase, collectionStrategy, fileWriter);
		det().setAdArrayPlugin(new ADArrayPlugin(ndArray));
		det().getAdArrayPlugin().setEnabled(true);
		det().setAdditionalPluginList(Arrays.asList((ADDetectorPlugin) adBasicStats));

		when(adDetectorPlugin1.getInputStreamExtraNames()).thenReturn(Arrays.asList(PLUGIN1_NAMES));
		when(adDetectorPlugin2.getInputStreamExtraNames()).thenReturn(Arrays.asList(PLUGIN2_NAMES));
		when(adDetectorPlugin1.getInputStreamFormats()).thenReturn(Arrays.asList(PLUGIN1_FORMATS));
		when(adDetectorPlugin2.getInputStreamFormats()).thenReturn(Arrays.asList(PLUGIN2_FORMATS));

		when(statsPlugin.getInputStreamFormats()).thenReturn(Arrays.asList(STATS_FORMATS));
		when(centroidPlugin.getInputStreamFormats()).thenReturn(Arrays.asList(CENTROID_FORMATS));
		when(ndArray.getPluginBase()).thenReturn(ndArrayBase);
		when(fileWriter.getInputStreamExtraNames()).thenReturn(Arrays.asList("filepath"));
		when(fileWriter.getInputStreamFormats()).thenReturn(Arrays.asList("%.2f"));
		Vector<NXDetectorDataAppender> dataAppenders = new Vector<NXDetectorDataAppender>();
		dataAppenders.add(new NXDetectorDataNullAppender());
		when(fileWriter.read(anyInt())).thenReturn(dataAppenders);

		when(collectionStrategy.getNumberImagesPerCollection(anyDouble())).thenReturn(1);

		enableReadAcquisitionTimeAndPeriod(true, false);
		enableFileWriter(false);
		enableStatsAndCentroid(false, false);

	}

	@Override
	protected void setUpNoConfigure() throws Exception {
		// ProvisionalADDetector constructor also configures
	}

	@Override
	protected void enableStatsAndCentroid(boolean computeStats, boolean computeCentroid) {
		adBasicStats.setComputeStats(computeStats);
		adBasicStats.setComputeCentroid(computeCentroid);
	}

	@Override
	protected void enableReadAcquisitionTimeAndPeriod(boolean enableTime, boolean enablePeriod) {

		List<String> fieldNames = new ArrayList<String>();
		List<String> formats = new ArrayList<String>();
		List<Double> times = new ArrayList<Double>();

		if (enableTime) {
			fieldNames.add("count_time");
			formats.add("%.2f");
			times.add(ACQUIRE_TIME);
		}
		if (enablePeriod) {
			fieldNames.add("period");
			formats.add("%.2f");
			times.add(ACQUIRE_PERIOD);

		}
		Vector<NXDetectorDataAppender> dataAppenders = new Vector<NXDetectorDataAppender>();
		dataAppenders.add(new NXDetectorDataDoubleAppender(fieldNames, times));
		when(collectionStrategy.getInputStreamExtraNames()).thenReturn(fieldNames);
		when(collectionStrategy.getInputStreamFormats()).thenReturn(formats);
		try {
			when(collectionStrategy.read(anyInt())).thenReturn(dataAppenders);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void enableFileWriter(boolean enableFileWriter) {
		if (enableFileWriter) {
			det().setFileWriter(fileWriter);
		} else {
			det().setFileWriter(null);
		}
	}

	@Override
	protected void enableArrayReadout(boolean enableArrayReadout) {
		det().getAdArrayPlugin().setEnabled(enableArrayReadout);
	}

	@Override
	@Test
	@Ignore
	public void testReadsArrayByDefault() throws Exception {
		super.testReadsArrayByDefault();
	}

	@Override
	@Test
	@Ignore
	public void testAtScanStartAllOffDisableCallbacks() throws Exception {
		super.testAtScanStartAllOffDisableCallbacks();
	}

	@Test
	public void testGetInputNamesAreAlwaysEmpty() {
		assertArrayEquals(new String[] {}, det().getInputNames());
		enableReadAcquisitionTimeAndPeriod(false, false);
		assertArrayEquals(new String[] {}, det().getInputNames());
	}

	@Override
	protected void setupForReadoutAndGetPositionWithFilenameAndTimes() throws Exception {
		super.setupForReadoutAndGetPositionWithFilenameAndTimes();
		Vector<NXDetectorDataAppender> dataAppenders = new Vector<NXDetectorDataAppender>();
		dataAppenders.add(new NXDetectorDataFileAppenderForSrs("/full/path/to/file99.cbf", "filepath"));
		when(fileWriter.read(anyInt())).thenReturn(dataAppenders);
	}

	@Override
	@Test
	public void testReadoutWithFilename() throws Exception {
		Vector<NXDetectorDataAppender> dataAppenders = new Vector<NXDetectorDataAppender>();
		dataAppenders.add(new NXDetectorDataFileAppenderForSrs("/full/path/to/file99.cbf", "filepath"));
		when(fileWriter.read(anyInt())).thenReturn(dataAppenders);
		super.testReadoutWithFilename();
	}

	@Test
	public void testGetExtraNamesNoPlugins() {
		assertArrayEquals(new String[] { "count_time" }, det().getExtraNames());
		enableReadAcquisitionTimeAndPeriod(true, true);
		assertArrayEquals(new String[] { "count_time", "period" }, det().getExtraNames());
		enableFileWriter(true);
		assertArrayEquals(new String[] { "count_time", "period", "filepath" }, det().getExtraNames());
	}

	@Test
	public void testGetExtraNamesOnePlugin() {
		enableReadAcquisitionTimeAndPeriod(false, false);
		det().setAdditionalPluginList(asList(adDetectorPlugin1));
		assertArrayEquals(PLUGIN1_NAMES, det().getExtraNames());
	}

	@Test
	public void testGetExtraNamesTwoPluginAndCounttime() {
		det().setAdditionalPluginList(asList(adDetectorPlugin1, adDetectorPlugin2));
		String[] expected = (String[]) addAll(new String[] { "count_time" }, addAll(PLUGIN1_NAMES, PLUGIN2_NAMES));
		assertArrayEquals(expected, det().getExtraNames());
	}

	@Test
	public void testGetOutputFormatTwoPlugins() {
		det().setAdditionalPluginList(asList(adDetectorPlugin1, adDetectorPlugin2));
		String[] PLUGIN_FORMATS = (String[]) addAll(PLUGIN1_FORMATS, PLUGIN2_FORMATS);

		enableReadAcquisitionTimeAndPeriod(false, false);
		assertArrayEquals(addAll(new String[] {}, PLUGIN_FORMATS), det().getOutputFormat());

		enableReadAcquisitionTimeAndPeriod(true, false);
		enableFileWriter(true);
		enableReadAcquisitionTimeAndPeriod(true, true);
		// the 3rd value in position is a number that represents the file.
		assertArrayEquals(addAll(new String[] { "%.2f", "%.2f", "%.2f" }, PLUGIN_FORMATS), det().getOutputFormat());
		enableFileWriter(false);
	}

	//
	@Test
	public void testReadoutNoPlugins() throws DeviceException {
		det().atScanStart();
		enableReadAcquisitionTimeAndPeriod(false, false);
		NXDetectorData data = (NXDetectorData) det().readout();
		Double[] doubleVals = data.getDoubleVals();
		assertEquals("", data.toString());
		assertArrayEquals(new Double[] {}, doubleVals);
	}

	@Test
	public void testReadoutOnePlugin() throws Exception { // TODO: Two next
		enableReadAcquisitionTimeAndPeriod(false, false);
		det().setAdditionalPluginList(asList(adDetectorPlugin1));

		Vector<NXDetectorDataAppender> dataAppenders = new Vector<NXDetectorDataAppender>();
		dataAppenders.add(new NXDetectorDataDoubleAppender(Arrays.asList(PLUGIN1_NAMES), Arrays.asList(0., 1., 2.)));
		when(adDetectorPlugin1.read(anyInt())).thenReturn(dataAppenders);

		det().atScanStart();
		NXDetectorData readout = (NXDetectorData) det().readout();
		assertArrayEquals(new Double[] { 0., 1., 2. }, readout.getDoubleVals());
		assertEquals("0.0\t1.00\t2.000", readout.toString());

		INexusTree rootNode = readout.getNexusTree().getChildNode(0);
		assertEquals("testdet", rootNode.getName());
		assertArrayEquals(new double[] { 0. }, (double[]) rootNode.getChildNode("1a", "SDS").getData().getBuffer(),
				.001);
		assertArrayEquals(new double[] { 1. }, (double[]) rootNode.getChildNode("1b", "SDS").getData().getBuffer(),
				.001);
		assertArrayEquals(new double[] { 2. }, (double[]) rootNode.getChildNode("1c", "SDS").getData().getBuffer(),
				.001);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetPositionCallableTwoPluginsReturningInChunks() throws Exception { // TODO: Two next
		enableReadAcquisitionTimeAndPeriod(false, false);
		det().setAdditionalPluginList(asList(adDetectorPlugin1, adDetectorPlugin2));

		Vector<NXDetectorDataAppender> dataAppenders1 = new Vector<NXDetectorDataAppender>();
		dataAppenders1.add(new NXDetectorDataDoubleAppender(Arrays.asList(PLUGIN1_NAMES), Arrays.asList(0., 1., 2.)));
		dataAppenders1
				.add(new NXDetectorDataDoubleAppender(Arrays.asList(PLUGIN1_NAMES), Arrays.asList(10., 11., 12.)));
		dataAppenders1
				.add(new NXDetectorDataDoubleAppender(Arrays.asList(PLUGIN1_NAMES), Arrays.asList(20., 21., 22.)));
		dataAppenders1
				.add(new NXDetectorDataDoubleAppender(Arrays.asList(PLUGIN1_NAMES), Arrays.asList(30., 31., 32.)));
		when(adDetectorPlugin1.read(anyInt())).thenReturn(dataAppenders1);
		Vector<NXDetectorDataAppender> dataAppenders2 = new Vector<NXDetectorDataAppender>();
		dataAppenders2.add(new NXDetectorDataDoubleAppender(Arrays.asList(PLUGIN2_NAMES), Arrays.asList(3., 4.)));
		dataAppenders2.add(new NXDetectorDataDoubleAppender(Arrays.asList(PLUGIN2_NAMES), Arrays.asList(13., 14.)));
		dataAppenders2.add(new NXDetectorDataDoubleAppender(Arrays.asList(PLUGIN2_NAMES), Arrays.asList(23., 24.)));
		dataAppenders2.add(new NXDetectorDataDoubleAppender(Arrays.asList(PLUGIN2_NAMES), Arrays.asList(33., 34.)));
		when(adDetectorPlugin2.read(anyInt())).thenReturn(dataAppenders2);

		det().atScanStart();
		Callable<NXDetectorData> readout0 = (Callable<NXDetectorData>) (Callable<?>) det().getPositionCallable();
		assertArrayEquals(new Double[] { 0., 1., 2., 3., 4. }, readout0.call().getDoubleVals());
		Callable<NXDetectorData> readout1 = (Callable<NXDetectorData>) (Callable<?>) det().getPositionCallable();
		Callable<NXDetectorData> readout2 = (Callable<NXDetectorData>) (Callable<?>) det().getPositionCallable();
		assertArrayEquals(new Double[] { 10., 11., 12., 13., 14. }, readout1.call().getDoubleVals());
		assertArrayEquals(new Double[] { 20., 21., 22., 23., 24. }, readout2.call().getDoubleVals());
		Callable<NXDetectorData> readout3 = (Callable<NXDetectorData>) (Callable<?>) det().getPositionCallable();
		assertArrayEquals(new Double[] { 30., 31., 32., 33., 34. }, readout3.call().getDoubleVals());
	}

	@Override
	@Test
	public void testReadoutStats() throws Exception {
		Vector<NXDetectorDataAppender> dataAppenders = new Vector<NXDetectorDataAppender>();
		dataAppenders.add(new NXDetectorDataDoubleAppender(Arrays.asList(STATS_NAMES), Arrays.asList(0., 1., 2., 3.,
				4., 5.)));
		when(statsPlugin.read(anyInt())).thenReturn(dataAppenders);
		det().atScanStart();
		super.testReadoutStats();
	}

	@Override
	@Test
	public void testReadoutCentroid() throws Exception {
		Vector<NXDetectorDataAppender> dataAppenders = new Vector<NXDetectorDataAppender>();
		dataAppenders.add(new NXDetectorDataDoubleAppender(Arrays.asList(CENTROID_NAMES), Arrays.asList(0., 1., 2., 3.,
				4.)));
		when(centroidPlugin.read(anyInt())).thenReturn(dataAppenders);
		det().atScanStart();
		super.testReadoutCentroid();
	}

	@Override
	@Test
	public void testConfigure() throws Exception {
		// ProvisionalADDetector constructor also configures
	}

	@Override
	@Test
	public void testConstructor() throws Exception {
		// TODO write testConstructor
	}

	@Override
	@Test(expected = RuntimeException.class)
	public void testReset() throws Exception {
		det().reset();
	}

	@Override
	@Test
	public void testGetStatusWithAdBaseFault() throws Exception {
		when(collectionStrategy.getStatus()).thenReturn(Detector.FAULT);
		assertEquals(Detector.FAULT, det().getStatus());
	}

	@Override
	@Test
	@Ignore
	// TODO testAtScanStartAllOff
	public void testAtScanStartAllOff() throws Exception {
		enableStatsAndCentroid(false, false);
		enableArrayReadout(false);
		det().atScanStart();
		verify(adBase).setArrayCallbacks((short) 1);
		verify(ndArrayBase).disableCallbacks();
		verify(ndArrayBase).setBlockingCallbacks((short) 0);
		// verify(ndStats).setComputeStatistics((short) 0);
		// verify(ndStats).setComputeCentroid((short) 0);
		// verify(ndStatsBase).setBlockingCallbacks((short) 0);
	}

	@Override
	@Test
	public void testAtScanStart() throws Exception {
		enableStatsAndCentroid(true, true);
		enableArrayReadout(true);
		det().setCollectionTime(1.);
		det().atScanStart();
		verify(adBase).setArrayCallbacks((short) 1);
		verify(ndArrayBase).enableCallbacks();
		verify(ndArrayBase).setBlockingCallbacks((short) 1);
		verify(collectionStrategy).prepareForCollection(1., 1);
	}

	@Override
	@Test
	public void testReadoutTimes() throws Exception {
		enableReadAcquisitionTimeAndPeriod(true, true);
		det().atScanStart();
		NXDetectorData readout = (NXDetectorData) det().readout();
		assertArrayEquals(new Double[] { 0.5, 0.55 }, readout.getDoubleVals());
		assertEquals("0.50\t0.55", readout.toString());

		INexusTree rootNode = readout.getNexusTree().getChildNode(0);
		assertArrayEquals(new double[] { 0.5 }, (double[]) rootNode.getChildNode("count_time", "SDS").getData()
				.getBuffer(), .001);
		assertArrayEquals(new double[] { 0.55 }, (double[]) rootNode.getChildNode("period", "SDS").getData()
				.getBuffer(), .001);

	}

	@Override
	@Test
	public void testReadoutNoArrayStatsOrCentroid() throws DeviceException {
		det().atScanStart();
		super.testReadoutNoArrayStatsOrCentroid();
	}

	@Override
	@Test
	public void testReadoutArray() throws Exception {
		det().atScanStart();
		super.testReadoutArray();
	}

	@Override
	@Test
	public void testStop() throws Exception {
		det().stop();
		verify(collectionStrategy).stop();

	}

	static <T> List<T> asList(T arg1) {
		ArrayList<T> arrayList = new ArrayList<T>();
		arrayList.add(arg1);
		return arrayList;
	}

	static <T> List<T> asList(T arg1, T arg2) {
		ArrayList<T> arrayList = new ArrayList<T>();
		arrayList.add(arg1);
		arrayList.add(arg2);
		return arrayList;
	}

}