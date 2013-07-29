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

import gda.device.DeviceException;
import gda.device.motor.simplemotor.SimpleIndexedMotorController;
import gda.io.BidiAsciiCommunicator;

import org.springframework.beans.factory.InitializingBean;

public class P2RMotorController implements SimpleIndexedMotorController, InitializingBean{

	BidiAsciiCommunicator bidiAsciiCommunicator;
	
	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isBusy() throws DeviceException {
		updateStatus();
		return busy;
	}

	@Override
	public void moveTo(double position, int index) throws DeviceException {
		updateStatus(); //to get current position for other motor
		String msg = String.format("M%f, %f,",index==0? position: m[0], index==1? position: m[1] );
		String reply = bidiAsciiCommunicator.send(msg);
		if( !reply.equals(msg)){
			throw new DeviceException("Error sending moveTo command '" + msg + "' reply = '" + reply + "'");
		}
		busy = true;
	}

	@Override
	public double getPosition(int index) throws DeviceException {
		updateStatus();
		return m[index];
	}
	
	void updateStatus() throws DeviceException {
		String reply = bidiAsciiCommunicator.send("S");
		boolean busy;
		Double m1, m2;
		if(reply.startsWith("T")){
			busy = true;
		} else if (reply.startsWith("F")){
			busy = false;
		} else {
			throw new DeviceException("Error returned from reading status. Reply = '" + reply + "'");
		}
		String substring = reply.substring(1, reply.length());
		String[] split = substring.split(",");
		if( split.length==2){
			m1 = Double.valueOf(split[0]);
			m2 = Double.valueOf(split[1]);
		} else {
			throw new DeviceException("Error returned from reading status. Reply = '" + reply+"'");
		}
		setStatus(busy, m1, m2);
	}

	boolean busy=false;
	double m[]=new double[]{0., 0.};
	
	private void setStatus(boolean busy, Double m1, Double m2) {
		this.busy = busy;
		this.m[0] = m1;
		this.m[1] = m2;
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if(bidiAsciiCommunicator == null){
			throw new Exception("conn = null");
		}
		
	}

	public BidiAsciiCommunicator getBidiAsciiCommunicator() {
		return bidiAsciiCommunicator;
	}

	public void setBidiAsciiCommunicator(BidiAsciiCommunicator bidiAsciiCommunicator) {
		this.bidiAsciiCommunicator = bidiAsciiCommunicator;
	}

	@Override
	public void setSpeed(double speed, int index) throws DeviceException {
		// do nothing - not supported
	}

	@Override
	public double getSpeed(int index) throws DeviceException {
		// do nothing - not supported
		return 0;
	}



}
