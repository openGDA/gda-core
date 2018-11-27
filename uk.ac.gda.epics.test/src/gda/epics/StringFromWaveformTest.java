/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.epics;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import gda.epics.LazyPVFactory.StringFromWaveform;

public class StringFromWaveformTest {

	@Test
	public void test_innerToOuter_with_text_and_many_null_bytes() {
		final StringFromWaveform sfw = new StringFromWaveform(null);
		final Byte[] bytes = new Byte[] {49, 50, 51, 0, 0, 0, 0, 0, 0, 0};
		final String str = sfw.innerToOuter(bytes);
		assertEquals(3, str.length());
		assertEquals("123", str);
	}

	@Test
	public void test_innerToOuter_with_text_and_one_null_byte() {
		final StringFromWaveform sfw = new StringFromWaveform(null);
		final Byte[] bytes = new Byte[] {49, 50, 51, 0};
		final String str = sfw.innerToOuter(bytes);
		assertEquals(3, str.length());
		assertEquals("123", str);
	}

	@Test
	public void test_innerToOuter_with_text_and_no_null_bytes() {
		final StringFromWaveform sfw = new StringFromWaveform(null);
		final Byte[] bytes = new Byte[] {49, 50, 51};
		final String str = sfw.innerToOuter(bytes);
		assertEquals(3, str.length());
		assertEquals("123", str);
	}

	@Test
	public void test_innerToOuter_with_only_null_bytes() {
		final StringFromWaveform sfw = new StringFromWaveform(null);
		final Byte[] bytes = new Byte[] {0, 0, 0};
		final String str = sfw.innerToOuter(bytes);
		assertEquals(0, str.length());
		assertEquals("", str);
	}

	@Test
	public void test_innerToOuter_with_text_and_null_byte_in_middle() {
		final StringFromWaveform sfw = new StringFromWaveform(null);
		final Byte[] bytes = new Byte[] {49, 50, 51, 0, 52, 53, 54, 0};
		final String str = sfw.innerToOuter(bytes);
		assertEquals(3, str.length());
		assertEquals("123", str);
	}

	@Test
	public void test_innerToOuter_with_text_and_null_bytes_either_side() {
		final StringFromWaveform sfw = new StringFromWaveform(null);
		final Byte[] bytes = new Byte[] {0, 80, 83, 45, 52, 56, 56, 54, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		final String str = sfw.innerToOuter(bytes);
		assertEquals(0, str.length());
		assertEquals("", str);
	}

	@Test
	public void test_innerToOuter_trims_leading_whitespace() {
		final StringFromWaveform sfw = new StringFromWaveform(null);
		final Byte[] bytes = new Byte[] {32, 32, 49, 50, 51, 0, 0, 0};
		final String str = sfw.innerToOuter(bytes);
		assertEquals(3, str.length());
		assertEquals("123", str);
	}

	@Test
	public void test_innerToOuter_trims_trailing_whitespace() {
		final StringFromWaveform sfw = new StringFromWaveform(null);
		final Byte[] bytes = new Byte[] {49, 50, 51, 32, 32, 0, 0, 0};
		final String str = sfw.innerToOuter(bytes);
		assertEquals(3, str.length());
		assertEquals("123", str);
	}

}
