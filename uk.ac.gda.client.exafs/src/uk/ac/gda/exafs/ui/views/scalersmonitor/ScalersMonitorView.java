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

import gda.configuration.properties.LocalProperties;
import gda.device.CounterTimer;
import gda.device.DeviceException;
import gda.device.detector.xspress.XspressDetector;
import gda.factory.Finder;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;

import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.axis.IAxis;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ILineTrace.TraceType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;

public class ScalersMonitorView extends MonitorViewBase {

	public static final String ID = "uk.ac.gda.exafs.ui.views.scalersmonitor"; //$NON-NLS-1$

	@SuppressWarnings("hiding")
	protected static final Logger logger = LoggerFactory.getLogger(ScalersMonitorView.class);
	
	private static final Double MAX_FLUO_RATE = 500000.0;

	protected ScalersMonitorViewData displayData;

	private IAxis dtAxis;

	private IAxis primaryAxis;

	public ScalersMonitorView() {
	}

	@Override
	public void createPartControl(Composite parent) {

		Group grpCurrentCountRates = new Group(parent, SWT.BORDER);
		grpCurrentCountRates.setText("Current count rates");
		grpCurrentCountRates.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		grpCurrentCountRates.setLayout(new GridLayout());

		displayData = new ScalersMonitorViewData(grpCurrentCountRates);
		
		myPlotter.createPlotPart(grpCurrentCountRates, "Rates", null, PlotType.XY, null);
		myPlotter.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		primaryAxis = myPlotter.getSelectedYAxis();
		primaryAxis.setTitle("Counts (Hz)");
		dtAxis = myPlotter.createAxis("Deadtime (%)", true, SWT.RIGHT);
		
		super.createPartControl(parent);
	}

	protected void updateXspressGrid(Double[] xspressStats, Double[] values) {
		try {
			double[] rates = new double[numElements];
			double[] dts = new double[numElements];
			Double FF = 0.0;
			double maxRate = 0;
			double maxDT = 0.0;
			for (int element = 0; element < numElements; element++) {
				rates[element] = xspressStats[element * 3]; // Hz
				dts[element] = (xspressStats[element * 3 + 1] - 1) * 100; // %
				FF += xspressStats[element * 3];

				maxRate = xspressStats[element * 3] > maxRate ? xspressStats[element * 3] : maxRate;
				maxDT = xspressStats[element * 3 + 1] > maxDT ? xspressStats[element * 3 + 1] : maxDT;
			}
			displayData.setFF(FF);

			// get the normalised in window counts for one of the highest rate elements
			switch (numElements) {
			case 9:
				displayData.setFFI0(xspressStats[8 * 3 + 2] / values[0]);
				break;
			case 64:
				displayData.setFFI0(xspressStats[36 * 3 + 2] / values[0]);  // use element 37 as I think this is one of the more central ones
				break;
			default:
				displayData.setFFI0(xspressStats[2] / values[0]);
				break;
			}

			for (int element = 0; element < numElements; element++) {
				rates[element] = rates[element];
				dts[element] = dts[element];
			}

			AbstractDataset dsRates = new DoubleDataset(rates);
			dsRates.setName("Rates (Hz)");
			
			AbstractDataset dsDeadTime = new DoubleDataset(dts);
			dsDeadTime.setName("Deadtime (%)");
			
			AbstractDataset x =  AbstractDataset.arange(numElements, AbstractDataset.FLOAT32);
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
			myPlotter.getSelectedYAxis().setRange(0, MAX_FLUO_RATE);
			
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
		String xspressName = LocalProperties.get("gda.exafs.xspressName", "xspress2system");
		XspressDetector xspress = (XspressDetector) Finder.getInstance().find(xspressName);
		numElements = xspress.getNumberOfDetectors();
		Double[] rates = (Double[]) xspress.getAttribute("liveStats");
		return rates;
	}

	@Override
	protected Double[] getIonChamberValues() throws Exception {

		String xspressName = LocalProperties.get("gda.exafs.xspressName", "xspress2system");
		XspressDetector xspress = (XspressDetector) Finder.getInstance().find(xspressName);
		String ionchambersName = LocalProperties.get("gda.exafs.ionchambersName", "counterTimer01");
		CounterTimer ionchambers = (CounterTimer) Finder.getInstance().find(ionchambersName);

		// only collect new data outside of scans else will readout the last data collected
		if (JythonServerFacade.getInstance().getScanStatus() == Jython.IDLE && !xspress.isBusy()
				&& !ionchambers.isBusy()) {
			xspress.collectData();
			ionchambers.setCollectionTime(1);
			ionchambers.collectData();

			xspress.waitWhileBusy();
			ionchambers.waitWhileBusy();
		} else {
			throw new Exception(ALREADY_RUNNING_MSG);
		}


		// read the latest frame
		int currentFrame = ionchambers.getCurrentFrame();
		if (currentFrame % 2 != 0) {
			currentFrame--;
		}
		if (currentFrame > 0) {
			currentFrame /= 2;
			currentFrame--;
		}

		int numChannels = ionchambers.getExtraNames().length;
		// works for TFG2 only where time if the first channel
		double[] ion_results = ionchambers.readFrame(1, numChannels, currentFrame);

		Double collectionTime = (Double) ionchambers.getAttribute("collectionTime");
		int i0Index = -1;
		String[] eNames = ionchambers.getExtraNames();
		// find the index for I0
		for (String s : eNames) {
			i0Index++;
			if (s.equals("I0"))
				break;

		}
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
