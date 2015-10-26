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

package gda.device.zebra;

import java.util.List;
import java.util.concurrent.Callable;

import org.jscience.physics.quantities.Energy;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.NonSI;
import org.jscience.physics.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.device.DeviceException;
import gda.device.Motor;
import gda.device.continuouscontroller.ContinuousMoveController;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.device.scannable.PositionCallableProvider;
import gda.device.scannable.ScannableMotor;
import gda.util.converters.JEPConverterHolder;
// TODO: Rename class to indicate that it just converts from one positino type to another

// TODO is this class necessary? could move the conversion functions into ZebraConstantVelocityMoveControllerForQexafs
// and then get rid of this. But maybe it's useful as a place to do the readout conversion, when that's done?
/*
 * This class is a decorator for Qexafs scan, it calculates the correct positions (start, end and step) for the Bragg angle and adds 
 * this information for the scannableMotor in order to configure Zebra
 */
public class ZebraScannableMotorForQexafs extends ScannableMotor implements ContinuouslyScannableViaController,
		ZebraMotorInfoProvider, PositionCallableProvider<Double>, InitializingBean {

	// zebraScannableMotor is Energy but this PV will move the DCM perpendicular motor and Bragg angle 
	private ZebraConstantVelocityMoveControllerForQexafs continuousMoveController;
	private ZebraScannableMotor zebraScannableMotor;
	private ScannableMotor braggScannableMotor;
	private JEPConverterHolder converter;

	@Override
	public void configure() {
		// Overriding ScannableMotor#configure() since we don't actually have a motor to wrap
		// (Does this class actually need to extend ScannableMotor or could it extend something else which is not intended
		// to wrap a single physical motor?)
		this.setInputNames(new String[] { "Energy" });
		configured = true;
	}

	@Override
	public ContinuousMoveController getContinuousMoveController() {
		return continuousMoveController;
	}

	public void setZebraConstantVelocityMoveControllerForQexafs(
			ZebraConstantVelocityMoveControllerForQexafs zebraConstantVelocityMoveControllerForQexafs) {
		this.continuousMoveController = zebraConstantVelocityMoveControllerForQexafs;
	}

	private static final Logger logger = LoggerFactory.getLogger(ZebraScannableMotorForQexafs.class);

	public ScannableMotor getBraggScannableMotor() {
		return braggScannableMotor;
	}

	public void setBraggScannableMotor(ScannableMotor braggScannableMotor) {
		this.braggScannableMotor = braggScannableMotor;
	}

	public ZebraScannableMotor getZebraScannableMotor() {
		return zebraScannableMotor;
	}

	public void setZebraScannableMotor(ZebraScannableMotor zebraScannableMotor) {
		this.zebraScannableMotor = zebraScannableMotor;
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		// TODO: Used during step scan ---ad converter
		zebraScannableMotor.asynchronousMoveTo(position);
	}

	@Override
	public Object getPosition() throws DeviceException {
		// TODO: Used during step scan ---add converter
		return zebraScannableMotor.getPosition();
	}
	
	public JEPConverterHolder getConverter() {
		return converter;
	}

	public void setConverter(JEPConverterHolder converter) {
		this.converter = converter;
	}

	@Override
	public double distanceToAccToVelocity(double requiredSpeed) {
		// TODO: Does this need converting from energy to angle?
		return zebraScannableMotor.distanceToAccToVelocity(requiredSpeed);
	}

	@Override
	public int getPcEnc() {
		return zebraScannableMotor.getPcEnc();
	}

	@Override
	public ScannableMotor getActualScannableMotor() {
		return zebraScannableMotor.getActualScannableMotor();
	}

	@Override
	public boolean isExposureStepDefined() {
		return zebraScannableMotor.isExposureStepDefined();
	}

	@Override
	public double getExposureStep() {
		return zebraScannableMotor.getExposureStep();
	}

	@Override
	public void setOperatingContinuously(boolean b) throws DeviceException {
		// TODO Do we not need to set this too - at least for the generic case.

	}

	@Override
	public boolean isOperatingContinously() {
		return zebraScannableMotor.isOperatingContinously();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		//zebraScannableMotor.afterPropertiesSet();
	}


	@Override
	public Callable<Double> getPositionCallable() throws DeviceException {
		Callable<Double> zebraPositionCallable = zebraScannableMotor.getPositionCallable();
		return new EnergyCallable(zebraPositionCallable);
	}

	public double convertEnergyToBraggAngle(double energy) throws Exception{		
		Energy energyEV = Quantity.valueOf(energy, NonSI.ELECTRON_VOLT);
		Quantity angle = converter.toTarget(energyEV);
		return (-1*(angle.getAmount())/1000);
	}

	public double convertBraggAngleToEnergy(double angledeg) throws Exception{
		Unit<?> mDegree = Unit.valueOf("mDeg");
		Quantity anglemDegree = Quantity.valueOf(Math.abs(angledeg),mDegree);
		Quantity energyEV = converter.toSource(anglemDegree);
		return energyEV.getAmount();
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		return;
	}

	@Override
	public void atScanStart() throws DeviceException {
		// Due to the fragility of ZebraConstantVelocityMoveController and particulary the life cycle of
		// its parts which read out time series arrays: here we will clear these so that controller this 
		//ZebraScannableMotor returns is fit to be used by the scan.
		zebraScannableMotor.atScanStart();
	}

	public double getScurveTimeToVelocity() {
		return zebraScannableMotor.getScurveTimeToVelocity();
	}

	/**
	 * 
	 * @param scurveTimeToVelocity - if using linear acceleration this is the ACCEL field of the 
	 * motor in EPICS (Time to Velocity)
	 */
	public void setScurveTimeToVelocity(double scurveTimeToVelocity) {
		zebraScannableMotor.setScurveTimeToVelocity(scurveTimeToVelocity);
	}

	public void setPcEnc(int pcEnc) {
		zebraScannableMotor.setPcEnc(pcEnc);
	}

	class EnergyCallable implements Callable<Double>{
		Callable<Double> braggAngleCallable;

		public EnergyCallable(Callable<Double> braggAngle){
				this.braggAngleCallable = braggAngle;
		}
		@Override
		public Double call() throws Exception{
			double energy;
			// needs to convert Bragg angle into energy
			energy = convertBraggAngleToEnergy(braggAngleCallable.call().doubleValue()*1000);
			return new Double(energy);
		}
	}

}
