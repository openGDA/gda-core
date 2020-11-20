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

import uk.ac.diamond.daq.client.gui.camera.liveview.DrawableRegion;

/**
 * This event is published when a DrawableRegion has been successfully
 * registered in an {@link IPlottingSystem}
 *
 * @author Maurizio Nagni
 */
public class DrawableRegionRegisteredEvent extends RootCompositeEvent {

	private static final long serialVersionUID = 4530246537809378671L;
	private final transient DrawableRegion drawableRegion;

	/**
	 * Creates an instance for this class
	 *
	 * @param source  the Object which published this event
	 * @param rootComposite the parent unique ID
	 * @param drawableRegion the {@link DrawableRegion} that has been registered
	 */
	public DrawableRegionRegisteredEvent(Object source, Optional<UUID> rootComposite, DrawableRegion drawableRegion) {
		super(source, rootComposite);
		this.drawableRegion = drawableRegion;
	}

	public DrawableRegion getDrawableRegion() {
		return drawableRegion;
	}
}
