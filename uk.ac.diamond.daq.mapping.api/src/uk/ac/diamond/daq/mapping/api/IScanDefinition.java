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

import java.util.List;

import org.eclipse.scanning.api.points.models.IAxialModel;

public interface IScanDefinition {

	/**
	 * Gets the mapping scan region to be used in the mapping scan. Includes the shape, shape parameters, pattern and
	 * pattern parameters.
	 *
	 * @return mappingScanRegion
	 */
	IMappingScanRegion getMappingScanRegion();

	/**
	 * Sets the mapping scan region to be used in the mapping scan. Includes the shape, shape parameters, pattern and
	 * pattern parameters.
	 *
	 * @param mappingScanRegion
	 */
	void setMappingScanRegion(IMappingScanRegion mappingScanRegion);

	/**
	 * Gets the list of outer scannables including their parameters for use outside the mapping scan. e.g change
	 * temperature and at each temperature take a map. This list allows complex scan to be built containing many outer
	 * loops. Should never be <code>null</code>.
	 *
	 * @return outerScannables the outer scannables
	 */
	List<IScanModelWrapper<IAxialModel>> getOuterScannables();

	/**
	 * Sets the list of outer scannables including their parameters for use outside the mapping scan. e.g change
	 * temperature and at each temperature take a map. This list allows complex scan to be built containing many outer
	 * loops.<br>
	 * This function should not be used to configure outer scannables in Spring, as it will be overwritten at runtime.
	 * Instead, use {@link IScanDefinition#setPermittedOuterScannables(List)} if you want to restrict the scannables that
	 * the user can choose.
	 *
	 * @param outerScannables
	 */
	void setOuterScannables(List<IScanModelWrapper<IAxialModel>> outerScannables);

	/**
	 * Gets the list of beamline scannables that the user may choose as outer scannables
	 * <p>
	 * If the list is empty, the user is free to choose any scannable available on the beamline.
	 *
	 * @return the permitted outer scannables for this beamline
	 */
	List<String> getPermittedOuterScannables();

	/**
	 * Sets the permitted outer scannables for this beamline. See {@link #getPermittedOuterScannables()}
	 *
	 * @param scannables
	 */
	void setPermittedOuterScannables(List<String> scannables);

	/**
	 * Gets the list of outer scannables that will be shown by default (on first use or when the client is reset)
	 *
	 * @return the default outer scannables
	 */
	List<String> getDefaultOuterScannables();

	/**
	 * Sets the list of default outer scannables (see {@link #getDefaultOuterScannables()}
	 *
	 * @param scannables
	 *            the default outer scannables
	 */
	void setDefaultOuterScannables(List<String> scannables);
}
