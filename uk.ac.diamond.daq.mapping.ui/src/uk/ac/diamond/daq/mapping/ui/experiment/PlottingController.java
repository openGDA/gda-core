/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

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
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;

/**
 * A wrapper around an {@link IPlottingSystem} for drawing mapping regions and paths.
 * The plotting system to be used can be set by calling {@link #setPlottingSystem(IPlottingSystem)}.
 * If this is done, then the plotting system to use will be 'Map' plotting system,
 * as retrieved by calling {@link IPlottingService#getPlottingSystem(String)} with the
 * value 'Map'. This name can be overridden by calling {@link #setPlottingSystemName(String)}.
 */
public class PlottingController {

	public static final String MAPPING_REGION_NAME = "Mapping Scan Region";
	public static final String MAPPING_PATH_NAME = "Mapping Scan Path";

	private static final Logger logger = LoggerFactory.getLogger(PlottingController.class);
	private static final int DEFAULT_PATH_TRACE_WIDTH = 1;
	private static final int DEFAULT_PATH_POINT_SIZE = 4;

	private IPlottingService plottingService;
	private String plottingSystemName = "Map";
	private IPlottingSystem<?> mapPlottingSystem;
	private Color mappingRegionColour;
	private Color scanPathColour;
	private PathInfo lastPathInfo;

	private volatile boolean scanPathVisible = true;
	private volatile boolean updatingROIFromRegion = false;

	public PlottingController() {
	}

	public PlottingController(IPlottingSystem<?> plottingSystem) {
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
			// These colours all look reasonable, should test them on various maps and see
			// (They come from IRegion or ColorConstants)
			// Color darkCyan = new Color(null, 0, 128, 128);
			// Color orange = new Color(null, 255, 196, 0);
			// Color yellow = new Color(null, 255, 255, 0);
			// Color darkGreen = new Color(null, 0, 127, 0);
			// Color lightBlue = new Color(null, 127, 127, 255);
			// Color blue = new Color(null, 0, 0, 255);
			mappingRegionColour = new Color(null, 255, 196, 0); // orange
			scanPathColour = new Color(null, 160, 32, 240); // purple
		}
	}

	private void initPlottingSystem() {
		// Get and check the plotting system
		if (mapPlottingSystem != null && !mapPlottingSystem.isDisposed()) return;
		mapPlottingSystem = plottingService.getPlottingSystem(plottingSystemName);
		if (mapPlottingSystem == null) {
			throw new NullPointerException("Couldn't get map plotting system");
		}

		logger.debug("Initialized plotting system");
	}

	public void updatePlotRegionFrom(IMappingScanRegionShape scanRegion) {
		checkPlottingSystem();
		if (scanRegion != null) {
			PlatformUI.getWorkbench().getService(UISynchronize.class).asyncExec(() -> {
				IRegion plotRegion = mapPlottingSystem.getRegion(MAPPING_REGION_NAME);
				if (plotRegion == null) {
					plotRegion = createNewPlotRegion(scanRegion);
					mapPlottingSystem.addRegion(plotRegion);
				}
				updatingROIFromRegion = true;
				plotRegion.setROI(scanRegion.toROI());
				updatingROIFromRegion = false;
			});
		}
	}

	public IRegion createNewPlotRegion(final IMappingScanRegionShape scanRegion) {
		checkPlottingSystem();
		// Get the scan region from the plotting system if it exists
		IRegion plotRegion = mapPlottingSystem.getRegion(MAPPING_REGION_NAME);

		// Keep the visibility and fill settings
		boolean plotRegionVisible = plotRegion != null ? plotRegion.isVisible() : true;
		boolean plotRegionFilled = plotRegion != null ? plotRegion.isFill() : true;

		// Clean up the current plot
		mapPlottingSystem.removeRegion(plotRegion);
		removePath();

		try {
			// If you create a new region without adding it the plotting system allows the user to draw it!
			plotRegion = mapPlottingSystem.createRegion(MAPPING_REGION_NAME, RegionType.valueOf(scanRegion.whichPlottingRegionType()));
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

	/**
	 * This should be called whenever a change to the plotted scan path is needed.
	 */
	public void plotPath(PathInfo pathInfo) {
		lastPathInfo = pathInfo;
		replotLastPath();
	}

	private void replotLastPath() {
		checkPlottingSystem();

		//Remove the previous trace
		removePath();

		// Check if the scan region is currently plotted - if not, we don't want to plot the path either
		// (This fixes a synchronisation bug where the path is added while the scan region drawing event is still
		// active, cancelling the event and making it impossible to draw regions)
		IRegion plotRegion = mapPlottingSystem.getRegion(MAPPING_REGION_NAME);
		if (plotRegion != null && lastPathInfo != null) {

			// Make a new line trace and configure it
			ILineTrace pathTrace = mapPlottingSystem.createLineTrace(MAPPING_PATH_NAME);
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

	public void removePath() {
		checkPlottingSystem();
		ITrace pathTrace = mapPlottingSystem.getTrace(MAPPING_PATH_NAME);
		if (pathTrace != null) {
			mapPlottingSystem.removeTrace(pathTrace);
		}
	}

	void toggleRegionVisibility() {
		checkPlottingSystem();
		IRegion plotRegion = mapPlottingSystem.getRegion(MAPPING_REGION_NAME);
		if (plotRegion != null) {
			plotRegion.setVisible(!plotRegion.isVisible());
		}
	}

	void toggleRegionFill() {
		checkPlottingSystem();
		IRegion plotRegion = mapPlottingSystem.getRegion(MAPPING_REGION_NAME);
		if (plotRegion != null) {
			plotRegion.setFill(!plotRegion.isFill());
		}
	}

	void togglePathVisibility() {
		ITrace pathTrace = mapPlottingSystem.getTrace(MAPPING_PATH_NAME);
		if (pathTrace != null && pathTrace.isVisible()) {
			// Path exists and is visible, so make it invisible.
			// (It might be behind an image trace but that doesn't matter since we're hiding it anyway. The user can
			// just re-toggle visibility to force it to the front.)
			scanPathVisible = false;
			pathTrace.setVisible(false);
		} else {
			// Path is either missing or hidden - replot to make sure it's visible and on top.
			scanPathVisible = true;
			replotLastPath();
			// TODO showing path after a new map is plotted hides the region - maybe a bug in the plotting system?
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