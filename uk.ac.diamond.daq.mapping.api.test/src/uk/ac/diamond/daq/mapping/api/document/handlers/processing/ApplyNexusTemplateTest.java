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

package uk.ac.diamond.daq.mapping.api.document.handlers.processing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.scanning.api.event.scan.ProcessingRequest;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.diamond.daq.mapping.api.document.DocumentMapper;
import uk.ac.gda.api.acquisition.configuration.processing.ApplyNexusTemplatesRequest;
import uk.ac.gda.common.exception.GDAException;

/**
 * @author Maurizio Nagni
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ProcessingRequestHandlerServiceTestConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ApplyNexusTemplateTest {

	@Autowired
	private DocumentMapper documentMapper;

	private String pathFile1 = "file:/lev1/lev2";
	private String pathFile2 = "file:/lev3/lev4";
	private URL file1;
	private URL file2;

	@Before
	public void before() throws Exception {
		file1 = new URL(pathFile1);
		file2 = new URL(pathFile2);
	}

	@Test
	public void testApplyNexusTemplateRequest() throws Exception {
		ProcessingRequestHandler handler = new ApplyNexusTemplateHandler();
		List<URL> paths = new ArrayList<>();
		paths.add(file1);
		paths.add(file2);
		ApplyNexusTemplatesRequest request = new ApplyNexusTemplatesRequest.Builder()
			.withValue(paths)
			.build();

		ScanRequest scanRequest = new ScanRequest();
		scanRequest.setProcessingRequest(new ProcessingRequest());
		scanRequest.getProcessingRequest().setRequest(new HashMap<>());
		handler.handle(request, scanRequest);



		Set<String> translated = scanRequest.getTemplateFilePaths();
		Assert.assertEquals(2, translated.size());
		Assert.assertTrue(translated.contains(file1.getPath()));
		Assert.assertTrue(translated.contains(file2.getPath()));
	}

	@Test
	public void testDeserialization() throws  GDAException {
		String json = String.format("{\"type\":\"%s\", \"value\": [\"%s\", \"%s\"]}", "applyNexusTemplates", pathFile1, pathFile2);

		ApplyNexusTemplatesRequest request = documentMapper.convertFromJSON(json, ApplyNexusTemplatesRequest.class);
		assertEquals("applyNexusTemplates", request.getKey());
		Assert.assertEquals(2, request.getValue().size());
		Assert.assertEquals(request.getValue().get(0), file1);
		Assert.assertEquals(request.getValue().get(1), file2);
	}

	@Test
	public void testSeerialization() throws  GDAException {
		List<URL> paths = new ArrayList<>();
		paths.add(file1);
		paths.add(file2);
		ApplyNexusTemplatesRequest request = new ApplyNexusTemplatesRequest.Builder()
			.withValue(paths)
			.build();

		String json = documentMapper.convertToJSON(request);

		assertTrue(json.contains("applyNexusTemplates"));
		assertTrue(json.contains(pathFile1));
		assertTrue(json.contains(pathFile2));
		assertTrue(json.contains("type"));
		assertTrue(json.contains("value"));
	}
}
