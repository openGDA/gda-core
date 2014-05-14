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

package gda.spring.propertyeditors;

import org.apache.commons.math.linear.RealVector;

import junit.framework.TestCase;

public class RealVectorPropertyEditorTest extends TestCase {
	
	public void testConversion() {
		RealVectorPropertyEditor pe = new RealVectorPropertyEditor();
		assertNull(pe.getValue());
		String text = "{1,2,3,4}";
		pe.setAsText(text);
		RealVector vector = pe.getValue();
		double[] expectedValues = new double[] {1, 2, 3, 4};
		assertVectorsEqual(expectedValues, vector);
		assertEquals(text, pe.getAsText());
	}
	
	protected void assertVectorsEqual(double[] expected, RealVector actual) {
		assertEquals("Length does not match", expected.length, actual.getDimension());
		for (int i=0; i<expected.length; i++) {
			assertEquals("Element " + i + " does not match", expected[i], actual.getData()[i]);
		}
	}

}
