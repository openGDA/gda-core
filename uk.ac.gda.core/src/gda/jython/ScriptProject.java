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

public class ScriptProject {
	private String path;
	private String name; 
	private ScriptProjectType type;

	public ScriptProject() {}
	
	public ScriptProject(String path, String name, ScriptProjectType type) {
		this.path = path;
		this.name = name;
		this.type = type;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setType (ScriptProjectType type) {
		this.type = type;
	}
	
	public ScriptProjectType type() {
		return type;
	}
	
	public Boolean isUserProject() {
		return type == ScriptProjectType.USER;
	}
	
	public Boolean isConfigProject() {
		return type == ScriptProjectType.CONFIG;
	}
	
	public Boolean isCoreProject() {
		return type == ScriptProjectType.CORE;
	}
}
