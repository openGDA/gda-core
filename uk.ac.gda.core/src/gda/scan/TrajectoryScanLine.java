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
import java.util.List;
import java.util.Map;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.continuouscontroller.TrajectoryMoveController;
import gda.device.detector.hardwaretriggerable.HardwareTriggeredDetector;
import gda.device.scannable.PositionConvertorFunctions;
import gda.device.scannable.VariableCollectionTimeDetector;
import gda.jython.commands.ScannableCommands;

public class TrajectoryScanLine extends AbtsractContinuousScanLine {

	public TrajectoryScanLine(Object[] args) throws IllegalArgumentException {
		super(args);
	}

	@Override
	protected TrajectoryMoveController getController() {
		return (TrajectoryMoveController) super.getController();
	}
	
	@Override
	protected void configureControllerTriggerTimes() throws DeviceException {
		// 3. Configure either the single trigger period or an array of trigger delta-times on the controller
		if (allDetectorsHaveCollectionTimeProfilesSet()) {
			getController().setTriggerDeltas(extractCommonCollectionTimeProfilesFromDetectors());
		} else {
			getController().setTriggerPeriod(extractCommonCollectionTimeFromDetectors());
		}
	}
	
	
	/**
	 * @return true if all Detectors support variable collection time profiles and have them set, false if non do.
	 * @throws IllegalArgumentException
	 *             if some but not all Detectors are providing variable collection time profiles.
	 * @throws DeviceException
	 */
	private boolean allDetectorsHaveCollectionTimeProfilesSet() throws IllegalArgumentException, DeviceException {
		int numberSupporting = 0;
		for (HardwareTriggeredDetector det : detectors) {
			if (det instanceof VariableCollectionTimeDetector) {
				if (((VariableCollectionTimeDetector) det).getCollectionTimeProfile() != null) {
					numberSupporting += 1;
				}
			}
		}
		if (numberSupporting == 0) {
			return false;
		}
		if (numberSupporting == detectors.size()) {
			return true;
		}
		throw new IllegalArgumentException("Some detectors have collection time profiles configured, but not all.");

	}
	
	private double[] extractCommonCollectionTimeProfilesFromDetectors() throws DeviceException {		
		double[] profile = ((VariableCollectionTimeDetector) detectors.get(0)).getCollectionTimeProfile();
		for (HardwareTriggeredDetector det : detectors.subList(1, detectors.size())) {
			double[] detsProfile = ((VariableCollectionTimeDetector) det).getCollectionTimeProfile();
			if (detsProfile.length != profile.length) {
				throw new DeviceException(
						"The detector's trigger time profiles have differing lengths.");
			}
			for (int i = 0; i < detsProfile.length; i++) {
				if (((Math.abs(detsProfile[i] - profile[i]) / profile[i]) > .1 / 100)) {
					throw new DeviceException(
						"The detector's trigger time profiles have values that differ by > .1%.");
				}
			}
		}
		return profile;
	}

	@Override
	protected void configureControllerPositions(boolean detectorsIntegrateBetweenTriggers) throws Exception {
		if (detectorsIntegrateBetweenTriggers) {
			List<Map<Scannable, double[]>> triggerPositions = generateTrajectoryForDetectorsThatIntegrateBetweenTriggers();
			getController().stopAndReset();
			for (Map<Scannable, double[]> point : triggerPositions) {
				moveMotorsToPositions(point);
			}
		} else {
			// Do nothing. The process of 'scanning' the Scannables will have resulted in calls to the
			// underlying TrajectoryMoveConroller
		}
		
	}

	private void moveMotorsToPositions(Map<Scannable, double[]> scannablePositions) throws Exception {
		ArrayList<Object> posArgs = new ArrayList<Object>(scannablePositions.size() * 2);
		for (Scannable scn : scannablePositions.keySet()) {
			posArgs.add(scn);
			Double[] posArray = PositionConvertorFunctions.toDoubleArray(scannablePositions.get(scn));
			posArgs.add(PositionConvertorFunctions.toObject(posArray));

		}
		ScannableCommands.pos(posArgs.toArray());
	}
}
