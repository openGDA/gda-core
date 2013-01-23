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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.DataSetPlotData;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.IPlotData;

/**
 * This class assumes that the point with energy less than A are to be included in the pre-edge.
 */
public class DerivativeScanPlotView extends ExafsScanPlotView {

	private static final Logger logger = LoggerFactory.getLogger(DerivativeScanPlotView.class);

	@SuppressWarnings("hiding")
	public static final String ID = "gda.rcp.views.scan.DerivativeScanPlotView"; //$NON-NLS-1$

	public DerivativeScanPlotView() {
		super();
		setSampleRate(1000);
	}

	/**
	 * Assumes that the cachedLn and cachedX arrays are fully up to date
	 */
	@Override
	protected IPlotData getY(IScanDataPoint... points) {

		if (cachedY.size() <= 3) {
			return null; // cannot estimate derivative
		}

		try {
			final AbstractDataset energy = AbstractDataset.createFromList(cachedX);
			final AbstractDataset lnI0It = AbstractDataset.createFromList(cachedY);
			AbstractDataset derv = Maths.derivative(energy, lnI0It, 1);
			return new DataSetPlotData(getYAxis(), derv);
		} catch (Exception e) {
			logger.warn("Exception in XafsFittingUtils calculating Derivative",e);
			return null;
		}
	}

	@Override
	protected String getCurrentPlotName(int scanNumber) {
		return "Scan " + scanNumber + " [First Derivative]";
	}

	@Override
	protected String getYAxis() {
		return "d(f[I0,It])/dE";
	}

	@Override
	protected String getGraphTitle() {
		String scanName = super.getGraphTitle() + " First Derivative";
		return scanName;
	}

}
