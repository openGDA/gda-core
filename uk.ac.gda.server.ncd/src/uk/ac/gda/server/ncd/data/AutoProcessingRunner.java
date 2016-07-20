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

package uk.ac.gda.server.ncd.data;

import java.io.IOException;

import gda.util.OSCommandRunner;
import gda.util.OSCommandRunner.LOGOPTION;

public class AutoProcessingRunner extends ProcessingRunner {
	private String scriptPath;
	@Override
	public void triggerProcessing(String... args) throws IOException {
		if (args.length != 1) {
			throw new IllegalArgumentException("Single argument required for autoprocessing");
		}
		if (getScriptPath() == null || getScriptPath().isEmpty()) {
			throw new IllegalStateException("No script set");
		}
		OSCommandRunner.runNoWait(new String[] { scriptPath, args[0] }, LOGOPTION.ONLY_ON_ERROR, null);
	}
	public String getScriptPath() {
		return scriptPath;
	}
	public void setScriptPath(String scriptPath) {
		this.scriptPath = scriptPath;
	}

}
