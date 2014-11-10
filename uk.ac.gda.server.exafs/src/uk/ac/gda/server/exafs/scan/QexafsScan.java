/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

import gda.commandqueue.Processor;
import gda.data.metadata.NXMetaDataProvider;
import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import gda.data.scan.datawriter.AsciiMetadataConfig;
import gda.device.detector.BufferedDetector;
import gda.device.scannable.ContinuouslyScannable;
import gda.exafs.scan.ScanStartedMessage;
import gda.jython.scriptcontroller.event.ScanCreationEvent;
import gda.jython.scriptcontroller.event.ScanFinishEvent;
import gda.jython.scriptcontroller.event.ScriptProgressEvent;
import gda.jython.scriptcontroller.logging.LoggingScriptController;
import gda.jython.scriptcontroller.logging.XasLoggingMessage;
import gda.jython.scriptcontroller.logging.XasProgressUpdater;
import gda.scan.ContinuousScan;

import java.util.ArrayList;
import java.util.List;

import uk.ac.gda.beans.exafs.QEXAFSParameters;

public class QexafsScan extends EnergyScan {

	private ContinuouslyScannable qexafsScanable;
	private QexafsDetectorPreparer qexafsdetectorPreparer;

	protected QexafsScan(BeamlinePreparer beamlinePreparer, QexafsDetectorPreparer detectorPreparer,
			SampleEnvironmentPreparer samplePreparer, OutputPreparer outputPreparer, Processor commandQueueProcessor,
			LoggingScriptController XASLoggingScriptController, AsciiDataWriterConfiguration datawriterconfig,
			ArrayList<AsciiMetadataConfig> original_header, ContinuouslyScannable energy_scannable,
			NXMetaDataProvider metashop, boolean includeSampleNameInNexusName) {
		super(beamlinePreparer, detectorPreparer, samplePreparer, outputPreparer, commandQueueProcessor,
				XASLoggingScriptController, datawriterconfig, original_header, energy_scannable, metashop,
				includeSampleNameInNexusName);
		qexafsdetectorPreparer = detectorPreparer;
		qexafsScanable = energy_scannable;
	}
	
	@Override
	public String getScanType(){
		return "Qexafs";
	}

	@Override
	protected void createAndRunScan(String sampleName, List<String> descriptions, XasLoggingMessage logmsg)
			throws Exception {

		BufferedDetector[] detectorList = _getQEXAFSDetectors();

		double initial_energy = ((QEXAFSParameters) scanBean).getInitialEnergy();
		double final_energy = ((QEXAFSParameters) scanBean).getFinalEnergy();
		double step_size = ((QEXAFSParameters) scanBean).getStepSize();
		int numberPoints = (int) Math.ceil((final_energy - initial_energy) / step_size);
		double scan_time = ((QEXAFSParameters) scanBean).getTime();

		XASLoggingScriptController.update(null, new ScanStartedMessage(scanBean, detectorBean));

		XasProgressUpdater loggingbean = new XasProgressUpdater(XASLoggingScriptController, logmsg,
				timeRepetitionsStarted);

		double current_energy = (Double) energy_scannable.getPosition();
		double dist_to_init = Math.abs(initial_energy - current_energy);
		double dist_to_final = Math.abs(final_energy - current_energy);

		log("Current energy: " + current_energy);
		log("Initial energy:  " + initial_energy);
		log("Final energy: " + final_energy);
		log("Distance to initial energy: " + dist_to_init);
		log("Distance to final energy: " + dist_to_final);

		double start = initial_energy;
		double end = final_energy;

		if (((QEXAFSParameters) scanBean).getBothWays()) {
			if (dist_to_init < dist_to_final) {
				// #go forward
				start = initial_energy;
				end = final_energy;
			} else {
				// #?go reverse
				start = final_energy;
				end = initial_energy;
			}
		}

		log("Scan: " + energy_scannable.getName() + " " + start + " " + end + " " + numberPoints + " " + scan_time
				+ " " + detectorList);
		XASLoggingScriptController.update(null, new ScriptProgressEvent("Running QEXAFS scan"));
		ContinuousScan thisscan = new ContinuousScan(qexafsScanable, start, end, numberPoints, scan_time, detectorList);
		thisscan = (ContinuousScan) setUpDataWriter(thisscan, sampleBean.getName(), sampleBean.getDescriptions());
		XASLoggingScriptController.update(null, new ScanCreationEvent(thisscan.getName()));
		loggingbean.atScanStart();
		thisscan.runScan();
		XASLoggingScriptController.update(null, new ScanFinishEvent(thisscan.getName(), ScanFinishEvent.FinishType.OK));
		loggingbean.atScanEnd();
	}

	private BufferedDetector[] _getQEXAFSDetectors() throws Exception {
		return qexafsdetectorPreparer.getQEXAFSDetectors();
	}
}
