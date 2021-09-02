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

package uk.ac.diamond.daq.service.rest;

import static gda.configuration.properties.LocalProperties.GDA_CONFIG;
import static gda.configuration.properties.LocalProperties.GDA_PROPERTIES_FILE;
import static gda.configuration.properties.LocalProperties.GDA_VISIT_DIR;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.DelegatingServletOutputStream;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.diamond.daq.mapping.api.document.DocumentMapper;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.gda.common.entity.Document;
import uk.ac.gda.common.exception.GDAException;
import uk.ac.gda.core.tool.GDAHttpException;

/**
 *
 * @author Maurizio Nagni
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ConfigurationsServiceTestConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ConfigurationsServiceTest {

	@Autowired
	private ConfigurationsRestService configurationsService;

	@Autowired
	private DocumentMapper documentMapper;

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(GDA_VISIT_DIR, "test/resources");
		System.setProperty(GDA_CONFIG, "test/resources/configurationsService");
        System.setProperty(GDA_PROPERTIES_FILE, "test/resources/configurationsService/properties/_common/common_instance_java.properties");
	}

	@Test(expected = GDAHttpException.class)
	@Ignore
	public void invalidIDTest() throws GDAHttpException {
		configurationsService.getDocument("id", null, null);
	}

	@Test
	@Ignore
	public void getDocumentTest() throws GDAHttpException, IOException  {
		var uuid = "a194ad2e-92f1-4fd5-b64f-0b36a5fe4dcf";
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		var stream = new ByteArrayOutputStream();
		ServletOutputStream os = new DelegatingServletOutputStream(stream);
		doReturn(os).when(response).getOutputStream();
		configurationsService.getDocument(uuid, null, response);
		assertTrue(stream.toString().contains(uuid));
	}

	@Test
	@Ignore
	public void getDocumentsIDTest() throws GDAHttpException, IOException {
		var uuid2 = "015748eb-4be4-4d0f-a509-f989ec3a0453";
		var uuid3 = "ab8d1bb0-f43f-4ce9-abfd-9b79778a7607";

		Map<String, String[]> parameterMap = new HashMap<>();
		parameterMap.put("fileExtension", new String[]{"tomo"});
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		doReturn(parameterMap).when(request).getParameterMap();

		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		var stream = new ByteArrayOutputStream();
		ServletOutputStream os = new DelegatingServletOutputStream(stream);
		doReturn(os).when(response).getOutputStream();

		configurationsService.getDocuments(request, response);
		assertTrue(stream.toString().contains(uuid2));
		assertTrue(stream.toString().contains(uuid3));
	}

	@Test
	@Ignore
	public void getSaveTest() throws IOException, GDAException {
		var scanningAcquisition = new ScanningAcquisition();
		scanningAcquisition.setName("saveTest" + UUID.randomUUID().toString());

		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		var stream = new ByteArrayOutputStream();
		ServletOutputStream os = new DelegatingServletOutputStream(stream);
		doReturn(os).when(response).getOutputStream();

		configurationsService.insertDiffraction(scanningAcquisition, request, response);

		var newDocument = documentMapper.convertFromJSON(stream.toString(), Document.class);
		assertNotNull(newDocument.getUuid());
	}
}
