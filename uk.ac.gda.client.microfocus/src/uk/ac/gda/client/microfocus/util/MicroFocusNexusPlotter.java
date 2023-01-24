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

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.ClickEvent;
import org.eclipse.dawnsci.plotting.api.axis.IClickListener;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;
import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.views.AbstractPlotView;
import uk.ac.gda.client.microfocus.views.ExafsSelectionView;
import uk.ac.gda.client.microfocus.views.scan.MapPlotView;
import uk.ac.gda.client.microfocus.views.scan.MicroFocusElementListView;

public class MicroFocusNexusPlotter {

	public static final String MCA_PLOTTER = "MCA Plot";

	private static final Logger logger = LoggerFactory.getLogger(MicroFocusNexusPlotter.class);

	private MicroFocusMappableDataProvider dataProvider;
	private int serverPlotChannel;

	private Integer lastLCoordinatePlotted = null;

	private Integer lastMCoordinatePlotted = null;

	private IClickListener mouseClickListener;

	public MicroFocusNexusPlotter() {
		super();
		createClickListener();
	}

	private void createClickListener() {
		mouseClickListener = new IClickListener() {
			@Override
			public void clickPerformed(ClickEvent evt) {
				// do conversion this way to always cast down
				int xArrayIndex = (int) evt.getxValue();
				int yArrayIndex = (int) evt.getyValue();

				// is it a ctrl-left click?
				if (evt.isControlDown()) {

					double[] dataXYValues = getDataXYValues(xArrayIndex, yArrayIndex);

					MicroFocusElementListView mfElements = (MicroFocusElementListView) PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage().findView(MicroFocusElementListView.ID);
					mfElements.setLastXYSelection(dataXYValues[0], dataXYValues[1]);
					Double[] xyz = mfElements.getLastXYZSelection();

					ExafsSelectionView selectionView = (ExafsSelectionView) PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage().findView(ExafsSelectionView.ID);
					if (selectionView != null) {
						selectionView.setSelectedPoint(xyz);
					}

					try {
						plotSpectrum(xArrayIndex, yArrayIndex);
					} catch (DatasetException e) {
						logger.error("Problem constructing mappable data", e);
					}
				}
			}

			@Override
			public void doubleClickPerformed(ClickEvent ignoreDoubleClicks) {
			}
		};

		addClickListenerToPlottingSystem(mouseClickListener);
	}

	private void addClickListenerToPlottingSystem(final IClickListener mouseClickListener) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					AbstractPlotView mapplotview = (AbstractPlotView) PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage().showView(MapPlotView.ID);
					IPlottingSystem plottingSystem = (IPlottingSystem) mapplotview.getAdapter(IPlottingSystem.class);
					plottingSystem.addClickListener(mouseClickListener);
				} catch (PartInitException e) {
					logger.warn("Problem linking the MapPlot mouse events to the Exafs Selection view. This view may not work properly. Switch away and back again to this perspective to try again.");
				}
			}
		});
	}

	private double[] getDataXYValues(int xPixel, int yPixel) {
		if (dataProvider == null) {

			// hack warning!
			String xyValues = InterfaceProvider.getCommandRunner().evaluateCommand(
					"map.getMFD().getXYPositions(" + xPixel + "," + yPixel + ")");
			String[] parts = xyValues.split("[\\[\\],]");
			Double x = Double.parseDouble(parts[2]);
			Double y = Double.parseDouble(parts[3]);

			return new double[] { x, y };
		}
		Double[] xData = dataProvider.getXarray();
		Double x = xData[xPixel];

		Double[] yData = dataProvider.getYarray();
		Double y = yData[yPixel];

		return new double[] { x, y };
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
					updateSpectrum();
//					createClickListener();
				} catch (Exception e) {
					logger.error("Error plotting the dataset in MicroFocusNexusPlotter", e);
				}
			}
		});
	}

	/**
	 * For plotting I0 and It data which have no related channel/element.
	 *
	 * @param dataset
	 */
	public void plotDataset(Dataset dataset) {
		try {
			SDAPlotter.imagePlot(MapPlotView.NAME, dataset);
			updateSpectrum();
//			createClickListener();
		} catch (Exception e) {
			logger.error("Error plotting the dataset in MicroFocusNexusPlotter", e);
		}
	}

	/*
	 * For use after the map has been updated
	 */
	private void updateSpectrum() throws DatasetException {
		if (lastLCoordinatePlotted != null && lastMCoordinatePlotted != null) {
			plotSpectrum(lastLCoordinatePlotted, lastMCoordinatePlotted);
		}
	}

	public void plotElement(MicroFocusMappableDataProvider fileDataProvider, final String elementName,
			Integer selectedChannel) {
		dataProvider = fileDataProvider;
		dataProvider.setSelectedElement(elementName);
		dataProvider.setSelectedChannel(selectedChannel);

		double[][] mapData;
		try {
			mapData = dataProvider.constructMappableData();
		} catch (DatasetException e1) {
			logger.error("Problem constructing mappable data", e1);
			return;
		}
		final Dataset plotSet = DatasetFactory.createFromObject(mapData);

		Double[] xData = dataProvider.getXarray();
		final Dataset xDataset = DatasetFactory.createFromObject(xData);

		Double[] yData = dataProvider.getYarray();
		final Dataset yDataset = DatasetFactory.createFromObject(yData);

		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					SDAPlotter.imagePlot(MapPlotView.NAME, xDataset, yDataset, plotSet);
					updateSpectrum();
//					createClickListener();
				} catch (Exception e) {
					logger.error("Error plotting the dataset in MicroFocusNexusPlotter", e);
				}
			}
		});
	}

	/*
	 * Display the MCA of the selected point. The x(l) and y(m) values are the data array indexes, not data values.
	 */
	private void plotSpectrum(final int xPixel, final int yPixel) throws DatasetException {

		if (dataProvider != null) {

			double[] spectrum = dataProvider.getSpectrum(dataProvider.getSelectedChannel(), xPixel, yPixel);
			if (spectrum != null) {
				final Dataset yaxis = DatasetFactory.createFromObject(spectrum);
				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							SDAPlotter.plot(MCA_PLOTTER, yaxis);
						} catch (Exception e) {
							logger.error("Unable to plot the spectrum for " + dataProvider.getSelectedChannel() + " "
									+ xPixel + " " + yPixel, e);
						}
					}
				});
			} else {
				logger.info("No Spectrum available for index " + dataProvider.getSelectedChannel() + " " + xPixel + ","
						+ yPixel);
			}
		} else {
			// server needs to show the spectrum
			JythonServerFacade.getInstance().evaluateCommand(
					"map.getMFD().plotSpectrum(" + serverPlotChannel + "," + xPixel + "," + yPixel + ")");
		}
		lastLCoordinatePlotted = xPixel;
		lastMCoordinatePlotted = yPixel;
	}

	public Double getZValueFromServer() {
		// Again, yuck, but it fits in with how things work until a refactor.
		// This is to get the z value from the server-side object for a map which has been collected,
		// instead of reading from an old map
		return Double.parseDouble(JythonServerFacade.getInstance().evaluateCommand("map.getMFD().getZValue()"));
	}
}
