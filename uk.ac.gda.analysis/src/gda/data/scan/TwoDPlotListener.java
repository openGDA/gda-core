/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.data.scan;

import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.scan.datawriter.DataWriterExtenderBase;
import gda.data.scan.datawriter.IDataWriterExtender;
import gda.jython.InterfaceProvider;
import gda.scan.IScanDataPoint;
import gda.scan.ScanInformation;
import uk.ac.diamond.scisoft.analysis.SDAPlotter;

public class TwoDPlotListener extends DataWriterExtenderBase {
	private static final Logger logger = LoggerFactory.getLogger(TwoDPlotListener.class);

	private String plotPanel;
	private ScanInformation scanInformation;
	private int[] dimensions;
	private DoubleDataset xaxis, yaxis, ds;
	private int x, y, pointNumber;

	public String getPlotPanel() {
		return plotPanel;
	}

	public void setPlotPanel(String plotPanel) {
		this.plotPanel = plotPanel;
	}

	@Override
	public void addData(IDataWriterExtender parent, IScanDataPoint dataPoint) {
		try {
			super.addData(parent, dataPoint);
			if (scanInformation == null) {
				scanInformation = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
				dimensions = scanInformation.getDimensions();
				if (dimensions.length != 2)
					return;
				xaxis = DatasetFactory.zeros(DoubleDataset.class, dimensions[0]);
				xaxis.setName(scanInformation.getScannableNames()[0]);
				yaxis = DatasetFactory.zeros(DoubleDataset.class, dimensions[1]);
				yaxis.setName(scanInformation.getScannableNames()[1]);
				ds = DatasetFactory.zeros(DoubleDataset.class, dimensions[1], dimensions[0]);
				ds.fill(Double.NaN); // Initialise dataset with NaN so auto histogramming will work.
			}

			if (dimensions.length != 2)
				return;

			pointNumber = dataPoint.getCurrentPointNumber();
			x = pointNumber / dimensions[1];
			y = pointNumber % dimensions[1];

			Double[] doubles = dataPoint.getAllValuesAsDoubles();

			xaxis.set(doubles[0], x);
			yaxis.set(doubles[1], y);
			ds.set(doubles[doubles.length - 1], y, x);
			// InterfaceProvider.getTerminalPrinter().print(String.format("x %d y %d xval %5.5f yval %5.5f", x, y, doubles[0], doubles[1]));
			if (plotPanel != null) {
				try {
					SDAPlotter.imagePlot(plotPanel, xaxis, yaxis, ds);
				} catch (Exception e) {
					logger.error("plotting to " + plotPanel + " failed", e);
				}
			}
		} catch (Exception e1) {
			logger.error("error processing 2d scan point", e1);
		}
	}

	@Override
	public void completeCollection(IDataWriterExtender parent) {
		super.completeCollection(parent);
		scanInformation = null;
	}
}