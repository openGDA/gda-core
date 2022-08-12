/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.spring;

import static java.util.Collections.emptySet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.PluggableSchemaResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;

import gda.util.TestUtils;
import uk.ac.diamond.daq.classloading.GDAClassLoaderService;

public class PropertyPlaceholderConfigurerTest {

	static class Bean {

		private String name;

		public void setName(String name) {
			this.name = name;
		}

	}

	private static final String VALUE = "value";

	private GenericApplicationContext getXMLSpringContext(String relativeFileName) throws FileNotFoundException {
		File file = TestUtils.getResourceAsFile(PropertyPlaceholderConfigurerTest.class, relativeFileName);
		GenericApplicationContext context = new GenericApplicationContext();
		ClassLoader cl = GDAClassLoaderService.getClassLoaderService()
				.getClassLoaderForLibraryWithGlobalResourceLoading(XmlBeanDefinitionReader.class, emptySet());
		XmlBeanDefinitionReader defReader = new XmlBeanDefinitionReader(context);
		defReader.setEntityResolver(new PluggableSchemaResolver(cl));
		defReader.setNamespaceHandlerResolver(new DefaultNamespaceHandlerResolver(cl));
		defReader.loadBeanDefinitions(file.toURI().toString());
		context.refresh();
		return context;
	}

	@Test
	public void testPropertyPlaceholderElementCanAppearAfterBeanDefinitionUsingPlaceholder()
			throws FileNotFoundException {
		try (var context = getXMLSpringContext("placeholder-test-1.xml")) {
			Bean b = (Bean) context.getBean("b");
			assertThat(b.name, is(VALUE));
		}
	}

	@Test
	public void testPropertyPlaceholderElementAffectsImportedBeanDefinitions() throws FileNotFoundException {
		try (var context = getXMLSpringContext("placeholder-test-2a.xml")) {
			Bean b = (Bean) context.getBean("b");
			assertThat(b.name, is(VALUE));
		}
	}

	@Test
	public void testImportedPropertyPlaceholderElementAffectsNonImportedBeanDefinitions() throws FileNotFoundException {
		try (var context = getXMLSpringContext("placeholder-test-3a.xml")) {
			Bean b = (Bean) context.getBean("b");
			assertThat(b.name, is(VALUE));
		}
	}

}
