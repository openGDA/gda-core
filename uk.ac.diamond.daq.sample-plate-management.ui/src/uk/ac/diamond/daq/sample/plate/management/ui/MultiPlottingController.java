/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.sample.plate.management.ui;

import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.PointStyle;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.api.document.scanpath.PathInfo;
import uk.ac.diamond.daq.mapping.ui.experiment.PlottingController;

public class MultiPlottingController {
	private static final Logger logger = LoggerFactory.getLogger(PlottingController.class);

	private static final int DEFAULT_PATH_TRACE_WIDTH = 1;

	private static final int DEFAULT_PATH_POINT_SIZE = 4;

	private IPlottingService plottingService;

	private String plottingSystemName = "Map";

	private IPlottingSystem<?> mapPlottingSystem;

	private Color mappingRegionColour;

	private Color scanPathColour;

	private PathInfo lastPathInfo;

	/**
	 * Controls whether the scan path is shown in the plot<br>
	 * Default value must match default value of <code>RegistryToggleState</code>
	 * for command id <code>uk.ac.diamond.daq.mapping.ui.command.showHideMappingPath</code>
	 * in <code>plugin.xml</code>
	 */
	private volatile boolean scanPathVisible = true;

	private volatile boolean updatingROIFromRegion = false;

	public MultiPlottingController() {}

	public MultiPlottingController(IPlottingSystem<?> plottingSystem) {
		this.mapPlottingSystem = plottingSystem;
	}

	public void setPlottingService(IPlottingService plottingService) {
		this.plottingService = plottingService;
	}

	public void setPlottingSystem(IPlottingSystem<Composite> plottingSystem) {
		this.mapPlottingSystem = plottingSystem;
	}

	public void setPlottingSystemName(String plottingSystemName) {
		this.plottingSystemName = plottingSystemName;
	}

	private void checkPlottingSystem() {
		if (mapPlottingSystem == null || mapPlottingSystem.isDisposed()) {
			initPlottingSystem();
		}
		if (mappingRegionColour == null) {
			mappingRegionColour = new Color(null, 255, 196, 0); // orange
			scanPathColour = new Color(null, 160, 32, 240); // purple
		}
	}

	private void initPlottingSystem() {
		// Get and check the plotting system
		if (mapPlottingSystem != null && !mapPlottingSystem.isDisposed()) return;
		mapPlottingSystem = plottingService.getPlottingSystem(plottingSystemName);
		if (mapPlottingSystem == null) {
			throw new IllegalStateException("Couldn't get map plotting system");
		}
		logger.debug("Initialized plotting system");
	}

	public IRegion createNewPlotRegion(final IMappingScanRegionShape scanRegion, String regionName) {
		checkPlottingSystem();

		// Get the scan region from the plotting system if it exists
		IRegion plotRegion = mapPlottingSystem.getRegion(regionName);

		// Keep the visibility and fill settings
		boolean plotRegionVisible = plotRegion == null || plotRegion.isVisible();
		boolean plotRegionFilled = plotRegion == null || plotRegion.isFill();

		// Clean up the current plot
		mapPlottingSystem.removeRegion(plotRegion);
		removePath(regionName);

		try {
			// If you create a new region without adding it the plotting system allows the user to draw it!
			plotRegion = mapPlottingSystem.createRegion(regionName, RegionType.valueOf(scanRegion.whichPlottingRegionType()));
			plotRegion.setFill(true);
			plotRegion.setAlpha(80);
			plotRegion.setRegionColor(mappingRegionColour);
			plotRegion.setLineWidth(5);
			plotRegion.setVisible(plotRegionVisible);
			plotRegion.setFill(plotRegionFilled);
			plotRegion.addROIListener(new IROIListener.Stub() {
				@Override
				public void roiChanged(ROIEvent evt) {
					if (!updatingROIFromRegion) {
						scanRegion.updateFromROI(evt.getROI());
					}
				}
			});
			return plotRegion;
		} catch (Exception e) {
			logger.error("Failed to create new mapping region", e);
			return null;
		}
	}

	public void plotPath(PathInfo pathInfo, String regionName) {
		lastPathInfo = pathInfo;
		replotLastPath(regionName);
	}

	private void replotLastPath(String regionName) {
		checkPlottingSystem();

		//Remove the previous trace
		removePath(regionName);

		// Check if the scan region is currently plotted - if not, we don't want to plot the path either
		// (This fixes a synchronisation bug where the path is added while the scan region drawing event is still
		// active, cancelling the event and making it impossible to draw regions)
		IRegion plotRegion = mapPlottingSystem.getRegion(regionName);

		if (plotRegion != null && lastPathInfo != null) {
			// Make a new line trace and configure it
			ILineTrace pathTrace = mapPlottingSystem.createLineTrace(regionName);
			pathTrace.setTraceColor(scanPathColour);
			pathTrace.setPointStyle(PointStyle.SQUARE);
			pathTrace.setPointSize(DEFAULT_PATH_POINT_SIZE);
			pathTrace.setLineWidth(DEFAULT_PATH_TRACE_WIDTH);
			pathTrace.setVisible(scanPathVisible);

			// Get the point coordinates from the last path info and add them to the trace
			IDataset xData = DatasetFactory.createFromObject(lastPathInfo.getXCoordinates());
			IDataset yData = DatasetFactory.createFromObject(lastPathInfo.getYCoordinates());
			pathTrace.setData(xData, yData);

			mapPlottingSystem.addTrace(pathTrace);
			mapPlottingSystem.setPlotType(PlotType.IMAGE);
			mapPlottingSystem.setShowLegend(false);
		}
	}

	public void removePath(String regionName) {
		checkPlottingSystem();
		ITrace pathTrace = mapPlottingSystem.getTrace(regionName);
		if (pathTrace != null) {
			mapPlottingSystem.removeTrace(pathTrace);
		}
	}

	public void dispose() {
		if (mappingRegionColour != null) {
			mappingRegionColour.dispose();
			mappingRegionColour = null;
		}
		if (scanPathColour != null) {
			scanPathColour.dispose();
			scanPathColour = null;
		}
	}

	public void centrePlotAroundPoint(double xPos, double yPos) {
		double xDelta = (mapPlottingSystem.getSelectedXAxis().getUpper()
				- mapPlottingSystem.getSelectedXAxis().getLower()) / 2;
		double yDelta = (mapPlottingSystem.getSelectedYAxis().getUpper()
				- mapPlottingSystem.getSelectedYAxis().getLower()) / 2;
		mapPlottingSystem.getSelectedXAxis().setRange(xPos - xDelta, xPos + xDelta);
		mapPlottingSystem.getSelectedYAxis().setRange(yPos - yDelta, yPos + yDelta);
	}
}