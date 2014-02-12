/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.diamond.tomography.reconstruction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.dawb.hdf5.model.IHierarchicalDataFileModel;
import org.dawb.hdf5.model.IHierarchicalDataModel;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.tomography.reconstruction.INexusFilterDescriptor.Operation;

public class NexusFilterTest {

	/**
	 * A value checked and somewhat easier to use Mock than wrapping with Mockito.
	 * Also makes a good basis for the "real" INexusFilterDescriptor
	 */
	private static class MockDescriptor implements INexusFilterDescriptor {
		private static final String[] EMPTY = new String[0];
		private String nexusFilterPath;
		private INexusFilterDescriptor.Operation nexusFilterOperation;
		private String[] nexusFilterOperands;

		public MockDescriptor(String nexusFilterPath, INexusFilterDescriptor.Operation nexusFilterOperation,
				String[] nexusFilterOperands) {
			// Verify conditions at this point so we don't violate non-null and similar contract returns on
			// INexusFilterDescriptor
			if (nexusFilterPath == null || nexusFilterOperation == null)
				throw new NullPointerException("Filter Path and Filter Operation must not be null");
			if (nexusFilterOperands == null) {
				nexusFilterOperands = EMPTY;
			}
			if (nexusFilterOperands.length != nexusFilterOperation.NUMBER_OF_OPERANDS) {
				throw new IllegalArgumentException("Operand count mismatch");
			}

			this.nexusFilterPath = nexusFilterPath;
			this.nexusFilterOperation = nexusFilterOperation;
			this.nexusFilterOperands = nexusFilterOperands;
		}

		@Override
		public String getNexusFilterPath() {
			return nexusFilterPath;
		}

		@Override
		public Operation getNexusFilterOperation() {
			return nexusFilterOperation;
		}

		@Override
		public String[] getNexusFilterOperands() {
			return nexusFilterOperands;
		}

		@Override
		public String getMementoString() {
			throw new UnsupportedOperationException();
		}

	}


	private NexusFilter nexusFilter;
	private IHierarchicalDataModel model;
	private INexusFilterPreferencesCache filterPreferencesCache;
	private IFile ifile_kichwa1_1; // contents: /kichwa1 == 1
	private IFile ifile_kichwa1_2; // contents: /kichwa1 == 2
	private IFile ifile_kichwa2_1; // contents: /kichwa2 == 1
	private IFile ifile_non_nexus; // not a nexus file

	@Before
	public void before() {
		filterPreferencesCache = mock(INexusFilterPreferencesCache.class);
		model = mock(IHierarchicalDataModel.class);
		nexusFilter = new NexusFilter(filterPreferencesCache, model);

		// Create a "fake" file system, refer to the files by their IFiles
		ifile_kichwa1_1 = makeIFile("kichwa1_1.nxs");
		ifile_kichwa1_2 = makeIFile("kichwa1_2.nxs");
		ifile_kichwa2_1 = makeIFile("kichwa2_1.nxs");
		ifile_non_nexus = makeIFile("not_nexus.txt");
		IHierarchicalDataFileModel makeFileModel_1_1 = makeFileModel("/kichwa1", 1);
		IHierarchicalDataFileModel makeFileModel_1_2 = makeFileModel("/kichwa1", 2);
		IHierarchicalDataFileModel makeFileModel_2_1 = makeFileModel("/kichwa2", 1);
		when(model.getFileModel(ifile_kichwa1_1)).thenReturn(makeFileModel_1_1);
		when(model.getFileModel(ifile_kichwa1_2)).thenReturn(makeFileModel_1_2);
		when(model.getFileModel(ifile_kichwa2_1)).thenReturn(makeFileModel_2_1);

		IHierarchicalDataFileModel ifile_non_nexus_contents;
		ifile_non_nexus_contents = mock(IHierarchicalDataFileModel.class);
		when(model.getFileModel(ifile_non_nexus)).thenReturn(ifile_non_nexus_contents);

		// default filter preferences is null
		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(null);
	}

	private IHierarchicalDataFileModel makeFileModel(String path, Object val) {
		IHierarchicalDataFileModel contents;
		contents = mock(IHierarchicalDataFileModel.class);
		when(contents.getPath(path)).thenReturn(val);
		when(contents.hasPath(path)).thenReturn(true);
		return contents;
	}

	private IFile makeIFile(String path) {
		IPath ipath = new Path(path);
		IFile ifile = mock(IFile.class);
		when(ifile.getRawLocation()).thenReturn(ipath);
		when(ifile.toString()).thenReturn(path);
		when(ifile.getName()).thenReturn(path);
		return ifile;
	}

	public boolean select(Object e2) {
		return nexusFilter.select(null, null, e2);
	}

	@Test
	public void testBasic() {
		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(null);
		assertTrue(select(1));
		assertTrue(select(""));
		assertTrue(select(ifile_non_nexus));
		assertTrue(select(ifile_kichwa1_1));
		assertTrue(select(ifile_kichwa1_2));
		assertTrue(select(ifile_kichwa2_1));
	}

	@Test
	public void test_CONTAINS() {
		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.CONTAINS, null));
		assertTrue(select(ifile_kichwa1_2));
		assertFalse(select(ifile_kichwa2_1));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa2", Operation.CONTAINS, null));
		assertFalse(select(ifile_kichwa1_2));
		assertTrue(select(ifile_kichwa2_1));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/not_in_any_file", Operation.CONTAINS, null));
		assertFalse(select(ifile_kichwa1_2));
		assertFalse(select(ifile_kichwa2_1));

		// we always let through non-Nexus files
		assertTrue(select(1));
		assertTrue(select(""));
		assertTrue(select(ifile_non_nexus));
	}

	@Test
	public void test_DOES_NOT_CONTAIN() {
		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.DOES_NOT_CONTAIN, null));
		assertFalse(select(ifile_kichwa1_2));
		assertTrue(select(ifile_kichwa2_1));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa2", Operation.DOES_NOT_CONTAIN, null));
		assertTrue(select(ifile_kichwa1_2));
		assertFalse(select(ifile_kichwa2_1));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa3", Operation.DOES_NOT_CONTAIN, null));
		assertTrue(select(ifile_kichwa1_2));
		assertTrue(select(ifile_kichwa2_1));

		// we always let through non-Nexus files
		assertTrue(select(1));
		assertTrue(select(""));
		assertTrue(select(ifile_non_nexus));
	}

	@Test
	public void testBehaviourWhenCompareFails() {
		// The behaviour in this case is hard to know what to do. If we are supposed to only
		// select things that match the filter, but we can't even run the comparison should
		// we show or hide the file. This is the case where we catch NumberFormatException
		boolean s = NexusFilter.SELECT_WHEN_COMPARE_FAILS_DUE_TO_NUMBERFORMATEXCEPTION;

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.EQUALS, new String[] { "1" }));
		assertEquals(s, select(ifile_kichwa2_1));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.EQUALS, new String[] { "not_a_number" }));
		assertEquals(s, select(ifile_kichwa1_1));
	}

	@Test
	public void test_EQUALS() {
		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.EQUALS, new String[] { "1" }));
		assertFalse(select(ifile_kichwa1_2));
		assertTrue(select(ifile_kichwa1_1));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.EQUALS, new String[] { "2" }));
		assertTrue(select(ifile_kichwa1_2));
		assertFalse(select(ifile_kichwa1_1));
	}

	@Test
	public void test_NOT_EQUALS() {
		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.NOT_EQUALS, new String[] { "1" }));
		assertTrue(select(ifile_kichwa1_2));
		assertFalse(select(ifile_kichwa1_1));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.NOT_EQUALS, new String[] { "2" }));
		assertFalse(select(ifile_kichwa1_2));
		assertTrue(select(ifile_kichwa1_1));
	}

	@Test
	public void test_GREATER_THAN() {
		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.GREATER_THAN, new String[] { "0" }));
		assertTrue(select(ifile_kichwa1_1));
		assertTrue(select(ifile_kichwa1_2));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.GREATER_THAN, new String[] { "1" }));
		assertFalse(select(ifile_kichwa1_1));
		assertTrue(select(ifile_kichwa1_2));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.GREATER_THAN, new String[] { "2" }));
		assertFalse(select(ifile_kichwa1_1));
		assertFalse(select(ifile_kichwa1_2));
	}

	@Test
	public void test_GREATER_THAN_OR_EQUAL() {
		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.GREATER_THAN_OR_EQUAL, new String[] { "1" }));
		assertTrue(select(ifile_kichwa1_1));
		assertTrue(select(ifile_kichwa1_2));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.GREATER_THAN_OR_EQUAL, new String[] { "2" }));
		assertFalse(select(ifile_kichwa1_1));
		assertTrue(select(ifile_kichwa1_2));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.GREATER_THAN_OR_EQUAL, new String[] { "3" }));
		assertFalse(select(ifile_kichwa1_1));
		assertFalse(select(ifile_kichwa1_2));
	}

	@Test
	public void test_LESS_THAN() {
		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.LESS_THAN, new String[] { "3" }));
		assertTrue(select(ifile_kichwa1_1));
		assertTrue(select(ifile_kichwa1_2));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.LESS_THAN, new String[] { "2" }));
		assertTrue(select(ifile_kichwa1_1));
		assertFalse(select(ifile_kichwa1_2));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.LESS_THAN, new String[] { "1" }));
		assertFalse(select(ifile_kichwa1_1));
		assertFalse(select(ifile_kichwa1_2));
	}

	@Test
	public void test_LESS_THAN_OR_EQUAL() {
		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.LESS_THAN_OR_EQUAL, new String[] { "2" }));
		assertTrue(select(ifile_kichwa1_1));
		assertTrue(select(ifile_kichwa1_2));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.LESS_THAN_OR_EQUAL, new String[] { "1" }));
		assertTrue(select(ifile_kichwa1_1));
		assertFalse(select(ifile_kichwa1_2));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.LESS_THAN_OR_EQUAL, new String[] { "0" }));
		assertFalse(select(ifile_kichwa1_1));
		assertFalse(select(ifile_kichwa1_2));
	}

	@Test
	public void testInverseInterval() {
		// The GUI/creator of INexusFilterDescriptor is expected to create intervals which
		// are logical (i.e. op0 < op1).
		// If the INexusFilterDescriptor returns such a case, then nothing matches...
		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.OPEN_INTERVAL, new String[] { "2", "0" }));
		assertFalse(select(ifile_kichwa1_1));
		assertFalse(select(ifile_kichwa1_2));

		// ... except files which the comparison fails on
		assertTrue(select(ifile_kichwa2_1));
	}

	@Test
	public void test_OPEN_INTERVAL() {
		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.OPEN_INTERVAL, new String[] { "0", "2" }));
		assertTrue(select(ifile_kichwa1_1));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.OPEN_INTERVAL, new String[] { "1", "2" }));
		assertFalse(select(ifile_kichwa1_1));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.OPEN_INTERVAL, new String[] { "0", "1" }));
		assertFalse(select(ifile_kichwa1_1));
	}

	@Test
	public void test_CLOSED_INTERVAL() {
		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.CLOSED_INTERVAL, new String[] { "0", "1" }));
		assertFalse(select(ifile_kichwa1_2));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.CLOSED_INTERVAL, new String[] { "1", "2" }));
		assertTrue(select(ifile_kichwa1_2));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.CLOSED_INTERVAL, new String[] { "1", "3" }));
		assertTrue(select(ifile_kichwa1_2));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.CLOSED_INTERVAL, new String[] { "2", "3" }));
		assertTrue(select(ifile_kichwa1_2));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", Operation.CLOSED_INTERVAL, new String[] { "3", "4" }));
		assertFalse(select(ifile_kichwa1_2));
	}

	@Test
	public void test_LEFT_CLOSED_RIGHT_OPEN_INTERVAL() {
		Operation leftClosedRightOpenInterval = Operation.LEFT_CLOSED_RIGHT_OPEN_INTERVAL;
		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", leftClosedRightOpenInterval, new String[] { "0", "1" }));
		assertFalse(select(ifile_kichwa1_2));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", leftClosedRightOpenInterval, new String[] { "0", "2" }));
		assertFalse(select(ifile_kichwa1_2));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", leftClosedRightOpenInterval, new String[] { "0", "3" }));
		assertTrue(select(ifile_kichwa1_2));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", leftClosedRightOpenInterval, new String[] { "1", "2" }));
		assertFalse(select(ifile_kichwa1_2));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", leftClosedRightOpenInterval, new String[] { "1", "3" }));
		assertTrue(select(ifile_kichwa1_2));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", leftClosedRightOpenInterval, new String[] { "2", "3" }));
		assertTrue(select(ifile_kichwa1_2));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", leftClosedRightOpenInterval, new String[] { "3", "4" }));
		assertFalse(select(ifile_kichwa1_2));
	}

	@Test
	public void test_LEFT_OPEN_RIGHT_CLOSED_INTERVAL() {
		Operation leftOpenRightClosedInterval = Operation.LEFT_OPEN_RIGHT_CLOSED_INTERVAL;
		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", leftOpenRightClosedInterval, new String[] { "0", "1" }));
		assertFalse(select(ifile_kichwa1_2));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", leftOpenRightClosedInterval, new String[] { "0", "2" }));
		assertTrue(select(ifile_kichwa1_2));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", leftOpenRightClosedInterval, new String[] { "0", "3" }));
		assertTrue(select(ifile_kichwa1_2));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", leftOpenRightClosedInterval, new String[] { "1", "2" }));
		assertTrue(select(ifile_kichwa1_2));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", leftOpenRightClosedInterval, new String[] { "1", "3" }));
		assertTrue(select(ifile_kichwa1_2));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", leftOpenRightClosedInterval, new String[] { "2", "3" }));
		assertFalse(select(ifile_kichwa1_2));

		when(filterPreferencesCache.getNexusFilterDescriptor()).thenReturn(
				new MockDescriptor("/kichwa1", leftOpenRightClosedInterval, new String[] { "3", "4" }));
		assertFalse(select(ifile_kichwa1_2));
	}
}
