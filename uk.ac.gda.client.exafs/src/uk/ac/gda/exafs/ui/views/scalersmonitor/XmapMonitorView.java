/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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
import gda.device.XmapDetector;
import gda.factory.Finder;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.Plot1DAppearance;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.Plot1DGraphTable;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotException;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlottingMode;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.Plot1DStyles;

public class XmapMonitorView extends MonitorViewBase {
	
	private static final Double MAX_FLUO_RATE = 499000.0;

	public static final String ID = "uk.ac.gda.exafs.ui.views.xmapmonitor"; //$NON-NLS-1$

	@SuppressWarnings("hiding")
	protected static final Logger logger = LoggerFactory.getLogger(XmapMonitorView.class);

	protected ScalersMonitorViewData displayData;

	public XmapMonitorView() {
	}

	@Override
	public void createPartControl(Composite parent) {

		Group grpCurrentCountRates = new Group(parent, SWT.BORDER);
		grpCurrentCountRates.setText("Current count rates");
		grpCurrentCountRates.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		grpCurrentCountRates.setLayout(new GridLayout());

		displayData = new ScalersMonitorViewData(grpCurrentCountRates);

		myPlotter = new DataSetPlotter(PlottingMode.ONED, grpCurrentCountRates, true);
		myPlotter.getComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		myPlotter.setXAxisLabel("Xmap element");

		myPlotter.updateAllAppearance();
		myPlotter.refresh(false);

		super.createPartControl(parent);
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
		updateXmapGrid(xspressStats, values);
	}

	protected void updateXmapGrid(Double[] xmapStats, Double[] values) {
		try {
			double[] rates = new double[numElements];
			double[] dts = new double[numElements];
			Double FF = 0.0;
			double maxRate = 0;
			double maxDT = 0.0;
			for (int element = 0; element < numElements; element++) {
				rates[element] = xmapStats[element * 3]; // Hz
				dts[element] = (xmapStats[element * 3 + 1] - 1) * 100; // %
				FF += xmapStats[element * 3];

				maxRate = xmapStats[element * 3] > maxRate ? xmapStats[element * 3] : maxRate;
				maxDT = xmapStats[element * 3 + 1] > maxDT ? xmapStats[element * 3 + 1] : maxDT;
			}
			displayData.setFF(FF);

			// get the normalised in window counts for one of the highest rate elements
			switch (numElements) {
			case 9:
				displayData.setFFI0(xmapStats[8 * 3 + 2] / values[0]);
				break;
			case 64:
				// TODO check that this is one of the higher rate elements in the 64Ge
				displayData.setFFI0(xmapStats[16 * 3 + 2] / values[0]);
				break;
			default:
				displayData.setFFI0(xmapStats[2] / values[0]);
				break;
			}

//			int rateOOM = findOrderOfMagnitude(maxRate);
//			int dtOOM = findOrderOfMagnitude(maxDT);

			for (int element = 0; element < numElements; element++) {
				rates[element] = rates[element];// / Math.pow(10, rateOOM);
				dts[element] = dts[element] * 10000;//  / Math.pow(10, dtOOM);
			}

			Vector<DoubleDataset> dataSets = new Vector<DoubleDataset>();
			dataSets.add(new DoubleDataset(rates));
			dataSets.add(new DoubleDataset(dts));
			dataSets.add(createFullRangeDataset(MAX_FLUO_RATE));
			myPlotter.replaceAllPlots(dataSets);

			Plot1DGraphTable legend = myPlotter.getColourTable();
			legend.clearLegend();
			legend.addEntryOnLegend(0, new Plot1DAppearance(java.awt.Color.BLUE, Plot1DStyles.SOLID,
					"All Counts Rate (Hz)"));
			legend.addEntryOnLegend(1, new Plot1DAppearance(java.awt.Color.RED, Plot1DStyles.DASHED,
					"Deadtime (100K = 10%)"));
			Plot1DAppearance whiteLine = new Plot1DAppearance(java.awt.Color.WHITE, Plot1DStyles.DASHED,
					"");
			whiteLine.setVisible(false);
			legend.addEntryOnLegend(2, whiteLine);

			myPlotter.updateAllAppearance();
			myPlotter.refresh(true);
		} catch (PlotException e) {
			// log and stop plotting
			runMonitoring = false;
			logger.error("Exception trying to plot Xmap statistics " + e.getMessage());
		}

	}

	@Override
	protected Double[] getFluoDetectorCountRatesAndDeadTimes() throws DeviceException {
		String xmapName = LocalProperties.get("gda.exafs.xmapName", "xmapMca");
		XmapDetector xmap = (XmapDetector) Finder.getInstance().find(xmapName);
		numElements = xmap.getNumberOfMca();
		return (Double[]) xmap.getAttribute("liveStats");
	}

	@Override
	protected Double[] getIonChamberValues() throws DeviceException {

		String xmapName = LocalProperties.get("gda.exafs.xmapName", "xmapMca");
		XmapDetector xmap = (XmapDetector) Finder.getInstance().find(xmapName);
		String ionchambersName = LocalProperties.get("gda.exafs.ionchambersName", "counterTimer01");
		CounterTimer ionchambers = (CounterTimer) Finder.getInstance().find(ionchambersName);

		// only collect new data outside of scans else will readout the last data collected
		try {
			if (JythonServerFacade.getInstance().getScanStatus() == Jython.IDLE && !xmap.isBusy()
					&& !ionchambers.isBusy()) {
				xmap.collectData();
				ionchambers.setCollectionTime(1);
				ionchambers.collectData();
			}
			xmap.waitWhileBusy();
			ionchambers.waitWhileBusy();
		} catch (InterruptedException e) {
			// ignore
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
		}
		return new Double[] { ion_results[i0Index + 0], ion_results[i0Index + 1], ion_results[i0Index + 2] };

	}
}
