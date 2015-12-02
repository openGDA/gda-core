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

import gda.device.detector.HardwareTriggeredNXDetector;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.exafs.scan.ScanStartedMessage;
import gda.jython.scriptcontroller.event.ScanCreationEvent;
import gda.jython.scriptcontroller.event.ScanFinishEvent;
import gda.jython.scriptcontroller.event.ScriptProgressEvent;
import gda.jython.scriptcontroller.logging.XasLoggingMessage;
import gda.jython.scriptcontroller.logging.XasProgressUpdater;
import gda.scan.ConstantVelocityScanLine;

import java.util.List;

import uk.ac.gda.beans.exafs.QEXAFSParameters;

public class QexafsConstantVelocityScan extends EnergyScan {
	private ContinuouslyScannableViaController qexafsScanable;
	private List<HardwareTriggeredNXDetector> nxDetectorList;
	private double start;
	private double end;
	private int numberPoints;
	private double step;
	private double scan_time;

	protected QexafsConstantVelocityScan() {
		// Used by XasScanFactory
	}

	@Override
	public String getScanType() {
		return "Qexafs";
	}

	public void prepareForCollection() throws Exception {
		start = ((QEXAFSParameters) scanBean).getInitialEnergy();
		end = ((QEXAFSParameters) scanBean).getFinalEnergy();
		step = ((QEXAFSParameters) scanBean).getStepSize();
		numberPoints = (int) Math.ceil((end - start) / step);
		scan_time = ((QEXAFSParameters) scanBean).getTime();

		double current_energy = (Double) energyScannable.getPosition();
		double dist_to_init = Math.abs(start - current_energy);
		double dist_to_final = Math.abs(end - current_energy);

		log("Current energy: " + current_energy);
		log("Initial energy:  " + start);
		log("Final energy: " + end);
		log("Distance to initial energy: " + dist_to_init);
		log("Distance to final energy: " + dist_to_final);
	}

	@Override
	protected void createAndRunScan(String sampleName, List<String> descriptions, XasLoggingMessage logmsg) throws Exception {
		prepareForCollection();

		loggingScriptController.update(null, new ScanStartedMessage(scanBean, detectorBean));
		XasProgressUpdater loggingbean = new XasProgressUpdater(loggingScriptController, logmsg, timeRepetitionsStarted);

		log("Scan: " + energyScannable.getName() + " " + start + " " + end + " " + numberPoints + " " + scan_time + " " + nxDetectorList);
		loggingScriptController.update(null, new ScriptProgressEvent("Running QEXAFS scan"));


		// parse arguments for ConstantVelocityScan
		Object[] args = parseArguments();
		ConstantVelocityScanLine thisscan = new ConstantVelocityScanLine(args);
		thisscan = (ConstantVelocityScanLine) setUpDataWriter(thisscan, sampleBean.getName(), sampleBean.getDescriptions());

		loggingScriptController.update(null, new ScanCreationEvent(thisscan.getName()));
		loggingbean.atScanStart();
		thisscan.runScan();
		loggingScriptController.update(null, new ScanFinishEvent(thisscan.getName(), ScanFinishEvent.FinishType.OK));
		loggingbean.atScanEnd();
	}

	public Object[] parseArguments() {
		double acquisitionTime = (scan_time) / numberPoints;
		Object[] args = new Object[10];
		args[0] = qexafsScanable;
		args[1] = new Double(start);
		args[2] = new Double(end);
		args[3] = new Double(step);
		int i = 4;
		for (HardwareTriggeredNXDetector nxDetector : nxDetectorList) {
			args[i] = nxDetector;
			args[i + 1] = new Double(acquisitionTime);
			i = i + 2;
		}
		return args;
	}

	public void setQexafsScanable(ContinuouslyScannableViaController qexafsScanable) {
		this.qexafsScanable = qexafsScanable;
	}

	public void setQexafsNXDetectorList(List<HardwareTriggeredNXDetector> nxDetectorList) {
		this.nxDetectorList = nxDetectorList;
	}

}
