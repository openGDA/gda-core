/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

public enum ScriptProjectType {
	/**
	 * Scripts edited by users
	 */
	USER,
	/**
	 * Scripts used to implement beamline functionality. Not user editable
	 */
	CONFIG,
	/**
	 * Scripts provided with GDA plugins. Not user editable
	 */
	CORE,

	/**
	 * Scripts provided with GDA EPICS plugins. Not user editable. Treated as CORE
	 * Use CORE rather than plugin specific values
	 * @deprecated this value will be removed in GDA 9.26
	 */
	@Deprecated(forRemoval = true, since = "Aug 2011")
	EPICS,

	/** Scripts that should not be shown in the PyDev Project Explorer, such as gdaserver.py
	 * which is used to provide imports/autocomplete, but should not be seen or edited.
	 */
	HIDDEN
}
