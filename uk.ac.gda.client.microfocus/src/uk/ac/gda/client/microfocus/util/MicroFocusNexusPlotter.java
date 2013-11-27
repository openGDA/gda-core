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

package uk.ac.gda.client.microfocus.util;

import gda.jython.JythonServerFacade;

import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.ROIEvent;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.rcp.views.AbstractPlotView;
import uk.ac.diamond.scisoft.analysis.roi.PointROI;
import uk.ac.gda.client.microfocus.views.scan.MapPlotView;

public class MicroFocusNexusPlotter implements IROIListener {

	public static final String MCA_PLOTTER = "MCA Plot";

	private static final Logger logger = LoggerFactory.getLogger(MicroFocusNexusPlotter.class);

	private MicroFocusMappableDataProvider dataProvider;

	private IRegion region;

	public MicroFocusMappableDataProvider getDataProvider() {
		return dataProvider;
	}

	public void setDataProvider(MicroFocusMappableDataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}

	public void plotDataset(AbstractDataset dataset) {
		try {
			SDAPlotter.imagePlot(MapPlotView.NAME, dataset);
		} catch (Exception e) {
			logger.error("Error plotting the dataset in MicroFocusNexusPlotter", e);
		}
	}

	private void removeRegion() {
		if (region != null) {
			region.removeROIListener(this);
			region.remove();
			region = null;
		}
	}

	public void plotElement(final String elementName) {


		dataProvider.setSelectedElement(elementName);
		double[][] mapData = dataProvider.constructMappableData();
		final AbstractDataset plotSet = AbstractDataset.array(mapData);
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					
					SDAPlotter.imagePlot(MapPlotView.NAME, plotSet);
					createPointRegionListener();
				} catch (Exception e) {
					logger.error("Error plotting the dataset in MicroFocusNexusPlotter", e);
				}
			}
		});
	}

	private void createPointRegionListener() throws Exception {
//		removeRegion();
		IViewPart mapplotview = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.showView(MapPlotView.ID);
		if (region == null) {
//			((AbstractPlotView) mapplotview).getPlottingSystem().removeRegion(region);
		
		region = ((AbstractPlotView) mapplotview).getPlottingSystem().createRegion("Select pixel",
				IRegion.RegionType.POINT);
		region.addROIListener(this);
		}
	}

	@Override
	public void roiChanged(ROIEvent evt) {

		PointROI selectedPixel = (PointROI) evt.getROI();

		int x = (int) Math.round(selectedPixel.getPointX());
		int y = (int) Math.round(selectedPixel.getPointY());

		displayPlot(x, y);

	}

	private void displayPlot(final int l, final int m) {
		if (dataProvider != null && ObjectStateManager.isActive(dataProvider)) {
			double[] spectrum = null;
			String detectorName = dataProvider.getDetectorName();
			if (detectorName.equals("xmapMca"))
				spectrum = dataProvider.getSpectrum(0, l, m);
			else
				spectrum = dataProvider.getSpectrum(0, m, l);

			if (spectrum != null) {
				final AbstractDataset yaxis = AbstractDataset.array(spectrum);

				logger.info("Plotting spectrum for element 0," + l + "," + m);
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							SDAPlotter.plot(MCA_PLOTTER, yaxis);
						} catch (Exception e) {
							logger.error("Unable to plot the spectrum for " + l + " " + m, e);
						}
					}
				});
			} else {
				logger.info("No Spectrum available for index " + l + "," + m);
			}
		} else {
			// server needs to show the spectrum
			logger.info("Plotting spectrum for element 0," + l + "," + m);
			JythonServerFacade.getInstance().evaluateCommand("map.getMFD().plotSpectrum(0," + l + "," + m + ")");
		}

	}

	@Override
	public void roiDragged(ROIEvent evt) {
		//
		logger.info("get heer");
	}

	@Override
	public void roiSelected(ROIEvent evt) {
		//
		logger.info("get heer");
	}
}
