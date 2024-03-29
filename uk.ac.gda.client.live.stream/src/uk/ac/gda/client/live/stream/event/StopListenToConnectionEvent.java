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

import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.client.event.RootCompositeAware;
import uk.ac.gda.client.live.stream.LiveStreamConnection;

/**
 * Message published either when a rootComposite {@link Composite} requires the children to stop listen at a
 * {@link LiveStreamConnection}.
 *
 * @author Maurizio Nagni
 */
public class StopListenToConnectionEvent extends LiveStreamEvent implements RootCompositeAware {

	private final Optional<UUID> rootComposite;

	/**
	 * @param source the liveStream instance to stop listen to
	 * @param rootComposite the component requiring the action
	 */
	public StopListenToConnectionEvent(Object source, UUID rootComposite) {
		super(source);
		this.rootComposite = Optional.ofNullable(rootComposite);
	}

	@Override
	public Optional<UUID> getRootComposite() {
		return rootComposite;
	}
}
