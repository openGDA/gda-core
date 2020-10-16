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

package uk.ac.gda.api.acquisition;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.gda.api.exception.GDAException;

public class AcquisitionTestUtils {
	public static final <T> T deserialiseDocument(String resourcePath, Class<T> clazz) throws GDAException {
		File resource = new File(resourcePath);
		try {
			return  new ObjectMapper().readValue(resource.toURI().toURL(), clazz);
		} catch (IOException e) {
			throw new GDAException(e);
		}
	}
}
