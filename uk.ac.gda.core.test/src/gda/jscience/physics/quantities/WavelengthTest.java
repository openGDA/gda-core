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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jscience.physics.quantities.Angle;
import org.jscience.physics.quantities.Energy;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.NonSI;
import org.jscience.physics.units.SI;
import org.junit.Test;

/**
 */
public class WavelengthTest
{
   /**
    * Test method for {@link gda.jscience.physics.quantities.Wavelength#wavelengthOf(org.jscience.physics.quantities.Energy)}.
    */
   @Test
   public void testWavelengthOfEnergy()
   {
      Energy photonEnergy;
      Energy negativeEnergy;
      Length wavelength;
      Length result;

      photonEnergy = Quantity.valueOf(2.34, NonSI.ELECTRON_VOLT);
      wavelength = Quantity.valueOf(529.847, SI.NANO(SI.METER));

      result = Wavelength.wavelengthOf(photonEnergy);
      assertEquals(wavelength.doubleValue(), result.doubleValue(), 0.0000000000001);

      result = Wavelength.wavelengthOf(null);
      assertTrue("Wavelength should be null", null == result);

      negativeEnergy = Quantity.valueOf(-1.0, NonSI.ELECTRON_VOLT);
      result = Wavelength.wavelengthOf(negativeEnergy);
      assertTrue("Wavelength should be null", null == result);

      result = Wavelength.wavelengthOf(Energy.ZERO);
      assertTrue("Wavelength should be null", null == result);
   }

   /**
    * Test method for {@link gda.jscience.physics.quantities.Wavelength#wavelengthOf(org.jscience.physics.quantities.Angle, org.jscience.physics.quantities.Length)}.
    */
   @Test
   public void testWavelengthOfAngleLength()
   {
      Length twoD;
      Angle braggAngle;
      Length result;
      Length wavelength;
      Length negative;
      Angle negativeAngle;

      twoD = Quantity.valueOf(6.271, NonSI.ANGSTROM);
      braggAngle = Quantity.valueOf(12787.5, SI.MILLI(NonSI.DEGREE_ANGLE));
      wavelength = Quantity.valueOf(1.388, NonSI.ANGSTROM);
      
      result = Wavelength.wavelengthOf(braggAngle, twoD);
      assertEquals(wavelength.doubleValue(), result.doubleValue(), 0.0000000000001);

      negative = Quantity.valueOf(-1.0, NonSI.ANGSTROM);
      result = Wavelength.wavelengthOf(braggAngle, negative);
      assertTrue("Wavelength should be null", null == result);

      negativeAngle = Quantity.valueOf(-1.0, SI.RADIAN);
      result = Wavelength.wavelengthOf(negativeAngle, twoD);
      assertTrue("Wavelength should be null", null == result);

      result = Wavelength.wavelengthOf(braggAngle, null);
      assertTrue("Wavelength should be null", null == result);

      result = Wavelength.wavelengthOf(null, twoD);
      assertTrue("Wavelength should be null", null == result);

      result = Wavelength.wavelengthOf(Angle.ZERO, twoD);
      assertTrue("Wavelength should be null", null == result);

      result = Wavelength.wavelengthOf(braggAngle, Length.ZERO);
      assertTrue("Wavelength should be null", null == result);
  }
}
