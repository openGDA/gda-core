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

package uk.ac.gda.server.ncd.scan;

import gda.data.scan.datawriter.DataWriterExtenderBase;
import gda.data.scan.datawriter.IDataWriterExtender;
import gda.jython.InterfaceProvider;
import gda.scan.IScanDataPoint;
import gda.scan.ScanInformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;

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
				xaxis = new DoubleDataset(dimensions[0]);
				xaxis.setName(dataPoint.getScannableHeader()[0]);
				yaxis = new DoubleDataset(dimensions[1]);
				xaxis.setName(dataPoint.getScannableHeader()[1]);
				ds = new DoubleDataset(dimensions[1], dimensions[0]);
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
			InterfaceProvider.getTerminalPrinter().print(String.format("x %d y %d", x, y));
			if (plotPanel != null) {
				try {
					SDAPlotter.imagePlot(plotPanel, yaxis, xaxis, ds);
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
