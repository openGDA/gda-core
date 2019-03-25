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

import java.util.List;

import junit.framework.TestCase;

/**
 * SplitQuantitiesConverterTest Class. It is assumed the that working directory points to the test folder above gda
 */
public class SplitQuantitiesConverterTest extends TestCase {
	final static String testFileFolder = "testfiles/gda/util/converters/JEPQuantityConverterTest";

	/**
	 * @param arg0
	 */
	public SplitQuantitiesConverterTest(String arg0) {
		super(arg0);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 *
	 */
	public final void testUnits() {
		GenQuantitiesConverter toSourceConverter = new GenQuantitiesConverter(new JEPQuantityConverter(testFileFolder
				+ "/DegToAngstrom.xml"));
		GenQuantitiesConverter calcMoveablesConverter = new GenQuantitiesConverter(new JEPQuantityConverter(
				testFileFolder + "/mmToDeg.xml"));
		SplitQuantitiesConverter splitConverter = new SplitQuantitiesConverter(toSourceConverter,
				calcMoveablesConverter);

		List<List<String>> acceptableUnits = splitConverter.getAcceptableUnits();
		List<List<String>> acceptableMoveableUnits = splitConverter.getAcceptableMoveableUnits();

		List<List<String>> acceptableCalcMoveablesUnits = calcMoveablesConverter.getAcceptableUnits();
		List<List<String>> acceptableToSourceMoveableUnits = toSourceConverter.getAcceptableMoveableUnits();

		assertTrue(LookupTableConverterHolder.UnitsAreEqual(acceptableCalcMoveablesUnits, acceptableUnits));
		assertTrue(LookupTableConverterHolder.UnitsAreEqual(acceptableToSourceMoveableUnits, acceptableMoveableUnits));
	}

}
