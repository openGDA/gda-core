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

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import gda.data.metadata.NXMetaDataProvider;
import gda.device.Scannable;
import gda.jython.IJythonNamespace;
import gda.jython.InterfaceProvider;
import gda.jython.scriptcontroller.logging.LoggingScriptController;

public abstract class SpectroscopyScanFactory {

	protected BeamlinePreparer beamlinePreparer;
	protected DetectorPreparer detectorPreparer;
	protected OutputPreparer outputPreparer;
	protected SampleEnvironmentPreparer samplePreparer;
	protected LoggingScriptController loggingScriptController;
	protected Scannable energyScannable;
	protected NXMetaDataProvider metashop;
	protected boolean includeSampleNameInNexusName;
	protected String scanName;

	private XasScanBase scan;

	protected void checkSharedObjectsNonNull() {

		checkDefined(beamlinePreparer, "beamlinePreparer");
		checkDefined(samplePreparer, "samplePreparer");
		checkDefined(outputPreparer, "outputPreparer");
		checkDefined(loggingScriptController, "LoggingScriptController");
		checkDefined(metashop, "metashop");
		checkDefined(includeSampleNameInNexusName, "includeSampleNameInNexusName");
		checkDefined(scanName, "scanName");
	}

	protected void checkDefined(Object objectToTest, String objectname) {
		if (objectToTest == null) {
			throw new IllegalArgumentException(objectname + " is not defined!");
		}
	}

	public BeamlinePreparer getBeamlinePreparer() {
		return beamlinePreparer;
	}

	public void setBeamlinePreparer(BeamlinePreparer beamlinePreparer) {
		this.beamlinePreparer = beamlinePreparer;
	}

	public DetectorPreparer getDetectorPreparer() {
		return detectorPreparer;
	}

	public void setDetectorPreparer(DetectorPreparer detectorPreparer) {
		this.detectorPreparer = detectorPreparer;
	}

	public OutputPreparer getOutputPreparer() {
		return outputPreparer;
	}

	public void setOutputPreparer(OutputPreparer outputPreparer) {
		this.outputPreparer = outputPreparer;
	}

	public SampleEnvironmentPreparer getSamplePreparer() {
		return samplePreparer;
	}

	public void setSamplePreparer(SampleEnvironmentPreparer samplePreparer) {
		this.samplePreparer = samplePreparer;
	}

	public LoggingScriptController getLoggingScriptController() {
		return loggingScriptController;
	}

	public void setLoggingScriptController(LoggingScriptController loggingScriptController) {
		this.loggingScriptController = loggingScriptController;
	}

	public Scannable getEnergyScannable() {
		return energyScannable;
	}

	public void setEnergyScannable(Scannable energyScannable) {
		this.energyScannable = energyScannable;
	}

	public NXMetaDataProvider getMetashop() {
		return metashop;
	}

	public void setMetashop(NXMetaDataProvider metashop) {
		this.metashop = metashop;
	}

	public boolean isIncludeSampleNameInNexusName() {
		return includeSampleNameInNexusName;
	}

	public void setIncludeSampleNameInNexusName(boolean includeSampleNameInNexusName) {
		this.includeSampleNameInNexusName = includeSampleNameInNexusName;
	}

	public String getScanName() {
		return scanName;
	}

	public void setScanName(String scanName) {
		this.scanName = scanName;
	}

	protected void placeInJythonNamespace(XasScanBase theScan) {
		scan = theScan;
		FutureTask<Void> placeInJythonTask = new FutureTask<Void>(new Callable<Void>() {
		@Override
		public Void call() {
//			try for 10 secs and give up
			for (int i=0; i<10; i++) {
				try {
					Thread.sleep(1000);
					IJythonNamespace jythonNamespace = InterfaceProvider.getJythonNamespace();
					jythonNamespace.placeInJythonNamespace(scanName, scan);
					return null;
				} catch (Exception e) {
					// ignore
				}
			}
			throw new IllegalArgumentException("Failed to put scan '" + scanName + "' into the Jython namespace!");
		}
	});

	new Thread(placeInJythonTask, "placeEnergyScanIntoJythonNamespace").start();
	}
}
