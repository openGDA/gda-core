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

import gda.scan.IScanDataPoint;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.DataSetPlotData;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.IPlotData;

public class ChiSquareErrorPlotView extends ExafsScanPlotView {

	private static final Logger logger = LoggerFactory.getLogger(ChiSquareErrorPlotView.class);

	@SuppressWarnings("hiding")
	public static final String ID = "gda.rcp.views.scan.ChiSquareErrorPlotView"; //$NON-NLS-1$

	private ArrayList<Double> cachedE, cachedChi2;

	public ChiSquareErrorPlotView() {
		super();
		setSampleRate(3000);
		cachedE = new ArrayList<Double>(89);
		cachedChi2 = new ArrayList<Double>(89);
	}

	@Override
	public void scanStarted() {
		super.scanStarted();

		if (cachedE == null)
			cachedE = new ArrayList<Double>(89);
		else
			cachedE.clear();

		if (cachedChi2 == null)
			cachedChi2 = new ArrayList<Double>(89);
		else
			cachedChi2.clear();
	}

	@Override
	protected IPlotData getY(IScanDataPoint... points) {
		if (cachedY.size() <= 3){
			return null; // cannot estimate edge
		}

		final AbstractDataset energy = AbstractDataset.createFromList(cachedX);
		final AbstractDataset lnI0It = AbstractDataset.createFromList(cachedY);
		
		try {
			Double[] edgePos = xafsFittingUtils.estimateEdgePosition(energy, lnI0It);

			if (edgePos != null)
				if (edgePos[0] > (edgePos[1] + 200.0)) {
					xafsFittingUtils.setDoFilter(false);
					Double[]  chi2result = xafsFittingUtils.getChi2Error(energy, lnI0It, 15.0, 25.0);
					if (!(Double.isNaN(chi2result[0])) && !(Double.isNaN(chi2result[1]))) {
						cachedE.add(chi2result[0]);
						cachedChi2.add(chi2result[1]);
					}
					return new DataSetPlotData(getYAxis(), AbstractDataset.createFromList(cachedChi2));
				}
			cachedE.clear();
			cachedChi2.clear();
			return null;
		} catch (Exception e) {
			logger.warn("Exception in XafsFittingUtils calculating Chi^2 error", e);
			return null;
		}
	}

	@Override
	protected IPlotData getX(IScanDataPoint... points) {
		if (cachedE.size() <= 3){
			return null;
		}
		return new DataSetPlotData(getXAxis(), AbstractDataset.createFromList(cachedE));
	}

	@Override
	protected String getCurrentPlotName(int scanNumber) {

		return "Scan " + scanNumber;
	}

	@Override
	protected String getYAxis() {

		return "Chi^2 (R)";
	}

	@Override
	protected String getXAxis() {
		return "Energy (eV)";
	}

	@Override
	protected String getGraphTitle() {

		return "Real-space chi^2 error (Estimate)";
	}

}
