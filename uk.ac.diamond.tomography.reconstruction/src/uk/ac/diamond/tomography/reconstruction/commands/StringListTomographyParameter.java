/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.diamond.tomography.reconstruction.commands;

import java.util.List;

//XXX Could almost certainly be better
public class StringListTomographyParameter implements ITomographyParameter {

	private String name;
	private int valueLocation;
	List<String> paramValues;
	
	public StringListTomographyParameter(String parameterName) {
		name = parameterName;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getValue() {
		return paramValues.get(valueLocation);
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValueLocation(int valueLocation) {
		this.valueLocation = valueLocation;
	}


	public void setParamValues(List<String> paramValues) {
		this.paramValues = paramValues;
	}



}
