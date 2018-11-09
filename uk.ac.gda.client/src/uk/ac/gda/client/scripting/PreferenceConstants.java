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

package uk.ac.gda.client.scripting;

public class PreferenceConstants {

	private PreferenceConstants() {
		throw new IllegalStateException("PreferenceConstants class (no instantiation)");
	}

	/**
	 * Create projects linking to beamline specific scripts.
	 */
	public static final String SHOW_CONFIG_SCRIPTS     = "uk.ac.gda.pydev.show.python.config.project";

	/**
	 * Create projects linking to GDA plugin scripts.
	 */
	public static final String SHOW_GDA_SCRIPTS        = "uk.ac.gda.pydev.show.python.gda.project";

	/**
	 * Name of preference to set TRUE if default java jars are to be added to ClassPath for PyDev interpreter Taken to
	 * be FALSE if not set.
	 */
	public static final String GDA_PYDEV_ADD_DEFAULT_JAVA_JARS = "gda.PyDev.addDefaultJavaJars";

}
