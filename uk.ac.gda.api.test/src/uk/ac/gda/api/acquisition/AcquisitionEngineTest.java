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

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.gda.api.acquisition.AcquisitionEngineDocument.AcquisitionEngineType;
import uk.ac.gda.common.exception.GDAException;

public class AcquisitionEngineTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void createAcquisitionEngineTest(){
		AcquisitionEngineDocument.Builder builder = new AcquisitionEngineDocument.Builder();
		builder.withId("test");
		builder.withType(AcquisitionEngineType.MALCOLM);
		AcquisitionEngineDocument engine = builder.build();

		Assert.assertEquals("test", engine.getId());
		Assert.assertEquals(AcquisitionEngineType.MALCOLM, engine.getType());
	}

	@Test
	public void serializeAcquisitionEngineTest() throws GDAException{
		AcquisitionEngineDocument.Builder builder = new AcquisitionEngineDocument.Builder();
		builder.withId("test");
		builder.withType(AcquisitionEngineType.MALCOLM);
		AcquisitionEngineDocument engine = builder.build();

		String document;
		try {
			document = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(engine);
		} catch (JsonProcessingException e) {
			throw new GDAException("Cannot create json document", e);
		}
		assertThat(document, containsString("\"id\" : \"test\""));
		assertThat(document, containsString("\"type\" : \"MALCOLM\""));
	}

	@Test
	public void deserializeAcquisitionEngineTest() throws GDAException{
		URL resource = AcquisitionEngineTest.class.getResource("/resources/AcquisitionEngineTest.json");
		AcquisitionEngineDocument engine;

		try {
			engine = objectMapper.readValue(resource, AcquisitionEngineDocument.class);
		} catch (JsonProcessingException e) {
			throw new GDAException("Cannot create json document", e);
		} catch (IOException e) {
			throw new GDAException("Cannot read json document", e);
		}
		Assert.assertEquals("test", engine.getId());
		Assert.assertEquals(AcquisitionEngineType.MALCOLM, engine.getType());
	}
}
