/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gda.device.DeviceException;
import gda.device.continuouscontroller.ConstantVelocityRasterMoveController;
import gda.device.scannable.PositionConvertorFunctions;

/**
 * NOTE: This code assumes that the controller will advance trigger by half a point.
 * It is up to the controller to determine whether a trailing trigger is given at the end of each line.
 * It would be quite tricky to handle the case where this is required, e.g. for a counter timer. Either
 * the counter timer would need to be set to count for a fixed time (like we assume the detectors do), or the
 * controller would need to send out an inhibit line.
 * <p>
 * IMPORTANT: Detectors will not recieve synchronised calls to atLineStart()! An example consequence is that
 * plugins must be setup to handle all points made during the scan, not just those from one line.
 */
public class ConstantVelocityRasterScan extends ConstantVelocityScanLine {

	private Double outerStart;
	private Double outerStop;
	private Double outerStep;

	public ConstantVelocityRasterScan(Object[] args) throws IllegalArgumentException {
		// ConstantVelocityRasterScan([outer, start, stop, step, RasterScanLine([inner, start, stop, step ...]))
		super(createNestedScan(args));

		// args[0] is outer scannable
		outerStart = PositionConvertorFunctions.toDouble(args[1]);
		outerStop = PositionConvertorFunctions.toDouble(args[2]);
		outerStep = PositionConvertorFunctions.toDouble(args[3]);

		// args[4] is inner scannable
		start = PositionConvertorFunctions.toDouble(args[5]);
		stop = PositionConvertorFunctions.toDouble(args[6]);
		step = PositionConvertorFunctions.toDouble(args[7]);
	}

	private static Object[] createNestedScan(Object[] args) {
		// TODO Check args

		checkRemainingArgs(args, 8);

		List<Object> argList = Arrays.asList(args);
		List<Object> outerScanArgs = argList.subList(0, 4); // outerscannable start stop step
		List<Object> innerScanArgs = argList.subList(4, argList.size()); // remaining

		List<Object> superArgs = new ArrayList<Object>();
		superArgs.addAll(outerScanArgs);
		superArgs.add(new RasterScanLine(innerScanArgs.toArray()));
		return superArgs.toArray();
	}

	@Override
	protected void parseArgsAgain(Object[] args) {
	}

	@Override
	protected ConstantVelocityRasterMoveController getController() {
		return (ConstantVelocityRasterMoveController) super.getController();
//		ConstantVelocityMoveController c = super.getController();
//		if (c instanceof ConstantVelocityRasterMoveController) {
//			return (ConstantVelocityRasterMoveController) c;
//		} // else
//		throw new IllegalArgumentException("ConstantVelocityRasterScan requires a ConstantVelocityRasterMoveController controller");
	}

	@Override
	protected void configureControllerPositions(boolean detectorsIntegrateBetweenTriggers) throws DeviceException, InterruptedException {

		getController().stopAndReset();

		getController().setOuterStart(outerStart);
		getController().setOuterEnd(outerStop);
		getController().setOuterStep(outerStep);

		// Please see class comment!
		if (detectorsIntegrateBetweenTriggers) {
			getController().setStart(start); // - step / 2.);
			getController().setEnd(stop); //  - step / 2.);
		} else {
			throw new IllegalArgumentException("The controller we have so far advances triggers by half a point, so detectors must integrate between triggers");
		}
		getController().setStep(step);

	}

}

class RasterScanLine extends ConcurrentScan {

	public RasterScanLine(Object[] args) throws IllegalArgumentException {
		super(args);
		callCollectDataOnDetectors = false;
	}

	@Override
	public boolean isReadoutConcurrent() {
		return false;  // should be false even if enabled for beamline
	}

}