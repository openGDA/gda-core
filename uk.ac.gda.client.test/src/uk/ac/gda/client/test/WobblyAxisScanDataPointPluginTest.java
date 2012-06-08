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
import gda.rcp.views.scan.AbstractScanPlotView;
import gda.scan.IScanDataPoint;

import java.net.URL;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.Before;
import org.junit.Test;

import uk.ac.gda.ClientManager;
import uk.ac.gda.client.XYPlotView;
import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.preferences.PreferenceConstants;

public class WobblyAxisScanDataPointPluginTest implements ViewTestObject.AscciLineParser  {
			
	
	private ViewTestObject testObject;

	@Before
	public void setUp() {
		ClientManager.setTestingMode(true);
		
		GDAClientActivator.getDefault().getPreferenceStore().setValue(PreferenceConstants.MAX_SIZE_CACHED_DATA_POINTS,1000);	
		
	}
	
	/**
	 * This test shows up the wobbly axis problem visually at the moment run as plugin test.
	 */
	@Test
	public void testWobblyAxes() throws Exception {
		
		final URL data  = WobblyAxisScanDataPointPluginTest.class.getResource("738.dat");
		this.testObject = new ViewTestObject(this, data);
		
		testObject.setPointPause(150);
		final XYPlotView part = (XYPlotView)testObject.openView("uk.ac.gda.client.xyplotview");
		testObject.createAndMonitorPoints();
		
		System.out.println("Sent "+testObject.getLineIndex()+" ScanDataPoints to "+part.getClass().getName());
					
		EclipseUtils.delay(2000);
		
		part.clearGraph();
    	final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		window.getActivePage().hideView(part);
		part.dispose();
	}
	
	/**
	 * This test shows the labels for the x-axis entirely wrong when compared to the data
	 */
	@Test
	public void testDodgyLabeling() throws Exception {
		
		final URL data  = WobblyAxisScanDataPointPluginTest.class.getResource("739.dat");
		this.testObject = new ViewTestObject(this, data);
		
		testObject.setPointPause(150);
		final XYPlotView part = (XYPlotView)testObject.openView("uk.ac.gda.client.xyplotview");
		testObject.createAndMonitorPoints();
		
		System.out.println("Sent "+testObject.getLineIndex()+" ScanDataPoints to "+part.getClass().getName());
					
		EclipseUtils.delay(5000); // Give it time to see the effect.
	}
	
	/**
	 * Causes a hang to happen
	 */
	@Test
	public void testGraphHangs() throws Exception {
		
		final URL data  = WobblyAxisScanDataPointPluginTest.class.getResource("738.dat");
		this.testObject = new ViewTestObject(this, data);

		// The way this view is set up hangs the graph - it should not be so.
		final AbstractScanPlotView part = (AbstractScanPlotView)testObject.openView("gda.rcp.views.scan.MultipleScanPlotView");
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
		point.setDetectorHeader(new String[]{"d3_updrain"});
	}	
}
