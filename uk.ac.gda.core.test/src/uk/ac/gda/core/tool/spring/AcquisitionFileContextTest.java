/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.core.tool.spring;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import org.eclipse.scanning.api.scan.IFilePathService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import gda.configuration.properties.LocalProperties;
import gda.data.ServiceHolder;
import uk.ac.gda.core.tool.spring.AcquisitionFileContext.ContextFile;


public class AcquisitionFileContextTest {

	private AcquisitionFileContext acquisitionFileContext = new AcquisitionFileContext();

	private IFilePathService filePathServiceMock;

	@Before
	public void before() {
		LocalProperties.clearProperty(AcquisitionFileContext.ACQUISITION_EXPERIMENT_DIRECTORY_PROPERTY);
		LocalProperties.clearProperty(AcquisitionFileContext.ACQUISITION_CALIBRATION_DIRECTORY_PROPERTY);
		LocalProperties.clearProperty(AcquisitionFileContext.ACQUISITION_CONFIGURATION_DIRECTORY_PROPERTY);
		filePathServiceMock = mock(IFilePathService.class);
	}

	/**
	 * If {@link IFilePathService#getProcessingDir()} is not absolute, cannot get the experiment directory
	 */
	@Test
	public void acquisitionExperimentDoesNotExistTest() {
		doReturn("dummyProcessingDir").when(filePathServiceMock).getProcessingDir();
		doReturn("dummyVisitDir").when(filePathServiceMock).getVisitConfigDir();
		ServiceHolder sh = new ServiceHolder();
		sh.setFilePathService(filePathServiceMock);

		URL url = acquisitionFileContext.getContextFile(ContextFile.ACQUISITION_EXPERIMENT_DIRECTORY);
		Assert.assertNull(url);
		url = acquisitionFileContext.getContextFile(ContextFile.ACQUISITION_CONFIGURATION_DIRECTORY);
		Assert.assertNull(url);
	}

	/**
	 * When no properties are specified the context uses their default value
	 *
	 * @throws IOException
	 */
	@Test
	public void acquisitionDefaultDirTest() throws IOException {
		prepareFilesystem();
		loadProperties("test/resources/gdaContext/defaultExperimentContext.properties");

		URL url = acquisitionFileContext.getContextFile(ContextFile.ACQUISITION_EXPERIMENT_DIRECTORY);
		Assert.assertNotNull(url);
		assertTrue(url.getPath().endsWith(AcquisitionFileContext.ACQUISITION_EXPERIMENT_DIRECTORY_PROPERTY_DEFAULT));

		url = acquisitionFileContext.getContextFile(ContextFile.ACQUISITION_CONFIGURATION_DIRECTORY);
		Assert.assertNotNull(url);
		assertTrue(url.getPath().endsWith(AcquisitionFileContext.ACQUISITION_CONFIGURATION_DIRECTORY_PROPERTY_DEFAULT));
	}

	/**
	 * When properties exist the context uses their value
	 *
	 * @throws IOException
	 */
	@Test
	public void acquisitionCustomExperimentTest() throws IOException {
		prepareFilesystem();
		loadProperties("test/resources/gdaContext/customExperimentContext.properties");

		URL url = acquisitionFileContext.getContextFile(ContextFile.ACQUISITION_EXPERIMENT_DIRECTORY);
		Assert.assertNotNull(url);
		assertTrue(url.getPath().endsWith("customExperiment"));

		url = acquisitionFileContext.getContextFile(ContextFile.ACQUISITION_CONFIGURATION_DIRECTORY);
		Assert.assertNotNull(url);
		assertTrue(url.getPath().endsWith("customConfiguration"));
	}

	/**
	 * If specified, AcquisitionFileContext.ACQUISITION_CALIBRATION_DIRECTORY_PERMISSIONS_PROPERTY
	 * sets the calibration directory permissions
	 *
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void changesPermissionToDirectoryTest() throws IOException, URISyntaxException {
		prepareFilesystem();
		loadProperties("test/resources/gdaContext/directoryWithPermissions.properties");

		URL url = acquisitionFileContext.getContextFile(ContextFile.DIFFRACTION_CALIBRATION_DIRECTORY);
		File file = new File(url.toURI());
		Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(file.toPath(), LinkOption.NOFOLLOW_LINKS);
		Assert.assertTrue(permissions.contains(PosixFilePermission.OWNER_READ));
		Assert.assertTrue(permissions.contains(PosixFilePermission.OWNER_EXECUTE));
		Assert.assertTrue(permissions.contains(PosixFilePermission.OWNER_WRITE));
		Assert.assertTrue(permissions.contains(PosixFilePermission.GROUP_READ));
		Assert.assertFalse(permissions.contains(PosixFilePermission.GROUP_WRITE));
		Assert.assertFalse(permissions.contains(PosixFilePermission.GROUP_EXECUTE));
		Assert.assertFalse(permissions.contains(PosixFilePermission.OTHERS_READ));
		Assert.assertFalse(permissions.contains(PosixFilePermission.OTHERS_WRITE));
		Assert.assertFalse(permissions.contains(PosixFilePermission.OTHERS_EXECUTE));
	}

	private void prepareFilesystem() throws IOException {
		Path testTmpDir = Files.createTempDirectory(AcquisitionFileContextTest.class.getName());
		File visitDir = new File(testTmpDir.toFile(), "visit");
		File processingDir = new File(visitDir, "processing");
		File xmlDir = new File(visitDir, "xml");
		File tmpDir = new File(visitDir, "tmp");


		doReturn(visitDir.getPath()).when(filePathServiceMock).getVisitDir();
		doReturn(processingDir.getPath()).when(filePathServiceMock).getProcessingDir();
		doReturn(xmlDir.getPath()).when(filePathServiceMock).getVisitConfigDir();
		doReturn(tmpDir.getPath()).when(filePathServiceMock).getTempDir();

		ServiceHolder sh = new ServiceHolder();
		sh.setFilePathService(filePathServiceMock);
	}



	private void loadProperties(String resourcePath) {
		File resource = new File(resourcePath);
		System.setProperty(LocalProperties.GDA_PROPERTIES_FILE, resource.getPath());
		LocalProperties.reloadAllProperties();
	}
}
