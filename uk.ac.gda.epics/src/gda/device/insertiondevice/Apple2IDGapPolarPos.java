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
 * Scannable to allow control of an AppleII ID by specifying gap, polarisation mode and phase motor position.
 * <p>
 * This class expects as input an array of elements: [&ltgap_in_mm&gt, &ltpolarisation_mode&gt, &ltphase_motor_position_in_mm&gt]
 * <p>
 * Polarisation Mode can be:<br>
 * <ul>
 * <li>LH (Linear Horizontal)<br>
 * <li>LV (Linear Vertical)<br>
 * <li>CR (Circular Right)<br>
 * <li>CL (Circular Left)<br>
 * <li>LAP (Linear Angular Positive)<br>
 * <li>LAN (Linear Angular Negative)
 * </ul>
 * <p>
 * Motor position will be used to set the positions of the individual soft motors as described in http://confluence.diamond.ac.uk/display/I21/Insertion+device
 * <p>
 * If polarisation mode is LH or LV, the phase motor position parameter will be ignored, but must be supplied.
 * <p>
 * Control of the ID is delegated to a lower-level control that implements IApple2ID.
 *
 * @see Apple2IDPolarisationMode
 */
public class Apple2IDGapPolarPos extends Apple2IDScannableBase {

	public Apple2IDGapPolarPos() {
		setInputNames(new String[] { "gap", "polarisation", "motorPos" });
		setExtraNames(new String[] { "mode", "enabled", "topOuterMotor", "topInnerMotor", "bottomOuterMotor", "bottomInnerMotor" });
		setOutputFormat(new String[] { "%5.3fmm", "%s", "%5.3fmm", "%s", "%b", "%5.3fmm", "%5.3fmm", "%5.3fmm", "%5.3fmm" });
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {

		Apple2IDPolarisationMode mode;
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
			mode = Apple2IDPolarisationMode.valueOf(params[1].toString().toUpperCase());
		} catch (Exception ex) {
			throw new DeviceException(String.format("invalid mode: must be LH, LV, CR, CL, LAP or LAN"));
		}

		motorPos = parseParamToDouble(params[2]);

		switch (mode) {
		case LH:
			controller.asynchronousMoveTo(new Apple2IDPosition(gap, 0, 0, 0, 0));
			break;
		case LV:
			controller.asynchronousMoveTo(new Apple2IDPosition(gap, controller.getMaxPhaseMotorPos(), 0, 0, controller.getMaxPhaseMotorPos()));
			break;
		case CR:
			controller.asynchronousMoveTo(new Apple2IDPosition(gap, motorPos, 0, 0, motorPos));
			break;
		case CL:
			controller.asynchronousMoveTo(new Apple2IDPosition(gap, -motorPos, 0, 0, -motorPos));
			break;
		case LAP:
			controller.asynchronousMoveTo(new Apple2IDPosition(gap, motorPos, 0, 0, -motorPos));
			break;
		case LAN:
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
		final Apple2IDPolarisationMode polarisationMode = controller.getPolarisationMode(position);
		result[0] = position.gap;
		result[1] = polarisationMode.toString();
		result[2] = (polarisationMode == Apple2IDPolarisationMode.LAN) ? position.topInnerPos : position.topOuterPos;
		result[3] = controller.getIDMode();
		result[4] = controller.isEnabled();
		result[5] = position.topOuterPos;
		result[6] = position.topInnerPos;
		result[7] = position.bottomOuterPos;
		result[8] = position.bottomInnerPos;
		return result;
	}
}
