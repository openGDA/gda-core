/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import gda.device.ControllerRecord;
import gda.device.ITweakable;
import gda.device.Motor;
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.epics.connection.EpicsController;
import gda.factory.FindableBase;
import gda.util.functions.ThrowingFunction;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

/**
 * implement motor position tweak control like those available in EPICS motor record.
 * This class support both {@link DummyMotor} and {@link EpicsMotor}.
 */
public class MotorPositionTweakControl extends FindableBase implements ITweakable, ControllerRecord {

	private static final Logger logger = LoggerFactory.getLogger(MotorPositionTweakControl.class);
	private static final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();
	private static final String TWEAK_FORWARD = ".TWF";
	private static final String TWEAK_REVERSE = ".TWR";
	private static final String TWEAK_STEP = ".TWV";
	private double increment = 1.0; // required for dummy
	private Motor motor;

	public void setMotor(Motor motor) {
		this.motor = motor;
	}

	/**
	 * a cached instance which maps EPICS PV to CA Channel created
	 */
	private static final Map<String, Channel> channelMap = new HashMap<>();

	@Override
	public void forward() throws MotorException {
		double maxPosition = motor.getMaxPosition();
		double position = motor.getPosition();
		if ((position + increment) > maxPosition) {
			throw new MotorException(MotorStatus.UPPER_LIMIT, getName() + ": tweak forward exceeds motor's maximum position " + maxPosition);
		}
		if (motor instanceof DummyMotor) {
			((DummyMotor) motor).tweakForward();
		}
		if (motor instanceof EpicsMotor) {
			EpicsMotor epicsmotor = (EpicsMotor)motor;
			if (epicsmotor.isMoving()) {
				// bypass GDA motor instance and control EPICS Tweak PV directly
				getChannel(getPvRoot() + TWEAK_FORWARD).ifPresent(this::applyTweak);
			} else {
				epicsmotor.moveBy(increment);
			}
		}
	}

	@Override
	public void reverse() throws MotorException {
		double minPosition = motor.getMinPosition();
		double position = motor.getPosition();
		if ((position + increment) < minPosition) {
			throw new MotorException(MotorStatus.LOWER_LIMIT, getName() + ": tweak forward exceeds motor's minimum position " + minPosition);
		}
		if (motor instanceof DummyMotor) {
			((DummyMotor) motor).tweakReverse();
		}
		if (motor instanceof EpicsMotor) {
			EpicsMotor epicsmotor = (EpicsMotor)motor;
			if (epicsmotor.isMoving()) {
				getChannel(getPvRoot() + TWEAK_REVERSE).ifPresent(this::applyTweak);
			} else {
				epicsmotor.moveBy(-increment);
			}
		}
	}

	@Override
	public void setIncrement(double value) {
		increment = value;
		if (motor instanceof DummyMotor) {
			((DummyMotor) motor).setTweakSize(value);
		}
		if (motor instanceof EpicsMotor) {
			getChannel(getPvRoot() + TWEAK_STEP).ifPresent(channel -> setValue(channel, value));
		}
	}

	private void setValue(Channel channel, double value) {
		try {
			EPICS_CONTROLLER.caput(channel, value);
		} catch (CAException e) {
			logger.error("set increment failed. ", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("set increment being interrupted. ", e);
		}
	}

	@Override
	public double getIncrement() {
		if (motor instanceof DummyMotor) {
			return ((DummyMotor) motor).getTweakSize();
		}
		if (motor instanceof EpicsMotor) {
			Optional<Double> map = getChannel(getPvRoot() + TWEAK_STEP).map(this::getValue);
			if (map.isPresent()) {
				return map.get();
			}
		}
		return increment;
	}

	/**
	 * post property set method to ensure required bean properties are set.
	 */
	@PostConstruct
	public void aftePropertiesSet() {
		Assert.notNull(motor, "Motor must not be null.");
	}

	/**
	 * clean up the resources - used for bean life cycle end call.
	 */
	@PreDestroy
	public void destroy() {
		channelMap.entrySet().stream().forEach(e -> {
			try {
				e.getValue().destroy();
			} catch (IllegalStateException | CAException e1) {
				logger.error("destroy channel for {} failed. ", e.getKey(), e1);
			}
		});
		channelMap.clear();
	}

	/**
	 * Lazy initialize channels and store them in a map for retrieval later. Intentionally designed to cope with channel creation failure. This way it will not
	 * block data collection from other PVs.
	 *
	 * @param pv
	 * @return channel - Optional<Channel>
	 */
	private Optional<Channel> getChannel(String pv) {
		ThrowingFunction<String, Channel> f = EPICS_CONTROLLER::createChannel;
		Channel channel = null;
		try {
			channel = channelMap.computeIfAbsent(pv, f);
			logger.trace("Created channel for PV: {}", pv);
		} catch (RuntimeException e) {
			logger.error("Error create Channel Access for {}", pv, e);
		}
		return Optional.ofNullable(channel);
	}

	private void applyTweak(Channel channel1) {
		try {
			EPICS_CONTROLLER.caput(channel1, 1);
		} catch (CAException e) {
			logger.error("apply tweak failed. ", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("apply tweak being interrupted. ", e);
		}
	}

	public String getPvRoot() {
		if (motor instanceof EpicsMotor) {
			return ((EpicsMotor)motor).getPvName();
		} else {
			throw new IllegalStateException("Instance of EpicsMotor is not available!");
		}
	}

	private Double getValue(Channel channel) {
		try {
			return EPICS_CONTROLLER.cagetDouble(channel);
		} catch (CAException | TimeoutException e) {
			logger.error("get increment failed. ", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("get increment being interrupted. ", e);
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(increment);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MotorPositionTweakControl other = (MotorPositionTweakControl) obj;
		return Double.doubleToLongBits(increment) == Double.doubleToLongBits(other.increment);
	}

	@Override
	public String getControllerRecordName() {
		return getPvRoot();
	}

}
