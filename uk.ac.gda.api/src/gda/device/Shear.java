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

package gda.device;

/**
 * Interface to the Couette shear cell
 */
public interface Shear extends Device {
	/**
	 * Get the thickness of the Couette cell
	 * 
	 * @return thickness expressed in mm
	 * @throws DeviceException
	 */
	public double getThickness() throws DeviceException;

	/**
	 * Get the radius of the Couette cell
	 * 
	 * @return radius expressed in mm
	 * @throws DeviceException
	 */
	public double getRadius() throws DeviceException;

	/**
	 * Get the shear rate of the Couette cell
	 * 
	 * @return gamma in per second
	 * @throws DeviceException
	 */
	public double getShearRate() throws DeviceException;

	/**
	 * Get the amplitude of oscillatory shear of the Couette cell
	 * 
	 * @return amplitude
	 * @throws DeviceException
	 */
	public double getAmplitude() throws DeviceException;

	/**
	 * Get the torque current of the Couette cell
	 * 
	 * @return current in amps
	 * @throws DeviceException
	 */
	public double getTorque() throws DeviceException;

	/**
	 * Generate a continous shear field within the Couette cell. It is possible to enter new shear parameters whilst the
	 * cell is performing continuous shear but not whilst it is performing oscillatory shear.
	 * 
	 * @param gamma
	 *            Shear rate in reciprocal seconds within range 0.05 to 1050.0
	 * @throws DeviceException
	 */
	public void continuousShear(double gamma) throws DeviceException;

	/**
	 * Generate an oscillatory shear field within the Couette cell.It is possible to enter new shear parameters whilst
	 * the cell is performing either oscillatory or continuous shear.
	 * 
	 * @param gamma
	 *            Shear rate in reciprocal seconds within range 0.05 to 1050.0
	 * @param amplitude
	 *            amplitude of oscillation in the range 1 to 1800.
	 * @throws DeviceException
	 */
	public void oscillatoryShear(double gamma, double amplitude) throws DeviceException;

	/**
	 * Stop shearing within the Couette cell. Also sets torque back to default value.
	 * 
	 * @throws DeviceException
	 */
	public void stopShear() throws DeviceException;

	/**
	 * Set the operating torque of the motor. The maximum torque is 2.54Nm, though the torque profile is a complicated
	 * function of the motor speed. See p134 of Aerotech manual. The torque is directly determined by the current drawn:
	 * thus it is this parameter which is programmed. It is not possible to alter the torque whilst shearing. This
	 * parameter resets to the defaultvalue when the stop method is called.
	 * 
	 * @param current
	 *            is the desired motor current, in the range between 0.5 & 6.0. If no value is specified then the motor
	 *            defaults to an operating current of 1A, corresponding to a torque of 0.4Nm
	 * @throws DeviceException
	 */
	public void setTorque(double current) throws DeviceException;

}
