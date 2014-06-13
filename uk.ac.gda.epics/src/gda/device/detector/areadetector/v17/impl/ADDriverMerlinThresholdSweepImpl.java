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

package gda.device.detector.areadetector.v17.impl;

import gda.device.detector.areadetector.v17.ADDriverMerlinThresholdSweep;
import gda.epics.LazyPVFactory;
import gda.epics.NoCallbackPV;
import gda.epics.PV;
import gda.epics.PVWithSeparateReadback;

import java.io.IOException;

import org.springframework.beans.factory.InitializingBean;

public class ADDriverMerlinThresholdSweepImpl implements ADDriverMerlinThresholdSweep, InitializingBean {

	private String basePVName;
	private boolean useTriggerModeNotStartThresholdScanning;
	
	private PV<Double> thresholdScanPVPair;
	private PV<Double> startThresholdScanPVPair;
	private PV<Double> stopThresholdScanPVPair;
	private PV<Double> stepThresholdScanPVPair;

	private NoCallbackPV<Boolean> startThresholdScanningPV;

	public String getBasePVName() {
		return basePVName;
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (getBasePVName() == null) {
			throw new IllegalArgumentException("'basePVName' needs to be declared");
		}
		createLazyPvs();
	}

	private void createLazyPvs() {
		
		thresholdScanPVPair = new PVWithSeparateReadback<Double>(
				LazyPVFactory.newDoublePV(basePVName + "ThresholdScan"),
				LazyPVFactory.newReadOnlyDoublePV(basePVName + "ThresholdScan_RBV"));
		
		startThresholdScanPVPair = new PVWithSeparateReadback<Double>(
				LazyPVFactory.newDoublePV(basePVName +"StartThresholdScan"),
				LazyPVFactory.newReadOnlyDoublePV(basePVName +"StartThresholdScan_RBV"));
		
		stopThresholdScanPVPair = new PVWithSeparateReadback<Double>(
				LazyPVFactory.newDoublePV(basePVName +"StopThresholdScan"),
				LazyPVFactory.newReadOnlyDoublePV(basePVName +"StopThresholdScan_RBV"));
		
		stepThresholdScanPVPair = new PVWithSeparateReadback<Double>(
				LazyPVFactory.newDoublePV(basePVName +"StepThresholdScan"),
				LazyPVFactory.newReadOnlyDoublePV(basePVName +"StepThresholdScan_RBV"));

		if (!isUseTriggerModeNotStartThresholdScanning()) {
			startThresholdScanningPV = LazyPVFactory.newNoCallbackBooleanFromIntegerPV(basePVName +"StartThresholdScanning");
		}
	}

	@Override
	public Double getNumber() throws IOException {
		return thresholdScanPVPair.get();
	}

	@Override
	public void setNumber(Double number) throws IOException {
		thresholdScanPVPair.putWait(number);
	}

	@Override
	public Double getStart() throws IOException {
		return startThresholdScanPVPair.get();
	}

	@Override
	public void setStart(Double start) throws IOException {
		startThresholdScanPVPair.putWait(start);
	}

	@Override
	public Double getStop() throws IOException {
		return stopThresholdScanPVPair.get();
	}

	@Override
	public void setStop(Double stop) throws IOException {
		stopThresholdScanPVPair.putWait(stop);
	}

	@Override
	public Double getStep() throws IOException {
		return stepThresholdScanPVPair.get();
	}

	@Override
	public void setStep(Double step) throws IOException {
		stepThresholdScanPVPair.putWait(step);
	}
	
	@Override
	public NoCallbackPV<Boolean> getStartThresholdScanningPV() {
		return startThresholdScanningPV;
	}

	@Override
	public int getNumberPointsPerSweep() throws IOException {
		double distance = getStop() - getStart();
		Double step = getStep();
		distance += step / 100.;  // Add 1% so that we include the last point in the 50% chance of rounding error
		return (int) Math.floor(distance / step) + 1;
	}

	@Override
	public boolean isUseTriggerModeNotStartThresholdScanning() {
		return useTriggerModeNotStartThresholdScanning;
	}

	public void setUseTriggerModeNotStartThresholdScanning(boolean useTriggerModeNotStartThresholdScanning) {
		this.useTriggerModeNotStartThresholdScanning = useTriggerModeNotStartThresholdScanning;
	}
}