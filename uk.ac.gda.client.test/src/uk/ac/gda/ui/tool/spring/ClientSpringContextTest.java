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

package uk.ac.gda.ui.tool.spring;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gda.configuration.properties.LocalProperties;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ClientSpringContextTestConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ClientSpringContextTest {

	@Autowired
	private ClientSpringContext clientContext;

	/**
	 * If no endpoint is configured, uses the default one
	 */
	@Test
	public void restServiceEndpointDoesNotExistTest() {
		Assert.assertTrue(ClientSpringContext.REST_ENDPOINT_DEFAULT.equals(clientContext.getRestServiceEndpoint()));
	}

	/**
	 * Verify that retrieves the configured endpoint
	 */
	@Test
	public void restServiceEndpointDoesExistTest() {
		loadProperties("test/resources/clientContext/clientContextWithRestEndpoint.properties");
		Assert.assertFalse(ClientSpringContext.REST_ENDPOINT_DEFAULT.equals(clientContext.getRestServiceEndpoint()));
		Assert.assertTrue(clientContext.getRestServiceEndpoint().equals("http://xyz-control.diamond.ac.uk"));
	}

	private void loadProperties(String resourcePath) {
		File resource = new File(resourcePath);
		System.setProperty(LocalProperties.GDA_PROPERTIES_FILE, resource.getPath());
		LocalProperties.reloadAllProperties();
	}
}
