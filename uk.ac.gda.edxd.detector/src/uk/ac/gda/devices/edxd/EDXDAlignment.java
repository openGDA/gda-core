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

package uk.ac.gda.devices.edxd;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.JythonServerFacade;
import uk.ac.gda.edxd.calibration.edxdcalibration.COLLIMATOR;
import uk.ac.gda.edxd.calibration.edxdcalibration.CalibrationConfig;
import uk.ac.gda.edxd.calibration.edxdcalibration.DocumentRoot;
import uk.ac.gda.edxd.calibration.edxdcalibration.EdxdCalibration;
import uk.ac.gda.edxd.calibration.edxdcalibration.HUTCH;
import uk.ac.gda.edxd.calibration.edxdcalibration.util.EdxdCalibrationResourceHandler;
import uk.ac.gda.edxd.common.IEdxdAlignment;

public class EDXDAlignment implements IEdxdAlignment {
	private static final Logger logger = LoggerFactory.getLogger(EDXDAlignment.class);

	private EdxdCalibrationResourceHandler edxdCalibrationResourceHandler;

	private AdDetectorExtRoiDraw eh1;

	private AdDetectorExtRoiDraw eh2;

	public void setEdxdCalibrationResourceHandler(EdxdCalibrationResourceHandler edxdCalibrationResourceHandler) {
		this.edxdCalibrationResourceHandler = edxdCalibrationResourceHandler;
	}

	@Override
	public String getLastSavedEnergyCalibrationFile() {
		EdxdCalibration edxdCalibrationConfiguration = getEdxdCalibrationConfiguration();
		if (edxdCalibrationConfiguration != null) {
			CalibrationConfig energyCalibration = edxdCalibrationConfiguration.getEnergyCalibration();
			if (energyCalibration != null) {
				return energyCalibration.getFileName();
			}
		}
		return null;
	}

	@Override
	public String getLastSaveEnergyCalibrationDateTime() {
		EdxdCalibration edxdCalibrationConfiguration = getEdxdCalibrationConfiguration();
		if (edxdCalibrationConfiguration != null) {
			CalibrationConfig energyCalibration = edxdCalibrationConfiguration.getEnergyCalibration();
			if (energyCalibration != null) {
				return energyCalibration.getLastCalibrated();
			}
		}
		return null;
	}

	@Override
	public String getLastSavedQCalibrationFile() {
		EdxdCalibration edxdCalibrationConfiguration = getEdxdCalibrationConfiguration();
		if (edxdCalibrationConfiguration != null) {
			CalibrationConfig qCalibration = edxdCalibrationConfiguration.getQCalibration();
			if (qCalibration != null) {
				return qCalibration.getFileName();
			}
		}
		return null;
	}

	@Override
	public String getLastSaveQCalibrationDateTime() {
		EdxdCalibration edxdCalibrationConfiguration = getEdxdCalibrationConfiguration();
		if (edxdCalibrationConfiguration != null) {
			CalibrationConfig qCalibration = edxdCalibrationConfiguration.getQCalibration();
			if (qCalibration != null) {
				return qCalibration.getLastCalibrated();
			}
		}
		return null;
	}

	@Override
	public String getLastSavedHutch() {
		EdxdCalibration edxdCalibrationConfiguration = getEdxdCalibrationConfiguration();
		if (edxdCalibrationConfiguration != null) {
			HUTCH hutch = edxdCalibrationConfiguration.getHutch();
			if (hutch != null) {
				return hutch.getName();
			}
		}
		return null;
	}

	@Override
	public String getLastSavedCollimator() {
		EdxdCalibration edxdCalibrationConfiguration = getEdxdCalibrationConfiguration();
		if (edxdCalibrationConfiguration != null) {
			COLLIMATOR collimator = edxdCalibrationConfiguration.getCollimator();
			if (collimator != null) {
				return collimator.getName();
			}
		}
		return null;
	}

	@Override
	public void runPreampGain() {
		logger.debug("Request to run preamp gain");
		JythonServerFacade.getInstance().runCommand(
				"run('edxd_calibrator_v3');ref=refinement();ref.calibrate(20.0, 22., 24., 22.4, 0.001);");
	}

	@Override
	public String runEnergyCalibration() {
		logger.debug("Request to run energy calibration");
		return null;
	}

	@Override
	public void runDetectorXYAlignment() {
		logger.debug("Request to run detector XY Alignment");
		JythonServerFacade.getInstance().runCommand("run('EDXD/edxdAlignment');testMisAlignmentForElementsPlot();");
	}

	@Override
	public void runCollimatorXYZAlignment() {
		logger.debug("Request to run collimator XYZ Alignment");
		JythonServerFacade.getInstance().runCommand("run('EDXD/edxdAlignment');testCollimatorXyzAlignment();");
	}

	@Override
	public void runCollimatorAngularAlignment() {
		logger.debug("Request to run collimator Angular Alignment");
		JythonServerFacade.getInstance().runCommand("run('EDXD/edxdAlignment');runCollimatorAngularAlignment();");
	}

	@Override
	public String runQAxisCalibration() {
		JythonServerFacade.getInstance().runCommand("run('EDXD/edxdAlignment');runQAxisCalibration();");
		return null;
	}

	@Override
	public void loadEnergyCalibrationFile(String fileName) {
		JythonServerFacade.getInstance().runCommand("run('EDXD/edxdAlignment');loadEnergyCalibrationFile();");
	}

	@Override
	public void loadQCalibrationFile(String fileName) {
		JythonServerFacade.getInstance().runCommand("run('EDXD/edxdAlignment');loadQCalibrationFile();");
	}

	private EdxdCalibration getEdxdCalibrationConfiguration() {
		EdxdCalibration calibration = null;
		if (edxdCalibrationResourceHandler != null) {
			Resource resource = edxdCalibrationResourceHandler.getResource(true);

			EObject eObject = resource.getContents().get(0);
			if (eObject instanceof DocumentRoot) {
				DocumentRoot dr = (DocumentRoot) eObject;
				calibration = dr.getEdxdCalibration().get(0);
			}
		}
		return calibration;
	}

	@Override
	public String getEh1MpegUrl() throws Exception {
		if (eh1 != null) {
			return eh1.getMjpeg1().getMJPG_URL_RBV();
		}
		return null;
	}

	@Override
	public String getEh2MpegUrl() throws Exception {
		if (eh2 != null) {
			return eh2.getMjpeg1().getMJPG_URL_RBV();
		}
		return null;
	}

	public void setEh1(AdDetectorExtRoiDraw eh1) {
		this.eh1 = eh1;
	}

	public void setEh2(AdDetectorExtRoiDraw eh2) {
		this.eh2 = eh2;
	}

	@Override
	public void startEh1Camera() throws Exception {
		eh1.getAdBase().startAcquiring();
	}

	@Override
	public void startEh2Camera() throws Exception {
		eh2.getAdBase().startAcquiring();
	}
}
