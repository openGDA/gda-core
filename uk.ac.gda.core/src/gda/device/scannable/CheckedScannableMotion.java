/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

/**
 * <p>
 * This interface extends ScannableMotion. It adds the methods rawAsyncrhonousMoveTo() and rawIsBusy(). These should be
 * written by the user of the scannable. To change a scannable into a checked scannable, rename the user written methods
 * AsyncrhonousMoveTo() and IsBusy() to match these new names.
 * <p>
 * The base class CheckedScannableBase which extends ScannableBase (@see gda.jython.scannable.CheckedScannableBase)
 * redefines the AsyncrhonousMoveTo and IsBusy methods to handoff control to the singleton CollisionAvoidanceController
 * object. (As well as moveTo(), moveBy()and asynchronousMoveBy()).
 * 
 * @see gda.device.Scannable
 * @see gda.device.scannable.ScannableBase
 * @see gda.server.collisionAvoidance.CollisionAvoidanceController
 */
public interface CheckedScannableMotion extends gda.device.ScannableMotion {

	/**
	 * Flag checked by isBusy method. Set by CAC controller indicate if is moving the motor. This will be set slightly
	 * before the move begins as and slightly after it finishes. The motor will appear busy while the CAC is checking
	 * the move is allowed and oce the move is complete, while its updating its internal state.
	 */
	boolean CacIsMovingThis = false;

	/**
	 * User written move command. In CheckedDofAdapeter should call DofAdapter.asynchMoveTo()
	 * 
	 * @param position
	 * @throws Exception
	 */
	public void rawAsynchronousMoveTo(Double[] position) throws Exception;

	/**
	 * User written is busy command.
	 * 
	 * @return True if underling system is busy
	 */
	public boolean rawIsBusy();

	/**
	 * Used by collision-avoidance-controller.
	 */
	public void setCacIsMovingThis();

	/**
	 * Used by collision-avoidance-controller.
	 */
	public void unsetCacIsMovingThis();

}
