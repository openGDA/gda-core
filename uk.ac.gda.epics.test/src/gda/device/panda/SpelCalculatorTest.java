/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package gda.device.panda;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class SpelCalculatorTest {

	private static final int ITERATION_LIMIT = 1000000;
	private static final double TOLERANCE = 1e-6;

	private SpelCalculator calculator = new SpelCalculator();

	@Before
	public void prepare() {
		calculator.clearOutputExpressions();
		calculator.addOutputExpression("val 1", "#A");
		calculator.addOutputExpression("val 2", "2*#log(#A)");
		calculator.setDataVariableNames(List.of("A"));
	}

	@Test
	public void testSingleInput() {
		for(double v=0.1; v<ITERATION_LIMIT*0.03; v*=1.59) {
			var result = calculator.computeOutput(v);
			assertEquals("Number of calculated values is not correct", 2, result.size());
			assertEquals(v, result.get(0), TOLERANCE);
			assertEquals(2 * Math.log(v), result.get(1), TOLERANCE);
		}
	}

	@Test
	public void testMathOperationsTwoInputs() {
		calculator.clearOutputExpressions();
		calculator.addOutputExpression("plus", "#A+#B");
		calculator.addOutputExpression("minus", "#A-#B");
		calculator.addOutputExpression("mult", "#A*#B");
		calculator.addOutputExpression("divide", "#A/#B");
		calculator.addOutputExpression("percentage", "#A/(#A+#B)");

		calculator.setDataVariableNames(List.of("A", "B"));
		var a = 1.460e6 * Math.E;
		var b = 0.47e-6 * Math.PI;
		for (int i = 1; i < ITERATION_LIMIT; i<<=1) {
			var results = calculator.computeOutput(List.of(a, b));
			assertEquals("Number of calculated values is not correct", 5, results.size(), TOLERANCE);
			assertEquals("addition", a+b, results.get(0), TOLERANCE);
			assertEquals("subtraction", a-b, results.get(1), TOLERANCE);
			assertEquals("multipication", a*b, results.get(2), TOLERANCE);
			assertEquals("division", a/b, results.get(3), TOLERANCE);
			assertEquals("percentage", a/(a+b), results.get(4), TOLERANCE);
		}
	}

	@Test
	public void testMathFunctions() {
		calculator.clearOutputExpressions();
		calculator.addOutputExpression("sin", "#sin(#A)");
		calculator.addOutputExpression("cos", "#cos(#A)");
		calculator.addOutputExpression("tan", "#tan(#A)");
		calculator.addOutputExpression("exp", "#exp(#A)");
		calculator.addOutputExpression("log", "#log(#A)");
		calculator.addOutputExpression("log_inv", "#log(1/#A)");
		calculator.addOutputExpression("exp_sin", "#sin(#A)*#exp(-#A*0.5)");

		calculator.setDataVariableNames(List.of("A"));
		for(double x = 0.1; x<10; x+=0.1) {
			List<Double> results = calculator.computeOutput(x);
			assertEquals(7, results.size());
			assertEquals("sin", Math.sin(x), results.get(0), TOLERANCE);
			assertEquals("cos", Math.cos(x), results.get(1), TOLERANCE);
			assertEquals("tan", Math.tan(x), results.get(2), TOLERANCE);
			assertEquals("exp", Math.exp(x), results.get(3), TOLERANCE);
			assertEquals("log", Math.log(x), results.get(4), TOLERANCE);
			assertEquals("log_inv", Math.log(1.0/x), results.get(5), TOLERANCE);
			assertEquals("exp_sin", Math.sin(x)*Math.exp(-x*0.5), results.get(6), TOLERANCE);
		}
	}

	@Test
	public void testExtraVariable() {
		calculator.clearOutputExpressions();
		calculator.addOutputExpression("plus", "#A+#B");
		calculator.setDataVariableNames(List.of("A"));

		// set value of a custom variable
		calculator.setVariable("B", 10.0);

		for(double i=0; i<10; i+=0.5) {
			List<Double> results = calculator.computeOutput(i);
			assertEquals(i+10, results.get(0), TOLERANCE);
		}
	}

	@Test
	public void testExtraVariable2() {
		calculator.clearOutputExpressions();
		calculator.addOutputExpression("plus", "(#A-#Aoffset)/(#B - #Boffset)");
		calculator.setDataVariableNames(List.of("A", "B"));
		double aOffset = 12.0;
		double bOffset = 17.0;

		// set value of a custom variable
		calculator.setVariable("Aoffset", aOffset);
		calculator.setVariable("Boffset", bOffset);

		List<Double> results = calculator.computeOutput(List.of(10.0,11.0));
		assertEquals( (10-aOffset)/(11-bOffset), results.get(0), TOLERANCE);

		results = calculator.computeOutput(List.of(1000.0, 100000.0));
		assertEquals( (1000-aOffset)/(100000-bOffset), results.get(0), TOLERANCE);
	}

	@Test(timeout = 1000)
	public void testIonchambers() {
		calculator.clearOutputExpressions();
		calculator.setDataVariableNames(List.of("I0", "It", "Iref"));

		calculator.addOutputExpression("I0", "#I0 - #I0Dark");
		calculator.addOutputExpression("It", "#It - #ItDark");
		calculator.addOutputExpression("Iref", "#Iref - #IrefDark");
		calculator.addOutputExpression("lnI0It", "#log( (#I0 - #I0Dark)/(#It - #ItDark))");
		calculator.addOutputExpression("lnItIref", "#log( (#It - #ItDark)/(#Iref - #IrefDark))");

		for (int i = 1; i < ITERATION_LIMIT; i<<=1) {
			double i0Dark = 1.0;
			double itDark = 52.0;
			double irefDark = 11.0;

			double i0 = i0Dark + 865.2613038;
			double it = itDark + 91.391045;
			double iref = irefDark + 128.3207;

			calculator.setVariable("I0Dark", i0Dark);
			calculator.setVariable("ItDark", itDark);
			calculator.setVariable("IrefDark", irefDark);
			List<Double> results = calculator.computeOutput(List.of(i0, it, iref));

			assertEquals("I0 value", i0 - i0Dark, results.get(0), TOLERANCE);
			assertEquals("It value", it - itDark, results.get(1), TOLERANCE);
			assertEquals("Iref value", iref - irefDark, results.get(2), TOLERANCE);
			assertEquals("lnI0It value", Math.log((i0 - i0Dark) / (it - itDark)), results.get(3), TOLERANCE);
			assertEquals("lnItIref value", Math.log((it - itDark) / (iref - irefDark)), results.get(4), TOLERANCE);
		}
	}
}