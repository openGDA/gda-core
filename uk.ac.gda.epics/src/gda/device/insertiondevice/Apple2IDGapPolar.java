/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.insertiondevice;

import java.util.List;

import gda.device.DeviceException;

/**
 * Scannable to allow control of an Apple II ID by specifying gap and polarisation (linear horizontal or linear vertical)
 * <p>
 * This is a relatively low-level control, allowing you to specify ID gap and polarisation. Beamlines may prefer to have two scannables to allow you to specify
 * energy and polarisation separately and will also adjust the PGM as appropriate.
 * <p>
 * This class expects as input an array of elements: [<gap in mm>, <polarisation>]
 * <p>
 * Polarisation can be:<br>
 * LH (horizontal)<br>
 * LV (vertical)<br>
 * <p>
 * In LH mode, all motors are at 0 In LV mode, the top outer & bottom inner motors are at their maximum position, as returned by
 * controller.getMaxPhaseMotorPos(); the top inner & bottom outer motors are at 0
 * <p>
 * Control of the ID is delegated to a lower-level control that implements IApple2ID.
 */
public class Apple2IDGapPolar extends Apple2IDScannableBase {

	// See comment above for description of possible modes
	private enum PolarisationMode {
		LH, LV, UNKNOWN
	}

	public Apple2IDGapPolar() {
		setInputNames(new String[] { "gap", "polarisation" });
		setExtraNames(new String[] { "mode", "enabled", "topOuterMotor", "topInnerMotor", "bottomOuterMotor", "bottomInnerMotor" });
		setOutputFormat(new String[] { "%5.3fmm", "%s", "%5.3fmm", "%s", "%5.3fmm", "%5.3fmm", "%5.3fmm", "%5.3fmm", "%5.3fmm" });
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {

		PolarisationMode mode;

		// Parse parameters
		if (!(position instanceof List<?>)) {
			throw new DeviceException("position should be a list");
		}

		final Object[] params = ((List<?>) position).toArray();
		if (params.length < 2) {
			throw new DeviceException("position should contain at least 2 elements");
		}

		final double gap = parseParamToDouble(params[0]);

		try {
			mode = PolarisationMode.valueOf(params[1].toString().toUpperCase());
		} catch (Exception ex) {
			throw new DeviceException(String.format("invalid mode: must be LH or LV"));
		}

		switch (mode) {
		case LH:
			controller.asynchronousMoveTo(new Apple2IDPosition(gap, 0, 0, 0, 0));
			break;
		case LV:
			final double maxPhaseMotorPos = controller.getMaxPhaseMotorPos();
			controller.asynchronousMoveTo(new Apple2IDPosition(gap, maxPhaseMotorPos, 0, 0, maxPhaseMotorPos));
			break;
		default:
			throw new DeviceException("Invalid mode " + mode.toString());
		}
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		final Object[] result = new Object[8];
		final Apple2IDPosition position = controller.getPosition();
		final PolarisationMode polarisationMode = getPolarisationMode(position.topOuterPos, position.topInnerPos, position.bottomOuterPos,
				position.bottomInnerPos);
		result[0] = position.gap;
		result[1] = polarisationMode.toString();
		result[2] = controller.getIDMode();
		result[3] = controller.isEnabled();
		result[4] = position.topOuterPos;
		result[5] = position.topInnerPos;
		result[6] = position.bottomOuterPos;
		result[7] = position.bottomInnerPos;
		return result;
	}

	private PolarisationMode getPolarisationMode(double topOuterPos, double topInnerPos, double bottomOuterPos, double bottomInnerPos) {

		if (motorPositionsEqual(topOuterPos, 0) && motorPositionsEqual(topInnerPos, 0) && motorPositionsEqual(bottomOuterPos, 0)
				&& motorPositionsEqual(bottomInnerPos, 0)) {
			return PolarisationMode.LH;
		}

		final double maxPhaseMotorPos = controller.getMaxPhaseMotorPos();
		if (motorPositionsEqual(topOuterPos, maxPhaseMotorPos) && motorPositionsEqual(bottomInnerPos, maxPhaseMotorPos) && motorPositionsEqual(topInnerPos, 0)
				&& motorPositionsEqual(bottomOuterPos, 0)) {
			return PolarisationMode.LV;
		}
		return PolarisationMode.UNKNOWN;
	}
}
