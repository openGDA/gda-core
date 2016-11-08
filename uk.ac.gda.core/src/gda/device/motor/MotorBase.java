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

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceBase;
import gda.device.Motor;
import gda.device.MotorException;
import gda.factory.Configurable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base implementation of a generic Motor for all real motor types, which are therefore subclasses of this class.
 */
public abstract class MotorBase extends DeviceBase implements Motor, Serializable, Configurable {

	private static final Logger logger = LoggerFactory.getLogger(MotorBase.class);

	private ObjectOutputStream out = null;

	private ObjectInputStream in = null;

	private String separator = System.getProperty("file.separator");

	private String filePath = ".";

	private double backlashSteps = 0.0;

	// README: protected as some subclasses need access to it
	protected boolean correctBacklash = false;

	private double[] speedLevel = new double[Motor.SPEED_LEVELS];

	private boolean[] speedPossible = new boolean[Motor.SPEED_LEVELS];

	private boolean limitsSettable = false;

	protected double minPosition = Double.NaN;

	protected double maxPosition = Double.NaN;

	protected volatile boolean isInitialised = false;

	/**
	 * Constructor.
	 */
	public MotorBase() {
		if ((filePath = LocalProperties.get("gda.motordir")) == null)
			filePath = ".";
	}

	/**
	 * Changes the maximum velocity fast speed setting.
	 *
	 * @param fastSpeed
	 *            velocity in motor hardware units
	 */
	public void setFastSpeed(double fastSpeed) {
		speedLevel[Motor.FAST] = fastSpeed;
		speedPossible[Motor.FAST] = true;
	}

	/**
	 * @return fast speed velocity in motor hardware units
	 */
	public double getFastSpeed() {
		return speedLevel[Motor.FAST];
	}

	/**
	 * Changes the maximum velocity medium speed setting.
	 *
	 * @param mediumSpeed
	 *            velocity in motor hardware units
	 */
	public void setMediumSpeed(double mediumSpeed) {
		speedLevel[Motor.MEDIUM] = mediumSpeed;
		speedPossible[Motor.MEDIUM] = true;
	}

	/**
	 * @return medium speed velocity in motor hardware units
	 */
	public double getMediumSpeed() {
		return speedLevel[Motor.MEDIUM];
	}

	/**
	 * Changes the maximum velocity slow speed setting.
	 *
	 * @param slowSpeed
	 *            velocity in motor hardware units
	 */
	public void setSlowSpeed(double slowSpeed) {
		speedLevel[Motor.SLOW] = slowSpeed;
		speedPossible[Motor.SLOW] = true;
	}

	/**
	 * @return slow speed velocity in motor hardware units
	 */
	public double getSlowSpeed() {
		return speedLevel[Motor.SLOW];
	}

	/**
	 * Changes the current backlash correction to the specified hardware units.
	 *
	 * @param backlashSteps
	 *            backlash correction drive in motor hardware units
	 */
	public void setBacklashSteps(double backlashSteps) {
		this.backlashSteps = backlashSteps;
	}

	/**
	 * @return current backlash correction in hardware units
	 */
	public double getBacklashSteps() {
		return backlashSteps;
	}

	/**
	 * Checks whether this motor is allowed to change its hardware soft limits. Motor implementations should overide
	 * this method if required.
	 *
	 * @return a default of false here, but would return true where limits are settable.
	 */
	@Override
	public boolean isLimitsSettable() {
		return limitsSettable;
	}

	/**
	 * Sets whether this motor should change the hardware's soft limits.
	 *
	 * @param limitsSettable
	 *            flag true if limits are settable
	 */
	public void setLimitsSettable(boolean limitsSettable) {
		this.limitsSettable = limitsSettable;
	}

	/**
	 * saves motor's current position, the persistence path is fixed by java.properties
	 *
	 * @param name
	 *            of file for position save
	 */
	public void savePosition(String name) {
		try {
			savePosition(name, getPosition());
		} catch (MotorException e) {
			logger.error("MotorBase.savePosition() for motor " + getName() + " caught exception " + e.getMessage());
		}
	}

	/**
	 * saves motor's current position, the persistence path is fixed by java.properties
	 *
	 * @param name
	 *            of file for position save
	 * @param currentPosition
	 */
	public void savePosition(String name, double currentPosition) {
		try {
			// work out the file name
			String filename = filePath + separator + name;
			File saveFile = new File(filename);

			// check if file exists
			if (!saveFile.exists()) {
				// if not, first test if the motorPositions folder has been
				// created
				String dirName = filePath;
				File motorDir = new File(dirName);

				// create motorPositions folder if necessary
				if (!motorDir.exists()) {
					logger.info("Motor positions folder not found. Creating new folder:" + motorDir);
					motorDir.mkdir();
				}

				// then create a new file
				logger.info("Motor positions file for motor " + name + " not found. Creating new file:" + filename);
				saveFile.createNewFile();
			}

			// open and write the file
			out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(saveFile)));
			out.writeDouble(currentPosition);
			out.flush();
			out.close();
		} catch (IOException ex) {
			logger.debug("IOException in savePosition(): " + ex.getMessage());
		}
	}

	/**
	 * loads motor's current position, the persistence path is fixed by java.properties FIXME perhaps loadPosition
	 * should throw MotorBaseException but may have big impact on code base
	 *
	 * @param name
	 *            persistent filename
	 * @param defaultPosition
	 *            default position if the motor position file does not exist or is empty
	 */
	public void loadPosition(String name, double defaultPosition) {

		if (!checkFilePathExistsOrCreate()) {
			logger.error("Motor Positions folder " + filePath + " does not exist and could not be created.");
			return;
		}

		String fullName = filePath + separator + name;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(fullName);
			in = new ObjectInputStream(new BufferedInputStream(fis));
			setPosition(in.readDouble());
			in.close();
		} catch (FileNotFoundException fnfe) {
			logger.info("Motor Position File " + fullName + " not found - setting " + name + " position to "
					+ defaultPosition + " and creating new file.");
			try {
				setPosition(defaultPosition);
				savePosition(name);
			} catch (MotorException e) {
				logger.error("MotorBase.loadPosition() for motor " + getName() + " caught exception " + e.getMessage());
			}
		} catch (EOFException eofe) {
			logger.error("unexpected EOF in Motor Position File " + fullName + " trying to read position as int");
			try {
				// have already asserted EOFException so OK to do this
				if (fis.available() > 0) {
					in = new ObjectInputStream(new BufferedInputStream(fis));
					setPosition(in.readInt());
					in.close();
					savePosition(name);
				} else {
					logger.info("Motor Position File empty setting posn to " + defaultPosition);
					setPosition(defaultPosition);
					savePosition(name);
				}
			} catch (IOException ioe) {
				logger.error("IOException in MotorBase.loadPosition");
				logger.debug(ioe.getStackTrace().toString());
			} catch (MotorException e) {
				logger.debug(e.getStackTrace().toString());
			}
		} catch (IOException ioe) {
			logger.debug(ioe.getStackTrace().toString());
		} catch (MotorException e) {
			logger.debug(e.getStackTrace().toString());
		}
	}

	public void loadPosition(String name) {
		loadPosition(name, 0);
	}

	private boolean checkFilePathExistsOrCreate() {
		// to avoid later FileNotFoundExceptions
		File filePathFolder = new File(filePath);
		if (!filePathFolder.exists()) {
			return filePathFolder.mkdirs();
		}

		return true;
	}

	@Override
	public double getTimeToVelocity() throws MotorException {
		throw new UnsupportedOperationException("Getting this motor's time to velocity is not supported");
	}

	@Override
	public void setTimeToVelocity(double timeToVelocity) throws MotorException {
		throw new UnsupportedOperationException("Setting this motor's time to velocity is not supported");
	}

	@Override
	public void setSpeedLevel(int speed) throws MotorException {
		if (speedPossible[speed])
			setSpeed(speedLevel[speed]);
		else {
			/*
			 * FIXME: Perhaps this should throw a MotorException. throw new MotorException(MotorStatus.FAULT, "Cannot
			 * set speed level"); I've not implemented this yet. I'm not sure this is an Exception or that everyone will
			 * want to have to set speeds up. The code is in place to deal with exceptions (so far as OEMove is
			 * concerned at least).
			 */
			logger.debug("Warning speed " + speed + " cannot be set.");
		}
	}

	/**
	 * The calculation used to determine the size of the first part of a backlash move and to set whether backlash is
	 * required.
	 *
	 * @param increment
	 *            requested size of move in hardware units
	 * @return size of move adjusted for backlash correction if needed
	 */
	public double addInBacklash(double increment) {
		if ((getBacklashSteps() > 0 && increment < 0.0) || (getBacklashSteps() < 0 && increment > 0.0)) {
			correctBacklash = true;
			increment -= getBacklashSteps();
		} else {
			correctBacklash = false;
		}
		return increment;
	}

	/**
	 * If backlash is required, this method instigates the final backlash move. This method works for motors which use
	 * incremental moves and should be overridden by motors which use absolute encoder positions.
	 *
	 * @throws MotorException
	 */
	@Override
	public void correctBacklash() throws MotorException {
		if (correctBacklash) {
			logger.debug("MotorBase correctBacklash about to move by " + getBacklashSteps() + " steps");
			moveBy(getBacklashSteps());
		}
	}

	/**
	 * Sets the software limits. Some motors/motor controllers are capable of setting up softlimits in them so that the
	 * so that the hard limit switch is never hit (at least theoretically) Such motor implementations should overide
	 * this method if required.
	 *
	 * @param minPosition
	 *            the minimum softlimit
	 * @param maxPosition
	 *            the maximum softlimit
	 * @throws MotorException
	 */
	@Override
	public void setSoftLimits(double minPosition, double maxPosition) throws MotorException {
		// README: delibrately do nothing
	}

	@Override
	public double getMinPosition() throws MotorException {
		return minPosition;
	}

	public void setMinPosition(double minPosition) throws MotorException {
		this.minPosition = minPosition;
	}

	@Override
	public double getMaxPosition() throws MotorException {
		return maxPosition;
	}

	public void setMaxPosition(double maxPosition) throws MotorException {
		this.maxPosition = maxPosition;
	}

	@Override
	public double getRetryDeadband() throws MotorException {
		logger.warn("Retry deadband or position tolerance is not implmented for {}", getName());
		return Double.NaN;
	}

	/**
	 * Checks if the motor is homeable or not. Motor implementations should overide this method if required.
	 *
	 * @return if the motor is homeable. Returns false!
	 */
	@Override
	public boolean isHomeable() {
		return false;
	}

	/**
	 * Checks if the motor is homed or not. Motor implementations should overide this method if required.
	 *
	 * @return if the motor is homed. Returns false!
	 * @throws MotorException
	 */
	@Override
	public boolean isHomed() throws MotorException {
		return false;
	}

	/**
	 * Moves the motor to a repeatable starting location. Motor implementations should overide this method if required.
	 *
	 * @throws MotorException
	 */
	@Override
	public void home() throws MotorException {
	}

	@Override
	public boolean isInitialised() {
		return isInitialised;
	}

	/**
	 * @param initialised
	 */
	public void setInitialised(boolean initialised) {
		this.isInitialised = initialised;
	}

	@Override
	public double getMotorResolution() throws MotorException {
		return Double.NaN;
	}

	@Override
	public double getUserOffset() throws MotorException {
		return Double.NaN;
	}
}
