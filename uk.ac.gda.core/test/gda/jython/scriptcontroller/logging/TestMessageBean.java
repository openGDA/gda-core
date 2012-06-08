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

package gda.jython.scriptcontroller.logging;

public class TestMessageBean implements ScriptControllerLoggingMessage {

	private String uniqueID = "";
	private String progressMessage = "";
	private String runName = "";
	private String userComment = "";
	private String percentComplete = "";

	@Override
	public String getUniqueID() {
		return uniqueID;
	}

	public void setUniqueID(String uniqueID) {
		this.uniqueID = uniqueID;
	}

	@Override
	public String getName() {
		return "test scan";
	}

	@ScriptControllerLogColumn(columnName = "Progress", refresh = true, columnIndex = 1)
	public String getProgressMessage() {
		return progressMessage;
	}

	public void setProgressMessage(String progressMessage) {
		this.progressMessage = progressMessage;
	}

	@ScriptControllerLogColumn(columnName = "Run name", refresh = false, columnIndex = 0)
	public String getRunName() {
		return runName;
	}

	public void setRunName(String runName) {
		this.runName = runName;
	}

	@ScriptControllerLogColumn(columnName = "Comment", refresh = false, columnIndex = 2)
	public String getUserComment() {
		return userComment;
	}

	public void setUserComment(String userComment) {
		this.userComment = userComment;
	}

	@ScriptControllerLogColumn(columnName = "Complete", refresh = true, columnIndex = 3)
	public String getPercentComplete() {
		return percentComplete;
	}

	public void setPercentComplete(String percentComplete) {
		this.percentComplete = percentComplete;
	}

	@Override
	public float getPercentDone() {
		String percent = percentComplete.replace("%", "").trim();
		return Float.parseFloat(percent);
	}

	@Override
	public String getMsg() {
		return getPercentComplete()  + " complete.";
	}
}