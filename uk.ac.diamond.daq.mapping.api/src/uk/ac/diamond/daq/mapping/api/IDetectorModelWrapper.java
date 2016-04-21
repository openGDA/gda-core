/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

import org.eclipse.scanning.api.device.models.IDetectorModel;

/**
 * A wrapper for detector models to allow a list of items to be edited in the GUI and optionally included in scans
 */
public interface IDetectorModelWrapper {

	/**
	 * Get the name. This could be the detector name if a detector model is wrapped, or a scannable if a scan model is wrapped.
	 *
	 * @return detectorName
	 */
	public String getName();

	/**
	 * Whether the item should be included in scans or not
	 *
	 * @return <code>true</code> if this item should be included in scans
	 */
	public boolean isIncludeInScan();

	/**
	 * Get the model
	 *
	 * @return the model
	 */
	public IDetectorModel getModel();
}
