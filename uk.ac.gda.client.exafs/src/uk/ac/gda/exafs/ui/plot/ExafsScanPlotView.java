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

import java.util.ArrayList;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.util.ScanDataPointEvent;
import gda.rcp.views.scan.AbstractCachedScanPlotView;
import gda.scan.IScanDataPoint;
import gda.util.exafs.Element;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.DataSetPlotData;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.IPlotData;
import uk.ac.diamond.scisoft.spectroscopy.fitting.XafsFittingUtils;
import uk.ac.diamond.scisoft.spectroscopy.rcp.SpectroscopyRCPActivator;
import uk.ac.diamond.scisoft.spectroscopy.rcp.preferences.XafsPreferences;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.QEXAFSParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.beans.exafs.XesScanParameters;
import uk.ac.gda.beans.microfocus.MicroFocusScanParameters;
import uk.ac.gda.exafs.ui.data.ScanObjectManager;

/**
 * This class assumes that the point with energy less than A are to be included in the pre-edge.
 * <p>
 * Note that the cachedY
 * array inherited from the AbstractCachedScanPlotView class is filled with the latest ln(I0/It) data and does not
 * contain the actual data to be plotted. That is provided by the subclasses' implementation of getY.
 */
abstract class ExafsScanPlotView extends AbstractCachedScanPlotView {

	private static final Logger logger = LoggerFactory.getLogger(ExafsScanPlotView.class);

	public static final String ID = "gda.rcp.views.scan.ExafsScanPlotView"; //$NON-NLS-1$

	protected static final int MIN_PLOT_POINTS = 10;  // Minimal number of points needed to start plotting

	protected double a = Double.NaN;

	protected final XafsFittingUtils xafsFittingUtils;

	protected ExafsScanPlotView() {
		super();
		xafsFittingUtils = new XafsFittingUtils();
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		setXafsPreferences();

		SpectroscopyRCPActivator.getDefault().getPreferenceStore().addPropertyChangeListener(event -> {
			switch (event.getProperty()) {
				case XafsPreferences.CHEBYSHEV:
					xafsFittingUtils.setUseChebyshevSeries((Boolean) event.getNewValue());
					break;
				case XafsPreferences.FILTER:
					xafsFittingUtils.setDoFilter((Boolean) event.getNewValue());
					break;
				case XafsPreferences.KWEIGHT:
					xafsFittingUtils.setKweight((Integer) event.getNewValue());
					break;
				case XafsPreferences.MAXORDER:
					xafsFittingUtils.setMaxPolynomialOrder((Integer) event.getNewValue());
					break;
				case XafsPreferences.OVERSAMPLE:
					xafsFittingUtils.setDoOversample((Boolean) event.getNewValue());
					break;
				case XafsPreferences.PRE_EDGE:
					xafsFittingUtils.setPreEdgeGap((Double) event.getNewValue());
					break;
				case XafsPreferences.POST_EDGE:
					xafsFittingUtils.setPostEdgeGap((Double) event.getNewValue());
					break;
				default:
					// do nothing
			}
		});
	}

	@Override
	public void scanDataPointChanged(ScanDataPointEvent e) {
		try {
			IScanParameters curScan = ScanObjectManager.getCurrentScan();
			if (!(curScan instanceof MicroFocusScanParameters)) {
				super.scanDataPointChanged(e);
//				scanID = e.getCurrentPoint().getScanIdentifier();
			}
		} catch (Exception exp) {
			logger.error("Unable to determine the scan type", exp);
		}
	}

	@Override
	public void scanStarted() {
		try {
			super.scanStarted();
			IScanParameters curScan = ScanObjectManager.getCurrentScan();
			if (curScan != null && !(curScan instanceof MicroFocusScanParameters)) {
				calculateA();
			}
		} catch (Exception e) {
			logger.error("Unable to determine the scan type", e);
		}
	}

	protected boolean calculateA() {
		if (!Double.isNaN(a))
			return true;
		try {
			final IScanParameters params = ScanObjectManager.getCurrentScan();
			if (params == null)
				return false; // Leave a as last calculated

			if (params instanceof XasScanParameters xasScanParams) {
				final Double A = xasScanParams.getA();
				if (A == null) {
					final Element element = Element.getElement(xasScanParams.getElement());
					final double coreHole = element.getCoreHole(xasScanParams.getEdge());
					final double edgeEn = (xasScanParams.getEdgeEnergy() == null) ? element.getEdgeEnergy(xasScanParams
							.getEdge()) : xasScanParams.getEdgeEnergy();
					this.a = edgeEn - (xasScanParams.getGaf1() * coreHole);
				} else {
					this.a = A;
				}
			} else if (params instanceof XanesScanParameters xanesScanParams) {
				this.a = xanesScanParams.getRegions().get(0).getEnergy();
			} else if (params instanceof XesScanParameters xesScanParams) {
				this.a = xesScanParams.getMonoInitialEnergy();
			} else if (params instanceof QEXAFSParameters qexafsScanParams) {
				this.a = qexafsScanParams.getInitialEnergy();
			} else {
				throw new IllegalArgumentException("Undefined scan parameters encountered");
			}
		} catch (Exception e) {
			logger.error("Cannot get scan parameters from " + ScanObjectManager.getCurrentScan(), e);
			this.a = 10000d;
		}
		return true;
	}

	@Override
	protected String getCurrentPlotName(int scanNumber) {
		return "Scan: " + scanNumber;
	}

	@Override
	protected void plotPointsFromService() throws Exception {

		if (!calculateA())
			return;
		super.plotPointsFromService();
	}

	/**
	 * Recalculates A and then adds to the cachedY and cachedX buffers.
	 * <p>
	 * Subclasses should then update/recalculate the cachedY buffer within their getY methods.
	 */
	@Override
	protected void updateCache(ArrayList<IScanDataPoint> collection, int startIndex) {
		calculateA();

		if (cachedX == null)
			cachedX = new ArrayList<Double>(89);
		if (cachedY == null)
			cachedY = new ArrayList<Double>(89);
		for (int i = startIndex; i < collection.size(); i++) {
			IScanDataPoint point = collection.get(i);
			double x = point.getAllValuesAsDoubles()[0];
			double ffi0 = getDetectorValueForHeaderName(point, "ff");
			double ffi1 = getDetectorValueForHeaderName(point, "ffi1");
			double ff = getDetectorValueForHeaderName(point, "ff");
			double i0 = getDetectorValueForHeaderName(point, "i0");
			double i1 = getDetectorValueForHeaderName(point, "i1");
			double it = getDetectorValueForHeaderName(point, "it");
			if (Double.isNaN(i0) && Double.isNaN(i1)) {
			  // do nothing
			} else if (!Double.isNaN(ffi0)) {
				cachedY.add(ffi0);
				cachedX.add(x);
			} else if (!Double.isNaN(ffi1)) {
				cachedY.add(ffi1);
				cachedX.add(x);
			} else if (!Double.isNaN(ff)) {
				Double y = ff / i0;
				if (y.isInfinite() || y.isNaN()) {
					y = 0.0;
				}
				cachedY.add(y);
				cachedX.add(x);
			} else if (!Double.isNaN(it)) {
				Double y = Math.log(i0 / it);
				if (y.isInfinite() || y.isNaN()){
					y = 0.0;
				}
				cachedY.add(y);
				cachedX.add(x);
			}
		}
	}

	private void setXafsPreferences() {
		IPreferenceStore preferences = SpectroscopyRCPActivator.getDefault().getPreferenceStore();
        xafsFittingUtils.setDoFilter(preferences.getBoolean(XafsPreferences.FILTER));
        xafsFittingUtils.setDoOversample(preferences.getBoolean(XafsPreferences.OVERSAMPLE));
        xafsFittingUtils.setKweight(preferences.getInt(XafsPreferences.KWEIGHT));
        xafsFittingUtils.setMaxPolynomialOrder(preferences.getInt(XafsPreferences.MAXORDER));
        xafsFittingUtils.setPostEdgeGap(preferences.getDouble(XafsPreferences.POST_EDGE));
        xafsFittingUtils.setPreEdgeGap(preferences.getDouble(XafsPreferences.PRE_EDGE));
        xafsFittingUtils.setUseChebyshevSeries(preferences.getBoolean(XafsPreferences.CHEBYSHEV));
	}

	@Override
	protected IPlotData getX(IScanDataPoint... points) {
		Dataset xValues = DatasetFactory.createFromList(cachedX);
		xValues.setName(getXAxisName());
		return new DataSetPlotData(getXAxisName(), xValues);
	}

	@Override
	protected String getXAxisName() {
		return "Energy (eV)";
	}
}
