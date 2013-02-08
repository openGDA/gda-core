/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.scannable;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gda.device.DeviceException;

import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;

public class PositionInputStreamCombinerTest {
	interface SimplePositionInputStream extends PositionInputStream<Object> {

	}

	class NamedObject {
		final private String name;

		public NamedObject(String name) {
			super();
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

	}

	private PositionInputStream<Object> stream1;
	private PositionInputStream<Object> stream2;
	private PositionInputStream<List<Object>> combinedStream;
	private Object a0 = new NamedObject("a0");
	private Object a1 = new NamedObject("a1");
	private Object a2 = new NamedObject("a2");
	private Object a3 = new NamedObject("a3");
	private Object b0 = new NamedObject("b0");
	private Object b1 = new NamedObject("b1");
	private Object b2 = new NamedObject("b2");
	private Object b3 = new NamedObject("b3");
	private List<Object> pair0 = asList(a0, b0);
	private List<Object> pair1 = asList(a1, b1);
	private List<Object> pair2 = asList(a2, b2);
	private List<Object> pair3 = asList(a3, b3);

	@Before
	public void setUp() {
		stream1 = mock(SimplePositionInputStream.class);
		stream2 = mock(SimplePositionInputStream.class);
		@SuppressWarnings("unchecked")
		List<PositionInputStream<Object>> streamList = asList(stream1, stream2);
		combinedStream = new PositionInputStreamCombiner<Object>(streamList);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetInOneBigChunck() throws Exception {
		when(stream1.read(anyInt())).thenReturn(asList(a0, a1, a2, a3));
		when(stream2.read(anyInt())).thenReturn(asList(b0, b1, b2, b3));
		assertEquals(asList(pair0, pair1, pair2, pair3), combinedStream.read(1000));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetInEvenChuncks() throws Exception {
		when(stream1.read(anyInt())).thenReturn(asList(a0), asList(a1, a2), asList(a3));
		when(stream2.read(anyInt())).thenReturn(asList(b0), asList(b1, b2), asList(b3));
		assertEquals(asList(pair0), combinedStream.read(1000));
		assertEquals(asList(pair1, pair2), combinedStream.read(1000));
		assertEquals(asList(pair3), combinedStream.read(1000));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetInUnevenChuncks() throws Exception {
		when(stream1.read(anyInt())).thenReturn(asList(a0, a1), asList(a2, a3));
		when(stream2.read(anyInt())).thenReturn(asList(b0), asList(b1, b2, b3));
		assertEquals(asList(pair0), combinedStream.read(1000));
		assertEquals(asList(pair1), combinedStream.read(1000));
		assertEquals(asList(pair2, pair3), combinedStream.read(1000));
	}

}
