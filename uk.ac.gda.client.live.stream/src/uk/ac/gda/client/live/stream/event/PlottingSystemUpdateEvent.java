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

import java.util.UUID;

/**
 *  Published when a component requires any child plotting system to update its data
 *
 * @author Maurizio Nagni
 */
public class PlottingSystemUpdateEvent extends LiveStreamEvent implements RootEvent {
	private static final long serialVersionUID = -933748166416333930L;

	private final UUID rootComposite;

    public PlottingSystemUpdateEvent(Object source, UUID rootComposite) {
		super(source);
		this.rootComposite = rootComposite;
	}

    @Override
	public UUID getRootComposite() {
		return rootComposite;
	}
}
