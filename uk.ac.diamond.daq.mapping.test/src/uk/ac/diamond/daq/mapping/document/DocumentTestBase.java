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

import java.io.IOException;
import java.net.URL;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.diamond.daq.mapping.api.document.DocumentMapper;

class DocumentTestBase {
	private final ObjectMapper objectMapper = DocumentMapper.getObjectMapper();

	protected <T> T deserialiseDocument(String resourcePath, Class<T> clazz) {
		URL resource = TwoAxisGridPointsModelDocumentTest.class.getResource(resourcePath);
		try {
			return objectMapper.readValue(resource, clazz);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected String serialiseDocument(Object modelDocument) {
		try {
			return new ObjectMapper().writeValueAsString(modelDocument);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
