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

package gda.device.detector.mythen.client;

import java.math.BigDecimal;

import org.junit.Test;
import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.ONE;
import static org.junit.Assert.*;

public class BigDecimalUtilsTest {

	private static final BigDecimal MINUS_ONE = new BigDecimal("-1");
	
	@Test
	public void testLess() {
		assertTrue( BigDecimalUtils.isLess(MINUS_ONE, ZERO));
		assertFalse(BigDecimalUtils.isLess(ZERO,      ZERO));
		assertFalse(BigDecimalUtils.isLess(ONE,       ZERO));
	}
	
	@Test
	public void testLessOrEqual() {
		assertTrue( BigDecimalUtils.isLessOrEqual(MINUS_ONE, ZERO));
		assertTrue( BigDecimalUtils.isLessOrEqual(ZERO,      ZERO));
		assertFalse(BigDecimalUtils.isLessOrEqual(ONE,       ZERO));
	}
	
	@Test
	public void testGreaterOrEqual() {
		assertFalse(BigDecimalUtils.isGreaterOrEqual(MINUS_ONE, ZERO));
		assertTrue( BigDecimalUtils.isGreaterOrEqual(ZERO,      ZERO));
		assertTrue( BigDecimalUtils.isGreaterOrEqual(ONE,       ZERO));
	}
	
	@Test
	public void testGreater() {
		assertFalse(BigDecimalUtils.isGreater(MINUS_ONE, ZERO));
		assertFalse(BigDecimalUtils.isGreater(ZERO,      ZERO));
		assertTrue( BigDecimalUtils.isGreater(ONE,       ZERO));
	}
	
}
