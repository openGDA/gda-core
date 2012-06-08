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

/**
 * An interface to be used by Converters to provide a suitable converterName from a possible list of converterNames. e.g
 * to be used with IReloadable converters
 */
public interface ConverterNameProvider {

	/**
	 * @return String converter name
	 */
	public abstract String getConverterName();

	/**
	 * @param compareTo
	 * @return String converter name
	 */
	public abstract String getConverterName(double compareTo);

	/**
	 * @param cName
	 */
	public abstract void setConverterName(String cName);
}
