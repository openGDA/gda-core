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
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.math3.linear.RealVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.IScannableMotor;
import uk.ac.diamond.daq.client.gui.camera.beam.state.BeamMappingStateContext;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.spring.FinderService;
import uk.ac.gda.ui.tool.spring.MotorUtils;

/**
 * Implements an automatic procedure to map the position of the motor driving
 * the beam to the camera pixels.
 * <p>
 * The class constructor {@link #BeamCameraMapping(String, String)} uses two
 * {@code String} to retrieve the two {@link IScannableMotor} controlling the
 * beam position.
 * </p>
 * @author Maurizio Nagni
 *
 */
public class BeamCameraMapping {

	private static final Logger logger = LoggerFactory.getLogger(BeamCameraMapping.class);

	private final String driverX;
	private final String driverY;

	private ScannableIterator beamX;
	private ScannableIterator beamY;

	private InitializationFailed initializationFailed;

	public BeamCameraMapping(String driverX, String driverY) {
		this.driverX = driverX;
		this.driverY = driverY;
	}

	/**
	 * Calculates the mapping for the {@code cameraConfiguration}. If fails displays
	 * an error dialog with a short description of the problem.
	 *
	 * @param context
	 */
	public void calibrate(final BeamMappingStateContext context) {
		initialise(context.getxSamplePoints(), context.getySamplePoints());
		if (!initializationFailed.reportErrorsToUser()) {
			var helper = new BeamCameraMappingHelper(context, beamX, beamY);
			helper.calibrate();
		}
	}

	/**
	 * Moves the beam drivers.
	 *
	 * @param kbVector a (pos_x, pos_y) vector describing the required position for the motors
	 */
	public void moveKB(RealVector kbVector) {
		moveBeam(beamX, kbVector.getEntry(0));
		moveBeam(beamY, kbVector.getEntry(1));
	}

	private void moveBeam(ScannableIterator scannableIterator, double newPosition) {
		if (scannableIterator != null) {
			getMotorUtils().moveMotorAsynchronously(scannableIterator.getScannableName(), newPosition);
		}
	}

	/**
	 * Set the resolution to map the camera.
	 *
	 * <p>
	 * A lower resolution will result in a faster mapping but less accurate.
	 * </p>
	 * @param xSteps the number of steps on the x axis
	 * @param ySteps the number of steps on the y axis
	 */
	private void initialise(int xSteps, int ySteps) {
		initializationFailed = new InitializationFailed();
		initializeAxis(driverX, this::setBeamX, xSteps);
		initializeAxis(driverY, this::setBeamY, ySteps);
		initializationFailed.reportErrorsToLog();
	}

	private void initializeAxis(String axis, Consumer<ScannableIterator> consumer, int steps) {
		getIScannableMotor(axis)
			.map(ScannableIterator::new)
			.ifPresent(b -> {
				consumer.accept(b);
				initialiseScannableIterator(steps).accept(b);
			});
	}

	private Consumer<ScannableIterator> initialiseScannableIterator(int steps) {
		return b -> {
			try {
				b.setSteps(steps);
			} catch (DeviceException e) {
				initializationFailed.addException(new GDAClientException("Failed to initalize ScannableIterator", e));
			}
		};
	}

	private void setBeamX(ScannableIterator scannableIterator) {
		beamX = scannableIterator;
	}

	private void setBeamY(ScannableIterator scannableIterator) {
		beamY = scannableIterator;
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
				exceptions.stream().forEach(e ->
					logger.error("BeamCameraMapping not initialized", e)
				);
			}
		}

		private boolean hasErrors() {
			return !exceptions.isEmpty();
		}
	}

	/**
	 * The motor moving the beam on the X axis
	 * @return the motor name, otherwise {@code emmpty()} if the scanable has not been found
	 */
	public Optional<String> getMotorXName() {
		// when a ClientContext (K11-633) will be available should be update from there
		return Optional.ofNullable(beamX)
				.map(ScannableIterator::getScannableName);
	}

	/**
	 * The motor moving the beam on the Y axis
	 * @return the motor name, otherwise {@code emmpty()} if the scanable has not been found
	 */
	public Optional<String> getMotorYName() {
		// when a ClientContext (K11-633) will be available should be update from there
		return Optional.ofNullable(beamY)
				.map(ScannableIterator::getScannableName);
	}

	private MotorUtils getMotorUtils() {
		return SpringApplicationContextFacade.getBean(MotorUtils.class);
	}

	private Optional<IScannableMotor> getIScannableMotor(String findableMotor) {
		return SpringApplicationContextFacade.getBean(FinderService.class)
				.getFindableObject(findableMotor, IScannableMotor.class);
	}
}
