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

package gda.jscience.physics.quantities;

import static gda.jscience.physics.quantities.QuantityConstants.PLANCKS_CONSTANT;
import static gda.jscience.physics.quantities.QuantityConstants.SPEED_OF_LIGHT;
import static gda.jscience.physics.quantities.QuantityConstants.ZERO_ANGLE;
import static gda.jscience.physics.quantities.QuantityConstants.ZERO_ENERGY;
import static gda.jscience.physics.quantities.QuantityConstants.ZERO_LENGTH;
import static javax.measure.unit.SI.RADIAN;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A method only class to extend the functionality of the {@link Length} class. This class implements
 * {@link Length} to provide additional methods for conversions between Wavelength,
 * Bragg Angle, and Photon Energy for X-Ray application. While these additional methods should be accessed using class
 * name, their returns are of the {@link Length} type, not {@link Wavelength} type, in order to make them compatible
 * with the Unit System defined in JScience. Ideally these method should be implemented directly into the {@link Length}
 * class.
 */
public class Wavelength implements Length {
	private static final Logger logger = LoggerFactory.getLogger(Wavelength.class);

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 2110601334827305173L;

	/**
	 * Default constructor (allows for derivation).
	 */
	protected Wavelength() {
	}

	/**
	 * Returns the X-Ray wavelength of the specified photon energy.
	 *
	 * @param photonEnergy
	 *            PhotonEnergy the energy of the photon.
	 * @return Wavelength the X-Ray wavelength of the specified photon energy.
	 */
	public static Amount<Length> wavelengthOf(Amount<Energy> photonEnergy) {
		logger.debug("wavelengthOf(photonEnergy = {}", photonEnergy);
		if (photonEnergy != null && photonEnergy.isGreaterThan(ZERO_ENERGY)) {
			// Returns (h*c/E)
			return PLANCKS_CONSTANT.times(SPEED_OF_LIGHT).divide(photonEnergy).to(Length.UNIT);
		}
		return null;
	}

	/**
	 * Returns the X-Ray wavelength of the specified Bragg Angle for the crystal.
	 *
	 * @param braggAngle
	 *            Angle the Bragg Angle of the crystal.
	 * @param twoD
	 *            Length the 2d spacing of the crystal.
	 * @return Wavelength the X-Ray wavelength selected by the crystal.
	 */
	public static Amount<Length> wavelengthOf(Amount<Angle> braggAngle, Amount<Length> twoD) {
		logger.debug("wavelengthOf(braggAngle = {}, twoD = {})", braggAngle, twoD);
		if (braggAngle != null && twoD != null && braggAngle.isGreaterThan(ZERO_ANGLE)
				&& twoD.isGreaterThan(ZERO_LENGTH)) {
			final double braggAngleSine = Math.sin(braggAngle.doubleValue(RADIAN));
			return twoD.times(braggAngleSine);
		}
		return null;
	}
}
