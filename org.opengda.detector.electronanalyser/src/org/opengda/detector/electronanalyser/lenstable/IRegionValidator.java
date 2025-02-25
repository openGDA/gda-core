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

import java.util.List;

import org.opengda.detector.electronanalyser.api.SESRegion;

import gda.factory.Findable;
import uk.ac.diamond.daq.pes.api.EnergyRange;

public interface IRegionValidator extends Findable{

	/**
	 * check if the given region is valid or not for the given element_set and required excitation energy (in eV).
	 *
	 * @param elementSet
	 * @param region
	 * @param excitationEnergy
	 * @return
	 */
	public boolean isValidRegion(SESRegion region, String elementSet, double excitationEnergy);

	public List<EnergyRange> getEnergyRangesAsBindingEnergy(String elementSet, SESRegion region, double excitationEnergy);

	public List<EnergyRange> getEnergyRangesAsKineticEnergy(String elementSet, SESRegion region);

	public String getErrorMessage();
}
