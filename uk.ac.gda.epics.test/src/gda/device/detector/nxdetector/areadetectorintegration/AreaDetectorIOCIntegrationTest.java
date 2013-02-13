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

package gda.device.detector.nxdetector.areadetectorintegration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.detector.NXDetector;
import gda.device.detector.addetector.triggering.SingleExposureStandard;
import gda.device.detector.areadetector.v17.impl.ADBaseImpl;
import gda.device.detector.areadetector.v17.impl.NDPluginBasePVsImpl;
import gda.device.detector.areadetector.v18.NDStatsPVs.BasicStat;
import gda.device.detector.areadetector.v18.impl.NDStatsImpl;
import gda.device.detector.nxdetector.NXCollectionStrategyPlugin;
import gda.device.detector.nxdetector.NXPlugin;
import gda.device.detector.nxdetector.plugin.areadetector.ADTimeSeriesStatsPlugin;
import gda.epics.LazyPVFactory;
import gda.jython.ICurrentScanInformationHolder;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.derby.iapi.store.access.ScanInfo;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AreaDetectorIOCIntegrationTest {
	
	final String BASE_PV_NAME = "BLRWI-DI-CAM-01:";
	
	final String STAT1_INITIAL_ARRAY_PORT = "DCAM1.ROI1";
	
	private ADBaseImpl adBase;

	private NXCollectionStrategyPlugin collectionStrategy;
	
	private NDPluginBasePVsImpl stat1basePVs;
	
	private NDStatsImpl stat1PVs;
	
	private NXDetector det;

	private ADTimeSeriesStatsPlugin adTimeSeriesStatsPlugin;

	private ScanInformation scanInfo;

	@BeforeClass
	public static void setUpBeforeClass() {
		URL resource = AreaDetectorIOCIntegrationTest.class.getResource("JCALibrary.p99-ws004.simulation.properties");
		String file = resource.getFile();
		System.setProperty("gov.aps.jca.JCALibrary.properties", file);
	}
	
	@Before
	public void verifyConnection() throws IOException {
		try {
			LazyPVFactory.newIntegerPV(BASE_PV_NAME + "CAM" + "Acquire").get();
		} catch (IOException e) {
			throw new IOException("Representative PV required for test unavailable: ", e);
		}
	}
	
	@Before
	public void setUp() throws Exception {
		
		scanInfo = mock(ScanInformation.class);
		ICurrentScanInformationHolder scanInfoHolder = mock(ICurrentScanInformationHolder.class);
		when(scanInfoHolder.getCurrentScanInformation()).thenReturn(scanInfo );
		InterfaceProvider.setCurrentScanInformationHolderForTesting(scanInfoHolder);
		
		adBase = new ADBaseImpl();
		adBase.setBasePVName(BASE_PV_NAME + "CAM");
		
		stat1basePVs = new NDPluginBasePVsImpl();
		stat1basePVs.setBasePVName(BASE_PV_NAME + "STAT1");
		stat1basePVs.afterPropertiesSet();
		
		stat1PVs = new NDStatsImpl();
		stat1PVs.setBasePVName(BASE_PV_NAME + "STAT1");
		stat1PVs.setPluginBasePVs(stat1basePVs);
		stat1PVs.afterPropertiesSet();
		
		collectionStrategy = new SingleExposureStandard(adBase, 0);
		adTimeSeriesStatsPlugin = new ADTimeSeriesStatsPlugin(stat1PVs, "stat1");
		
		det = new NXDetector();
		det.setCollectionStrategy(collectionStrategy);
		
		stat1basePVs.getNDArrayPortPVPair().putCallback(STAT1_INITIAL_ARRAY_PORT);
	}
	
	@Test
	public void verifyAdBaseConnection() throws Exception {
		System.out.println(adBase.getAcquirePeriod());
	}

	@Test
	public void verifyStat1basePVs() throws Exception {
		String x = stat1basePVs.getNDArrayPortPVPair().get();
		System.out.println(x);
	}
	
	@Test
	public void testScanEmulation() throws Exception {

		det.setCollectionTime(.5);
		det.atScanStart();
		det.atScanLineStart();
		det.collectData();
		det.waitWhileBusy();
		NexusTreeProvider readout0 = det.readout();
		det.collectData();
		det.waitWhileBusy();
		NexusTreeProvider readout1 = det.readout();
	}

	@Test
	public void testWithStat1WiredToCAM() throws Exception {
		
		when(scanInfo.getDimensions()).thenReturn(new int[] {0, 2});
		List<NXPlugin> plugins = new ArrayList<NXPlugin>(Arrays.asList(adTimeSeriesStatsPlugin));
		det.setAdditionalPluginList(plugins);
		adTimeSeriesStatsPlugin.setEnabledBasicStats(Arrays.asList(BasicStat.MaxX));
		stat1basePVs.getNDArrayPortPVPair().putCallback("DCAM1.CAM");
		
		det.setCollectionTime(.5);
		det.atScanStart();
		det.atScanLineStart();
		det.collectData();
		det.waitWhileBusy();
		NexusTreeProvider readout0 = det.readout();
		det.collectData();
		det.waitWhileBusy();
		NexusTreeProvider readout1 = det.readout();
		System.out.println(readout0);
		System.out.println(readout1);
	}
	
	

}
