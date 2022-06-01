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

package uk.ac.gda.api.acquisition.parameters;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DevicePositionDocumentTest {

	private static final String MOTOR_X = "motor_x";

	@Test
	public void serialiseDevicePositionDocument() throws IOException {

		DevicePositionDocument.Builder builder = new DevicePositionDocument.Builder();
		builder.withDevice(MOTOR_X);
		builder.withAxis("X");
		builder.withPosition(2.0);

		String json = new ObjectMapper().writeValueAsString(builder.build());

		assertThat(json, containsString("\"device\":\"motor_x\""));
		assertThat(json, containsString("\"axis\":\"X\""));
		assertThat(json, containsString("\"position\":2.0"));
	}
}
