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

import org.eclipse.richbeans.widgets.scalebox.DemandBox;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
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
	
	private void testBounds(int decimalPlaces, String newUnit, double expectedLow, double expectedHigh, String txtLow, String txtHigh){
		ScaleBox boxLow = new ScaleBox(shell, SWT.NONE);
		boxLow.setDecimalPlaces(decimalPlaces);
		boxLow.setUnit(newUnit);
		ScaleBox boxHigh = new ScaleBox(shell, SWT.NONE);
		boxHigh.setDecimalPlaces(decimalPlaces);
		boxHigh.setUnit(newUnit);
		
		boxLow.setMinimum(Double.NEGATIVE_INFINITY);
		boxHigh.setMaximum(Double.POSITIVE_INFINITY);
		
		boxLow.setMaximum(boxHigh);
		boxHigh.setMinimum(boxLow);
		
		boxLow.setValue(txtLow);
		boxHigh.setValue(txtHigh);
		
		double min = boxHigh.getMinimum();
		double max = boxLow.getMaximum();
		
		assertEquals("0.12345", expectedLow, min ,1e-10);
		assertEquals("1234.5", expectedHigh, max ,1e-10);
	}
	
	@Test
	public void testDecimalPlaces0() {
		testDecimalPlaces(0, null, 0., "0.12345");
	}

	@Test
	public void testDecimalPlaces6() {
		testDecimalPlaces(2, null, 3.0, "3.");
	}
	
	@Test
	public void testInfinity() {
		testDecimalPlaces(0, null, Double.POSITIVE_INFINITY, "Infinity");
	}
	
	@Test
	public void testNegativeInfinity() {
		testDecimalPlaces(0, null, Double.NEGATIVE_INFINITY, "-Infinity");
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
	public void test5Figures() {
		testDecimalPlaces(5, null, 12345, "12345");
	}

	@Test
	public void testDecimalPlaces0Units() {
		testDecimalPlaces(0, "mm", 0., "0.12345 mm");
	}
	
	@Test
	public void test10Percent() {
		testDecimalPlaces(5, "%", 10.000, "10.000 %");
	}
	
	@Test
	public void test10PercentSpaces() {
		testDecimalPlaces(5, "%", 10.000, "10.000 %");
	}
	
	@Test
	public void testDecimalPlaces0UnitsNoSpace() {
		testDecimalPlaces(0, "mm", 0., "0.12345mm");
	}
	
	@Test
	public void testDecimalPlaces5UnitsNoSpace() {
		testDecimalPlaces(5, "mm", 0.12345, "0.12345mm");
	}
	
	@Test
	public void testDecimalPlaces0UnitsSomeSpace() {
		testDecimalPlaces(5, "mm", 0.12345, "0.12345   mm");
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
	
	@Test
	public void testDecimalPlaces5UnitsPlusMinus() {
		testDecimalPlaces(5, "mm", Double.NaN, "+-0.12345 mm");
	}
	
	@Test
	public void testDecimalPlaces5UnitsSpaceInNumber() {
		testDecimalPlaces(5, "mm", Double.NaN, "0. 12345 mm");
	}
	
	@Test
	public void testDot() {
		testDecimalPlaces(5, null , Double.NaN, ".");
	}
	
	@Test
	public void testDotAndUnits() {
		testDecimalPlaces(5, null , Double.NaN, ". mm");
	}
	
	@Test
	public void testBounds() {
		testBounds(5, "mm",0.12345,1234.5, "0.12345 mm", "1234.5 mm");
	}
	
	@Test
	public void testBoundsInfinite() {
		testBounds(5, null,0.12345,Double.POSITIVE_INFINITY, "0.12345", "Infinity");
	}
	
	@Test
	public void testBoundsInfiniteUnits() {
		testBounds(5, "mm" ,0.12345,Double.POSITIVE_INFINITY, "0.12345 mm", "Infinity mm");
	}
	
	@Test
	public void testBoundsNegativeInfinite() {
		testBounds(5, null,Double.NEGATIVE_INFINITY, 0.12345,"-Infinity" , "0.12345");
	}
	
	@Test
	public void testBoundsNegativeInfiniteUnits() {
		testBounds(5, "mm" ,Double.NEGATIVE_INFINITY, 0.12345,"-Infinity mm" , "0.12345 mm");
	}

}
