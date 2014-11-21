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

import gda.device.scannable.ContinuouslyScannable;

public class XasScanFactory extends SpectroscopyScanFactory {

	private QexafsDetectorPreparer qexafsDetectorPreparer;
	private ContinuouslyScannable qexafsEnergyScannable;

	public XasScanFactory() {
	}

	public EnergyScan createEnergyScan() {

		checkSharedObjectsNonNull();

		checkDefined(energyScannable, "energyScannable");
		checkDefined(detectorPreparer, "detectorPreparer");

		EnergyScan newScan = new EnergyScan();
		newScan.setBeamlinePreparer(beamlinePreparer);
		newScan.setDetectorPreparer(detectorPreparer);
		newScan.setOutputPreparer(outputPreparer);
		newScan.setSamplePreparer(samplePreparer);
		newScan.setLoggingScriptController(loggingScriptController);
		newScan.setDatawriterconfig(datawriterconfig);
		newScan.setEnergyScannable(energyScannable);
		newScan.setMetashop(metashop);
		newScan.setIncludeSampleNameInNexusName(includeSampleNameInNexusName);
		newScan.setScanName(scanName);
		placeInJythonNamespace(newScan);
		return newScan;
	}

	public QexafsScan createQexafsScan() {

		checkSharedObjectsNonNull();

		checkDefined(qexafsEnergyScannable, "qexafsEnergyScannable");
		checkDefined(qexafsDetectorPreparer, "qexafsDetectorPreparer");

		QexafsScan newScan = new QexafsScan();
		newScan.setBeamlinePreparer(beamlinePreparer);
		newScan.setDetectorPreparer(qexafsDetectorPreparer);
		newScan.setOutputPreparer(outputPreparer);
		newScan.setSamplePreparer(samplePreparer);
		newScan.setLoggingScriptController(loggingScriptController);
		newScan.setDatawriterconfig(datawriterconfig);
		newScan.setEnergyScannable(qexafsEnergyScannable);
		newScan.setMetashop(metashop);
		newScan.setIncludeSampleNameInNexusName(includeSampleNameInNexusName);
		newScan.setScanName(scanName);
		placeInJythonNamespace(newScan);
		return newScan;
	}

	public ContinuouslyScannable getQexafsEnergyScannable() {
		return qexafsEnergyScannable;
	}

	public void setQexafsEnergyScannable(ContinuouslyScannable qexafsEnergyScannable) {
		this.qexafsEnergyScannable = qexafsEnergyScannable;
	}

	public QexafsDetectorPreparer getQexafsDetectorPreparer() {
		return qexafsDetectorPreparer;
	}

	public void setQexafsDetectorPreparer(QexafsDetectorPreparer qexafsDetectorPreparer) {
		this.qexafsDetectorPreparer = qexafsDetectorPreparer;
	}
}
