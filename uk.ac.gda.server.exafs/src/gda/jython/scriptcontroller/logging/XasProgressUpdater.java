/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;
import gda.exafs.scan.RepetitionsProperties;
import gda.jython.IScanDataPointObserver;
import gda.jython.IScanDataPointProvider;
import gda.jython.InterfaceProvider;
import gda.scan.IScanDataPoint;
import gda.scan.ScanDataPoint;

import java.util.concurrent.TimeUnit;

/**
 * A zero input, zero extra names Scannable which should be included in XAS scans to send progress messages to the
 * LoggingScriptController.
 */
public class XasProgressUpdater extends ScannableBase implements Scannable, IScanDataPointObserver {

	private transient final LoggingScriptController controller;
	private String id;
	private String predictedTotalTime;
	private String scriptName;
	private String thisScanrepetition;
	private String totalScanRepetitions;
	private String sampleEnvironmentRepetition;
	private String sampleEnvironmentRepetitions;
	private String outputFolder;
	private long timeRepetitionsStarted;
	private long timeStarted;
	private String lastPercentComplete = "0%";
	private volatile boolean atEndCalled = false;
	private String uniqueName;

	public XasProgressUpdater(LoggingScriptController controller, XasLoggingMessage msg, long timeRepetitionsStarted) {
		this.controller = controller;
		this.timeRepetitionsStarted = timeRepetitionsStarted;
		id = msg.getUniqueID();
		scriptName = msg.getName();
		thisScanrepetition = msg.getScanRepetitionNumber();
		totalScanRepetitions = msg.getScanRepetitions();
		sampleEnvironmentRepetition = msg.getSampleEnvironmentRepetitionNumber();
		sampleEnvironmentRepetitions = msg.getSampleEnvironmentRepetitions();
		predictedTotalTime = msg.getPredictedTotalTime();
		outputFolder = msg.getOutputFolder();
		setInputNames(new String[] {});
		setExtraNames(new String[] {});
		setOutputFormat(new String[] {});
		setName("XAS Progress Updater");
	}

	@Override
	public String toFormattedString() {
		return getName();
	}

	@Override
	public String toString() {
		return toFormattedString();
	}

	@Override
	public void atScanStart() throws DeviceException {
		timeOfLastReport = 0;
		atEndCalled = false;
		InterfaceProvider.getScanDataPointProvider().addIScanDataPointObserver(this);
		timeStarted = System.currentTimeMillis();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		atEndCalled = true;
		InterfaceProvider.getScanDataPointProvider().deleteIScanDataPointObserver(this);

		String status = "Repetition complete";
		IScanDataPoint lastSDP = InterfaceProvider.getScanDataPointProvider().getLastScanDataPoint();
		String percentComplete = determinePercentComplete(lastSDP,true) + "%";
		if (!sampleEnvironmentRepetition.equals(sampleEnvironmentRepetitions))
			status = "Sample Env repetition complete";
		else if (thisScanrepetition.equals(totalScanRepetitions)) {
			status = "Scan Complete";
			percentComplete = "100%";
		}

		XasLoggingMessage msg = new XasLoggingMessage(id, scriptName, status, thisScanrepetition,
				getTotalRepetitions(), sampleEnvironmentRepetitions, sampleEnvironmentRepetitions, percentComplete,
				getElapsedTime(), getElapsedTotalTime(), predictedTotalTime, outputFolder);
		controller.update(this, msg);
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		atEndCalled = true;
		InterfaceProvider.getScanDataPointProvider().deleteIScanDataPointObserver(this);
		XasLoggingMessage msg = new XasLoggingMessage(id, scriptName, "Aborted", thisScanrepetition,
				getTotalRepetitions(), sampleEnvironmentRepetitions, sampleEnvironmentRepetitions, lastPercentComplete,
				getElapsedTime(), getElapsedTotalTime(), predictedTotalTime, outputFolder);
		controller.update(this, msg);
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return null;
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		// do nothing
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	long timeOfLastReport = 0;

	@Override
	public void update(Object source, Object arg) {
		if (source instanceof IScanDataPointProvider && arg instanceof ScanDataPoint && !atEndCalled) {
			ScanDataPoint sdp = (ScanDataPoint) arg;
			if (uniqueName == null || uniqueName.equals(sdp.getUniqueName())) {
				uniqueName = sdp.getUniqueName();
				long now = System.currentTimeMillis();
				int percentComplete = determinePercentComplete(sdp,false);
				lastPercentComplete = percentComplete + "%";

				if (now - timeOfLastReport > 500) {
					timeOfLastReport = now;
					String elapsedTime = getElapsedTime();
					XasLoggingMessage msg = new XasLoggingMessage(id, scriptName, "In Progress", thisScanrepetition,
							getTotalRepetitions(), sampleEnvironmentRepetition, sampleEnvironmentRepetitions,
							percentComplete + "%", elapsedTime, getElapsedTotalTime(), predictedTotalTime, outputFolder);
					controller.update(this, msg);
				}
			} 
			else
				InterfaceProvider.getScanDataPointProvider().deleteIScanDataPointObserver(this);
		}
	}

	private int determinePercentComplete(IScanDataPoint sdp, Boolean lastPoint) {
		int pointsPerScan = sdp.getNumberOfPoints();
		int pointsPerScanRepetition = sdp.getNumberOfPoints() * Integer.parseInt(sampleEnvironmentRepetitions);
		int pointsInWholeExperiment = pointsPerScanRepetition * Integer.parseInt(getTotalRepetitions());
		int currentPointInThisScan = sdp.getCurrentPointNumber() + 1;
		// at the last point in the scan we get the call via atScanEnd instead of via update, so fix the current point number
		if (lastPoint)
			currentPointInThisScan = sdp.getNumberOfPoints();
		int currentPointInThisScanRepetition = currentPointInThisScan + (pointsPerScan * (Integer.parseInt(sampleEnvironmentRepetition) - 1));
		int currentPointInWholeExperiment = currentPointInThisScanRepetition + (pointsPerScanRepetition * (Integer.parseInt(thisScanrepetition) - 1));
		int percentComplete = (int) Math.round((currentPointInWholeExperiment * 100.0) / pointsInWholeExperiment);
		return percentComplete;
	}

	private String getTotalRepetitions() {
		String property = LocalProperties.get(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY);
		if (property == null || property.isEmpty())
			return totalScanRepetitions;
		return property;
	}

	private String getElapsedTime() {
		return calTimeDiff(timeStarted);
	}

	private String getElapsedTotalTime() {
		return calTimeDiff(timeRepetitionsStarted);
	}

	private String calTimeDiff(long startTime) {
		long now = System.currentTimeMillis();
		long duration = now - startTime;
		long hours = TimeUnit.MILLISECONDS.toHours(duration);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(hours);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minutes);
		String diff = String.format("%2dh%2dm%2ds", hours, minutes, seconds);
		return diff;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = super.hashCode();
		result = prime * result + (atEndCalled ? 1231 : 1237);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((lastPercentComplete == null) ? 0 : lastPercentComplete.hashCode());
		result = prime * result + ((outputFolder == null) ? 0 : outputFolder.hashCode());
		result = prime * result + ((predictedTotalTime == null) ? 0 : predictedTotalTime.hashCode());
		result = prime * result	+ ((sampleEnvironmentRepetitions == null) ? 0 : sampleEnvironmentRepetitions.hashCode());
		result = prime * result + ((scriptName == null) ? 0 : scriptName.hashCode());
		result = prime * result + ((thisScanrepetition == null) ? 0 : thisScanrepetition.hashCode());
		result = prime * result + (int) (timeRepetitionsStarted ^ (timeRepetitionsStarted >>> 32));
		result = prime * result + (int) (timeStarted ^ (timeStarted >>> 32));
		result = prime * result + ((totalScanRepetitions == null) ? 0 : totalScanRepetitions.hashCode());
		result = prime * result + ((uniqueName == null) ? 0 : uniqueName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		XasProgressUpdater other = (XasProgressUpdater) obj;
		if (atEndCalled != other.atEndCalled)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} 
		else if (!id.equals(other.id))
			return false;
		if (lastPercentComplete == null) {
			if (other.lastPercentComplete != null)
				return false;
		} 
		else if (!lastPercentComplete.equals(other.lastPercentComplete))
			return false;
		if (outputFolder == null) {
			if (other.outputFolder != null)
				return false;
		} 
		else if (!outputFolder.equals(other.outputFolder))
			return false;
		if (predictedTotalTime == null) {
			if (other.predictedTotalTime != null)
				return false;
		} 
		else if (!predictedTotalTime.equals(other.predictedTotalTime))
			return false;
		if (sampleEnvironmentRepetitions == null) {
			if (other.sampleEnvironmentRepetitions != null)
				return false;
		} 
		else if (!sampleEnvironmentRepetitions.equals(other.sampleEnvironmentRepetitions))
			return false;
		if (scriptName == null) {
			if (other.scriptName != null)
				return false;
		} 
		else if (!scriptName.equals(other.scriptName))
			return false;
		if (thisScanrepetition == null) {
			if (other.thisScanrepetition != null)
				return false;
		} 
		else if (!thisScanrepetition.equals(other.thisScanrepetition))
			return false;
		if (timeRepetitionsStarted != other.timeRepetitionsStarted)
			return false;
		if (timeStarted != other.timeStarted)
			return false;
		if (totalScanRepetitions == null) {
			if (other.totalScanRepetitions != null)
				return false;
		} 
		else if (!totalScanRepetitions.equals(other.totalScanRepetitions))
			return false;
		if (uniqueName == null) {
			if (other.uniqueName != null)
				return false;
		} 
		else if (!uniqueName.equals(other.uniqueName))
			return false;
		return true;
	}
}
