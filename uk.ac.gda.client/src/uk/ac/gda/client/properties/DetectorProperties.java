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

package uk.ac.gda.client.properties;

import java.util.Optional;

/**
 * Defines the detector properties required by the acquisition configuration GUI
 *
 * @author Maurizio Nagni
 */
public interface DetectorProperties {

	/**
	 * Identifies a detector in its context.
	 *
	 * @return the detector index number
	 */
	int getIndex();

	/**
	 * The context specific detector label.
	 *
	 * @return the label used for the detector in the GUI
	 */
	String getName();

	/**
	 * The detector property identifier. As the {@link Optional} return type indicate, this is still not a required
	 * property
	 *
	 * @return Returns the detector property identifier.
	 */
	Optional<String> getId();

	/**
	 * The Spring bean id, associated with a <code>org.eclipse.scanning.api.device.models.IDetectorModel</code>, as
	 * defined on the server.
	 *
	 * @return the bean id
	 */
	String getDetectorBean();
}
