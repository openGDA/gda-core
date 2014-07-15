/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.dawnsci.hdf5.model.IHierarchicalDataFileModel;
import org.eclipse.dawnsci.hdf5.model.IHierarchicalDataModel;
import org.junit.Before;
import org.junit.Test;

public class NexusSorterTest {

	private NexusSorter nexusSorter;
	private IHierarchicalDataModel model;
	private INexusSorterPreferencesCache sorterPreferencesCache;
	private IFile ifile_a; // contents: /kichwa1 == 2
	private IFile ifile_b; // contents: /kichwa1 == 1
	private IFile ifile_c; // contents: /kichwa1 == 2
	private IFile ifile_d; // contents: /kichwa2 == 1
	private IFile ifile_e; // contents: /kichwa1 == 2

	@Before
	public void before() {
		sorterPreferencesCache = mock(INexusSorterPreferencesCache.class);
		model = mock(IHierarchicalDataModel.class);
		nexusSorter = new NexusSorter(sorterPreferencesCache, model);

		// Create a "fake" file system, refer to the files by their IFiles
		ifile_a = makeIFile("a.nxs");
		ifile_b = makeIFile("b.nxs");
		ifile_c = makeIFile("c.nxs");
		ifile_d = makeIFile("d.nxs");
		ifile_e = makeIFile("e.nxs");
		IHierarchicalDataFileModel makeFileModel_a = makeFileModel("/kichwa1", 2);
		IHierarchicalDataFileModel makeFileModel_b = makeFileModel("/kichwa1", 1);
		IHierarchicalDataFileModel makeFileModel_c = makeFileModel("/kichwa1", 2);
		IHierarchicalDataFileModel makeFileModel_d = makeFileModel("/kichwa2", 1);
		IHierarchicalDataFileModel makeFileModel_e = makeFileModel("/kichwa1", 2);
		when(model.getFileModel(ifile_a)).thenReturn(makeFileModel_a);
		when(model.getFileModel(ifile_b)).thenReturn(makeFileModel_b);
		when(model.getFileModel(ifile_c)).thenReturn(makeFileModel_c);
		when(model.getFileModel(ifile_d)).thenReturn(makeFileModel_d);
		when(model.getFileModel(ifile_e)).thenReturn(makeFileModel_e);

		// default sort order is none (aka null)
		when(sorterPreferencesCache.getNexusSortPath()).thenReturn(null);
	}

	private IHierarchicalDataFileModel makeFileModel(String path, Object val) {
		IHierarchicalDataFileModel contents;
		contents = mock(IHierarchicalDataFileModel.class);
		when(contents.getPath(path)).thenReturn(val);
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


	public int compare(Object e1, Object e2) {
		return nexusSorter.compare(null, e1, e2);
	}

	@Test
	public void testBasic() {
		assertTrue(compare("", "") == 0);
		assertTrue(compare(1, 2) < 0);
		assertTrue(compare(2, 1) > 0);
	}

	@Test
	public void testCompareNexusToNonNexus() {
		when(sorterPreferencesCache.getNexusSortPath()).thenReturn("/kichwa1");
		assertTrue(compare(1, ifile_a) > 0);
		assertTrue(compare(ifile_a, 1) < 0);
	}

	@Test
	public void testNexusFiles() {
		when(sorterPreferencesCache.getNexusSortPath()).thenReturn("/kichwa1");

		assertTrue(compare(ifile_a, ifile_b) > 0);
		assertTrue(compare(ifile_b, ifile_a) < 0);
		// values are equal, falls to second order sort, i.e. by name
		assertTrue(compare(ifile_a, ifile_c) < 0);
		assertTrue(compare(ifile_e, ifile_c) > 0);
		assertTrue(compare(ifile_c, ifile_c) == 0);
	}

	@Test
	public void testNexusFilesNoSortSet1() {
		when(sorterPreferencesCache.getNexusSortPath()).thenReturn(null);

		assertTrue(compare(ifile_a, ifile_b) < 0);
		assertTrue(compare(ifile_b, ifile_c) < 0);
		assertTrue(compare(ifile_a, ifile_c) < 0);
		assertTrue(compare(ifile_c, ifile_a) > 0);
		assertTrue(compare(ifile_c, ifile_c) == 0);
	}
	@Test
	public void testNexusFilesNoSortSet2() {
		when(sorterPreferencesCache.getNexusSortPath()).thenReturn("");

		assertTrue(compare(ifile_a, ifile_b) < 0);
		assertTrue(compare(ifile_b, ifile_c) < 0);
		assertTrue(compare(ifile_a, ifile_c) < 0);
		assertTrue(compare(ifile_c, ifile_a) > 0);
		assertTrue(compare(ifile_c, ifile_c) == 0);
	}

	@Test
	public void testNexusFileNoKey() {
		when(sorterPreferencesCache.getNexusSortPath()).thenReturn("/kichwa1");
		assertTrue(compare(ifile_b, ifile_d) < 0);
		when(sorterPreferencesCache.getNexusSortPath()).thenReturn("/kichwa2");
		assertTrue(compare(ifile_b, ifile_d) > 0);

		when(sorterPreferencesCache.getNexusSortPath()).thenReturn("/does_not_exists_in_any");
		assertTrue(compare(ifile_a, ifile_b) < 0);
		assertTrue(compare(ifile_b, ifile_c) < 0);
		assertTrue(compare(ifile_a, ifile_c) < 0);
	}

}
