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

package uk.ac.diamond.daq.service.context;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.scanning.api.scan.IFilePathService;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gda.data.ServiceHolder;
import uk.ac.gda.core.tool.spring.AcquisitionFileContext;
import uk.ac.gda.test.helpers.ClassLoaderInitializer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AcquisitionFileContextTestConfiguration.class }, initializers = {ClassLoaderInitializer.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
abstract class AcquisitionFileContextTestBase {

	@Autowired
	private AcquisitionFileContext acquisitionFileContext;

	private IFilePathService filePathServiceMock;

	@Before
	public void before() {
		filePathServiceMock = mock(IFilePathService.class);
	}

	protected void prepareFilesystem() throws IOException {
		Path testTmpDir = Files.createTempDirectory(AcquisitionFileContextTestBase.class.getName());
		testTmpDir.toFile().deleteOnExit();
		var visitDir = new File(testTmpDir.toFile(), "visit");
		var processingDir = new File(visitDir, "processing");
		var xmlDir = new File(visitDir, "xml");
		var tmpDir = new File(visitDir, "tmp");


		doReturn(visitDir.getPath()).when(filePathServiceMock).getVisitDir();
		doReturn(processingDir.getPath()).when(filePathServiceMock).getProcessingDir();
		doReturn(xmlDir.getPath()).when(filePathServiceMock).getVisitConfigDir();
		doReturn(tmpDir.getPath()).when(filePathServiceMock).getTempDir();

		var sh = new ServiceHolder();
		sh.setFilePathService(filePathServiceMock);
	}

	protected AcquisitionFileContext getAcquisitionFileContext() {
		return acquisitionFileContext;
	}

	protected IFilePathService getFilePathServiceMock() {
		return filePathServiceMock;
	}
}
