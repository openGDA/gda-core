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

package uk.ac.gda.api.acquisition.parameters;

import java.util.Set;

import uk.ac.gda.api.acquisition.configuration.AcquisitionConfiguration;

/**
 * Defines an acquisition geometry and detectors
 *
 * @author Maurizio Nagni
 */
public interface AcquisitionParameters {

	/**
	 * The detector involved in the acquisition
	 * @return the detector configuration
	 */
	DetectorDocument getDetector();

	/**
	 * Defines, per device, the position where the acquisition starts. Note that this position
	 * <ul>
	 * <li>
	 *   is optional
	 * </li>
	 * <li>
	 *   when not empty, is strictly true only for the first acquisition
	 * </li>
	 * <li>
	 *   when not empty and {@link AcquisitionConfiguration#getMultipleScans()} is not null, acquisitions after the first may be overridden it
	 * </li>
	 * </ul>
	 * may be overridden by multiple acquisition logic
	 * @return a set of position documents, otherwise an empty set.
	 */
	Set<DevicePositionDocument> getPosition();

}
