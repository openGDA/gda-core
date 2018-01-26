/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.pco;

import java.util.Random;

import gda.device.detector.IPCODiverController;
import gda.factory.Findable;

/**
 * simulate PCO Driver specific parameters by java bean.
 *
 */
public class DummyPCODriverController implements Findable, IPCODiverController {
	private int pixelRate;
	private PCOADCMode adcmode=PCOADCMode.OneADC;
	private double camRamUsage=1;
	private Random random=new Random();
	private double electricTemp=20.0;
	private double powersupplytemp=39;
	private int storageMode=1;
	private int recorderMode=1;
	private int timestampMode=1;
	private int acquireMode=1;
	private int armMode=1;
	private double delayTime;
	private String name;

	public DummyPCODriverController() {
	}

	@Override
	public int getADCMode() {
		return adcmode.ordinal();
	}

	@Override
	public void setADCMode(int value) {
		adcmode=PCOADCMode.values()[value];
	}
	@Override
	public void setADCMode(PCOADCMode value) {
		adcmode=value;
	}

	@Override
	public int getPixRate() {
		return pixelRate;
	}

	@Override
	public void setPixRate(int value) {
		pixelRate=value;
	}

	@Override
	public double getCamRamUsage() {
		return camRamUsage*random.nextInt(100);
	}

	@Override
	public double getElectronicTemperature() {
		return electricTemp+electricTemp*random.nextDouble();
	}

	@Override
	public double getPowerSupplyTemperature() {
		return powersupplytemp;
	}
	@Override
	public void afterPropertiesSet(){
		//No-Op
	}
	@Override
	public int getStorageMode() {
		return storageMode;
	}

	@Override
	public void setStorageMode(int value) {
		storageMode=value;
	}

	@Override
	public int getRecorderMode() {
		return recorderMode;
	}

	@Override
	public void setRecorderMode(int value) {
		recorderMode=value;
	}

	@Override
	public int getTimestampMode() {
		return timestampMode;
	}

	@Override
	public void setTimestampMode(int value) {
		timestampMode=value;
	}

	@Override
	public int getAcquireMode() {
		return acquireMode;
	}

	@Override
	public void setAcquireMode(int value) {
		acquireMode=value;
	}

	@Override
	public int getArmMode() {
		return armMode;
	}

	@Override
	public void armCamera() {
		setArmMode(1);
	}

	@Override
	public void disarmCamera() {
		setArmMode(0);
	}

	@Override
	public void setArmMode(int value) {
		armMode=value;
	}

	@Override
	public double getDelayTime() {
		return delayTime;
	}

	@Override
	public void setDelayTime(double value) {
		delayTime=value;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

}
