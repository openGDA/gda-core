/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import static java.lang.Math.abs;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.ControllerRecord;
import gda.device.MotorException;
import gda.device.MotorProperties.MotorEvent;
import gda.device.MotorProperties.MotorProperty;
import gda.device.MotorStatus;
import gda.epics.AccessControl;
import gda.epics.connection.CompoundDataTypeHandler;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.EpicsController.MonitorType;
import gda.epics.connection.InitializationListener;
import gda.epics.connection.STSHandler;
import gda.epics.connection.TIMEHandler;
import gda.epics.util.EpicsGlobals;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.jython.JythonServerFacade;
import gda.jython.JythonStatus;
import gda.observable.IObserver;
import gov.aps.jca.CAException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.dbr.DBR_Float;
import gov.aps.jca.dbr.DBR_Short;
import gov.aps.jca.dbr.Severity;
import gov.aps.jca.dbr.Status;
import gov.aps.jca.dbr.TimeStamp;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

/**
 * EpicsMotor implements GDA Motor interface and provide mapping from GDA interface to EPICS motor record. Note only
 * selected PVs or channels are instantiated in this class as required by the GDA motor interface.
 */
public class EpicsMotor extends MotorBase implements InitializationListener, IObserver, ControllerRecord {

	/** Possible actions to take when a motor does not reach its target during a move */
	public enum MissedTargetLevel {
		/** Positions are not checked at the end of moves */
		IGNORE,
		/** Positions are checked at the end of each move and a warning is logged if target is missed */
		WARN,
		/** Positions are checked at the end of each move and the motor is put in an error state if target is missed */
		FAULT;
	}

	private static final Logger logger = LoggerFactory.getLogger(EpicsMotor.class);

	private static MoveEventQueue moveEventQueue = new MoveEventQueue();

	private static final short SET_USE_PV_USE_VALUE = 0;
	private static final short SET_USE_PV_SET_VALUE = 1;

	/** field bit positions in MSTA - leave all for legibility, cross-reference */
	@SuppressWarnings("unused")
	private static final short MSTA_DIRECTION_POSITIVE = 0;
	private static final short MSTA_DONE = 1;
	private static final short MSTA_UPPER_LIMIT = 2;
	@SuppressWarnings("unused")
	private static final short MSTA_HOME_LIMIT = 3;
	@SuppressWarnings("unused")
	private static final short MSTA_UNUSED = 4;
	@SuppressWarnings("unused")
	private static final short MSTA_CLOSED_LOOP = 5;
	private static final short MSTA_FOLLOWING_ERROR = 6;
	@SuppressWarnings("unused")
	private static final short MSTA_AT_HOME = 7;
	@SuppressWarnings("unused")
	private static final short MSTA_ENCODER_PRESENT = 8;
	private static final short MSTA_FAULT = 9;
	@SuppressWarnings("unused")
	private static final short MSTA_MOVING = 10;
	@SuppressWarnings("unused")
	private static final short MSTA_GAIN_SUPPORT = 11;
	private static final short MSTA_COMMS_ERROR = 12;
	private static final short MSTA_LOWER_LIMIT = 13;
	private static final short MSTA_HOMED = 14;

	/**
	 * EPICS channels to connect
	 */
	protected Channel val = null; // user desired value .VAL, double in EGU

	protected Channel rbv = null; // User readback value .RBV, double in EGU

	protected Channel direction;

	protected Channel offset = null; // set motor offset without moving motor

	protected Channel stop = null; // the motor stop control

	protected Channel velo = null; // Velocity (EGU/s) .VELO, FLOAT

	protected Channel vmax; // Max Velocity

	protected Channel accl;

	protected Channel lvio = null; // Limit Violation, .LVIO, SHORT

	protected Channel dmov = null; // Done move to value, .DMOV, SHORT

	protected Channel rdbd = null; // retry deadband

	protected Channel hlm = null; // User High Limit .HLM, FLOAT

	protected Channel llm = null; // User Lower Limit .LLM, FLOAT

	protected Channel hls;

	protected Channel lls;

	protected Channel dhlm;

	protected Channel dllm;

	protected Channel homf = null; // Home Forward, .HOMF, SHORT

	protected Channel mres = null; // motor resolution

	protected Channel unitString = null; // EPICS motor Unit

	protected Channel msta = null;// Hardware status

	protected Channel spmg = null; // motor template mode (Go or Stop)

	/**
	 * monitor EPICS motor position
	 */
	protected MonitorListener positionMonitor;

	/**
	 * Monitor EPICS motor's DMOV - EPICS motor motion completion status
	 */
	protected MonitorListener statusMonitor;

	/**
	 * Monitor EPICS motor lower limit
	 */
	protected MonitorListener lowLimitMonitor;

	/**
	 * Monitor EPICS motor higher limit
	 */
	protected MonitorListener highLimitMonitor;

	/**
	 * Monitor EPICS motor dial higher limit
	 */
	protected MonitorListener dialHighLimitMonitor;

	/**
	 * Monitor EPICS motor dial lower limit
	 */
	protected MonitorListener dialLowLimitMonitor;

	protected MonitorListener mstaMonitorListener;
	/**
	 * Monitor EPICS motor limit violation
	 */
	protected MonitorListener lvioMonitor;

	/**
	 * Monitors for the limit switch states .LLS and .HLS
	 */
	protected MonitorListener lowLimitStateMonitor;
	protected MonitorListener highLimitStateMonitor;

	protected Channel setPv;
	protected MonitorListener setUseListener;

	/**
	 * EPICS Put call back handler
	 */
	protected PutListener putCallbackListener;

	protected String pvName;

	/**
	 * EPICS controller
	 */
	protected EpicsController controller;

	/**
	 * EPICS Channel Manager
	 */
	protected EpicsChannelManager channelManager;

	private final Object _motorStatusMonitor = new Object();
	private final ReadWriteLock setUseLock = new ReentrantReadWriteLock();

	private double dialHighLimit = Double.NaN;
	private double dialLowLimit = Double.NaN;

	private boolean assertHomedBeforeMoving = false;

	/**
	 * Cached motor properties
	 */
	private volatile double currentPosition = Double.NaN;

	private volatile double currentSpeed = Double.NaN;

	private volatile MotorStatus _motorStatus = MotorStatus.UNKNOWN;

	private volatile Boolean homed = null;

	private volatile double targetPosition = Double.NaN;

	private volatile double retryDeadband;

	private boolean callbackWait = false;

	private boolean DMOVRefreshEnabled = true;
	// we always get a DMOV after a caput_callback so we do not need to act on it
	// private boolean ignoreNextDMOV = false;

	private Status status = Status.NO_ALARM;
	private Severity severity = Severity.NO_ALARM;
	private TimeStamp timestamp = null;

	private boolean alarmRaised = false;

	private MotorStatus mstaStatus = MotorStatus.READY;

	private SetUseState setUseMode = SetUseState.UNKNOWN;

	private MotorStatus lastMotorStatus = MotorStatus.UNKNOWN;

	/**
	 * Motor access control object name (CASTOR XML)
	 */
	private String accessControlName;

	/**
	 * Motor access control object (hook-up in CASTOR XML)
	 */
	private AccessControl accessControl;

	private AccessControl.Status acs = AccessControl.Status.ENABLED;

	/**
	 * request completion flag
	 */
	@SuppressWarnings("unused")
	private volatile boolean requestDone = true; // start true

	/** The action to take when a motor does not reach its target during a move */
	private MissedTargetLevel missedTargetAction = MissedTargetLevel.WARN;

	/**
	 * Normally the unitString is read from EPICS EGu field. But if this is no supported then we may have to set it.
	 * Only setter is provided for object configuration
	 */
	private String unitStringOverride = null;

	public EpicsMotor() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		positionMonitor = this::rbvMonitorChanged;
		statusMonitor = this::dmovMonitorChanged;
		putCallbackListener = this::putCompleted;
		highLimitMonitor = this::hlmMonitorChanged;
		lowLimitMonitor = this::llmMonitorChanged;
		highLimitStateMonitor = this::hlsMonitorChanged;
		lowLimitStateMonitor = this::llsMonitorChanged;
		dialHighLimitMonitor = this::dhlmMonitorChanged;
		dialLowLimitMonitor = this::dllmMonitorChanged;
		lvioMonitor = this::lvioMonitorChanged;
		mstaMonitorListener = this::mstaMonitorChanged;
		setUseListener = this::setUseMonitorChanged;
	}

	/**
	 * Constructor taking a motor name
	 *
	 * @param name
	 *            name of the motor
	 */
	public EpicsMotor(String name) {
		this();
		setName(name);
	}


	public void setUnitStringOverride(String unitStringOverride) {
		this.unitStringOverride = unitStringOverride;
	}

	/**
	 * Sets the record name that this motor will link to.
	 *
	 * @param pvName
	 *            the record name
	 */
	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	public String getPvName() {
		return pvName;
	}

	/**
	 * Sets the access control object used by this motor.
	 *
	 * @param accessControl
	 *            the access control object
	 */
	public void setAccessControl(AccessControl accessControl) {
		this.accessControl = accessControl;
	}

	/**
	 * Initialise the motor object.
	 */
	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {

			if (pvName == null) {
				throw new IllegalStateException("pvName is required to configure EpicsMotor");
			}

			createChannelAccess();
			channelManager.tryInitialize(100);

			// If no access control object has been set, but a name has been specified, look up the name
			if (accessControl == null && getAccessControlName() != null) {
				accessControl = Finder.find(accessControlName);
				if (accessControl == null) {
					throw new FactoryException("Can not find access control object " + accessControl.getName());
				}
			}

			if (accessControl != null) {
				this.acs = accessControl.getStatus();
				accessControl.addIObserver(this);
			}
			setConfigured(true);
		}
	}

	public void forceCallback() throws MotorException {
		moveEventQueue.addMoveCompleteEvent(EpicsMotor.this, MotorStatus.READY, STATUSCHANGE_REASON.CAPUT_MOVECOMPLETE);
	}

	/**
	 * Create Channel access for motor. This must must on to EPICS motor record.
	 */
	protected void createChannelAccess() throws FactoryException {
		try {
			val = channelManager.createChannel(pvName + ".VAL", false);
			rbv = channelManager.createChannel(pvName + ".RBV", positionMonitor, MonitorType.TIME, false);

			direction = channelManager.createChannel(pvName + ".DIR", false);
			offset = channelManager.createChannel(pvName + ".OFF", false);

			stop = channelManager.createChannel(pvName + ".STOP", false);
			velo = channelManager.createChannel(pvName + ".VELO", false);
			vmax = channelManager.createChannel(pvName + ".VMAX", false);
			accl = channelManager.createChannel(pvName + ".ACCL", false);
			dmov = channelManager.createChannel(pvName + ".DMOV", statusMonitor, false);
			lvio = channelManager.createChannel(pvName + ".LVIO");
			hlm = channelManager.createChannel(pvName + ".HLM", highLimitMonitor, false);
			llm = channelManager.createChannel(pvName + ".LLM", lowLimitMonitor, false);

			dhlm = channelManager.createChannel(pvName + ".DHLM", dialHighLimitMonitor, false);
			dllm = channelManager.createChannel(pvName + ".DLLM", dialLowLimitMonitor, false);
			homf = channelManager.createChannel(pvName + ".HOMF", false);

			rdbd = channelManager.createChannel(pvName + ".RDBD", false);
			mres = channelManager.createChannel(pvName + ".MRES", false);
			unitString = channelManager.createChannel(pvName + ".EGU", false);
			msta = channelManager.createChannel(pvName + ".MSTA", mstaMonitorListener, false);
			spmg = channelManager.createChannel(pvName + ".SPMG", false);
			setPv = channelManager.createChannel(pvName + ".SET", setUseListener, false);

			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();

		} catch (Exception ex) {
			throw new FactoryException("failed to connect to all channels", ex);
		}
	}

	private void waitForInitialisation() throws TimeoutException, FactoryException {
		configure();
		final long startTime_ms = System.currentTimeMillis();
		final double timeout_s = EpicsGlobals.getTimeout();
		final long timeout_ms = (long) (timeout_s * 1000.);

		while (!isInitialised() && (System.currentTimeMillis() - startTime_ms < timeout_ms)) {
			try {
				Thread.sleep(timeout_ms / 5); // TODO: Are we sure!?
			} catch (InterruptedException e) {
				// Reset interrupt status
				Thread.currentThread().interrupt();
			}
		}
		if (!isInitialised())
			throw new TimeoutException(getName() + " not yet initalised. Does the PV " + pvName + " exist?");
	}

	/**
	 * gets the unit string from EPICS motor.
	 *
	 * @return unit string
	 */
	@Override
	public String getUnitString() throws MotorException {
		try {
			if (unitStringOverride != null)
				return unitStringOverride;
			waitForInitialisation();
			return controller.caget(unitString);
		} catch (Exception e) {
			throw new MotorException(getStatus(), "failed to get motor engineering unit", e);
		}
	}

	/**
	 * Sets the speed of the motor in IOC in mm/second.
	 */
	@Override
	public void setSpeed(double mmPerSec) throws MotorException {
		try {
			// must use caputWait to ensure the speed is set before we start moving
			controller.caputWait(velo, mmPerSec);
			currentSpeed = mmPerSec;
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "failed to set setSpeed", ex);
		}
	}

	/**
	 * Gets the current speed of the motor in mm/second
	 *
	 * @return double the motor speed in revolution per second
	 */
	@Override
	public double getSpeed() throws MotorException {
		try {
			currentSpeed = controller.cagetDouble(velo);
			return currentSpeed;
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "failed to get speed", ex);
		}
	}

	public double getMaxSpeed() throws MotorException {
		try {
			return controller.cagetDouble(vmax);
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "failed to get max speed", ex);
		}
	}

	@Override
	public void setTimeToVelocity(double timeToVelocity) throws MotorException {
		try {
			controller.caput(accl, timeToVelocity);
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "failed to set acceleration", ex);
		}
	}

	@Override
	public double getTimeToVelocity() throws MotorException {
		try {
			final double timeToVelocity = controller.cagetDouble(accl);
			return timeToVelocity;
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "failed to get acceleration", ex);
		}
	}

	/**
	 * Gets the retry dead band for this motor from EPICS.
	 *
	 * @return double - the retry dead band.
	 */
	@Override
	public double getRetryDeadband() throws MotorException {
		try {
			retryDeadband = controller.cagetDouble(rdbd);
			return retryDeadband;
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "failed to get speed", ex);
		}
	}

	/**
	 * Gets the motor resolution from EPICS motor.
	 *
	 * @return double - the motor resolution
	 */
	@Override
	public double getMotorResolution() throws MotorException {
		try {
			return controller.cagetDouble(mres);
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "failed to get resolution", ex);
		}
	}

	public MotorDirection getDirection() throws MotorException {
		try {
			final int directionValue = controller.cagetInt(direction);
			return MotorDirection.fromInt(directionValue);
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "Failed to get direction", ex);
		}
	}

	@Override
	public double getUserOffset() throws MotorException {
		try {
			return controller.cagetDouble(offset);
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "failed to get speed", ex);
		}

	}

	public void setUserOffset(double userOffset) throws MotorException {
		try {
			controller.caput(offset, userOffset);
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "failed to set user offset", ex);
		}
	}

	@Override
	public boolean isMoving() throws MotorException {
		return (checkStatus() == MotorStatus.BUSY);
	}

	/**
	 * checks motor Status
	 */
	protected MotorStatus checkStatus() throws MotorException {
		MotorStatus readMotorStatus = getStatus();
		logger.trace("Checking motor status {}", readMotorStatus);
		if (readMotorStatus == MotorStatus.UNKNOWN || readMotorStatus == MotorStatus.FAULT) {
			logger.error("Motor exception for {} or {}", MotorStatus.UNKNOWN, MotorStatus.FAULT);
			throw new MotorException(MotorStatus.FAULT, "getStatus returned " + readMotorStatus.toString());
		}
		return readMotorStatus;
	}

	/**
	 * Returns the motor status from the motor object.
	 */
	@Override
	public MotorStatus getStatus() throws MotorException {
		return _motorStatus;
	}

	/**
	 * Relative move, moves the motor by the specified mount in user coordinate system units, specified in the .EGU
	 * field of the Motor record.
	 *
	 * @note The target position is re-checked before move as many limits in the EPICS motor changes dynamically.
	 * @param increament
	 *            - double the distance that motor need to travel in EGU
	 */
	@Override
	public void moveBy(double increament) throws MotorException {
		try {
			targetPosition = getPosition() + increament;
			targetRangeCheck(targetPosition);
			moveTo(targetPosition);
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "failed to moveBy", ex);
		}
	}

	enum STATUSCHANGE_REASON {

		START_MOVETO,

		MOVETO,

		INITIALISE,

		CAPUT_MOVECOMPLETE,

		/**
		 * Like CAPUT_MOVECOMPLETE but the msta has to be queried in a separate thread to prevent EPICS timeouts
		 */
		CAPUT_MOVECOMPLETE_IN_ERROR,

		DMOV_MOVECOMPLETE,

		NEWSTATUS

	}

	/*
	 * This is where the observers are updated with status. It is only called by the single thread so no need for
	 * synchronisation objects. DO NOT CALL DIRECTLY - use MoveEventQueue.addMoveCompleteEvent instead
	 */
	void changeStatusAndNotify(MotorStatus newStatus, STATUSCHANGE_REASON reason) throws MotorException {
		try {
			logger.trace("{} changeStatusAndNotify started.. newStatus = {}. reason = {}", getName(), newStatus, reason);
			switch (reason) {
			case INITIALISE:
				setMotorStatus(MotorStatus.READY);
				notifyIObservers(MotorProperty.STATUS, getStatus());
				setInitialised(true);
				DMOVRefreshEnabled = true;
				logger.debug("{} initialised.", getName());
				break;
			case START_MOVETO:
				DMOVRefreshEnabled = false; // prevent DMOV listener update
				final MotorStatus oldStatus = getStatus();
				setMotorStatus(MotorStatus.BUSY);
				try {
					logger.debug("{}: caput with callback {} <<<", getName(), targetPosition);
					controller.caput(val, targetPosition, putCallbackListener);
				} catch (Exception ex) {
					DMOVRefreshEnabled = true;
					setMotorStatus(oldStatus);
					throw ex;
				}
				break;
			case MOVETO:
				/* this is called in the queue after the START_MOVETO was executed straightaway */
				notifyIObservers(MotorProperty.STATUS, getStatus());
				break;
			case CAPUT_MOVECOMPLETE:
				if (newStatus != null)
					setMotorStatus(newStatus);
				notifyIObservers(EpicsMotor.this, MotorEvent.MOVE_COMPLETE);
				logger.debug("{} notifying CAPUT_MOVECOMPLETE {}", getName(), getStatus());
				DMOVRefreshEnabled = true; // allow DMOV listener to refresh
				break;
			case CAPUT_MOVECOMPLETE_IN_ERROR:
				MotorStatus motorStatusFromMSTAValue = MotorStatus.FAULT;
				try {
					Double mstaVal = controller.cagetDouble(msta);
					motorStatusFromMSTAValue = getMotorStatusFromMSTAValue(mstaVal.intValue());
				} catch (Exception e) {
					logger.error("Error gettting msta val for {}", getName(), e);
				}
				changeStatusAndNotify(motorStatusFromMSTAValue, STATUSCHANGE_REASON.CAPUT_MOVECOMPLETE);
				break;
			case DMOV_MOVECOMPLETE:
				// DMOVRefreshEnabled could have been changed to false by START_MOVETO since the event was added to the
				// MoveEvent queue
				if (DMOVRefreshEnabled) {
					if (newStatus != null)
						setMotorStatus(newStatus);
					notifyIObservers(EpicsMotor.this, MotorEvent.MOVE_COMPLETE);
					logger.trace("{} notifying DMOV_MOVECOMPLETE. New Status: {}", getName(), getStatus());
				}
				break;
			case NEWSTATUS:
				// DMOVRefreshEnabled could have been changed to false by START_MOVETO since the event was added to the
				// MoveEvent queue
				if (DMOVRefreshEnabled) {
					if (newStatus != null && !newStatus.equals(getStatus())) {
						setMotorStatus(newStatus);
						notifyIObservers(EpicsMotor.this, MotorEvent.REFRESH);
						logger.trace("{} notifying NEWSTATUS {}", getName(), getStatus());
					}
				}
				break;
			}
		} catch (MotorException me) {
			throw me;
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "Error in changeStatusAndNotify", ex);
		} finally {
			logger.trace("{} changeStatusAndNotify complete", getName());
		}
	}

	/**
	 * Absolute move, moves the motor to the specified position in user coordinate system units, specified by .EGU field
	 * of the Motor Record.
	 *
	 * @note The target position is re-checked before move as many limits in the EPICS motor changes dynamically.
	 * @param position
	 *            - double - the absolute position of the motor in EGU (
	 */
	@Override
	public void moveTo(double position) throws MotorException {

		checkMotorIsInUseMode();

		targetPosition = position;
		targetRangeCheck(position);
		logger.debug("{}: moveto {}", getName(), position);

		if (acs == AccessControl.Status.DISABLED)
			throw new MotorException(getStatus(), "moveTo aborted because this motor is disabled");

		if (getStatus() == MotorStatus.BUSY)
			throw new MotorException(getStatus(), "moveTo aborted because previous move not yet completed");
		if (getStatus() == MotorStatus.FAULT)
			throw new MotorException(getStatus(), "moveTo aborted because EPICS Motor is at Fault status. Please check EPICS Screen.");
		if (isAssertHomedBeforeMoving() && !isHomed()) {
			throw new MotorException(getStatus(), "moveTo aborted because EPICS Motor is not homed (and assertHomedBeforeMoving is set)");
		}

		moveEventQueue.addMoveCompleteEvent(EpicsMotor.this, null, STATUSCHANGE_REASON.MOVETO);
	}

	/**
	 * moves motor to the specified position with timeout in seconds. If motor does not callback within the specified
	 * time, this method time-out.
	 *
	 * @note The target position is re-checked before move as many limits in the EPICS motor changes dynamically.
	 */
	public void moveTo(double position, double timeout) throws MotorException, TimeoutException, InterruptedException {

		checkMotorIsInUseMode();
		targetPosition = position;
		targetRangeCheck(position);
		/*
		 * This moveTo does not change motorStatus and so cannot use caputListener which does
		 */
		try {
			controller.caput(val, targetPosition, timeout);
		} catch (CAException ex) {
			throw new MotorException(getStatus(), "Error in moveTo with timeout", ex);
		}
	}

	/**
	 * Asynchronously moves the motor to the specified position in EGU with a specified PutListener. You must handle the
	 * callback in your PutListener code.
	 *
	 * @note The target position is re-checked before move as many limits in the EPICS motor changes dynamically.
	 * @param position
	 *            the absolute position of the motor in EGU
	 */
	/*
	 * This moveTo does not change motorStatus.
	 */
	public void moveTo(double position, PutListener moveListener) throws MotorException {

		checkMotorIsInUseMode();

		try {
			targetRangeCheck(position);
			// to reduce the race condition between EPICS Control and
			// GDA request, however it does NOT prevent or eliminate the
			// race condition as GDA can not lock EPICS access, so some
			// sort of delayed action on EPICS DISABLED still required.
			while (acs == AccessControl.Status.DISABLED) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// Reset interrupt status
					Thread.currentThread().interrupt();
				}
			}
			controller.caput(val, position, moveListener);
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "failed to moveTo", ex);
		}
	}

	/**
	 * Reads the motor's dial low limit (DLLM).
	 *
	 * @return the dial low limit
	 */
	protected double getDialLowLimit() throws MotorException {
		try {
			return Double.isNaN(dialLowLimit) ? controller.cagetDouble(dllm) : dialLowLimit;
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "Unable to read DLLM for " + getName(), ex);
		}
	}

	/**
	 * Reads the motor's dial high limit (DHLM).
	 *
	 * @return the dial high limit
	 */
	protected double getDialHighLimit() throws MotorException {
		try {
			return Double.isNaN(dialHighLimit) ? controller.cagetDouble(dhlm) : dialHighLimit;
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "Unable to read DHLM for " + getName(), ex);
		}
	}

	/**
	 * This method check the target position is within the limit range.
	 *
	 * @param requestedPosition
	 *            absolute requested target to validate within limits
	 */
	private void targetRangeCheck(double requestedPosition) throws MotorException {
		if (!hasLimitsToCheck()) {
			return;
		}

		double lowerLimit = getMinPosition();
		if (requestedPosition < lowerLimit) {
			throw (new MotorException(MotorStatus.LOWER_LIMIT, requestedPosition + " outside lower hardware limit of " + lowerLimit));
		}
		double upperLimit = getMaxPosition();
		if (requestedPosition > upperLimit) {
			throw (new MotorException(MotorStatus.UPPER_LIMIT, requestedPosition + " outside upper hardware limit of " + upperLimit));
		}
	}

	/**
	 * Checks if limits should be checked. The Epics convention is that if the dial high/low limits are zero, this means
	 * there are no limits.
	 *
	 * @return true unless both dial limits are 0
	 */
	private boolean hasLimitsToCheck() throws MotorException {
		boolean hasActiveHighLimit = 0.0 < abs(getDialHighLimit());
		boolean hasActiveLowLimit = 0.0 < abs(getDialLowLimit());

		return hasActiveLowLimit || hasActiveHighLimit;
	}

	/**
	 * Sets the minimum position. This does write to EPICS database.
	 *
	 * @param minimumPosition
	 *            the minimum position
	 */
	@Override
	public void setMinPosition(double minimumPosition) throws MotorException {
		try {
			controller.caput(llm, minimumPosition);
			this.minPosition = minimumPosition;
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "failed to set min position", ex);
		}
	}

	/**
	 * {@inheritDoc} Get the minimum position from EPICS database.
	 *
	 * @return the minimum position, or NaN if limits are not be checked
	 */
	@Override
	public double getMinPosition() throws MotorException {
		if (!hasLimitsToCheck()) {
			return Double.NaN;
		}
		try {
			return Double.isNaN(minPosition) ? (minPosition = controller.cagetDouble(llm)) : minPosition;
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "failed to get min position", ex);
		}
	}

	/**
	 * Sets the maximum position. This does write to EPICS database.
	 *
	 * @param maximumPosition
	 *            the maximum position
	 */
	@Override
	public void setMaxPosition(double maximumPosition) throws MotorException {
		try {
			controller.caput(hlm, maximumPosition);
			this.maxPosition = maximumPosition;
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "failed to set max position", ex);
		}
	}

	/**
	 * {@inheritDoc} Get the maximum position from EPICS database.
	 *
	 * @return the maximum position, or NaN if limits are not be checked
	 */
	@Override
	public double getMaxPosition() throws MotorException {
		if (!hasLimitsToCheck()) {
			return Double.NaN;
		}
		try {
			return Double.isNaN(maxPosition) ? (maxPosition = controller.cagetDouble(hlm)) : maxPosition;
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "failed to get max position", ex);
		}
	}

	/**
	 * Stops the motor
	 */
	@Override
	public void stop() throws MotorException {
		try {
			if (isConfigured())
				controller.caput(stop, 1);
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "failed to stop", ex);
		}
	}

	/**
	 * Tells the motor record to stop trying to move the motor and then resets it.
	 * <p>
	 * This is different to a normal stop and should be used when the motor is 'stuck' in a moving state. This is the
	 * same as using the Combo box control in the edm screens.
	 * <p>
	 * This is for EpicsMotor specific error handling and would probably only need to be used when there are underlying
	 * hardware issues.
	 */
	public void stopGo() throws MotorException {
		try {
			controller.caputWait(spmg, "Stop");
			controller.caput(spmg, "Go");
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "failed to stop", ex);
		}
	}

	/**
	 * Some motors offer a control for emergence stop which stop the motor and switch off the power. This is not
	 * implemented here for EPICS motor, i.e. code block is empty.
	 */
	@Override
	public void panicStop() throws MotorException {
		// no op
	}

	@Override
	public void moveContinuously(int direction) throws MotorException {
		// TODO check if this implementation correct
		try {
			if (direction > 0) {
				moveTo(controller.cagetFloat(hlm));
			} else {
				moveTo(controller.cagetFloat(llm));
			}
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "failed to move continuously", ex);
		}
	}

	/**
	 * Set the position of the motor without moving it
	 *
	 * @param position
	 *            the new offset in motor units
	 */
	@Override
	public void setPosition(double position) throws MotorException {
		try {
			final short initialSetUse = controller.cagetShort(setPv);
			controller.caput(setPv, SET_USE_PV_SET_VALUE);
			controller.caput(val, position);
			controller.caput(setPv, initialSetUse);
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "failed to set motor position", ex);
		}
	}

	/**
	 * This method returns the current position of the motor in user coordinates.
	 *
	 * @return the current position
	 */
	@Override
	public double getPosition() throws MotorException {
		try {
			currentPosition = controller.cagetDouble(rbv);
			return currentPosition;
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "failed to get position", ex);
		}
	}

	@Override
	public void home() throws MotorException {
		try {
			controller.caput(homf, 1, channelManager);
		} catch (Exception ex) {
			throw new MotorException(getStatus(), "failed to home", ex);
		}
	}

	@Override
	public void initializationCompleted() {
		try {
			// indicate that the channels are connected
			setInitialised(true);
			// if retry dead band does not set, using motor resolution as this
			// dead band.
			if ((retryDeadband = getRetryDeadband()) == 0) {
				retryDeadband = getMotorResolution();
			}
		} catch (MotorException e) {
			logger.error("Can not get retry deadband value from EPICS {}", rdbd.getName());
		}
		if (retryDeadband == 0) {
			logger.warn("{} retry Deadband is set to {}", getName(), retryDeadband);
		} else {
			logger.debug("{} retry Deadband is set to {}", getName(), retryDeadband);
		}
		try {
			moveEventQueue.addMoveCompleteEvent(EpicsMotor.this, MotorStatus.READY, STATUSCHANGE_REASON.INITIALISE);
		} catch (Exception ex) {
			logger.error("{} - Could not add move complete event to queue", getName(), ex);
		}
	}

	private void rbvMonitorChanged(MonitorEvent mev) {
		try {
			final DBR dbr = mev.getDBR();
			if (dbr.isTIME()) {
				currentPosition = CompoundDataTypeHandler.getDouble(dbr)[0];
				status = STSHandler.getStatus(dbr);
				severity = STSHandler.getSeverity(dbr);
				timestamp = TIMEHandler.getTimeStamp(dbr);
				notifyIObservers(MotorProperty.POSITION, new Double(currentPosition));
			} else {
				logger.error("Motor Alarm should return DBRTime value.");
			}

			if (status != Status.NO_ALARM || severity != Severity.NO_ALARM) {
				if (!alarmRaised) {
					logger.error("{} raises Alarm at {} : Status={}; Severity={}", getName(), timestamp.toMONDDYYYY(), status.getName(),
							severity.getName());
					alarmRaised = true;
				}
			} else {
				alarmRaised = false;
			}

		} catch (Exception ex) {
			logger.error("{} - Error in RBVMonitor", getName(), ex);
		}
	}

	/*
	 * Checks the most important bits first.
	 */
	private MotorStatus getMotorStatusFromMSTAValue(int msta) {
		MotorStatus decodedStatus = MotorStatus.UNKNOWN;
		String binaryStatus = Integer.toBinaryString(msta);
		String decimalStatus = Integer.toString(msta);

		if ( hasTrueBitAtAnyPosition(msta, MSTA_FAULT, MSTA_FOLLOWING_ERROR, MSTA_COMMS_ERROR) ) {
			decodedStatus = MotorStatus.FAULT;
			if (lastMotorStatus != decodedStatus) {
				logger.error( "Motor - {} has FAULT STATUS: Please check EPICS motor's status word logged at DEBUG level.", getName() );
				logger.debug("EPICS Motor {} FAULT STATUS in decimal {} and binary {}.", getName(), decimalStatus, binaryStatus );
				lastMotorStatus = decodedStatus;
			}
			return decodedStatus;
		}

		if ( hasTrueBitAtPosition(msta,MSTA_UPPER_LIMIT) ) {
			decodedStatus = MotorStatus.UPPER_LIMIT;
			if (lastMotorStatus != decodedStatus) {
				logger.warn( "Motor - {} is at UPPERLIMIT; status word logged at DEBUG level.", getName() );
				logger.debug("EPICS Motor {} FAULT STATUS in decimal {} and binary {}.", getName(), decimalStatus, binaryStatus );
				lastMotorStatus = decodedStatus;
			}
			return decodedStatus;
		}

		if ( hasTrueBitAtPosition(msta,MSTA_LOWER_LIMIT) ) {
			decodedStatus = MotorStatus.LOWER_LIMIT;
			if (lastMotorStatus != decodedStatus) {
				logger.warn( "Motor - {} is at LOWERLIMIT; status word logged at DEBUG level.", getName() );
				logger.debug("EPICS Motor {} FAULT STATUS in decimal {} and binary {}.", getName(), decimalStatus, binaryStatus );
				lastMotorStatus = decodedStatus;
			}
			return decodedStatus;
		}
		if ( hasTrueBitAtPosition(msta, MSTA_DONE) ) {
			decodedStatus = MotorStatus.READY;
			if (lastMotorStatus != decodedStatus) {
				logger.debug("Motor - {} is READY.",getName());
				lastMotorStatus = decodedStatus;
			}
			return decodedStatus;
		}

		lastMotorStatus = decodedStatus;
		return decodedStatus;
	}

	public boolean isHomedFromMSTAValue(double msta) {
		int motorStatusAsInteger = new Double(msta).intValue();
		return hasTrueBitAtPosition(motorStatusAsInteger, MSTA_HOMED);
	}

	@Override
	public boolean isHomed() { // cannot throw checked exceptions
		if (homed != null) {
			return homed;
		}
		try {
			homed = isHomedFromMSTAValue(readMsta());
		} catch (Exception e) {
			logger.error("{} could not read MSTA record to get homed status (swallowed exception--RETURNING UNHOMED)", getName(), e);
			return false;
		}
		return homed;
	}

	public double readMsta() throws TimeoutException, CAException, InterruptedException {
		return controller.cagetShort(msta);
	}

	private void dmovMonitorChanged(MonitorEvent mev) {

		try {
			int dmovValue = -1;
			final DBR dbr = mev.getDBR();
			if (dbr.isSHORT()) {
				dmovValue = ((DBR_Short) dbr).getShortValue()[0];
			} else {
				logger.error(".DMOV should return SHORT type value.");
			}
			if (getStatus() == MotorStatus.BUSY) {
				if (dmovValue == 0) {
					logger.trace("Motor {} is moving ", getName());
				} else if (dmovValue == 1) {
					logger.trace("Motor {} is stopped at {}.", getName(), currentPosition);
				} else {
					logger.error("Illegal .DMOV value. {}", dmovValue);
				}
			} else {
				/*
				 * We cannot change the status as that is only to be looked after by the caput listener. Instead we simply cause the positioner to refresh.
				 */
				if (dmovValue == 1 && DMOVRefreshEnabled) {
					moveEventQueue.addMoveCompleteEvent(EpicsMotor.this, mstaStatus, STATUSCHANGE_REASON.DMOV_MOVECOMPLETE);
				}
			}
		} catch (Exception e) {
			logger.error("{} - Error in DMOV monitor", getName(), e);
		}
	}

	public double getTargetPosition() throws MotorException {
		try {
			return controller.cagetDouble(val);
		} catch (Exception e) {
			throw new MotorException(getStatus(), "failed to get target position", e);
		}
	}

	private enum SetUseState {
		UNKNOWN,
		SET,
		USE
	}

	private void setUseMonitorChanged(MonitorEvent event) {
		final DBR dbr = event.getDBR();

		if (!dbr.isENUM()) {
			logger.error("New value for {} SET PV has type {}; expected {}", getName(), dbr.getType().getName(), DBRType.ENUM.getName());
			return;
		}

		final DBR_Enum dbrEnum = (DBR_Enum) dbr;
		final short[] values = dbrEnum.getEnumValue();

		if (values.length != 1) {
			logger.error("New value for {} SET PV has {} value(s); expected 1", getName(), values.length);
			return;
		}

		final short newValue = values[0];
		if (newValue != SET_USE_PV_USE_VALUE && newValue != SET_USE_PV_SET_VALUE) {
			logger.error("New value for {} SET PV is {}; expected {} or {}", getName(), newValue, SET_USE_PV_USE_VALUE, SET_USE_PV_SET_VALUE);
			return;
		}

		try {
			setUseLock.writeLock().lock();

			final boolean firstUpdate = (setUseMode == SetUseState.UNKNOWN);
			final SetUseState newState = (newValue == SET_USE_PV_USE_VALUE) ? SetUseState.USE : SetUseState.SET;
			final boolean stateChanged = !firstUpdate && (setUseMode != newState);
			final boolean logNewState = (firstUpdate && newState == SetUseState.SET) || stateChanged;

			if (logNewState) {
				if (newState == SetUseState.USE) {
					logger.info("Motor {} is now in 'Use' mode", getName());
				} else if (newState == SetUseState.SET) {
					logger.warn("Motor {} is now in 'Set' mode - this will cause moves to fail", getName());
				}
			}
			setUseMode = newState;
		} finally {
			setUseLock.writeLock().unlock();
		}
	}

	private void checkMotorIsInUseMode() throws MotorException {
		try {
			setUseLock.readLock().lock();
			if (setUseMode == SetUseState.SET) {
				throw new MotorException(getStatus(), String.format("Motor %s is in 'Set' mode - check the Set/Use PV in the motor's EDM screen", getName()));
			}
		} finally {
			setUseLock.readLock().unlock();
		}
	}

	private void mstaMonitorChanged(MonitorEvent mev) {
		try {
			DBR dbr = mev.getDBR();
			if (dbr.isDOUBLE()) {
				Double msta = ((DBR_Double) dbr).getDoubleValue()[0];
				MotorStatus status = getMotorStatusFromMSTAValue(msta.intValue());
				if ((status == MotorStatus.READY || status == MotorStatus.LOWER_LIMIT || status == MotorStatus.UPPER_LIMIT
						|| status == MotorStatus.FAULT)) {
					mstaStatus = status;
					moveEventQueue.addMoveCompleteEvent(EpicsMotor.this, status, STATUSCHANGE_REASON.NEWSTATUS);

				}
				homed = isHomedFromMSTAValue(msta);
			} else {
				logger.error(".RBV should return DOUBLE type value. Instead returned {} type.", dbr.getType());
			}
		} catch (Exception ex) {
			logger.error("{} - Error in MSTA monitor", getName(), ex);
		}
	}

	/**
	 * update upper soft limit when and if it changes in EPICS.
	 */
	private void hlmMonitorChanged(MonitorEvent mev) {
		final DBR dbr = mev.getDBR();
		if (dbr.isFLOAT()) {
			setMaxPositionFromListener(new Float(((DBR_Float) dbr).getFloatValue()[0]).doubleValue());
		} else if (dbr.isDOUBLE()) {
			setMaxPositionFromListener(((DBR_Double) dbr).getDoubleValue()[0]);
		} else {
			logger.error("Illegal .HLM value. Expecting float or double, got {}", dbr.getType());
		}
	}

	private void setMinPositionFromListener(double minPosition) {
		this.minPosition = minPosition;
		notifyIObservers(MotorProperty.LOWLIMIT, new Double(minPosition));
	}

	private void setMaxPositionFromListener(double maxPosition) {
		this.maxPosition = maxPosition;
		notifyIObservers(MotorProperty.HIGHLIMIT, new Double(maxPosition));
	}

	/**
	 * updates the lower soft limit when and if it changes in EPICS.
	 */
	private void llmMonitorChanged(MonitorEvent mev) {
		final DBR dbr = mev.getDBR();
		if (dbr.isFLOAT()) {
			setMinPositionFromListener(new Float(((DBR_Float) dbr).getFloatValue()[0]).doubleValue());
		} else if (dbr.isDOUBLE()) {
			setMinPositionFromListener(((DBR_Double) dbr).getDoubleValue()[0]);
		} else {
			logger.error("Illegal .LLM value.");
		}
	}

	/**
	 * update upper dial limit when and if it changes in EPICS.
	 */
	private void dhlmMonitorChanged(MonitorEvent mev) {
		final DBR dbr = mev.getDBR();
		if (dbr.isDOUBLE()) {
			dialHighLimit = ((DBR_Double) dbr).getDoubleValue()[0];
		} else {
			logger.error("Illegal .DHLM value.");
		}
	}

	/**
	 * update lower dial limit when and if it changes in EPICS.
	 */
	private void dllmMonitorChanged(MonitorEvent mev) {
		final DBR dbr = mev.getDBR();
		if (dbr.isDOUBLE()) {
			dialLowLimit = ((DBR_Double) dbr).getDoubleValue()[0];
		} else {
			logger.error("Illegal .DLLM value.");
		}
	}

	/**
	 * update upper dial alarm when and if it changes in EPICS.
	 */
	private void hlsMonitorChanged(MonitorEvent mev) {
		final DBR dbr = mev.getDBR();
		if (dbr.isSHORT()) {
			final short value = ((DBR_Short) dbr).getShortValue()[0];
			if (value == 1) {
				setMotorStatus(MotorStatus.UPPER_LIMIT);
			}
		} else {
			logger.error("Expecting Int type but got {} type.", dbr.getType());
		}
	}

	/**
	 * update lower dial alarm when and if it changes in EPICS.
	 */
	private void llsMonitorChanged(MonitorEvent mev) {
		final DBR dbr = mev.getDBR();
		if (dbr.isSHORT()) {
			final short value = ((DBR_Short) dbr).getShortValue()[0];
			if (value == 1) {
				setMotorStatus(MotorStatus.LOWER_LIMIT);
			}
		} else {
			logger.error("Expecting Int type but got {} type.", dbr.getType());
		}
	}

	/**
	 * updates limit violation status from EPICS.
	 */
	private void lvioMonitorChanged(MonitorEvent mev) {
		int value = -1;
		final DBR dbr = mev.getDBR();
		if (dbr.isSHORT()) {
			value = ((DBR_Short) dbr).getShortValue()[0];
		} else {
			logger.error("Expecting Int type but got {} type.", dbr.getType());
		}

		if (value == 1) {
			logger.warn("EPICS motor {} raises Limit Violation.", getName());
		}
	}

	/**
	 * This function defines the call back handler for an asynchronous motor move request. It sets motor status to FAULT if
	 * put failed, or target is missed when missing target is not permitted. It also checks the motor access status, if
	 * its access is DISABLED, it will suspend the current scan or script before setting motor status to READY in order
	 * to prevent sending next point request to a already disabled motor. It notifies all its observers of these motor
	 * status (critical to GDA DOF locking release). This function is designed to support both scan and GUI driven
	 * processes.
	 */
	private void putCompleted(PutEvent ev) {
		MotorStatus newStatus = MotorStatus.READY;
		try {
			logger.debug("{}: callback received >>>", getName());
			if (isCallbackWait()) { // delay is needed to DCM energy update in EPICS
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// Reset interrupt status
					Thread.currentThread().interrupt();

					logger.error("Interrupted waiting for callback", e);
				}
			}

			if (ev.getStatus() != CAStatus.NORMAL) {
				logger.error("Put failed. Channel {} : Status {}", ((Channel) ev.getSource()).getName(), ev.getStatus());
				newStatus = MotorStatus.FAULT;
			} else {
				// if access is disabled we must pause before set motor status
				// to READY to prevent sending the next scan point request. This
				// also ensure the current point reading complete before pausing
				if (acs == AccessControl.Status.DISABLED) {
					if (JythonServerFacade.getInstance().getScanStatus() == JythonStatus.RUNNING) {
						JythonServerFacade.getInstance().pauseCurrentScan();
						JythonServerFacade.getInstance().print("current scan paused after motor " + getName() + " is disabled.");
					}
					if (JythonServerFacade.getInstance().getScriptStatus() == JythonStatus.RUNNING) {
						JythonServerFacade.getInstance().pauseCurrentScript();
						JythonServerFacade.getInstance().print("current script paused after motor " + getName() + " is disabled.");
					}
				}
				if (missedTargetAction != MissedTargetLevel.IGNORE) {
					double deadband = getRetryDeadband();
					double current = getPosition();
					if (deadband > 0 && !Double.isNaN(deadband)) {
						if (abs(targetPosition - current) > deadband) {
							logger.error("{} : target requested is missed (target: {}, actual: {}, deadband: {}). Report to Controls Engineer", getName(),
									targetPosition, currentPosition, retryDeadband);
							if (missedTargetAction == MissedTargetLevel.FAULT) {
								newStatus = MotorStatus.FAULT;
							}
						}
					} else {
						logger.warn("{} motor's retry deadband is {}. Motor may miss its target.", getName(), retryDeadband);
					}
				}
				logger.trace("{} - At end of move: target={}, position={}, deadband={}", getName(), targetPosition, currentPosition, retryDeadband);
			}

			if (status == Status.NO_ALARM && severity == Severity.NO_ALARM) {
				moveEventQueue.addMoveCompleteEvent(EpicsMotor.this, newStatus, STATUSCHANGE_REASON.CAPUT_MOVECOMPLETE);
			} else {
				// if Alarmed, check and report MSTA status
				moveEventQueue.addMoveCompleteEvent(EpicsMotor.this, null, STATUSCHANGE_REASON.CAPUT_MOVECOMPLETE_IN_ERROR);
			}

		} catch (Exception ex) {
			logger.error("Error in putCompleted for {}", getName(), ex);
		}
	}

	/**
	 * gets EPICS access control name.
	 *
	 * @return name of the access control.
	 */
	public String getAccessControlName() {
		return accessControlName;
	}

	/**
	 * sets the EPICS access control name.
	 */
	public void setAccessControlName(String accessControlName) {
		this.accessControlName = accessControlName;
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if (theObserved instanceof AccessControl && theObserved == accessControl && !accessControl.isDefaultAction()) {
			// set the access control flag of this object
			this.acs = (AccessControl.Status) changeCode;
			if ((AccessControl.Status) changeCode == AccessControl.Status.ENABLED) {
				logger.info("Beamline control of the device {} is enabled.", getName());
				if (JythonServerFacade.getInstance().getScanStatus() == JythonStatus.PAUSED) {
					JythonServerFacade.getInstance().resumeCurrentScan();
					JythonServerFacade.getInstance().print("current scan resumed after motor: " + getName() + " is enabled.");
				}
				if (JythonServerFacade.getInstance().getScriptStatus() == JythonStatus.PAUSED) {
					JythonServerFacade.getInstance().resumeCurrentScript();
					JythonServerFacade.getInstance().print("current script resumed after motor: " + getName() + " is enabled.");
				}
			} else if ((AccessControl.Status) changeCode == AccessControl.Status.DISABLED) {
				logger.warn("Beamline control of the device {} is disabled.", getName());
			}
		}
		notifyIObservers(theObserved, changeCode);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "-" + getName();
	}

	public boolean isCallbackWait() {
		return callbackWait;
	}

	public void setCallbackWait(boolean callbackDelay) {
		this.callbackWait = callbackDelay;
	}

	public void setAssertHomedBeforeMoving(boolean assertHomedBeforeMoving) {
		this.assertHomedBeforeMoving = assertHomedBeforeMoving;
	}

	public boolean isAssertHomedBeforeMoving() {
		return assertHomedBeforeMoving;
	}

	private void setMotorStatus(MotorStatus motorStatus) {
		synchronized (_motorStatusMonitor) {
			this._motorStatus = motorStatus;
			this._motorStatusMonitor.notifyAll();
		}
	}

	@Override
	public MotorStatus waitWhileStatusBusy() throws InterruptedException {
		synchronized (_motorStatusMonitor) {
			while (_motorStatus == MotorStatus.BUSY) {
				_motorStatusMonitor.wait();
			}
			return _motorStatus;
		}
	}

	@Override
	public void reconfigure() throws FactoryException {
		if (!isConfigured()) {
			configure();
		}
	}

	public void setMissedTargetLevel(MissedTargetLevel level) {
		missedTargetAction = level;
	}

	private static int maskOfBitAt(short bitPosition) {
		if(bitPosition < 0 || bitPosition > 30) throw new IllegalArgumentException("Integer bit position limits exceeded: " + Short.toString(bitPosition));
		return 1 << bitPosition;
	}

	private static boolean hasTrueBitAtAnyPosition(int binaryValue, short ...bitLocations) {
		for(short location : bitLocations) {
			if(hasTrueBitAtPosition(binaryValue, location)) return true;
		}
		return false;
	}

	private static boolean hasTrueBitAtPosition(int binaryValue, short bitLocation) {
		int maskedOffBit = binaryValue & maskOfBitAt(bitLocation);
		return maskedOffBit > 0;
	}

	@Override
	public String getControllerRecordName() {
		return getPvName();
	}
}
