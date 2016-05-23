/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.zebra.controller.impl;

import java.io.IOException;

import gda.device.zebra.controller.SoftInputChangedEvent;
import gda.device.zebra.controller.Zebra;
import gda.epics.ReadOnlyPV;
import gda.factory.Findable;
import gda.observable.Observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class ZebraDummy implements Zebra, Findable, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(ZebraImpl.class);
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub

	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Findable method implementations
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//Set and get the bean name 
	private String name;

	@Override
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String getName() {
		return name;
	}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unused")
	public void encCopyMotorPosToZebra(int posNum) throws Exception {
	}
	
	@Override
	public double getPCPulseDelay() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPCPulseDelayRBV() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPCPulseDelay(double delay) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public double getPCPulseWidth() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPCPulseWidthRBV() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPCPulseWidth(double width) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public double getPCPulseStep() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPCPulseStepRBV() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPCPulseStep(double step) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPCPulseSource(int ordinal) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPCPulseSource() throws Exception, Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getPCGateSource() throws Exception, Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPCGateSource(int ordinal) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public double getPCGateStart() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPCGateStartRBV() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPCGateWidth() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getPCGateNumberOfGates() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPCGateStep() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPCGateStart(double start) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPCGateWidth(double width) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public double getPCGateWidthRBV() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPCGateNumberOfGates(int numberOfGates) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPCGateStep(double step) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPCArmSource() throws Exception {
		return 0;
	}

	@Override
	public void setPCArmSource(int ordinal) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isPCArmed() throws Exception {
		return false;
	}

	@Override
	public void pcArm() throws Exception {
		logger.info("Arm Zebra Box");

	}

	@Override
	public void pcDisarm() throws Exception {
		logger.info("Dis-Arm Zebra Box");

	}

	@Override
	public void setPCCaptureBitField(int val) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPCCaptureBitField() throws Exception {
		return 0;
	}

	@Override
	public void setPCEnc(int val) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPCEnc() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ReadOnlyPV<Double[]> getPcCapturePV(int capture) {
		// TODO Auto-generated method stub
		return null;
	}

	@Deprecated
	@Override
	public ReadOnlyPV<Double[]> getEnc1AvalPV() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReadOnlyPV<Integer> getNumberOfPointsCapturedPV() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPCNumberOfPointsCaptured(int val) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPCNumberOfPointsCaptured() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPCTimeUnit(int i) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPCTimeUnit() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPCPulseMax(int numberTriggers) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPCPulseMax() throws Exception, Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ReadOnlyPV<Integer> getNumberOfPointsDownloadedPV() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReadOnlyPV<Double[]> getPCTimePV() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPCDir(int i) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPCDir() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPulseInput(int pulseId, int input) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPulseDelay(int pulseId, double delay) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPulseWidth(int pulseId, double width) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPulseTimeUnit(int pulseId, int timeunit) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOutTTL(int outId, int val) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getTtlOutputState(int output) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setPCArmInput(int input) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSoftInputSet(int inputNumber) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setSoftInput(int inputNumber, boolean set) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public Observable<SoftInputChangedEvent> getSoftInputObservable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reset() throws IOException {
		// TODO Auto-generated method stub
	}
}
