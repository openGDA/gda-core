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

import static org.mockito.Mockito.verify;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.service.core.ConfigurationsServiceCore;
import uk.ac.diamond.daq.service.core.ConfigurationsServiceCoreTest;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;

/**
 * In this test we simply verify that the service API calls the correct methods
 * of {@link ConfigurationsServiceCoreTest}, the logic of which is tested elsewhere
 */
public class ConfigurationsRestServiceTest {

	private ConfigurationsRestService service;

	@Mock
	private ConfigurationsServiceCore core;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Rule
	public MockitoRule initialiseMocks = MockitoJUnit.rule();

	@Before
	public void injectServiceCore() {
		service = new ConfigurationsRestService();
		ReflectionTestUtils.setField(service, "serviceCore", core);
	}

	@Test (expected = ResponseStatusException.class)
	public void invalidIDTest() throws Exception {
		service.getDocument("id", null, null);
	}

	@Test
	public void getDocument() {
		var uuid = UUID.randomUUID();
		service.getDocument(uuid.toString(), request, response);
		verify(core).selectDocument(uuid, request, response);
	}

	@Test
	public void getDocuments() {
		service.getDocuments(request, response);
		verify(core).selectDocuments(request, response);
	}

	@Test
	public void deleteDocument() {
		var uuid = UUID.randomUUID();
		service.deleteDocument(uuid.toString(), request, response);
		verify(core).deleteDocument(uuid, request, response);
	}

	@Test
	public void saveDiffraction() {
		var scan = new ScanningAcquisition();
		service.insertDiffraction(scan, request, response);
		verify(core).insertDocument(scan, AcquisitionConfigurationResourceType.MAP, request, response);
	}

	@Test
	public void saveTomography() {
		var scan = new ScanningAcquisition();
		service.insertTomography(scan, request, response);
		verify(core).insertDocument(scan, AcquisitionConfigurationResourceType.TOMO, request, response);
	}

}
