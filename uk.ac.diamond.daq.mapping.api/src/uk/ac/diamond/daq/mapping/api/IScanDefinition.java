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

public interface IScanDefinition {

	/**
	 * Gets the mapping scan region to be used in the mapping scan. Includes the shape, shape parameters, pattern and pattern parameters.
	 *
	 * @return mappingScanRegion
	 */
	public IMappingScanRegion getMappingScanRegion();

	/**
	 * Sets the mapping scan region to be used in the mapping scan. Includes the shape, shape parameters, pattern and pattern parameters.
	 *
	 * @param mappingScanRegion
	 */
	public void setMappingScanRegion(IMappingScanRegion mappingScanRegion);

	/**
	 * Gets the list of outer scannables including their parameters for use outside the mapping scan. e.g change temperature and at each temperature take a map.
	 * This list allows complex scan to be built containing many outer loops. Can be <code>null</code> indicating no outer scannables
	 *
	 * @return outerScannables
	 */
	public List<IScanPathModelWrapper> getOuterScannables();

	/**
	 * Sets the list of outer scannables including their parameters for use outside the mapping scan. e.g change temperature and at each temperature take a map.
	 * This list allows complex scan to be built containing many outer loops. Can be <code>null</code> indicating no outer scannables
	 *
	 * @param outerScannables
	 */
	public void setOuterScannables(List<IScanPathModelWrapper> outerScannables);

}
