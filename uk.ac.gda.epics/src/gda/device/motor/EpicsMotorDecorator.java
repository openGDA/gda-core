/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

import gda.device.Motor;
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.device.scannable.MotorUnitStringSupplier;
import gda.factory.FactoryException;
import gda.observable.IObserver;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.event.PutListener;

/**
 * Decorator for EPICS Motor which checks IOC status before connecting and accessing EPICS motor PVs.
 * This class doesn't decorate all the methods in an {@link EpicsMotor}, but only those methods that
 * access to EPICS PVs in the specified IOC. If IOC is up and running, access is delegated to the
 * decorated motor instance; If IOC is down, any access to PVs in the specified IOC will throw {@link MotorException}
 * except for {@link EpicsMotorDecorator#configure()} which throws {@link FactoryException} if
 * {@link EpicsMotorDecorator#isConfigureAtStartup()} is set to true, otherwise it just delays the configure later.
 * <p>
 * Spring XML configuration example:
 * <pre>
 * {@code
 * <bean id="d6x_mtr" class="gda.device.motor.EpicsMotorDecorator">
 * 	<property name="iocPv" value="BL11I-MO-IOC-03:STATUS"/>
 * 	<constructor-arg>
 * 		<bean class="gda.device.motor.EpicsMotor">
 * 			<property name="deviceName" value="D6.X"/>
 * 		</bean>
 * 	</constructor-arg>
 * 	<property name="configureAtStartup" value="true">
 * </bean>
 * }
 * </pre>
 * </p>
 */
public class EpicsMotorDecorator extends MotorIocDecorator implements MotorUnitStringSupplier, IObserver {

	public EpicsMotorDecorator() {
		super();
	}

	public EpicsMotorDecorator(Motor decoratedMotor) {
		super(decoratedMotor);
	}

	@Override
	public void configure() throws FactoryException {
		if (isIocRunning()) {
			decoratedMotor.reconfigure();
			decoratedMotor.addIObserver(this);
		} else {
			if (isConfigureAtStartup()) {
				throw new FactoryException("Motor IOC " + getIocPv().split(":")[0] + " is not running");
			}
			setConfigureAtStartup(false);
		}
	}

	@Override
	public String getUnitString() throws MotorException {
		if (isIocRunning()) {
			// Split interface i.e. Unit interface is not in Motor.java interface
			if (decoratedMotor instanceof EpicsMotor) {
				return ((EpicsMotor) decoratedMotor).getUnitString();
			}
		} else {
			throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
		}
		return null;
	}

	@Override
	public void setSpeed(double speed) throws MotorException {
		if (isIocRunning()) {
			decoratedMotor.setSpeed(speed);
		} else {
			throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
		}
	}

	@Override
	public double getSpeed() throws MotorException {
		if (isIocRunning()) {
			return decoratedMotor.getSpeed();
		}
		throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
	}

	@Override
	public void setTimeToVelocity(double timeToVelocity) throws MotorException {
		if (isIocRunning()) {
			decoratedMotor.setTimeToVelocity(timeToVelocity);
		} else {
			throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
		}
	}

	@Override
	public double getTimeToVelocity() throws MotorException {
		if (isIocRunning()) {
			return decoratedMotor.getTimeToVelocity();
		}
		throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
	}

	@Override
	public double getRetryDeadband() throws MotorException {
		if (isIocRunning()) {
			return decoratedMotor.getRetryDeadband();
		}
		throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
	}

	@Override
	public double getMotorResolution() throws MotorException {
		if (isIocRunning()) {
			return decoratedMotor.getMotorResolution();
		}
		throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
	}

	@Override
	public double getUserOffset() throws MotorException {
		if (isIocRunning()) {
			return decoratedMotor.getUserOffset();
		}
		throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
	}

	// intercepted method is not in the Motor interface
	public void setUserOffset(double userOffset) throws MotorException {
		if (isIocRunning()) {
			if (decoratedMotor instanceof EpicsMotor) {
				((EpicsMotor) decoratedMotor).setUserOffset(userOffset);
			}
		} else {
			throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
		}
	}

	@Override
	public boolean isMoving() throws MotorException {
		if (isIocRunning()) {
			return decoratedMotor.isMoving();
		}
		throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
	}

	// intercepted method is not in the Motor interface
	protected MotorStatus checkStatus() throws MotorException {
		if (isIocRunning()) {
			if (decoratedMotor instanceof EpicsMotor)
				return ((EpicsMotor) decoratedMotor).checkStatus();
		}
		throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
	}

	@Override
	public MotorStatus getStatus() throws MotorException {
		if (isIocRunning()) {
			return decoratedMotor.getStatus();
		}
		throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
	}

	@Override
	public void moveBy(double steps) throws MotorException {
		if (isIocRunning()) {
			decoratedMotor.moveBy(steps);
		} else {
			throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
		}
	}

	@Override
	public void moveTo(double steps) throws MotorException {
		if (isIocRunning()) {
			decoratedMotor.moveTo(steps);
		} else {
			throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
		}
	}

	public void moveTo(double position, double timeout) throws MotorException, TimeoutException, InterruptedException {
		if (isIocRunning()) {
			if (decoratedMotor instanceof EpicsMotor) {
				((EpicsMotor) decoratedMotor).moveTo(position, timeout);
			}
		} else {
			throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
		}
	}

	public void moveTo(double position, PutListener moveListener) throws MotorException {
		if (isIocRunning()) {
			if (decoratedMotor instanceof EpicsMotor) {
				((EpicsMotor) decoratedMotor).moveTo(position, moveListener);
				;
			}
		} else {
			throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
		}
	}

	protected double getDialLowLimit() throws MotorException {
		if (isIocRunning()) {
			if (decoratedMotor instanceof EpicsMotor) {
				return ((EpicsMotor) decoratedMotor).getDialLowLimit();
			}
		}
		throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
	}

	protected double getDialHighLimit() throws MotorException {
		if (isIocRunning()) {
			if (decoratedMotor instanceof EpicsMotor) {
				return ((EpicsMotor) decoratedMotor).getDialHighLimit();
			}
		}
		throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
	}

	@Override
	public void setMinPosition(double minimumPosition) throws MotorException {
		if (isIocRunning()) {
			if (decoratedMotor instanceof EpicsMotor) {
				((EpicsMotor) decoratedMotor).setMinPosition(minimumPosition);
			}
		} else {
			throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
		}
	}

	@Override
	public double getMinPosition() throws MotorException {
		if (isIocRunning()) {
			return decoratedMotor.getMinPosition();
		}
		throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
	}

	@Override
	public void setMaxPosition(double maximumPosition) throws MotorException {
		if (isIocRunning()) {
			if (decoratedMotor instanceof EpicsMotor) {
				((EpicsMotor) decoratedMotor).setMaxPosition(maximumPosition);
			}
		} else {
			throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
		}
	}

	@Override
	public double getMaxPosition() throws MotorException {
		if (isIocRunning()) {
			return decoratedMotor.getMaxPosition();
		}
		throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
	}

	@Override
	public void stop() throws MotorException {
		if (isIocRunning()) {
			decoratedMotor.stop();
		} else {
			throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
		}
	}
	public void stopGo() throws MotorException {
		if (isIocRunning()) {
			if (decoratedMotor instanceof EpicsMotor) {
				((EpicsMotor) decoratedMotor).stopGo();
			}
		} else {
			throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
		}
	}

	@Override
	public void panicStop() throws MotorException {
		if (isIocRunning()) {
			decoratedMotor.panicStop();
		} else {
			throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
		}
	}
	@Override
	public void moveContinuously(int direction) throws MotorException {
		if (isIocRunning()) {
			decoratedMotor.moveContinuously(direction);
		} else {
			throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
		}
	}

	@Override
	public void setPosition(double steps) throws MotorException {
		if (isIocRunning()) {
			decoratedMotor.setPosition(steps);
		}
		throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
	}

	@Override
	public double getPosition() throws MotorException {
		if (isIocRunning()) {
			return decoratedMotor.getPosition();
		}
		throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
	}
	@Override
	public void home() throws MotorException {
		if (isIocRunning()) {
			decoratedMotor.home();
		}
		throw new MotorException(MotorStatus.UNKNOWN, "Motor IOC " + getIocPv().split(":")[0] + " is not running");
	}
	public boolean isHomedFromMSTAValue(double msta) {
		if (isIocRunning()) {
			if (decoratedMotor instanceof EpicsMotor) {
				return ((EpicsMotor) decoratedMotor).isHomedFromMSTAValue(msta);
			}
		} else {
			throw new IllegalStateException("Motor IOC " + getIocPv().split(":")[0] + " is not running");
		}
		return false;
	}

	@Override
	public boolean isHomed() throws MotorException {
		if (isIocRunning()) {
			return decoratedMotor.isHomed();
		}
		throw new IllegalStateException("Motor IOC " + getIocPv().split(":")[0] + " is not running");
	}
	public double readMsta() throws TimeoutException, CAException, InterruptedException {
		if (isIocRunning()) {
			if (decoratedMotor instanceof EpicsMotor) {
				return ((EpicsMotor) decoratedMotor).readMsta();
			}
		}
		throw new IllegalStateException("Motor IOC " + getIocPv().split(":")[0] + " is not running");
	}
	@Override
	public void update(Object theObserved, Object changeCode) {
		notifyIObservers(theObserved, changeCode);
	}

}
