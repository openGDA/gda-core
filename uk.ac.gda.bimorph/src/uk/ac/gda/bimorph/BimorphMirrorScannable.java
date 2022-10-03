/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.bimorph;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.IntStream.range;

import java.util.Arrays;

import gda.device.DeviceException;
import gda.device.scannable.PositionConvertorFunctions;
import gda.device.scannable.ScannableBase;
import gda.factory.FactoryException;

/**
 * Scannable for controlling Bimorph Mirrors independently of brand of PSU.
 */
public class BimorphMirrorScannable extends ScannableBase {

	/** The controller for device specific control of the mirror */
	private BimorphMirrorController controller;

	/** Offset to use in channel naming */
	private int offset = 0;

	@Override
	public void configure() throws FactoryException {
		requireNonNull(controller, getName() + ": Bimorph controller is required");
		int channelCount;
		try {
			channelCount = controller.getNumberOfChannels();
		} catch (DeviceException e) {
			throw new FactoryException("Could not read number of channels from controller", e);
		}
		setInputNames(range(0, channelCount).mapToObj(c -> "ch"+(c+offset)).toArray(String[]::new));
		setExtraNames(new String[0]);
		setOutputFormat(range(0, channelCount).mapToObj(c -> "%.2f").toArray(String[]::new));
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return controller.getVoltages();
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		Double[] newPosition = PositionConvertorFunctions.toDoubleArray(position);
		if (newPosition.length != controller.getNumberOfChannels()) {
			throw new IllegalArgumentException(getName() + ": Incorrect number of voltages. This mirror has " + controller.getNumberOfChannels() + " channels.");
		} else if (excessiveVoltageDeltas(newPosition)) {
			throw new IllegalArgumentException(getName() + ": Maximum difference between voltages exceeds limit (" + controller.getMaxDelta() + "V)");
		} else if (outsideVoltageLimits(newPosition)) {
			throw new IllegalArgumentException(getName() + ": Voltages must be between limits [" + getMinVoltage() + ", " + getMaxVoltage() + "]");
		}
		controller.setVoltages(Arrays.stream(newPosition).mapToDouble(Double::doubleValue).toArray());
	}

	/**
	 * Check if any voltage of new position is outside the limits of the power supply
	 * @param newPosition The array of new voltages
	 * @return true if any voltages are outside the limits.
	 */
	private boolean outsideVoltageLimits(Double[] newPosition) {
		return stream(newPosition)
				.anyMatch(d -> d < getMinVoltage() || d > getMaxVoltage());
	}

	/**
	 * Check if the difference between consecutive voltages is outside permissible voltage step
	 * @param newPosition The array of new voltages
	 * @return true if any step is too great for the power supply
	 */
	private boolean excessiveVoltageDeltas(Double[] newPosition) {
		return range(0, newPosition.length -1)
				.mapToDouble(i -> Math.abs(newPosition[i] - newPosition[i+1]))
				.anyMatch(d -> d > controller.getMaxDelta());
	}

	/** Compatibility method for use during transition of bimorph scripts */
	public int getNumOfChans() throws DeviceException {
		return controller.getNumberOfChannels();
	}

	private double getMaxVoltage() {
		return controller.getMaxVoltage();
	}

	private double getMinVoltage() {
		return controller.getMinVoltage();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return controller.isBusy();
	}

	public void setController(BimorphMirrorController controller) {
		this.controller = controller;
	}

	public BimorphMirrorController getController() {
		return controller;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}
}
