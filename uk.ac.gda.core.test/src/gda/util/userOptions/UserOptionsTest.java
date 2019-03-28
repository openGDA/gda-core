/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

/**
 *
 */
package gda.util.userOptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import gda.util.TestUtils;

public class UserOptionsTest {
	static final String TEMPLATE_NAME = "GDAUserOptionsTemplate";
	static final String OPTIONS_NAME = "GDAUserOptions";
	static final String TEMPLATE_TITLE = "USER_OPTIONS";

	/**
	 * Test method for {@link gda.util.userOptions}.
	 * @throws Exception
	 */
	@Test
	public void testLoadConfig() throws Exception {
		String testScratchDirectoryName = TestUtils.setUpTest(UserOptionsTest.class, "testLoadConfig", true);

		UserOptionsManager manager = new UserOptionsManager();
		manager.setTemplateConfigDir(testScratchDirectoryName);
		manager.setTemplateConfigName(TEMPLATE_NAME);
		manager.setConfigured(true);

		UserOptionsMap template = new UserOptionsMap();
		template.put("key1", new UserOption<String, Boolean>("Boolean", true));
		template.put("key2", new UserOption<String, Double>("Double", 1.0));
		template.put("key3", new UserOption<String, String>("String", "string_value"));
		template.put("key4", new UserOption<String, Integer>("Integer", 1234));
		template.put("key5", new UserOption<String, Boolean>("Boolean", true));
		template.setTitle(TEMPLATE_TITLE);

		manager.saveOptionsMapToTemplate(template);

		// Get defaults when specified source is empty
		UserOptionsMap options = manager.getOptionsMapFromConfig(testScratchDirectoryName, OPTIONS_NAME);
		assertEquals(options, template);

		// Alter a value and compare
		options.get("key1").setValue(false);
		assertNotEquals(options, template);

		// Save options map to (non-template) file
		manager.saveOptionsMapValuesToConfig(testScratchDirectoryName, OPTIONS_NAME, options);
		UserOptionsMap reread = manager.getOptionsMapFromConfig(testScratchDirectoryName, OPTIONS_NAME);
		assertEquals(reread, options);

		UserOptionsMap cleared = manager.resetOptions(testScratchDirectoryName, OPTIONS_NAME);
		assertEquals(cleared, template);
	}
}
