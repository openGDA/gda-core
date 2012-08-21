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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;

public class MultiplePositionStreamIndexerTest {
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
	private PositionCallableProvider<List<Object>> indexer;
	private List<Object> objects1;
	private List<Object> objects2;

	@Before
	public void setUp() {
		stream1 = mock(SimplePositionInputStream.class);
		stream2 = mock(SimplePositionInputStream.class);
		objects1 = new ArrayList<Object>();
		objects2 = new ArrayList<Object>();
		for (int i = 0; i < 10; i++) {
			objects1.add(new NamedObject("object_1_" + i));
			objects2.add(new NamedObject("object_2_" + i));
		}
		List<PositionInputStream<Object>> streamList = Arrays.asList((PositionInputStream<Object>) stream1,
				(PositionInputStream<Object>) stream2);
		indexer = new MultiplePositionStreamIndexer<Object>(streamList);
	}

	@Test
	public void testGetInOneBigChunckWithCreateAllAndThenCallAll() throws Exception {
		streamsReturnOneBigChunk();
		checkWithCreateAllAndThenCallAll();
	}

	private void streamsReturnOneBigChunk() throws InterruptedException, DeviceException {
		when(stream1.read(anyInt())).thenReturn(objects1);
		when(stream2.read(anyInt())).thenReturn(objects2);
	}

	@Test
	public void testGetInOneBigChunckWithCreationFollowedImmediatelyByCall() throws Exception {
		streamsReturnOneBigChunk();
		checkWithCreationFollowedImmediatelyByCall();
	}

	@Test
	public void testGetInOneBigChunckWithMixedOrder() throws Exception {
		streamsReturnOneBigChunk();
		checkWithMixedOrder();
	}

	@Test
	public void testGetInMultipleChunksWithCreateAllAndThenCallAll() throws Exception {
		streamsReturnUncorellatedChunks();
		checkWithCreateAllAndThenCallAll();
	}

	@Test
	public void testGetInMultipleChunksWithCreationFollowedImmediatelyByCall() throws Exception {
		streamsReturnUncorellatedChunks();
		checkWithCreationFollowedImmediatelyByCall();
	}

	@SuppressWarnings("unchecked")
	private void streamsReturnUncorellatedChunks() throws InterruptedException, DeviceException {
		when(stream1.read(anyInt())).thenReturn(objects1.subList(0, 1), objects1.subList(1, 3), objects1.subList(3, 6),
				objects1.subList(6, 9), objects1.subList(9, 10));
		when(stream2.read(anyInt())).thenReturn(objects2.subList(0, 2), objects2.subList(2, 3), objects2.subList(3, 5),
				objects2.subList(5, 10));
	}

	@Test
	public void testGetInMultipleChunksWithMixedOrder() throws Exception {
		streamsReturnUncorellatedChunks();
		checkWithMixedOrder();
	}

	private void checkWithCreationFollowedImmediatelyByCall() throws Exception, DeviceException {
		for (int i = 0; i < 10; i++) {
			List<Object> objectList = indexer.getPositionCallable().call();
			assertEquals(objects1.get(i), objectList.get(0));
			assertEquals(objects2.get(i), objectList.get(1));
		}
	}

	private void checkWithCreateAllAndThenCallAll() throws DeviceException, Exception {
		ArrayList<Callable<List<Object>>> callables = new ArrayList<Callable<List<Object>>>();
		for (int i = 0; i < 10; i++) {
			callables.add(indexer.getPositionCallable());
		}
		for (int i = 0; i < 10; i++) {
			List<Object> objectList = callables.get(i).call();
			assertEquals(objects1.get(i), objectList.get(0));
			assertEquals(objects2.get(i), objectList.get(1));
		}
	}

	private void checkWithMixedOrder() throws DeviceException, Exception {
		ArrayList<Callable<List<Object>>> callables = new ArrayList<Callable<List<Object>>>();
		callables.add(indexer.getPositionCallable());
		callables.add(indexer.getPositionCallable());
		callables.add(indexer.getPositionCallable());
		List<Object> call = callables.get(0).call();
		call = callables.get(0).call();
		assertEquals(objects1.get(0), call.get(0));
		assertEquals(objects2.get(0), call.get(1));
//		assertEquals(objects.get(1), callables.get(1).call());
//		callables.add(indexer.getPositionCallable());
//		callables.add(indexer.getPositionCallable());
//		assertEquals(objects.get(2), callables.get(2).call());
//		callables.add(indexer.getPositionCallable());
//		callables.add(indexer.getPositionCallable());
//		assertEquals(objects.get(3), callables.get(3).call());
//		callables.add(indexer.getPositionCallable());
//		assertEquals(objects.get(4), callables.get(4).call());
//		callables.add(indexer.getPositionCallable());
//		assertEquals(objects.get(5), callables.get(5).call());
//		callables.add(indexer.getPositionCallable());
//		assertEquals(objects.get(6), callables.get(6).call());
//		assertEquals(objects.get(7), callables.get(7).call());
//		assertEquals(objects.get(8), callables.get(8).call());
//		assertEquals(objects.get(9), callables.get(9).call());
	}

}
