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

package uk.ac.diamond.daq.mapping.api;

/**
 * A wrapper for model that can optionally be included in scan
 * @param <M>
 */
public interface IScanModelWrapper<M> {

	/**
	 * The name of the element, i.e. the detector name.
	 *
	 * @return name element name
	 */
	public String getName();

	/**
	 * Whether the item should be included in scans.
	 *
	 * @return <code>true</code> if this item should be included in scans,
	 *    <code>false</code> otherwise
	 */
	public boolean isIncludeInScan();

	/**
	 * Set whether this item should be include in the scan
	 */
	public void setIncludeInScan(boolean includeInScan);

	/**
	 * Get the model.
	 *
	 * @return the model
	 */
	public M getModel();
}
