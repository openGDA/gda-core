/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import gda.configuration.properties.LocalProperties;
import gda.device.CounterTimer;
import gda.device.DeviceException;
import gda.factory.Finder;
import uk.ac.gda.beans.xspress.XspressDetector;

public class XspressMonitorView extends MonitorViewBase {
	public static final String ID = "uk.ac.gda.exafs.ui.views.scalersmonitor";
	// protected static final Logger logger = LoggerFactory.getLogger(XspressMonitorView.class);
	// private static final Double MAX_FLUO_RATE = 500000.0;
	protected ScalersMonitorConfig displayData;
	private IAxis dtAxis;
	private IAxis primaryAxis;
	private XspressDetector xspress;
	private CounterTimer ionchambers;
	private Double collectionTime;
	private Double maxFluoRate;

	public XspressMonitorView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		Group grpCurrentCountRates = new Group(parent, SWT.BORDER);
		grpCurrentCountRates.setText("Current count rates");
		grpCurrentCountRates.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		grpCurrentCountRates.setLayout(new GridLayout());

		displayData = new ScalersMonitorConfig(grpCurrentCountRates);

		myPlotter.createPlotPart(grpCurrentCountRates, "Rates", null, PlotType.XY, null);
		myPlotter.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		primaryAxis = myPlotter.getSelectedYAxis();
		primaryAxis.setTitle("Counts (Hz)");
		dtAxis = myPlotter.createAxis("Deadtime (%)", true, SWT.RIGHT);

		super.createPartControl(parent);

		// TODO why get the detector names from java properties? Spring is a much nicer way of configuring as it means
		// not having to look in the code to see what devices are used.
		String xspressName = LocalProperties.get("gda.exafs.xspressName", "xspress2system");
		xspress = (XspressDetector) Finder.getInstance().find(xspressName);
		String ionchambersName = LocalProperties.get("gda.exafs.ionchambersName", "counterTimer01");
		ionchambers = (CounterTimer) Finder.getInstance().find(ionchambersName);
		collectionTime = Double.valueOf(LocalProperties.get("gda.exafs.ui.views.scalersMonitor.collectionTime", "1.0"));
		maxFluoRate = Double.valueOf(LocalProperties.get("gda.exafs.ui.views.scalersMonitor.maxFluoRate", "500000"));
	}

	protected void updateXspressGrid(Double[] xspressStats, Double[] values) {
		try {
			double[] rates = new double[numElements];
			double[] dts = new double[numElements];
			Double FF = 0.0;
			double maxRate = 0;
			double maxDT = 0.0;
			int maxElement = 0;
			for (int element = 0; element < numElements; element++) {
				rates[element] = xspressStats[element * 3]; // Hz
				dts[element] = (xspressStats[element * 3 + 1] - 1) * 100;
				if (dts[element] < 0.0)
					dts[element] = 0.0; // %
				FF += xspressStats[element * 3];
				// find which element gives the max rate
				if (xspressStats[element * 3] > maxRate) {
					maxRate = xspressStats[element * 3];
					maxElement = element;
				}
				maxDT = xspressStats[element * 3 + 1] > maxDT ? xspressStats[element * 3 + 1] : maxDT;
			}
			displayData.setFF(FF);

			// get the normalised in window counts for the highest rate element
			switch (numElements) {
			case 9:
				displayData.setFFI0(xspressStats[maxElement * 3 + 2] / values[0]);
				break;
			case 64:
				displayData.setFFI0(xspressStats[36 * 3 + 2] / values[0]); // use element 37 as I think this is one of
																			// the more central ones
				break;
			default:
				displayData.setFFI0(xspressStats[2] / values[0]);
				break;
			}

			Dataset dsRates = DatasetFactory.createFromObject(rates);
			dsRates.setName("Rates (Hz)");

			Dataset dsDeadTime = DatasetFactory.createFromObject(dts);
			dsDeadTime.setName("Deadtime (%)");

			Dataset x = DatasetFactory.createRange(numElements, Dataset.FLOAT32);
			x.setName("Element");

			myPlotter.clear();

			// rates plot
			myPlotter.setSelectedYAxis(primaryAxis);
			ILineTrace ratesTrace = myPlotter.createLineTrace("Rates (Hz)");
			ratesTrace.setTraceType(TraceType.HISTO);
			ratesTrace.setLineWidth(5);
			ratesTrace.setTraceColor(new Color(null, 0, 0, 128));
			ratesTrace.setData(x, dsRates);
			myPlotter.addTrace(ratesTrace);
			myPlotter.getSelectedXAxis().setRange(0, numElements);
			myPlotter.getSelectedXAxis().setTitle("Element");
			myPlotter.getSelectedYAxis().setRange(0, maxFluoRate);

			// deadtime plot
			myPlotter.setSelectedYAxis(dtAxis);
			ILineTrace deadTimeTrace = myPlotter.createLineTrace("Red (%)");
			deadTimeTrace.setLineWidth(1);
			deadTimeTrace.setTraceColor(new Color(null, 255, 0, 0));
			deadTimeTrace.setData(x, dsDeadTime);
			myPlotter.addTrace(deadTimeTrace);
			myPlotter.getSelectedYAxis().setShowMajorGrid(false);
			myPlotter.getSelectedYAxis().setRange(0, 100);

			myPlotter.setSelectedYAxis(primaryAxis);
			myPlotter.setShowLegend(false);
			myPlotter.repaint(false);

		} catch (Exception e) {
			// log and stop plotting
			runMonitoring = false;
			logger.error("Exception trying to plot Xspress statistics " + e.getMessage());
		}

	}

	@Override
	protected Double[] getFluoDetectorCountRatesAndDeadTimes() throws DeviceException {
		numElements = xspress.getNumberOfDetectors();
		Double[] rates = (Double[]) xspress.getAttribute("liveStats");
		return rates;
	}

	@Override
	protected Double[] getIonChamberValues() throws Exception {
		if (xspress == null) {
			String xspressName = LocalProperties.get("gda.exafs.xspressName", "xspress2system");
			xspress = (XspressDetector) Finder.getInstance().find(xspressName);
		}
		if (ionchambers == null) {
			String ionchambersName = LocalProperties.get("gda.exafs.ionchambersName", "counterTimer01");
			ionchambers = (CounterTimer) Finder.getInstance().find(ionchambersName);
		}

		// Check to make sure that no scan or script is currently running before reading from detectors
		// to avoid indadvertently interfering with data collection.
		if ( !getScriptOrScanIsRunning() && !xspress.isBusy()
				&& !ionchambers.isBusy()) {
			xspress.collectData();
			ionchambers.setCollectionTime(collectionTime);
			ionchambers.clearFrameSets();
			ionchambers.collectData();
			xspress.waitWhileBusy();
			ionchambers.waitWhileBusy();
		} else {
			throw new Exception(ALREADY_RUNNING_MSG);
		}

		// read the latest frame
		int currentFrame = ionchambers.getCurrentFrame();
		if (currentFrame % 2 != 0)
			currentFrame--;

		if (currentFrame > 0) {
			currentFrame /= 2;
			currentFrame--;
		}

		// assumes an column called I0
		double[] ion_results = (double[]) ionchambers.readout();
		Double collectionTime = (Double) ionchambers.getAttribute("collectionTime");

		String[] extraNames = ionchambers.getExtraNames();
		int i0Index = ArrayUtils.indexOf(extraNames, "I0");
		if (collectionTime != null) {
			ion_results[i0Index] /= collectionTime;
			ion_results[i0Index + 1] /= collectionTime;
			ion_results[i0Index + 2] /= collectionTime;
		}


		return new Double[] { ion_results[i0Index + 0], ion_results[i0Index + 1], ion_results[i0Index + 2] };
	}

	@Override
	protected void updateDisplay(Double[] values, Double[] xspressStats) {
		displayData.setI0(values[0]);
		displayData.setIt(values[1]);
		displayData.setIref(values[2]);
		double ItI0 = Math.log(values[0] / values[1]);
		displayData.setItI0(ItI0);
		double IrefIt = Math.log(values[2] / values[1]);
		displayData.setIrefIt(IrefIt);
		updateXspressGrid(xspressStats, values);
	}
}
