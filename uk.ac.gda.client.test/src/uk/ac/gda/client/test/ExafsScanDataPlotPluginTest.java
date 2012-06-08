/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package uk.ac.gda.client.test;

import gda.rcp.GDAClientActivator;
import gda.rcp.views.scan.AbstractCachedScanPlotView;
import gda.scan.IScanDataPoint;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import uk.ac.gda.ClientManager;
import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.preferences.PreferenceConstants;

public class ExafsScanDataPlotPluginTest  implements ViewTestObject.AscciLineParser{
	
	private ViewTestObject testObject;

	@Before
	public void setUp() throws Exception {
		
		ClientManager.setTestingMode(true);
		
		GDAClientActivator.getDefault().getPreferenceStore().setValue(PreferenceConstants.MAX_SIZE_CACHED_DATA_POINTS,1000);	
		
		final URL data  = ExafsScanDataPlotPluginTest.class.getResource("Mofoil2.dat");
		this.testObject = new ViewTestObject(this, data);
	}

	@Test
	public void testLnIoIt() throws Exception {
		
		final AbstractCachedScanPlotView part = (AbstractCachedScanPlotView)testObject.openView("gda.rcp.views.scan.LnI0ItScanPlotView");
		testObject.createAndMonitorPoints();
		
		System.out.println("Sent "+testObject.getLineIndex()+" ScanDataPoints to "+part.getClass().getName());
		
		testObject.checkData(part);
		
		EclipseUtils.delay(2000);
	}

	@Test
	public void testDerivative() throws Exception {
		
		final AbstractCachedScanPlotView part = (AbstractCachedScanPlotView)testObject.openView("gda.rcp.views.scan.DerivativeScanPlotView");
		testObject.createAndMonitorPoints();
		
		System.out.println("Sent "+testObject.getLineIndex()+" ScanDataPoints to "+part.getClass().getName());
		
		testObject.checkData(part, 395);
		
		EclipseUtils.delay(2000);
	}
	
	@Test
	public void testFourier() throws Exception {
		
		final AbstractCachedScanPlotView part = (AbstractCachedScanPlotView)testObject.openView("gda.rcp.views.scan.FourierScanPlotView");
		testObject.createAndMonitorPoints();
		
		System.out.println("Sent "+testObject.getLineIndex()+" ScanDataPoints to "+part.getClass().getName());
		
		testObject.checkData(part);
		
		EclipseUtils.delay(2000);
	}
	
	@Test
	public void testExafs() throws Exception {
		
		final AbstractCachedScanPlotView part = (AbstractCachedScanPlotView)testObject.openView("gda.rcp.views.scan.ExafsScanPlotView");
		testObject.createAndMonitorPoints();
		
		System.out.println("Sent "+testObject.getLineIndex()+" ScanDataPoints to "+part.getClass().getName());
		
		testObject.checkData(part);
		
		EclipseUtils.delay(2000);
	}

	@Override
	public void parseLine(IScanDataPoint point, final String line) {

		// Set some reasonable data
		final String [] d = line.split(" ");
		point.addDetectorData(Double.parseDouble(d[2]),null);
		point.addDetectorData(Double.parseDouble(d[3]),null);
		point.setDetectorHeader(new String[]{"I0","It"});
	}	
}
