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

import gda.device.Detector;
import gda.device.continuouscontroller.ContinuousMoveController;
import gda.device.continuouscontroller.HardwareTriggerProvider;
import gda.device.scannable.PositionCallableProvider;
import gda.device.scannable.PositionInputStream;
import gda.device.scannable.PositionStreamIndexer;
import gda.device.scannable.VariableCollectionTimeDetector;
import gda.scan.TrajectoryScanLine;

import java.util.concurrent.Callable;

/**
 * A hardware triggered detector can be configured to accept hardware triggers from a {@link HardwareTriggerProvider}
 * representing a controller that it is physically wired to (e.g. a {@link ContinuousMoveController}). To work with the
 * {@link TrajectoryScanLine} class, an implementation should behave as described here:
 * <p>
 * An AbtsractContinuousScanLine will call {@link Detector#setCollectionTime(double)}, then
 * {@link #setNumberImagesToCollect(int)} then {@link Detector#prepareForCollection()}. The implementation will then
 * have {@link Detector}{@link #readout()} or if implemented {@link PositionCallableProvider#getPositionCallable()}
 * called for each point that *will* be collected. A call to {@link Detector#collectData()} will then be made which should
 * in this mode of operation arm the detector.
 * <p>
 * Most implementations should also implement {@link PositionCallableProvider} indicating that they will return data via
 * {@link Callable}s which block until the data is actually available. Implementing {@link PositionInputStream} and
 * deferring calls to {@link PositionCallableProvider#getPositionCallable()} to a {@link PositionStreamIndexer} may be
 * the simplest way to do this.
 * <p>
 * Detectors which operate in applications that require variable trigger times during a scan should implement
 * {@link VariableCollectionTimeDetector}.
 */
public interface HardwareTriggeredDetector extends Detector {

	/**
	 * Get the {@link HardwareTriggerProvider} that represents the controller this Detector is wired to.
	 * 
	 * @return the trigger provider
	 */
	public HardwareTriggerProvider getHardwareTriggerProvider();

	/**
	 * Tell the detector how many scan points to collect. (Unfortunately named images).
	 * @param numberImagesToCollect
	 */
	public void setNumberImagesToCollect(int numberImagesToCollect);
	
	/**
	 * Detectors that sample some value at the time of a trigger should return False. Detectors such as counter timers
	 * should return True. If true ,TrajectoryScanLine will generate a trigger half a point before the motor reaches a
	 * demanded point such that the resulting bin of data is centred on the demand position. Area detectors that will be
	 * triggered by the first pulse should also return true.
	 * @return true for detectors that integrates b
	 */
	public boolean integratesBetweenPoints();
	
}
