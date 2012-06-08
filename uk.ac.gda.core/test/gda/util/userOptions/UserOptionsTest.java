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
import gda.TestHelpers;

import org.junit.Test;

/**
 * 
 */
public class UserOptionsTest {
	final static String TestFileFolder = "test/gda/util/userOptions";

	/**
	 * Test method for {@link gda.util.userOptions}.
	 * @throws Exception 
	 */
	@Test
	public void testLoadConfig() throws Exception {
		String testScratchDirectoryName = TestHelpers.setUpTest(UserOptionsTest.class, "testLoadConfig", true);
		
		UserOptions options = new UserOptions();
		options.title = "Test options";
		options.put("key1", new UserOption<String, Boolean>("Boolean", true));
		options.put("key2", new UserOption<String, Double>("Double", 1.0));
		options.put("key3", new UserOption<String, String>("String", "String"));
		options.put("key3", new UserOption<String, Integer>("Integer", 1234));
		options.put("key5", new UserOption<String, Boolean>("Boolean", true));
		options.saveToTemplate(testScratchDirectoryName, "testOptionsConfig");
		options.get("key1").value = false;
		options.saveValuesToConfig(testScratchDirectoryName, "testOptions");
		UserOptions newOptions = UserOptions.createFromTemplate(testScratchDirectoryName, "testOptionsConfig");
		newOptions.setValuesFromConfig(testScratchDirectoryName, "testOptions");
		assertEquals(options, newOptions);
	}
}
