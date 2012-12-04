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

package uk.ac.gda.client;

import gda.TestHelpers;
import gda.gui.scanplot.ScanDataPointHandler;
import gda.gui.scanplot.ScanDataPointHandlerTester;
import gda.jython.IScanDataPointObserver;
import gda.jython.IScanDataPointProvider;
import gda.jython.InterfaceProvider;
import gda.observable.ObservableComponent;
import gda.rcp.GDAClientActivator;
import gda.rcp.util.UIScanDataPointEventService;
import gda.scan.IScanDataPoint;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.dawnsci.plotting.jreality.impl.Plot1DAppearance;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.gda.ClientManager;
import uk.ac.gda.preferences.PreferenceConstants;

public class XYPlotViewPluginTest implements IScanDataPointProvider, ScanDataPointHandler {
	private XYPlotView view;
	String scratchFolder;
	URL testdataFile;
	@Before
	public void setUp() throws Exception {

		ClientManager.setTestingMode(true);
		scratchFolder=TestHelpers.setUpTest(XYPlotViewPluginTest.class, "setUp", true);
		testdataFile = XYPlotViewPluginTest.class.getResource("XYPlotViewTestTestData.nxs");
		GDAClientActivator.getDefault().getPreferenceStore().setValue(PreferenceConstants.MAX_SIZE_CACHED_DATA_POINTS,1);	
		
		InterfaceProvider.setScanDataPointProviderForTesting(this);
		UIScanDataPointEventService.getInstance().reconnect();
		
	}

	/**
	 * Perform post-test cleanup.
	 */
	@After
	public void tearDown() {
		// Dispose of test fixture.

		waitForJobs();
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if( page != null)
			page.hideView(view);

		// Add additional teardown code here.
	}

	@Test
	public void test1() throws IOException, PartInitException {
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		this.view = (XYPlotView)window.getActivePage().showView(XYPlotView.getID());
		window.getActivePage().activate(view);
		
		ActionFactory.IWorkbenchAction  maximizeAction = ActionFactory.MAXIMIZE.create(window);
		maximizeAction.run(); // Will maximize the active part		
		waitForJobs();
		delay(3000);		
		
		ScanDataPointHandlerTester addDataQueue = new ScanDataPointHandlerTester(this);
		for(int i=0; i< 3; i++){
			File f =  new File(scratchFolder + File.separator + Integer.toString(i)+".nxs");
			gda.util.FileUtil.copy(testdataFile, f);
			addDataQueue.startAddingData(f.getAbsolutePath());
			while( addDataQueue.isAddingData()){
				delay(1000);
			}
			switch(i){
			case 0:
				{
				/* first sets of lines is a nested scan the first scannable has 3 positions, the second 2 positions
				 * and the 3rd 100, so there are 6 combinations of first and second scannables. 
				 * There are 2 extra scannables being monitored so the total number of lines per combination is 4
				 * making 4*6 lines = 24.
				 * So x is the 3rd scannable 
				 */
				Assert.assertNotNull(this.view.xyPlot.plotView.scans[23]);
				Assert.assertNull(this.view.xyPlot.plotView.scans[24]);
				XYData scanLine24 = this.view.xyPlot.plotView.scans[23];
				Assert.assertEquals("Scan:1 ,0=2,,1=1, YY1", scanLine24.name);
				Assert.assertEquals(100, scanLine24.archive.getyVals().getSize());
				Assert.assertEquals(5.0, scanLine24.archive.getxAxis().getValue(5), 0.001);
				Plot1DAppearance appearance = scanLine24.archive.getAppearance();
				Assert.assertEquals(1, appearance.getLineWidth());
				Assert.assertTrue(appearance.isVisible());
				}
				break;
			case 1:
				{
				/* second set is simple a scan of x with Y being read at each point so the additional
				 * number of lines is 1
				 */
				Assert.assertNotNull(this.view.xyPlot.plotView.scans[24]);
				Assert.assertNull(this.view.xyPlot.plotView.scans[25]);
				XYData scanLine24 = this.view.xyPlot.plotView.scans[23];
				XYData scanLine25 = this.view.xyPlot.plotView.scans[24];
				Assert.assertEquals("Scan:2  Y0", scanLine25.name);
				Assert.assertEquals(100, scanLine25.archive.getyVals().getSize());
				Assert.assertEquals(5.0, scanLine25.archive.getxAxis().getValue(5), 0.001);
				Plot1DAppearance appearance = scanLine25.archive.getAppearance();
				Assert.assertTrue(appearance.isVisible());
				Assert.assertFalse(scanLine24.archive.getAppearance().isVisible());
				}
				break;
			case 2:
				{
				/* third second set is the same as the first
				 */
				Assert.assertNotNull(this.view.xyPlot.plotView.scans[48]);
				Assert.assertNull(this.view.xyPlot.plotView.scans[49]);
				XYData scanLine48 = this.view.xyPlot.plotView.scans[48];
				Assert.assertEquals("Scan:3 ,0=2,,1=1, YY1", scanLine48.name);
				}
				break;
			}
		}
		delay(15000); //time to 'play with the graph if wanted
	}

	
	/**
	 * Process UI input but do not return for the specified time interval.
	 * 
	 * @param waitTimeMillis
	 *            the number of milliseconds
	 */
	private void delay(long waitTimeMillis) {
		Display display = Display.getCurrent();

		// If this is the UI thread,
		// then process input.

		if (display != null) {
			long endTimeMillis = System.currentTimeMillis() + waitTimeMillis;
			while (System.currentTimeMillis() < endTimeMillis) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			display.update();
		}
		// Otherwise, perform a simple sleep.

		else {
			try {
				Thread.sleep(waitTimeMillis);
			} catch (InterruptedException e) {
				// Ignored.
			}
		}
	}

	/**
	 * Wait until all background tasks are complete.
	 */
	public void waitForJobs() {
		while (Job.getJobManager().currentJob() != null)
			delay(1000);
	}

	private ObservableComponent comp = new ObservableComponent();
	private IScanDataPoint lastScanDataPoint;
	@Override
	public void addIScanDataPointObserver(IScanDataPointObserver anObserver) {
		comp.addIObserver(anObserver);
	}

	@Override
	public void deleteIScanDataPointObserver(IScanDataPointObserver anObserver) {
		comp.deleteIObserver(anObserver);
	}

	@Override
	public void update(Object dataSource, Object data) {
		if( data instanceof IScanDataPoint)
			lastScanDataPoint = (IScanDataPoint)data;
		comp.notifyIObservers(dataSource, data);
	}

	@Override
	public IScanDataPoint getLastScanDataPoint() {
		return lastScanDataPoint;
	}

	@Override
	public void handlePoint(IScanDataPoint point) {
		update(this, point);
	}	
}
