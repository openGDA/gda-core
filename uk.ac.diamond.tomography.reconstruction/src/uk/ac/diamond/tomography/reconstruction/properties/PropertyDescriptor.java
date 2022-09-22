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

package uk.ac.diamond.tomography.reconstruction.properties;

import org.eclipse.jface.viewers.CellEditor;

public abstract class PropertyDescriptor {
	private String label;

	private String displayString;

	private String id;

	public PropertyDescriptor(String id, String label) {
		this(id, label, null);
	}

	public PropertyDescriptor(String id, String label, String displayString) {
		this.id = id;
		this.label = label;
		this.displayString = displayString;
	}

	public abstract void setValue(Object value);

	public String getLabel() {
		return label;
	}

	public String getValue() {
		return displayString;
	}

	public String getId() {
		return id;
	}

	public boolean canEdit() {
		return false;
	}

	public CellEditor createCellEditor() {
		return null;
	}

	public Object getSelectedValue() {
		return getValue();
	}

}