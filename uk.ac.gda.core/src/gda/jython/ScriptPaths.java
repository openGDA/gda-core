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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implements a search path for Jython scripts, so the server and script
 * commands can find scripts without knowing whether they belong to the user,
 * the beamline or something else and without needing to know paths to the
 * script folders.
 */
public class ScriptPaths {

	private List<ScriptProject> projects;
	private String _startupScript;

	/* TODO
	 * Move the gda var folder and cache folder to here from the JythonServer class.
	 */

	/**
	 * Default constructor provides an empty list of paths.
	 */
	public ScriptPaths() {
		projects = Arrays.asList(new ScriptProject[] {});
	}

	/**
	 * Take a list of the script folder projects.
	 * @param projects The list of paths to script folders, with names and project types..
	 */
	public ScriptPaths(List<ScriptProject> projects) {
		this.projects = projects;
	}

	/**
	 * The list of paths to script folders.
	 */
	List<String> getPaths() {
		ArrayList<String> paths = new ArrayList<String>();
		for(ScriptProject project : projects) {
			paths.add(project.getPath());
		}
		return paths;
	}
	/**
	 * Provide a new list of projects for this object to search.
	 * @param projects A java.util.List of Jython script projects.
	 */
	public void setProjects(List<ScriptProject> projects) {
		this.projects = projects;
	}

	public List<ScriptProject> getProjects() {
		return projects;
	}

	/**
	 * The script that should be executed to initialise the Jython namespace.
	 * @return The full path to a Jython script.
	 */
	String getStartupScript() {
		return _startupScript;
	}

	/**
	 * Set the script to be used on launch to initialise the Jython namespace.
	 * @param startupScript The full path to a Jython script.
	 */
	public void setStartupScript(String startupScript) {
		_startupScript = startupScript;
	}

	/**
	 * Find a Jython script in this object's list of folders.
	 * @param script The name of the script, optionally without the ".py"
     *               extension.
	 * @return The path to the script if it exists in one of this object's
	 *         folders, or null if the script can't be found.
	 */
	public String pathToScript(String script) {
		if(script == null || script.equals("")) {
			return null;
		}
		String fullScript = (script.endsWith(".py") ? script : script + ".py");
		for(String path : this.getPaths()) {
			String possibleLocation = path + File.separator + fullScript;
			if (new File(possibleLocation).exists()) {
				return possibleLocation;
			}
		}
		return null;
	}

	/**
	 * Build a description containing the paths searched by this object.
	 * @note This is part of the interface contract of the object, rather than
	 *       simply overriding {@link java.lang.Object#toString()}, because
	 *       that method doesn't make guarantees regarding its content.
	 * @return A comma-separated list of the paths used by this object.
	 */
	public String description() {
		StringBuilder builder = new StringBuilder("script search paths: ");
		for (String path: this.getPaths()) {
			builder.append(path + ", ");
		}
		builder.delete(builder.length() - 2, builder.length());
		builder.append(".");
		return builder.toString();
	}

	/**
	 * A debugging description.
	 */
	@Override
	public String toString() {
		return this.description();
	}

	String nameAt(int index) {
		ScriptProject project = projects.get(index);
		if (project == null) return null;
		return project.getName();
	}

	ScriptProject getProject(int index) {
		return projects.get(index);
	}

	/**
	 * Add the incoming {@link ScriptProject} object to the projects collection
	 * if it's not already present
	 *
	 * @param scriptProject
	 */
	void addProject(final ScriptProject scriptProject) {
		if (!projects.contains(scriptProject)) {
			projects.add(scriptProject);
		}
	}
}