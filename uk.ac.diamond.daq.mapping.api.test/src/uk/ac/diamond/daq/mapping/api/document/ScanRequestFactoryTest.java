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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.scan.ScanningException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.gda.api.acquisition.AcquisitionEngineDocument;
import uk.ac.gda.api.acquisition.AcquisitionEngineDocument.AcquisitionEngineType;
import uk.ac.gda.api.acquisition.configuration.processing.ApplyNexusTemplatesRequest;
import uk.ac.gda.client.properties.acquisition.AcquisitionTypeProperties;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ScanRequestFactoryTestConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ScanRequestFactoryTest {

	private IRunnableDeviceService runnableService = Mockito.mock(IRunnableDeviceService.class);

	@SuppressWarnings("unchecked")
	private IRunnableDevice<Object> detectorModel = Mockito.mock(IRunnableDevice.class);

	private IDetectorModel model = Mockito.mock(IDetectorModel.class);

	@Before
	public void before() throws ScanningException {
		when(runnableService.getRunnableDevice(anyString())).thenReturn(detectorModel);
		when(detectorModel.getModel()).thenReturn(model);
	}

	@Test
	public void testEmptyTemplateFile() throws Exception {
		ScanRequestFactory scanRequestFactory = new ScanRequestFactory(newScanningAcquisition());
		ScanRequest scanRequest = scanRequestFactory.createScanRequest(runnableService);

		Assert.assertTrue(scanRequest.getTemplateFilePaths().isEmpty());
	}

	@Test
	public void testNotEmptyTemplateFile() throws Exception {
		ScanningAcquisition scanningAcquisition = newScanningAcquisition();

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


	private ScanningAcquisition newScanningAcquisition() {
			ScanningAcquisition newConfiguration = new ScanningAcquisition();
			newConfiguration.setUuid(UUID.randomUUID());
			ScanningConfiguration configuration = new ScanningConfiguration();
			newConfiguration.setAcquisitionConfiguration(configuration);

			AcquisitionEngineDocument acquisitionEngineDocument = new AcquisitionEngineDocument();
			acquisitionEngineDocument.setType(AcquisitionEngineType.MALCOLM);
			acquisitionEngineDocument.setId("testMalcolm");

			newConfiguration.setAcquisitionEngine(acquisitionEngineDocument);

			ScanningParameters acquisitionParameters = new ScanningParameters();
			String acquisitionType = "diffraction";
			ScanpathDocument.Builder scanpathBuilder =
					AcquisitionTypeProperties.getAcquisitionProperties(acquisitionType)
					.buildScanpathBuilder(AcquisitionTemplateType.ONE_DIMENSION_LINE);
			List<ScannableTrackDocument> scanableTracks = new ArrayList<>();
			ScannableTrackDocument scannableTrackDocument = (new ScannableTrackDocument.Builder())
			.withAxis("x")
			.withPoints(10)
			.withStart(0d)
			.withStop(20)
			.build();
			scanableTracks.add(scannableTrackDocument);
			scanpathBuilder.withScannableTrackDocuments(scanableTracks);
			acquisitionParameters.setScanpathDocument(scanpathBuilder.build());

			newConfiguration.getAcquisitionConfiguration().setAcquisitionParameters(acquisitionParameters);

			configuration.setProcessingRequest(null);

			return newConfiguration;
	}
}
