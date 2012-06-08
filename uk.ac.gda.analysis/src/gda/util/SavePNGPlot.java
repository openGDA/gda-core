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

package gda.util;

import gda.analysis.ScanFileHolder;
import gda.analysis.io.ScanFileHolderException;

import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.io.SRSLoader;

/**
 *Saves a scan plot as a .PNG image.
 *example usage from the GDA Jython terminal:
 *gda.util.SavePNGPlot.save("/home/zhb16119/chart.png",  "/home/zhb16119/gdaWorkspace/gda-config-base/data/sc666-0/base-8.dat", 1024, 768)
 *or
 *gda.util.SavePNGPlot.save("/home/zhb16119/chart.png",  data, 1024, 768), where data is a ScanFileHolder
 */
public class SavePNGPlot {

	/**
	 * @param imageFile - the full location of the image to save
	 * @param dataFile - the full location of the .dat scan file
	 * @param width - the width of the saved image in pixels
	 * @param height - the height of the saved image in pixels
	 * @param chartTitle
	 * @throws IOException 
	 */
	public static void save(String imageFile,
			String dataFile, int width, int height, String chartTitle) throws IOException {

		ScanFileHolder scan = new ScanFileHolder();
		try {
			scan.load(new SRSLoader(dataFile));
		} catch (ScanFileHolderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		save(imageFile, scan, width, height, chartTitle);
	}
	
	/**
	 * 
	 * @param imageFile
	 * @param scan
	 * @param width
	 * @param height
	 * @param chartTitle
	 * @throws IOException
	 */
		public static void save(String imageFile,
				ScanFileHolder scan, int width, int height, String chartTitle) throws IOException {


		final XYSeriesCollection dataset = new XYSeriesCollection();

		XYSeries series;

		IDataset x_axis = scan.getAxis(0);

		String[] headings = scan.getHeadings();
		String yAxisName;
		if (headings.length == 2)
			yAxisName = headings[1];
		else
			yAxisName = "various";

		for (int seriesNum = 1; seriesNum < headings.length; seriesNum++) {
			series = new XYSeries("");
			for (int point = 0, max = x_axis.getSize(); point < max - 1; point++)
				series.add(x_axis.getDouble(point),
						scan.getAxis(seriesNum).getDouble(point));
			series.setKey(headings[seriesNum]);
			dataset.addSeries(series);
		}
		
		final JFreeChart chart = ChartFactory.createXYLineChart(chartTitle, // chart
																			// title
				headings[0], // x axis label
				yAxisName, // y axis label
				dataset, // data
				PlotOrientation.VERTICAL, true, // include legend
				false, // tool tips
				false // url's
				);
			ChartUtilities.saveChartAsPNG(new File(imageFile),
					chart, width, height);
	}
}
