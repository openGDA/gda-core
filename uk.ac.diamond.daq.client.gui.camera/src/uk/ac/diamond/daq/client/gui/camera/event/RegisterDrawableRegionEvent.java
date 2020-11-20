/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.camera.event;

import java.util.Optional;
import java.util.UUID;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.swt.graphics.Color;

import uk.ac.diamond.daq.client.gui.camera.liveview.DrawableRegion;
import uk.ac.gda.ui.tool.ClientMessages;

/**
 * Requests to register a new {@link DrawableRegion} in an
 * {@link IPlottingSystem}. Usually the container of the composite publishing
 * this event has to contain an instance of the plotting system.
 *
 * @author Maurizio Nagni
 */
public class RegisterDrawableRegionEvent extends RootCompositeEvent {

	private static final long serialVersionUID = 4530246537809378671L;
	/**
	 * The UUID to use as name for the region to be created
	 */
	private final UUID regionID;
	/**
	 * The ROI color
	 */
	private final Color color;
	/**
	 * The label to use in the GUI to identify to be created
	 */
	private final ClientMessages name;

	/**
	 * Creates an instance for this class
	 *
	 * @param source  the Object which published this event
	 * @param color   the colour to use when drawing the region
	 * @param regionName how the region is identified both in the GUI and the plotting system
	 * @param rootComposite the parent unique ID. Used to filter cross components messages
	 * @param regionID the identificator to use for the region to be created
	 */
	public RegisterDrawableRegionEvent(Object source, Color color, ClientMessages regionName, Optional<UUID> rootComposite, UUID regionID) {
		super(source, rootComposite);
		this.color = color;
		this.name = regionName;
		this.regionID = regionID;
	}

	/**
	 * Returns the color to use when drawing the region
	 * @return a color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Returns the string identifying the region both in the GUI and the plotting system
	 * @return a string
	 */
	public ClientMessages getName() {
		return name;
	}

	/**
	 * @return the unique id to be used when creating the region
	 */
	public UUID getRegionID() {
		return regionID;
	}
}
