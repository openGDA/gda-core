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
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.gda.api.acquisition.configuration.processing.SavuProcessingRequest;

public class SavuProcessingRequestHandlerTest {


	@Test
	public void testSavuProcessingRequest() throws Exception {
		ProcessingRequestHandler handler = new SavuProcessingRequestHandler();
		String file1 = "file://lev1/lev2";
		String file2 = "file://lev3/lev4";
		List<URL> paths = new ArrayList<>();
		paths.add(new URL(file1));
		paths.add(new URL(file2));
		SavuProcessingRequest request = new SavuProcessingRequest.Builder()
			.withValue(paths)
			.build();

		Collection<Object> translated = handler.translateToCollection(request);
		Assert.assertEquals(2, translated.size());
		Assert.assertTrue(translated.contains(file1));
		Assert.assertTrue(translated.contains(file2));
	}

}
