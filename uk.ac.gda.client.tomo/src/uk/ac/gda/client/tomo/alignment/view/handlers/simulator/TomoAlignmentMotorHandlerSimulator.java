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

package uk.ac.gda.client.tomo.alignment.view.handlers.simulator;

import gda.device.DeviceException;

import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.gda.client.tomo.alignment.view.controller.TomoAlignmentViewController;
import uk.ac.gda.client.tomo.alignment.view.handlers.IMotorHandler;

/**
 *
 */
public class TomoAlignmentMotorHandlerSimulator implements IMotorHandler {

	@Override
	public void openShutter(IProgressMonitor progress) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public double getSamplePosition() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void moveSampleScannable(IProgressMonitor monitor, double d) throws DeviceException, InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Double getRotationMotorDeg() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTomoAlignmentViewController(TomoAlignmentViewController tomoAlignmentViewController) {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveRotationMotorBy(IProgressMonitor monitor, double deg) throws DeviceException, InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveRotationMotorTo(IProgressMonitor monitor, double deg) throws DeviceException, InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeShutter(IProgressMonitor monitor) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public double getSampleScannableSpeed() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getDistanceToMoveSampleOut() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void moveT3XTo(IProgressMonitor monitor, Double t3xMoveToPosition) throws DeviceException,
			InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveT3M1YTo(IProgressMonitor monitor, Double t3m1yMoveToPosition) throws DeviceException,
			InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public double getCam1XPosition() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCam1ZPosition() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCam1RollPosition() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getSs1RzPosition() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getSs1RxPosition() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Double getT3XPosition() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getT3M1YPosition() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getCam1XTolerance() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCam1ZTolerance() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getCam1RollTolerance() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getT3XTolerance() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getT3M1YTolerance() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void aysncMoveSs1Rx(Double ss1RxMoveToPosition) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void aysncMoveSs1Rz(Double ss1RzMoveToPosition) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveCam1Z(IProgressMonitor monitor, Double cam1zPos) throws DeviceException, InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveCam1X(IProgressMonitor monitor, double cam1xPos) throws DeviceException, InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveCam1Roll(IProgressMonitor monitor, double cam1RollPos) throws DeviceException, InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveSs1Tz(IProgressMonitor monitor, Double ss1TzPosition) throws DeviceException, InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveSs1Tx(IProgressMonitor monitor, Double ss1TxPosition) throws DeviceException, InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public double getThethaOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getThethaMotorName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void moveSs1Rz(IProgressMonitor monitor, Double ss1RzPosition) throws DeviceException, InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveSs1RzBy(IProgressMonitor monitor, Double ss1RzPosition) throws DeviceException,
			InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveSs1Rx(IProgressMonitor monitor, Double ss1RxPosition) throws DeviceException, InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveSs1RxBy(IProgressMonitor monitor, Double ss1RxPosition) throws DeviceException,
			InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public double getSs1Y2Position() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void moveSs1Y2To(IProgressMonitor monitor, double position) throws DeviceException, InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopMotors() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveSs1TxBy(IProgressMonitor monitor, Double ss1TxPosition) throws DeviceException,
			InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveSs1TzBy(IProgressMonitor monitor, Double ss1TzPosition) throws DeviceException,
			InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public void moveSampleScannableBy(IProgressMonitor monitor, double distanceMoveBy) throws DeviceException,
			InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public double getSs1RzTolerance() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Double getT3M1ZPosition() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getT3xOffset() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getT3m1zOffset() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getT3m1yOffset() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void moveT3M1ZTo(IProgressMonitor monitor, double t3m1zValue) throws DeviceException, InterruptedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double getDefaultSampleInPosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSs1RzBusy() throws DeviceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getSs1RzName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSs1RxBusy() throws DeviceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getSs1RxName() {
		// TODO Auto-generated method stub
		return null;
	}

}
