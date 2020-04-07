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
import java.util.ArrayList;
import java.util.List;

public class SpecsPhoibosRegionValidation implements Serializable {

	private final SpecsPhoibosRegion region;
	private List<String> errors = new ArrayList<>();

	public SpecsPhoibosRegionValidation(SpecsPhoibosRegion region) {
		this.region = region;
	}

	public SpecsPhoibosRegion getInvalidRegion() {
		return region;
	}

	public List<String> getErrorMessages() {
		return errors;
	}

	public boolean hasRegion(SpecsPhoibosRegion region) {
		return this.region.equals(region);
	}

	public void addErrors(List<String> errors) {
		this.errors.addAll(errors);
	}

	public boolean isValid() {
		return errors.isEmpty();
	}

}
