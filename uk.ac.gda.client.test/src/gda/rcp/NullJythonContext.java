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

package gda.rcp;

import java.util.List;

import gda.jython.IJythonContext;

public class NullJythonContext implements IJythonContext {

	@Override
	public String getDefaultScriptProjectFolder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getAllScriptProjectFolders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProjectNameForPath(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean projectIsUserType(String path) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean projectIsConfigType(String path) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean projectIsCoreType(String path) {
		// TODO Auto-generated method stub
		return false;
	}

}
