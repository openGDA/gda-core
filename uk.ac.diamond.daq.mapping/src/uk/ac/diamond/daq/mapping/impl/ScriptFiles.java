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

import uk.ac.diamond.daq.mapping.api.IScriptFiles;

/**
 * Implementation of {@link IScriptFiles}.
 */
public class ScriptFiles implements IScriptFiles {

	private String beforeScanScript;

	private String afterScanScript;

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

}
