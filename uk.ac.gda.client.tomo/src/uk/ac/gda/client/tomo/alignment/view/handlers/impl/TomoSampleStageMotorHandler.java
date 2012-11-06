/*-
. * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.alignment.view.handlers.impl;

import gda.device.DeviceException;
import gda.device.IScannableMotor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.TomoClientActivator;
import uk.ac.gda.client.tomo.alignment.view.controller.TomoAlignmentController;
import uk.ac.gda.client.tomo.alignment.view.handlers.ISampleStageMotorHandler;
import uk.ac.gda.client.tomo.preferences.TomoAlignmentPreferencePage;

/**
 * Class which handles motors commands
 */
public class TomoSampleStageMotorHandler extends BaseMotorHandler implements ISampleStageMotorHandler {

	private IScannableMotor rotationScannable;
	private IScannableMotor ss1TxScannable;
	private IScannableMotor ss1TzScannable;
	private IScannableMotor ss1Y2Scannable;
	private IScannableMotor sampleHolderScannable;
	private IScannableMotor ss1RxScannable;
	private IScannableMotor ss1RzScannable;
	private static final Logger logger = LoggerFactory.getLogger(TomoSampleStageMotorHandler.class);
	private TomoAlignmentController tomoAlignmentViewController;


	/**
	 * Generally expected to be zero but this may be the initial angle of the 'theta' motor
	 */
	private double thethaOffset;
	private Double defaultDistanceToMoveForFlat;
	/**
	 * The absolute position of the sample stage. This value is only used for calculations.
	 */
	private Double defaultSampleInPosition;

	public TomoSampleStageMotorHandler() {
	
	}

	/**
	 * @return Returns the rotationScannable.
	 */
	public IScannableMotor getRotationScannable() {
		return rotationScannable;
	}

	/**
	 * @param rotationScannable
	 *            The rotationScannable to set.
	 */
	public void setRotationScannable(IScannableMotor rotationScannable) {
		this.rotationScannable = rotationScannable;
	}

	/**
	 * @return Returns the txScannable.
	 */
	public IScannableMotor getSs1TxScannable() {
		return ss1TxScannable;
	}

	/**
	 * @param txScannable
	 *            The txScannable to set.
	 */
	public void setSs1TxScannable(IScannableMotor txScannable) {
		this.ss1TxScannable = txScannable;
	}

	/**
	 * @return Returns the tzScannable.
	 */
	public IScannableMotor getSs1TzScannable() {
		return ss1TzScannable;
	}

	/**
	 * @param tzScannable
	 *            The tzScannable to set.
	 */
	public void setSs1TzScannable(IScannableMotor tzScannable) {
		this.ss1TzScannable = tzScannable;
	}

	/**
	 * @return Returns the y2Scannable.
	 */
	public IScannableMotor getSs1Y2Scannable() {
		return ss1Y2Scannable;
	}

	/**
	 * @param y2Scannable
	 *            The y2Scannable to set.
	 */
	public void setSs1Y2Scannable(IScannableMotor y2Scannable) {
		this.ss1Y2Scannable = y2Scannable;
	}

	public IScannableMotor getSampleHolderScannable() {
		return sampleHolderScannable;
	}

	public void setSampleHolderScannable(IScannableMotor sampleHolderScannable) {
		this.sampleHolderScannable = sampleHolderScannable;
	}

	@Override
	public double getSampleBaseMotorPosition() throws DeviceException {
		Object position = sampleHolderScannable.getPosition();
		if (position instanceof Double) {
			return ((Double) position).doubleValue();
		}
		return Double.MIN_VALUE;
	}

	@Override
	public void dispose() {
		logger.debug("Disposing TomoAlignmentMotorHandler");
	}

	@Override
	public double getVerticalPosition() throws DeviceException {
		return (Double) ss1Y2Scannable.getPosition();
	}

	@Override
	public Double getRotationMotorDeg() throws DeviceException {
		return (Double) rotationScannable.getPosition();
	}

	// @Override
	// public void update(Object source, Object arg) {
	// try {
	// if (source == this.rotationScannable && arg instanceof ScannableStatus) {
	// int status = ((ScannableStatus) arg).getStatus();
	// if (status > 0) {
	// tomoAlignmentViewController.setIsRotationMotorBusy(true);
	// while (rotationScannable.isBusy()) {
	// tomoAlignmentViewController.updateRotationDegree((Double) rotationScannable.getPosition());
	// }
	// } else {
	// tomoAlignmentViewController.setIsRotationMotorBusy(false);
	// }
	// }
	// } catch (Exception ex) {
	// logger.error("Exception while updating rotation scannable{}", ex);
	// }
	// }

	/**
	 * @param tomoAlignmentViewController
	 *            The tomoAlignmentViewController to set.
	 */
	@Override
	public void setTomoAlignmentViewController(TomoAlignmentController tomoAlignmentViewController) {
		this.tomoAlignmentViewController = tomoAlignmentViewController;
	}

	@Override
	public double getSampleScannableSpeed() throws DeviceException {
		return sampleHolderScannable.getSpeed();
	}

	@Override
	public double getDistanceToMoveSampleOut() {
		return TomoClientActivator.getDefault().getPreferenceStore()
				.getDouble(TomoAlignmentPreferencePage.TOMO_CLIENT_FLAT_DIST_MOVE_PREF);
	}

	/**
	 * @return Returns the defaultDistanceToMoveForFlat.
	 */
	public Double getDefaultDistanceToMoveForFlat() {
		return defaultDistanceToMoveForFlat;
	}

	/**
	 * @param defaultDistanceToMoveForFlat
	 *            The defaultDistanceToMoveForFlat to set.
	 */
	public void setDefaultDistanceToMoveForFlat(Double defaultDistanceToMoveForFlat) {
		this.defaultDistanceToMoveForFlat = defaultDistanceToMoveForFlat;
		TomoClientActivator.getDefault().getPreferenceStore()
				.setDefault(TomoAlignmentPreferencePage.TOMO_CLIENT_FLAT_DIST_MOVE_PREF, defaultDistanceToMoveForFlat);
	}

	/**
	 * @return Returns the thethaOffset.
	 */
	@Override
	public double getThethaOffset() {
		return thethaOffset;
	}

	/**
	 * @param thethaOffset
	 *            The thethaOffset to set.
	 */
	public void setThethaOffset(double thethaOffset) {
		this.thethaOffset = thethaOffset;
	}

	


	@Override
	public double getSs1RxPosition() throws DeviceException {
		return (Double) ss1RxScannable.getPosition();
	}

	public void setSs1RxScannable(IScannableMotor ss1RxScannable) {
		this.ss1RxScannable = ss1RxScannable;
	}

	@Override
	public void aysncMoveSs1Rx(Double ss1RxMoveToPosition) throws DeviceException {
		ss1RxScannable.asynchronousMoveTo(ss1RxMoveToPosition);
	}

	@Override
	public double getSs1RzPosition() throws DeviceException {
		return (Double) ss1RzScannable.getPosition();
	}

	public void setSs1RzScannable(IScannableMotor ss1RzScannable) {
		this.ss1RzScannable = ss1RzScannable;
	}

	@Override
	public void aysncMoveSs1Rz(Double ss1RzMoveToPosition) throws DeviceException {
		ss1RzScannable.asynchronousMoveTo(ss1RzMoveToPosition);
	}


	

	@Override
	public void moveSampleScannable(IProgressMonitor monitor, final double moveToLocation) throws DeviceException,
			InterruptedException {
		moveMotor(monitor, sampleHolderScannable, moveToLocation);
	}

	@Override
	public void moveSampleScannableBy(IProgressMonitor monitor, final double moveByLocation) throws DeviceException,
			InterruptedException {
		Double position = (Double) sampleHolderScannable.getPosition();
		moveSampleScannable(monitor, position + moveByLocation);
	}

	@Override
	public void moveRotationMotorBy(IProgressMonitor monitor, double deg) throws DeviceException, InterruptedException {
		Double position = (Double) rotationScannable.getPosition();
		moveRotationMotorTo(monitor, position + deg);

	}

	@Override
	public void moveRotationMotorTo(IProgressMonitor monitor, double deg) throws DeviceException, InterruptedException {
		// Dont call move motor for rotation motor because there is a GUI element on this. The IObserver will pick up
		// the status and keep it on wait.
		//XXX - Check
//		motorsRunning.add(rotationScannable);
		rotationScannable.asynchronousMoveTo(deg);
		while (rotationScannable.isBusy()) {
			tomoAlignmentViewController.setIsRotationMotorBusy(true);
			tomoAlignmentViewController.updateRotationDegree((Double) rotationScannable.getPosition());
			Thread.sleep(10);
		}
		tomoAlignmentViewController.setIsRotationMotorBusy(false);
		//motorsRunning.remove(rotationScannable);
	}

	@Override
	public void moveSs1Tx(IProgressMonitor monitor, Double ss1TxPosition) throws DeviceException, InterruptedException {
		moveMotor(monitor, ss1TxScannable, ss1TxPosition);
	}

	@Override
	public void moveSs1TxBy(IProgressMonitor monitor, Double offset) throws DeviceException, InterruptedException {
		Double ss1TxPos = (Double) ss1TxScannable.getPosition();

		moveSs1Tx(monitor, offset + ss1TxPos);
	}

	@Override
	public Double getSs1TxPosition() throws DeviceException {
		return (Double) ss1TxScannable.getPosition();
	}

	@Override
	public Double getSs1TzPosition() throws DeviceException {
		return (Double) ss1TzScannable.getPosition();
	}

	@Override
	public void moveSs1Tz(IProgressMonitor monitor, Double ss1TzPosition) throws DeviceException, InterruptedException {
		moveMotor(monitor, ss1TzScannable, ss1TzPosition);
	}

	@Override
	public void moveSs1TzBy(IProgressMonitor monitor, Double ss1TzPosition) throws DeviceException,
			InterruptedException {
		Double ss1TzPos = (Double) ss1TzScannable.getPosition();

		moveSs1Tz(monitor, ss1TzPosition + ss1TzPos);
	}

	@Override
	public void moveSs1Rx(IProgressMonitor monitor, Double ss1RxPosition) throws DeviceException, InterruptedException {
		moveMotor(monitor, ss1RxScannable, ss1RxPosition);
	}

	@Override
	public void moveSs1RxBy(IProgressMonitor monitor, Double ss1RxPosition) throws DeviceException,
			InterruptedException {
		Double ss1RxPos = (Double) ss1RxScannable.getPosition();

		moveMotor(monitor, ss1RxScannable, ss1RxPos + ss1RxPosition);
	}

	@Override
	public void moveSs1Rz(IProgressMonitor monitor, Double ss1RzPosition) throws DeviceException, InterruptedException {
		moveMotor(monitor, ss1RzScannable, ss1RzPosition);
	}

	@Override
	public void moveSs1RzBy(IProgressMonitor monitor, Double ss1RzPosition) throws DeviceException,
			InterruptedException {
		Double ss1RzPos = (Double) ss1RzScannable.getPosition();

		moveMotor(monitor, ss1RzScannable, ss1RzPos + ss1RzPosition);
	}

	@Override
	public void moveSs1Y2To(IProgressMonitor monitor, double position) throws DeviceException, InterruptedException {
		moveMotor(monitor, ss1Y2Scannable, position);
	}

	@Override
	public String getThethaMotorName() {
		return rotationScannable.getName();
	}

	



	@Override
	public double getSs1RzTolerance() {
		return ss1RzScannable.getDemandPositionTolerance();
	}

	

	public void setDefaultSampleInPosition(Double defaultSampleInPosition) {
		this.defaultSampleInPosition = defaultSampleInPosition;
	}

	@Override
	public Double getDefaultSampleInPosition() {
		return defaultSampleInPosition;
	}

	@Override
	public String getTiltXMotorName() {
		return ss1RxScannable.getName();
	}

	@Override
	public String getTiltZMotorName() {
		return ss1RzScannable.getName();
	}

	@Override
	public String getCentreXMotorName() {
		return ss1TxScannable.getName();
	}

	@Override
	public String getCentreZMotorName() {
		return ss1TzScannable.getName();
	}

	@Override
	public String getSampleBaseMotorName() {
		return sampleHolderScannable.getName();
	}

	@Override
	public String getVerticalMotorName() {
		return ss1Y2Scannable.getName();
	}

	@Override
	public boolean isSs1RxBusy() throws DeviceException {
		return ss1RxScannable.isBusy();
	}

	@Override
	public boolean isSs1RzBusy() throws DeviceException {
		return ss1RzScannable.isBusy();
	}

	@Override
	public String getSs1RzMotorName() {
		return ss1RzScannable.getName();
	}

	@Override
	public String getSs1RxMotorName() {
		return ss1RxScannable.getName();
	}
	
	@Override
	public void stopMotors() throws DeviceException {
		stopAllMotors();
	}
}