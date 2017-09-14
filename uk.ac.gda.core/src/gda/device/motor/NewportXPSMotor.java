/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.motor;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Motor;
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.factory.Configurable;
import gda.factory.Finder;
import gda.observable.IObservable;

/**
 * NewportXPSMotor Class
 */
public class NewportXPSMotor extends MotorBase implements Configurable, IObservable, Motor {

	private static final Logger logger = LoggerFactory.getLogger(NewportXPSMotor.class);

	String xpsGroupName;

	String xpsPositionerName;

	private String newportXpsControllerName;

	private double velocity;

	private double acceleration;

	private double minJerkTime;

	private double maxJerkTime;

	private double pcoMinimumPosition;

	private double pcoMaximumPosition;

	private double pcoPositionStep;

	private boolean pcoEnableState;

	private NewportXPSController newportXpsController;

	NewportXPSController newportXpsController1;

	NewportXPSController newportXpsController2;

	private boolean safeToAutoHome = false;

	/**
	 *
	 */
	public boolean controllerAvailable = false;

	/**
	 * Constructor.
	 */
	public NewportXPSMotor() {
	}

	@Override
	public void configure() {
		logger.debug("CONFIGURING XPS MOTOR " + getName());
		// Find and connect to the controller that is used to store the
		// configuration only.
		logger.debug("Finding: " + newportXpsControllerName);
		if ((newportXpsController = (NewportXPSController) Finder.getInstance().find(newportXpsControllerName)) == null) {
			logger.error("newportController " + newportXpsControllerName + " not found");
		} else {
			logger.debug("Connecting to Newport Controller");
			// newportXpsController.xpsconnect();

			// Use a separate (i.e. private) controller to send commands.
			newportXpsController1 = new NewportXPSController();
			newportXpsController1.setHost(newportXpsController.getHost());
			newportXpsController1.setPort(newportXpsController.getPort());
			newportXpsController1.setReadtimeout(newportXpsController.getReadtimeout());
			// newportXpsController1.xpsconnect();
			// use a second controller connected on a separate socket to
			// access
			// position and status information whilst a move is active.
			newportXpsController2 = new NewportXPSController();
			newportXpsController2.setHost(newportXpsController.getHost());
			newportXpsController2.setPort(newportXpsController.getPort());
			newportXpsController2.setReadtimeout(newportXpsController.getReadtimeout());
			// newportXpsController2.xpsconnect();
			controllerAvailable = true;
			try {
				initializeMotor();
				if (safeToAutoHome) {
					home();
				}
				getSpeed();
			} catch (MotorException me) {
				logger.error("NewportXPSMotor: " + me);
			}
		}
		isInitialised = true;
	}

	/**
	 * Sets the name of the Newport XPS Controller.
	 *
	 * @param newportControllerName
	 */
	public void setNewportXpsControllerName(String newportControllerName) {
		this.newportXpsControllerName = newportControllerName;
	}

	/**
	 * Gets the name of the Newport XPS Controller.
	 *
	 * @return Returns the NewportXpsControllerName
	 */
	public String getNewportXpsControllerName() {
		return newportXpsControllerName;
	}

	/**
	 * @return xps group name
	 */
	public String getXpsGroupName() {
		return xpsGroupName;
	}

	/**
	 * Sets the group name of the motor as known in the Newport hardware
	 *
	 * @param xpsGroupName
	 */
	public void setXpsGroupName(String xpsGroupName) {
		this.xpsGroupName = xpsGroupName;
	}

	/**
	 * @return xps positioner name
	 */
	public String getXpsPositionerName() {
		return xpsPositionerName;
	}

	/**
	 * Sets the positioner name of the motor as know in the Newport hardware
	 *
	 * @param xpsPositionerName
	 */
	public void setXpsPositionerName(String xpsPositionerName) {
		this.xpsPositionerName = xpsPositionerName;
	}

	/**
	 * Returns true if this object will home the motor during the configure method (ie at GDA startup).
	 *
	 * @return boolean
	 */
	public boolean isSafeToAutoHome() {
		return safeToAutoHome;
	}

	/**
	 * Sets the safeToAutoHome attribute. If true then the motor will be homed during the configure method.
	 *
	 * @param safeToAutoHome
	 */
	public void setSafeToAutoHome(boolean safeToAutoHome) {
		this.safeToAutoHome = safeToAutoHome;
	}

	/**
	 * Sends command to hardware to initialise the motor.
	 *
	 * @throws MotorException
	 */
	public void initializeMotor() throws MotorException {
		checkControllerAvailable();
		newportXpsController1.xpswrite("GroupInitialize(" + xpsGroupName + ")");
		int error = Integer.parseInt(newportXpsController1.getErrnum());

		if (error == 0) {
			logger.debug("NewportXPSMotor: initializeMotor() Command successful");
		} else if (error == -22) {
			logger.debug("NewportXPSMotor: Controller already initialised");
		} else {
			throw new MotorException(MotorStatus.FAULT, newportXpsController1.getErrorMessage(error));
		}
	}

	@Override
	public void moveBy(double steps) throws MotorException {
		checkControllerAvailable();
		newportXpsController1.xpswritenowait("GroupMoveRelative(" + xpsGroupName + ", " + steps + ")");

		int error = Integer.parseInt(newportXpsController1.getErrnum());
		if (error == 0) {
			logger.debug("NewportXPSMotor: moveBy() Command successful");
		} else {
			throw new MotorException(MotorStatus.FAULT, newportXpsController1.getErrorMessage(error));
		}
	}

	@Override
	public void moveTo(double steps) throws MotorException {

		checkControllerAvailable();
		newportXpsController1.xpswritenowaitWithHalt("GroupMoveAbsolute(" + xpsGroupName + ", " + steps + ")");

		// Note: at this point the error number has more than likely not yet
		// been
		// updated in response to the move request just made. This code is
		// repeated
		// (with a check to throw an exception if the move fails) in the run()
		// methodf of the PipeR (readback) thread started during the above
		// request---where
		// it is guaranteed to reflect the result of the requested move.

		int error = Integer.parseInt(newportXpsController1.getErrnum());
		logger.debug("NewportXPSMotor: moveTo() returned error no:" + error);
	}

	@Override
	public void moveContinuously(int direction) throws MotorException {
		// Not implemented
	}

	@Override
	public void setPosition(double steps) throws MotorException {
		// TODO Check programming Manual for Command
	}

	@Override
	public double getPosition() throws MotorException {
		checkControllerAvailable();
		newportXpsController2.xpswrite("GroupPositionCurrentGet(" + xpsGroupName + ", double *)");

		int error = Integer.parseInt(newportXpsController2.getErrnum());
		if (error == 0) {
			logger.debug("Command successful");
		} else {
			throw new MotorException(MotorStatus.FAULT, newportXpsController2.getErrorMessage(error));
		}
		double pos = Double.parseDouble(newportXpsController2.getRetValue()[0]);
		return pos;

	}

	@Override
	public void setSpeed(double speed) throws MotorException {
		checkControllerAvailable();
		velocity = speed;
		newportXpsController1.xpswrite("PositionerSGammaParametersSet(" + xpsGroupName + "." + xpsPositionerName + ","
				+ velocity + "," + acceleration + "," + minJerkTime + "," + maxJerkTime + ")");
		int error = Integer.parseInt(newportXpsController1.getErrnum());
		if (error == 0) {
			logger.debug("Command successful");
		} else {
			throw new MotorException(MotorStatus.FAULT, newportXpsController1.getErrorMessage(error));
		}
	}

	@Override
	public double getSpeed() throws MotorException {
		checkControllerAvailable();
		newportXpsController2.xpswrite("PositionerSGammaParametersGet(" + xpsGroupName + "." + xpsPositionerName
				+ ", double *, double *, double *, double *)");

		int error = Integer.parseInt(newportXpsController2.getErrnum());
		if (error == 0) {
			logger.debug("NewportXPSMotor: getSpeed() Command successful");
		} else {
			throw new MotorException(MotorStatus.FAULT, newportXpsController2.getErrorMessage(error));
		}

		String ret[] = newportXpsController2.getRetValue();
		velocity = Double.parseDouble(ret[0]);
		acceleration = Double.parseDouble(ret[1]);
		minJerkTime = Double.parseDouble(ret[2]);
		maxJerkTime = Double.parseDouble(ret[3]);
		return velocity;
	}

	@Override
	public void stop() throws MotorException {
		checkControllerAvailable();

		// Get status code back from XPS
		checkControllerAvailable();
		newportXpsController2.xpswrite("GroupStatusGet(" + xpsGroupName + ",int  *)");
		int error1 = Integer.parseInt(newportXpsController2.getErrnum());
		if (error1 == 0) {
			logger.debug("NewportXPSMotor: getStatus() Command successful");

		} else {
			throw new MotorException(MotorStatus.FAULT, newportXpsController2.getErrorMessage(error1));
		}
		int status = Integer.parseInt(newportXpsController2.getRetValue()[0]);

		logger.debug("Status = " + status);

		newportXpsController2.xpswrite("GroupMoveAbort(" + xpsGroupName + ")");
		int error = Integer.parseInt(newportXpsController2.getErrnum());
		if (error == 0) {
			logger.debug("Command successful");
		} else {
			// Just print error instead of throwing an exception, as it can
			// causu nasty infinite loops: If there there is a problem
			// moving a
			// motor (a following error for example), then this stop method
			// is
			// called. The same problem may cause this stop command to
			// return a
			// fault, causing a new stop command to be sent and so on...

			// << There is currently a bug in that the first time a move
			// fails, no
			// no error is thrown or reported. This chnage did not casue
			// that bug.
			// >>

			// throw new MotorException(MotorStatus.FAULT, (removed)
			// newportXpsController1.getErrorMessage(error)); (removed)

			logger.debug("Error occured while stopping XPS motor (exception not re-thrown):"
					+ newportXpsController1.getErrorMessage(error));
		}
	}

	// this method puts the motor in the non initialized state.
	// The motor then needs to be instialized and homed to get to ready
	// state
	@Override
	public void panicStop() throws MotorException {
		checkControllerAvailable();
		newportXpsController2.xpswrite("GroupKill(" + xpsGroupName + ")");
		int error = Integer.parseInt(newportXpsController2.getErrnum());
		if (error == 0) {
			logger.debug("NewportXPSMotor: panicStop() Command successful");
		} else {
			throw new MotorException(MotorStatus.FAULT, newportXpsController1.getErrorMessage(error));
		}
	}

	/**
	 * Checks if the motor is homed or not. Motor implementations should overide this method if required.
	 *
	 * @return if the motor is homed. Returns false!
	 */
	@Override
	public boolean isHomed() {
		newportXpsController2.xpswrite("GroupStatusGet(" + xpsGroupName + ",int  *)");

		int error = Integer.parseInt(newportXpsController2.getErrnum());
		if (error == 0) {
			logger.debug("NewportXPSMotor: getStatus() Command successful");

		} else {
			logger.info(newportXpsController2.getErrorMessage(error));
		}
		int status = Integer.parseInt(newportXpsController2.getRetValue()[0]);
		if ((status >= 0 && status <= 9) || status == 50 || status == 63)
			return false;
		else if ((status >= 10 && status <= 18) || (status >= 20 && status <= 36) || (status >= 40 && status <= 49)
				|| status == 51 || status == 64)
			return true;
		else
			logger.error("Error finding the Home status of Motor " + getName());
		return false;

	}

	@Override
	public MotorStatus getStatus() throws MotorException {
		checkControllerAvailable();
		newportXpsController2.xpswrite("GroupStatusGet(" + xpsGroupName + ",int  *)");

		int error = Integer.parseInt(newportXpsController2.getErrnum());
		if (error == 0) {
			logger.debug("NewportXPSMotor: getStatus() Command successful");

		} else {
			throw new MotorException(MotorStatus.FAULT, newportXpsController2.getErrorMessage(error));
		}
		int status = Integer.parseInt(newportXpsController2.getRetValue()[0]);
		switch (status) {
		case (0):
		case (1):
		case (2):
		case (3):
		case (4):
		case (5):
		case (6):
		case (7):
		case (8):
		case (9):
		case (42):
			return MotorStatus.UNKNOWN; // Not referenced state
		case (10):
		case (11):
		case (12):
		case (13):
		case (14):
		case (15):
		case (16):
		case (17):
		case (18):
		case (46): // Slave mode enabled
		case (48): // tracking enabled
		case (49): // encoder calibrating state
			return MotorStatus.READY;
		case (20):
			return MotorStatus.FAULT; // Disabled State
		case (21):
			return MotorStatus.FAULT; // Disabled state due to a following
			// error on ready state
		case (22):
			return MotorStatus.FAULT; // Disabled state due to a following
			// error during motion
		case (23):
		case (24):
		case (25):
		case (26):
		case (27):
		case (28):
		case (29):
		case (30):
		case (31):
		case (32):
		case (33):
		case (34):
		case (35):
		case (36):
		case (63):
			return MotorStatus.FAULT; // Not initialized state due to a motor
			// initialization error
		case (43):
		case (44):
		case (47):
		case (50): // The motor will be in the spinning mode
			return MotorStatus.BUSY; // homing, Moving or Jogging state
		default:
			return MotorStatus.UNKNOWN;
		}
	}

	@Override
	public boolean isMoving() throws MotorException {
		return (getStatus() == MotorStatus.BUSY);
	}

	/**
	 * Moves the motor to a repeatable starting location allows homing only once after a power cycle.
	 *
	 * @throws MotorException
	 */
	@Override
	public void home() throws MotorException {
		checkControllerAvailable();
		newportXpsController1.xpswrite("GroupHomeSearch(" + xpsGroupName + ")");

		int error = Integer.parseInt(newportXpsController1.getErrnum());
		if (error == 0) {
			logger.debug("Command successful");
		} else if (error == -22) {
			logger.debug("NewportXPSMotor: is already homed");
		} else {
			throw new MotorException(MotorStatus.FAULT, newportXpsController1.getErrorMessage(error));
		}
	}

	/**
	 * sets the soft limits of the motor itself (i.e. NOT limits in our software)
	 *
	 * @param minPosition
	 *            minimum software limit
	 * @param maxPosition
	 *            maximum software limit
	 * @throws MotorException
	 */
	@Override
	public void setSoftLimits(double minPosition, double maxPosition) throws MotorException {
		checkControllerAvailable();
		newportXpsController1.xpswrite("PositionerUserTravelLimitsSet(" + xpsGroupName + "." + xpsPositionerName + ","
				+ minPosition + "," + maxPosition + ")");

		int error = Integer.parseInt(newportXpsController1.getErrnum());
		if (error == 0) {
			logger.debug("Command successful");
		} else {
			throw new MotorException(MotorStatus.FAULT, newportXpsController1.getErrorMessage(error));
		}
	}

	/**
	 * Turns on data gathering by the XPS for the CurrentPosition.
	 *
	 * @throws MotorException
	 */
	public void gatherCurrentPosition() throws MotorException {
		checkControllerAvailable();
		newportXpsController1.xpswrite("GatheringConfigurationSet(" + xpsGroupName + "." + xpsPositionerName + "."
				+ "CurrentPosition)");
	}

	/**
	 * Turns on data gathering by the XPS for the CurrentVelocity.
	 *
	 * @throws MotorException
	 */
	public void gatherCurrentVelocity() throws MotorException {
		checkControllerAvailable();
		newportXpsController1.xpswrite("GatheringConfigurationSet(" + xpsGroupName + "." + xpsPositionerName + "."
				+ "CurrentPosition)");
	}

	/**
	 * Turns on data gathering by the XPS for the CurrentAcceleration.
	 *
	 * @throws MotorException
	 */
	public void gatherCurrentAcceleration() throws MotorException {
		checkControllerAvailable();
		newportXpsController1.xpswrite("GatheringConfigurationSet(" + xpsGroupName + "." + xpsPositionerName + "."
				+ "CurrentAcceleration)");
	}

	/**
	 * Turns on data gathering by the XPS for the SetpointPosition.
	 *
	 * @throws MotorException
	 */
	public void gatherSetpointPosition() throws MotorException {
		checkControllerAvailable();
		newportXpsController1.xpswrite("GatheringConfigurationSet(" + xpsGroupName + "." + xpsPositionerName + "."
				+ "SetpointPosition)");
	}

	/**
	 * Turns on data gathering by the XPS for the SetpointVelocity.
	 *
	 * @throws MotorException
	 */
	public void gatherSetpointVelocity() throws MotorException {
		checkControllerAvailable();
		newportXpsController1.xpswrite("GatheringConfigurationSet(" + xpsGroupName + "." + xpsPositionerName + "."
				+ "SetpointVelocity)");
	}

	/**
	 * Turns on data gathering by the XPS for the SetpointAcceleration.
	 *
	 * @throws MotorException
	 */
	public void gatherSetpointAcceleration() throws MotorException {
		checkControllerAvailable();
		newportXpsController1.xpswrite("GatheringConfigurationSet(" + xpsGroupName + "." + xpsPositionerName + "."
				+ "SetpointAcceleration)");
	}

	/**
	 * Turns on data gathering by the XPS for the FollowingError.
	 *
	 * @throws MotorException
	 */
	public void gatherFollowingError() throws MotorException {
		checkControllerAvailable();
		newportXpsController1.xpswrite("GatheringConfigurationSet(" + xpsGroupName + "." + xpsPositionerName + "."
				+ "FollowingError)");
	}

	/**
	 * Adds an event to start internal triggered data gathering for the case of a MotionStart.
	 *
	 * @param numberOfPoints
	 * @param divisor
	 * @throws MotorException
	 */
	public void gatherAddMotionStartEvent(int numberOfPoints, int divisor) throws MotorException {
		checkControllerAvailable();
		newportXpsController1.xpswrite("EventAdd(" + xpsGroupName + "." + xpsPositionerName
				+ ",SGamma.MotionStart,0,GatheringRun," + numberOfPoints + "," + divisor + ",0)");
	}

	/**
	 * Adds an event to start internal triggered data gathering for the case of a MotionStart.
	 *
	 * @throws MotorException
	 */
	public void gatherAddMotionStartEvent() throws MotorException {
		gatherAddMotionStartEvent(1000000, 100);
	}

	/**
	 * Stops internal triggered data gathering and saves data to the XPS controller. Data is stored in the file
	 * GATHERING.DAT in the "..\PUBLIC" folder of the XPS controller.
	 *
	 * @throws MotorException
	 */
	public void gatheringStopAndSave() throws MotorException {
		checkControllerAvailable();
		newportXpsController1.xpswrite("GatheringStopAndSave()");
	}

	/**
	 * Transfers the internal data gathering data file from the XPS controller and save it as a file with the specified
	 * filename.
	 *
	 * @param filename
	 */
	public void getGatheringData(String filename) {

		try {
			URL url = new URL("ftp://Administrator:Administrator@" + newportXpsController.getHost()
					+ "/public/Gathering.dat");

			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

			FileWriter testFile = new FileWriter(filename);

			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				testFile.write(inputLine + "\r\n");
			}

			in.close();
			testFile.close();

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Transfers the internal data gathering data file from the XPS controller and saves it to the temp directory.
	 */
	public void getGatheringData() {

		String localDir = System.getProperty("java.io.tmpdir");
		String filename = "Gathering.dat";
		getGatheringData(localDir + "/" + filename);

	}

	/**
	 * Sets the values used (ie min, max and step) for the Position Compare output.
	 *
	 * @param minPosition
	 * @param maxPosition
	 * @param positionStep
	 * @throws MotorException
	 */
	public void setPositionCompare(double minPosition, double maxPosition, double positionStep) throws MotorException {
		checkControllerAvailable();
		pcoMinimumPosition = minPosition;
		pcoMaximumPosition = maxPosition;
		pcoPositionStep = positionStep;

		newportXpsController1.xpswrite("PositionerPositionCompareSet(" + xpsGroupName + "." + xpsPositionerName + ","
				+ pcoMinimumPosition + "," + pcoMaximumPosition + "," + pcoPositionStep + ")");
		int error = Integer.parseInt(newportXpsController1.getErrnum());
		if (error == 0) {
			logger.debug("Command successful");
		} else {
			throw new MotorException(MotorStatus.FAULT, newportXpsController1.getErrorMessage(error));
		}
	}

	/**
	 * Gets the parameters (ie min,max and step) used for Position Compare output.
	 *
	 * @throws MotorException
	 */
	public void getPositionCompare() throws MotorException {
		checkControllerAvailable();
		newportXpsController2.xpswrite("PositionerPositionCompareGet(" + xpsGroupName + "." + xpsPositionerName
				+ ", double *, double *, double *, bool *)");

		int error = Integer.parseInt(newportXpsController2.getErrnum());
		if (error == 0) {
			logger.debug("NewportXPSMotor: getPositionCompare() Command successful");
		} else {
			throw new MotorException(MotorStatus.FAULT, newportXpsController2.getErrorMessage(error));
		}

		String ret[] = newportXpsController2.getRetValue();
		pcoMinimumPosition = Double.parseDouble(ret[0]);
		pcoMaximumPosition = Double.parseDouble(ret[1]);
		pcoPositionStep = Double.parseDouble(ret[2]);
		pcoEnableState = Boolean.parseBoolean(ret[3]);
	}

	/**
	 * Enables the Positioner Compare output.
	 *
	 * @throws MotorException
	 */
	public void enablePositionerCompare() throws MotorException {
		checkControllerAvailable();
		newportXpsController1.xpswrite("PositionerPositionCompareEnable(" + xpsGroupName + "." + xpsPositionerName
				+ ")");
		int error = Integer.parseInt(newportXpsController1.getErrnum());
		if (error == 0) {
			logger.debug("Command successful");
		} else {
			throw new MotorException(MotorStatus.FAULT, newportXpsController1.getErrorMessage(error));
		}
	}

	/**
	 * Disables the Positioner Compare output.
	 *
	 * @throws MotorException
	 */
	public void disablePositionerCompare() throws MotorException {
		checkControllerAvailable();
		newportXpsController1.xpswrite("PositionerPositionCompareDisable(" + xpsGroupName + "." + xpsPositionerName
				+ ")");
		int error = Integer.parseInt(newportXpsController1.getErrnum());
		if (error == 0) {
			logger.debug("Command successful");
		} else {
			throw new MotorException(MotorStatus.FAULT, newportXpsController1.getErrorMessage(error));
		}
	}

	void checkControllerAvailable() throws MotorException {
		if (controllerAvailable) {
			return;
		}
		throw new MotorException(MotorStatus.FAULT, "Newport XPS controller " + newportXpsControllerName
				+ " not available");
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		String error = null;
		double pco[] = new double[3];

		try {
			if ("PositionCompare".equalsIgnoreCase(attributeName)) {

				if ("enable".equalsIgnoreCase((String) value)) {
					enablePositionerCompare();
				} else if ("disable".equalsIgnoreCase((String) value)) {
					disablePositionerCompare();
				} else {
					// Assume that we are being passed 3 numbers (min, max,
					// step) in
					// a double[].
					pco = (double[]) value;
					setPositionCompare(pco[0], pco[1], pco[2]);
				}
			}
		} catch (MotorException e) {
			throw new DeviceException("Newport XPS motor (" + getXpsGroupName() + "." + getXpsPositionerName() + "):"
					+ error);
		}
	}

	/**
	 * Gets current state of Position compare output.
	 *
	 * @return State of Position Compare Output
	 */
	public boolean isPcoEnableState() {
		return pcoEnableState;
	}

	/**
	 * Gets PCO maximum position.
	 *
	 * @return maximum position
	 */
	public double getPcoMaximumPosition() {
		return pcoMaximumPosition;
	}

	/**
	 * Gets PCO minimum position.
	 *
	 * @return minimum position
	 */
	public double getPcoMinimumPosition() {
		return pcoMinimumPosition;
	}

	/**
	 * Gets PCO step size.
	 *
	 * @return step size
	 */
	public double getPcoPositionStep() {
		return pcoPositionStep;
	}

}
