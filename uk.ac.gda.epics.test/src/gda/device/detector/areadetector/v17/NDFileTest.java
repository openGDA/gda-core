/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.areadetector.v17;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import gda.device.detector.areadetector.v17.impl.NDFileImpl;
import gda.device.detector.areadetector.v17.impl.NDPluginBaseImpl;
import gda.util.TestUtils;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author rsr31645
 */
@Ignore("2011/08/04 Test ignored since it requires resources that are not generally available")
public class NDFileTest {

	private static final String EXCALIBUR_TIFF = "EXCALIBUR:TIFF:";
	private NDFileImpl file;
	private NDPluginBaseImpl pluginBase;

	/**
	 * Skip entire test class if PV used for testing is not reachable from this machine
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
		String hostname;
		try {
			InetAddress addr = InetAddress.getLocalHost();
			hostname = addr.getHostName();
		} catch (UnknownHostException e) {
			return;
		}
		if (hostname.startsWith("p99-ws100") || hostname.startsWith("ws018")) {
			TestUtils.skipTest("NDFileTest requires access to PV " + EXCALIBUR_TIFF
					+ ", which does not appear to be accessible from this machine (" + hostname + ")");
		}
	}

	/**
	 * 
	 */
	@Before
	public void setUp() throws Exception {
		pluginBase = new NDPluginBaseImpl();
		pluginBase.setBasePVName(EXCALIBUR_TIFF);
		pluginBase.setNDArrayPort("det.sim");
		pluginBase.setNDArrayAddress(0);
		file = new NDFileImpl();
		file.setBasePVName(EXCALIBUR_TIFF);
		file.setPluginBase(pluginBase);

	}

	@Test
	public void testPluginBase() {
		assertNotNull(pluginBase);
	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.v17.impl.NDFileImpl#setFilePath(java.lang.String)}.
	 */
	@Test
	public void testSetFilePath() throws Exception {
		file.setFilePath("/scratch/file/");
		assertEquals("/scratch/file/", file.getFilePath());
		assertEquals("/scratch/file/", file.getFilePath_RBV());

	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.v17.impl.NDFileImpl#setFileName(java.lang.String)}.
	 */
	@Test
	public void testSetFileName() throws Exception {
		file.setFileName("/scratch/file");
		assertEquals("/scratch/file", file.getFileName());
		assertEquals("/scratch/file", file.getFileName_RBV());

	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.v17.impl.NDFileImpl#setFileNumber(int)}.
	 */
	@Test
	public void testSetFileNumber() throws Exception {
		file.setFileNumber(3);
		assertEquals(3, file.getFileNumber());
		assertEquals(3, file.getFileNumber_RBV());
	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.v17.impl.NDFileImpl#setAutoIncrement(int)}.
	 */
	@Test
	public void testSetAutoIncrement() throws Exception {
		file.setAutoIncrement((short) 1);
		assertEquals(1, file.getAutoIncrement());
		assertEquals(1, file.getAutoIncrement_RBV());
	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.v17.impl.NDFileImpl#setFileTemplate(java.lang.String)}.
	 */
	@Test
	public void testSetFileTemplate() throws Exception {
		file.setFileTemplate("Filetemplate");
		assertEquals("Filetemplate", file.getFileTemplate());
		assertEquals("Filetemplate", file.getFileTemplate_RBV());

	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.v17.impl.NDFileImpl#setAutoSave(int)}.
	 */
	@Test
	public void testSetAutoSave() throws Exception {
		file.setAutoSave((short) 1);
		assertEquals(1, file.getAutoSave());
		assertEquals(1, file.getAutoSave_RBV());

	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.v17.impl.NDFileImpl#setWriteFile(int)}.
	 */
	@Test
	public void testSetWriteFile() throws Exception {
		file.setWriteFile((short) 0);
		assertEquals(0, file.getWriteFile());
		assertEquals(0, file.getWriteFile_RBV());
	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.v17.impl.NDFileImpl#setReadFile(int)}.
	 */
	@Test
	public void testSetReadFile() throws Exception {
		file.setReadFile((short) 0);
		assertEquals(0, file.getReadFile());
		assertEquals(0, file.getReadFile_RBV());

	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.v17.impl.NDFileImpl#setFileFormat(int)}.
	 */
	@Test
	public void testSetFileFormat() throws Exception {
		file.setFileFormat((short) 1);
		assertEquals(1, file.getFileFormat());
		assertEquals(1, file.getFileFormat_RBV());
	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.v17.impl.NDFileImpl#setFileWriteMode(int)}.
	 */
	@Test
	public void testSetFileWriteMode() throws Exception {
		file.setFileWriteMode((short) 1);
		assertEquals(1, file.getFileWriteMode());
		assertEquals(1, file.getFileWriteMode_RBV());
	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.v17.impl.NDFileImpl#startCapture()}.
	 */
	@Test
	public void testStartCapture() throws Exception {
		file.startCapture();
		assertEquals(1, file.getCapture());
		assertEquals(1, file.getCapture_RBV());
	}

	/**
	 * Test method for {@link gda.device.detector.areadetector.v17.impl.NDFileImpl#setNumCapture(int)}.
	 */
	@Test
	public void testSetNumCapture() throws Exception {
		file.setNumCapture(54);
		assertEquals(54, file.getNumCapture());
		assertEquals(54, file.getNumCapture_RBV());
	}

}
