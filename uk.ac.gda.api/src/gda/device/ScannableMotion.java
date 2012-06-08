/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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
 * Interface for any object which operates in scans and represents a value or array of numbers. This could be a physical
 * value such as motor positions or sample environment attribute, or something abstract such as time.
 * <p>
 * This extends the Scannable interface with methods relating to an array of limits. These are user limits which can be
 * changed dynamically at runtime and are in addition to any hard limits or those held at another level of software
 */
public interface ScannableMotion extends Scannable {
	/**
	 * The attribute to ask for to get the effective limits of the first input of the Scannable. 
	 * Returns Double[2]. The array or individual entries could be null
	 * to indicate no limit.
	 */
	public static final String FIRSTINPUTLIMITS = "limits"; // same string as ScannableMotor.INNERLIMITS

	/**
	 * Returns null if okay, or a descriptive error if not. Code only called if limits are set.
	 * 
	 * @param externalPosition
	 *            position in external representation
	 * @return null if okay, or a descriptive error if not
	 */
	public String checkPositionWithinGdaLimits(Double[] externalPosition);

	/**
	 * Returns null if okay, or a descriptive error if not. Code only called if limits are set.
	 * 
	 * @param externalPosition
	 *            position in external representation
	 * @return null or description of any error
	 */
	public String checkPositionWithinGdaLimits(Object externalPosition);

	/**
	 * Set lower Scannable limits in external representation. Setting the input null will remove lower limits from the
	 * Scannable. Otherwise, any element can be null indicating that the corresponding field has no limit.
	 * 
	 * @param externalLowerLim limit in external representation
	 * @throws Exception
	 */
	public void setLowerGdaLimits(Double[] externalLowerLim) throws Exception;

	/**
	 * Set lower Scannable limit in external representation. Setting the input null will remove the lower limit from the
	 * Scannable.
	 * 
	 * @param externalLowerLim limit in external representation
	 * @throws Exception
	 */
	public void setLowerGdaLimits(Double externalLowerLim) throws Exception;

	/**
	 * Get lower Scannable limits in external representation. Will be null if no lower limits are set. Otherwise,
	 * any element can be null indicating that the corresponding field has no limit.
	 * 
	 * @return Double[] limit in external representation
	 */
	public Double[] getLowerGdaLimits();

	/**
	 * Set upper Scannable limits in external representation. Setting the input null will remove upper limits from the
	 * Scannable. Otherwise, any element can be null indicating that the corresponding field has no limit.
	 * @param externalUpperLim limit in external representation
	 * @throws Exception
	 */
	public void setUpperGdaLimits(Double[] externalUpperLim) throws Exception;

	/**
	 * Set upper Scannable limit in external representation. Setting the input null will remove the upper limit from the
	 * Scannable.
	 * 
	 * @param externalUpperLim limit in external representation
	 * @throws Exception
	 */
	public void setUpperGdaLimits(Double externalUpperLim) throws Exception;

	/**
	 * Get upper Scannable limits in external representation
	 * 
	 * @return Double[] limit in external representation
	 */
	public Double[] getUpperGdaLimits();

	/**
	 * @see gda.device.Scannable#checkPositionValid(Object) Like isPositionValid(), tests if the given object is meaningful
	 *      to this Scannable and so could be used by one of the move commands. May check limits and other things too.
	 *      However this method ill return null if okay, otherwise it returns a brief description of the problem.
	 * @param position
	 * @return null if position is valid, or returns a description if not.
	 * @throws DeviceException
	 */
	@Override
	public String checkPositionValid(Object position) throws DeviceException;

	/**
	 * Gets the scannables values of tolerance. If no values have been set then returns an array of zeros. This is for
	 * 'real world' motors which may not move to their desired location and do not throw an exception.
	 * 
	 * @return Double[]
	 * @throws DeviceException
	 */
	public Double[] getTolerances() throws DeviceException;

	/**
	 * @param tolerence
	 * @throws DeviceException
	 */
	public void setTolerance(Double tolerence) throws DeviceException;

	/**
	 * @param tolerence
	 * @throws DeviceException
	 */
	public void setTolerances(Double[] tolerence) throws DeviceException;

	/**
	 * This is the number of times the Scannable should attempt to move to the target position. Useful when the
	 * underlying hardware is problematic and often fails or ignores calls without throwing an exception. This should be
	 * used in conjunction with setting the tolerance attribute.
	 * 
	 * @return the numberTries
	 */
	public int getNumberTries();

	/**
	 * @param numberTries
	 *            the numberTries to set
	 */
	public void setNumberTries(int numberTries);

	public void a(Object position) throws DeviceException;

	public void ar(Object position) throws DeviceException;

	public void r(Object position) throws DeviceException;

	/**
	 * Set offset(s) in amounts of external quantities. May be null, or any element may be null, indicating no offset is
	 * to be applied/removed.
	 * 
	 * @param offsetArray
	 *            must match number of input fields.
	 */
	public void setOffset(Double... offsetArray);

	/**
	 * Set scaling factor(s) as Doubles. May be null, or any element may be null, indicating no scale is to be
	 * applied/removed.
	 * 
	 * @param scaleArray
	 *            must match number of input fields.
	 */

	public void setScalingFactor(Double... scaleArray);

	/**
	 * Returns offset(s) in amounts of external quantities. May be null, or any element may be null, indicating no
	 * offset is to be applied/removed.
	 */
	public Double[] getOffset();

	/**
	 * Returns scaling factor(s) as Doubles. May be null, or any element may be null, indicating no scale is to be
	 * applied/removed.
	 */
	public Double[] getScalingFactor();

}
