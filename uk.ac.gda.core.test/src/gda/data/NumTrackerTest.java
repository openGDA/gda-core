/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.data;

import gda.TestHelpers;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class NumTrackerTest {

	static String testDirName;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testDirName = TestHelpers.setUpTest(NumTrackerTest.class, "NumTrackerTest", true);
	}

	@Test
	public void testIncrementNumber() throws Exception {
		NumTracker tracker = new NumTracker("ext", testDirName);
		Assert.assertEquals(0, tracker.getCurrentFileNumber());
		for( int i=0;i<1000; i++){
			Assert.assertEquals(i+1, tracker.incrementNumber());
		}
		NumTracker tracker2 = new NumTracker("ext", testDirName);
		Assert.assertEquals(1000, tracker2.getCurrentFileNumber());
		
	}

	
}
