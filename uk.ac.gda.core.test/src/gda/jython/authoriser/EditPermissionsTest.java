/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.jython.authoriser;

import static org.junit.Assert.fail;
import gda.util.TestUtils;
import gda.configuration.properties.LocalProperties;
import gda.util.exceptionUtils;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * TODO
 * This should not form part of the unit test suite as it produces a GUI. This GUI should be tested when such procedures
 * are in place.
 */
public class EditPermissionsTest {
	private static String testScratchDirectoryName = null;
	private static FileAuthoriser fileAuthoriser;
	private static EditPermissions inst;

	/**
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUp() throws Exception {
		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(EditPermissions.class
				.getCanonicalName());
		try {
			TestUtils.makeScratchDirectory(testScratchDirectoryName + "xml");
		} catch (Exception e) {
			fail(exceptionUtils.getFullStackMsg(e));
		}
		System.setProperty(LocalProperties.GDA_CONFIG, testScratchDirectoryName);
		LocalProperties.set(Authoriser.AUTHORISERCLASS_PROPERTY, "gda.jython.authoriser.FileAuthoriser");
		fileAuthoriser = (FileAuthoriser) AuthoriserProvider.getAuthoriser();
		fileAuthoriser.addEntry("i02user", 5, false);
		fileAuthoriser.addEntry("rjw82", 5, true);
		LocalProperties.set(FileAuthoriser.DEFAULTLEVELPROPERTY, "3");

		inst = new EditPermissions();
		inst.setLocationRelativeTo(null);
		inst.setVisible(true);
	}

	/**
	 * 
	 */
	@Test
	public void testCanSee() {
		while (inst.isVisible()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}

}
