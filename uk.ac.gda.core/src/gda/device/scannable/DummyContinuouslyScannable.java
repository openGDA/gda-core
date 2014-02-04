/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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
import gda.device.detector.SimulatedBufferedDetector;

import org.apache.commons.lang.ArrayUtils;

/**
 * Dummy implementation for testing / simulations. Works with the SimulatedBufferedDetector class.
 * <p>
 * This works on the assumption that its position is defined by a single number.
 */
public class DummyContinuouslyScannable extends ScannableMotionUnitsBase implements SimulatedContinuouslyScannable {

	ContinuousParameters continuousParameters;
	SimulatedBufferedDetector[] observers = new SimulatedBufferedDetector[0];

	private volatile double currentPosition = 0;
	private volatile boolean busy = false;

	@Override
	public void asynchronousMoveTo(Object externalPosition) throws DeviceException {
		if (busy) {
			throw new DeviceException("device is busy so cannot start another move");
		}
		currentPosition = ScannableUtils.objectToArray(externalPosition)[0];
	}

	@Override
	public void performContinuousMove() throws DeviceException {

		if (continuousParameters == null) {
			busy = false;
			throw new DeviceException("ContinuousParameters not set!");
		}

		if (busy) {
			throw new DeviceException("device is busy so cannot start another move");
		}

		// start a thread which will run for time s and inform its observers every pulseFreq s.
		busy = true;
		(uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName())).start();

	}

	@Override
	public void run() {
		double time = continuousParameters.getTotalTime();
		double step = Math.abs(continuousParameters.getEndPosition() - continuousParameters.getStartPosition())
				/ (continuousParameters.getNumberDataPoints() - 1);

		// assume time in seconds and need pulseFreq in ms for sleep() call
		long pulseFreq = Math.round((time / continuousParameters.getNumberDataPoints()) * 1000);

		currentPosition = continuousParameters.getStartPosition();
		int numPoints = 0;
		try {
			do {
				Thread.sleep(pulseFreq);
				currentPosition += step;

				for (SimulatedBufferedDetector det : observers) {
					det.addPoint();
				}
				numPoints++;

			} while (numPoints < continuousParameters.getNumberDataPoints());
		} catch (Exception e) {
			//
		}
		busy = false;
	}

	@Override
	public Object getPosition() throws DeviceException {
		return currentPosition;
	}

	@Override
	public boolean isBusy() {
		return busy;
	}

	/**
	 * For testing and simulation only. Real hardware represented by the ContinuousScannable and DummyHistogramDetector
	 * interfaces would be physically wired together.
	 * 
	 * @param detector
	 */
	public void addObserver(final SimulatedBufferedDetector detector) {
		if (!ArrayUtils.contains(observers, detector)) {
			observers = (SimulatedBufferedDetector[]) ArrayUtils.add(observers, detector);
		}
	}

	/**
	 * For testing and simulation only.
	 * 
	 * @param detector
	 */
	public void removeObserver(final SimulatedBufferedDetector detector) {
		if (ArrayUtils.contains(observers, detector)) {
			int index = ArrayUtils.indexOf(observers, detector);
			observers = (SimulatedBufferedDetector[]) ArrayUtils.remove(observers, index);
		}
	}

	@Override
	public String checkPositionValid(Object illDefinedPosObject) {
		try {
			Double.parseDouble(illDefinedPosObject.toString());
		} catch (NumberFormatException e) {
			return e.getMessage();
		}
		return null;
	}

	@Override
	public ContinuousParameters getContinuousParameters() {
		return continuousParameters;
	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters) {
		continuousParameters = parameters;
	}

	@Override
	public void continuousMoveComplete() throws DeviceException {
		// not required by this class
	}

	@Override
	public void prepareForContinuousMove() throws DeviceException {
		currentPosition = continuousParameters.getStartPosition();
	}

	@Override
	public double calculateEnergy(int frameIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberOfDataPoints() {
		return continuousParameters.getNumberDataPoints();
	}
}
