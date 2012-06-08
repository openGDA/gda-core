/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

import gda.device.Scannable;

import java.lang.reflect.Array;

import org.apache.commons.lang.ArrayUtils;
import org.python.core.PyArray;
import org.python.core.PyFloat;
import org.python.core.PyList;

/**
 * Dummy Pseudo Device whose position is defined by an array of doubles.
 * <P>
 * For simulation/demo/testing purposes.
 */
public class DummyMultiElementScannable extends ScannableMotionBase implements Scannable {
	double[] currentPosition;

	/**
	 * Constructor for Castor.
	 */
	public DummyMultiElementScannable() {
		this.inputNames = new String[0];
		this.extraNames = new String[0];
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 * @param initialPosition
	 * @param elementNames
	 */
	public DummyMultiElementScannable(String name, double[] initialPosition, String[] elementNames) {
		this.setName(name);
		this.currentPosition = initialPosition;
		this.setInputNames(elementNames);
		this.setExtraNames(new String[] { "extra" });

		String[] outputFormat = new String[initialPosition.length + 1];

		for (int i = 0; i < initialPosition.length + 1; i++) {
			outputFormat[i] = "%5.5g";
		}

		this.setOutputFormat(outputFormat);

		currentPosition = new double[initialPosition.length];
		for (int i = 0; i < currentPosition.length; i++) {
			currentPosition[i] = 0.0;
		}
	}

	@Override
	public void configure() {

		if (this.getOutputFormat().length != inputNames.length + extraNames.length) {
			// set the output format array
			String[] outputFormat = new String[inputNames.length + extraNames.length];

			for (int i = 0; i < outputFormat.length; i++) {
				outputFormat[i] = "%5.5g";
			}

			this.setOutputFormat(outputFormat);
		}

		if (currentPosition == null) {
			// set the default position
			currentPosition = new double[inputNames.length];
			for (int i = 0; i < currentPosition.length; i++) {
				currentPosition[i] = 0.0;
			}
		}
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) {

		Double[] target = ScannableUtils.objectToArray(position);

		if (target.length == currentPosition.length) {
			currentPosition = ArrayUtils.toPrimitive(target);
		} else {
			// throw error
		}
	}

	@Override
	public Object rawGetPosition() {
		double[] returnPos = currentPosition;
		// add the 'extra' element to the array of positions
		for (@SuppressWarnings("unused")
		String foo : extraNames) {
			returnPos = ArrayUtils.add(returnPos, Math.random()*10-5);
		}
		return returnPos;
	}

	@Override
	public boolean rawIsBusy() {
		return false;
	}

	@Override
	public String checkPositionValid(Object position) {
		try {
			if (position instanceof double[] || position instanceof Double[] || position instanceof PyFloat[]) {
				if (Array.getLength(position) == this.currentPosition.length) {
					return null;
				}
			} else if (position instanceof PyArray) {
				if (((PyArray) position).__len__() == this.currentPosition.length) {
					return null;
				}

			} else if (position instanceof PyList) {
				if (((PyList) position).__len__() == this.currentPosition.length) {
					return null;
				}
			}
			return "position must be an array of doubles of the correct length";
		} catch (NumberFormatException e) {
			return e.getMessage();
		}
	}
}