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
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;

public class PositionStreamIndexerTest {
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
	
	private PositionInputStream<Object> stream;
	private PositionStreamIndexer<Object> indexer;
	private List<Object> objects;
	
	@Before
	public void setUp() {
		stream = mock(SimplePositionInputStream.class);
		objects = new ArrayList<Object>();
		for (int i = 0; i < 10; i++) {
			objects.add(new NamedObject("object_" + i));
		}
		indexer = new PositionStreamIndexer<Object>(stream);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testExceptionWhenReadingEmptyList() throws Exception {
		when(stream.read(anyInt())).thenReturn(new ArrayList<Object>());
		indexer.getPositionCallable().call();
	}

	@Test
	public void testGetInOneBigChunckWithCreateAllAndThenCallAll() throws Exception {
		streamReturnOneBigChunk();
		checkWithCreateAllAndThenCallAll();
	}
	private void streamReturnOneBigChunk() throws InterruptedException, DeviceException {
		when(stream.read(anyInt())).thenReturn(objects);
	}
	@Test
	public void testGetInOneBigChunckWithCreationFollowedImmediatelyByCall() throws Exception {
		streamReturnOneBigChunk();
		checkWithCreationFollowedImmediatelyByCall();
	}
	
	@Test
	public void testGetInOneBigChunckWithMixedOrder() throws Exception {
		streamReturnChunks();
		checkWithMixedOrder();
	}
	@SuppressWarnings("unchecked")
	private void streamReturnChunks() throws InterruptedException, DeviceException {
		when(stream.read(anyInt())).thenReturn(objects.subList(0, 1), objects.subList(1, 3), objects.subList(3, 6), objects.subList(6, 9), objects.subList(9, 10));
	}
	@Test
	public void testGetInMultipleChunksWithCreateAllAndThenCallAll() throws Exception {
		streamReturnChunks();
		checkWithCreateAllAndThenCallAll();
	}
	@Test
	public void testGetInMultipleChunksWithCreationFollowedImmediatelyByCall() throws Exception {
		streamReturnChunks();
		checkWithCreationFollowedImmediatelyByCall();
	}
	
	@Test
	public void testGetInMultipleChunksWithMixedOrder() throws Exception {
		streamReturnOneBigChunk();
		checkWithMixedOrder();
	}
	
	private void checkWithCreationFollowedImmediatelyByCall() throws Exception, DeviceException {
		for (int i = 0; i < 10; i++) {
			Callable<Object> positionCallable = indexer.getPositionCallable();
			assertEquals(objects.get(i),positionCallable.call());
			assertEquals(objects.get(i),positionCallable.call());
		}
	}
	private void checkWithCreateAllAndThenCallAll() throws DeviceException,
			Exception {
		ArrayList<Callable<Object>> callables = new ArrayList<Callable<Object>>();
		for (int i = 0; i < 10; i++) {
			callables.add(indexer.getPositionCallable());
		}
		for (int i = 0; i < 10; i++) {
			assertEquals(objects.get(i),callables.get(i).call());
		}
	}
	private void checkWithMixedOrder() throws DeviceException, Exception {
		ArrayList<Callable<Object>> callables = new ArrayList<Callable<Object>>();
		callables.add(indexer.getPositionCallable());
		callables.add(indexer.getPositionCallable());
		callables.add(indexer.getPositionCallable());
		assertEquals(objects.get(0),callables.get(0).call());
		assertEquals(objects.get(1),callables.get(1).call());
		callables.add(indexer.getPositionCallable());
		callables.add(indexer.getPositionCallable());
		assertEquals(objects.get(2),callables.get(2).call());
		callables.add(indexer.getPositionCallable());
		callables.add(indexer.getPositionCallable());
		assertEquals(objects.get(3),callables.get(3).call());
		callables.add(indexer.getPositionCallable());
		assertEquals(objects.get(4),callables.get(4).call());
		callables.add(indexer.getPositionCallable());
		assertEquals(objects.get(5),callables.get(5).call());
		callables.add(indexer.getPositionCallable());
		assertEquals(objects.get(6),callables.get(6).call());
		assertEquals(objects.get(7),callables.get(7).call());
		assertEquals(objects.get(8),callables.get(8).call());
		assertEquals(objects.get(9),callables.get(9).call());
	}

}
