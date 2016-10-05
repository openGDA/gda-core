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

import static gda.jython.InterfaceProvider.getTerminalPrinter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.continuouscontroller.ConstantVelocityMoveController;
import gda.device.continuouscontroller.ConstantVelocityMoveController2;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.device.scannable.PositionConvertorFunctions;

public class ConstantVelocityScanLine extends AbstractContinuousScanLine {

	private static final Logger logger = LoggerFactory.getLogger(AbstractContinuousScanLine.class);
	protected Double start;
	protected Double stop;
	protected Double step;


	public ConstantVelocityScanLine(Object[] args) throws IllegalArgumentException {
		super(args);
		parseArgsAgain(args);
	}

	protected void parseArgsAgain(Object[] args) {
		start = PositionConvertorFunctions.toDouble(args[1]);
		stop = PositionConvertorFunctions.toDouble(args[2]);
		step = PositionConvertorFunctions.toDouble(args[3]);
		checkRemainingArgs(args, 4);
	}

	// Check that any subsequent Scannables do not have target positions, i.e. that their
	// positions will only be read.
	protected static void checkRemainingArgs(Object[] args, int argIndex) {

		boolean allowDetectorCollectionTime = false;

		for (int i = argIndex; i < args.length; i++) {

			if (args[i] instanceof Detector) {
				allowDetectorCollectionTime = true;

			} else if (args[i] instanceof ContinuouslyScannableViaController) {
				allowDetectorCollectionTime = false;

			} else if (isZeroInputExtraNamesScannable(args[i])) {
				allowDetectorCollectionTime = false;

			} else if (allowDetectorCollectionTime) {
				// This is neither a Detector, ContinuouslyScannableViaController, or zie scannable. It is probabaly a
				// number and is allowed based on the nature of the previous arg.
				allowDetectorCollectionTime = false;  // i.e. for the following element

			} else {
				throw new IllegalArgumentException("Invalid argument " + args[i]);
			}

		}

	}

	private static boolean isZeroInputExtraNamesScannable(Object object) {

		if (object instanceof Scannable) {
			Scannable scannable = (Scannable) object;
			if ((scannable.getInputNames().length + scannable.getExtraNames().length) == 0) {
				return true;
			}
		}
		return false;

	}

	@Override
	protected ConstantVelocityMoveController getController() {
		return (ConstantVelocityMoveController) super.getController();
	}

	@Override
	protected void extractScannablesToScan() {
		super.extractScannablesToScan();
	}

		@Override
	protected void configureControllerTriggerTimes() throws DeviceException {
		getController().setTriggerPeriod(extractCommonCollectionTimeFromDetectors());
		ConstantVelocityMoveController controller = getController();
		if( controller instanceof ConstantVelocityMoveController2){
			ConstantVelocityMoveController2 cvmc2 = (ConstantVelocityMoveController2)controller;
			cvmc2.setDetectors(detectors);
		}
	}


	@Override
	protected void callAtCommandFailureHooks() {
		super.callAtCommandFailureHooks();

		try {
			logger.info("Problem with scan, stopping and resetting controller: " + getController().getName());
			getController().stopAndReset();
		} catch (Exception e) {
			String message = "Catching " + e.getClass().getSimpleName() + " stopping and resetting controller " + getController().getName();
			logger.error(message, e);
			getTerminalPrinter().print(message);
		}
	}

	@Override
	protected void configureControllerPositions(boolean detectorsIntegrateBetweenTriggers) throws DeviceException, InterruptedException {

		ConstantVelocityMoveController controller = getController();
		controller.stopAndReset();
		if( controller instanceof ConstantVelocityMoveController2){
			ConstantVelocityMoveController2 cvmc2 = (ConstantVelocityMoveController2)controller;
			cvmc2.setStart(start);
			cvmc2.setEnd(stop);
			cvmc2.setScannableToMove(scannablesToMove);
		}else{
		if (detectorsIntegrateBetweenTriggers) {
			controller.setStart(start - step / 2.);
			controller.setEnd(stop - step / 2.);
		} else {
			controller.setStart(start);
			controller.setEnd(stop);
		}
		}
		controller.setStep(step);
	}

}
