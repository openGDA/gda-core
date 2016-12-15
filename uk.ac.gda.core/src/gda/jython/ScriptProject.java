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

import java.io.IOException;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptProject {
	private static final Logger logger = LoggerFactory.getLogger(ScriptProject.class);

	private String path;
	private String name;
	private ScriptProjectType type;

	public ScriptProject() {}

	public ScriptProject(String path, String name, ScriptProjectType type) {
		this.path = resolvePath(path);
		this.name = name;
		this.type = type;
	}

	public void setPath(String path) {
		this.path = resolvePath(path);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScriptProject other = (ScriptProject) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	/**
	 * Checks that the supplied path can be resolved to a real one and in the
	 * process expands links or abbreviations like ../ so that it is the full
	 * path that is returned for storage in the path instance variable. If it
	 * cannot be resolved to a full path, an error is written to the log and
	 * the path String is marked as having failed resolution. This will cause
	 * a further error to be reported when the Jython interpreter is initialised.
	 *
	 * @param path		The configured path String usually from a Spring bena definition
	 * @return			The corresponding full path or a marked version of the original
	 * 					indicating failure.
	 */
	private String resolvePath(String path) {
		try {
			path = Paths.get(path).toRealPath().toString();
		} catch (IOException ioex) {
			logger.error("The script path {} cannot be resolved to a real path", path);
			path = String.format("UNRESOLVED: %s", path);
		}
		return path;
	}
}
