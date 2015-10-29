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

package gda.device.scannable;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.continuouscontroller.ContinuousMoveController;

/**
 * A Scannable that can me moved continuously via a {@link ContinuousMoveController}. When operating continuously the
 * Scannable will hand over control to the configured {@link ContinuousMoveController}. This is done by deferring
 * {@link #asynchronousMoveTo(Object)}, {@link #getPosition()}, {@link #isBusy()} and {@link #waitWhileBusy()} to the
 * controller.
 */
public interface ContinuouslyScannableViaController extends Scannable {

	/**
	 * Enable or disable control through the configured {@link ContinuousMoveController}
	 * @param b
	 * @throws DeviceException
	 */
	public void setOperatingContinuously(boolean b) throws DeviceException;

	/**
	 * Indicates whether the Scannable has deferred control to the he configured {@link ContinuousMoveController}
	 * @return true if control is defered
	 */
	public boolean isOperatingContinously();

	public ContinuousMoveController getContinuousMoveController();

	public void setContinuousMoveController(ContinuousMoveController controller);
}
