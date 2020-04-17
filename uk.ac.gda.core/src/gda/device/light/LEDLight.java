/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.device.light;

import java.util.Objects;

import gda.device.DeviceException;
import gda.device.ILEDLight;
import gda.device.Scannable;
import gda.factory.FindableBase;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * Represents and controls a LED light. The LED light should have either On/Off switch, or brightness control, or both.
 *
 * <p>
 * Usage example:
 * <pre>
 * Server-side beans:
 *
 * {@code
 * <bean id="cl1_switch" class="gda.device.enumpositioner.EpicsSimpleBinary">
 *	<property name="pvName" value="BL06J-EA-DDIFF-01:LED1:TOGGLE"/>
 * </bean>
 * <bean id="cl1_intensity" class="gda.device.scannable.PVScannable">
 *	<property name="pvName" value="BL06J-EA-DDIFF-01:LED1:PWMDEMAND"/>
 * </bean>
 * <bean id="cl1"  class="gda.device.light.LEDLight" init-method="afterPropertiesSet">
 * 	<property name="lightSwitch" ref="cl1_switch"/>
 * 	<property name="lightIntensity" ref="cl1_intensity"/>
 * 	<property name="hasSwitch" value="true"/>
 * 	<property name="hasBrightnessControl" value="true"/>
 * </bean>
 * }
 * </pre>
 *
 * @since 9.16
 */
@ServiceInterface(ILEDLight.class)
public class LEDLight extends FindableBase implements ILEDLight {
	private Scannable lightSwitch;
	private Scannable lightIntensity;
	private boolean hasSwitch;
	private boolean hasBrightnessControl;

	@Override
	public void on() throws DeviceException {
		if (hasSwitch) lightSwitch.moveTo("On");
		else throw new DeviceException(getName() + " doesn't have switch control.");
	}

	@Override
	public void off() throws DeviceException {
		if (hasSwitch) lightSwitch.moveTo("Off");
		else throw new DeviceException(getName() + " doesn't have switch control.");
	}

	@Override
	public void setBrightness(double v) throws DeviceException {
		if (hasBrightnessControl) lightIntensity.asynchronousMoveTo(v);
		else throw new DeviceException(getName() + " doesn't have brightness control.");
	}

	@Override
	public double getBrightness() throws DeviceException {
		if (hasBrightnessControl) return (double) lightIntensity.getPosition();
		else throw new DeviceException(getName() + " doesn't have brightness control.");
	}

	public void afterPropertiesSet() {
		if (hasSwitch && Objects.isNull(lightSwitch))
			throw new IllegalStateException("lightSwitch scannable is not set.");
		if (hasBrightnessControl && Objects.isNull(lightIntensity))
			throw new IllegalStateException("lightIntensity scannable is not set.");
		if (!hasSwitch && !hasBrightnessControl)
			throw new IllegalStateException("hasSwitch and hasBrighnessControl cannot be both false.");
	}

	public Scannable getLightSwitch() {
		return lightSwitch;
	}

	public void setLightSwitch(Scannable lightSwitch) {
		this.lightSwitch = lightSwitch;
	}

	public Scannable getLightIntensity() {
		return lightIntensity;
	}

	public void setLightIntensity(Scannable lightIntensity) {
		this.lightIntensity = lightIntensity;
	}

	@Override
	public boolean hasSwitch() {
		return hasSwitch;
	}

	@Override
	public boolean hasBrightnessControl() {
		return hasBrightnessControl;
	}

	public void setHasSwitch(boolean hasSwitch) {
		this.hasSwitch = hasSwitch;
	}

	public void setHasBrightnessControl(boolean hasBrightnessControl) {
		this.hasBrightnessControl = hasBrightnessControl;
	}

}
