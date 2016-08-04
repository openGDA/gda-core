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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Scannable;
import gda.factory.Finder;
import gda.observable.IObserver;
import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;

/**
 * Class which observes the position of two motors (usually the X and Y stage motors) and marks their position on the
 * map plot. This is intended to show the current position of the beam on the sample, allowing users to visualise the
 * progress of mapping experiments and check that the stage is in the expected position for other scans.
 * <p>
 * It is designed to be instantiated as an OSGi service, and is then injected into the mapping views which call the
 * init() method. This delays initialisation until the map plot is available for this to connect to.
 * <p>
 * The motor names to be observed come from the {@link MappingStageInfo} object, which is typically configured in Spring
 * to hold the correct motor names initially. The MappingStageInfo also contains a beam size field which determines the
 * size of the circle used to mark the beam on the map.
 */
public class BeamPositionPlotter implements IObserver, PropertyChangeListener {

	public static final String POSITION_MARKER_NAME = "Beam Position";

	private static final Logger logger = LoggerFactory.getLogger(BeamPositionPlotter.class);

	private MappingStageInfo mappingStageInfo;
	private IPlottingService plottingService;

	private IPlottingSystem<Composite> mapPlottingSystem;
	private Color beamMarkerColour;

	private Scannable xAxisScannable;
	private Scannable yAxisScannable;

	private boolean initialised = false;
	private boolean showBeamPosition = true;
	private double lastXCoordinate;
	private double lastYCoordinate;

	/**
	 * For use by OSGi DS or in testing only!
	 */
	public void setPlottingService(IPlottingService plottingService) {
		this.plottingService = plottingService;
	}

	/**
	 * For use by OSGi DS or in testing only!
	 */
	public void setMappingStageInfo(MappingStageInfo mappingStageInfo) {
		this.mappingStageInfo = mappingStageInfo;
		mappingStageInfo.addPropertyChangeListener(this);
	}

	public BeamPositionPlotter() {
	}

	/**
	 * Try to initialise the beam position plotter. This should be called after the map plot view is open. It is safe
	 * to call this multiple times.
	 */
	public void init() {
		if (!initialised) {
			initialise();
		}
	}

	private void initialise() {
		try {
			if (mapPlottingSystem == null) {
				mapPlottingSystem = plottingService.getPlottingSystem("Map");
				if (mapPlottingSystem == null) {
					throw new NullPointerException("Couldn't get map plotting system");
				}
			}

			beamMarkerColour = new Color(null, 127, 127, 255); // light blue

			xAxisScannable = Finder.getInstance().find(mappingStageInfo.getActiveFastScanAxis());
			xAxisScannable.addIObserver(this);
			lastXCoordinate = (double) xAxisScannable.getPosition();

			yAxisScannable = Finder.getInstance().find(mappingStageInfo.getActiveSlowScanAxis());
			yAxisScannable.addIObserver(this);
			lastYCoordinate = (double) yAxisScannable.getPosition();

			initialised = true;
			replot();
		} catch (Exception e) {
			logger.warn("Error initialising beam position plotter", e);
			dispose();
		}
	}

	/**
	 * Remove all interactions with the rest of the application and close operating system resources.
	 */
	public void dispose() {
		initialised = false;
		if (mapPlottingSystem != null) {
			IRegion markerRegion = mapPlottingSystem.getRegion(POSITION_MARKER_NAME);
			if (markerRegion != null) {
				mapPlottingSystem.removeRegion(markerRegion);
			}
			mapPlottingSystem = null;
		}
		if (beamMarkerColour != null) {
			beamMarkerColour.dispose();
			beamMarkerColour = null;
		}
		if (xAxisScannable != null) {
			xAxisScannable.deleteIObserver(this);
			xAxisScannable = null;
		}
		if (yAxisScannable != null) {
			yAxisScannable.deleteIObserver(this);
			yAxisScannable = null;
		}
	}

	void toggleShowBeamPosition() {
		showBeamPosition = !showBeamPosition;
		replot();
	}

	/**
	 * Called in response to property changes on the {@link MappingStageInfo} object.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String propertyName = event.getPropertyName();
		if (propertyName.equals("beamSize")) {
			// If the beam size has changed, replot the position marker to show the new size
			replot();
		} else if (propertyName.equals("activeFastScanAxis") || propertyName.equals("activeSlowScanAxis")) {
			// If the motor names have changed, we need to stop observing the old motors and start observing the new
			// ones. The easiest way to achieve this is to call dispose() to remove all observers and listeners, and
			// call initialise() again to reconnect to the new motors.
			// TODO check if any maps are plotted using the old axis names and warn the user that the plot will be
			// inconsistent unless they clear the old maps.
			dispose();
			initialise();
		} else {
			logger.warn("Unknown property change event received");
		}
	}

	/**
	 * Called when a motor is reporting a new position
	 */
	@Override
	public void update(Object source, Object arg) {
		try {
			if (((Scannable) source).equals(xAxisScannable)) {
				lastXCoordinate = (double) xAxisScannable.getPosition();
			} else if (((Scannable) source).equals(yAxisScannable)) {
				lastYCoordinate = (double) yAxisScannable.getPosition();
			} else {
				logger.warn("Unexpected event received; stopping position plotting");
				dispose();
			}
		} catch (Exception e) {
			logger.warn("Error getting motor position; stopping position plotting", e);
			dispose();
		}
		PlatformUI.getWorkbench().getDisplay().asyncExec(this::replot);
	}

	/**
	 * Plot the current beam position by drawing a CircularROI at the current stage coordinates
	 */
	private void replot() {
		if (initialised) {
			// Get the region from the plot. If it already exists we just set the new parameters, otherwise we create
			// a new region and configure it first.
			IRegion markerRegion = mapPlottingSystem.getRegion(POSITION_MARKER_NAME);
			if (markerRegion == null) {
				try {
					markerRegion = mapPlottingSystem.createRegion(POSITION_MARKER_NAME, RegionType.CIRCLE);
					markerRegion.setRegionColor(beamMarkerColour);
					markerRegion.setFill(true);
					markerRegion.setAlpha(128);
					markerRegion.setMobile(false);
					markerRegion.setUserRegion(false);
					markerRegion.setLineWidth(5);
					mapPlottingSystem.addRegion(markerRegion);
				} catch (Exception e) {
					logger.warn("Error creating beam position region; stopping position plotting", e);
					initialised = false;
					return;
				}
			}
			markerRegion.setVisible(showBeamPosition);
			markerRegion.setROI(new CircularROI(mappingStageInfo.getBeamSize(), lastXCoordinate, lastYCoordinate));
		}
	}
}
