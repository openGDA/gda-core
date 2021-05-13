/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.tool;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests basic functionalities for the {@link ClientVerifyListener} class.
 *
 * @author Maurizio Nagni
 */
public class VerifyListenerTest {


	/**
	 * Verify a number as signed double
	 */
	@Test
	public void stringIsDoubleNumber() {
		assertTrue(ClientVerifyListener.stringIsDoubleNumber("123.1"));
		assertTrue(ClientVerifyListener.stringIsDoubleNumber("+123.1"));
		assertTrue(ClientVerifyListener.stringIsDoubleNumber("-123.1"));
		assertTrue(ClientVerifyListener.stringIsDoubleNumber("-123.1 "));
		assertTrue(ClientVerifyListener.stringIsDoubleNumber(" -123.1 "));
		assertFalse(ClientVerifyListener.stringIsDoubleNumber("-123..1"));
	}

	/**
	 * Verify a number as signed integer
	 */
	@Test
	public void stringIsIntegerNumber() {
		assertTrue(ClientVerifyListener.stringIsDoubleNumber("+123"));
		assertTrue(ClientVerifyListener.stringIsDoubleNumber("-123"));
		assertTrue(ClientVerifyListener.stringIsDoubleNumber("-123 "));
		assertTrue(ClientVerifyListener.stringIsDoubleNumber(" -123 "));
		assertFalse(ClientVerifyListener.stringIsDoubleNumber("-123 1"));
	}
}
