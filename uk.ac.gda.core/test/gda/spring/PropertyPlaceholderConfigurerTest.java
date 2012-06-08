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

import java.io.File;

import org.springframework.context.support.FileSystemXmlApplicationContext;

import gda.util.TestUtils;
import junit.framework.TestCase;

public class PropertyPlaceholderConfigurerTest extends TestCase {

	static class Bean {
		
		String name;
		
		public void setName(String name) {
			this.name = name;
		}
		
	}
	
	public void testPropertyPlaceholderElementCanAppearAfterBeanDefinitionUsingPlaceholder() throws Exception {
		File f = TestUtils.getResourceAsFile(PropertyPlaceholderConfigurerTest.class, "placeholder-test-1.xml");
		FileSystemXmlApplicationContext x = new FileSystemXmlApplicationContext("file:" + f.getAbsolutePath());
		Bean b = (Bean) x.getBean("b");
		assertEquals("value", b.name);
	}
	
	public void testPropertyPlaceholderElementAffectsImportedBeanDefinitions() throws Exception {
		File f = TestUtils.getResourceAsFile(PropertyPlaceholderConfigurerTest.class, "placeholder-test-2a.xml");
		FileSystemXmlApplicationContext x = new FileSystemXmlApplicationContext("file:" + f.getAbsolutePath());
		Bean b = (Bean) x.getBean("b");
		assertEquals("value", b.name);
	}
	
	public void testImportedPropertyPlaceholderElementAffectsNonImportedBeanDefinitions() throws Exception {
		File f = TestUtils.getResourceAsFile(PropertyPlaceholderConfigurerTest.class, "placeholder-test-3a.xml");
		FileSystemXmlApplicationContext x = new FileSystemXmlApplicationContext("file:" + f.getAbsolutePath());
		Bean b = (Bean) x.getBean("b");
		assertEquals("value", b.name);
	}
	
}
