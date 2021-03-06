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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.eclipse.scanning.api.event.scan.ProcessingRequest;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.junit.Assert;
import org.junit.Test;

import uk.ac.gda.api.acquisition.configuration.processing.SavuProcessingRequest;

/**
 * Test the {@link SavuProcessingRequestHandler}
 *
 * @author Maurizio Nagni
 */
public class SavuProcessingRequestHandlerTest {


	@Test
	public void testSavuProcessingRequest() throws Exception {
		ProcessingRequestHandler handler = new SavuProcessingRequestHandler();
		URL file1 = new URL("file:/lev1/lev2");
		URL file2 = new URL("file:/lev3/lev4");
		List<URL> paths = new ArrayList<>();
		paths.add(file1);
		paths.add(file2);
		SavuProcessingRequest request = new SavuProcessingRequest.Builder()
			.withValue(paths)
			.build();

		ScanRequest scanRequest = new ScanRequest();
		scanRequest.setProcessingRequest(new ProcessingRequest());
		scanRequest.getProcessingRequest().setRequest(new HashMap<>());
		handler.handle(request, scanRequest);

		Collection<Object> translated = scanRequest.getProcessingRequest().getRequest().get(request.getKey());
		Assert.assertEquals(2, translated.size());
		Assert.assertTrue(translated.contains(file1.getPath()));
		Assert.assertTrue(translated.contains(file2.getPath()));
	}

}
