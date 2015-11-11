/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.example.scriptcontroller.logging;

import gda.jython.scriptcontroller.logging.ScriptControllerLogColumn;
import gda.jython.scriptcontroller.logging.ScriptControllerLoggingMessage;

import java.io.Serializable;

/**
 * Message bean as a demo for the LooginScriptController object. Works with the MessagingDemoScript.py
 */
public class ExampleLoggingMessage implements ScriptControllerLoggingMessage, Serializable {

	private final String visitID;
	String id = "";
	String scriptName;
	String progress;
	String percentComplete;
	String sampleTemperature;

	public ExampleLoggingMessage(String visitID, String id, String scriptName, String progress, String percentComplete,
			String sampleTemperature) {
		this.visitID = visitID;
		this.id = id;
		this.scriptName = scriptName;
		this.progress = progress;
		this.percentComplete = percentComplete;
		this.sampleTemperature = sampleTemperature;
	}

	@Override
	public String getUniqueID() {
		return id;
	}

	@Override
	public String getName() {
		return scriptName;
	}

	@ScriptControllerLogColumn(columnName = "Progress", refresh = true, columnIndex = 0)
	public String getProgress() {
		return progress;
	}

	@ScriptControllerLogColumn(columnName = "Complete", refresh = true, columnIndex = 1)
	public String getPercentComplete() {
		return percentComplete;
	}

	@ScriptControllerLogColumn(columnName = "Initial Temp", refresh = false, columnIndex = 2)
	public String getSampleTemperature() {
		return sampleTemperature;
	}

	@Override
	public float getPercentDone() {
		String percent = percentComplete.replace("%", "").trim();
		return Float.parseFloat(percent);
	}

	@Override
	public String getMsg() {
		return getProgress() + " (" + getPercentComplete() + " complete)";
	}

	@Override
	@ScriptControllerLogColumn(columnName = "Visit ID", refresh = false, columnIndex = 0)
	public String getVisitID() {
		return visitID;
	}

}