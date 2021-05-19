/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package org.eclipse.scanning.device;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;

/**
 * A field written to the nexus file as a scalar value.
 */
public class ScalarField extends AbstractMetadataField {

	private boolean isDefaultValue;

	private Object value;

	public ScalarField() {
		// no-arg constructor for spring initialization
	}

	public ScalarField(String fieldName, Object value) {
		super(fieldName);
		this.value = value;
	}

	public ScalarField(String fieldName, Object value, String units) {
		this(fieldName, value, units, false);
	}

	public ScalarField(String fieldName, Object value, boolean isDefault) {
		this(fieldName, value, null, isDefault);
	}

	public ScalarField(String fieldName, Object value, String units, boolean isDefault) {
		this(fieldName, value);
		setDefaultValue(isDefault);
		setUnits(units);
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public boolean isDefaultValue() {
		return isDefaultValue;
	}

	public void setDefaultValue(boolean isDefaultValue) {
		this.isDefaultValue = isDefaultValue;
	}

	@Override
	protected DataNode createDataNode() {
		final Object value = getValue();
		return createDataNode(value);
	}

}
