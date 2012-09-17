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

package gda.data.nexus;

import static org.junit.Assert.fail;
import gda.util.TestUtils;

import java.io.File;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;

/**
 * A collection of tests that causes the JVM to crash
 */
public class NexusFileStressTest {
	static String testScratchDirectoryName;
	static String filename;

	/**
	 * Creates an empty directory for use by test code.
	 * 
	 * @throws Exception if setup fails
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(NexusFileStressTest.class.getCanonicalName());
		TestUtils.makeScratchDirectory(testScratchDirectoryName);
		filename = testScratchDirectoryName + "file.nxs";
	}

	@Before
	public void deleteFile() {
		File handle = new File(filename);
		if (handle.isFile()) {
			if (!(handle.delete()))
				fail("Unable to delete file " + filename);
		}
	}

	/**
	 * @throws Exception 
	 */
	@SuppressWarnings("unused")
	@Test(expected=NullPointerException.class)
	public void testConstructor() throws Exception {
		new NexusFile(null, NexusFile.NXACC_READ);
	}

	/**
	 * @throws Exception 
	 */
	@Test(expected=NullPointerException.class)
	public void testMakeGroupNPE1() throws Exception {
		NexusFile file = null;
		try {
			file = new NexusFile(filename, NexusFile.NXACC_CREATE5);
			file.makegroup(null, "NXentry");
			file.opengroup("entry1", "NXentry");
			file.closegroup();
			}
		finally {
			if (file != null) file.close();
		}
	}

	/**
	 * @throws Exception 
	 */
	@Test(expected=NullPointerException.class)
	public void testMakeGroupNPE2() throws Exception {
		NexusFile file = null;
		try {
			file = new NexusFile(filename, NexusFile.NXACC_CREATE5);
			file.makegroup("entry1", null);
			file.opengroup("entry1", "NXentry");
			file.closegroup();
		}
		finally {
			if (file != null) file.close();
		}
	}

	/**
	 * @throws Exception 
	 */
	@Test(expected=NullPointerException.class)
	public void testOpenGroupNPE1() throws Exception {
		NexusFile file = null;
		try {
			file = new NexusFile(filename, NexusFile.NXACC_CREATE5);
			file.opengroup(null, "NXentry");
			file.closegroup();
		}
		finally {
			if (file != null) file.close();
		}
	}

	/**
	 * @throws Exception 
	 */
	@Test(expected=NullPointerException.class)
	public void testOpenGroupNPE2() throws Exception {
		NexusFile file = null;
		try {
			file = new NexusFile(filename, NexusFile.NXACC_CREATE5);
			file.opengroup("entry1", null);
			file.closegroup();
		}
		finally {
			if (file != null) file.close();
		}
	}

	/**
	 * @throws Exception 
	 */
	@Test(expected=NullPointerException.class)
	public void testMakeDataNPE1() throws Exception {
		NexusFile file = null;
		try {
			file = new NexusFile(filename, NexusFile.NXACC_CREATE5);
			file.makegroup("entry1", "NXentry");
			file.opengroup("entry1", "NXentry");
			file.makedata(null, NexusFile.NX_FLOAT64, 2, new int[] { 1 } );
			file.closegroup();
		}
		finally {
			if (file != null) file.close();
		}
	}

	/**
	 * @throws Exception 
	 */
	@Test(expected=NullPointerException.class)
	public void testMakeDataNPE2() throws Exception {
		NexusFile file = null;
		try {
			file = new NexusFile(filename, NexusFile.NXACC_CREATE5);
			file.makegroup("entry1", "NXentry");
			file.opengroup("entry1", "NXentry");
			file.makedata("data", NexusFile.NX_FLOAT64, 2, (long[]) null);
			file.closegroup();
		}
		finally {
			if (file != null) file.close();
		}
	}

	/**
	 * @throws Exception 
	 */
	@Test(expected=NullPointerException.class)
	public void testMultiDimPutSlab() throws Exception {
		//**// configure here if you like //**//
		// data dimension to write on each slab
		int[] dim = new int[] { 512, 512 };
		// number of slabs
		int numberOfPoints = 2;
		
			// setup data etc. according to configuration
			int rank = dim.length+1;
			int[] dimArray = new int[rank];
			dimArray[0] = NexusFile.NX_UNLIMITED;
			int size = 1;
			for (int i = 0; i < dim.length; i++) {
				dimArray[i+1] = dim[i];
				size *= dim[i];
			}
			double[] data = new double[size];
			int[] startPos = new int[rank];
			
			// create the file
			NexusFile file = null;
			try {
				file = new NexusFile(filename, NexusFile.NXACC_CREATE5);
				file.makegroup("entry1", "NXentry");
				file.opengroup("entry1", "NXentry");
				file.makedata(null, NexusFile.NX_FLOAT64, rank, dimArray);
				file.opendata("data");
	
				// write the slabs
				dimArray[0] = 1;
				for (int i = 0; i < numberOfPoints; i++) {
					startPos[0] = i;
					file.putslab(data, startPos, dimArray);
				}
	
				// wrap up
				file.closedata();
				file.closegroup();
				file.closegroup();
			}
			finally {
				if (file != null) file.close();
			}
	}

	/**
	 * @throws Exception 
	 */
	@Test(expected=NexusException.class)
	public void testFileNotThere() throws Exception {
			deleteFile();
			NexusFile file = new NexusFile(filename, NexusFile.NXACC_READ);
			file.opengroup("entry1", "NXentry");
			file.closegroup();
			file.close();
	}

	public static void main(String[] args) {
	     try {
	    	NexusFileStressTest.setUpBeforeClass();
			NexusFileStressTest nfsst = new NexusFileStressTest();
			nfsst.testFileNotThere();
			System.err.println("not good - no exception");
		} catch (NexusException e) {
			System.out.println("all good - expected behaviour");
		} catch (Exception e) {
			System.err.println("not so good - unexpected exception seen: "+e.getMessage());
		}
	}
}