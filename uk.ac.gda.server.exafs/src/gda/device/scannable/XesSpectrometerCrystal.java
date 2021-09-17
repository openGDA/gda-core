/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.beamline.i20.scannable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.factory.FactoryException;

/**
 * This extension of {@link ScannableGroup} is intended to group together scannables relevant for
 * controlling {@link XesSpectrometer} crystals.
 * <li>Scannables for the x, y, rotation and pitch are set by calls to {@link #setxMotor(Scannable)},
 * {@link #setyMotor(Scannable)}, {@link #setRotMotor(Scannable)} and {@link #setPitchMotor(Scannable)}.
 * <li> The horizontal index of the crystal is set by {@link #setHorizontalIndex(int)}. This defines
 * the crystal position in the spectrometer relative to the central position
 * (i.e. 0 corresponds to the central crystal, +-1 for the crystals either side of the centre etc).
 * <br>
 * There is also an 'allowedToMove' flag set by calling {@link #setAllowedToMove(boolean)}.
 * If 'allowedToMove' is set to true, the scannable will behave as a normal ScannableGroup. If it is
 * set to false then :
 * <li> None of the scannables in the group will move when asynchronousMoveTo is called.
 * <li> {@link #isBusy()} will return false, and {@link #stop()} will do nothing.
 */
public class XesSpectrometerCrystal extends ScannableGroup {

	private int horizontalIndex = 0;
	private boolean isAllowedToMove = true;
	private Scannable xMotor;
	private Scannable yMotor;
	private Scannable rotMotor; // rotation in Rowland circle plane (previously, 'rot' scannable -> 'tilt' motor) yaw/tilt
	private Scannable pitchMotor; // tilt towards detector ('pitch' scannable)

	@Override
	public void configure() throws FactoryException {
		List<Scannable> allNonNullMotors = Arrays.asList(xMotor, yMotor, rotMotor, pitchMotor)
			.stream()
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		setGroupMembersWithList(allNonNullMotors, false);
		super.configure();
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		if (isAllowedToMove) {
			super.asynchronousMoveTo(position);
		}
	}

	@Override
	public boolean isBusy() throws DeviceException {
		if (isAllowedToMove) {
			return super.isBusy();
		} else {
			return false;
		}
	}

	@Override
	public void stop() throws DeviceException {
		if (isAllowedToMove) {
			super.stop();
		}
	}

	public void setAllowedToMove(boolean isAllowedToMove) {
		this.isAllowedToMove = isAllowedToMove;
	}

	public boolean isAllowedToMove() {
		return isAllowedToMove;
	}

	public int getHorizontalIndex() {
		return horizontalIndex;
	}

	public void setHorizontalIndex(int horizontalIndex) {
		this.horizontalIndex = horizontalIndex;
	}

	public Scannable getxMotor() {
		return xMotor;
	}

	public void setxMotor(Scannable xMotor) {
		this.xMotor = xMotor;
	}

	public Scannable getyMotor() {
		return yMotor;
	}

	public void setyMotor(Scannable yMotor) {
		this.yMotor = yMotor;
	}

	public Scannable getPitchMotor() {
		return pitchMotor;
	}

	public void setPitchMotor(Scannable pitchMotor) {
		this.pitchMotor = pitchMotor;
	}

	public Scannable getRotMotor() {
		return rotMotor;
	}

	public void setRotMotor(Scannable rotMotor) {
		this.rotMotor = rotMotor;
	}

}
