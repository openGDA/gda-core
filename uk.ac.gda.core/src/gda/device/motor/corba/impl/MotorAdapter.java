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

package gda.device.motor.corba.impl;

import gda.device.Motor;
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.device.corba.impl.DeviceAdapter;
import gda.device.motor.corba.CorbaMotor;
import gda.device.motor.corba.CorbaMotorException;
import gda.device.motor.corba.CorbaMotorHelper;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the motor class
 */
public class MotorAdapter extends DeviceAdapter implements Motor {
	private CorbaMotor corbaMotor;

	/**
	 * Create client side interface to the CORBA package.
	 *
	 * @param obj
	 *            the CORBA object
	 * @param name
	 *            the name of the object
	 * @param netService
	 *            the CORBA naming service
	 */
	public MotorAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaMotor = CorbaMotorHelper.narrow(obj);
		this.netService = netService;
		this.name = name;
	}

	@Override
	public void moveBy(double steps) throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMotor.moveBy(steps);
				return;
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}

		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}

	@Override
	public void moveTo(double steps) throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMotor.moveTo(steps);
				return;
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}

		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}

	@Override
	public void moveContinuously(int direction) throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMotor.moveContinuously(direction);
				return;
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}

		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}

	@Override
	public void setPosition(double steps) throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMotor.setPosition(steps);
				return;
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}

		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}

	@Override
	public double getPosition() throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMotor.getPosition();
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}

		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}

	@Override
	public void setSpeed(double speed) throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMotor.setSpeed(speed);
				return;
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}

		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}

	@Override
	public double getSpeed() throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMotor.getSpeed();
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
				netService.reconnect(name);
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}
		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}

	@Override
	public double getRetryDeadband() throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMotor.getRetryDeadband();
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
				netService.reconnect(name);
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}
		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}

	@Override
	public double getTimeToVelocity() throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMotor.getTimeToVelocity();
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
				netService.reconnect(name);
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}
		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}

	@Override
	public void setTimeToVelocity(double timeToVelocity) throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMotor.setTimeToVelocity(timeToVelocity);
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
				netService.reconnect(name);
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}
		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}

	@Override
	public void stop() throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMotor.stop();
				return;
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}

		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}

	@Override
	public void panicStop() throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMotor.panicStop();
				return;
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}

		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}

	@Override
	public MotorStatus getStatus() throws MotorException {
		MotorStatus mstat = null;

		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				mstat = MotorStatus.fromInt(corbaMotor.getStatus().value());
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}
		}
		return mstat;
	}

	@Override
	public void correctBacklash() throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMotor.correctBacklash();
				return;
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}

		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}

	@Override
	public boolean isMoving() throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMotor.isMoving();
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}

		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}

	@Override
	public void setSpeedLevel(int speedLevel) throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMotor.setSpeedLevel(speedLevel);
				return;
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}

		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}

	@Override
	public boolean isHomeable() throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMotor.isHomeable();
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}

		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}

	@Override
	public boolean isHomed() throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMotor.isHomed();
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}

		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}

	@Override
	public void home() throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMotor.home();
				return;
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}

		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}

	@Override
	public void setSoftLimits(double min, double max) throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaMotor.setSoftLimits(min, max);
				return;
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}

		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}

	@Override
	public boolean isLimitsSettable() throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMotor.isLimitsSettable();
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}

		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}

	@Override
	public double getMinPosition() throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMotor.getMinPosition();
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}

		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}

	@Override
	public double getMaxPosition() throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMotor.getMaxPosition();
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}

		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}

	@Override
	public boolean isInitialised() throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMotor.isInitialised();
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}

		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}

	@Override
	public double getMotorResolution() throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMotor.getMotorResolution();
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}

		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}

	@Override
	public double getUserOffset() throws MotorException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaMotor.getUserOffset();
			} catch (COMM_FAILURE cf) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaMotor = CorbaMotorHelper.narrow(netService.reconnect(name));
			} catch (CorbaMotorException ex) {
				MotorStatus ms = MotorStatus.fromInt(ex.status.value());
				throw new MotorException(ms, ex.message);
			}

		}
		throw new MotorException(MotorStatus.FAULT, "Communication failure: retry failed");
	}
}
