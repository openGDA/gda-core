/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.common.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.gda.common.entity.device.DeviceValue;

/**
 * Tests deserialization for {@link DeviceValue}
 *
 * @author Maurizio Nagni
 */
public class DeviceValueTest {

	@Test
	public void testDeviceValueAsNumber() throws JsonParseException, JsonMappingException, IOException {
		String json = "{\"documentType\": \"deviceValue\", \"value\":1, \"serviceName\": \"test\", \"property\": \"testProperty\", \"uuid\": \"a194ad2e-92f1-4fd5-b64f-0b36a5fe4dcf\" }";
		DeviceValue deviceValue = new ObjectMapper().readValue(json, DeviceValue.class);
		assertTrue(Number.class.isInstance(deviceValue.getValue()));
		assertTrue(Number.class.cast(deviceValue.getValue()).intValue() == 1);
		assertEquals("test", deviceValue.getServiceName());
		assertEquals("testProperty", deviceValue.getProperty());
		assertEquals(UUID.fromString("a194ad2e-92f1-4fd5-b64f-0b36a5fe4dcf"), deviceValue.getUuid());
	}

	@Test
	public void testDeviceValueAsArray() throws JsonParseException, JsonMappingException, IOException {
		String json = "{\"documentType\": \"deviceValue\", \"value\":[1,2,3], \"serviceName\": \"test\", \"property\": \"testProperty\", \"uuid\": \"a194ad2e-92f1-4fd5-b64f-0b36a5fe4dcf\" }";
		DeviceValue deviceValue = new ObjectMapper().readValue(json, DeviceValue.class);
		assertTrue(List.class.isInstance(deviceValue.getValue()));
		ArrayList arrayValue = ArrayList.class.cast(deviceValue.getValue());
		assertEquals(3, arrayValue.size());
		assertTrue(Number.class.isInstance(arrayValue.get(0)));
		assertTrue(Number.class.cast(arrayValue.get(0)).intValue() == 1);
		assertEquals("test", deviceValue.getServiceName());
		assertEquals("testProperty", deviceValue.getProperty());
		assertEquals(UUID.fromString("a194ad2e-92f1-4fd5-b64f-0b36a5fe4dcf"), deviceValue.getUuid());
	}
}
