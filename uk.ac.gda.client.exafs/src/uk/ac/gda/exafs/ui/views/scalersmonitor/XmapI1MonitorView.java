/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.views.scalersmonitor;

import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.detector.DetectorMonitorDataProvider.COLLECTION_TYPES;


/**
 * Version of XmapMonitorView which does not display the results from I0,It,Iref but the next channel from the TFG
 * instead.
 */
public class XmapI1MonitorView extends XmapMonitorView  {

	public static final String ID = "uk.ac.gda.exafs.ui.views.xmapi1monitor"; //$NON-NLS-1$

	protected static final Logger logger = LoggerFactory.getLogger(XmapI1MonitorView.class);

	private COLLECTION_TYPES collectionType = COLLECTION_TYPES.XMAP_I1;

	// Column names and formats used for displayed rate data
	private String[] titles = {"I1", "Input Count Rate", "Dead Time(%)", "FF Rate", "FF / I1"};
	private String[] formats = {"%.0f", "%.0f", "%.4f", "%.4f", "%.4f"};

	@Override
	protected void setupDisplayData(Composite parent) {
		displayData = new ScalersMonitorConfig(parent);
		displayData.setTitles(titles);
		displayData.setFormats(formats);
		displayData.createControls();
	};

	@Override
	protected void updateDisplayedData(Double[] xmapStats, Double[] ionchamberValues) {
		double I1counts = ionchamberValues[0];
		double rate = xmapStats[0]; // Rate in Hz
		double deadTimePercent = (xmapStats[1] - 1)*100; // Deadtime as percentage
		double FF = xmapStats[2]*xmapStats[1]; // FF only for first element? XmapMonitor does sum over all elements...

		displayData.setTextInColumn(0, I1counts);
		displayData.setTextInColumn(1, rate);
		displayData.setTextInColumn(2, deadTimePercent);
		displayData.setTextInColumn(3, FF);
		displayData.setTextInColumn(4, FF/I1counts);

		double[] rates = getRatesFromStats(xmapStats);
		double[] dts = getDeadtimePercentFromStats(xmapStats);

		updatePlot(rates, dts);
	}

	@Override
	protected Double[] getFluoDetectorCountRatesAndDeadTimes() throws DeviceException {
		numElements = dataProvider.getNumElements(collectionType);
		return dataProvider.getFluoDetectorCountRatesAndDeadTimes(collectionType);
	}

	@Override
	protected Double[] getIonChamberValues() throws Exception {
		return dataProvider.getIonChamberValues(collectionType);
	}
}
