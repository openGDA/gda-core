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

package gda.device.scannable;

import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.observable.IObserver;
import gda.scan.Trajectory;
import gda.scan.TrajectoryScanController;
import gda.scan.TrajectoryScanController.BuildStatus;
import gda.scan.TrajectoryScanController.ExecuteStatus;
import gda.scan.TrajectoryScanController.ReadStatus;
import gda.scan.TrajectoryScanController.TrajectoryScanProperty;
import gda.util.OutOfRangeException;
import gov.aps.jca.TimeoutException;

/**
 * Operates a single axis motion within the EPICS trajectory system for ContinuousScan scans (e.g. I18 raster map scans)
 */
public class EpicsSingleTrajectoryScannable extends ScannableMotionUnitsBase implements ContinuouslyScannable, IObserver {


	@SuppressWarnings("unused")
	private static final int ONEDMODE = 1;
	private static final int TWODMODE = 2;

	protected TrajectoryScanController tracController;
	private double readTimeout = 60.0;

	protected boolean trajMoveComplete;
	protected int trajectoryIndex = 0;
	protected int actualPulses;

	private ContinuousParameters continuousParameters;
	private double[] scannablePositions;
	private int mode;
	protected boolean trajectoryBuildDone;

	/**
	 * Property to enable/disable readback from EPICs trajectory scan controller. On I18, the readback causes a lot f issues so it was disabled permanently.
	 */
	private boolean isReadingBack = true;

	@Override
	public void configure() throws FactoryException {
		tracController.addIObserver(this);
		inputNames = new String[] { getName() };
	}

	@Override
	public void continuousMoveComplete() throws DeviceException {
		try {
			if (tracController.getExecuteStatus() != ExecuteStatus.ABORT) {
				if (!isReadingBack) {
					tracController.read();
					double waitedSoFar = 0.0;
					while (tracController.isReading() && waitedSoFar < (readTimeout * 1000)) {
						Thread.sleep(1000);
						waitedSoFar += 1000.0;
					}
					// check the read status from the controller
					if (tracController.getReadStatus() != ReadStatus.SUCCESS) {
						trajectoryBuildDone = false;
						throw new DeviceException("Exception while waiting for the the Trajectory Scan Controller to complete read out");
					}
				}
				actualPulses = tracController.getActualPulses();
			}
		} catch (TimeoutException e) {
			trajectoryBuildDone = false;
			throw new DeviceException(getName() + " exception in continuousMoveComplete", e);
		} catch (InterruptedException e) {
			trajectoryBuildDone = false;
			throw new DeviceException(getName() + " exception in continuousMoveComplete", e);
		}
	}

	@Override
	public ContinuousParameters getContinuousParameters() {
		return continuousParameters;
	}

	@Override
	public void performContinuousMove() throws DeviceException {
		trajMoveComplete = false;
		try {
			tracController.execute();
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in performContinuousMove", e);
		}
	}

	@Override
	public boolean isBusy() throws DeviceException {
		try {
			return tracController.isBusy();
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in isBusy", e);
		}
	}

	@Override
	public void stop() throws DeviceException {
		try {
			tracController.stop();
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in stop", e);
		}
		super.stop();
	}

	@Override
	public void prepareForContinuousMove() throws DeviceException {
		// build the trajectory
		if (mode == TWODMODE && trajectoryBuildDone)
			return;

		try {
			for (int i = 1; i <= TrajectoryScanController.MAX_TRAJECTORY; i++) {
				tracController.setMMove(i, false);
			}

			tracController.setMMove(trajectoryIndex, true);

			Trajectory trajectory = new Trajectory();
			trajectory.setTotalElementNumber(continuousParameters.getNumberDataPoints() + 1);
			trajectory.setTotalPulseNumber(continuousParameters.getNumberDataPoints() + 1);
			trajectory.setController(tracController);
			double[] path = trajectory.defineCVPath(continuousParameters.getStartPosition(), continuousParameters.getEndPosition(), continuousParameters.getTotalTime());

			tracController.setMTraj(trajectoryIndex, path);

			tracController.setNumberOfElements((int) trajectory.getElementNumbers());
			tracController.setNumberOfPulses((int) trajectory.getPulseNumbers());
			tracController.setStartPulseElement((int) trajectory.getStartPulseElement());
			tracController.setStopPulseElement((int) trajectory.getStopPulseElement());

			if (tracController.getStopPulseElement() != (int) (trajectory.getStopPulseElement())) {
				tracController.setStopPulseElement((int) (trajectory.getStopPulseElement()));
			}

			tracController.setTime((trajectory.getTotalTime()));

			tracController.build();


			//wait for the build to finsih
			while (tracController.isBuilding()) {
				// wait for the build to finish
				Thread.sleep(30);
			}
			// check the build status from epics
			if (tracController.getBuildStatus() != BuildStatus.SUCCESS) {
				throw new DeviceException(
						"Unable to build the trajectory with the given start and stop positions and the time");
			}

			trajectoryBuildDone = true;
		} catch (TimeoutException e) {
			throw new DeviceException(getName() + " exception in continuousMoveComplete", e);
		} catch (InterruptedException e) {
			throw new DeviceException(getName() + " exception in continuousMoveComplete", e);
		} catch (OutOfRangeException e) {
			throw new DeviceException(getName() + " exception in continuousMoveComplete", e);
		}
	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters) {
		this.continuousParameters = parameters;

	}

	@Override
	// TO DO discuss with FY
	public void update(Object source, Object arg) {
		if (source instanceof TrajectoryScanProperty) {
			if (((TrajectoryScanProperty) source) == TrajectoryScanProperty.EXECUTE) {
				if (arg instanceof ExecuteStatus) {
					if (((ExecuteStatus) arg) == ExecuteStatus.SUCCESS)
						trajMoveComplete = true;
					else
						trajMoveComplete = false;

				}
			}
		}

	}

	@Override
	public double calculateEnergy(int frameIndex) {
		double stepSize = (continuousParameters.getEndPosition() - continuousParameters.getStartPosition())
				/ continuousParameters.getNumberDataPoints();
		return (continuousParameters.getStartPosition() + (frameIndex * stepSize));

	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return tracController.getName();

	}

	@Override
	public void atScanLineEnd() throws DeviceException {
		try {
			scannablePositions = tracController.getMActual(trajectoryIndex);
		} catch (TimeoutException e) {
			throw new DeviceException(getName() + " exception in atScanLineEnd", e);
		} catch (InterruptedException e) {
			throw new DeviceException(getName() + " exception in atScanLineEnd", e);
		}
	}

	@Override
	public void atScanEnd() throws DeviceException {
		trajectoryBuildDone = false;
	}

	@Override
	public void atCommandFailure() {
		trajectoryBuildDone = false;
	}

	@Override
	public int getNumberOfDataPoints() {
		return continuousParameters.getNumberDataPoints();
	}

	public double[] getScannablePositions() {
		return scannablePositions;
	}

	public int getActualPulses() {
		return actualPulses;
	}

	public void setTrajectoryIndex(int trajectoryIndex) {
		this.trajectoryIndex = trajectoryIndex;
	}

	public int getTrajectoryIndex() {
		return trajectoryIndex;
	}

	public TrajectoryScanController getTracController() {
		return tracController;
	}

	public void setTracController(TrajectoryScanController tracController) {
		this.tracController = tracController;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public double getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(double readTimeout) {
		this.readTimeout = readTimeout;
	}

	public boolean isReadingBack() {
		return isReadingBack;
	}

	public void setReadingBack(boolean isReadingBack) {
		this.isReadingBack = isReadingBack;
	}

}
