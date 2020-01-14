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

package uk.ac.gda.client.properties;

/**
 * Defines the motor properties required by the GUI to expose a motor
 *
 * @author Maurizio Nagni
 */
public interface MotorProperties {

	/**
	 * The Spring bean associated with a {@link gda.device.scannable.ScannableMotor} (already defined in XML or programmatically)
	 * @return the bean id
	 */
	String getController();

	/**
	 * The context specific motor label.
	 * @return the label used for the motor in the GUI
	 */
	String getName();
}
