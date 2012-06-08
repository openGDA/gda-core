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

import gda.rcp.views.scan.MultipleScanPlotView;
import gda.rcp.views.scan.AbstractScanPlotView.GRAPH_MODE;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.rcp.views.plot.PlotData;
import uk.ac.gda.ClientManager;

/**
 * Run as junit plugin test.
 */
public class MultipleScanPlotViewPluginTest {

	private MultipleScanPlotView view;

	/**
	 * @throws Throwable
	 */
	@Before
	public void setUp() throws Throwable {
		ClientManager.setTestingMode(true);
		
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		
		// Close the introduction page.
		this.view = (MultipleScanPlotView)window.getActivePage().showView(MultipleScanPlotView.ID);
	}
	
	/**
	 * Test method for scan data point plotting. Simply tests
	 * that the view comes up and works.
	 * @throws Throwable 
	 */
	@Test
	public final void testMultipleScanPlotView() throws Throwable {
		
		// We need a test DataSet to test
		final PlotData x = getXPlot(5000);
		final PlotData y = getYPlots(5000);
		
		final long start = (new Date()).getTime();
		view.createPlot(x, y, GRAPH_MODE.DIRECT_DRAW);
		final long end = (new Date()).getTime();
		
		final long time = (end-start);
		assert time<10000 : "It took took too long to plot 5000 verices.";
		System.out.println("Tested 10x(5000) vertices - passed in "+time);
	}

	/**
	 * 
	 * @throws Throwable
	 */
	@Test
	public final void testMultipleScanPlotViewLarge() throws Throwable {
		
		System.out.println("Testing how long it takes to send 200000 points to the plotter a five times to get the average.");

		// We need a test DataSet to test
		final PlotData x = getXPlot(200000);
		final PlotData y = getYPlots(200000);
		
		long time = 0;
		for (int i = 0; i < 5; i++) {
			long start = (new Date()).getTime();
			view.createPlot(x, y, GRAPH_MODE.DIRECT_DRAW);
			long end = (new Date()).getTime();
			time    += (end-start);
		}
		
		assert (time/5)<20000 : "It took took too long to plot 200,000 verices on average.";
		System.out.println("Tested 10x(200,000) vertices - passed in "+(time/5)+" on average.");

	}

	private PlotData getXPlot(int size) {
		final List<Double> x = new ArrayList<Double>(size);
		for (int i = 0; i < size; i++) x.add(Double.valueOf(i));
		return new PlotData("x",x);
	}

	private PlotData getYPlots(int size) {
		
		final Map<String,List<Double>> data = new LinkedHashMap<String,List<Double>>();
		for (int i = 0; i < 10; i++) {
			data.put("fx"+i, new ArrayList<Double>(size));
		}
		
		for (int x = 0; x < size; x++) {
			for (int i = 0; i < 10; i++) {
				data.get("fx"+i).add(Double.valueOf(x*i));
			}
		}
		
		return new PlotData(data);
	}

}
