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

package uk.ac.gda.client.live.stream.event;

import java.util.Optional;
import java.util.UUID;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.client.event.RootCompositeAware;

/**
 *  Published by a component to communicate it created a {@link IPlottingSystem}.
 *
 * @author Maurizio Nagni
 */
public class PlottingSystemUpdateEvent extends LiveStreamEvent implements RootCompositeAware {
	private static final long serialVersionUID = -933748166416333930L;

	private final Optional<UUID> rootComposite;
	private final IPlottingSystem<Composite> plottingSystem;

    /**
     * @param source the object which published the event
     * @param rootComposite the id of the root parent composite, eventually {@code null} if the composite has no root parent
     * @param plottingSystem the new plotting system
     */
    public PlottingSystemUpdateEvent(Object source, UUID rootComposite, IPlottingSystem<Composite> plottingSystem) {
		super(source);
		this.rootComposite = Optional.ofNullable(rootComposite);
		this.plottingSystem = plottingSystem;
	}

	@Override
	public Optional<UUID> getRootComposite() {
		return rootComposite;
	}

	public IPlottingSystem<Composite> getPlottingSystem() {
		return plottingSystem;
	}
}
