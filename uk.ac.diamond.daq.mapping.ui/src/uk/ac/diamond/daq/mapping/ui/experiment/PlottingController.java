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

import static uk.ac.gda.preferences.PreferenceConstants.GDA_MAPPING_MAPPING_REGION_COLOUR;
import static uk.ac.gda.preferences.PreferenceConstants.GDA_MAPPING_SCAN_PATH_COLOUR;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
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
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.api.document.scanpath.MappingPathInfo;
import uk.ac.diamond.daq.mapping.ui.Activator;

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
	private MappingPathInfo lastPathInfo;

	private IPropertyChangeListener propertyChangeListener = null;

	/**
	 * Controls whether the scan path is shown in the plot<br>
	 * Default value must match default value of <code>RegistryToggleState</code>
	 * for command id <code>uk.ac.diamond.daq.mapping.ui.command.showHideMappingPath</code>
	 * in <code>plugin.xml</code>
	 */
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
			initPlottingColours();
		}
	}

	private void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(GDA_MAPPING_MAPPING_REGION_COLOUR) || event.getProperty().equals(GDA_MAPPING_SCAN_PATH_COLOUR)) {
			initPlottingColours();

			final IRegion oldRegion = mapPlottingSystem.getRegion(MAPPING_REGION_NAME);
			if (oldRegion == null) return;

			final IROI roi = oldRegion.getROI();
			final RegionType regionType = oldRegion.getRegionType();
			oldRegion.removeROIListener(roiListener);

			final IRegion newRegion = createNewPlotRegion(regionType);
			if (newRegion == null) return;
			newRegion.setROI(roi);
			newRegion.addROIListener(roiListener);
			mapPlottingSystem.addRegion(newRegion);

			replotLastPath();
		}
	}

	private void initPlottingColours() {
		if (propertyChangeListener == null) {
			propertyChangeListener = this::propertyChange;
			Activator.getDefault().getPreferenceStore().addPropertyChangeListener(propertyChangeListener);
		}

		mappingRegionColour = getColour(GDA_MAPPING_MAPPING_REGION_COLOUR);
		scanPathColour = getColour(GDA_MAPPING_SCAN_PATH_COLOUR);
	}

	private Color getColour(String colourPropertyName) {
		return new Color(null, PreferenceConverter.getColor(Activator.getDefault().getPreferenceStore(), colourPropertyName));
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
		final RegionType regionType = RegionType.valueOf(scanRegion.whichPlottingRegionType());
		final IRegion plotRegion = createNewPlotRegion(regionType);

		if (plotRegion != null) {
			roiListener = new IROIListener.Stub() {
				@Override
				public void roiChanged(ROIEvent evt) {
					if (!updatingROIFromRegion) {
						scanRegion.updateFromROI(evt.getROI());
					}
				}
			};
			plotRegion.addROIListener(roiListener);
		}

		return plotRegion;
	}

	private IRegion createNewPlotRegion(RegionType regionType) {
		checkPlottingSystem();
		// Get the scan region from the plotting system if it exists
		IRegion plotRegion = mapPlottingSystem.getRegion(MAPPING_REGION_NAME);

		// Keep the visibility and fill settings
		boolean plotRegionVisible = plotRegion == null || plotRegion.isVisible();
		boolean plotRegionFilled = plotRegion == null || plotRegion.isFill();

		// Clean up the current plot
		mapPlottingSystem.removeRegion(plotRegion);
		removePath();

		try {
			// If you create a new region without adding it the plotting system allows the user to draw it!
			plotRegion = mapPlottingSystem.createRegion(MAPPING_REGION_NAME, regionType);
			plotRegion.setFill(true);
			plotRegion.setAlpha(80);
			plotRegion.setRegionColor(mappingRegionColour);
			plotRegion.setLineWidth(5);
			plotRegion.setVisible(plotRegionVisible);
			plotRegion.setFill(plotRegionFilled);

			return plotRegion;
		} catch (Exception e) {
			logger.error("Failed to create new mapping region", e);
			return null;
		}
	}

	private IROIListener roiListener = null;

	/**
	 * This should be called whenever a change to the plotted scan path is needed.
	 */
	public void plotPath(MappingPathInfo pathInfo) {
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

	boolean toggleRegionVisibility() {
		checkPlottingSystem();
		IRegion plotRegion = mapPlottingSystem.getRegion(MAPPING_REGION_NAME);
		if (plotRegion == null) {
			return false;
		} else {
			final boolean regionVisible = !plotRegion.isVisible();
			plotRegion.setVisible(regionVisible);
			return regionVisible;
		}
	}

	boolean toggleRegionFill() {
		checkPlottingSystem();
		IRegion plotRegion = mapPlottingSystem.getRegion(MAPPING_REGION_NAME);
		if (plotRegion == null) {
			return false;
		} else {
			final boolean fillVisible = !plotRegion.isFill();
			plotRegion.setFill(fillVisible);
			return fillVisible;
		}
	}

	boolean togglePathVisibility() {
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
		return scanPathVisible;
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

		if (propertyChangeListener != null) {
			Activator.getDefault().getPreferenceStore().removePropertyChangeListener(propertyChangeListener);
			propertyChangeListener = null;
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