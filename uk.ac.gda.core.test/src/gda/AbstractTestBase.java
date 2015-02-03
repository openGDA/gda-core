/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base test to do common stuff
 *
 * @author Simon Berriman
 */
public abstract class AbstractTest {

	/** Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTest.class);

	@Rule
	public TestName testName = new TestName();

	@Before
	public void setUp() {
		LOGGER.info("Starting test '" + testName.getMethodName() + "'");
	}

	@After
	public void tearDown() {
		LOGGER.info("End of test '" + testName.getMethodName() + "'\n");
	}
}
