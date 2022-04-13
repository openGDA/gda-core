/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.device;

import gda.factory.Findable;

/**
 * an interface for a device whose position can be tweaked forward or reverse by the increment amount
 */
public interface ITweakable extends Findable{

	/**
	 * increase value of this object by a fixed amount given in {@link #getIncrement()}
	 * @throws MotorException
	 */
	void forward() throws MotorException;
	/**
	 * decrease value of this object by a fixed amount given in {@link #getIncrement()}
	 * @throws MotorException
	 */
	void reverse() throws MotorException;

	/**
	 * set the increment amount for this object
	 */
	void setIncrement(double value);
	/**
	 * returns the increment amount of this object
	 * @return the increment amount
	 */
	double getIncrement();

}
