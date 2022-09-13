/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.diffcalc.gda.scannables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.factory.FactoryException;

/**
 * A scannable group which can move to a position that contains nulls.
 *
 * It does this by replacing any nulls in the position with the current
 * position of that motor.
 */
public class TpScannableGroup extends ScannableGroup {

	private static final double TP_TOLERANCE = 0.0001;
	public TpScannableGroup() {}

	public TpScannableGroup(String name, List<Scannable> groupMembers) throws FactoryException {
		setName(name);
		setGroupMembers(groupMembers);
	}

	/**
	 * Replaces and nulls in the position given, with the current position of the ScannableGroup.
	 */
	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		List<Double> currentPosition = getPositionAsList();
		List<Double> desiredPosition = null;
		if (position instanceof Double[]) {
			desiredPosition = Arrays.asList((Double []) position);
		}
		else if (position instanceof List) {
			desiredPosition = (List<Double>) position;
		}
		else {
			throw new IllegalArgumentException("position must be list like.");
		}


		for (int i=0; i<currentPosition.size(); i++) {
			Scannable scannable = getGroupMembers().get(i);
			Double nonNullPosition = Objects.isNull(desiredPosition.get(i)) ? currentPosition.get(i) : desiredPosition.get(i);

			// Don't bother moving to a new position if it's less than some amount away from current position.
			if (Math.abs(nonNullPosition - currentPosition.get(i)) > TP_TOLERANCE) {
				scannable.asynchronousMoveTo(nonNullPosition);
			}
		}
	}

	public List<Double> getPositionAsList() throws DeviceException {
		Object[] position = (Object[]) getPosition();
		List<Double> convertedPosition = new ArrayList<>();

		for (int i=0; i < position.length; i++) {
			convertedPosition.add((Double) position[i]);
		}

		return convertedPosition;
	}
}
