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

import junit.framework.TestCase;

import org.jscience.physics.quantities.Quantity;

// It is assumed the that working directory points to the test folder above gda
/**
 * JEPQuantityConverterTest Class
 */
public class JEPQuantityConverterTest extends TestCase {
	private String testFileFolder;

	/**
	 * @param arg0
	 */
	public JEPQuantityConverterTest(String arg0) {
		super(arg0);
	}

	@Override
	protected void setUp() throws Exception {
		testFileFolder = "test/gda/util/converters/JEPConverterParametersJUnitTestFiles";
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * 
	 */
	public void testToSource() {
		JEPQuantityConverter converter = new JEPQuantityConverter(JEPQuantityConverterParameters.jUnitTestFileName);
		Quantity targetBeforeConversion = Quantity.valueOf(1.0, converter.getAcceptableTargetUnits().get(0));
		Quantity source = converter.toSource(targetBeforeConversion);
		Quantity targetAfterConversion = converter.toTarget(source);
		assertEquals(targetAfterConversion, targetBeforeConversion);
	}

	/**
	 * 
	 */
	public final void testReal() {
		JEPQuantityConverter converter = new JEPQuantityConverter(testFileFolder + "/Simple.xml");
		Quantity targetBeforeConversion = Quantity.valueOf(1.0, converter.getAcceptableTargetUnits().get(0));
		Quantity source = converter.toSource(targetBeforeConversion);
		Quantity targetAfterConversion = converter.toTarget(source);
		assertEquals(targetAfterConversion, targetBeforeConversion);
	}

	/**
	 * 
	 */
	public final void testSourceMinIsTargetMax() {
		assertEquals(false, new JEPQuantityConverter(testFileFolder + "/Simple.xml").sourceMinIsTargetMax());
		assertEquals(true, new JEPQuantityConverter(testFileFolder + "/SourceMinIsTargetMax.xml")
				.sourceMinIsTargetMax());
		assertEquals(false, new JEPQuantityConverter(testFileFolder + "/SourceMinIsNOTTargetMax.xml")
				.sourceMinIsTargetMax());
	}

	/**
	 * 
	 */
	public final void testComplex() {
		JEPQuantityConverter converter = new JEPQuantityConverter(testFileFolder + "/Complex.xml");
		Quantity targetBeforeConversion = Quantity.valueOf(1.0, converter.getAcceptableTargetUnits().get(0));
		Quantity source = converter.toSource(targetBeforeConversion);
		Quantity targetAfterConversion = converter.toTarget(source);
		assertEquals(targetBeforeConversion, targetAfterConversion);
	}

	/**
	 * 
	 */
	public final void testCoupled() {
		JEPQuantityConverter converterTarget = new JEPQuantityConverter(testFileFolder + "/DegToAngstrom.xml");
		JEPQuantityConverter converterSource = new JEPQuantityConverter(testFileFolder + "/mmToDeg.xml");
		CoupledQuantityConverter converter = new CoupledQuantityConverter(converterSource, converterTarget);
		Quantity targetBeforeConversion = Quantity.valueOf(1.0, converterTarget.getAcceptableTargetUnits().get(0));
		try {
			Quantity source = converter.toSource(targetBeforeConversion); // target
			// in
			// Angstrom,
			// source
			// should
			// be in
			// mm
			Quantity targetAfterConversion = converter.toTarget(source);
			assertEquals(targetAfterConversion, targetBeforeConversion);
		} catch (Exception e) {
			assertEquals("", e.getMessage());
		}
	}

	/**
	 * 
	 */
	@SuppressWarnings("unused")
	public final void testBadCoupled() {
		JEPQuantityConverter converterTarget = new JEPQuantityConverter(testFileFolder + "/mmToDeg.xml");
		JEPQuantityConverter converterSource = new JEPQuantityConverter(testFileFolder + "/DegToAngstrom.xml");
		try {
			new CoupledQuantityConverter(converterSource, converterTarget);
			assertTrue(false);
		} catch (IllegalArgumentException e) {
			String msg = e.getMessage();
			assertEquals("CoupledQuantityConverter.CoupledQuantityConverter: Error target unit (Ang)\n"
					+ " of converter (JEPQuantityConverter using details in " + converterTarget.getExpressionFileName()
					+ ")\n" + " does not match source units (mm)\n"
					+ " of converter (JEPQuantityConverter using details in " + converterSource.getExpressionFileName()
					+ ")", msg);
		}
	}

	/**
	 * 
	 */
	public final void testUnits() {
		JEPQuantityConverter converter = new JEPQuantityConverter(testFileFolder + "/mmToDeg.xml");
		JEPQuantityConverter dummyToGetUnits = new JEPQuantityConverter(testFileFolder + "/DegToAngstrom.xml");
		Quantity targetBeforeConversion = Quantity.valueOf(1.0, dummyToGetUnits.getAcceptableTargetUnits().get(0));
		try {
			converter.toSource(targetBeforeConversion);
			assertTrue(false);
		} catch (IllegalArgumentException e) {
			String msg = e.getMessage();
			assertEquals(
					"JEPQuantityConverter.ToSource: target units (Ang) do not match acceptableUnits (Deg)JEPQuantityConverter using details in "
							+ converter.getExpressionFileName(), msg);
		}
	}

}
