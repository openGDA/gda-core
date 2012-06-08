/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.util.converters.util;

import gda.factory.Findable;

import java.util.ArrayList;

/**
 * Class to hold a list of converter names and a range(from - to) for which the converter name is valid. Selects the
 * appropriate converter name for the given input by comparing with the converter ranges available. e.g Can have a list
 * of lookuptable converters and their valid range.
 */

public class RangeConverterNameProvider implements ConverterNameProvider, Findable {

	private ArrayList<RangeandConverterNameHolder> converterList = new ArrayList<RangeandConverterNameHolder>();

	private String name;

	private String converterName = "";

	/**
	 * @param name
	 * @param converterName
	 */
	public RangeConverterNameProvider(String name, String converterName) {
		if (name == null || converterName == null) {
			throw new IllegalArgumentException("RenameableConverter. name or converterName cannot be null");
		}
		if (name.equals(converterName)) {
			throw new IllegalArgumentException("RenameableConverter. name and converterName cannot be the same");
		}
		this.name = name;
		this.converterName = converterName;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getConverterName() {
		// TODO Auto-generated method stub
		return converterName;
	}

	@Override
	public String getConverterName(double compareTo) {
		selectConverter(compareTo);
		return getConverterName();
	}

	/**
	 * @param rcnh
	 */
	public void addConverter(RangeandConverterNameHolder rcnh) {
		converterList.add(rcnh);
	}
	
	/**
	 * Sets the converters for this name provider.
	 * 
	 * @param converters the list of converters
	 */
	public void setConverters(ArrayList<RangeandConverterNameHolder> converters) {
		this.converterList = converters;
	}

	/**
	 * @return converterList
	 */
	public ArrayList<RangeandConverterNameHolder> getConverterList() {
		return converterList;
	}

	private void selectConverter(double source) {
		int count = converterList.size();
		for (int i = 0; i < count; i++) {
			RangeandConverterNameHolder rcnh = converterList.get(i);
			// check to see if the input value falls within converter's range
			if (source >= rcnh.getRangeStart() && source <= rcnh.getRangeStop()) {
				if (!rcnh.getConverterName().equals(converterName)) {
					setConverterName(rcnh.getConverterName());
				}
				break;
			}
		}
	}

	@Override
	public void setConverterName(String converterName2) {
		int count = converterList.size();
		for (int i = 0; i < count; i++) {
			if (converterList.get(i).getConverterName().equals(converterName2)) {
				this.converterName = converterName2;
				return;

			}
		}

	}
}
