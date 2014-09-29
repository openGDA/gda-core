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
import gda.configuration.properties.LocalProperties;
import gda.data.metadata.NXMetaDataProvider;
import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import gda.data.scan.datawriter.AsciiMetadataConfig;
import gda.device.DeviceException;
import gda.device.detector.BufferedDetector;
import gda.device.scannable.ContinuouslyScannable;
import gda.exafs.scan.RepetitionsProperties;
import gda.exafs.scan.ScanStartedMessage;
import gda.jython.InterfaceProvider;
import gda.jython.ScriptBase;
import gda.jython.scriptcontroller.event.ScanCreationEvent;
import gda.jython.scriptcontroller.event.ScanFinishEvent;
import gda.jython.scriptcontroller.event.ScriptProgressEvent;
import gda.jython.scriptcontroller.logging.LoggingScriptController;
import gda.jython.scriptcontroller.logging.XasLoggingMessage;
import gda.jython.scriptcontroller.logging.XasProgressUpdater;
import gda.scan.ContinuousScan;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PySequence;

import uk.ac.gda.beans.exafs.QEXAFSParameters;

public class QexafsScan extends ExafsScan {

	private boolean beamCheck;
	private boolean gmsd_enabled;
	private boolean additional_channels_enabled;
	private boolean cirrusEnabled;
	private ContinuouslyScannable qexafsScanable;

	public QexafsScan(DetectorPreparer detectorPreparer, SampleEnvironmentPreparer samplePreparer,
			OutputPreparer outputPreparer, Processor commandQueueProcessor,
			LoggingScriptController XASLoggingScriptController, AsciiDataWriterConfiguration datawriterconfig,
			ArrayList<AsciiMetadataConfig> original_header, ContinuouslyScannable energy_scannable,
			boolean includeSampleNameInNexusName, NXMetaDataProvider metashop) {
		super(detectorPreparer, samplePreparer, outputPreparer, commandQueueProcessor, XASLoggingScriptController,
				datawriterconfig, original_header, energy_scannable, includeSampleNameInNexusName, metashop);
		beamCheck = true;
		gmsd_enabled = false;
		additional_channels_enabled = false;
		qexafsScanable = energy_scannable;
	}

	// TODO can we live without ionchambers and cirrus objects?
	// def __init__(self,detectorPreparer, samplePreparer, outputPreparer, commandQueueProcessor, ExafsScriptObserver,
	// XASLoggingScriptController, datawriterconfig, original_header, energy_scannable, ionchambers, cirrus=None):
	// Scan.__init__(self, detectorPreparer, samplePreparer, outputPreparer, commandQueueProcessor, ExafsScriptObserver,
	// XASLoggingScriptController, datawriterconfig, original_header, energy_scannable, ionchambers)
	// cirrus = cirrus
	// cirrusEnabled = False
	// beamCheck = True
	// gmsd_enabled = False
	// additional_channels_enabled = False

	// def __call__(self, sampleFileName, scanFileName, detectorFileName, outputFileName, experimentFullPath,
	// numRepetitions= -1, validation=True):
	public PyObject __call__(PyObject pyArgs) throws Exception {

		String sampleFileName = ((PySequence) pyArgs).__finditem__(0).asString();
		String scanFileName = ((PySequence) pyArgs).__finditem__(1).asString();
		String detectorFileName = ((PySequence) pyArgs).__finditem__(2).asString();
		String outputFileName = ((PySequence) pyArgs).__finditem__(3).asString();
		String experimentFullPath = ((PySequence) pyArgs).__finditem__(4).asString();
		numRepetitions = ((PySequence) pyArgs).__finditem__(5).asInt();

		// print ""
		log("Starting QEXAFS scan...");
		determineExperimentPath(experimentFullPath);

		_createBeans(sampleFileName, scanFileName, detectorFileName, outputFileName);

		// experimentFullPath, experimentFolderName = determineExperimentPath(experimentFullPath)
		// #print "qexafs XML file names",sampleFileName, scanFileName, detectorFileName, outputFileName
		// setXmlFileNames(sampleFileName, scanFileName, detectorFileName, outputFileName)

		// TODO cirrus?
		// if cirrusEnabled:
		// t = None

		// sampleBean, scanBean, detectorBean, outputBean = _createBeans(experimentFullPath, sampleFileName,
		// scanFileName, detectorFileName, outputFileName)
		// controller = ExafsScriptObserver

		outputBean.setAsciiFileName(sampleBean.getName());
		// beanGroup = _createBeanGroup(experimentFolderName, validation, controller, experimentFullPath, sampleBean,
		// scanBean, detectorBean, outputBean)
		// # work out which detectors to use (they will need to have been configured already by the GUI)
		BufferedDetector[] detectorList = _getQEXAFSDetectors();
		log("Detectors: " + detectorList);

		// # send initial message to the log
		// loggingcontroller = XASLoggingScriptController
		scriptType = "Qexafs";
		scan_unique_id = LoggingScriptController.createUniqueID(scriptType);
		// unique_id = LoggingScriptController.createUniqueID(scriptType);

		// outputFolder = outputBean.getAsciiDirectory()+ "/" + outputBean.getAsciiFileName()

		setQueuePropertiesStart();
		int currentRepetition = 0;
		long timeRepetitionsStarted = System.currentTimeMillis();

		try {
			while (true) {
				currentRepetition++;
				runPreparers();
				double initial_energy = ((QEXAFSParameters) scanBean).getInitialEnergy();
				double final_energy = ((QEXAFSParameters) scanBean).getFinalEnergy();
				double step_size = ((QEXAFSParameters) scanBean).getStepSize();
				// #print 'Prepare output'
				// if len(outputBean.getCheckedSignalList()) > 0:
				// print "Signal parameters not available with QEXAFS"
				// if energy_scannable == None:
				// raise
				// "No object for controlling energy during QEXAFS found! Expected qexafs_energy (or scannable1 for testing)"
				int numberPoints = (int) Math.ceil((final_energy - initial_energy) / step_size);
				_runScript(outputBean.getBeforeScriptName());
				double scan_time = ((QEXAFSParameters) scanBean).getTime();

				String initialPercent = calcInitialPercent();
				long timeSinceRepetitionsStarted = System.currentTimeMillis() - timeRepetitionsStarted;
				String sampleName = sampleBean.getName();
				XasLoggingMessage logmsg = new XasLoggingMessage(_getMyVisitID(), scan_unique_id, scriptType,
						"Starting " + scriptType + " scan...", Integer.toString(currentRepetition),
						Integer.toString(numRepetitions), Integer.toString(1), Integer.toString(1), initialPercent,
						Integer.toString(0), Long.toString(timeSinceRepetitionsStarted), scanBean,
						experimentFolderName, sampleName, 0);

				XASLoggingScriptController.update(null, new ScanStartedMessage(scanBean, detectorBean));// # informs
																										// parts of

				// if cirrusEnabled:
				// acquireCirrus()

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

				log("Scan: " + energy_scannable.getName() + " " + start + " " + end + " " + numberPoints + " "
						+ scan_time + " " + detectorList);
				XASLoggingScriptController.update(null, new ScriptProgressEvent("Running QEXAFS scan"));
				ContinuousScan thisscan = new ContinuousScan(qexafsScanable, start, end, numberPoints, scan_time,
						detectorList);
				thisscan = (ContinuousScan) _setUpDataWriter(thisscan, sampleBean.getName(),
						sampleBean.getDescriptions());
				XASLoggingScriptController.update(null, new ScanCreationEvent(thisscan.getName()));
				try {
					if (numRepetitions > 1) {
						log("Starting repetition " + currentRepetition + " of " + numRepetitions);
					}
					loggingbean.atScanStart();
					thisscan.runScan();
					XASLoggingScriptController.update(null, new ScanFinishEvent(thisscan.getName(),
							ScanFinishEvent.FinishType.OK));
					loggingbean.atScanEnd();
				} catch (DeviceException e) {
					_resetHeader();
					loggingbean.atCommandFailure();
					if (LocalProperties.get(RepetitionsProperties.SKIP_REPETITION_PROPERTY).equals("true")) {
						LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY, "false");
						// # check if an abort or panic stop has been issued, so the whole script should stop
						if (Thread.currentThread().isInterrupted()) {
							throw e;
						}
						// # only wanted to skip this repetition, so absorb the exception and continue the loop
						if (numRepetitions > 1) {
							log("Repetition " + currentRepetition + " skipped.");
						}
					} else {
						log("Exception while running QEXAFS:" + e);
						log("Will not abort queue but will continue to the next scan, if available");
					}
				} catch (java.lang.Exception e) {
					_resetHeader();
					loggingbean.atCommandFailure();
					if (LocalProperties.get(RepetitionsProperties.SKIP_REPETITION_PROPERTY).equals("true")) {
						LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY, "false");
						// # check if an abort or panic stop has been issued, so the whole script should stop
						if (Thread.currentThread().isInterrupted()) {
							throw e;
						}
						// # only wanted to skip this repetition, so absorb the exception and continue the loop
						if (numRepetitions > 1) {
							log("Repetition " + currentRepetition + " skipped.");
						}
					} else {
						throw e;
					}
				}
				_runScript(outputBean.getAfterScriptName());

				// #check if halt after current repetition set to true
				if (LocalProperties.get(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY).equals("true")) {
					log("Paused scan after repetition "
							+ currentRepetition
							+ ". To resume the scan, press the Start button in the Command Queue view. To abort this scan, press the Skip Task button.");
					LocalProperties.set(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY, "false");
					ScriptBase.setPaused(true);
					ScriptBase.checkForPauses();
				}
				// #check if the number of repetitions has been altered and we should now end the loop
				int numRepsFromProperty = LocalProperties.getAsInt(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY);
				if (numRepsFromProperty != numRepetitions && numRepsFromProperty <= (currentRepetition)) {
					log("The number of repetitions has been reset to " + numRepsFromProperty + ". As "
							+ currentRepetition + "repetitions have been completed this scan will now end.");
					_resetHeader();
					break;
				} else if (numRepsFromProperty <= (currentRepetition)) {
					_resetHeader();
					break;
				}
			}
		} finally {
			LocalProperties.set("gda.scan.useScanPlotSettings", "false");
			LocalProperties.set("gda.plot.ScanPlotSettings.fromUserList", "false");
			_resetHeader();
			if (cirrusEnabled) {
				// t.stop;
			}

			log("QEXAFS finished.");
		}
		return new PyInteger(0);
	}

	private BufferedDetector[] _getQEXAFSDetectors() throws Exception {
		String expt_type = detectorBean.getExperimentType();
		if (expt_type.equals("Transmission")) {
			// #print "This is a transmission scan"
			if (gmsd_enabled) {
				return _createBufferedDetArray(new String[] { "qexafs_counterTimer01_gmsd" });
			} else if (additional_channels_enabled) {
				return _createBufferedDetArray(new String[] { "qexafs_counterTimer01",
						"qexafs_counterTimer01_gmsd" });
			} else {
				// # when using xspress3 in qexafs scans. NB: must use the Transmission option in the UI
				// # return _createDetArray(["qexafs_FFI0_xspress3","qexafs_xspress3","qexafs_counterTimer01"],
				// scanBean);
				return _createBufferedDetArray(new String[] { "qexafs_counterTimer01" });
			}
		}

		if (detectorBean.getFluorescenceParameters().getDetectorType().equals("Silicon")) {
			return _createBufferedDetArray(new String[] { "qexafs_counterTimer01", "qexafs_xmap",
					"VortexQexafsFFI0" });
		} else if (detectorBean.getFluorescenceParameters().getDetectorType().equals("Xspress3")) {
			return _createBufferedDetArray(new String[] { "qexafs_counterTimer01", "qexafs_xspress3",
					"qexafs_FFI0_xspress3" });
		} else {
			return _createBufferedDetArray(new String[] { "qexafs_counterTimer01", "qexafs_xspress",
					"QexafsFFI0" });
		}

	}

	protected BufferedDetector[] _createBufferedDetArray(String[] names) throws Exception {
		BufferedDetector[] dets = new BufferedDetector[] {};
		for (String name : names) {
			Object detector = InterfaceProvider.getJythonNamespace().getFromJythonNamespace(name);
			if (detector == null) {
				throw new Exception("detector named " + name + " not found!");
			}
			dets = (BufferedDetector[]) ArrayUtils.add(dets, detector);
		}
		return dets;
	}
	
	
	public boolean isBeamCheck() {
		return beamCheck;
	}

	public void turnOnBeamCheck() {
		beamCheck = true;
	}

	public void turnOffBeamCheck() {
		beamCheck = false;
	}

	public void useCirrus(boolean isUsed) {
		cirrusEnabled = isUsed;
	}

	public void acquireCirrus() {
		// TODO there is a better way of doing this now
		// from cirrus import ThreadClass
		// cirrus.setMasses([2, 28, 32])
		// t = ThreadClass(cirrus, energy_scannable, initial_energy, final_energy, "cirrus_scan.dat")
		// t.setName("cirrus")
		// t.start()
	}

	public void enableGMSD() {
		gmsd_enabled = true;
	}

	public void disableGMSD() {
		gmsd_enabled = false;
	}

	public void enableAdditionalChannels() {
		additional_channels_enabled = true;
	}

	public void disableAdditionalChannels() {
		additional_channels_enabled = false;
	}

}
