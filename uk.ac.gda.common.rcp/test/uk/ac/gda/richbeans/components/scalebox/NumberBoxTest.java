/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.richbeans.components.scalebox;

import static org.junit.Assert.assertEquals;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.BeforeClass;
import org.junit.Test;

import swing2swt.layout.BorderLayout;

public class NumberBoxTest {


	
	static private Display display;
	static private Shell shell;
	@BeforeClass
	static public void beforeClass(){
		display = new Display();
		shell = new Shell(display);
		shell.setLayout(new BorderLayout());
	}

	private void testDecimalPlaces(int decimalPlaces, String newUnit, double expected, String txt){
		DemandBox box = new DemandBox(shell, SWT.NONE);
		box.setDecimalPlaces(decimalPlaces);
		box.setUnit(newUnit);
		assertEquals("0.12345", expected,   box.getNumericValue(txt),1e-10);
	}
	
	@Test
	public void testDecimalPlaces0() {
		testDecimalPlaces(0, null, 0., "0.12345");
	}
	
	@Test
	public void testDecimalPlaces1() {
		testDecimalPlaces(1, null, 0.1, "0.12345");
	}
	@Test
	public void testDecimalPlaces2() {
		testDecimalPlaces(2, null, 0.12, "0.12345");
	}
	@Test
	public void testDecimalPlaces3() {
		testDecimalPlaces(3, null, 0.123, "0.12345");
	}
	
	// should round up to nearest value 
	@Test
	public void testDecimalPlaces4() {
		testDecimalPlaces(4, null, 0.1235, "0.12345");
	}

	@Test
	public void testDecimalPlaces5() {
		testDecimalPlaces(5, null, 0.12345, "0.12345");
	}

	@Test
	public void testDecimalPlaces0Units() {
		testDecimalPlaces(0, "mm", 0., "0.12345 mm");
	}
	
	@Test
	public void testDecimalPlaces1Units() {
		testDecimalPlaces(1, "mm", 0.1, "0.12345 mm");
	}
	@Test
	public void testDecimalPlaces2Units() {
		testDecimalPlaces(2, "mm", 0.12, "0.12345 mm");
	}
	@Test
	public void testDecimalPlaces3Units() {
		testDecimalPlaces(3, "mm", 0.123, "0.12345 mm");
	}
	@Test
	public void testDecimalPlaces4Units() {
		testDecimalPlaces(4, "mm", 0.1235, "0.12345 mm");
	}

	@Test
	public void testDecimalPlaces5Units() {
		testDecimalPlaces(5, "mm", 0.12345, "0.12345 mm");
	}

	@Test
	public void testDecimalPlaces5UnitsPlus() {
		testDecimalPlaces(5, "mm", 0.12345, "+0.12345 mm");
	}

	@Test
	public void testDecimalPlaces5UnitsMinus() {
		testDecimalPlaces(5, "mm", -0.12345, "-0.12345 mm");
	}
	
	
	@Test
	public void testDecimalPlaces0NoDecPoint() {
		testDecimalPlaces(0, null, 0, "0");
	}

	@Test
	public void testDecimalPlaces5UnitsSpaceBefore() {
		testDecimalPlaces(5, "mm", 0.12345, " 0.12345 mm");
	}
	
	@Test
	public void testDecimalPlaces5UnitsSpaceBeforeNoDecPlace() {
		testDecimalPlaces(5, "mm", 1234, " 1234 mm");
	}

	@Test
	public void testDecimalPlaces5UnitsSpaceBeforeNoNumberBeforeDecPlace() {
		testDecimalPlaces(5, "mm", .1234, " .1234 mm");
	}

}
