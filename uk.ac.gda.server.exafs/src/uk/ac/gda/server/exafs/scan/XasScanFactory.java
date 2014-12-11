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
	private EnergyScan energyScan;
	private QexafsScan qexafsScan;

	public XasScanFactory() {
	}

	public EnergyScan createEnergyScan() {

		if (energyScan != null){
			return energyScan;
		}

		checkSharedObjectsNonNull();

		checkDefined(energyScannable, "energyScannable");
		checkDefined(detectorPreparer, "detectorPreparer");

		energyScan = new EnergyScan();
		energyScan.setBeamlinePreparer(beamlinePreparer);
		energyScan.setDetectorPreparer(detectorPreparer);
		energyScan.setOutputPreparer(outputPreparer);
		energyScan.setSamplePreparer(samplePreparer);
		energyScan.setLoggingScriptController(loggingScriptController);
		energyScan.setDatawriterconfig(datawriterconfig);
		energyScan.setEnergyScannable(energyScannable);
		energyScan.setMetashop(metashop);
		energyScan.setIncludeSampleNameInNexusName(includeSampleNameInNexusName);
		energyScan.setScanName(scanName);
		placeInJythonNamespace(energyScan);
		return energyScan;
	}

	public QexafsScan createQexafsScan() {

		if (qexafsScan != null){
			return qexafsScan;
		}

		checkSharedObjectsNonNull();

		checkDefined(qexafsEnergyScannable, "qexafsEnergyScannable");
		checkDefined(qexafsDetectorPreparer, "qexafsDetectorPreparer");

		qexafsScan = new QexafsScan();
		qexafsScan.setBeamlinePreparer(beamlinePreparer);
		qexafsScan.setDetectorPreparer(qexafsDetectorPreparer);
		qexafsScan.setOutputPreparer(outputPreparer);
		qexafsScan.setSamplePreparer(samplePreparer);
		qexafsScan.setLoggingScriptController(loggingScriptController);
		qexafsScan.setDatawriterconfig(datawriterconfig);
		qexafsScan.setEnergyScannable(qexafsEnergyScannable);
		qexafsScan.setMetashop(metashop);
		qexafsScan.setIncludeSampleNameInNexusName(includeSampleNameInNexusName);
		qexafsScan.setScanName(scanName);
		placeInJythonNamespace(qexafsScan);
		return qexafsScan;
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
