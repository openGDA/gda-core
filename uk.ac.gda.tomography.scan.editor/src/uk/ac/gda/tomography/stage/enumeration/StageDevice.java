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

package uk.ac.gda.tomography.stage.enumeration;

/**
 * Defines a set of components each of which may, or may not be part of a stage.
 *
 * @author Maurizio Nagni
 */
public enum StageDevice {

	/**
	 * A motor on axis X
	 */
	MOTOR_STAGE_X,
	/**
	 * A motor on axis Y
	 */
	MOTOR_STAGE_Y,
	/**
	 * A motor on axis Z
	 */
	MOTOR_STAGE_Z,
	/**
	 * A motor rotating axis Y
	 */
	MOTOR_STAGE_ROT_Y,
}
