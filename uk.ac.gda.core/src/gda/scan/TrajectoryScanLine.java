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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math.linear.MatrixUtils;
import org.apache.commons.math.linear.RealVector;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.continuouscontroller.ContinuousMoveController;
import gda.device.continuouscontroller.TrajectoryMoveController;
import gda.device.detector.hardwaretriggerable.HardwareTriggeredDetector;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.device.scannable.PassthroughScannableDecorator;
import gda.device.scannable.PositionCallableProvider;
import gda.device.scannable.PositionConvertorFunctions;
import gda.device.scannable.VariableCollectionTimeDetector;
import gda.jython.commands.ScannableCommands;

public class TrajectoryScanLine extends AbstractContinuousScanLine {


//	interface ContinuouslyScannableCallableProviderViaController<T> extends ContinuouslyScannableViaController, PositionCallableProvider<T> {}

	private static class PassthroughContinouselyScannableViaControllerDecorator extends PassthroughScannableDecorator implements ContinuouslyScannableViaController {

		public PassthroughContinouselyScannableViaControllerDecorator(ContinuouslyScannableViaController delegate) {
			super(delegate);
		}

		@Override
		public ContinuouslyScannableViaController getDelegate() {
			return (ContinuouslyScannableViaController) super.getDelegate();
		}

		@Override
		public void setOperatingContinuously(boolean b) throws DeviceException {
			getDelegate().setOperatingContinuously(b);
		}

		@Override
		public boolean isOperatingContinously() {
			return getDelegate().isOperatingContinously();
		}

		@Override
		public ContinuousMoveController getContinuousMoveController() {
			return getDelegate().getContinuousMoveController();
		}

		@Override
		public void setContinuousMoveController(ContinuousMoveController controller) {
			getDelegate().setContinuousMoveController(controller);
		}
	}

	private static class PositionGrabbingDecorator<T> extends PassthroughContinouselyScannableViaControllerDecorator {

		private ScanPositionRecorder recorder;

		public PositionGrabbingDecorator(ContinuouslyScannableViaController delegate) {
			super(delegate);
		}

		public void setRecorder(ScanPositionRecorder recorder) {
			this.recorder = recorder;
		}

		@Override
		public void asynchronousMoveTo(Object position) throws DeviceException {
			if (recorder != null) {
				recorder.addPositionToCurrentPoint(getDelegate(), position);
			}
			getDelegate().asynchronousMoveTo(position);
		}
	}

	private static class PositionGrabbingCallableDecorator<T> extends PositionGrabbingDecorator<T> implements PositionCallableProvider<T> {

		public PositionGrabbingCallableDecorator(ContinuouslyScannableViaController delegate) {
			super(delegate);
			if (!(delegate instanceof PositionCallableProvider<?>)) {
				throw new IllegalArgumentException("Expected PositionCallableProvider");
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public Callable<T> getPositionCallable() throws DeviceException {
			return ((PositionCallableProvider<T>) getDelegate()).getPositionCallable();
		}

	}

	class ScanPositionRecorder {

		LinkedList<Map<Scannable, RealVector>> points =
			new LinkedList<Map<Scannable, RealVector>>();

		void startNewPoint() {
			points.add(new HashMap<Scannable, RealVector>());
		}

		void addPositionToCurrentPoint(Scannable scannable, Object demandPosition) {
			double[] doublePosition = ArrayUtils.toPrimitive(PositionConvertorFunctions.toDoubleArray(demandPosition));
			RealVector positionVector = MatrixUtils.createRealVector(doublePosition);
			points.getLast().put(scannable, positionVector);
		}

		List<Map<Scannable, RealVector>> getPoints() {
			return points;
		}

	}

	private static Object[] wrapContinuouslyScannables(Object[] args) {
		for (int i = 0; i < args.length; i++) {
			Object object = args[i];
			if (object instanceof ContinuouslyScannableViaController) {
				if((object instanceof PositionCallableProvider<?>)){
					args[i] = new PositionGrabbingCallableDecorator<>((ContinuouslyScannableViaController) args[i]);
				} else {
					args[i] = new PositionGrabbingDecorator<>((ContinuouslyScannableViaController) args[i]);
				}
			}
		}
		return args;
	}

	ScanPositionRecorder scanPositionRecorder;


	public TrajectoryScanLine(Object[] args) throws IllegalArgumentException {
		super(wrapContinuouslyScannables(args));
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

	@SuppressWarnings("rawtypes")
	@Override
	public void doCollection() throws Exception {
		if (detectorsIntegrateBetweenTriggers) {
			scanPositionRecorder = new ScanPositionRecorder();
			for (ContinuouslyScannableViaController scn : scannablesToMove) {
				((PositionGrabbingDecorator) scn).setRecorder(scanPositionRecorder);
			}
		}
		super.doCollection();
	}

	@Override
	protected void callAtPointStartHooks() throws DeviceException {
		super.callAtPointStartHooks();
		if (scanPositionRecorder != null) {
			scanPositionRecorder.startNewPoint();
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

	List<Map<Scannable,double[]>> generateTrajectoryForDetectorsThatIntegrateBetweenTriggers() {
		List<Map<Scannable, double[]>> triggers =
			new LinkedList<Map<Scannable, double[]>>();
		List<Map<Scannable, RealVector>> binCentres =
			scanPositionRecorder.getPoints();

		HashMap<Scannable, double[]> pointToAdd;

		Set<Scannable> scannables = binCentres.get(0).keySet();

		// Add first trigger: xtrig[0] = bincentre[0] - (bincentre[1] - bincentre[0]) / 2
		pointToAdd = new HashMap<Scannable, double[]>();
		for (Scannable scannable : scannables) {
			RealVector first = binCentres.get(0).get(scannable);
			RealVector second = binCentres.get(1).get(scannable);
			double[] firstTrigger = first.subtract(second.subtract(first).mapDivide(2.)).toArray();
			pointToAdd.put(scannable, firstTrigger );
		}
		triggers.add(pointToAdd);

		// Add middle triggers: xtrig[i] = (bincentre[i] + bincentre[i+1]) / 2
		for (int i = 0; i < binCentres.size() -1 ; i++) { // not the last one
			pointToAdd = new HashMap<Scannable, double[]>();
			for (Scannable scannable : scannables) {
				RealVector current = binCentres.get(i).get(scannable);
				RealVector next = binCentres.get(i+1).get(scannable);
				double[] trigger = current.add(next).mapDivide(2.).toArray();
				pointToAdd.put(scannable, trigger);
			}
			triggers.add(pointToAdd);
		}

		// Add last trigger: xtrig[n+1] = bincentre[n] + (bincentre[n] - bincentre[n-1]) / 2
		pointToAdd = new HashMap<Scannable, double[]>();
		for (Scannable scannable : scannables) {
			int lastIndex = binCentres.size() - 1;
			RealVector last = binCentres.get(lastIndex).get(scannable);
			RealVector secondLast = binCentres.get(lastIndex-1).get(scannable);
			double[] lastTrigger = last.add(last.subtract(secondLast).mapDivide(2.)).toArray();
			pointToAdd.put(scannable, lastTrigger );
		}
		triggers.add(pointToAdd);

		return triggers;
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
