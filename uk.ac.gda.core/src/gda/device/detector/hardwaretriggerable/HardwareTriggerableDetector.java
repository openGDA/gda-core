/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.hardwaretriggerable;

import java.util.concurrent.Callable;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.continuouscontroller.ContinuousMoveController;
import gda.device.continuouscontroller.HardwareTriggerProvider;
import gda.device.scannable.PositionCallableProvider;
import gda.device.scannable.PositionInputStream;
import gda.device.scannable.PositionStreamIndexer;
import gda.device.scannable.VariableCollectionTimeDetector;
import gda.scan.TrajectoryScanLine;

/**
 * A hardware triggerable detector can be configured to accept hardware triggers from a {@link HardwareTriggerProvider}
 * representing a controller that it is physically wired to (e.g. a {@link ContinuousMoveController}). To work with the
 * {@link TrajectoryScanLine} class, an implementation should behave as described here:
 * <p>
 * When configured to trigger from hardware an implementation should do nothing when asked to {@link #collectData()}. It
 * should instead wait for a call to {@link #arm()} during which it should prepare to respond to hardware triggers. When
 * asked to {@link #arm()} the {@link HardwareTriggerProvider#getNumberTriggers()} will indicate how many trigger should
 * be expected.
 * <p>
 * Most implementations should also implement {@link PositionCallableProvider} indicating that they will return data via
 * {@link Callable}s which block until the data is actually available. Implementing {@link PositionInputStream} and
 * deferring calls to {@link PositionCallableProvider#getPositionCallable()} to a {@link PositionStreamIndexer} may be
 * the simplest way to do this.
 * <p>
 * Detectors which operate in applications that require variable trigger times during a scan should implement
 * {@link VariableCollectionTimeDetector}.
 */
public interface HardwareTriggerableDetector extends Detector {

	/**
	 * Get the {@link HardwareTriggerProvider} that represents the controller this Detector is wired to.
	 * 
	 * @return the trigger provider
	 */
	public HardwareTriggerProvider getHardwareTriggerProvider();

	/**
	 * Configure the Detector to trigger on hardware triggers, or not.
	 * 
	 * @param b
	 * @throws DeviceException
	 */
	public void setHardwareTriggering(boolean b) throws DeviceException;

	/**
	 * @return true if configured to triger on hardware triggers.
	 */
	public boolean isHardwareTriggering();

	/**
	 * Block while arming the detector in preparation to receive hardware triggers. At this point, if operated in a
	 * {@link TrajectoryScanLine} the trigger provider knows how many triggers to expect and how these will be spaced.
	 */
	public void arm() throws DeviceException;
	
	/**
	 * Detectors that sample some value at the time of a trigger should return False. Detectors such as counter timers
	 * should return True. If true ,TrajectoryScanLine will generate a trigger half a point before the motor reaches a
	 * demanded point such that the resulting bin of data is centred on the demand position. Area detectors that will be
	 * triggered by the first pulse should also return true.
	 * @return true for detectors that integrates b
	 */
	public boolean integratesBetweenPoints();

}
