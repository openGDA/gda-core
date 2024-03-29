/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.plugin.areadetector;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import gda.device.detector.areadetector.v17.NDPluginBasePVs;
import gda.device.detector.areadetector.v18.NDStatsPVs;
import gda.device.detector.areadetector.v18.NDStatsPVs.BasicStat;
import gda.device.detector.areadetector.v18.NDStatsPVs.CentroidStat;
import gda.device.detector.areadetector.v18.NDStatsPVs.Stat;
import gda.device.detector.areadetector.v18.NDStatsPVs.TSControlCommands;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataDoubleAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;
import gda.device.detector.nxdetector.roi.RectangularROI;
import gda.device.detector.nxdetector.roi.RectangularROIProvider;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;
import gda.scan.ScanInformation;

@RunWith(MockitoJUnitRunner.class)
public class ADTimeSeriesStatsPluginTest {

	@Mock
	private PV<TSControlCommands> tsControlPV;

	@Mock
	private PV<Integer> tsNumPointsPV;

	@Mock
	private PV<Integer> tsReadScanPV;

	@Mock
	private PV<Boolean> enableCallbacksPV;

	@Mock
	private ReadOnlyPV<Boolean> tsAquiringPV;

	@Mock
	private Map<Stat, ReadOnlyPV<Double[]>> tsArrayPVMap;

	@Mock
	private ReadOnlyPV<Integer> tsCurrentPointPV;

	@Mock
	private ReadOnlyPV<Double[]> maxArrayPV;

	@Mock
	private ReadOnlyPV<Double[]> cenxArrayPV;

	@Mock
	private NDStatsPVs pvs;

	@Mock
	private ScanInformation scanInfo;

	@Mock
	private NDPluginBasePVs pluginBasePVs;

	@Mock
	private PV<Boolean> computeStatisticsPV;

	@Mock
	private PV<Boolean> computeCentroidPV;

	@Mock
	private PV<Boolean> computeProfilesPV;

	@Mock
	private RectangularROI<Integer> roi;

	private ADTimeSeriesStatsPlugin plugin;

	@Mock
	private RectangularROIProvider<Integer> roiProvider;

	@Before
	public void setUp() throws Exception {
		when(pvs.getComputeStatistsicsPVPair()).thenReturn(computeStatisticsPV);
		when(pvs.getComputeCentroidPVPair()).thenReturn(computeCentroidPV);
		when(pvs.getComputeProfilesPVPair()).thenReturn(computeProfilesPV);
		when(pvs.getTSArrayPV(CentroidStat.CentroidX)).thenReturn(cenxArrayPV);
		when(pvs.getTSArrayPV(BasicStat.MaxValue)).thenReturn(maxArrayPV);
		when(maxArrayPV.getPvName()).thenReturn(BasicStat.MaxValue.toString());
		when(cenxArrayPV.getPvName()).thenReturn(CentroidStat.CentroidX.toString());
		when(pvs.getTSControlPV()).thenReturn(tsControlPV);
		when(pvs.getTSCurrentPointPV()).thenReturn(tsCurrentPointPV);
		when(pvs.getTSNumPointsPV()).thenReturn(tsNumPointsPV);
		when(pvs.getTSReadScanPV()).thenReturn(tsReadScanPV);
		when(pvs.getPluginBasePVs()).thenReturn(pluginBasePVs);
		when(roiProvider.getRoi()).thenReturn(roi);
		when(roi.getName()).thenReturn("testRoi");
		when(pluginBasePVs.getEnableCallbacksPVPair()).thenReturn(enableCallbacksPV);
		when(pvs.getTSNumPointsPV().get()).thenReturn(3);
		when(pvs.getTSArrayPV(BasicStat.MaxValue).get(pvs.getTSNumPointsPV().get())).thenReturn(new Double[] {0., 1., 2.});
		when(pvs.getTSArrayPV(CentroidStat.CentroidX).get(pvs.getTSNumPointsPV().get())).thenReturn(new Double[] {10., 11., 12.});
		plugin = new ADTimeSeriesStatsPlugin(pvs, "name", roiProvider, true);
	}

	@Test
	public void testGetInputStreamNamesNonEnabled() {
		assertEquals(asList(), plugin.getInputStreamNames());
		assertEquals(asList(), plugin.getInputStreamFormats());
		assertEquals(false, plugin.willRequireCallbacks());
	}

	@Test
	public void testGetInputStreamNamesBasicStatsOnly() throws Exception {
		plugin.setEnabledBasicStats(asList(BasicStat.MaxX, BasicStat.Total));
		plugin.prepareForCollection(1, scanInfo);
		assertEquals(asList("testRoi_maxx", "testRoi_total"), plugin.getInputStreamNames());
		assertEquals(asList("%f", "%f"), plugin.getInputStreamFormats());
		assertEquals(true, plugin.willRequireCallbacks());
	}

	@Test
	public void testGetInputStreamNamesCentroidStatsOnly() throws Exception {
		plugin.setEnabledCentroidStats(asList(CentroidStat.CentroidX));
		plugin.prepareForCollection(1, scanInfo);
		assertEquals(asList("testRoi_centroidx"), plugin.getInputStreamNames());
		assertEquals(asList("%f"), plugin.getInputStreamFormats());
		assertEquals(true, plugin.willRequireCallbacks());
	}

	@Test
	public void testGetInputStreamNamesBasicAndCentroidStats() throws Exception {
		plugin.setEnabledBasicStats(asList(BasicStat.MaxX, BasicStat.Total));
		plugin.setEnabledCentroidStats(asList(CentroidStat.CentroidX));
		plugin.prepareForCollection(1, scanInfo);
		assertEquals(asList("testRoi_maxx", "testRoi_total", "testRoi_centroidx"), plugin.getInputStreamNames());
		assertEquals(asList("%f", "%f", "%f"), plugin.getInputStreamFormats());
		assertEquals(true, plugin.willRequireCallbacks());
	}

	@Test
	public void testPrepareForCollectionNoneEnabled() throws Exception{
		plugin.prepareForCollection(1, scanInfo);
		verify(enableCallbacksPV).putWait(false);
		verify(computeStatisticsPV).putWait(false);
		verify(computeCentroidPV).putWait(false);
		verify(computeProfilesPV).putWait(false);
	}

	@Test
	public void testPrepareForCollectionStatsEnabled() throws Exception{
		plugin.setEnabledBasicStats(asList(BasicStat.MaxX, BasicStat.Total));
		plugin.prepareForCollection(1, scanInfo);
		verify(enableCallbacksPV).putWait(true);
		verify(computeStatisticsPV).putWait(true);
		verify(computeCentroidPV).putWait(false);
		verify(computeProfilesPV).putWait(false);
	}

	@Test
	public void testPrepareForLine() throws Exception{
		when(tsNumPointsPV.get()).thenReturn(3);
		when(scanInfo.getDimensions()).thenReturn(new int[] {99, 9, 3});
		plugin.setEnabledBasicStats(asList(BasicStat.MaxValue));
		plugin.prepareForCollection(1, scanInfo);

		plugin.prepareForLine();
		InOrder inOrder = Mockito.inOrder(tsControlPV, tsNumPointsPV);

		inOrder.verify(tsNumPointsPV).putWait(3);
		inOrder.verify(tsNumPointsPV).setValueMonitoring(true);
		inOrder.verify(tsControlPV).putWait(TSControlCommands.ERASE_AND_START);
	}

	@Test
	public void testPrepareForLineWhileDisabled() throws Exception {
		plugin.prepareForCollection(1, scanInfo);
		plugin.prepareForLine();
		verify(tsControlPV, never()).putWait(TSControlCommands.ERASE_AND_START);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testPrepareForLineWithTooManyPointsThrowsException() throws Exception {
		plugin.setEnabledCentroidStats(asList(CentroidStat.CentroidX));
		when(tsNumPointsPV.get()).thenReturn(100);
		when(scanInfo.getDimensions()).thenReturn(new int[] {99, 9, 101});
		plugin.prepareForCollection(1, scanInfo);
		plugin.prepareForLine();
	}

	@Test(expected = IllegalStateException.class)
	public void testPrepareForLineWithNoPrepareForCollectionFails() throws Exception {
		plugin.prepareForLine();
	}

	@Test(expected = IllegalStateException.class)
	public void testPrepareForLineTwiceFails() throws Exception {
		testPrepareForLine();
		plugin.prepareForLine();
	}

	@Test
	public void testStop() throws Exception {
		testPrepareForLine();
		plugin.stop();
		verify(tsNumPointsPV).setValueMonitoring(false);
		verify(tsControlPV).putWait(TSControlCommands.STOP);
	}

	@Test
	public void testAtCommandFailure() throws Exception {
		testPrepareForLine();
		plugin.stop();
		verify(tsNumPointsPV).setValueMonitoring(false);
		verify(tsControlPV).putWait(TSControlCommands.STOP);
	}

	@Test
	public void testReadWhileDisabled() throws Exception {
		testPrepareForLineWhileDisabled();
		NXDetectorDataAppender nullAppender = new NXDetectorDataNullAppender();
		assertEquals(asList(nullAppender), plugin.read(1000));
	}

	@Test
	public void testReadWhileEnabled() throws Exception {
		plugin.setEnabledBasicStats(asList(BasicStat.MaxValue));
		plugin.setEnabledCentroidStats(asList(CentroidStat.CentroidX));
		testPrepareForLine();
		when(tsCurrentPointPV.waitForValue(any(), eq(-1.0))).thenReturn(1).thenReturn(3);
		List<String> names = asList("testRoi_maxvalue", "testRoi_centroidx");
		NXDetectorDataDoubleAppender appender1 = new NXDetectorDataDoubleAppender(names, asList(0., 10.));
		NXDetectorDataDoubleAppender appender2 = new NXDetectorDataDoubleAppender(names, asList(1., 11.));
		NXDetectorDataDoubleAppender appender3 = new NXDetectorDataDoubleAppender(names, asList(2., 12.));
		assertEquals(asList(appender1), plugin.read(1000));
		assertEquals(asList(appender2, appender3), plugin.read(1000));
	}

	@Test (expected = IllegalStateException.class)
	public void testReadTooManyPoints() throws Exception {
		testReadWhileEnabled();
		plugin.read(1000);
	}

	@Test
	public void  testCompleteLineStopsMonitoring() throws Exception {
		testReadWhileEnabled();
		plugin.completeLine();
		verify(tsNumPointsPV, times(2)).setValueMonitoring(false);
	}

	@Test
	public void  testEndCollectionWaitsForPointstoHaveBeenReadOut() throws Exception {
		plugin.setEnabledBasicStats(asList(BasicStat.MaxValue));
		plugin.setEnabledCentroidStats(asList(CentroidStat.CentroidX));
		testPrepareForLine();
		when(tsCurrentPointPV.waitForValue(any(), eq(-1.0))).thenReturn(1).thenReturn(3);
		List<String> names = asList("testRoi_maxvalue", "testRoi_centroidx");
		final NXDetectorDataDoubleAppender appender1 = new NXDetectorDataDoubleAppender(names, asList(0., 10.));
		final NXDetectorDataDoubleAppender appender2 = new NXDetectorDataDoubleAppender(names, asList(1., 11.));
		final NXDetectorDataDoubleAppender appender3 = new NXDetectorDataDoubleAppender(names, asList(2., 12.));

		Callable<Void> reader = new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				Thread.sleep(501);
				assertEquals(asList(appender1), plugin.read(1000));
				Thread.sleep(501);
				assertEquals(asList(appender2, appender3), plugin.read(1000));
				return null;
			}
		};
		FutureTask<Void> futureTask = new FutureTask<Void>(reader);
		long startMillis = System.currentTimeMillis();
		new Thread(futureTask).start();
		plugin.completeLine();
		assertTrue((System.currentTimeMillis() - startMillis) > 1000);
		futureTask.get(5, TimeUnit.SECONDS);
	}
}
