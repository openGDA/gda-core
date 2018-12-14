/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.devices.p2r;

import org.springframework.beans.factory.InitializingBean;

import gda.device.DeviceException;
import gda.device.motor.simplemotor.SimpleMotorController;
import gda.io.BidiAsciiCommunicator;

public class P2RMotorController implements SimpleMotorController, InitializingBean {

	BidiAsciiCommunicator bidiAsciiCommunicator;
	private String prefix;
	boolean busy = false;
	double m[] = new double[] { 0., 0., 0., 0. ,0.};
	private int position_index = -1;
	private int speed_index = -1;
	private boolean checkReply = true;

	@Override
	public void stop() throws DeviceException {
		sendAndCheckReply(String.format("AB"));
	}

	@Override
	public boolean isBusy() throws DeviceException {
		updateStatus();
		return busy;
	}

	@Override
	public void moveTo(double position) throws DeviceException {
		sendAndCheckReply(String.format("M%s%f", prefix, position));
		sendAndCheckReply(String.format("MS"));
		busy = true;

	}

	private void sendAndCheckReply(String msg) throws DeviceException {
		String reply = bidiAsciiCommunicator.send(msg);
		if (checkReply && !reply.equals(msg)) {
			throw new DeviceException("Error sending moveTo command '" + msg + "' reply = '" + reply + "'");
		}
	}
	/**
	 * Send ST
	 * Get
	 *
	 * [T|F],Displacement(0), Rotation(1), Force(2), Speed of displacement(3), Speed of rotation(4)
	 * @throws DeviceException
	 */
	void updateStatus() throws DeviceException {
		String reply = bidiAsciiCommunicator.send("ST");
		boolean busy;
		Double m1, m2, m3, m4,m5;
		if (reply.startsWith("T")) {
			busy = true;
		} else if (reply.startsWith("F")) {
			busy = false;
		} else {
			throw new DeviceException("Error returned from reading status. Reply = '" + reply + "'");
		}
		String substring = reply.substring(1, reply.length());
		String[] split = substring.split(",");
		if (split.length == 5) {
			m1 = Double.valueOf(split[0]);
			m2 = Double.valueOf(split[1]);
			m3 = Double.valueOf(split[2]);
			m4 = Double.valueOf(split[3]);
			m5 = Double.valueOf(split[4]);
		} else {
			throw new DeviceException("Error returned from reading status. Reply = '" + reply + "'");
		}
		setStatus(busy, m1, m2, m3, m4,m5);
	}

	private void setStatus(boolean busy, Double m1, Double m2, Double m3, Double m4, Double m5) {
		this.busy = busy;
		this.m[0] = m1;
		this.m[1] = m2;
		this.m[2] = m3;
		this.m[3] = m4;
		this.m[4] = m5;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (bidiAsciiCommunicator == null) {
			throw new Exception("conn = null");
		}
		if (prefix == null) {
			throw new Exception("prefix = null");
		}
		if (position_index < 0 || position_index > 4) {
			throw new Exception("position_index <0 || position_index > 4");
		}
		if (speed_index < 0 || speed_index > 4) {
			throw new Exception("speed_index <0 || speed_index > 4");
		}
	}

	public BidiAsciiCommunicator getBidiAsciiCommunicator() {
		return bidiAsciiCommunicator;
	}

	public void setBidiAsciiCommunicator(BidiAsciiCommunicator bidiAsciiCommunicator) {
		this.bidiAsciiCommunicator = bidiAsciiCommunicator;
	}

	@Override
	public double getMotorPosition() throws DeviceException {
		updateStatus();
		return m[position_index];
	}

	@Override
	public void setSpeed(double speed) throws DeviceException, InterruptedException {
		sendAndCheckReply(String.format("S%s%f", prefix, speed));
		sendAndCheckReply(String.format("SS"));
		while (isBusy()) {
			Thread.sleep(50);
		}
	}

	@Override
	public double getSpeed() throws DeviceException {
		updateStatus();
		return m[speed_index];
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public int getPosition_index() {
		return position_index;
	}

	public void setPosition_index(int position_index) {
		this.position_index = position_index;
	}

	public int getSpeed_index() {
		return speed_index;
	}

	public void setSpeed_index(int speed_index) {
		this.speed_index = speed_index;
	}

	public boolean isCheckReply() {
		return checkReply;
	}

	public void setCheckReply(boolean checkReply) {
		this.checkReply = checkReply;
	}
}
