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

import gda.device.DeviceException;
import gda.device.detector.DetectorMonitorDataProvider.COLLECTION_TYPES;

public class XmapMonitorView extends MonitorViewBase {

	private static final Double MAX_FLUO_RATE = 500000.0;

	public static final String ID = "uk.ac.gda.exafs.ui.views.xmapmonitor";

	protected ScalersMonitorConfig displayData;

	private IAxis primaryAxis;

	private IAxis dtAxis;

	public XmapMonitorView() {
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
		double[] rates = new double[numElements];
		double[] dts = new double[numElements];
		Double FF = 0.0;
		double maxRate = 0;
		double maxDT = 0.0;
		for (int element = 0; element < numElements; element++) {
			rates[element] = xmapStats[element * 3]; // Hz
			dts[element] = (xmapStats[element * 3 + 1] - 1) * 100; // %
			FF += xmapStats[element * 3 + 2] * xmapStats[element * 3 + 1];

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
			displayData.setFFI0(xmapStats[16 * 3 + 2] / values[0]);
			break;
		default:
			displayData.setFFI0(FF / values[0]);
			break;
		}

		if (myPlotter == null) {
			return;
		}

		for (int element = 0; element < numElements; element++) {
			rates[element] = rates[element];
			dts[element] = dts[element];
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
		if (numElements == 1) {
			myPlotter.getSelectedXAxis().setRange(-1, numElements);
		} else {
			myPlotter.getSelectedXAxis().setRange(0, numElements);
		}
		myPlotter.getSelectedXAxis().setTitle("Element");
		myPlotter.getSelectedYAxis().setRange(0, MAX_FLUO_RATE);

		// deadtime plot
		myPlotter.setSelectedYAxis(dtAxis);
		ILineTrace deadTimeTrace = myPlotter.createLineTrace("Red (%)");
		deadTimeTrace.setTraceType(TraceType.HISTO);
		deadTimeTrace.setLineWidth(10);
		deadTimeTrace.setTraceColor(new Color(null, 255, 0, 0));
		deadTimeTrace.setData(x, dsDeadTime);
		myPlotter.addTrace(deadTimeTrace);
		myPlotter.getSelectedYAxis().setShowMajorGrid(false);
		myPlotter.getSelectedYAxis().setRange(0, 100);

		myPlotter.setSelectedYAxis(primaryAxis);
		myPlotter.setShowLegend(false);
		myPlotter.repaint(false);
	}

	@Override
	protected Double[] getFluoDetectorCountRatesAndDeadTimes() throws DeviceException {
		numElements = dataProvider.getNumElements(COLLECTION_TYPES.XMAP);
		return dataProvider.getFluoDetectorCountRatesAndDeadTimes(COLLECTION_TYPES.XMAP);
	}

	@Override
	protected Double[] getIonChamberValues() throws Exception {
		return dataProvider.getIonChamberValues(COLLECTION_TYPES.XMAP);
	}
}
