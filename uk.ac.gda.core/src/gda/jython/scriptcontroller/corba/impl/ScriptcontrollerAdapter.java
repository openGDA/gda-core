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

package gda.jython.scriptcontroller.corba.impl;

import gda.device.DeviceException;
import gda.device.corba.CorbaDeviceException;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.corba.CorbaFactoryException;
import gda.factory.corba.util.EventService;
import gda.factory.corba.util.EventSubscriber;
import gda.factory.corba.util.NameFilter;
import gda.factory.corba.util.NetService;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.jython.scriptcontroller.corba.CorbaScriptController;
import gda.jython.scriptcontroller.corba.CorbaScriptControllerHelper;
import gda.observable.IIsBeingObserved;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.scan.ScanDataPointClient;
import gda.scan.ScanDataPointVar;

import java.util.Enumeration;
import java.util.Vector;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A client side implementation of the adapter pattern for the ScriptController class
 */
public class ScriptcontrollerAdapter implements Findable, Scriptcontroller, EventSubscriber, IObservable,
		IIsBeingObserved

{

	private static final Logger logger = LoggerFactory.getLogger(ScriptcontrollerAdapter.class);

	private final Vector<IObserver> myIObservers = new Vector<IObserver>();

	CorbaScriptController controller;

	NetService netService;

	String name;

	IObserver terminal = null;

	/**
	 * Create client side interface to the CORBA package.
	 *
	 * @param obj
	 *            the CORBA object
	 * @param name
	 *            the name of the object
	 * @param netService
	 *            the CORBA naming service
	 */
	public ScriptcontrollerAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		controller = CorbaScriptControllerHelper.narrow(obj);
		this.netService = netService;
		this.name = name;

		// subscribe to events coming over CORBA from the impl
		EventService.getInstance().subscribe(this, new NameFilter(name, this));
	}

	@Override
	public void inform(Object obj) {
		try {
			if (obj instanceof ScanDataPointVar) {
				obj = ScanDataPointClient.convertToken((ScanDataPointVar)obj);
			}
			notifyIObservers(this, obj);
			//logger.debug("ScriptcontrollerAdapter " + name + ": Notified observers");
		} catch (DeviceException e) {
			logger.error("Could not convert {} to IScanDataPoint", obj, e);
		}
	}

	@Override
	public void reconfigure() throws FactoryException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				controller.reconfigure();
				return;
			} catch (CorbaFactoryException ex) {
				throw new FactoryException(ex.message);
			} catch (COMM_FAILURE cf) {
				controller = CorbaScriptControllerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				controller = CorbaScriptControllerHelper.narrow(netService.reconnect(name));
			}
		}
		throw new FactoryException("Communication failure: retry failed");
	}

	@Override
	public void setName(String name) {
		// see bugzilla bug #443
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getCommand() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return controller.getCommand();
			} catch (COMM_FAILURE cf) {
				controller = CorbaScriptControllerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				controller = CorbaScriptControllerHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				logger.error("Could not getCommand", ex);
			}
		}
		return "";
	}

	@Override
	public void setCommand(String scriptName) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				controller.setCommand(scriptName);
				return;
			} catch (COMM_FAILURE cf) {
				controller = CorbaScriptControllerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				controller = CorbaScriptControllerHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				logger.error("Could not set command to {}", scriptName, ex);
			}
		}
	}

	@Override
	public String getParametersName() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return controller.getParametersName();
			} catch (COMM_FAILURE cf) {
				controller = CorbaScriptControllerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				controller = CorbaScriptControllerHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				logger.error("Could not get parameters name", ex);
			}
		}
		return "";
	}

	@Override
	public void setParametersName(String parametersName) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				controller.setParametersName(parametersName);
				return;
			} catch (COMM_FAILURE cf) {
				controller = CorbaScriptControllerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				controller = CorbaScriptControllerHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				logger.error("Could not set parameters name to {}", parametersName, ex);
			}
		}
	}

	@Override
	public String getImportCommand() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return controller.getImportCommand();
			} catch (COMM_FAILURE cf) {
				controller = CorbaScriptControllerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				controller = CorbaScriptControllerHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				logger.error("Could not get import command", ex);
			}
		}
		return "";
	}

	@Override
	public void setImportCommand(String command) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				controller.setImportCommand(command);
				return;
			} catch (COMM_FAILURE cf) {
				controller = CorbaScriptControllerHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				controller = CorbaScriptControllerHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				logger.error("Could not set import command to {}", command, ex);
			}
		}
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		if (!myIObservers.contains(anIObserver))
			myIObservers.addElement(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		myIObservers.removeElement(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		myIObservers.removeAllElements();
	}

	/**
	 * Notify all observers on the list of the requested change.
	 *
	 * @param theObserved
	 *            the observed component
	 * @param changeCode
	 *            the data requested by the observer.
	 */
	public void notifyIObservers(Object theObserved, Object changeCode) {
		// This must be an enumeration as Iterators allow replacement of
		// elements
		// which will lead to ConcurrentModificationException. You can't replace
		// this with the Java 1.5 foreach loop.
		Enumeration<IObserver> myIObserversList = myIObservers.elements();
		while (myIObserversList.hasMoreElements()) {
			IObserver anIObserver = myIObserversList.nextElement();
			anIObserver.update(theObserved, changeCode);
		}
	}

	@Override
	public boolean IsBeingObserved() {
		// TODO Auto-generated method stub
		return myIObservers.size() > 0;
	}

}
