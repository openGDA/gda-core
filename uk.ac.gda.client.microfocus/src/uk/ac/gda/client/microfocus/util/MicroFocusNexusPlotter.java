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

import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.region.IROIListener;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.region.IRegionListener;
import org.dawnsci.plotting.api.region.ROIEvent;
import org.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.roi.PointROI;
import uk.ac.gda.client.microfocus.views.scan.MapPlotView;
import uk.ac.gda.client.microfocus.views.scan.MicroFocusElementListView;

public class MicroFocusNexusPlotter extends IROIListener.Stub {

	public static final String MCA_PLOTTER = "MCA Plot";

	private static final Logger logger = LoggerFactory.getLogger(MicroFocusNexusPlotter.class);

	private boolean createdListener = false;
	private MicroFocusMappableDataProvider dataProvider;
	private IRegion region;
	private IRegion lastregion;
	private int regionUID = 0; // so all regions have a unique name
	private int serverPlotChannel;

	public MicroFocusNexusPlotter() {
		super();
	}

	public MicroFocusMappableDataProvider getDataProvider() {
		return dataProvider;
	}

	public void setDataProvider(MicroFocusMappableDataProvider dataProvider) {
		this.dataProvider = dataProvider;
	}

	public void plotMapFromServer(final String elementName, final int selectedChannel) {
		this.dataProvider = null;
		this.serverPlotChannel = selectedChannel;
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					JythonServerFacade.getInstance().evaluateCommand(
							"map.getMFD().displayPlot(\"" + elementName + "\"," + selectedChannel + ")");
					createPointRegionListener();
				} catch (Exception e) {
					logger.error("Error plotting the dataset in MicroFocusNexusPlotter", e);
				}
			}
		});
	}

	public void plotDataset(AbstractDataset dataset) {
		try {
			SDAPlotter.imagePlot(MapPlotView.NAME, dataset);
		} catch (Exception e) {
			logger.error("Error plotting the dataset in MicroFocusNexusPlotter", e);
		}
	}

	public void plotElement(final String elementName, Integer selectedChannel) {
		ObjectStateManager.setActive(dataProvider);

		dataProvider.setSelectedElement(elementName);
		dataProvider.setSelectedChannel(selectedChannel);

		double[][] mapData = dataProvider.constructMappableData();
		final AbstractDataset plotSet = AbstractDataset.array(mapData);

		Double[] xData = dataProvider.getXarray();
		final AbstractDataset xDataset = AbstractDataset.array(xData);

		Double[] yData = dataProvider.getYarray();
		final AbstractDataset yDataset = AbstractDataset.array(yData);

		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					SDAPlotter.imagePlot(MapPlotView.NAME, xDataset, yDataset, plotSet);
					createPointRegionListener();
				} catch (Exception e) {
					logger.error("Error plotting the dataset in MicroFocusNexusPlotter", e);
				}
			}
		});
	}

	private void createPointRegionListener() throws Exception {
		registerRegionListener();
		updateRegion();
	}

	@Override
	public void update(ROIEvent evt) {
		try {
			// the ROI is not in the data values, but the pixel index number
			PointROI selectedPixel = (PointROI) evt.getROI();
			// cast to int to round down, not round to nearest int
			int xArrayIndex = (int) selectedPixel.getPointX();
			int yArrayIndex = (int) selectedPixel.getPointY();

			if (dataProvider == null || !ObjectStateManager.isActive(dataProvider)) {
				JythonServerFacade.getInstance().runCommand(
						"map.getMFD().plotSpectrum(" + serverPlotChannel + "," + xArrayIndex + "," + yArrayIndex + ")");

				// hack warning!
				String xyValues = InterfaceProvider.getCommandRunner().evaluateCommand(
						"map.getMFD().getXYPositions(" + xArrayIndex + "," + yArrayIndex + ")");
				String[] parts = xyValues.split("[\\[\\],]");
				Double x = Double.parseDouble(parts[2]);
				Double y = Double.parseDouble(parts[3]);

				MicroFocusElementListView mfElements = (MicroFocusElementListView) PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage().findView(MicroFocusElementListView.ID);
				mfElements.setLastXYSelection(x, y);
			} else {
				Double[] xData = dataProvider.getXarray();
				Double selectedXDataValue = xData[xArrayIndex];

				Double[] yData = dataProvider.getYarray();
				Double selectedYDataValue = yData[yArrayIndex];

				MicroFocusElementListView mfElements = (MicroFocusElementListView) PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage().findView(MicroFocusElementListView.ID);
				mfElements.setLastXYSelection(selectedXDataValue, selectedYDataValue);

				displayPlot(xArrayIndex, yArrayIndex);
			}

			updateRegion();

		} catch (Throwable ne) {
			logger.debug(ne.getMessage());
		}
	}

	private void updateRegion() throws PartInitException, Exception {
		IViewPart mapplotview = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.showView(MapPlotView.ID);
		IPlottingSystem system = (IPlottingSystem) mapplotview.getAdapter(IPlottingSystem.class);
		if (lastregion != null) {
			system.removeRegion(lastregion);
		}
		lastregion = region;
		regionUID++;
		region = system.createRegion("Select pixel " + regionUID, IRegion.RegionType.POINT);
	}

	/*
	 * Display the MCA of the selected point. The x and y values are the data array indexes, not data values.
	 */
	private void displayPlot(final int l, final int m) {
		if (dataProvider != null && ObjectStateManager.isActive(dataProvider)) {
			double[] spectrum = null;
			String detectorName = dataProvider.getDetectorName();
			if (detectorName.equals("xmapMca")) {
				spectrum = dataProvider.getSpectrum(dataProvider.getSelectedChannel(), l, m);
			} else {
				spectrum = dataProvider.getSpectrum(dataProvider.getSelectedChannel(), m, l);
			}

			if (spectrum != null) {
				final AbstractDataset yaxis = AbstractDataset.array(spectrum);

				logger.info("Plotting spectrum for channel " + dataProvider.getSelectedChannel() + ", pixel " + l + ","
						+ m);
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							SDAPlotter.plot(MCA_PLOTTER, yaxis);
						} catch (Exception e) {
							logger.error("Unable to plot the spectrum for " + dataProvider.getSelectedChannel() + " "
									+ l + " " + m, e);
						}
					}
				});
			} else {
				logger.info("No Spectrum available for index " + dataProvider.getSelectedChannel() + " " + l + "," + m);
			}
		} else {
			// server needs to show the spectrum
			logger.info("Plotting spectrum for element 0," + l + "," + m);
			JythonServerFacade.getInstance().evaluateCommand(
					"map.getMFD().plotSpectrum(" +serverPlotChannel+ "," + l + "," + m + ")");
		}

	}

	private void registerRegionListener() throws Exception {
		if (!createdListener) {
			IViewPart mapplotview = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(MapPlotView.ID);

			IPlottingSystem system = (IPlottingSystem) mapplotview.getAdapter(IPlottingSystem.class);
			system.addRegionListener(new IRegionListener.Stub() {
				@Override
				public void regionCreated(RegionEvent evt) {
					if (evt.getRegion().getRegionType() != RegionType.POINT)
						return;
					evt.getRegion().addROIListener(MicroFocusNexusPlotter.this);
				}

				@Override
				public void regionsRemoved(RegionEvent evt) {
					// classcast exception inside evt.getRegion(), so just skip this code
					// if (evt.getRegion().getRegionType() != RegionType.POINT)
					// return;
					// evt.getRegion().removeROIListener(MicroFocusNexusPlotter.this);
				}
			});

			createdListener = true;
		}
	}

	@Override
	public void roiDragged(ROIEvent evt) {
		// Do nothing
	}

	@Override
	public void roiChanged(ROIEvent evt) {
		update(evt);

	}

	@Override
	public void roiSelected(ROIEvent evt) {
		update(evt);

	}

}
