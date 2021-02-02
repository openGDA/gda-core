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

import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.getBean;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataListener;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.IScannableMotor;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.CameraStreamsManager;
import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.diamond.daq.client.gui.camera.beam.state.BeamMappingStateContext;
import uk.ac.diamond.daq.client.gui.camera.beam.state.BeamMappingStateContext.Outcome;
import uk.ac.diamond.daq.client.gui.camera.event.BeamCameraMappingEvent;
import uk.ac.diamond.daq.client.gui.camera.liveview.CameraImageComposite;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;
import uk.ac.gda.client.properties.camera.CameraToBeamMap;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.spring.MotorUtils;

/**
 * Implements an automatic procedure to map the position of the motor driving the beam to the camera pixels.
 * <p>
 * Uses two {@code ScannableIterator} to retrieve the two {@link IScannableMotor} controlling the beam position.
 * </p>
 * <p>
 * Executing {@link #calibrate()}, the instance drives the beam and at the same time listen to the stream connected with
 * the camera specified as parameter. The procedure ends when publishes, in Spring, a {@link BeamCameraMappingEvent} on
 * which {@link CameraImageComposite} is listening.
 * </p>
 *
 *
 * @author Maurizio Nagni
 *
 */
public class BeamCameraMappingHelper {

	private static final Logger logger = LoggerFactory.getLogger(BeamCameraMappingHelper.class);

	private LiveStreamConnection liveStream;
	private IDataListener iDataListener;

	private final BeamMappingStateContext context;
	private final ScannableIterator beamX;
	private final ScannableIterator beamY;

	private MappingStatus sc;

	/**
	 * Creates a instance to handle a new beam camera mapping process
	 * @param context the context for the mapping process
	 * @param beamX the beam driver for the X axis
	 * @param beamY the beam driver for the X axis
	 */
	public BeamCameraMappingHelper(BeamMappingStateContext context, ScannableIterator beamX, ScannableIterator beamY) {
		this.context = context;
		this.beamX = beamX;
		this.beamY = beamY;
	}

	/**
	 * Calculates the mapping for the {@code cameraConfiguration}
	 */
	public void calibrate() {
		if (!context.getCameraConfiguration().isPresent()) {
			return;
		}

		// advance the state to START
		context.start();
		context.getCameraConfiguration()
			.ifPresent(this::initialise);
		// starts the mapping
		try {
			// The real mapping process
			scanArea();
			// Estimates the transformation matrix from the camera space (x,y) pixels to the beam drivers one (kb_x, kb_y)
			// If the transformation exists, update the related camera configuration to make this available to the rest of the application
			RealMatrix transformation = sc.estimateMatrix();
			if (transformation != null) {
				updateCameraConfiguration(transformation);
				context.setOutcome(Outcome.SUCCESS);
			} else {
				context.setOutcome(Outcome.FAILED);
			}
		} catch (GDAClientException e) {
			UIHelper.showError("Cannot calibrate camera to motors", e, logger);
		} finally {
			// Release the resources
			terminateMapping();
			// advance the state to STOP
			context.stop();
		}
	}

	private void initialise(ICameraConfiguration cameraConfiguration) {
			cameraConfiguration.getCameraConfiguration()
				.ifPresent(this::initializeStream);
			sc = new MappingStatus();
	}

	private void initializeStream(CameraConfiguration cc) {
		// Uses the same stream for all the configured cameras
		liveStream = getBean(CameraStreamsManager.class).getStreamConnection(cc, StreamType.EPICS_ARRAY);
	}

	private void updateCameraConfiguration(RealMatrix transformation) {
		CameraToBeamMap cameraToBeamMap = new CameraToBeamMap();
		cameraToBeamMap.setMap(transformation.getData());
		cameraToBeamMap.setDriver(new ArrayList<>());
		cameraToBeamMap.getDriver().add(beamX.getScannableName());
		cameraToBeamMap.getDriver().add(beamY.getScannableName());
		cameraToBeamMap.setActive(true);
		context.getCameraConfiguration()
			.ifPresent(cc -> CameraHelper.addBeamCameraMap(cc, cameraToBeamMap));
	}

	private void analyzeImage() {
		try {
			ILazyDataset ld = liveStream.getStream().getDataset();
			ArrayRealVector position = calculateMomentum(ld);
			Point2D newPoint = new Point2D.Double(position.getEntry(0), position.getEntry(1));
			IDataset dataset = ld.getSlice();
			// Set a lower threshold for the beam brightness. May be will be parametrised in
			// future
			int threshold = 1;
			// if lower that threshold means the beam is out of the camera field of view
			if (dataset.getInt((int) position.getEntry(1), (int) position.getEntry(0)) <= threshold) {
				return;
			}
			sc.addCenteringPoints(new BeamCameraPoint(newPoint,
					new Point2D.Double(beamX.getDriverPosition(), beamY.getDriverPosition())));
		} catch (DatasetException | GDAClientException e) {
			abortMapping(e);
		}
	}

	private ArrayRealVector calculateMomentum(ILazyDataset ld) throws DatasetException {
		long intensity = 0;
		ArrayRealVector position = new ArrayRealVector(new double[] { 0, 0 });
		int[] maxPos = ld.getSlice().maxPos();
		if (maxPos[1] + maxPos[0] == 0) {
			return position;
		}
		int squareSize = 5;
		for (int indexY = -1 * squareSize; indexY < squareSize; indexY++) {
			int tempY = maxPos[0] + indexY;
			for (int indexX = -1 * squareSize; indexX < squareSize; indexX++) {
				int tempX = maxPos[1] + indexX;
				if (tempX < 0 || tempY < 0) {
					continue;
				}
				ArrayRealVector tempVector = new ArrayRealVector(new double[] { tempX, tempY });
				long tempIntensity = ld.getSlice().getInt(tempY, tempX);
				tempVector.mapMultiplyToSelf(tempIntensity);
				position = position.add(tempVector);
				intensity = intensity + tempIntensity;
			}
		}
		if (intensity != 0) {
			position.mapDivideToSelf(intensity);
		}
		return position;
	}

	private void terminateMapping() {
		Optional.ofNullable(iDataListener).ifPresent(liveStream::removeDataListenerFromStream);
	}

	private void abortMapping(Exception e) {
		terminateMapping();
		UIHelper.showError(ClientMessagesUtility.getMessage(ClientMessages.BEAM_CAMERA_MAPPING_ABORTED), e);
		logger.error(ClientMessagesUtility.getMessage(ClientMessages.BEAM_CAMERA_MAPPING_ABORTED), e);
	}

	/**
	 * The status of the mapping procedure is defined by this class. When a motor is
	 * moved, the data listener uses this object to know the status of the
	 * processing and set the next one (see findTargetNew(newPoint))
	 */
	private class MappingStatus {
		private Set<BeamCameraPoint> mappedPoints = new HashSet<>();

		private BeamCameraPoint p0;
		private BeamCameraPoint p1;
		private double p0p1Distance;

		public MappingStatus() {
			super();
		}

		public void addCenteringPoints(BeamCameraPoint beamCameraPoint) {
			mappedPoints.add(beamCameraPoint);
			p0 = Optional.ofNullable(p0).orElse(beamCameraPoint);
			double newDistance = p0.getArrayPosition().distance(beamCameraPoint.getArrayPosition());
			if (newDistance >= p0p1Distance) {
				p0p1Distance = newDistance;
				p1 = beamCameraPoint;
			}
		}

		public RealMatrix estimateMatrix() {
			if (mappedPoints.size() != 2) {
				return null;
			}

			double p0x = p0.getArrayPosition().getX();
			double p0y = p0.getArrayPosition().getY();
			double k0x = p0.getDriverPosition().getX();
			double k0y = p0.getDriverPosition().getY();

			double p1x = p1.getArrayPosition().getX();
			double p1y = p1.getArrayPosition().getY();
			double k1x = p1.getDriverPosition().getX();
			double k1y = p1.getDriverPosition().getY();

			double[][] dmatrix = new double[2][2];

			dmatrix[0][0] = (k1x * p0y - k0x * p1y) / (p1x * p0y - p0x * p1y);
			dmatrix[1][1] = (k1y * p0x - k0y * p1x) / (p1y * p0x - p0y * p1x);
			dmatrix[0][1] = (k0x - dmatrix[0][0] * p0x) / p0y;
			dmatrix[1][0] = (k0y - dmatrix[1][1] * p0y) / p0x;

			RealMatrix transformation = new Array2DRowRealMatrix(dmatrix);
			double[] kb = transformation.operate(new double[] { p0x, p0y });
			logger.debug("p0: {} {}", p0x, p0y);
			logger.debug("KB: {} {}", k0x, k0y);
			logger.debug("kb: {}", kb);

			// assures the beam is visible at the end of the calibration moving the beam at the centre of the mapped
			// allowed area, which, depending on the beam drivers ranges mapping, may be smaller, equal or bigger than the camera array
			getBean(MotorUtils.class).moveMotorAsynchronously(beamX.getScannableName(), (k0x + k1x) / 2);
			getBean(MotorUtils.class).moveMotorAsynchronously(beamY.getScannableName(), (k0y + k1y) / 2);

			return new Array2DRowRealMatrix(dmatrix);
		}
	}

	private void backwardX() throws GDAClientException {
		// moves backward through all X
		while (beamX.hasPrevious()) {
			isInterrupted();
			beamX.previous();
			estimateTimeToComplete();
			analyzeImage();
		}
		if (!beamY.hasNext()) {
			return;
		}
		isInterrupted();
		// moves to the next Y
		beamY.next();
		// There is no need to analyse the image because the beamX.next() will be equal
		// to the last beamX.previous()
	}

	private void forwardX() throws GDAClientException {
		// moves forward through all X
		while (beamX.hasNext()) {
			isInterrupted();
			beamX.next();
			estimateTimeToComplete();
			analyzeImage();
		}
		if (!beamY.hasNext()) {
			return;
		}
		isInterrupted();
		// moves to the next Y
		beamY.next();
		// There is no need to analyse the image because the beamX.previous() will be
		// equal to the last beamX.next()
	}

	private void scanArea() throws GDAClientException {
		// Moves and analyses the starting point
		if (beamY.hasNext() && beamX.hasNext()) {
			isInterrupted();
			beamX.next();
			beamY.next();
			estimateTimeToComplete();
			analyzeImage();
		}

		while (beamY.hasNext()) {
			forwardX();
			backwardX();
		}
	}

	private void isInterrupted() throws GDAClientException {
		if (Thread.currentThread().isInterrupted()) {
			throw new GDAClientException("Interrupt mapping");
		}
	}

	private void estimateTimeToComplete() {
		// TBD
		// SpringApplicationContextProxy.publishEvent(new
		// BeamMappingEstimateTimeEvent(this, pos));
	}
}
