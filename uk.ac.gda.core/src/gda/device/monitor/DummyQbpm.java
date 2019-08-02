/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.device.monitor;

import java.util.Random;

import gda.device.DeviceException;
import gda.device.Qbpm;
import gda.device.enumpositioner.DummyEnumPositioner;

public class DummyQbpm extends DummyEnumPositioner implements Qbpm {

	private String bpmName;
	private String currAmpQuadName;
	private final Random rng = new Random();
	private double intensity;
	private double xPosition;
	private double yPosition;

	public DummyQbpm() {
		addPosition("Auto");
		addPosition("1mA");
		addPosition("100uA");
		addPosition("10uA");
		addPosition("1uA");
		addPosition("100nA");
		addPosition("10nA");
		addPosition("1nA");
		addPosition("100pA");
	}

	@Override
	public double getCurrent1() throws DeviceException {
		return rng.nextGaussian();
	}

	@Override
	public double getCurrent2() throws DeviceException {
		return rng.nextGaussian();
	}

	@Override
	public double getCurrent3() throws DeviceException {
		return rng.nextGaussian();
	}

	@Override
	public double getCurrent4() throws DeviceException {
		return rng.nextGaussian();
	}

	@Override
	public String getRangeValue() throws DeviceException {
		return getPosition();
	}

	@Override
	public double getIntensityTotal() throws DeviceException {
		return rng.nextGaussian() + intensity;
	}

	@Override
	public double getXPosition() throws DeviceException {
		return xPosition + rng.nextGaussian();
	}

	@Override
	public double getYPosition() throws DeviceException {
		return yPosition + rng.nextGaussian();
	}

	@Override
	public String getBpmName() throws DeviceException {
		return bpmName;
	}

	@Override
	public void setBpmName(String name) throws DeviceException {
		bpmName = name;
	}

	@Override
	public String getCurrAmpQuadName() throws DeviceException {
		return currAmpQuadName;
	}

	@Override
	public void setCurrAmpQuadName(String name) throws DeviceException {
		currAmpQuadName = name;
	}

	public void setXPosition(double x) {
		xPosition = x;
	}

	public void setYPosition(double y) {
		yPosition = y;
	}
}
