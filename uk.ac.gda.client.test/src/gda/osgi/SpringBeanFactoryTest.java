/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.osgi;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.junit.Before;
import org.junit.Test;

public class SpringBeanFactoryTest {
	
	private IConfigurationElement configElement;
	
	@Before
	public void createConfigElement() {
		final IContributor contributor = mock(IContributor.class);
		when(contributor.getName()).thenReturn("uk.ac.gda.client.test");
		
		configElement = mock(IConfigurationElement.class);
		when(configElement.getContributor()).thenReturn(contributor);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void callSetWithInvalidPropertyName() throws Exception {
		SpringBeanFactory factory = new SpringBeanFactory();
		factory.setInitializationData(configElement, "xyz", null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void callSetWithNoData() throws Exception {
		SpringBeanFactory factory = new SpringBeanFactory();
		factory.setInitializationData(configElement, "class", null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void callSetWithDataOfWrongType() throws Exception {
		SpringBeanFactory factory = new SpringBeanFactory();
		factory.setInitializationData(configElement, "class", 123);
	}
	
	@Test
	public void callSetWithValidData() throws Exception {
		SpringBeanFactory factory = new SpringBeanFactory();
		factory.setInitializationData(configElement, "class", "beanName");
	}
	
}
