/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.camera.beam;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.RealVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gda.device.DeviceException;
import gda.device.IScannableMotor;
import gda.device.scannable.iterator.ScannableIterator;
import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.diamond.daq.client.gui.camera.event.BeamCameraMappingEvent;
import uk.ac.diamond.daq.client.gui.camera.liveview.CameraImageComposite;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.composites.FinderHelper;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;

/**
 * Implements an automatic procedure to map the position of the motor driving
 * the beam to the camera pixels.
 * <p>
 * The class constructor {@link #BeamCameraMapping(String, String)} uses two
 * {@code String} to retrieve the two {@link IScannableMotor} controlling the
 * beam position.
 * </p>
 * <p>
 * Executing {@link #calibrate(ICameraConfiguration)}, the instance drives the
 * beam and at the same time listen to the stream connected with the camera
 * specified as parameter. The procedure ends when publishes, in Spring, a
 * {@link BeamCameraMappingEvent} on which {@link CameraImageComposite} is
 * listening.
 * </p>
 *
 *
 * @author Maurizio Nagni
 *
 */
@Component
public class BeamCameraMapping {

	private static final Logger logger = LoggerFactory.getLogger(BeamCameraMapping.class);

	private final String driverX;
	private final String driverY;

	private ScannableIterator beamX;
	private ScannableIterator beamY;

	private InitializationFailed initializationFailed = new InitializationFailed();

	@Autowired
	public BeamCameraMapping(@Value("${client.beam.position.driverX}") String driverX,
			@Value("${client.beam.position.driverY}") String driverY) {
		this.driverX = driverX;
		this.driverY = driverY;
	}

	/**
	 * Calculates the mapping for the {@code cameraConfiguration}
	 *
	 * @param cameraConfiguration
	 */
	public void calibrate(final ICameraConfiguration cameraConfiguration) {
		if (initializationFailed.reportErrorsToUser()) {
			return;
		}
		BeamCameraMappingHelper helper = new BeamCameraMappingHelper(beamX, beamY, cameraConfiguration);
		helper.calibrate();
	}

	void moveKB(RealVector kbVector) throws GDAClientException {
		try {
			beamX.forceToPosition(kbVector.getEntry(0));
			beamY.forceToPosition(kbVector.getEntry(1));
		} catch (OutOfRangeException | DeviceException e) {
			throw new GDAClientException("Cannot moves motors", e);
		}
	}

	@PostConstruct
	private void initialise() {
		FinderHelper.getIScannableMotor(driverX).ifPresent(this::setBeamX);
		FinderHelper.getIScannableMotor(driverY).ifPresent(this::setBeamY);
		initializationFailed.reportErrorsToLog();
	}

	private void setBeamX(IScannableMotor scannableMotor) {
		try {
			beamX = new ScannableIterator(scannableMotor);
		} catch (DeviceException e) {
			initializationFailed.addException(new GDAClientException("Failed to initalize ScannableIterator", e));
		}
	}

	private void setBeamY(IScannableMotor scannableMotor) {
		try {
			beamY = new ScannableIterator(scannableMotor);
		} catch (DeviceException e) {
			initializationFailed.addException(new GDAClientException("Failed to initalize ScannableIterator", e));
		}
	}

	private class InitializationFailed {
		private final List<GDAClientException> exceptions = new ArrayList<>();

		public void addException(GDAClientException exception) {
			exceptions.add(exception);
		}

		public boolean reportErrorsToUser() {
			if (!hasErrors()) {
				return false;
			}
			UIHelper.showError(ClientMessagesUtility.getMessage(ClientMessages.BEAM_CAMERA_MAPPING_ABORTED),
					"BeamCameraMapping not initialized");
			return true;
		}

		public void reportErrorsToLog() {
			if (hasErrors()) {
				exceptions.stream().forEach(e -> {
					logger.error("BeamCameraMapping not initialized", e);
				});
			}
		}

		private boolean hasErrors() {
			return !exceptions.isEmpty();
		}
	}
}
