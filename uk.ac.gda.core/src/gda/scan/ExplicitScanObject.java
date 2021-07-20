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

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import uk.ac.gda.api.scan.IExplicitScanObject;

public class ExplicitScanObject extends ScanObject implements IExplicitScanObject {

	private static final Logger logger = LoggerFactory.getLogger(ExplicitScanObject.class);

	private final ScanPositionProvider pointProvider;

	private int lastPoint = -1;

	/**
	 * @param scannable
	 * @param pointProvider
	 */
	public ExplicitScanObject(Scannable scannable, ScanPositionProvider pointProvider) {
		super(scannable);
		this.pointProvider = pointProvider;
	}

	@Override
	public ScanObjectType getType() {
		return ScanObjectType.EXPLICIT;
	}

	@Override
	public ScanStepId moveToStart() throws Exception {
		Object pos = pointProvider.get(0);
		logger.debug("Moving " + scannable.getName() + " to " + pos);
		scannable.asynchronousMoveTo(pos);
		lastPoint = 0;
		return new ScanStepId(scannable.getName(), pos);
	}

	@Override
	public ScanStepId moveStep() throws Exception {
		Object pos = null;

		if (lastPoint != pointProvider.size()) {
			pos = pointProvider.get(lastPoint + 1);
			logger.debug("Moving " + scannable.getName() + " to " + pos);
			scannable.asynchronousMoveTo(pos);
			lastPoint++;
		}
		return new ScanStepId(scannable.getName(), pos);
	}

	@Override
	public int getNumberPoints() {
		return pointProvider.size();
	}

	@Override
	public Object getPoint(int index) {
		return pointProvider.get(index);
	}

	@Override
	public void setNumberPoints(int number) {
		// do nothing
	}

	@Override
	public String arePointsValid() throws DeviceException {
		for (int i = 0; i < getNumberPoints(); i++) {
			// if any points invalid then return false
			String reason = this.scannable.checkPositionValid(pointProvider.get(i));
			if (reason != null) {
				return reason;
			}
		}
		return null;
	}

	@Override
	public boolean hasStart() {
		return getNumberPoints() > 0;
	}

	@Override
	public boolean hasStop() {
		return getNumberPoints() > 1;
	}

	@Override
	public Iterator<Object> iterator() {
		return pointProvider.iterator();
	}

	@Override
	public String toString() {
		return "ExplicitScanObject [pointProvider=" + pointProvider + "]";
	}

}
