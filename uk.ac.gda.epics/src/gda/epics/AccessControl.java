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

package gda.epics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gov.aps.jca.CAException;
import gov.aps.jca.CAStatusException;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

/**
 * <p>
 * This is an EPICS helper class designed to address the problem of potential conflicts arising from the presence of two
 * control systems to the same device (e.g. ID), that is, GDA and EPICS, or more precisely beamline GDA control and MCR
 * EPICS control. Currently MCR system is the master controller that can block GDA access to the device. This class
 * provides an extensible capability for GDA objects to monitor the EPICS Access Control set by MCR for any EPICS Record
 * if present, so if the access state is changed, GDA would be notified thus to take a proper action to avoid potential
 * duel control race condition. When access to a device is disabled by MCR, GDA should pause or suspend its own access
 * request to the device, thus ensures at any time there will be only one controller in action.
 * </p>
 * <h1>Default Behaviour of this class</h1>
 * <p>
 * The default action is set to {@code false} in this class. That is, if user does not
 * explicitly set {@code <defaultAction>true</defaultAction>} in {@code <AccessControl>} element of its XML
 * configuration or change it by {@code setDefaultAction(true)} in code explicitly, this class will just change the
 * access status and the changed status is also notified to any observers. In this case any observer should provide its
 * own handler for access status change, otherwise no additional action will take place.
 * </p>
 * <p>
 * The default value for {@code defaultAction} is set to {@code false} so users can perform scan on other
 * devices except ID at any time by default, even during a synchrotron shutdown when ID is normally disabled.
 * </p>
 * <p>
 * If the {@code <defaultAction>} is set to {@code true} in its XML configuration, when access control changes
 * <ul>
 * <li>from {@code ENABLED} to {@code DISABLED}: GDA will suspend or pause the current running scan or script if any
 * exists</li>
 * <li>from {@code DISABLED} to {@code ENABLED}: GDA will resume the currently paused scan or script if any exists</li>
 * </ul>
 * and the changed status is also notified to any observers. In this case an observer are not required to provide any
 * handler, but if necessary, an observer can extend the default action above by adding additional behaviours in its
 * {@code update()} method.
 * </p>
 * <p>
 * During a synchrotron run, it is reasonable to set {@code defaultAction} to {@code true} to enable this
 * default actions, as during electron beam injection, ID gap are increased X-ray beam energy is lost or changed, so it
 * is reasonable for GDA to suspend any current scan until beam is restored.
 * </p>
 * <h1>Side effect of the default implementation</h1>
 * <p>
 * Although by default, this class will not pause any current scan, however, when the {@code <defaultAction>} is set to
 * {@code true}, change of access status to DISABLED will pause any current scan in the command-server, even it not related to
 * ID. (Need to look at this further)
 * </p>
 * <h1>Manual Overwrite</h1>
 * The {@code defaultAction} can be manually overwritten by {@code setDefaultAction(boolean)} on the access control
 * object.
 * <h1>Customisation of EPICS classes in GDA</h1>
 * <p>
 * To customise the behaviour on the access control change, users need
 * <ul>
 * <li>first disable the default actions by set {@code defaultAction} to {@code false},</li>
 * <li>then register itself as observer of the {@code AccessControl} object and providing its own custom implementation
 * of the event handling.</li>
 * </ul>
 * <p>The XML configuration will look like below, (using motor as example) <br>
 * <br>
 * &nbsp&nbsp&nbsp&nbsp{@code <AccessControl>}<br>
 * &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp {@code <name>idac</name>}<br>
 * &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp &nbsp&nbsp&nbsp&nbsp{@code <accessControlPvName>BL11I-MO-SERVC-01:IDBLENA</accessControlPvName>}<br>
 * &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp {@code <defaultAction>false</defaultAction>}<br>
 * &nbsp&nbsp&nbsp&nbsp{@code </AccessControl>}<br>
 * &nbsp&nbsp&nbsp&nbsp{@code .....}<br>
 * &nbsp&nbsp&nbsp&nbsp{@code .....}<br>
 * &nbsp&nbsp&nbsp&nbsp{@code <EpicsMotor>}<br>
 * &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp {@code <name>idgap</name>}<br>
 * &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp {@code <deviceName>ID.GAP</deviceName>}<br>
 * &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp {@code <accessControlName>idac</accessControlName>}<br>
 * &nbsp&nbsp&nbsp&nbsp{@code </EpicsMotor>}<br>
 * <br>
 * For the java implementation that use this pluggable feature or functionality, please see
 * {@link gda.device.motor.EpicsMotor#update(Object, Object)} and {@link gda.device.motor.EpicsMotor#configure()}
 * </p>
 * <p>
 * <h2>Additional changes required to customisation</h2>
 * Of course the GDASchema.xsd need to be changed accordingly, i.e. add {@code <accessControlName>} as an optional
 * element. And also add {@code <field name="accessControlName" type="string">} to the mapping.xml.
 * </p>
 */
public class AccessControl implements Configurable, Findable, IObservable, InitializationListener {
	/**
	 * status enum
	 */
	public enum Status {
		/**
		 * disabled Enum
		 */
		DISABLED,
		/**
		 * enabled enum
		 */
		ENABLED
	}

	private String name;

	private ObservableComponent observableComponent = new ObservableComponent();

	private static final Logger logger = LoggerFactory.getLogger(AccessControl.class);

	private boolean configured = false;

	private String accessControlPvName;

	private Channel blctrl;

	private Status acStatus;

	private boolean defaultAction = false;

	private int enableValue = 0;
	private int disableValue = 1;

	/**
	 * EPICS controller
	 */
	private EpicsController controller;

	/**
	 * EPICS Channel Manager
	 */
	private EpicsChannelManager channelManager;

	/**
	 * An EPICS Monitor for DISABLED field
	 */
	private AccessControlListener aclistener;

	/**
	 * Constructor
	 */
	public AccessControl() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		aclistener = new AccessControlListener();
	}

	/**
	 * configure the object
	 *
	 * @exception FactoryException
	 */
	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			if (getAccessControlPvName() != null) {
				createChannelAccess(accessControlPvName);
				channelManager.tryInitialize(100);
			} else {
				throw new FactoryException("Can not find the access control PV name.");
			}
			configured = true;
		}
	}

	/**
	 * Creates EPICS Channel Access Objects
	 *
	 * @param pvName
	 * @throws FactoryException
	 */
	private void createChannelAccess(String pvName) throws FactoryException {
		try {
			blctrl = channelManager.createChannel(pvName, aclistener, false);

			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();
		} catch (Throwable th) {
			throw new FactoryException("failed to connect to the channel " + pvName, th);
		}
	}

	/**
	 * attach a Monitor to access control PV, users must provide its own event handler for process the Monitor event
	 * properly.
	 *
	 * @param ml
	 * @throws IllegalStateException
	 * @throws CAException
	 */
	public void addMonitor(MonitorListener ml) throws IllegalStateException, CAException {
		blctrl.addMonitor(Monitor.VALUE, ml);
	}

	/**
	 * gets the EPICS access control state.
	 *
	 * @return the EPICS access control state.
	 * @throws TimeoutException
	 * @throws CAException
	 * @throws InterruptedException
	 */
	public Status getAccessControlState() throws TimeoutException, CAException, InterruptedException {
		int value = controller.cagetInt(blctrl);
		if (value == 0) {
			return Status.ENABLED;
		} else if (value == 1) {
			return Status.DISABLED;
		} else {
			logger.error("Unknown Access Control Status " + value);
		}
		return Status.ENABLED;
	}

	/**
	 * implement initialisation after successful connections by polling EPICS PVs at the start once only.
	 * @throws InterruptedException
	 */
	@Override
	public void initializationCompleted() throws InterruptedException {
		try {
			// initialise the access control status at start up.
			acStatus = getAccessControlState();
			logger.debug("Update Access Control status to " + acStatus + " during initialisation.");
		} catch (TimeoutException e) {
			logger.warn("Failed to initialise Access Control Status for " + getName(), e);
		} catch (CAException e) {
			logger.warn("Failed to initialise Access Control Status for " + getName(), e);
		}
		logger.info("Beamline/MCR access control - " + getName() + " initialsed.");

	}

	/**
	 * Update access control status when it changes and notify all IObservers of this status change. The default actions
	 * on access control changes are
	 * <li> from ENABLED to DISABLED: suspend or pause the current running scan or script if any exists</li>
	 * <li>from DISABLED to ENABLED: resume the currently paused scan or script if any exists</li>
	 */
	public class AccessControlListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent mev) {
			Status lastAccessControlStatus = acStatus;
			int value = -1;
			DBR dbr = mev.getDBR();
			if (dbr != null) {
				try {
					dbr = dbr.convert(DBRType.INT);
				} catch (CAStatusException e) {
					logger.error("Conversion DBR to DBRType.INT failed. ", e);
				}
				value = ((DBR_Int) dbr).getIntValue()[0];
			} else {
				logger.error("Error: DBR returned by Monitor Event is NULL.");
			}

			if (value == enableValue) {
				logger.info("Beamline control of the device " + blctrl.getName() + " is enabled.");
				acStatus = Status.ENABLED;
				if (defaultAction) {
					if (JythonServerFacade.getInstance().getScanStatus() == Jython.PAUSED) {
						JythonServerFacade.getInstance().resumeCurrentScan();
						JythonServerFacade.getInstance().print("current scan resumed as " + getName() + " is enabled.");
					}
					if (JythonServerFacade.getInstance().getScriptStatus() == Jython.PAUSED) {
						JythonServerFacade.getInstance().resumeCurrentScript();
						JythonServerFacade.getInstance().print(
								"current running script resumed as " + getName() + " is enabled.");
					}
				}
			}
			else if (value == disableValue) {
				logger.warn("Beamline control of the device " + blctrl.getName() + " is disabled.");
				acStatus = Status.DISABLED;
				if (defaultAction) {
					if (JythonServerFacade.getInstance().getScanStatus() == Jython.RUNNING) {
						JythonServerFacade.getInstance().pauseCurrentScan();
						JythonServerFacade.getInstance().print("current scan paused as " + getName() + " is disabled.");
					}
					if (JythonServerFacade.getInstance().getScriptStatus() == Jython.RUNNING) {
						JythonServerFacade.getInstance().pauseCurrentScript();
						JythonServerFacade.getInstance().print(
								"current running script paused as " + getName() + " is disabled.");
					}
				}
			}
			else {
				logger.error("Error: illegal access status " + ((dbr != null) ? dbr.getValue() : "null")
						+ " is returned.");
			}

			if (!defaultAction)
				notifyObservers(lastAccessControlStatus, acStatus);
		}
	}

	private void notifyObservers(Status lastAccessControlStatus, Status acStatus) {
		if (lastAccessControlStatus != acStatus)
			notifyIObservers(this, acStatus);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;

	}

	/**
	 * Add an object to this objects's list of IObservers.
	 *
	 * @param anIObserver
	 *            object that implement IObserver and wishes to be notified by this object
	 */
	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);

	}

	/**
	 * Delete an object from this objects's list of IObservers.
	 *
	 * @param anIObserver
	 *            object that implement IObserver and wishes to be notified by this object
	 */
	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);

	}

	/**
	 * delete all IObservers from list of observing objects
	 */
	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();

	}

	/**
	 * Notify all observers on the list of the requested change.
	 *
	 * @param theObserved
	 *            the observed component
	 * @param theArgument
	 *            the data to be sent to the observer.
	 */
	public void notifyIObservers(Object theObserved, Object theArgument) {
		observableComponent.notifyIObservers(theObserved, theArgument);
	}

	/**
	 * get access control PV name.
	 *
	 * @return access control PV name.
	 */
	public String getAccessControlPvName() {
		return accessControlPvName;
	}

	/**
	 * sets the access control PV name, used by CASTOR.
	 *
	 * @param accessControlPvName
	 */
	public void setAccessControlPvName(String accessControlPvName) {
		this.accessControlPvName = accessControlPvName;
	}

	/**
	 * method for check the default action on or off.
	 *
	 * @return the default action
	 */
	public boolean isDefaultAction() {
		return defaultAction;
	}

	/**
	 * sets the default action switch, used by CASTOR and also provide a way to manually overwrite the value.
	 *
	 * @param defaultAction
	 */
	public void setDefaultAction(boolean defaultAction) {
		this.defaultAction = defaultAction;
	}

	public void setEnableValue(int value){
		this.enableValue = value;
	}
	public int getEnableValue(){
		return this.enableValue;
	}

	public void setDisableValue(int value){
		this.disableValue = value;
	}
	public int getDisableValue(){
		return this.disableValue;
	}

	public Status getStatus(){
		return this.acStatus;
	}
}
