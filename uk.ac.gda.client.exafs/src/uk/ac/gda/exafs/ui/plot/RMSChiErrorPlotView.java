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
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.DataSetPlotData;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.IPlotData;

public class RMSChiErrorPlotView extends ExafsScanPlotView {

	private static final Logger logger = LoggerFactory.getLogger(RMSChiErrorPlotView.class);

	@SuppressWarnings("hiding")
	public static final String ID = "gda.rcp.views.scan.RMSChiErrorPlotView"; //$NON-NLS-1$

	private ArrayList<Double> cachedE, cachedChi2;
	private DataSetPlotData xDataSetData;

	public RMSChiErrorPlotView() {
		super();
		setSampleRate(3000);
		cachedE = new ArrayList<Double>(89);
		cachedChi2 = new ArrayList<Double>(89);
	}

	@Override
	public void scanStarted() {
		super.scanStarted();
		scanning = true; //FIXME: should be set by a superclass but isn't

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

			if (edgePos != null) {
				double postEdgeStart = xafsFittingUtils.getPostEdgeGap();
				int idxStart = DatasetUtils.findIndexGreaterThanorEqualTo(energy, edgePos[1] + postEdgeStart);

				if (lnI0It.getSize() > (idxStart + minPlotPoints)) {
					Double[]  chi2result = xafsFittingUtils.getChi2Error(energy, lnI0It, 15.0, 25.0);
					if (!(Double.isNaN(chi2result[0])) && !(Double.isNaN(chi2result[1]))) {
						cachedE.add(chi2result[0]);
						cachedChi2.add(chi2result[1]);
					}
					this.xDataSetData = new DataSetPlotData(getXAxis(), AbstractDataset.createFromList(cachedE));
					return new DataSetPlotData(getYAxis(), AbstractDataset.createFromList(cachedChi2));
				}
			}
			cachedE.clear();
			cachedChi2.clear();
			this.xDataSetData = new DataSetPlotData(getXAxis(), AbstractDataset.zeros(energy));
			return new DataSetPlotData(getYAxis(), AbstractDataset.zeros(lnI0It));
		} catch (Exception e) {
			logger.warn("Exception in XafsFittingUtils calculating Chi^2 error", e);
			return null;
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

		return "RMS(\u03c7(k))";
	}

	@Override
	protected String getXAxis() {
		return "Energy (eV)";
	}

	@Override
	protected String getGraphTitle() {

		String scanName = super.getGraphTitle() + " \u03c7(k) RMS error (Estimate)";  
		return  scanName;
	}

}
