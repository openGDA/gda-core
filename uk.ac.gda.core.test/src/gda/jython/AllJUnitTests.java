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

package gda.jython;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Run all JUnit tests in gda*
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	gda.jython.accesscontrol.DeviceInterceptorTest.class,
	gda.jython.authenticator.JaasAuthenticatorTest.class,
	gda.jython.authoriser.FileAuthoriserTest.class,
	gda.jython.batoncontrol.BatonManagerTest.class,
	gda.jython.ScriptPathsTest.class,
	gda.jython.GDAJythonClassloaderTest.class,
	gda.jython.logger.BatonChangedLoggerAdapterTest.class,
	gda.jython.logger.ScanDataPointLoggerAdapterTest.class,
	gda.jython.logger.RedirectableFileLoggerTest.class,
	gda.jython.logger.OutputTerminalLoggerAdapterTest.class
//	gda.jython.socket.CommandSocketTest.class FIXME re-enable when tests are fixed
})

public class AllJUnitTests {
}