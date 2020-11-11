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

package uk.ac.diamond.daq.mapping.document;

import java.io.File;
import java.net.MalformedURLException;

import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import uk.ac.diamond.daq.mapping.api.document.DocumentMapper;
import uk.ac.gda.api.exception.GDAException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { DocumentTestBaseConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class DocumentTestBase {

	protected <T> T deserialiseDocument(String resourcePath, Class<T> clazz) throws GDAException {
		File resource = new File(resourcePath);
		try {
			return DocumentMapper.fromJSON(resource.toURI().toURL(), clazz);
		} catch (MalformedURLException e) {
			throw new GDAException(e);
		}
	}

	protected String serialiseDocument(Object modelDocument) throws GDAException {
		return DocumentMapper.toJSON(modelDocument);
	}
}
