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

package gda.device.enumpositioner;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import gda.device.DeviceException;
import gda.device.EnumPositionerStatus;
import gda.device.Scannable;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.observable.IObserver;

/**
 * A class that acts as an EnumPositioner. It monitors a scannable that gives the current value of the 'hardware' in
 * internal form. The class uses EnumValueMapper to map the external to internal forms moveTo accepts the position in
 * external form. If a control scannable is provided then the moveTo calls moveTo on the control scannable with position
 * set to the value in internal form If setCmdTemplate is provided then moveTo evaluates the command :
 * String.format(getSetCmdTemplate(),newExternalValue, getInternalValue(newExternalValue)) Only one of control or
 * setCmdTemplate can be set
 */
public abstract class MapperBasedEnumPositionerBase<T> extends EnumPositionerBase implements InitializingBean {

	EnumValueMapper<T> mapper;
	Scannable monitor;
	Scannable control;
	private String setCmdTemplate;

	public EnumValueMapper<T> getMapper() {
		return mapper;
	}

	public void setMapper(EnumValueMapper<T> mapper) {
		this.mapper = mapper;
	}

	private String getSetCmdTemplate() {
		return setCmdTemplate;
	}

	public void setSetCmdTemplate(String setCmdTemplate) {
		this.setCmdTemplate = setCmdTemplate;
	}

	/**
	 * @param monitor
	 *            Scannable getPosition returns Integer and notifies observers with arg set to Integer
	 */
	public void setMonitor(Scannable monitor) {
		this.monitor = monitor;
	}

	/**
	 * @param control
	 *            Scannable whose moveTo accepts an Integer
	 */
	public void setControl(Scannable control) {
		this.control = control;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (monitor == null)
			throw new IllegalArgumentException("monitor == null");
		if (mapper == null)
			throw new IllegalArgumentException("mapper == null");
		if (StringUtils.isEmpty(setCmdTemplate) && control == null)
			throw new IllegalArgumentException("setCmdTemplate and control are not set");
		if (!StringUtils.isEmpty(setCmdTemplate) && control != null)
			throw new IllegalArgumentException("Both setCmdTemplate and control are set");
	}


	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			this.inputNames = new String[] { getName() };
			this.outputFormat = new String[] { "%s" };

			monitor.addIObserver(new IObserver() {

				@Override
				public void update(Object source, Object arg) {
					ScannablePositionChangeEvent event = getScannablePositionChangeEvent(source, arg);
					if (event != null)
						notifyIObservers(MapperBasedEnumPositionerBase.this, event);
				}
			});
			setConfigured(true);
		}
	}

	protected abstract ScannablePositionChangeEvent getScannablePositionChangeEvent(Object source, Object arg);

	@Override
	public String checkPositionValid(Object position) {
		return mapper.isExternalValueValid(position.toString()) ? null : (position.toString() + " is invalid");
	}

	@Override
	public EnumPositionerStatus getStatus() throws DeviceException {
		return EnumPositionerStatus.IDLE; // the move completes in rawAsynchronousMoveTo
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		String newExternalValue = position.toString();
		if (getSetCmdTemplate() != null) {
			String cmd = String.format(getSetCmdTemplate(), newExternalValue, getInternalValue(newExternalValue));
			InterfaceProvider.getCommandRunner().evaluateCommand(cmd);
		} else if (control != null) {
			try {
				control.moveTo(getInternalValue(newExternalValue));
			} catch (IllegalArgumentException e) {
				throw new DeviceException(getName() + " : Error in rawAsynchronousMoveTo to "
						+ StringUtils.quote(newExternalValue), e);
			}
		}
	}

	@Override
	public String getPosition() throws DeviceException {
		try {
			return getExternalValueFromMonitor();
		} catch (IllegalArgumentException e) {
			throw new DeviceException(getName() + " : Error in getPosition", e);
		}

	}

	protected abstract String getExternalValueFromMonitor() throws IllegalArgumentException, DeviceException;

	public T getInternalValue(String externalValue) throws IllegalArgumentException {
		return mapper.getInternalValue(externalValue);

	}

	public String getExternalValue(T internalValue) throws IllegalArgumentException {
		return mapper.getExternalValue(internalValue);

	}

	@Override
	public String[] getPositions() throws DeviceException {
		return mapper.getExternalValues();
	}

}
