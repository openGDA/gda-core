/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos.api;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class SpecsPhoibosSequenceValidation implements Serializable {

	private final List<SpecsPhoibosRegionValidation> regionValidations;

	/**
	 * Constructor
	 */
	public SpecsPhoibosSequenceValidation(List<SpecsPhoibosRegionValidation> regionValidations){
		this.regionValidations = regionValidations;
	}

	public List<SpecsPhoibosRegionValidation> getRegionValidations() {
		return regionValidations;
	}

	public List<SpecsPhoibosRegion> getRegionsWithErrors() {
		return regionValidations.stream()
				.filter(item -> !item.isValid())
				.map(SpecsPhoibosRegionValidation::getInvalidRegion)
				.collect(Collectors.toList());
	}

	public List<String> getErrorMessagesforRegion(SpecsPhoibosRegion region) {
		return regionValidations.stream()
				.filter(item -> item.hasRegion(region))
				.findFirst()
				.map(result -> result.getErrorMessages())
				.orElse(null);
	}

	public boolean isValid() {
		return getRegionsWithErrors().isEmpty();
	}
}