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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.anyInt;
import gda.data.nexus.tree.INexusTree;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.addetector.ADDetectorTest;
import gda.device.detector.addetector.filewriter.FileWriter;
import gda.device.detector.addetector.triggering.ADTriggeringStrategy;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.areadetector.v17.NDStats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;



public class ProvisionalADDetectorTest extends ADDetectorTest{

	@Mock private ADDetectorPlugin<Double[]> adDetectorPlugin1;
	@Mock private ADDetectorPlugin<Double[]> adDetectorPlugin2;
	@Mock private ADDetectorPlugin<Double[]> statsPlugin;
	@Mock private ADDetectorPlugin<Double[]> centroidPlugin;
	
	private ProvisionalADDetector adDet;
	
	private static String[] PLUGIN1_NAMES = new String[] { "1a", "1b", "1c" };
	private static String[] PLUGIN2_NAMES = new String[] { "2a", "2b"};
	private static String[] PLUGIN1_FORMATS = new String[] { "%.1f", "%.2f", "%.3f" };
	private static String[] PLUGIN2_FORMATS = new String[] { "%.4f", "%.5f"};

	@Override
	public ProvisionalADDetector det() {
		return adDet;
	}

	@Override
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(ndStats.getPluginBase()).thenReturn(ndStatsBase);
		adDet = new ProvisionalADDetector("testdet", adBase, collectionStrategy, fileWriter);
		det().setNdArray(ndArray);
		when(adDetectorPlugin1.getInputStreamFieldNames()).thenReturn(Arrays.asList(PLUGIN1_NAMES));
		when(adDetectorPlugin2.getInputStreamFieldNames()).thenReturn(Arrays.asList(PLUGIN2_NAMES));
		when(adDetectorPlugin1.getInputStreamFormats()).thenReturn(Arrays.asList(PLUGIN1_FORMATS));
		when(adDetectorPlugin2.getInputStreamFormats()).thenReturn(Arrays.asList(PLUGIN2_FORMATS));
		
		when(statsPlugin.getInputStreamFieldNames()).thenReturn(Arrays.asList(STATS_NAMES));
		when(centroidPlugin.getInputStreamFieldNames()).thenReturn(Arrays.asList(CENTROID_NAMES));

		when(statsPlugin.getInputStreamFormats()).thenReturn(Arrays.asList(STATS_FORMATS));
		when(centroidPlugin.getInputStreamFormats()).thenReturn(Arrays.asList(CENTROID_FORMATS));
		when(ndArray.getPluginBase()).thenReturn(ndArrayBase);
		
	}

	@Override
	protected void setUpNoConfigure() throws Exception {
		// ProvisionalADDetector constructor also configures 
	}

	@Override
	protected void enableStatsAndCentroid(boolean computeStats, boolean computeCentroid) {
		ArrayList<ADDetectorPlugin<Double[]>> pluginList = new ArrayList<ADDetectorPlugin<Double[]>>();
		if (computeStats) {
			pluginList.add(statsPlugin);
		}
		if (computeCentroid) {
			pluginList.add(centroidPlugin);
		}
		det().setPluginList(pluginList);
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
		det().setReadAcquisitionTime(false);
		assertArrayEquals(new String[] {}, det().getInputNames());
	}

	@Test
	public void testGetExtraNamesNoPlugins() {
		assertArrayEquals(new String[] { "count_time" }, det().getExtraNames());
		det().setReadAcquisitionPeriod(true);
		assertArrayEquals(new String[] { "count_time", "period" }, det().getExtraNames());
		det().setReadFilepath(true);
		assertArrayEquals(new String[] { "count_time", "period", "filepath" }, det().getExtraNames());
	}

	@Test
	public void testGetExtraNamesOnePlugin() {
		det().setReadAcquisitionTime(false);
		det().setPluginList(asList(adDetectorPlugin1));
		assertArrayEquals(PLUGIN1_NAMES, det().getExtraNames());
	}
	@Test
	public void testGetExtraNamesTwoPluginAndCounttime() {
		det().setPluginList(asList(adDetectorPlugin1,adDetectorPlugin2));
		String[] expected = (String[]) addAll(new String[] { "count_time" } ,addAll(PLUGIN1_NAMES, PLUGIN2_NAMES));
		assertArrayEquals(expected, det().getExtraNames());
	}

	@Test
	public void testGetOutputFormatTwoPlugins() {
		det().setPluginList(asList(adDetectorPlugin1,adDetectorPlugin2));
		String[] PLUGIN_FORMATS = (String[]) addAll(PLUGIN1_FORMATS, PLUGIN2_FORMATS);
		
		det().setReadAcquisitionTime(false);
		det().setReadAcquisitionPeriod(false);
		assertArrayEquals(addAll(new String[] {}, PLUGIN_FORMATS), det().getOutputFormat());
	
		det().setReadAcquisitionTime(true);
		det().setReadFilepath(true);
		det().setReadAcquisitionPeriod(true);
		// the 3rd value in position is a number that represents the file.
		assertArrayEquals(addAll(new String[] { "%.2f", "%.2f", "%.2f" }, PLUGIN_FORMATS), det().getOutputFormat());
		det().setReadFilepath(false);
	}

	//
	@Test
	public void testReadoutNoPlugins() throws DeviceException {
		det().setReadAcquisitionTime(false);
		NXDetectorData data = (NXDetectorData) det().readout();
		assertEquals("", data.toString());
		Double[] doubleVals = data.getDoubleVals();
		assertArrayEquals(new Double[] { }, doubleVals);
	}

	@Test
	public void testReadoutOnePlugin() throws Exception { // TODO: Two next
		det().setReadAcquisitionTime(false);
		det().setPluginList(asList(adDetectorPlugin1));
		Vector<Double[]> vector = new Vector<Double[]>();
		vector.add(new Double[] {0., 1., 2.});
		when(adDetectorPlugin1.read(anyInt())).thenReturn(vector);
		
		NXDetectorData readout = (NXDetectorData) det().readout();
		assertArrayEquals(new Double[] { 0., 1., 2.}, readout.getDoubleVals());
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

	
	
	
	@Override
	@Test
	public void testReadoutStats() throws Exception {
		Vector<Double[]> vector = new Vector<Double[]>();
		vector.add(new Double[] {0., 1., 2., 3., 4., 5.});
		when(statsPlugin.read(anyInt())).thenReturn(vector);
		super.testReadoutStats();
	}
	
	@Override
	@Test
	public void testReadoutCentroid() throws Exception {
		Vector<Double[]> vector = new Vector<Double[]>();
		vector.add(new Double[] {0., 1., 2., 3., 4.});
		when(centroidPlugin.read(anyInt())).thenReturn(vector);
		super.testReadoutCentroid();
	}
	@Override
	@Test
	public void testConfigure() throws Exception {
		// ProvisionalADDetector constructor also configures 
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
		public void testAtScanStartAllOff() throws Exception {
			enableStatsAndCentroid(false, false);
			enableArrayReadout(false);
			det().atScanStart();
			verify(adBase).setArrayCallbacks((short) 1);
			verify(ndArrayBase).disableCallbacks();
			verify(ndArrayBase).setBlockingCallbacks((short) 0);
	//		verify(ndStats).setComputeStatistics((short) 0);
	//		verify(ndStats).setComputeCentroid((short) 0);
	//		verify(ndStatsBase).setBlockingCallbacks((short) 0);
		}

	@Override
	@Test
	public void testReadoutTimes() throws Exception {
		det().setReadAcquisitionPeriod(true);

		when(collectionStrategy.getAcquireTime()).thenReturn(0.5);
		when(collectionStrategy.getAcquirePeriod()).thenReturn(0.55);
		
		NXDetectorData readout = (NXDetectorData) det().readout();
		assertEquals("0.50\t0.55", readout.toString());
		assertArrayEquals(new Double[] { 0.5, 0.55 }, readout.getDoubleVals());

		INexusTree rootNode = readout.getNexusTree().getChildNode(0);
		assertArrayEquals(new double[] { 0.5 }, (double[]) rootNode.getChildNode("count_time", "SDS").getData()
				.getBuffer(), .001);
		assertArrayEquals(new double[] { 0.55 }, (double[]) rootNode.getChildNode("period", "SDS").getData()
				.getBuffer(), .001);

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