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

import static org.mockito.Mockito.doReturn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.DelegatingServletOutputStream;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.api.document.DocumentMapper;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.service.rest.exception.GDAHttpException;
import uk.ac.gda.common.entity.Document;
import uk.ac.gda.common.exception.GDAException;

/**
 *
 * @author Maurizio Nagni
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ConfigurationsServiceTestConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ConfigurationsServiceTest {

	@Autowired
	private ConfigurationsService service;

	@Autowired
	private DocumentMapper documentMapper;

	@BeforeClass
	public static void beforeClass() {
		System.setProperty(LocalProperties.GDA_VISIT_DIR, "test/resources");
	}

	@Test(expected = GDAHttpException.class)
	public void invalidIDTest() throws GDAHttpException {
		service.getDocument("id", null, null);
	}

	@Test
	public void getDocumentTest() throws GDAHttpException, IOException  {
		String uuid = "a194ad2e-92f1-4fd5-b64f-0b36a5fe4dcf";
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ServletOutputStream os = new DelegatingServletOutputStream(stream);
		doReturn(os).when(response).getOutputStream();
		service.getDocument(uuid, null, response);
		Assert.assertTrue(stream.toString().contains(uuid));
	}

	@Test
	public void getDocumentsIDTest() throws GDAHttpException, IOException {
		String uuid2 = "015748eb-4be4-4d0f-a509-f989ec3a0453";
		String uuid3 = "ab8d1bb0-f43f-4ce9-abfd-9b79778a7607";

		Map<String, String[]> parameterMap = new HashMap<String, String[]>();
		parameterMap.put("fileExtension", new String[]{"tomo"});
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		doReturn(parameterMap).when(request).getParameterMap();

		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ServletOutputStream os = new DelegatingServletOutputStream(stream);
		doReturn(os).when(response).getOutputStream();

		service.getDocuments(request, response);
		Assert.assertTrue(stream.toString().contains(uuid2));
		Assert.assertTrue(stream.toString().contains(uuid3));
	}

	@Test
	public void getSaveTest() throws IOException, GDAException {
		ScanningAcquisition scanningAcquisition = new ScanningAcquisition();
		scanningAcquisition.setName("saveTest" + UUID.randomUUID().toString());

		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		ServletOutputStream os = new DelegatingServletOutputStream(stream);
		doReturn(os).when(response).getOutputStream();

		service.insertDiffraction(scanningAcquisition, request, response);

		Document newDocument = documentMapper.convertFromJSON(stream.toString(), Document.class);
		Assert.assertNotNull(newDocument.getUuid());
	}
}
