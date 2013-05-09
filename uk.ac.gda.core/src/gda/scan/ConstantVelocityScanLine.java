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
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.continuouscontroller.ConstantVelocityMoveController;
import gda.device.scannable.ContinuouslyScannable;
import gda.device.scannable.PositionConvertorFunctions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstantVelocityScanLine extends AbstractContinuousScanLine {

	private static final Logger logger = LoggerFactory.getLogger(AbstractContinuousScanLine.class);
	private Double start;
	private Double stop;
	private Double step;
	

	public ConstantVelocityScanLine(Object[] args) throws IllegalArgumentException {
		super(args);
		start = PositionConvertorFunctions.toDouble(args[1]);
		stop = PositionConvertorFunctions.toDouble(args[2]);
		step = PositionConvertorFunctions.toDouble(args[3]);
		int argIndex=4;
		boolean allowDetectorCollectionTime = false;
		while(argIndex < args.length){
			if( args[argIndex] instanceof ContinuouslyScannable || args[argIndex] instanceof PositionGrabbingAdapter){
				argIndex++;
				allowDetectorCollectionTime=false;
				continue;
			}
			if( args[argIndex] instanceof Detector){
				argIndex++;
				allowDetectorCollectionTime=true;
				continue;
			}
			if( allowDetectorCollectionTime ){
				argIndex++;
				allowDetectorCollectionTime=false;
				continue;
			}
			throw new IllegalArgumentException("Invalid argument " + args[argIndex]);
		}
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
		
		getController().stopAndReset();
		if (detectorsIntegrateBetweenTriggers) {
			getController().setStart(start - step / 2.);
			getController().setEnd(stop - step / 2.);
		} else {
			getController().setStart(start);
			getController().setEnd(stop);
		}
		getController().setStep(step);
	}

}
