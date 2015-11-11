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

import gda.device.corba.CorbaDeviceException;
import gda.factory.FactoryException;
import gda.factory.corba.CorbaFactoryException;
import gda.factory.corba.util.EventDispatcher;
import gda.factory.corba.util.EventService;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.jython.scriptcontroller.corba.CorbaScriptControllerPOA;
import gda.observable.IObserver;
import gda.scan.ScanDataPoint;
import gda.scan.ScanDataPointServer;

/**
 * A server side implementation for a distributed Scriptcontroller class
 */
public class ScriptcontrollerImpl extends CorbaScriptControllerPOA implements IObserver {

	private Scriptcontroller controller;

	private org.omg.PortableServer.POA poa;

	private EventDispatcher dispatcher;

	private String name;

	/**
	 * Create server side implementation to the CORBA package.
	 *
	 * @param controller
	 *            the Scriptcontroller implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public ScriptcontrollerImpl(Scriptcontroller controller, org.omg.PortableServer.POA poa) {
		this.controller = controller;
		this.poa = poa;

		name = controller.getName();

		dispatcher = EventService.getInstance().getEventDispatcher();

		controller.addIObserver(this); //FIXME: potential race condition
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public String getCommand() throws CorbaDeviceException {
		try {
			return controller.getCommand();
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void reconfigure() throws CorbaFactoryException {
		try {
			controller.reconfigure();
		} catch (FactoryException ex) {
			throw new CorbaFactoryException(ex.getMessage());
		}
	}

	@Override
	public void setCommand(String scriptName) throws CorbaDeviceException {
		try {
			controller.setCommand(scriptName);
			return;
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public String getParametersName() throws CorbaDeviceException {
		try {
			return controller.getParametersName();
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void setParametersName(String parametersName) throws CorbaDeviceException {
		try {
			controller.setParametersName(parametersName);
			return;
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public String getImportCommand() throws CorbaDeviceException {
		try {
			return controller.getImportCommand();
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void setImportCommand(String command) throws CorbaDeviceException {
		try {
			controller.setImportCommand(command);
			return;
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void update(java.lang.Object o, java.lang.Object arg) {
		if (arg instanceof ScanDataPoint) {
			arg = ScanDataPointServer.getToken((ScanDataPoint) arg);
		}
		dispatcher.publish(name, arg);
	}
}
