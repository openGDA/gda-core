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

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

/**
 * SplitQuantitiesConverterTest Class. It is assumed the that working directory points to the test folder above gda
 */
public class SplitQuantitiesConverterTest {
	private final static String testFileFolder = "testfiles/gda/util/converters/JEPQuantityConverterTest";

	@Test
	public void testUnits() {
		final GenQuantitiesConverter<Angle, Length> toSourceConverter = new GenQuantitiesConverter<>(
				new JEPQuantityConverter<Angle, Length>(testFileFolder + "/DegToAngstrom.xml"));
		final GenQuantitiesConverter<Angle, Length> calcMoveablesConverter = new GenQuantitiesConverter<>(
				new JEPQuantityConverter<Angle, Length>(testFileFolder + "/mmToDeg.xml"));
		final SplitQuantitiesConverter<Angle, Length> splitConverter = new SplitQuantitiesConverter<>(toSourceConverter, calcMoveablesConverter);

		final List<List<String>> acceptableUnits = splitConverter.getAcceptableUnits();
		final List<List<String>> acceptableMoveableUnits = splitConverter.getAcceptableMoveableUnits();

		final List<List<String>> acceptableCalcMoveablesUnits = calcMoveablesConverter.getAcceptableUnits();
		final List<List<String>> acceptableToSourceMoveableUnits = toSourceConverter.getAcceptableMoveableUnits();

		assertTrue(ConverterUtils.unitsAreEqual(acceptableCalcMoveablesUnits, acceptableUnits));
		assertTrue(ConverterUtils.unitsAreEqual(acceptableToSourceMoveableUnits, acceptableMoveableUnits));
	}

}
