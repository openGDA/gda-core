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

package uk.ac.gda.exafs.ui.plot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.exafs.scan.ExafsScanPointCreator;
import gda.scan.IScanDataPoint;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.DataSetPlotData;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.IPlotData;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.exafs.ui.data.ScanObjectManager;

/**
 * This class assumes that the point with energy less than A are to be included in the pre-edge.
 */
public class FourierScanPlotView extends ExafsScanPlotView {
	
	private static final Logger logger = LoggerFactory.getLogger(FourierScanPlotView.class);

	@SuppressWarnings("hiding")
	public static final String ID = "gda.rcp.views.scan.FourierScanPlotView"; //$NON-NLS-1$

	private IPlotData xDataSetData;

	public FourierScanPlotView() {
		super();
		setSampleRate(1500);
	}

	private double kStartEnergy;

	@Override
	protected IPlotData getY(IScanDataPoint... points) {
		if (cachedY.size() <= 3) {
			return null; // cannot estimate edge
		}

		try {
			AbstractDataset energy = AbstractDataset.createFromList(cachedX);
			AbstractDataset lnI0It = AbstractDataset.createFromList(cachedY);

			Double[] edgePos = xafsFittingUtils.estimateEdgePosition(energy, lnI0It);
			if (edgePos != null) {
				double postEdgeStart = xafsFittingUtils.getPostEdgeGap();
				int idxStart = DatasetUtils.findIndexGreaterThanOrEqualTo(energy, edgePos[1] + postEdgeStart);

				if (lnI0It.getSize() > (idxStart + minPlotPoints)) {
					AbstractDataset[] fft = xafsFittingUtils.getFFT(energy, lnI0It);

					this.xDataSetData = new DataSetPlotData(getXAxis(), fft[0]);

					// At the time of writing this code DataSet does not inherit from AbstractDataset!!
					return new DataSetPlotData(getYAxis(), fft[1]);
				}
			}
			this.xDataSetData = new DataSetPlotData(getXAxis(), energy);
			return new DataSetPlotData(getYAxis(), AbstractDataset.zeros(lnI0It.getShape(), lnI0It.getDtype()));
		} catch (Exception e) {
			logger.warn("Exception in XafsFittingUtils calculating FFT",e);
			return null;
		}
	}

	@Override
	protected boolean calculateA() {
		if (!super.calculateA())
			return false;
		this.kStartEnergy = getKStartEnergy();
		return true;
	}

	private double getKStartEnergy() {
		try {
			final IScanParameters params = ScanObjectManager.getCurrentScan();
			if (params == null) {
				return kStartEnergy; // Leave as last calculated
			}
			return ExafsScanPointCreator.getStartOfConstantKRegion(params);
		} catch (Exception e) {
			return a + 300;
		}
	}

	@Override
	protected IPlotData getX(IScanDataPoint... points) {
		return xDataSetData;
	}

	@Override
	protected String getCurrentPlotName(int scanNumber) {
		return "Scan " + scanNumber;
	}

	@Override
	protected String getYAxis() {
		return "\u03c7(R)";
	}

	@Override
	protected String getXAxis() {
		return "R (\u212b)";
	}

	@Override
	protected String getGraphTitle() {
		String scanName = super.getGraphTitle() + " Fourier Transform";
		return scanName;
	}

}
