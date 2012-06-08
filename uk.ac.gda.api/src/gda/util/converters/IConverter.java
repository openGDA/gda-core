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

package gda.util.converters;

/**
 * Converts forward and back between a source and a target
 * 
 * @param <Source>
 *            Can be of any type.
 * @param <Target>
 *            Can be of any type.
 */
public interface IConverter<Source, Target> {

	/**
	 * Calculates a value for Source given the value of the target
	 * 
	 * @param target
	 * @return The value calculated from the value of the target *
	 * @throws Exception
	 */
	public Source toSource(Target target) throws Exception;

	/**
	 * Calculates a value for Target given the value of the source
	 * 
	 * @param source
	 * @return The value calculated from the value of the source
	 * @throws Exception
	 */
	public Target toTarget(Source source) throws Exception;

	/**
	 * Returns true is the conversion reverses the sense. So that to get the max of the source you convert the min of
	 * the target and vice versa
	 * 
	 * @return true is the conversion reverses the sense.
	 */
	public boolean sourceMinIsTargetMax();

}
