/*-
 * Copyright © 2026 Diamond Light Source Ltd.
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

import java.util.Arrays;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.python.core.PyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.scan.datawriter.DataWriterExtenderBase;
import gda.data.scan.datawriter.IDataWriterExtender;
import gda.jython.InterfaceProvider;
import gda.scan.IScanDataPoint;
import gda.scan.ScanInformation;
import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.gda.api.scan.IExplicitScanObject;
import uk.ac.gda.api.scan.IScanObject;

public class TwoDPathScanPlotListener extends DataWriterExtenderBase {
	private static final Logger logger = LoggerFactory.getLogger(TwoDPathScanPlotListener.class);
	private static final String PATH_GROUP_NAME = "pathgroup";

	private String plotPanel;
	private ScanInformation scanInformation;
	private int[] dimensions;
	private DoubleDataset xaxis;
	private DoubleDataset  yaxis;
	private DoubleDataset  ds;
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
			}

			switch (checkValidScanDimensions(dataPoint)) {
			case 1:
				process2DPathScan(dataPoint);
				break;
			case 2:
				process2DScan(dataPoint);
				break;
			default:
				logger.trace("Scan is neither normal 2D nor 1D pathscan with 2 scannables - will not be plotted.");
				break;
			}
		} catch (Exception e1) {
			logger.error("error processing 2d scan point", e1);
		}
	}


	private int checkValidScanDimensions(IScanDataPoint dataPoint) {
		if (dimensions.length == 2) return 2;
		if ((dimensions.length == 1)
				&& (Arrays.stream(scanInformation.getScannableNames()).anyMatch(i->i.contains(PATH_GROUP_NAME)))
				&& (dataPoint.getScannable(PATH_GROUP_NAME).getInputNames().length == 2))  return 1;
		return 0;
	}


	private void setupInitialDataset(int dimensionX, int dimensionY) {
		ds = DatasetFactory.zeros(DoubleDataset.class, dimensionX, dimensionY);
		ds.fill(Double.NaN);
	}


	private void process2DScan(IScanDataPoint dataPoint) {
		pointNumber = dataPoint.getCurrentPointNumber();
		x = pointNumber / dimensions[1];
		y = pointNumber % dimensions[1];

		Double[] doubles = dataPoint.getAllValuesAsDoubles();
		if (pointNumber == 0) {
			xaxis = DatasetFactory.zeros(DoubleDataset.class, dimensions[0]);
			xaxis.setName(scanInformation.getScannableNames()[0]);
			xaxis.fill(Double.NaN);
			yaxis = DatasetFactory.zeros(DoubleDataset.class, dimensions[1]);
			yaxis.setName(scanInformation.getScannableNames()[1]);
			yaxis.fill(Double.NaN);
			setupInitialDataset(dimensions[0], dimensions[1]);
		}

		xaxis.set(doubles[0], x);
		yaxis.set(doubles[1], y);
		ds.set(doubles[doubles.length - 1], x, y);

		updatePlot(plotPanel,ds, yaxis, xaxis);
	}

	private void process2DPathScan(IScanDataPoint dataPoint) {
		pointNumber = dataPoint.getCurrentPointNumber();
		Double[] doubles = dataPoint.getAllValuesAsDoubles();
		logger.debug("Values as doubles: {}", Arrays.toString(doubles));

		if (pointNumber == 0) {
			//Create axes and fill them in advance
			DoubleDataset rawXaxis = DatasetFactory.zeros(DoubleDataset.class, dataPoint.getNumberOfPoints());
			DoubleDataset rawYaxis = DatasetFactory.zeros(DoubleDataset.class, dataPoint.getNumberOfPoints());
			for (IScanObject scanObject : dataPoint.getScanObjects()) {
				if (scanObject instanceof IExplicitScanObject explisitScanObject) {
					for (int i=0; i<dimensions[0];i++) {
						rawXaxis.set(((PyList) explisitScanObject.getPoint(i)).get(0),i);
						rawYaxis.set(((PyList) explisitScanObject.getPoint(i)).get(1),i);
					}
				}
			}
			xaxis = (DoubleDataset) DatasetFactory.createFromObject(Arrays.stream(rawXaxis.getData()).distinct().toArray());
			xaxis.setName(dataPoint.getScannable(PATH_GROUP_NAME).getInputNames()[0]);
			yaxis = (DoubleDataset) DatasetFactory.createFromObject(Arrays.stream(rawYaxis.getData()).distinct().toArray());
			yaxis.setName(dataPoint.getScannable(PATH_GROUP_NAME).getInputNames()[1]);
			setupInitialDataset(xaxis.getShape()[0],yaxis.getShape()[0]);
		}
		if (dimensions[0]==xaxis.getShape()[0]) {
			x = pointNumber;
			y = pointNumber;
		} else {
			x = pointNumber / yaxis.getShape()[0];
			y = (x%2 == 0)? pointNumber % yaxis.getShape()[0]: yaxis.getShape()[0]-pointNumber % yaxis.getShape()[0]-1;
		}
		ds.set(doubles[doubles.length - 1], x, y);

		updatePlot(plotPanel, ds, yaxis, xaxis);
	}


	private void updatePlot(String plotPanel, Dataset ds, Dataset xaxis, Dataset yaxis) {
		if (plotPanel != null) {
			try {
				SDAPlotter.imagePlot(plotPanel, xaxis, yaxis, ds);
			} catch (Exception e) {
				logger.error("plotting to " + plotPanel + " failed", e);
			}
		}
	}

	@Override
	public void completeCollection(IDataWriterExtender parent) {
		super.completeCollection(parent);
		scanInformation = null;
	}

}
