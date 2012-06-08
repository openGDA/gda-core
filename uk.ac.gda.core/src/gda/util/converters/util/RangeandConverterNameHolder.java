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

/**
 * RangeandConverterNameHolder Class
 */
public class RangeandConverterNameHolder implements Findable {
	private double rangeStart;

	private double rangeStop;

	private String converterName;

	/**
	 * 
	 */
	public String name;

	/**
	 * @param name
	 * @param converterName
	 * @param rangeStart
	 * @param rangeEnd
	 */
	public RangeandConverterNameHolder(String name, String converterName, double rangeStart, double rangeEnd) {
		setName(name);
		this.converterName = converterName;
		this.rangeStart = rangeStart;
		this.rangeStop = rangeEnd;
	}

	/**
	 * @return rangeStart
	 */
	public double getRangeStart() {
		return rangeStart;
	}

	/**
	 * @param lowLimit
	 */
	public void setRangeStart(double lowLimit) {
		this.rangeStart = lowLimit;
	}

	/**
	 * @return rangeStop
	 */
	public double getRangeStop() {
		return rangeStop;
	}

	/**
	 * @param highLimit
	 */
	public void setRangeStop(double highLimit) {
		this.rangeStop = highLimit;
	}

	/**
	 * @return converterName
	 */
	public String getConverterName() {
		return converterName;
	}

	/**
	 * @param converterName
	 */
	public void setConverterName(String converterName) {
		this.converterName = converterName;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

}
