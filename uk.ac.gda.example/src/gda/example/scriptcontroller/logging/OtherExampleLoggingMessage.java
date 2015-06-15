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
public class OtherExampleLoggingMessage implements ScriptControllerLoggingMessage, Serializable {

	private String visitID;
	String id = "";
	String scriptName;
	String progress;

	public OtherExampleLoggingMessage(String visitID,String id, String scriptName, String progress) {
		this.visitID = visitID;
		this.id = id;
		this.scriptName = scriptName;
		this.progress = progress;
	}

	@Override
	public String getUniqueID() {
		return id;
	}

	@Override
	public String getName() {
		return scriptName;
	}

	@ScriptControllerLogColumn(columnName = "Progress", refresh = true, columnIndex = 1)
	public String getProgress() {
		return progress;
	}

	@Override
	public float getPercentDone() {
		return 0f;  // no progress value in this bean, so show nothing in the bar
	}

	@Override
	public String getMsg() {
		return getProgress();
	}
	@Override
	@ScriptControllerLogColumn(columnName = "Visit ID", refresh = false, columnIndex = 0)
	public String getVisitID() {
		return visitID;
	}

}