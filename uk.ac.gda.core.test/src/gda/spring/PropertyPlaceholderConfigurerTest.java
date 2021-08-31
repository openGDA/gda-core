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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.FileNotFoundException;

import org.junit.Test;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import gda.util.TestUtils;

public class PropertyPlaceholderConfigurerTest {

	static class Bean {

		private String name;

		public void setName(String name) {
			this.name = name;
		}

	}

	private static final String VALUE = "value";

	private FileSystemXmlApplicationContext getXMLSpringContext(String relativeFileName) throws FileNotFoundException {
		var file = TestUtils.getResourceAsFile(PropertyPlaceholderConfigurerTest.class, relativeFileName);
		return new FileSystemXmlApplicationContext("file:" + file.getAbsolutePath());
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
