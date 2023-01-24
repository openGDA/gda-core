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
public class SubtractedBackgroundScanPlotView extends ExafsScanPlotView {

	private static final Logger logger = LoggerFactory.getLogger(SubtractedBackgroundScanPlotView.class);

	@SuppressWarnings("hiding")
	public static final String ID = "gda.rcp.views.scan.SubtractedBackgroundScanPlotView"; //$NON-NLS-1$

	private DataSetPlotData xDataSetData;

	public SubtractedBackgroundScanPlotView() {
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
					Dataset[] exafs = xafsFittingUtils.getSubtractedBackgroundInK(energy, lnI0It);
					exafs[0].setName(getXAxisName());
					exafs[1].setName(getYAxisName());
					this.xDataSetData = new DataSetPlotData(getXAxisName(), exafs[0]);
					return new DataSetPlotData(getYAxisName(), exafs[1]);
				}
			}
			energy.setName(getXAxisName());
			return null;
		} catch (Exception e) {
			logger.warn("Exception in XafsFittingUtils calculating Subtracted background",e);
		}
		return null;

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
			if (params == null)
				return kStartEnergy; // Leave as last calculated

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
		int kw = xafsFittingUtils.getKweight();
		String prefix = "";
		switch (kw) {
		case 1:
			prefix = "k";
			break;
		case 2:
			prefix = "k\u00b2";
			break;
		case 3:
			prefix = "k\u00b3";
			break;
		default:
			break;
		}
		return prefix + "\u03c7(k)";
	}

	@Override
	protected String getXAxisName() {
		return "k (\u212b\u207b\u00b9)";
	}
}
