/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.util;

import org.eclipse.scanning.jython.JythonInterpreterManager;

/**
 * This is an OSGi DS component which allows the static state of Jython
 * (as represented by PySystemState's initialized field) to be configured early
 * in the life of the JVM so that tests may run in any order.
 * <p>
 * This is becuase some tests will indirectly cause Jython to be initalized whilst
 * others require specific initialization properties to be applied.
 * <p>
 * In this case a sufficient setup is provided by the solstice scanning {@link JythonInterpreterManager}
 * <p>
 * This is registered via its DS xml file under the OSGI-INF directory. Note that fragent bundles
 * (such as uk.ac.gda.core.test) cannot contain Service-Component manifest headers
 * so this works by the fact that the host bundle (uk.ac.gda.core) contains the header:
 * {@code Service-Component:OSGI-INF/*.xml}
 */
public class JythonTestInitializer {

	public void activate() {
		JythonInterpreterManager.setupSystemState();
	}

}
