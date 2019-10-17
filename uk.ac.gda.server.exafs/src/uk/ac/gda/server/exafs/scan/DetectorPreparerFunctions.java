/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import java.io.File;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.xmap.NexusXmapFluorescenceDetectorAdapter;
import gda.device.detector.xmap.Xmap;
import gda.device.detector.xspress.Xspress2Detector;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import uk.ac.gda.beans.exafs.IonChamberParameters;
import uk.ac.gda.devices.detector.FluorescenceDetector;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.devices.detector.xspress3.Xspress3Detector;
import uk.ac.gda.devices.detector.xspress4.Xspress4Detector;
import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * Functionality common across {@link DetectorPreparer} implementations for different beamlines.
 */
public class DetectorPreparerFunctions {
	private static final Logger logger = LoggerFactory.getLogger(DetectorPreparerFunctions.class);

	private Scannable[] sensitivities;
	private Scannable[] sensitivityUnits;
	private Scannable[] offsets;
	private Scannable[] offsetUnits;

	public DetectorPreparerFunctions() {
		// nothing to do here
	}

	/**
	 * Load a {@link FluorescenceDetectorParameters} bean from an XML file, and apply it to the detector.
	 * @param xmlFileName
	 * @throws Exception
	 */
	public Detector configureDetector(String xmlFileName) throws Exception {
		FluorescenceDetectorParameters params = getDetectorParametersBean(xmlFileName);
		Detector det = configureDetector(params);
		setConfigFilename(det, xmlFileName);
		return det;
	}

	public FluorescenceDetectorParameters getDetectorParametersBean(String xmlFileName) throws Exception {
		return XMLHelpers.readBean(new File(xmlFileName), FluorescenceDetectorParameters.class);
	}

	public void setConfigFilename(Detector det, String xmlFilename) {
		// setConfigFileName should be added to the interface...
		if (det instanceof Xspress2Detector) {
			((Xspress2Detector) det).setConfigFileName(xmlFilename);
		} else if (det instanceof Xspress3Detector) {
			((Xspress3Detector) det).setConfigFileName(xmlFilename);
		} else if (det instanceof Xspress4Detector) {
			((Xspress4Detector) det).setConfigFileName(xmlFilename);
		} else if (det instanceof Xmap) {
			((Xmap)det).setConfigFileName(xmlFilename);
		}
	}

	/**
	 * Apply {@link FluorescenceDetectorParameters} to the detector named by {@link FluorescenceDetectorParameters#getDetectorName()}.
	 * @param params
	 * @throws Exception if named detector could not be found
	 */
	public Detector configureDetector(FluorescenceDetectorParameters params) throws Exception {
		String detName = params.getDetectorName();
		Optional<FluorescenceDetector> det = Finder.getInstance().findOptional(detName);
		if (!det.isPresent()) {
			throw new NoSuchElementException("Unable to find detector called "+detName+" on server\n");
		}
		det.get().applyConfigurationParameters(params);

		// For Xmap, return NexusXmap object since is this detector actually used for scans.
		if (det.get() instanceof NexusXmapFluorescenceDetectorAdapter) {
			return ((NexusXmapFluorescenceDetectorAdapter)det.get()).getXmap();
		}
		return (Detector) det.get();
	}

	public void setupAmplifierSensitivity(IonChamberParameters ionChamberParams, int index) throws DeviceException {
		if (!ionChamberParams.getChangeSensitivity()) {
			return;
		}

		if (ionChamberParams.getGain() == null || ionChamberParams.getGain().isEmpty()) {
			return;
		}

		try {
			showAndLogMessage("Changing sensitivity of " + ionChamberParams.getName() + " to " + ionChamberParams.getGain());
			String[] gainStringParts = ionChamberParams.getGain().split(" ");
			moveAmplifierScannable(sensitivities, index, gainStringParts[0]);
			moveAmplifierScannable(sensitivityUnits, index, gainStringParts[1]);

			if (offsets != null && offsetUnits != null) {
				showAndLogMessage("Changing offset of " + ionChamberParams.getName() + " to " + ionChamberParams.getOffset());
				String[] offsetStringParts = ionChamberParams.getOffset().split(" ");
				moveAmplifierScannable(offsets, index, offsetStringParts[0]);
				moveAmplifierScannable(offsetUnits, index, offsetStringParts[1]);
			}
		} catch (Exception e) {
			InterfaceProvider.getTerminalPrinter().print(
					"Exception while trying to change the sensitivity of ion chamber" + ionChamberParams.getName());
			InterfaceProvider.getTerminalPrinter().print(
					"Set the ion chamber sensitivity manually, uncheck the box in the Detector Parameters editor and restart the scan");
			InterfaceProvider.getTerminalPrinter().print("Please report this problem to Data Acquisition");
			throw e;
		}
	}

	private void showAndLogMessage(String message) {
		InterfaceProvider.getTerminalPrinter().print(message);
		logger.info(message);
	}

	private void moveAmplifierScannable(Scannable[] scnArray, int index, Object position) throws DeviceException {
		if (scnArray == null || scnArray.length < index) {
			return;
		}
		logger.info("Moving {} to {}", scnArray[index].getName(), position);
		scnArray[index].moveTo(position);
	}

	public Scannable[] getSensitivities() {
		return sensitivities;
	}

	public void setSensitivities(Scannable[] sensitivities) {
		this.sensitivities = sensitivities;
	}

	public Scannable[] getSensitivityUnits() {
		return sensitivityUnits;
	}

	public void setSensitivityUnits(Scannable[] sensitivityUnits) {
		this.sensitivityUnits = sensitivityUnits;
	}

	public Scannable[] getOffsets() {
		return offsets;
	}

	public void setOffsets(Scannable[] offsets) {
		this.offsets = offsets;
	}

	public Scannable[] getOffsetUnits() {
		return offsetUnits;
	}

	public void setOffsetUnits(Scannable[] offsetUnits) {
		this.offsetUnits = offsetUnits;
	}
}
