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

import static si.uom.NonSI.DEGREE_ANGLE;
import static si.uom.NonSI.ELECTRON_VOLT;

import java.util.Map;
import java.util.concurrent.Callable;

import javax.measure.Quantity;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Energy;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.continuouscontroller.ContinuousMoveController;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.device.scannable.PositionCallableProvider;
import gda.device.scannable.ScannableMotor;
import gda.util.converters.JEPConverterHolder;
import tec.units.indriya.quantity.Quantities;
// TODO: Rename class to indicate that it just converts from one position type to another

// TODO is this class necessary? could move the conversion functions into ZebraConstantVelocityMoveControllerForQexafs
// and then get rid of this. But maybe it's useful as a place to do the readout conversion, when that's done?
/*
 * This class is a decorator for Qexafs scan, it calculates the correct positions (start, end and step) for the Bragg angle and adds
 * this information for the scannableMotor in order to configure Zebra
 */
public class ZebraScannableMotorForQexafs extends ScannableMotor implements ContinuouslyScannableViaController,
		ZebraMotorInfoProvider, PositionCallableProvider<Double> {

	// zebraScannableMotor is Energy but this PV will move the DCM perpendicular motor and Bragg angle
	private ZebraConstantVelocityMoveControllerForQexafs continuousMoveController;
	private ZebraScannableMotor zebraScannableMotor;
	private ScannableMotor braggScannableMotor;
	private JEPConverterHolder<Energy, Angle> converter;
	private Map<String, JEPConverterHolder<Energy, Angle>> delegateConverters;
	private EnumPositioner selector;

	@Override
	public void configure() {
		// Overriding ScannableMotor#configure() since we don't actually have a motor to wrap
		// (Does this class actually need to extend ScannableMotor or could it extend something else which is not intended
		// to wrap a single physical motor?)
		if (isConfigured()) {
			return;
		}
		this.setInputNames(new String[] { "Energy" });
		setConfigured(true);
	}

	@Override
	public ContinuousMoveController getContinuousMoveController() {
		return continuousMoveController;
	}

	public void setZebraConstantVelocityMoveControllerForQexafs(
			ZebraConstantVelocityMoveControllerForQexafs zebraConstantVelocityMoveControllerForQexafs) {
		this.continuousMoveController = zebraConstantVelocityMoveControllerForQexafs;
	}

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

	public void setDelegateConverters(Map<String, JEPConverterHolder<Energy, Angle>> delegateConverters){
		this.delegateConverters = delegateConverters;
	}

	public void setSelector(EnumPositioner selector) {
		this.selector = selector;
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		zebraScannableMotor.asynchronousMoveTo(position);
	}

	@Override
	public Object getPosition() throws DeviceException {
		return zebraScannableMotor.getPosition();
	}

	public JEPConverterHolder<Energy, Angle> getConverter() {
		return converter;
	}

	@Override
	public double distanceToAccToVelocity(double requiredSpeed) {
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
		// do nothing i guess
	}

	@Override
	public boolean isOperatingContinously() {
		return zebraScannableMotor.isOperatingContinously();
	}


	@Override
	public Callable<Double> getPositionCallable() throws DeviceException {
		Callable<Double> zebraPositionCallable = zebraScannableMotor.getPositionCallable();
		return new EnergyCallable(zebraPositionCallable);
	}

	protected double convertEnergyToBraggAngle(double energy) throws Exception {
		Quantity<Energy> energyEV = Quantities.getQuantity(energy, ELECTRON_VOLT);
		Quantity<Angle> angle = getDelegateConverter().toTarget(energyEV).to(DEGREE_ANGLE);
		return angle.getValue().doubleValue();
	}

	protected double convertBraggAngleToEnergy(double angle) throws Exception {
		Quantity<Angle> angleDegree = Quantities.getQuantity(Math.abs(angle), DEGREE_ANGLE);
		Quantity<Energy> energyEV = getDelegateConverter().toSource(angleDegree).to(ELECTRON_VOLT);
		return energyEV.getValue().doubleValue();
	}

	@Override
	public void waitWhileBusy() throws DeviceException, InterruptedException {
		// never busy
	}

	@Override
	public void atScanEnd() throws DeviceException {
		zebraScannableMotor.atScanEnd();
		continuousMoveController.atScanEnd();
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
			// needs to convert Bragg angle into energy
			return convertBraggAngleToEnergy(braggAngleCallable.call().doubleValue());
		}
	}

	@Override
	public void setContinuousMoveController(ContinuousMoveController controller) {
		throw new IllegalArgumentException("setContinuousMoveController(" + controller.getName() + ") not supported on " + this.getName());
	}

	@Override
	public void stop() throws DeviceException {
		zebraScannableMotor.getActualScannableMotor().stop();
	}

	/**
	 * Get the current converter from a map, using the selector position as the key.
	 * If the selector position does not exists in the map of converters then the first
	 * entry in the map will be returned.
	 * */
	private JEPConverterHolder<Energy, Angle> getDelegateConverter() {
		try {
			// Use the selector position to get the converter to use - i.e. Si111 or Si311
			converter = delegateConverters.get(selector.getPosition());
		}catch(Exception ex) {
			// If the selector cannot find the converter from the map then use the first entry in the map
			converter = delegateConverters.entrySet().stream().findFirst().get().getValue();
		}
		return converter;
	}
}
