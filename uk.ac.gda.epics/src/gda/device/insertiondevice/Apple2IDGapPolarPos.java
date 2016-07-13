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
 * Scannable to allow control of an AppleII ID by specifying gap, polarisation and motor position
 * <p>
 * This is a relatively low-level control, allowing you to specify ID gap, polarisation and motor position. Beamlines may prefer to have two scannables to allow
 * you to specify energy and polarisation separately and will also adjust the PGM as appropriate.
 * <p>
 * This class expects as input an array of elements: [<gap in mm>, <polarisation>, <motor position in mm>]
 * <p>
 * Polarisation can be:<br>
 * LH (horizontal)<br>
 * C (circular)<br>
 * L1 (linear 1)<br>
 * L2 (linear 2)
 * <p>
 * Motor position will be used to set the positions of the individual soft motors as described in
 * http://confluence.diamond.ac.uk/display/I21/i21+software+setup#i21softwaresetup-Insertiondevice%28ID%29
 * <p>
 * If polarisation is LH, the motor position parameter will be ignored, but must be supplied.
 * <p>
 * Vertical polarisation is a special case of circular polarisation, where motor position is the maximum allowed value.
 * <p>
 * Control of the ID is delegated to a lower-level control that implements IApple2ID.
 */
public class Apple2IDGapPolarPos extends Apple2IDScannableBase {

	// See comment above for description of possible modes
	private enum PolarisationMode {
		LH, C, L1, L2, UNKNOWN
	}

	public Apple2IDGapPolarPos() {
		setInputNames(new String[] { "gap", "polarisation", "motorPos" });
		setExtraNames(new String[] { "mode", "enabled", "topOuterMotor", "topInnerMotor", "bottomOuterMotor", "bottomInnerMotor" });
		setOutputFormat(new String[] { "%5.3fmm", "%s", "%5.3fmm", "%s", "%5.3fmm", "%5.3fmm", "%5.3fmm", "%5.3fmm", "%5.3fmm" });
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {

		PolarisationMode mode;
		double motorPos = 0;

		// Parse parameters
		if (!(position instanceof List<?>)) {
			throw new DeviceException("position should be a list");
		}

		final Object[] params = ((List<?>) position).toArray();
		if (params.length < 3) {
			throw new DeviceException("position should contain at least 3 elements");
		}

		final double gap = parseParamToDouble(params[0]);

		try {
			mode = PolarisationMode.valueOf(params[1].toString().toUpperCase());
		} catch (Exception ex) {
			throw new DeviceException(String.format("invalid mode: must be LH, C, L1 or L2"));
		}

		motorPos = parseParamToDouble(params[2]);

		switch (mode) {
		case LH:
			controller.asynchronousMoveTo(new Apple2IDPosition(gap, 0, 0, 0, 0));
			break;
		case C:
			controller.asynchronousMoveTo(new Apple2IDPosition(gap, motorPos, 0, 0, motorPos));
			break;
		case L1:
			controller.asynchronousMoveTo(new Apple2IDPosition(gap, motorPos, 0, 0, -motorPos));
			break;
		case L2:
			controller.asynchronousMoveTo(new Apple2IDPosition(gap, 0, motorPos, -motorPos, 0));
			break;
		default:
			throw new DeviceException("Invalid mode " + mode.toString());
		}
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		final Object[] result = new Object[9];
		final Apple2IDPosition position = controller.getPosition();
		final PolarisationMode polarisationMode = getPolarisationMode(position.topOuterPos, position.topInnerPos, position.bottomOuterPos,
				position.bottomInnerPos);
		result[0] = position.gap;
		result[1] = polarisationMode.toString();
		result[2] = (polarisationMode == PolarisationMode.L2) ? position.topInnerPos : position.topOuterPos;
		result[3] = controller.getIDMode();
		result[4] = controller.isEnabled();
		result[5] = position.topOuterPos;
		result[6] = position.topInnerPos;
		result[7] = position.bottomOuterPos;
		result[8] = position.bottomInnerPos;
		return result;
	}

	// Get polarisation mode. This is currently the same code as in Apple2IDBase, but the definition of polarisation may change in future,
	// so the two functions are kept separate.
	private PolarisationMode getPolarisationMode(double topOuterPos, double topInnerPos, double bottomOuterPos, double bottomInnerPos) {

		if (motorPositionsEqual(topOuterPos, 0) && motorPositionsEqual(topInnerPos, 0) && motorPositionsEqual(bottomOuterPos, 0)
				&& motorPositionsEqual(bottomInnerPos, 0)) {
			return PolarisationMode.LH;
		}
		if (motorPositionsEqual(topOuterPos, bottomInnerPos) && motorPositionsEqual(topInnerPos, 0) && motorPositionsEqual(bottomOuterPos, 0)) {
			return PolarisationMode.C;
		}
		if (motorPositionsEqual(topOuterPos, -bottomInnerPos) && motorPositionsEqual(topInnerPos, 0) && motorPositionsEqual(bottomOuterPos, 0)) {
			return PolarisationMode.L1;
		}
		if (motorPositionsEqual(topInnerPos, -bottomOuterPos) && motorPositionsEqual(topOuterPos, 0) && motorPositionsEqual(bottomInnerPos, 0)) {
			return PolarisationMode.L2;
		}
		return PolarisationMode.UNKNOWN;
	}
}
