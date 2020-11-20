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

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataListener;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.IScannableMotor;
import gda.device.scannable.iterator.ScannableIterator;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.diamond.daq.client.gui.camera.event.BeamCameraMappingEvent;
import uk.ac.diamond.daq.client.gui.camera.liveview.CameraImageComposite;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.LiveStreamConnectionManager;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;

/**
 * Implements an automatic procedure to map the position of the motor driving the beam to the camera pixels.
 * <p>
 * The class constructor {@link #BeamCameraMappingHelper(ScannableIterator, ScannableIterator, ICameraConfiguration)}
 * uses two {@code ScannableIterator} to retrieve the two {@link IScannableMotor} controlling the beam position.
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

	private final ICameraConfiguration cameraConfiguration;
	private final ScannableIterator beamX;
	private final ScannableIterator beamY;

	private MappingStatus sc;

	public BeamCameraMappingHelper(ScannableIterator beamX, ScannableIterator beamY,
			ICameraConfiguration cameraConfiguration) {
		this.beamX = beamX;
		this.beamY = beamY;
		this.cameraConfiguration = cameraConfiguration;
	}

	/**
	 * Calculates the mapping for the {@code cameraConfiguration}
	 */
	public void calibrate() {
		initialise();
		// starts the mapping
		scanForBeam();
	}

	private void scanForBeam() {
		// FIRST STEP: moves the beam to find any beam spot on the array (the beam may
		// be out of the camera field of view)
		sc = new MappingStatus();
		try {
			moveMirrors();
		} catch (DeviceException e) {
			sc = null;
			UIHelper.showError("Cannot move motors", e);
			logger.error("Cannot move motors", e);
		}
	}

	private void initialise() {
		cameraConfiguration.getCameraConfiguration().ifPresent(this::initializeStream);
	}

	private void initializeStream(CameraConfiguration cc) {
		try {
			UUID streamConnection = LiveStreamConnectionManager.getInstance().getIStreamConnection(cc,
					StreamType.EPICS_ARRAY);
			liveStream = (LiveStreamConnection) LiveStreamConnectionManager.getInstance()
					.getIStreamConnection(streamConnection);
			// This is essental to make the instance aware of any change in the camera
			// stream
			iDataListener = getDataListener();
			liveStream.addDataListenerToStream(iDataListener);
		} catch (LiveStreamException e) {
			logger.error("Could not initialize stream", e);
		}
	}

	private void scanForBeamInCamera(Point2D newPoint, IDataset dataset) throws DeviceException {
		sc.addCenteringPoints(dataset, new BeamCameraPoint(newPoint,
				new Point2D.Double(beamX.getDriverPosition(), beamY.getDriverPosition())));

		if (!sc.isCompleted()) {
			moveMirrors();
		} else if (sc.isCompleted()) {
			sc.estimateMatrix().ifPresent(this::updateCameraConfiguration);
			terminateMapping();
		}
	}

	private void moveMirrors() throws DeviceException {
		beamX.moveToRandom();
		// Once a point is found, moving a single axes reduces the searching time
		if (sc.mappedPoints.size() < 1) {
			beamY.moveToRandom();
		}
		sc.setArmed(true);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void updateCameraConfiguration(RealMatrix transformation) {
		CameraHelper.addBeamCameraMap(cameraConfiguration.getCameraIndex(), new BeamCameraMap(transformation,
				cameraConfiguration, beamX.getScannableName(), beamY.getScannableName()));
		terminateMapping();
	}

	// This is central to the whole procedure: Listens to the live stream and
	// calculate the position pixel of max intensity calculating the momentum around
	// the pixel.
	private IDataListener getDataListener() {
		return evt -> {
			try {
				if (sc == null || beamX.isBusy() || beamY.isBusy() || !sc.isArmed()) {
					return;
				}
			} catch (DeviceException e) {
				logger.error("Device Error", e);
				return;
			}
			analyzeImage();
		};
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
			if (dataset.getInt((int) position.getEntry(1), (int) position.getEntry(0)) < threshold) {
				moveMirrors();
				return;
			}
			if (!sc.containCameraPoint(dataset)) {
				// we have a point let's check it
				scanForBeamInCamera(newPoint, dataset);
			} else {
				moveMirrors();
			}
		} catch (DatasetException | DeviceException e) {
			abortMapping(e);
		}
	}

	private ArrayRealVector calculateMomentum(ILazyDataset ld) throws DatasetException {
		long intensity = 0;
		ArrayRealVector position = new ArrayRealVector(new double[] { 0, 0 });
		int[] maxPos = ld.getSlice().maxPos();
		for (int indexY = -5; indexY < 5; indexY++) {
			for (int indexX = -5; indexX < 5; indexX++) {
				int tempX = maxPos[1] + indexX;
				int tempY = maxPos[0] + indexY;
				if (tempX < 0 || tempY < 0) {
					continue;
				}
				ArrayRealVector tempVector = new ArrayRealVector(new double[] { tempX, tempY });
				long tempIntensity = ld.getSlice().getInt(tempY, tempX) + 128;
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
		sc = null;
		liveStream.removeDataListenerFromStream(iDataListener);
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
		private Map<IDataset, BeamCameraPoint> mappedPoints = new HashMap<>();

		// When no motor has been moved, this flag allows the data listener
		// (getDataListener()) to skip the frame
		private boolean armed = false;

		public MappingStatus() {
			super();
		}

		public void addCenteringPoints(IDataset dataset, BeamCameraPoint beamCameraPoint) {
			if (mappedPoints.containsKey(dataset)) {
				return;
			}
			mappedPoints.put(dataset, beamCameraPoint);
		}

		public boolean containCameraPoint(IDataset dataset) {
			return mappedPoints.containsKey(dataset);
		}

		public boolean isCompleted() {
			return mappedPoints.size() == 2;
		}

		public boolean isArmed() {
			return armed;
		}

		public void setArmed(boolean armed) {
			this.armed = armed;
		}

		public Optional<RealMatrix> estimateMatrix() {
			if (Objects.isNull(mappedPoints) || mappedPoints.size() < 2) {
				return Optional.empty();
			}
			BeamCameraPoint[] points = mappedPoints.values().toArray(new BeamCameraPoint[0]);
			BeamCameraPoint p0 = points[0];
			BeamCameraPoint p1 = points[1];

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

//			kb = transformation.operate(new double[] { 13.0, 284 });
//			logger.debug("p0: {} {}", 13.0, 284);
//			logger.debug("KB: {} {}", 0.134717692857, 2.840574094501);
//			logger.debug("kb: {}", kb);
//
//			kb = transformation.operate(new double[] { 245.0, 503.0 });
//			logger.debug("p0: {} {}", 245.0, 503.0);
//			logger.debug("KB: {} {}", 2.457312296261, 5.03962307316);
//			logger.debug("kb: {}", kb);

			return Optional.of(new Array2DRowRealMatrix(dmatrix));
		}
	}
}
