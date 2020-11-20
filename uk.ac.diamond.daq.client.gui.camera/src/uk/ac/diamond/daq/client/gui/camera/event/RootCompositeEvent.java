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

import uk.ac.gda.client.event.RootCompositeAware;

/**
 *  Represents an event published by an mutually dependent collection of Composites
 *
 * @author Maurizio Nagni
 */
public class RootCompositeEvent extends CameraEvent implements RootCompositeAware {

	private static final long serialVersionUID = -5094032542760128396L;
	private final Optional<UUID> rootComposite;

	public RootCompositeEvent(Object source, Optional<UUID> rootComposite) {
		super(source);
		this.rootComposite = rootComposite;
	}

	@Override
	public Optional<UUID> getRootComposite() {
		return rootComposite;
	}

//	public final boolean hasSameParent(Composite other) {
//		//Avoids disposed widget
//		if (other == null || other.isDisposed() || getRootComposite() == null) {
//			return false;
//		}
//		return getRootComposite().equals(ClientSWTElements.findParentUUID(other));
//	}
}
