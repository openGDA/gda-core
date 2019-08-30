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

package uk.ac.diamond.daq.devices.specs.phoibos.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class SpecsPhoibosSequenceValidation implements Serializable {

	private HashMap<SpecsPhoibosRegion, List<String>> validationErrors;


	/**
	 * Constructor
	 */
	public SpecsPhoibosSequenceValidation(){
		validationErrors = new HashMap<SpecsPhoibosRegion, List<String>>();
	}


	public void addValidationErrors(SpecsPhoibosRegion region, List<String> errors) {
		validationErrors.put(region, errors );
	}

	public HashMap<SpecsPhoibosRegion, List<String>> getValidationErrors() {
		return validationErrors;
	}

	public boolean isValid() {
		return validationErrors.isEmpty();
	}

	@Override
	/**
	 * Create a pretty, readable message from the validation HashMap
	 * @param validationErrors
	 * @return A string
	 */
	public String toString(){
		String prettyMessage = "";
		for (SpecsPhoibosRegion r : validationErrors.keySet()) {
			prettyMessage += r.getName() + " " + validationErrors.get(r) + System.lineSeparator();
		}
		return prettyMessage;
	}


}
