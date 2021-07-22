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

import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableUtils;
import uk.ac.gda.api.scan.IImplicitScanObject;


public class ImplicitScanObject extends ScanObject implements IImplicitScanObject {

	private static final Logger logger = LoggerFactory.getLogger(ImplicitScanObject.class);

	private int numberPoints = 0;

	private Object start;

	private Object stop;

	private Object step;

	private List<Object> points = null;

	private boolean isDetector = false;

	private int lastCommandedPosition = 0;

	/**
	 * @param scannable
	 * @param start
	 * @param stop
	 * @param step
	 */
	public ImplicitScanObject(Scannable scannable, Object start, Object stop, Object step) {
		super(scannable);
		this.start = start;
		this.stop = stop;
		this.step = step;

		if (scannable instanceof Detector) {
			isDetector = true;
			if (start != null && !(start instanceof Scannable)) {
				try {
					((Detector) scannable).setCollectionTime(Double.parseDouble(start.toString()));
				} catch (NumberFormatException e) {
					logger.error("Detector setCollectionTime() throws ", e);
				} catch (DeviceException e) {
					logger.error("Detector setCollectionTime() throws ", e);
				}
			}
		}
	}

	@Override
	public ScanObjectType getType() {
		return ScanObjectType.IMPLICIT;
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
			return  ScannableUtils.getNumberSteps(scannable, start, stop, step) + 1; // + 1 for the first point
		} catch (Exception e) {
			return 0;
		}
	}

	private void checkPointsCalculated() {
		if (points == null) throw new IllegalStateException("Points not initialized"); // calculateScanPoints not called
	}

	@Override
	public String arePointsValid() throws DeviceException {
		checkPointsCalculated();

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
		checkPointsCalculated();

		Object pos = null;
		if (isDetector && start != null) {
			pos = start;
			((Detector) scannable).setCollectionTime(Integer.parseInt(start.toString()));
		} else if (start != null) {
			pos = points.get(0);
			logger.debug("Moving {} to {}", scannable.getName(), pos);
			scannable.asynchronousMoveTo(pos);
			lastCommandedPosition = 0;
		}
		return new ScanStepId(scannable.getName(), pos);
	}

	@Override
	public ScanStepId moveStep() throws Exception {
		checkPointsCalculated();

		Object pos = null;
		if (step != null) {
			logger.debug("Moving {} by {}", scannable.getName(), step);
			lastCommandedPosition++;
			pos = points.get(lastCommandedPosition);
		} else if (start != null) {
			logger.debug("Moving {} to {}", scannable.getName(), start);
			pos = start;
		}
		if (pos != null)
			scannable.asynchronousMoveTo(pos);
		return new ScanStepId(scannable.getName(), pos);
	}

	/**
	 * Fill the array of points to move the scannable to
	 */
	@Override
	public void calculateScanPoints() {
		if (numberPoints != 0 && step != null) {
			points = Stream.iterate(start, prev -> ScannableUtils.calculateNextPoint(prev, step))
					.limit(numberPoints)
					.collect(toUnmodifiableList());
		} else {
			points = Arrays.asList(start);
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

	@Override
	public boolean hasStep() {
		return step != null;
	}

	@Override
	public Object getStart() {
		return start;
	}

	@Override
	public Object getStop() {
		return stop;
	}

	@Override
	public Object getStep() {
		return step;
	}

	@Override
	public String toString() {
		return "ImplicitScanObject [numberPoints=" + numberPoints + ", start=" + start + ", stop=" + stop + ", step=" + step + "]";
	}

}
