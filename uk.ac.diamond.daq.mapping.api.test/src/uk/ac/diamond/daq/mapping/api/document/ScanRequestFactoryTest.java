/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api.document;

import static gda.configuration.properties.LocalProperties.GDA_CONFIG;
import static gda.configuration.properties.LocalProperties.GDA_PROPERTIES_FILE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.scan.ScanningException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.gda.api.acquisition.configuration.processing.ApplyNexusTemplatesRequest;
import uk.ac.gda.common.exception.GDAException;
import uk.ac.gda.util.io.FileUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ScanRequestFactoryTestConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ScanRequestFactoryTest {

	@Autowired
	private DocumentMapper documentMapper;

	private IRunnableDeviceService runnableService = Mockito.mock(IRunnableDeviceService.class);

	@SuppressWarnings("unchecked")
	private IRunnableDevice<Object> detectorModel = Mockito.mock(IRunnableDevice.class);

	@Before
	public void before() throws ScanningException {
		when(runnableService.getRunnableDevice(anyString())).thenReturn(detectorModel);
	}

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(GDA_CONFIG, "test/resources/defaultContext");
        System.setProperty(GDA_PROPERTIES_FILE, "test/resources/defaultContext/properties/_common/common_instance_java.properties");
	}

	/**
	 * @throws Exception
	 */
	@Test(expected = ScanningException.class)
	public void testWrongDetectorModelFile() throws Exception {
		IDetectorModel model = Mockito.mock(IDetectorModel.class);
		when(detectorModel.getModel()).thenReturn(model);

		new ScanRequestFactory(loadScanningAcquisition())
			.createScanRequest(runnableService);
	}

	@Test
	public void testEmptyTemplateFile() throws Exception {
		IMalcolmModel model = Mockito.mock(IMalcolmModel.class);
		when(detectorModel.getModel()).thenReturn(model);

		ScanRequestFactory scanRequestFactory = new ScanRequestFactory(loadScanningAcquisition());
		ScanRequest scanRequest = scanRequestFactory.createScanRequest(runnableService);

		Assert.assertTrue(scanRequest.getTemplateFilePaths().isEmpty());
	}

	@Test
	public void testNotEmptyTemplateFile() throws Exception {
		IMalcolmModel model = Mockito.mock(IMalcolmModel.class);
		when(detectorModel.getModel()).thenReturn(model);

		ScanningAcquisition scanningAcquisition = loadScanningAcquisition();

		URL file1 = new URL("file:/lev1/lev2");
		URL file2 = new URL("file:/lev3/lev4");
		List<URL> paths = new ArrayList<>();
		paths.add(file1);
		paths.add(file2);
		ApplyNexusTemplatesRequest request = (new ApplyNexusTemplatesRequest.Builder())
				.withValue(paths)
				.build();
		scanningAcquisition.getAcquisitionConfiguration().setProcessingRequest(new ArrayList<>());
		scanningAcquisition.getAcquisitionConfiguration().getProcessingRequest().add(request);
		ScanRequestFactory scanRequestFactory = new ScanRequestFactory(scanningAcquisition);
		ScanRequest scanRequest = scanRequestFactory.createScanRequest(runnableService);

		Assert.assertEquals(2, scanRequest.getTemplateFilePaths().size());
	}


	private ScanningAcquisition loadScanningAcquisition() throws Exception {
		return deserialiseDocument("test/resources/scanningAcquisition.json",
				ScanningAcquisition.class);
	}

	protected <T> T deserialiseDocument(String resourcePath, Class<T> clazz) throws Exception {
		try {
			return documentMapper.convertFromJSON(FileUtils.readFile(new File(resourcePath)).toString(), clazz);
		} catch (MalformedURLException e) {
			throw new GDAException(e);
		}
	}
}
