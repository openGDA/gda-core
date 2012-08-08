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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gda.data.nexus.tree.INexusTree;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.addetector.filewriter.FileWriter;
import gda.device.detector.addetector.triggering.ADTriggeringStrategy;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.areadetector.v17.NDStats;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class ADDetectorTest {

	private ADDetector adDet;
	@Mock protected ADBase adBase;
	@Mock protected ADTriggeringStrategy collectionStrategy;
	@Mock protected FileWriter fileWriter;
	@Mock protected NDArray ndArray;
	@Mock protected NDStats ndStats;
	@Mock protected NDPluginBase ndArrayBase;
	@Mock protected NDPluginBase ndStatsBase;
	
	protected static String[] STATS_NAMES = new String[] { "min", "max", "total", "net", "mean", "sigma" };
	protected static String[] STATS_FORMATS = new String[] { "%5.5g", "%5.5g", "%5.5g", "%5.5g", "%5.5g", "%5.5g" };
	protected static String[] CENTROID_NAMES = new String[] { "centroidX", "centroidY", "centroid_sigmaX",
			"centroid_sigmaY", "centroid_sigmaXY" };
	protected static String[] CENTROID_FORMATS = new String[] { "%5.5g", "%5.5g", "%5.5g", "%5.5g", "%5.5g" };

	protected double READOUT_TIME = 0.1;
	
	public ADDetector det() {
		return adDet;
	}

	@Before
	
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		setUpNoConfigure();
		det().configure();
	}

	protected void setUpNoConfigure() throws Exception {
		createDetector();
		det().setName("testdet");
		det().setAdBase(adBase);
		det().setNdArray(ndArray);
		det().setNdStats(ndStats);
		det().setFileWriter(fileWriter);
		det().afterPropertiesSet();
		when(ndArray.getPluginBase()).thenReturn(ndArrayBase);
		when(ndStats.getPluginBase()).thenReturn(ndStatsBase);
	}

	@SuppressWarnings("unused")
	protected void createDetector() throws Exception {
		adDet = new ADDetector();
		adDet.setCollectionStrategy(collectionStrategy);
	}

	protected void enableStatsAndCentroid(boolean computeStats, boolean computeCentroid) {
		det().setComputeStats(computeStats);
		det().setComputeCentroid(computeCentroid);
	}
	
	protected void enableArrayReadout(boolean enableArrayReadout) {
		det().setReadArray(enableArrayReadout);
	}
	
	@Test
	public void testConstructor() throws Exception {
		setUpNoConfigure();
		assertFalse(det().isComputeStats());
		assertFalse(det().isComputeCentroid());
		assertFalse(det().createsOwnFiles());
		assertTrue(det().isLocal());
		assertTrue(det().isReadAcquisitionTime());
		assertFalse(det().isReadAcquisitionPeriod());
		assertFalse(det().isReadFilepath());
	}
	
	@Test
	public void testReadsArrayByDefault() throws Exception {
		setUpNoConfigure();
		assertTrue(det().isReadArray());
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
		verify(adBase, times(2)).reset();
		verify(ndArray, times(2)).reset();
		verify(ndStats, times(2)).reset();
	}

	@Test
	public void testReset() throws Exception {
		setUpNoConfigure();
		det().reset();
		verify(adBase, times(2)).reset();
		verify(ndArray, times(2)).reset();
		verify(ndStats, times(2)).reset();
	}

	@Test
	public void testGetInputNames() {
		assertArrayEquals(new String[] {}, det().getInputNames());
		det().setReadAcquisitionTime(false);
		assertArrayEquals(new String[] {}, det().getInputNames());

	}

	@Test
	public void testGetExtraNamesNostats() {
		enableStatsAndCentroid(false, false);
		assertArrayEquals(new String[] {"count_time"}, det().getExtraNames());
		det().setReadAcquisitionPeriod(true);
		assertArrayEquals(new String[] {"count_time", "period" }, det().getExtraNames());
		det().setReadFilepath(true);
		assertArrayEquals(new String[] {"count_time", "period", "filepath" }, det().getExtraNames());
	}
	
	@Test
	public void testGetExtraNamesCentroid() {
		enableStatsAndCentroid(false, true);
		assertArrayEquals( ArrayUtils.addAll(new String[] {"count_time"}, CENTROID_NAMES), det().getExtraNames());
	}

	@Test
	public void testGetExtraNamesStats() {
		enableStatsAndCentroid(true, false);
		assertArrayEquals(ArrayUtils.addAll(new String[] {"count_time"}, STATS_NAMES), det().getExtraNames());
	}

	@Test
	public void testGetExtraNamesStatsAndCentroidAndPeriod() {
		enableStatsAndCentroid(true, true);
		det().setReadAcquisitionPeriod(true);
		assertArrayEquals(addAll(new String[] { "count_time", "period" }, addAll(STATS_NAMES, CENTROID_NAMES)), det().getExtraNames());
	}

	@Test
	public void testGetOutputFormat() {
		det().setReadAcquisitionTime(false);
		det().setReadAcquisitionPeriod(false);
		enableStatsAndCentroid(false, false);
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

		enableStatsAndCentroid(true, false);
		assertArrayEquals(new String[] { "%.2f", "%.2f", "%5.5g", "%5.5g", "%5.5g", "%5.5g", "%5.5g", "%5.5g" },
				det().getOutputFormat());

		enableStatsAndCentroid(true, true);
		assertArrayEquals(new String[] { "%.2f", "%.2f", "%5.5g", "%5.5g", "%5.5g", "%5.5g", "%5.5g", "%5.5g", "%5.5g",
				"%5.5g", "%5.5g", "%5.5g", "%5.5g" }, det().getOutputFormat());
	}

	@Test
	public void testStatus() throws Exception {
		when(collectionStrategy.getStatus()).thenReturn(Detector.IDLE);
		assertEquals(Detector.IDLE, det().getStatus());

		when(collectionStrategy.getStatus()).thenReturn(Detector.BUSY);
		assertEquals(Detector.BUSY, det().getStatus());
	}
	
	@Test
	public void waitWhileBusy() throws Exception {
		det().waitWhileBusy();
		verify(collectionStrategy).waitWhileBusy();
		
	}

	@Test
	public void testAtScanStart() throws Exception {
		enableStatsAndCentroid(true, true);
		enableArrayReadout(true);
		det().setCollectionTime(1.);
		det().atScanStart();
		verify(adBase).setArrayCallbacks((short) 1);
		verify(ndArrayBase).enableCallbacks();
		verify(ndArrayBase).setBlockingCallbacks((short) 1);
//		verify(ndStats).setComputeStatistics((short) 1);
//		verify(ndStats).setComputeCentroid((short) 1);
//		verify(ndStatsBase).setBlockingCallbacks((short) 1);
		verify(collectionStrategy).prepareForCollection(1.,  1);
	}

	@Test
	public void testAtScanStartAllOff() throws Exception {
		enableStatsAndCentroid(false, false);
		enableArrayReadout(false);
		det().atScanStart();
		verify(adBase).setArrayCallbacks((short) 1);
		verify(ndArrayBase).disableCallbacks();
		verify(ndArrayBase).setBlockingCallbacks((short) 0);
		verify(ndStats).setComputeStatistics((short) 0);
		verify(ndStats).setComputeCentroid((short) 0);
		verify(ndStatsBase).setBlockingCallbacks((short) 0);
	}

	@Test
	public void testAtScanStartAllOffDisableCallbacks() throws Exception {
		enableArrayReadout(false);
		det().setDisableCallbacks(true);
		det().atScanStart();
		verify(adBase).setArrayCallbacks((short) 0);
		verify(ndArrayBase).disableCallbacks();
		verify(ndArrayBase).setBlockingCallbacks((short) 0);
		verify(ndStats).setComputeStatistics((short) 0);
		verify(ndStats).setComputeCentroid((short) 0);
		verify(ndStatsBase).setBlockingCallbacks((short) 0);
	}

	
	
	@Test
	public void testCollectData() throws Exception {
		enableStatsAndCentroid(true, true);
		enableArrayReadout(true);

		det().setCollectionTime(1);
		det().collectData();
		when(collectionStrategy.getStatus()).thenReturn(Detector.BUSY);


		verify(collectionStrategy).collectData();
	}

	@Test
	public void testCollectDataPutsTimesOnlyOncePerScan() throws Exception {
		det().setCollectionTime(1);
		det().atScanStart();
		det().collectData();
		det().collectData();
		det().collectData();
		verify(collectionStrategy, times(1)).prepareForCollection(1., 1);
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
		when(collectionStrategy.getStatus()).thenReturn(Detector.IDLE);
		assertEquals(Detector.IDLE, det().getStatus());
	}

	@Test
	public void testGetStatusWithAdBaseFault() throws DeviceException, Exception {
		when(collectionStrategy.getStatus()).thenReturn(Detector.FAULT);
		assertEquals(Detector.FAULT, det().getStatus());
	}

	@Test
	public void testReadoutNoArrayStatsOrCentroid() throws DeviceException {
		enableStatsAndCentroid(false, false);
		enableArrayReadout(false);
		det().setReadAcquisitionTime(false);
		NXDetectorData data = (NXDetectorData) det().readout();
		assertEquals("", data.toString());
		Double[] doubleVals = data.getDoubleVals();
		assertArrayEquals(new Double[] { }, doubleVals);
	}

	@Test
	public void testReadoutArray() throws Exception {
		enableStatsAndCentroid(false, false);
		enableArrayReadout(true);
		det().setReadAcquisitionTime(false);
		byte[] byteArray = new byte[] { 0, 1, 2, 3, 4, 6 };
		when(ndArrayBase.getArraySize0_RBV()).thenReturn(2);
		when(ndArrayBase.getArraySize1_RBV()).thenReturn(3);
		when(ndArrayBase.getArraySize2_RBV()).thenReturn(0);
		when(ndArray.getByteArrayData()).thenReturn(byteArray);

		NXDetectorData data = (NXDetectorData) det().readout();
		assertEquals("", data.toString());
		assertArrayEquals(new Double[] { }, data.getDoubleVals());
	}

	@Test
	public void testReadoutStats() throws Exception {
		enableArrayReadout(false);
		enableStatsAndCentroid(true, false);
		det().setReadAcquisitionTime(false);

		when(ndStats.getMinValue_RBV()).thenReturn(0.);
		when(ndStats.getMaxValue_RBV()).thenReturn(1.);
		when(ndStats.getTotal_RBV()).thenReturn(2.);
		when(ndStats.getNet_RBV()).thenReturn(3.);
		when(ndStats.getMeanValue_RBV()).thenReturn(4.);
		when(ndStats.getSigma_RBV()).thenReturn(5.);
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
		enableArrayReadout(false);
		enableStatsAndCentroid(false, false);

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

	// "CentroidX", "CentroidY", "Centroid_sigmaX", "Centroid_sigmaY", "Centroid_sigmaXY"};
	@Test
	public void testReadoutCentroid() throws Exception {
		enableArrayReadout(false);
		enableStatsAndCentroid(false, true);
		det().setReadAcquisitionTime(false);
		when(ndStats.getCentroidX_RBV()).thenReturn(0.);
		when(ndStats.getCentroidY_RBV()).thenReturn(1.);
		when(ndStats.getSigmaX_RBV()).thenReturn(2.);
		when(ndStats.getSigmaY_RBV()).thenReturn(3.);
		when(ndStats.getSigmaXY_RBV()).thenReturn(4.);

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
		enableArrayReadout(false);
		enableStatsAndCentroid(false, false);
		det().setReadAcquisitionTime(false);
		det().setReadFilepath(true);

		when(fileWriter.getFullFileName_RBV()).thenReturn("/full/path/to/file99.cbf");

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
		enableArrayReadout(false);
		enableStatsAndCentroid(false, false);
		det().setReadFilepath(true);

		when(fileWriter.getFullFileName_RBV()).thenReturn("/full/path/to/file99.cbf");

		when(collectionStrategy.getAcquireTime()).thenReturn(0.5);
		when(collectionStrategy.getAcquirePeriod()).thenReturn(0.55);
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
		verify(collectionStrategy).stop();
		
	}
}