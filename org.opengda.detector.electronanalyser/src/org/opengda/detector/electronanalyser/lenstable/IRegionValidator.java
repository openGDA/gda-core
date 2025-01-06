/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package org.opengda.detector.electronanalyser.lenstable;

import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;

import gda.factory.Findable;

public interface IRegionValidator extends Findable{

	/**
	 * Check if a given region is valid or not for a given element set. The region's excitation energy is used to convert binding energy to kinetic energy
	 * before performing validation.
	 *
	 * @param region
	 * @param elementSet
	 * @return boolean true or false
	 */
	public boolean isValidRegion(Region region, String elementSet);

	/**
	 * check if the given region is valid or not for the given element_set and required excitation energy (in eV).
	 *
	 * @param elementSet
	 * @param region
	 * @param excitationEnergy
	 * @return
	 */
	public boolean isValidRegion(Region region, String elementSet, double excitationEnergy);

	public Double getMinKE(String elementSet, Region region);

	public Double getMaxKE(String elementSet, Region region);

	public String getErrorMessage();
}
