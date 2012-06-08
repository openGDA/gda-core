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

import java.util.List;

/**
 * A description of the context in which the Jython server is running. The
 * client can use this to discover information about, for example, where
 * the scripts and execution history can be found.
 */
/* TODO
 * If this interface has a method List<ScriptProject> getAllScriptProjects(), then
 * the other methods are not necessary. The client code can use whatever information
 * it needs from the project definitions themselves, satisfying the "tell, don't ask"
 * principle. That requires that the ScriptProject objects can be serialised and sent
 * over CORBA.
 */
public interface IJythonContext {

	/**
	 * The first folder in which the Jython server looks for scripts.
	 * @return A path to the default location for Jython scripts.
	 */
	public String getDefaultScriptProjectFolder();
	
	/**
	 * Given the path to a Jython project, return its name.
	 * @return The name of the specified Jython project. If the path is not
	 *         a known project, then null is returned.
	 * @param path The path to a Jython script project.
	 */
	public String getProjectNameForPath(String path);
	
	/**
	 * All of the folders in which the Jython server looks for scripts.
	 * @return A list of paths to the Jython script project folders.
	 */
	public List<String> getAllScriptProjectFolders();
	
	/**
	 * Whether a project specified by path is a user project.
	 * @param path The project folder path.
	 * @return true if the project exists and is a user project.
	 */
	public boolean projectIsUserType(String path);
	
	/**
	 * Whether a project specified by path is a configuration project.
	 * @param path The project folder path.
	 * @return true if the project exists and is a config project.
	 */
	public boolean projectIsConfigType(String path);
	
	/**
	 * Whether a project specified by path is a core project.
	 * @param path The project folder path.
	 * @return true if the project exists and is a core project.
	 */
	public boolean projectIsCoreType(String path);
}
