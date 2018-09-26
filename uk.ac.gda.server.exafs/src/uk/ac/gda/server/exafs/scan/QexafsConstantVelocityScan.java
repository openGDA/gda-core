/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.gda.server.exafs.scan;

import java.util.List;

import gda.device.DeviceException;
import gda.device.detector.HardwareTriggeredNXDetector;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.exafs.scan.ScanStartedMessage;
import gda.jython.scriptcontroller.event.ScanCreationEvent;
import gda.jython.scriptcontroller.event.ScanFinishEvent;
import gda.jython.scriptcontroller.event.ScriptProgressEvent;
import gda.jython.scriptcontroller.logging.XasLoggingMessage;
import gda.jython.scriptcontroller.logging.XasProgressUpdater;
import gda.scan.ConstantVelocityScanLine;
import uk.ac.gda.beans.exafs.QEXAFSParameters;

public class QexafsConstantVelocityScan extends EnergyScan {
	private ContinuouslyScannableViaController qexafsScannable;
	private List<HardwareTriggeredNXDetector> nxDetectorList;
	private double start;
	private double end;
	private int numberPoints;
	private double step;
	private double scanTime;

	QexafsConstantVelocityScan() {
		// Used by XasScanFactory
	}

	@Override
	public String getScanType() {
		return "Qexafs";
	}

	private void prepareForCollection() throws DeviceException {
		start = ((QEXAFSParameters) scanBean).getInitialEnergy();
		end = ((QEXAFSParameters) scanBean).getFinalEnergy();
		step = ((QEXAFSParameters) scanBean).getStepSize();
		numberPoints = (int) Math.ceil((end - start) / step);
		scanTime = ((QEXAFSParameters) scanBean).getTime();

		double currentEnergy = (Double) qexafsScannable.getPosition();
		double distanceToInitialEnergy = Math.abs(start - currentEnergy);
		double distanceToFinalEnergy = Math.abs(end - currentEnergy);

		log("Current energy: " + currentEnergy);
		log("Initial energy:  " + start);
		log("Final energy: " + end);
		log("Distance to initial energy: " + distanceToInitialEnergy);
		log("Distance to final energy: " + distanceToFinalEnergy);
	}

	@Override
	protected void createAndRunScan(String sampleName, List<String> descriptions, XasLoggingMessage logmsg) throws Exception {
		prepareForCollection();

		loggingScriptController.update(null, new ScanStartedMessage(scanBean, detectorBean));
		XasProgressUpdater loggingbean = new XasProgressUpdater(loggingScriptController, logmsg, timeRepetitionsStarted);

		log("Scan: " + qexafsScannable.getName() + " " + start + " " + end + " " + numberPoints + " " + scanTime + " " + nxDetectorList);
		loggingScriptController.update(null, new ScriptProgressEvent("Running QEXAFS scan"));


		// parse arguments for ConstantVelocityScan
		Object[] args = parseArguments();
		ConstantVelocityScanLine thisscan = new ConstantVelocityScanLine(args);
		thisscan.setSendUpdateEvents(false);
		thisscan = (ConstantVelocityScanLine) setUpDataWriter(thisscan, sampleBean.getName(), sampleBean.getDescriptions());

		loggingScriptController.update(null, new ScanCreationEvent(thisscan.getName()));
		loggingbean.atScanStart();
		thisscan.runScan();
		loggingScriptController.update(null, new ScanFinishEvent(thisscan.getName(), ScanFinishEvent.FinishType.OK));
		loggingbean.atScanEnd();
	}

	private Object[] parseArguments() {
		double acquisitionTime = scanTime / numberPoints;
		// First four arguments are [scannable, start, stop, step]
		// then for each detector args [detector, exposure time].
		int numberOfArguments = 4 + 2 * nxDetectorList.size();
		final Object[] scanArguments = new Object[numberOfArguments];
		scanArguments[0] = qexafsScannable;
		scanArguments[1] = Double.valueOf(start);
		scanArguments[2] = Double.valueOf(end);
		scanArguments[3] = Double.valueOf(step);
		int i = 4;
		for (HardwareTriggeredNXDetector nxDetector : nxDetectorList) {
			scanArguments[i] = nxDetector;
			scanArguments[i + 1] = Double.valueOf(acquisitionTime);
			i = i + 2;
		}

		return scanArguments;
	}

	void setQexafsScannable(ContinuouslyScannableViaController qexafsScannable) {
		this.qexafsScannable = qexafsScannable;
	}

	void setQexafsNXDetectorList(List<HardwareTriggeredNXDetector> nxDetectorList) {
		this.nxDetectorList = nxDetectorList;
	}

}
