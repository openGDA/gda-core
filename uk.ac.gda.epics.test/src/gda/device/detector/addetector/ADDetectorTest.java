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

package gda.device.detector.addetector;

import static org.apache.commons.lang.ArrayUtils.addAll;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gda.data.ObservablePathProvider;
import gda.data.PathChanged;
import gda.data.nexus.tree.INexusTree;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.addetector.triggering.SingleExposureStandard;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.areadetector.v17.NDStats;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;


public class ADDetectorTest {

	private ADDetector adDet;
	protected ADBase mockAdBase;
	protected NDArray mockNdArray;
	protected NDStats mockNdStats;
	protected NDPluginBase mockNdArrayBase;
	protected NDPluginBase mockNdStatsBase;
	protected NDFile mockNdFile;
	private static String[] STATS_NAMES = new String[] { "min", "max", "total", "net", "mean", "sigma" };
	private static String[] CENTROID_NAMES = new String[] { "centroidX", "centroidY", "centroid_sigmaX",
			"centroid_sigmaY", "centroid_sigmaXY" };

	protected double READOUT_TIME = 0.1;
	
	public ADDetector det() {
		return adDet;
	}

	@Before
	
	public void setUp() throws Exception {
		mockAdBase = mock(ADBase.class);
		mockNdArray = mock(NDArray.class);
		mockNdStats = mock(NDStats.class);
		mockNdArrayBase = mock(NDPluginBase.class);
		mockNdStatsBase = mock(NDPluginBase.class);
		mockNdFile = mock(NDFile.class);
		setUpNoConfigure();
		det().configure();
	}

	protected void setUpNoConfigure() throws Exception {
		createDetector();
		det().setName("testdet");
		det().setAdBase(mockAdBase);
		det().setNdArray(mockNdArray);
		det().setNdStats(mockNdStats);
		det().setNdFile(mockNdFile);
		det().afterPropertiesSet();
		when(mockNdArray.getPluginBase()).thenReturn(mockNdArrayBase);
		when(mockNdStats.getPluginBase()).thenReturn(mockNdStatsBase);
	}

	@SuppressWarnings("unused")
	protected void createDetector() throws Exception {
		adDet = new ADDetector();
		adDet.setCollectionStrategy(new SingleExposureStandard(mockAdBase, 0.1)); // default strategy
	}

	@Test
	public void testConstructor() throws Exception {
		setUpNoConfigure();
		assertFalse(det().isComputeStats());
		assertFalse(det().isComputeCentroid());
		assertTrue(det().isReadArray());
		assertFalse(det().createsOwnFiles());
		assertTrue(det().isLocal());
		assertTrue(det().isReadAcquisitionTime());
		assertFalse(det().isReadAcquisitionPeriod());
		assertFalse(det().isReadFilepath());
	}
	
	@Test(expected=RuntimeException.class)
	public void testAsynchronousMoveTo() throws DeviceException {
		det().asynchronousMoveTo(1.);
	}
	
	@Test(expected=RuntimeException.class)
	public void testGetPosition() throws DeviceException {
		det().getPosition();
	}
	
	@Test(expected=RuntimeException.class)
	public void testIsBusy() {
		det().isBusy();
	}

	@Test
	public void testConfigure() throws Exception {
		setUpNoConfigure();
		det().configure();
		// duplicated below
		verify(mockAdBase, times(2)).reset();
		verify(mockNdArray, times(2)).reset();
		verify(mockNdStats, times(2)).reset();
		verify(mockNdFile, times(2)).reset();
	}

	@Test
	public void testReset() throws Exception {
		setUpNoConfigure();
		det().reset();
		verify(mockAdBase, times(2)).reset();
		verify(mockNdArray, times(2)).reset();
		verify(mockNdStats, times(2)).reset();
		verify(mockNdFile, times(2)).reset();
	}

	@Test
	public void testGetInputNames() {
		assertArrayEquals(new String[] {}, det().getInputNames());
		det().setReadAcquisitionTime(false);
		assertArrayEquals(new String[] {}, det().getInputNames());

	}

	@Test
	public void testGetExtraNamesNostats() {
		det().setComputeCentroid(false);
		det().setComputeStats(false);
		assertArrayEquals(new String[] {"count_time"}, det().getExtraNames());
		det().setReadAcquisitionPeriod(true);
		assertArrayEquals(new String[] {"count_time", "period" }, det().getExtraNames());
		det().setReadFilepath(true);
		assertArrayEquals(new String[] {"count_time", "period", "filepath" }, det().getExtraNames());
	}

	@Test
	public void testGetExtraNamesCentroid() {
		det().setComputeCentroid(true);
		det().setComputeStats(false);
		assertArrayEquals( ArrayUtils.addAll(new String[] {"count_time"}, CENTROID_NAMES), det().getExtraNames());
	}

	@Test
	public void testGetExtraNamesStats() {
		det().setComputeCentroid(false);
		det().setComputeStats(true);
		assertArrayEquals(ArrayUtils.addAll(new String[] {"count_time"}, STATS_NAMES), det().getExtraNames());
	}

	@Test
	public void testGetExtraNamesStatsAndCentroidAndPeriod() {
		det().setComputeCentroid(true);
		det().setComputeStats(true);
		det().setReadAcquisitionPeriod(true);
		assertArrayEquals(addAll(new String[] { "count_time", "period" }, addAll(STATS_NAMES, CENTROID_NAMES)), det().getExtraNames());
	}

	@Test
	public void testGetOutputFormat() {
		det().setReadAcquisitionTime(false);
		det().setReadAcquisitionPeriod(false);
		det().setComputeStats(false);
		det().setComputeCentroid(false);
		assertArrayEquals(new String[] {}, det().getOutputFormat());

		det().setReadAcquisitionTime(true);
		assertArrayEquals(new String[] { "%.2f" }, det().getOutputFormat());

		det().setReadFilepath(true);
		//the 2nd value in position is a number that represents the file.
		assertArrayEquals(new String[] { "%.2f", "%.2f" }, det().getOutputFormat());
		det().setReadFilepath(false);

		det().setReadAcquisitionPeriod(true);
		assertArrayEquals(new String[] { "%.2f", "%.2f" }, det().getOutputFormat());

		det().setReadFilepath(true);
		det().setReadAcquisitionPeriod(true);
		//the 3rd value in position is a number that represents the file.
		assertArrayEquals(new String[] { "%.2f", "%.2f",  "%.2f" }, det().getOutputFormat());
		det().setReadFilepath(false);

		det().setComputeStats(true);
		assertArrayEquals(new String[] { "%.2f", "%.2f", "%5.5g", "%5.5g", "%5.5g", "%5.5g", "%5.5g", "%5.5g" },
				det().getOutputFormat());

		det().setComputeCentroid(true);
		assertArrayEquals(new String[] { "%.2f", "%.2f", "%5.5g", "%5.5g", "%5.5g", "%5.5g", "%5.5g", "%5.5g", "%5.5g",
				"%5.5g", "%5.5g", "%5.5g", "%5.5g" }, det().getOutputFormat());
	}

	@Test
	public void testStatusInitially() throws Exception {
		when(mockNdArrayBase.getUniqueId_RBV()).thenReturn(100);
		when(mockNdStatsBase.getUniqueId_RBV()).thenReturn(200);
		assertEquals(Detector.IDLE, det().getStatus());
	}

	@Test
	public void testAtScanStart() throws Exception {

		det().setComputeCentroid(true);
		det().setComputeStats(true);
		det().setReadArray(true);
		det().setCollectionTime(1.);
		det().atScanStart();
		verify(mockAdBase).setArrayCallbacks((short) 1);
		verify(mockNdArrayBase).enableCallbacks();
		verify(mockNdArrayBase).setBlockingCallbacks((short) 1);
		verify(mockNdStats).setComputeStatistics((short) 1);
		verify(mockNdStats).setComputeCentroid((short) 1);
		verify(mockNdStatsBase).setBlockingCallbacks((short) 1);
		verify(mockAdBase).setAcquireTime(1.);
		verify(mockAdBase).setAcquirePeriod(1+ READOUT_TIME);
	}

	@Test
	public void testAtScanStartAllOff() throws Exception {
		det().setComputeCentroid(false);
		det().setComputeStats(false);
		det().setReadArray(false);
		det().atScanStart();
		verify(mockAdBase).setArrayCallbacks((short) 1);
		verify(mockNdArrayBase).disableCallbacks();
		verify(mockNdArrayBase).setBlockingCallbacks((short) 0);
		verify(mockNdStats).setComputeStatistics((short) 0);
		verify(mockNdStats).setComputeCentroid((short) 0);
		verify(mockNdStatsBase).setBlockingCallbacks((short) 0);
	}

	@Test
	public void testAtScanStartAllOffDisableCallbacks() throws Exception {
		det().setReadArray(false);
		det().setDisableCallbacks(true);
		det().atScanStart();
		verify(mockAdBase).setArrayCallbacks((short) 0);
		verify(mockNdArrayBase).disableCallbacks();
		verify(mockNdArrayBase).setBlockingCallbacks((short) 0);
		verify(mockNdStats).setComputeStatistics((short) 0);
		verify(mockNdStats).setComputeCentroid((short) 0);
		verify(mockNdStatsBase).setBlockingCallbacks((short) 0);
	}

	
	
	@Test
	public void testCollectData() throws Exception {
		det().setComputeCentroid(true);
		det().setComputeStats(true);
		det().setReadArray(true);
		when(mockNdArrayBase.getUniqueId_RBV()).thenReturn(100);
		when(mockNdStatsBase.getUniqueId_RBV()).thenReturn(200);

		det().setCollectionTime(1);
		det().collectData();
		when(mockAdBase.getStatus()).thenReturn(Detector.BUSY);


		verify(mockAdBase).startAcquiring();
	}

	@Test
	public void testCollectDataPutsTimesOnlyOncePerScan() throws Exception {
		det().setCollectionTime(1);
		det().atScanStart();
		det().collectData();
		det().collectData();
		det().collectData();
		verify(mockAdBase, times(1)).setAcquireTime(1.);
		verify(mockAdBase, times(1)).setAcquirePeriod(1+ READOUT_TIME);
	}

	@Test
	public void testGetStatusAfterCollectDataGoesWhenAllBusy() throws Exception {
		testCollectData();
		assertEquals(Detector.BUSY, det().getStatus());
	}

	@Test
	public void testGetStatusAfterCollectDataGoesWhenNotBusy() throws Exception {
		testCollectData();
		assertEquals(Detector.BUSY, det().getStatus());
		when(mockNdArrayBase.getUniqueId_RBV()).thenReturn(101);
		when(mockNdStatsBase.getUniqueId_RBV()).thenReturn(201);
		when(mockAdBase.getStatus()).thenReturn(Detector.IDLE);
		assertEquals(Detector.IDLE, det().getStatus());
	}

	@Test
	public void testGetStatusAfterCollectDataWaitsForNdStats() {
		// plugins are now configured to block in prepareForCollection()
	}

	@Test
	public void testGetStatusAfterCollectDataWaitsForNdArray() {
		// plugins are now configured to block in prepareForCollection()
	}

	@Test
	public void testGetStatusAfterCollectDataWaitsForNdBase() {
		// plugins are now configured to block in prepareForCollection()
	}

	@Test
	public void testGetStatusWithAdBaseFault() throws DeviceException {
		when(mockAdBase.getStatus()).thenReturn(Detector.FAULT);
		assertEquals(Detector.FAULT, det().getStatus());
	}

	@Test
	public void testReadoutNoArrayStatsOrCentroid() throws DeviceException {
		det().setReadArray(false);
		det().setComputeStats(false);
		det().setComputeCentroid(false);
		det().setReadAcquisitionTime(false);
		NXDetectorData data = (NXDetectorData) det().readout();
		assertEquals("", data.toString());
		Double[] doubleVals = data.getDoubleVals();
		assertArrayEquals(new Double[] { null }, doubleVals); // TODO: null from NxDetectorData default
	}

	@Test
	public void testReadoutArray() throws Exception {
		det().setReadArray(true);
		det().setComputeStats(false);
		det().setComputeCentroid(false);
		det().setReadAcquisitionTime(false);
		byte[] byteArray = new byte[] { 0, 1, 2, 3, 4, 6 };
		when(mockNdArrayBase.getArraySize0_RBV()).thenReturn(2);
		when(mockNdArrayBase.getArraySize1_RBV()).thenReturn(3);
		when(mockNdArrayBase.getArraySize2_RBV()).thenReturn(0);
		when(mockNdArray.getByteArrayData()).thenReturn(byteArray);

		NXDetectorData data = (NXDetectorData) det().readout();
		assertEquals("", data.toString());
		assertArrayEquals(new Double[] { null }, data.getDoubleVals());// TODO: null from NxDetectorData default
	}

	@Test
	public void testReadoutStats() throws Exception {
		det().setReadArray(false);
		det().setComputeStats(true);
		det().setComputeCentroid(false);
		det().setReadAcquisitionTime(false);

		when(mockNdStats.getMinValue_RBV()).thenReturn(0.);
		when(mockNdStats.getMaxValue_RBV()).thenReturn(1.);
		when(mockNdStats.getTotal_RBV()).thenReturn(2.);
		when(mockNdStats.getNet_RBV()).thenReturn(3.);
		when(mockNdStats.getMeanValue_RBV()).thenReturn(4.);
		when(mockNdStats.getSigma_RBV()).thenReturn(5.);
		// private static String[] STATS_NAMES = new String[]{"min", "max", "total", "net", "mean", "sigma"};

		NXDetectorData readout = (NXDetectorData) det().readout();
		assertEquals("0.0000\t1.0000\t2.0000\t3.0000\t4.0000\t5.0000", readout.toString());
		assertArrayEquals(new Double[] { 0., 1., 2., 3., 4., 5. }, readout.getDoubleVals());

		INexusTree rootNode = readout.getNexusTree().getChildNode(0);
		assertEquals("testdet", rootNode.getName());
		assertArrayEquals(new double[] { 0. }, (double[]) rootNode.getChildNode("min", "SDS").getData().getBuffer(),
				.001);
		assertArrayEquals(new double[] { 5. }, (double[]) rootNode.getChildNode("sigma", "SDS").getData().getBuffer(),
				.001);
	}

	@Test
	public void testReadoutTimes() throws Exception {
		det().setReadAcquisitionPeriod(true);
		det().setReadArray(false);
		det().setComputeStats(false);
		det().setComputeCentroid(false);

		when(mockAdBase.getAcquireTime_RBV()).thenReturn(0.5);
		when(mockAdBase.getAcquirePeriod_RBV()).thenReturn(0.55);
		NXDetectorData readout = (NXDetectorData) det().readout();
		assertEquals("0.50\t0.55", readout.toString());
		assertArrayEquals(new Double[] { 0.5, 0.55 }, readout.getDoubleVals());

		INexusTree rootNode = readout.getNexusTree().getChildNode(0);
		assertArrayEquals(new double[] { 0.5 }, (double[]) rootNode.getChildNode("count_time", "SDS").getData()
				.getBuffer(), .001);
		assertArrayEquals(new double[] { 0.55 }, (double[]) rootNode.getChildNode("period", "SDS").getData()
				.getBuffer(), .001);

	}

	// "CentroidX", "CentroidY", "Centroid_sigmaX", "Centroid_sigmaY", "Centroid_sigmaXY"};
	@Test
	public void testReadoutCentroid() throws Exception {
		det().setReadArray(false);
		det().setComputeStats(false);
		det().setComputeCentroid(true);
		det().setReadAcquisitionTime(false);
		when(mockNdStats.getCentroidX_RBV()).thenReturn(0.);
		when(mockNdStats.getCentroidY_RBV()).thenReturn(1.);
		when(mockNdStats.getSigmaX_RBV()).thenReturn(2.);
		when(mockNdStats.getSigmaY_RBV()).thenReturn(3.);
		when(mockNdStats.getSigmaXY_RBV()).thenReturn(4.);

		NXDetectorData readout = (NXDetectorData) det().readout();
		assertEquals("0.0000\t1.0000\t2.0000\t3.0000\t4.0000", readout.toString());
		assertArrayEquals(new Double[] { 0., 1., 2., 3., 4. }, readout.getDoubleVals());

		INexusTree rootNode = readout.getNexusTree().getChildNode(0);
		assertEquals("testdet", rootNode.getName());
		assertArrayEquals(new double[] { 0. }, (double[]) rootNode.getChildNode("centroidX", "SDS").getData()
				.getBuffer(), .001);
		assertArrayEquals(new double[] { 4. }, (double[]) rootNode.getChildNode("centroid_sigmaXY", "SDS").getData()
				.getBuffer(), .001);
		assertArrayEquals(new double[] { 4. }, (double[]) rootNode.getChildNode("centroid_sigmaXY", "SDS").getData()
				.getBuffer(), .001);
		

	}

	@Test
	public void testReadoutWithFilename() throws Exception {
		det().setReadArray(false);
		det().setComputeStats(false);
		det().setComputeCentroid(false);
		det().setReadAcquisitionTime(false);
		det().setReadFilepath(true);

		when(mockNdFile.getFullFileName_RBV()).thenReturn("/full/path/to/file99.cbf");

		NXDetectorData readout = (NXDetectorData) det().readout();
		assertEquals("/full/path/to/file99.cbf", readout.toString());
		Double[] doubleVals = readout.getDoubleVals();
		assertArrayEquals(new Double[] { 0.0 }, doubleVals); 
		INexusTree rootNode = readout.getNexusTree().getChildNode(0);
		assertEquals("testdet", rootNode.getName());
		String actualPath = new String((byte[]) rootNode.getChildNode("data_file", "NXnote")
				.getChildNode("file_name", "SDS").getData().getBuffer());
		assertEquals("/full/path/to/file99.cbf", actualPath.trim()); // trim gets rid of the internal null bytes from
																		// actualString

	}

	@Test
	public void testReadoutWithFilenameAndTimes() throws Exception {
		setupForReadoutAndGetPositionWithFilenameAndTimes();

		NXDetectorData readout = (NXDetectorData) det().readout();
		assertEquals("0.50\t0.55\t/full/path/to/file99.cbf", readout.toString());
		assertArrayEquals(new Double[] { 0.5, 0.55, 0.0}, readout.getDoubleVals());
	}

	protected void setupForReadoutAndGetPositionWithFilenameAndTimes() throws Exception {
		det().setReadAcquisitionTime(true);
		det().setReadAcquisitionPeriod(true);
		det().setReadArray(false);
		det().setComputeStats(false);
		det().setComputeCentroid(false);
		det().setReadFilepath(true);

		when(mockNdFile.getFullFileName_RBV()).thenReturn("/full/path/to/file99.cbf");
		when(mockAdBase.getAcquireTime_RBV()).thenReturn(0.5);
		when(mockAdBase.getAcquirePeriod_RBV()).thenReturn(0.55);
	}

	protected void checkReadoutWithFilenameAndTimes(NXDetectorData readout, String pathname) {
		INexusTree rootNode = readout.getNexusTree().getChildNode(0);
		assertArrayEquals(new double[] { 0.5 }, (double[]) rootNode.getChildNode("count_time", "SDS").getData()
				.getBuffer(), .001);
		assertArrayEquals(new double[] { 0.55 }, (double[]) rootNode.getChildNode("period", "SDS").getData()
				.getBuffer(), .001);
		String actualPath = new String((byte[]) rootNode.getChildNode("data_file", "NXnote")
				.getChildNode("file_name", "SDS").getData().getBuffer());
		assertEquals(pathname, actualPath.trim()); // trim gets rid of the internal null bytes from
	}

	@Test
	public void testStop() throws Exception {
		det().stop();
		verify(mockAdBase).stopAcquiring();
		
	}
}