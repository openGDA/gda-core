/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.motor;

import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.factory.FactoryException;
import gov.aps.jca.Channel;

/**
 * Extension of EPicsMotors to allow crude homing of the polarimeter EPICS motors via GDA gui
 */
public class PolarimeterEpicsMotor extends EpicsMotor {

	private Channel athm = null; // Set to 1 when motor has been homed
	private Channel jogf = null; // Set to 1 when causes motor to jog forwawrd continuously
	private Channel jogr = null; // Set to 1 when causes motor to jog reverse continuously
	private Channel lls = null; // At Lower Limit Switch .LLS, SHORT
	private Channel hls = null; // At High Limit Switch, .HLS, SHORT
	private String homeDirection; // Direction of home move
	private Channel homr = null;

	@Override
	public void configure() throws FactoryException {
		super.configure();
		String recordName = this.getEpicsRecordName();
		if (recordName != null) {
			try {
				athm = channelManager.createChannel(recordName + ".ATHM", false);
				jogf = channelManager.createChannel(recordName + ".JOGF", false);
				jogr = channelManager.createChannel(recordName + ".JOGR", false);
				lls = channelManager.createChannel(recordName + ".LLS", false);
				hls = channelManager.createChannel(recordName + ".HLS", false);
				homf = channelManager.createChannel(recordName + ".HOMF", false);
				// acknowledge that creation phase is completedthrows FactoryException
				channelManager.creationPhaseCompleted();
			} catch (Throwable th) {
				// TODO take care of destruction
				throw new FactoryException("failed to connect to homingl channels", th);
			}
			channelManager.tryInitialize(100);
		}// end of if (epicspv != null)

	}

	@Override
	public boolean isHomeable() {
		return true;
	}

	@Override
	public boolean isHomed() {

		boolean homed = false;
		try {
			if (controller.cagetShort(athm) == 1) {
				homed = true;
			}
		} catch (Throwable th) {
			// TODo
		}
		return homed;
	}

	@Override
	public void home() throws MotorException {

		try {
			// If positive home move to lower limit
			if (homeDirection.equals("positive")) {
				this.stop();
				controller.caput(jogr, 1, channelManager);
				while (controller.cagetShort(lls) != 1) {
					// Do nothing
					Thread.sleep(500);
				}
				// Move to home postion
				controller.caput(homf, 1, channelManager);
				Thread.sleep(500);
			}
			// If negative home move to upper limit
			if (homeDirection.equals("negative")) {
				this.stop();
				controller.caput(jogf, 1, channelManager);
				while (controller.cagetShort(hls) != 1) {
					// Do nothing
					Thread.sleep(500);
				}
				// Move to home postion
				controller.caput(homr, 1, channelManager);
				Thread.sleep(500);
			}
			// Loop while motor homes
			while (controller.cagetShort(athm) != 1) {
				// Do nothing
				Thread.sleep(500);
			}
		} catch (Throwable ex) {
			throw new MotorException(MotorStatus.FAULT, "failed to home", ex);
		}
	}

	/**
	 * This method returns the homing direction of the motor.
	 * 
	 * @return the motor homing direction.
	 */
	public String getHomingDirection() {
		return this.homeDirection;
	}

	/**
	 * This method sets the homing direction of the motor.
	 * 
	 * @param direction
	 * @direction the motor homing direction.
	 */
	public void setHomingDirection(String direction) {
		this.homeDirection = direction;
	}
}
