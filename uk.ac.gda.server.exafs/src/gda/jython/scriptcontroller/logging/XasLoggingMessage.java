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

import java.util.concurrent.TimeUnit;

import gda.exafs.scan.ExafsTimeEstimator;
import uk.ac.gda.beans.exafs.IScanParameters;

public class XasLoggingMessage implements ScriptControllerLoggingMessage {

	private String id;
	private String scriptName;
	private String message;
	private String repetition;
	private String totalRepetitions;
	private String percentComplete;
	private String elaspedScanTime;
	private String elaspedTotalTime;
	private String predictedTotalTime;
	private String outputFolder;
	
	
	protected XasLoggingMessage(String id, String scriptName, String message, String repetition, String totalRepetitions, String percentComplete,
			String elaspedScanTime, String elaspedTotalTime, String predictedTotalTime, String outputFolder) {
		super();
		this.id = id;
		this.scriptName = scriptName;
		this.message = message;
		this.repetition = repetition;
		this.totalRepetitions = totalRepetitions;
		this.percentComplete = percentComplete;
		this.elaspedScanTime = elaspedScanTime;
		this.elaspedTotalTime = elaspedTotalTime;
		this.predictedTotalTime = predictedTotalTime;
		this.outputFolder = outputFolder;
	}
	
	public XasLoggingMessage(String id, String scriptName, String message, String repetition, String totalRepetitions, String percentComplete,
			String elaspedScanTime, String elaspedTotalTime, IScanParameters parameters, String outputFolder) throws Exception {
		super();
		this.id = id;
		this.scriptName = scriptName;
		this.message = message;
		this.repetition = repetition;
		this.totalRepetitions = totalRepetitions;
		this.percentComplete = percentComplete;
		this.elaspedScanTime = elaspedScanTime;
		this.elaspedTotalTime = elaspedTotalTime;
		this.outputFolder = outputFolder;
		
		long totalTime = ExafsTimeEstimator.getTime(parameters);
		
		long hours = TimeUnit.MILLISECONDS.toHours(totalTime);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(totalTime) - TimeUnit.HOURS.toMinutes(hours);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(totalTime) - TimeUnit.HOURS.toSeconds(hours)
				- TimeUnit.MINUTES.toSeconds(minutes);
		String diff = String.format("%2dh%2dm%2ds", hours, minutes, seconds);
		
		this.predictedTotalTime = diff;
	}

	@Override
	public String getUniqueID() {
		return id;
	}

	@Override
	public String getName() {
		return scriptName;
	}
	
	@ScriptControllerLogColumn(columnName = "Status", refresh = true, columnIndex = 0)
	public String getMessage() {
		return message;
	}

	@ScriptControllerLogColumn(columnName = "Repetition", refresh = true, columnIndex = 1)
	public String getRepetition() {
		return repetition + " of " + totalRepetitions;
	}
	
	public Integer getRepetitionNumber() {
		return Integer.parseInt(repetition);
	}

	public String getTotalRepetitions() {
		return totalRepetitions;
	}

	@ScriptControllerLogColumn(columnName = "Percent Complete", refresh = true, columnIndex = 2)
	public String getPercentComplete() {
		return percentComplete;
	}

	@ScriptControllerLogColumn(columnName = "Scan Elapsed Time", refresh = true, columnIndex = 3)
	public String getElaspedScanTime() {
		return elaspedScanTime;
	}

	@ScriptControllerLogColumn(columnName = "Total Elapsed Time", refresh = true, columnIndex = 4)
	public String getElaspedTotalTime() {
		return elaspedTotalTime;
	}

	@ScriptControllerLogColumn(columnName = "Total Time", refresh = false, columnIndex = 5)
	public String getPredictedTotalTime() {
		return predictedTotalTime;
	}
	
	@ScriptControllerLogColumn(columnName = "output Folder", refresh = false, columnIndex = 6)
	public String getOutputFolder() {
		return outputFolder;
	}

	@Override
	public float getPercentDone() {
		String percent = percentComplete.replace("%", "").trim();
		return Float.parseFloat(percent);
	}

	@Override
	public String getMsg() {
		return getPercentComplete();
	}
}
