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

import java.util.UUID;

import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.ui.tool.ClientSWTElements;

/**
 *  Represents an event published by an mutually dependent collection of Composites
 *
 * @author Maurizio Nagni
 */
public class RootCompositeEvent extends CameraEvent implements RootCompositeAware {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5094032542760128396L;
	private final UUID rootComposite;

	public RootCompositeEvent(Object source, UUID rootComposite) {
		super(source);
		this.rootComposite = rootComposite;
	}

	@Override
	public UUID getRootComposite() {
		return rootComposite;
	}
	
	public final boolean hasSameParent(Composite other) {
		if (other == null) {
			return false;
		}	
		return ClientSWTElements.findParentUUID(other).equals(getRootComposite());
	}
}
