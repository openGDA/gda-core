/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.impl;

import java.util.Map;
import java.util.Objects;

import uk.ac.diamond.daq.mapping.api.IScriptFiles;

/**
 * Implementation of {@link IScriptFiles}.
 */
public class ScriptFiles implements IScriptFiles {

	private String beforeScanScript;

	private String afterScanScript;

	private boolean alwaysRunAfterScript;

	private Map<String,String> environment;

	@Override
	public String getBeforeScanScript() {
		return beforeScanScript;
	}

	@Override
	public void setBeforeScanScript(String beforeScanScript) {
		this.beforeScanScript = beforeScanScript;
	}

	@Override
	public String getAfterScanScript() {
		return afterScanScript;
	}

	@Override
	public void setAfterScanScript(String afterScanScript) {
		this.afterScanScript = afterScanScript;
	}

	@Override
	public boolean isAlwaysRunAfterScript() {
		return alwaysRunAfterScript;
	}

	@Override
	public void setAlwaysRunAfterScript(boolean alwaysRunAfterScript) {
		this.alwaysRunAfterScript = alwaysRunAfterScript;
	}

	@Override
	public Map<String, String> getEnvironment() {
		return environment;
	}

	@Override
	public void setEnvironment(Map<String, String> environment) {
		this.environment = environment;
	}

	@Override
	public int hashCode() {
		return Objects.hash(afterScanScript, alwaysRunAfterScript, beforeScanScript, environment);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScriptFiles other = (ScriptFiles) obj;
		return Objects.equals(afterScanScript, other.afterScanScript)
				&& alwaysRunAfterScript == other.alwaysRunAfterScript
				&& Objects.equals(beforeScanScript, other.beforeScanScript)
				&& Objects.equals(environment, other.environment);
	}

	@Override
	public String toString() {
		return "ScriptFiles [beforeScanScript=" + beforeScanScript + ", afterScanScript=" + afterScanScript
				+ ", alwaysRunAfterScript=" + alwaysRunAfterScript + ", environment=" + environment + "]";
	}


}

