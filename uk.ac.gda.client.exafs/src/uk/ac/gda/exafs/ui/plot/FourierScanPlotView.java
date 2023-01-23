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

package uk.ac.gda.exafs.ui.plot;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.exafs.scan.ExafsScanPointCreator;
import gda.scan.IScanDataPoint;
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
			Dataset energy = DatasetFactory.createFromList(cachedX);
			Dataset lnI0It = DatasetFactory.createFromList(cachedY);

			Double[] edgePos = xafsFittingUtils.estimateEdgePosition(energy, lnI0It);
			if (edgePos != null) {
				double postEdgeStart = xafsFittingUtils.getPostEdgeGap();
				int idxStart = DatasetUtils.findIndexGreaterThanOrEqualTo(energy, edgePos[1] + postEdgeStart);

				if (lnI0It.getSize() > (idxStart + minPlotPoints)) {
					Dataset[] fft = xafsFittingUtils.getFFT(energy, lnI0It);
					fft[0].setName(getXAxisName());
					this.xDataSetData = new DataSetPlotData(getXAxisName(), fft[0]);

					// At the time of writing this code DataSet does not inherit from Dataset!!
					fft[1].setName(getYAxisName());
					return new DataSetPlotData(getYAxisName(), fft[1]);
				}
			}
			energy.setName(getXAxisName());
			return null;
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
	protected String getYAxisName() {
		return "\u03c7(R)";
	}

	@Override
	protected String getXAxisName() {
		return "R (\u212b)";
	}
}
