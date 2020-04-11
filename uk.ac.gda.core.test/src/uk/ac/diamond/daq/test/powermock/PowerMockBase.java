/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.test.powermock;

import org.powermock.core.classloader.ClassPathAdjuster;
import org.powermock.core.classloader.MockClassLoader;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.UseClassPathAdjuster;
import org.python.core.Py;

import gda.jython.server.shell.JythonShellParserTest;
import javassist.ClassPool;
import javassist.LoaderClassPath;

/**
 * This should be extended by all classes using PowerMock within
 * this plugin: {@code uk.ac.gda.core.test}.
 * <p>
 * Provides a classpath modification to reconcile Tycho Surefire's OSGi class loading environment
 * with PowerMock's class loading mechanism.
 * <p>
 * It should have been sufficient to only use {@link ClassPathAdjuster}
 * for the test that mocks {@link Py} however the class loading state of
 * PowerMock persists in some form between tests in the same OSGi runtime
 * therefore we must set things correctly for every PowerMock test in
 * the plugin.
 * <p>
 * The alternative mechanism is PowerMock's {@link PowerMockIgnore} but this
 * faces similar state issues with the specific class being mocked by one
 * of the tests in this plugin.
 *
 */
@UseClassPathAdjuster(PowerMockBase.CoreTestsClassPathAdjuster.class)
public abstract class PowerMockBase {

	public static class CoreTestsClassPathAdjuster implements ClassPathAdjuster {

		/**
		 * This adds the Jython bundle to the classpath of the {@link MockClassLoader} that
		 * powermock uses to load classes. It is required because {@link JythonShellParserTest}
		 * mocks {@link Py}.
		 */
		@Override
		public void adjustClassPath(ClassPool classPool) {
			classPool.insertClassPath(new LoaderClassPath(Py.class.getClassLoader()));
		}
	}
}
