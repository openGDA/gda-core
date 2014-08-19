/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.client.plotting.model;

import org.eclipse.core.databinding.observable.list.IObservableList;

import uk.ac.gda.beans.ObservableModel;

public abstract class Node extends ObservableModel {

	public static final String DATA_CHANGED_PROP_NAME = "changedData";
	public static final String DATA_ADDED_PROP_NAME = "addedData";
	public static final String SCAN_ADDED_PROP_NAME = "addedScan";

	protected final Node parent;

	public Node(Node parent) {
		this.parent = parent;
	}

	public abstract IObservableList getChildren();

	public abstract String getIdentifier();

	public Node getParent() {
		return parent;
	}

	public String getLabel() {
		return toString();
	}

	public abstract void removeChild(Node dataNode);

	public void disposeResources() {
		// Nothing to dispose
	}
}
