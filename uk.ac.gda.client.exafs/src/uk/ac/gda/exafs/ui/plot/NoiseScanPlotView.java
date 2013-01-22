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

import org.eclipse.jface.preference.IPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.scan.IScanDataPoint;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.DataSetPlotData;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.IPlotData;
import uk.ac.diamond.scisoft.spectroscopy.rcp.SpectroscopyRCPActivator;
import uk.ac.diamond.scisoft.spectroscopy.rcp.preferences.XafsPreferences;

public class NoiseScanPlotView extends ExafsScanPlotView {

	private static final Logger logger = LoggerFactory.getLogger(NoiseScanPlotView.class);

	@SuppressWarnings("hiding")
	public static final String ID = "gda.rcp.views.scan.NoiseScanPlotView"; //$NON-NLS-1$

	public NoiseScanPlotView() {
		super();
		setSampleRate(1000);
	}

	/**
	 * Assumes that the cachedLn and cachedX arrays are fully up to date
	 */
	@Override
	protected IPlotData getY(IScanDataPoint... points) {

		if (cachedY.size() <= 3) {
			return null; // cannot estimate noise
		}

		try {
			final AbstractDataset energy = AbstractDataset.createFromList(cachedX);
			final AbstractDataset lnI0It = AbstractDataset.createFromList(cachedY);
			IPreferenceStore preferences = SpectroscopyRCPActivator.getDefault().getPreferenceStore();
	        int windowSize = preferences.getInt(XafsPreferences.NOISE_WINDOW);
	        int polyOrder = preferences.getInt(XafsPreferences.NOISE_ORDER);
			final AbstractDataset medi = xafsFittingUtils.getPolynomialSmoothed(energy, lnI0It, windowSize, polyOrder);
			final AbstractDataset noise = (new DoubleDataset(lnI0It)).isubtract(medi);
			return new DataSetPlotData(getYAxis(), noise);
		} catch (Exception e) {
			logger.warn("Cannot calculate noise profile", e);
			return null;
		}
	}

	@Override
	protected String getCurrentPlotName(int scanNumber) {
		return "Scan " + scanNumber + " [Noise Profile]";
	}

	@Override
	protected String getYAxis() {
		return "Noise";
	}

	@Override
	protected String getGraphTitle() {
		String scanName = super.getGraphTitle() + " Noise Profile";  
		return  scanName;
	}

}
