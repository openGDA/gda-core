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

package gda.util.logging;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import ch.qos.logback.core.PropertyDefinerBase;
import ch.qos.logback.core.spi.PropertyDefiner;

/**
 * <p>
 * A Logback {@link PropertyDefiner} that returns the JVM name, as reported by
 * {@link RuntimeMXBean#getName()}.
 * </p>
 *
 * <p>
 * On Linux this is "{@code <pid>@<hostname>}", but could change in the future.
 * </p>
 */
public class JvmNamePropertyDefiner extends PropertyDefinerBase {

	@Override
	public String getPropertyValue() {
		final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
		return jvmName;
	}

}
