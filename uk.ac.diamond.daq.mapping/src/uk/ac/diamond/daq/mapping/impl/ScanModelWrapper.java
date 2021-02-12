/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.impl;

import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;

/**
 * Abstract superclass of wrappers for elements in the mapping UI. Wraps the model describing
 * the item (e.g. a detector) and adds a name to be displayed to the user and a boolean to
 * indicate whether the element should be included in the scan.
 *
 * @param <T> the wrapped model type
 */
public abstract class ScanModelWrapper<T> implements IScanModelWrapper<T> {

	private String name;
	private boolean includeInScan;
	private T model;

	public ScanModelWrapper() {
		// no-arg
	}

	public ScanModelWrapper(String name, T model, boolean includeInScan) {
		this.name = name;
		this.model = model;
		this.includeInScan = includeInScan;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean isIncludeInScan() {
		return includeInScan;
	}

	@Override
	public void setIncludeInScan(boolean includeInScan) {
		this.includeInScan = includeInScan;
	}

	@Override
	public T getModel() {
		return model;
	}

	@Override
	public void setModel(T model) {
		this.model = model;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (includeInScan ? 1231 : 1237);
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScanModelWrapper<?> other = (ScanModelWrapper<?>) obj;
		if (includeInScan != other.includeInScan)
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ScanModelWrapper [name=" + name + ", includeInScan=" + includeInScan + ", model=" + model + "]";
	}
}
