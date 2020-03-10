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
import org.eclipse.swt.widgets.Composite;

/**
 * This event is published when a DrawableRegion has been successfully
 * registered in an {@link IPlottingSystem}
 *
 * @author Maurizio Nagni
 */
public class DrawableRegionRegisteredEvent extends RootCompositeEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4530246537809378671L;
	private final IPlottingSystem<Composite> plottingSystem;

	/**
	 * Creates an instance for this class
	 * 
	 * @param source  the Object which published this event
	 * @param plottingSystem   the plottingSystem where the region is registered 
	 * @param rootComposite the parent unique ID
	 */
	public DrawableRegionRegisteredEvent(Object source, IPlottingSystem<Composite> plottingSystem, Optional<UUID> rootComposite) {
		super(source, rootComposite);
		this.plottingSystem = plottingSystem;
	}

	/**
	 * @return the region plotting system
	 */
	public IPlottingSystem<Composite> getPlottingSystem() {
		return plottingSystem;
	}
}
