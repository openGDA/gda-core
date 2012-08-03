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

package uk.ac.gda.client.tomo.alignment.view.handlers;

import gda.device.DeviceException;

import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.gda.client.tomo.alignment.view.controller.TomoAlignmentViewController;

/**
 * Interface that is called by the controller - this handles all the stage motors and most of the movements and position
 * getters from the motors in beamline hutch.
 * 
 * @author rsr31645 - Ravi Somayaji
 */
public interface IMotorHandler extends ITomoHandler {

	/**
	 * Delegates to open the experimental shutter -
	 * 
	 * @param progress
	 * @throws DeviceException
	 */
	void openShutter(IProgressMonitor progress) throws DeviceException;

	/**
	 * Delegates to close the experimental shutter.
	 * 
	 * @param monitor
	 * @throws DeviceException
	 */
	void closeShutter(IProgressMonitor monitor) throws DeviceException;

	/**
	 * @return the position of the sample stage base motor.
	 * @throws DeviceException
	 */
	double getSampleBaseMotorPosition() throws DeviceException;

	/**
	 * Request to move the sample stage to the given position.
	 * 
	 * @param monitor
	 * @param pos
	 *            - the position to which the sample stage should be moved to.
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveSampleScannable(IProgressMonitor monitor, double pos) throws DeviceException, InterruptedException;

	/**
	 * Request to move the sample stage by the given position.
	 * 
	 * @param monitor
	 * @param distanceMoveBy
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveSampleScannableBy(IProgressMonitor monitor, double distanceMoveBy) throws DeviceException,
			InterruptedException;

	/**
	 * @return the degree(position) of the thetha stage - [on I12 - ss1.thetha - on the huber sample stage]
	 * @throws DeviceException
	 */
	Double getRotationMotorDeg() throws DeviceException;

	/**
	 * The {@link TomoAlignmentViewController} controls the GUI with the backend.
	 * 
	 * @param tomoAlignmentViewController
	 */
	void setTomoAlignmentViewController(TomoAlignmentViewController tomoAlignmentViewController);

	/**
	 * @return thetha value - considered as offset when calculating the horizontal distance to move to. [on I12 -
	 *         ss1.thetha - on the huber sample stage]
	 */
	double getThethaOffset();

	/**
	 * @return the motor name of the rotation stage - [on I12 - ss1.thetha - on the huber sample stage]
	 */
	String getThethaMotorName();

	/**
	 * Moves the rotation stage by the given degrees.- [on I12 - ss1.thetha - on the huber sample stage]
	 * 
	 * @param monitor
	 * @param deg
	 *            - degree by which the stage needs to be moved from the current position.
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveRotationMotorBy(IProgressMonitor monitor, double deg) throws DeviceException, InterruptedException;

	/**
	 * Moves the rotation stage to the position value provided.- [on I12 - ss1.thetha - on the huber sample stage]
	 * 
	 * @param monitor
	 * @param deg
	 *            - value to which the motor needs to be moved to
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveRotationMotorTo(IProgressMonitor monitor, double deg) throws DeviceException, InterruptedException;

	/**
	 * @return the speed configured on the sample scannable [on I12 - ssbase.x - sample stages]
	 * @throws DeviceException
	 */
	double getSampleScannableSpeed() throws DeviceException;

	/**
	 * @return the distance configured in the xml file which mentions the distance to move the sample stage away from
	 *         the beam so that a flat image can be captured
	 */
	double getDistanceToMoveSampleOut();

	/**
	 * Delegate method to move the T3.X motor to the given position [on i12 - t3.x - Main X on the large detector table]
	 * 
	 * @param monitor
	 * @param t3xMoveToPosition
	 *            - the position to which this motor should be moved to.
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveT3XTo(IProgressMonitor monitor, Double t3xMoveToPosition) throws DeviceException, InterruptedException;

	/**
	 * Delegate method to move the T3.M1Y motor to the given position [on i12 - t3.m1y - Mod1 Y on the large detector
	 * table]
	 * 
	 * @param monitor
	 * @param t3m1yMoveToPosition
	 *            - the position to which this motor should be moved to.
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveT3M1YTo(IProgressMonitor monitor, Double t3m1yMoveToPosition) throws DeviceException, InterruptedException;

	/**
	 * Delegate method to move the T3.MZY motor to the given position [on i12 - t3.mZy - Mod1 Z on the large detector
	 * table]
	 * 
	 * @param monitor
	 * @param t3m1zValue
	 *            - the position to which this motor should be moved to.
	 * @throws DeviceException
	 * @throws InterruptedException
	 */

	void moveT3M1ZTo(IProgressMonitor monitor, double t3m1zValue) throws DeviceException, InterruptedException;

	/**
	 * @return the position of the Lens motor [on i12 cam1.x on the Monochromatic camera]
	 * @throws DeviceException
	 */
	double getCam1XPosition() throws DeviceException;

	/**
	 * Move the Lens motor [on i12 cam1.x on the Monochromatic camera] to the given position
	 * 
	 * @param monitor
	 * @param cam1xPos
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveCam1X(IProgressMonitor monitor, double cam1xPos) throws DeviceException, InterruptedException;

	/**
	 * @return the position of the Optic focus motor [on i12 cam1.z on the Monochromatic camera]
	 * @throws DeviceException
	 */
	double getCam1ZPosition() throws DeviceException;

	/**
	 * @param monitor
	 * @param cam1ZPos
	 *            - position to which the motor should be moved to.
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveCam1Z(IProgressMonitor monitor, Double cam1ZPos) throws DeviceException, InterruptedException;

	/**
	 * @return the position of the Optic roll motor [on i12 cam1.roll on the Monochromatic camera]
	 * @throws DeviceException
	 */
	double getCam1RollPosition() throws DeviceException;

	/**
	 * Move the Optic roll motor [on i12 cam1.roll on the Monochromatic camera] to the given position.
	 * 
	 * @param monitor
	 * @param cam1RollPos
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveCam1Roll(IProgressMonitor monitor, double cam1RollPos) throws DeviceException, InterruptedException;

	/**
	 * @return the position of the 3B Z tilt motor [on i12 ss1.rz on the Huber sample stage]
	 * @throws DeviceException
	 */
	double getSs1RzPosition() throws DeviceException;

	/**
	 * Moves the 3B Z tilt motor[on i12 ss1.rz on the Huber sample stage] to the given position
	 * 
	 * @param monitor
	 * @param ss1RzPosition
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveSs1Rz(IProgressMonitor monitor, Double ss1RzPosition) throws DeviceException, InterruptedException;

	/**
	 * Moves the 3B Z tilt motor[on i12 ss1.rz on the Huber sample stage] by the given position.
	 * 
	 * @param monitor
	 * @param ss1RzPosition
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveSs1RzBy(IProgressMonitor monitor, Double ss1RzPosition) throws DeviceException, InterruptedException;

	/**
	 * @return the position of the 3A X tilt motor [on i12 ss1.rx on the Huber sample stage]
	 * @throws DeviceException
	 */
	double getSs1RxPosition() throws DeviceException;

	/**
	 * Moves the 3A X tilt motor[on i12 ss1.rx on the Huber sample stage] to the given position
	 * 
	 * @param monitor
	 * @param ss1RxPosition
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveSs1Rx(IProgressMonitor monitor, Double ss1RxPosition) throws DeviceException, InterruptedException;

	/**
	 * Moves the 3A X tilt motor asynchronously to the given position [on i12 ss1.rx on the Huber sample stage]
	 * 
	 * @param ss1RxMoveToPosition
	 * @throws DeviceException
	 */
	void aysncMoveSs1Rx(Double ss1RxMoveToPosition) throws DeviceException;

	/**
	 * Moves the 3A X tilt motor[on i12 ss1.rx on the Huber sample stage] by the given position
	 * 
	 * @param monitor
	 * @param ss1RxPosition
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveSs1RxBy(IProgressMonitor monitor, Double ss1RxPosition) throws DeviceException, InterruptedException;

	/**
	 * @return the motor position of the T3.X motor [on i12 - t3.x - Main X on the large detector table]
	 * @throws DeviceException
	 */
	Double getT3XPosition() throws DeviceException;

	/**
	 * @return the motor position of the T3.m1z motor [on i12 - t3.m1z - Main Z on the large detector table]
	 * @throws DeviceException
	 */
	Double getT3M1ZPosition() throws DeviceException;

	/**
	 * @return the motor position of the T3.M1Y motor [on i12 - t3.m1y - Mod1 Y on the large detector table]
	 * @throws DeviceException
	 */
	Double getT3M1YPosition() throws DeviceException;

	/**
	 * @return the dead band retry value of the Lens motor [on i12 cam1.x on the Monochromatic camera]
	 */
	double getCam1XTolerance();

	/**
	 * @return the dead band retry value of the Optic focus motor [on i12 cam1.z on the Monochromatic camera]
	 */
	double getCam1ZTolerance();

	/**
	 * Move the Sample Z motor [on i12 ss1t.z on the Huber sample stage] to the given position
	 * 
	 * @param monitor
	 * @param ss1TzPosition
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveSs1Tz(IProgressMonitor monitor, Double ss1TzPosition) throws DeviceException, InterruptedException;

	/**
	 * Move the Sample Z motor [on i12 ss1t.z on the Huber sample stage] by the given position
	 * 
	 * @param monitor
	 * @param ss1TzPosition
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveSs1TzBy(IProgressMonitor monitor, Double ss1TzPosition) throws DeviceException, InterruptedException;

	/**
	 * @return the dead band retry value of the Optic roll motor [on i12 cam1.roll on the Monochromatic camera]
	 */
	double getCam1RollTolerance();

	/**
	 * @return the dead band retry value of the T3.X motor [on i12 - t3.x - Main X on the large detector table]
	 */
	double getT3XTolerance();

	/**
	 * @return the dead band retry value of the T3.M1Y motor [on i12 - t3.m1y - Mod1 Y on the large detector table]
	 */
	double getT3M1YTolerance();

	/**
	 * Moves the 3B Z tilt motor asynchronously to the given position [on i12 ss1.rz on the Huber sample stage]
	 * 
	 * @param ss1RzMoveToPosition
	 * @throws DeviceException
	 */
	void aysncMoveSs1Rz(Double ss1RzMoveToPosition) throws DeviceException;

	/**
	 * Move the Sample X motor to the given position [on i12 ss1t.x on the Huber sample stage]
	 * 
	 * @param monitor
	 * @param ss1TxPosition
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveSs1Tx(IProgressMonitor monitor, Double ss1TxPosition) throws DeviceException, InterruptedException;

	/**
	 * Move the Sample X motor by the given position [on i12 ss1t.x on the Huber sample stage]
	 * 
	 * @param monitor
	 * @param ss1TxPosition
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveSs1TxBy(IProgressMonitor monitor, Double ss1TxPosition) throws DeviceException, InterruptedException;

	/**
	 * @return the motor position of Y2B motor [on i12 ss1.y2 on the Huber sample stage]
	 * @throws DeviceException
	 */
	double getVerticalPosition() throws DeviceException;

	/**
	 * Move the Y2B motor by the given position [on i12 ss1.y2 on the Huber sample stage]
	 * 
	 * @param monitor
	 * @param position
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	void moveSs1Y2To(IProgressMonitor monitor, double position) throws DeviceException, InterruptedException;

	/**
	 * Method to stop all motor motions.
	 * 
	 * @throws DeviceException
	 */
	void stopMotors() throws DeviceException;

	/**
	 * @return the dead band retry value of the sample stage roll [sample stage roll]
	 */
	double getSs1RzTolerance();

	/**
	 * @return the user offset for t3.x motor
	 * @throws DeviceException
	 */
	double getT3xOffset() throws DeviceException;

	/**
	 * @return the user offset for t3.m1z motor
	 * @throws DeviceException
	 */
	double getT3m1zOffset() throws DeviceException;

	/**
	 * @return the user offset for t3.m1y motor
	 * @throws DeviceException
	 */
	double getT3m1yOffset() throws DeviceException;

	/**
	 * @return the default configured for the Sample In position.
	 */
	Double getDefaultSampleInPosition();

	/**
	 * @return true if 3B Z tilt motor [on i12 ss1.rz on the Huber sample stage] is busy
	 * @throws DeviceException
	 */
	boolean isSs1RzBusy() throws DeviceException;

	/**
	 * @return name of 3B Z tilt motor [on i12 ss1.rz on the Huber sample stage]
	 */

	String getTiltZMotorName();

	/**
	 * @return true if 3A X tilt motor [on i12 ss1.rx on the Huber sample stage] is busy
	 * @throws DeviceException
	 */
	boolean isSs1RxBusy() throws DeviceException;

	/**
	 * @return name of 3A X tilt motor [on i12 ss1.rx on the Huber sample stage]
	 */

	String getTiltXMotorName();

	/**
	 * @return position of the Sample X motor [on i12 ss1t.x on the Huber sample stage]
	 * @throws DeviceException
	 */
	Double getSs1TxPosition() throws DeviceException;

	/**
	 * @return the postion of Sample Z motor [on i12 ss1t.z on the Huber sample stage]
	 * @throws DeviceException
	 */
	Double getSs1TzPosition() throws DeviceException;

	/**
	 * @return name of the motor responsible for centreX - (on i12 it is ss1_tx)
	 */
	String getCentreXMotorName();

	/**
	 * @return name of the motor responsible for centreZ - (on i12 it is ss1_tz)
	 */
	String getCentreZMotorName();

	/**
	 * @return name of the motor responsible for sample base - (on i12 it is ss1_x)
	 */
	String getSampleBaseMotorName();

	/**
	 * @return name of the motor responsible for vertical motor - (on i12 it is ss1_y2)
	 */
	String getVerticalMotorName();

	/**
	 * @return name of the motor responsible for camera stage z axisF - (on i12 it is t3_m1z)
	 */
	String getCameraStageZMotorName();

}
