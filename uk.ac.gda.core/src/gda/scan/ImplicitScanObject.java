/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.scan;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;

import java.util.Vector;


public class ImplicitScanObject extends ScanObject {
	private int numberPoints = 0;

	private Object start;

	private Object stop;

	private Object step;

	private Vector<Object> points = new Vector<Object>();

	private boolean isDetector = false;

	private int lastCommandedPosition = 0;

	/**
	 * @param scannable
	 * @param start
	 * @param stop
	 * @param step
	 */
	public ImplicitScanObject(Scannable scannable, Object start, Object stop, Object step) {
		this.scannable = scannable;
		this.start = start;
		this.stop = stop;
		this.step = step;

		if (scannable instanceof Detector) {
			isDetector = true;
			if (start != null && !(start instanceof Scannable)) {
				try {
					((Detector) scannable).setCollectionTime(Double.valueOf(start.toString()).doubleValue());
				} catch (NumberFormatException e) {
					logger.error("Detector setCollectionTime() throws ", e);
				} catch (DeviceException e) {
					logger.error("Detector setCollectionTime() throws ", e);
				}
			}
		}
	}

	@Override
	public void setNumberPoints(int number) {
		numberPoints = number;
	}

	@Override
	public int getNumberPoints() {
		if (stop == null || step == null) {
			return 0;
		}
		try {
			int number = ScannableUtils.getNumberSteps(scannable, start, stop, step);
			return number + 1; // to reflect the first point
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public String arePointsValid() throws DeviceException {
		if (start != null) {
			// loop through all points
			for (Object thisPosition : this.points) {
				// if any invalid then return false
				String reason = this.scannable.checkPositionValid(thisPosition);
				if (reason != null) {
					return reason;
				}
			}
		}
		return null;
	}

	@Override
	public ScanStepId moveToStart() throws Exception {
		Object pos = null;
		if (isDetector && start != null) {
			pos = start;
			((Detector) scannable).setCollectionTime(Integer.parseInt(start.toString()));
		} else if (start != null) {
			pos = points.get(0);
			logger.debug("Moving " + scannable.getName() + " to " + pos);
			scannable.asynchronousMoveTo(pos);
			lastCommandedPosition = 0;
		}
		return new ScanStepId(scannable.getName(), pos);
	}

	@Override
	public ScanStepId moveStep() throws Exception {
		Object pos = null;
		if (step != null) {
			logger.debug("Moving " + scannable.getName() + " by " + step);
			lastCommandedPosition++;
			pos = points.get(lastCommandedPosition);
		} else if (start != null) {
			logger.debug("Moving " + scannable.getName() + " to " + start);
			pos = start;
		}
		if (pos != null)
			scannable.asynchronousMoveTo(pos);
		return new ScanStepId(scannable.getName(), pos);
	}

	/**
	 * Fill the array of points to move the scannable to
	 */
	public void calculateScanPoints() {
		points.add(start);
		if (this.numberPoints != 0 && step != null) {
			// loop through all points and create vector of points
			Object previousPoint = start;
			for (int i = 1; i < numberPoints; i++) {
				Object nextPoint = ScannableUtils.calculateNextPoint(previousPoint, step);
				points.add(nextPoint);
				previousPoint = nextPoint;
			}
		}
	}

	@Override
	public boolean hasStart() {
		return start != null;
	}

	@Override
	public boolean hasStop() {
		return stop != null;
	}
}
