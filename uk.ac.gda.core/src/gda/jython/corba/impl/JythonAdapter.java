/*-
 * Copyright © 2014 Diamond Light Source Ltd., Science and Technology
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

package gda.jython.corba.impl;

import gda.device.DeviceException;
import gda.device.corba.CorbaDeviceException;
import gda.factory.corba.util.EventService;
import gda.factory.corba.util.EventSubscriber;
import gda.factory.corba.util.NameFilter;
import gda.factory.corba.util.NetService;
import gda.jython.Jython;
import gda.jython.UserMessage;
import gda.jython.batoncontrol.ClientDetails;
import gda.jython.commandinfo.CommandThreadEvent;
import gda.jython.commandinfo.ICommandThreadInfo;
import gda.jython.completion.AutoCompletion;
import gda.jython.corba.CorbaJython;
import gda.jython.corba.CorbaJythonHelper;
import gda.observable.IObserver;
import gda.scan.ScanDataPointClient;
import gda.scan.ScanDataPointVar;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.omg.CORBA.Any;
import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;
import org.python.core.PyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * A client side implementation of the adapter pattern for the Jython class
 */
public class JythonAdapter implements Jython, EventSubscriber {
	private static final Logger logger = LoggerFactory.getLogger(JythonAdapter.class);

	CorbaJython jythonServer;

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
	public JythonAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		jythonServer = CorbaJythonHelper.narrow(obj);
		this.netService = netService;
		this.name = name;

		// subscribe to events coming over CORBA from the impl
		EventService.getInstance().subscribe(this, new NameFilter(name, null));
	}

	@Override
	public void inform(Object obj) {
		try {
			/*
			 * As we only pass events to terminal do nothing if terminal is null. in fact we have not subscribed to
			 * events in the constructor if terminal is null so this really is belts and braces
			 */
			if (terminal == null)
				return;
			if (obj instanceof ScanDataPointVar) {
				obj = ScanDataPointClient.convertToken((ScanDataPointVar) obj);
			}
			terminal.update(this, obj);
		} catch (DeviceException ex) {
			logger.error("Could not update terminal {} of {}", terminal, obj, ex);
		}
	}

	// to fulfil the Findable interface
	@Override
	public void setName(String name) {
		// GDA-443
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String evaluateCommand(String command, String JSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return jythonServer.evaluateCommand(command, JSFIdentifier);
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (Exception ex) {
				//the command failed but ran successfully so do not rerun
				logger.error("Error evaluating command '" + command + "'",ex );
				break;
				// other exceptions will be recorded by JythonServer, so no need to duplicate. Only log comms exceptions
				// in this method.,otherwsie users get longer error messages and this may confuse
			}
		}
		return null;
	}

	@Override
	public void requestFinishEarly(String JSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				jythonServer.requestFinishEarly(JSFIdentifier);
				return;
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
	}

	@Override
	public boolean isFinishEarlyRequested() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return jythonServer.isFinishEarlyRequested();
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (Exception ex) {
				//the command failed but ran successfully so do not rerun
				logger.error("Error evaluating command 'isFinishEarlyRequested'",ex );
				break;
				// other exceptions will be recorded by JythonServer, so no need to duplicate. Only log comms exceptions
				// in this method.,otherwsie users get longer error messages and this may confuse
			}
		}
		return false;
	}

	@Override
	public void beamlineHalt(String JSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				jythonServer.beamlineHalt(JSFIdentifier);
				return;
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
	}

	@Override
	public void abortCommands(String JSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				jythonServer.abortCommands(JSFIdentifier);
				return;
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
	}

	@Override
	public void pauseCurrentScan(String JSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				jythonServer.pauseCurrentScan(JSFIdentifier);
				return;
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
	}

	@Override
	public void pauseCurrentScript(String JSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				jythonServer.pauseCurrentScript(JSFIdentifier);
				return;
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
	}

	@Override
	public void resumeCurrentScan(String JSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				jythonServer.resumeCurrentScan(JSFIdentifier);
				return;
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
	}

	@Override
	public void resumeCurrentScript(String JSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				jythonServer.resumeCurrentScript(JSFIdentifier);
				return;
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
	}

	@Override
	public void runCommand(String command, String JSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				jythonServer.runCommand(command, JSFIdentifier);
				return;
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
	}

	@Override
	public boolean runsource(String command, String JSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return jythonServer.runsource(command, JSFIdentifier);
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return false;
	}

	@Override
	public int addFacade(IObserver anIObserver, String JSFIdentifier, String hostName, String username, String fullname, String visitID) {
		terminal = anIObserver;
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				logger.info("JSFIdentifier = " + StringUtils.quote(JSFIdentifier));
				logger.info("hostName = " + StringUtils.quote(hostName));
				logger.info("username = " + StringUtils.quote(username));
				logger.info("fullname = " + StringUtils.quote(fullname));
				logger.info("visitID = " + StringUtils.quote(visitID));
				if (JSFIdentifier == null) {
					JSFIdentifier = "";
				}
				if (hostName == null) {
					hostName = "";
				}
				if (username == null) {
					username = "";
				}
				if (fullname == null) {
					fullname = "";
				}
				if (visitID == null) {
					visitID = "";
				}
				return jythonServer.addFacade(JSFIdentifier, hostName, username, fullname, visitID);
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to the object primarily when the server has
				// failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return 0;

	}

	@Override
	public int getScanStatus(String JSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return jythonServer.getScanStatus(JSFIdentifier);
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return -99;
	}

	@Override
	public int getScriptStatus(String JSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return jythonServer.getScriptStatus(JSFIdentifier);
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return -99;
	}

	@Override
	public void setScriptStatus(int status, String JSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				jythonServer.setScriptStatus(status, JSFIdentifier);
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return;
	}

	@Override
	public void placeInJythonNamespace(String objectName, Object obj, String JSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
				any.insert_Value((Serializable) obj);
				jythonServer.placeInJythonNamespace(objectName, any, JSFIdentifier);
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return;
	}

	@Override
	public Object getFromJythonNamespace(String objectName, String JSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				Any any = jythonServer.getFromJythonNamespace(objectName, JSFIdentifier);
				return any.extract_Value();
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				throw new RuntimeException("getFromJythonNamespace exception for object " + objectName, ct);
			} catch (CorbaDeviceException ex) {
				throw new RuntimeException("getFromJythonNamespace exception for object " + objectName, ex);
			} catch (Exception ex) {
				throw new RuntimeException("getFromJythonNamespace exception for object " + objectName, ex);
			}
		}
		return null;
	}

	@Override
	public String getRelease(String JSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return jythonServer.getRelease(JSFIdentifier);
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return null;
	}

	@Override
	public String getStartupOutput(String JSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return jythonServer.getStartupOutput(JSFIdentifier);
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return null;
	}

	@Override
	public void restartCurrentScan(String JSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				jythonServer.restartCurrentScan(JSFIdentifier);
				return;
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return;
	}

	@Override
	public CommandThreadEvent runScript(String command, String JSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				Any any = jythonServer.runScript(command, JSFIdentifier);
				return (CommandThreadEvent) any.extract_Value();
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return null;
	}

	@Override
	public void setRawInput(String theInput, String JSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				jythonServer.setRawInput(theInput, JSFIdentifier);
				return;
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return;

	}

	@Override
	public void addAliasedCommand(String commandName, String JSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				jythonServer.addAliasedCommand(commandName, JSFIdentifier);
				return;
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return;

	}

	@Override
	public void addAliasedVarargCommand(String commandName, String JSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				jythonServer.addAliasedVarargCommand(commandName, JSFIdentifier);
				return;
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return;
	}

	@Override
	public boolean amIBatonHolder(String myJSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return jythonServer.amIBatonHolder(myJSFIdentifier);
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return false;
	}

	@Override
	public void assignBaton(String myJSFIdentifier, int indexOfReciever) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				jythonServer.assignBaton(myJSFIdentifier, indexOfReciever);
				return;
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
	}

	@Override
	public ClientDetails getClientInformation(String myJSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				// get any from impl
				Any any = jythonServer.getClientInformation(myJSFIdentifier);
				ClientDetails details = (ClientDetails) any.extract_Value();
				return details;
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return null;
	}

	@Override
	public ClientDetails[] getOtherClientInformation(String myJSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				// get any from impl
				Any any = jythonServer.getOtherClientInformation(myJSFIdentifier);
				ClientDetails[] details = (ClientDetails[]) any.extract_Value();
				return details;
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return new ClientDetails[0];
	}

	@Override
	public ICommandThreadInfo[] getCommandThreadInfo() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				Any any = jythonServer.getCommandThreadInfo();
				ICommandThreadInfo[] infos = (ICommandThreadInfo[]) any.extract_Value();
				return infos;
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object primarily when the server has failed.
				break;
			} catch (Exception ex) {
				logger.error("Could not get command thread info", ex);
			}
		}
		return new ICommandThreadInfo[0];
	}

	@Override
	public boolean requestBaton(String uniqueIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return jythonServer.requestBaton(uniqueIdentifier);
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return false;
	}

	@Override
	public void returnBaton(String uniqueIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				jythonServer.returnBaton(uniqueIdentifier);
				return;
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
	}

	@Override
	public boolean isBatonHeld() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return jythonServer.isBatonHeld();
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return false;
	}

	@Override
	public void removeFacade(String uniqueFacadeName) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				jythonServer.removeFacade(uniqueFacadeName);
				return;
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
	}

	@Override
	public int getAuthorisationLevel(int indexOfClient) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return jythonServer.getAuthorisationLevel(indexOfClient);
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return 0;
	}

	@Override
	public void switchUser(String uniqueFacadeName, String username, String visitID) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				jythonServer.switchUser(uniqueFacadeName, username, visitID);
				return;
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
	}

	@Override
	public void sendMessage(String myJSFIdentifier, String message) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				jythonServer.sendMessage(myJSFIdentifier, message);
				return;
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
	}

	@Override
	public List<UserMessage> getMessageHistory(String myJSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				Any any = jythonServer.getMessageHistory(myJSFIdentifier);
				@SuppressWarnings("unchecked")
				List<UserMessage> messages = (List<UserMessage>) any.extract_Value();
				return messages;
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return null;
	}

	@Override
	public Vector<String> getAliasedCommands(String JSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String[] aliaslist = jythonServer.getAliasedCommand(JSFIdentifier);
				return convert2Vector(aliaslist);
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return null;
	}

	private Vector<String> convert2Vector(String[] aliaslist) {
		Vector<String> me = new Vector<String>();
		for (String each : aliaslist) {
			me.add(each);
		}
		return me;
	}

	@Override
	public Vector<String> getAliasedVarargCommands(String JSFIdentifier) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String[] aliaslist = jythonServer.getAliasedVarargCommand(JSFIdentifier);
				return convert2Vector(aliaslist);
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return null;
	}

	@Override
	public Map<String, Object> getAllFromJythonNamespace() throws DeviceException {
		throw new DeviceException("operation not supported remotely");
	}

	@Override
	public String locateScript(String scriptToRun) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return jythonServer.locateScript(scriptToRun);
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return null;
	}

	@Override
	public String getDefaultScriptProjectFolder() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return jythonServer.getDefaultScriptProjectFolder();
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return null;
	}

	@Override
	public List<String> getAllScriptProjectFolders() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				String[] folders = jythonServer.getAllScriptProjectFolders();
				return Arrays.asList(folders);
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return null;
	}

	@Override
	public String getProjectNameForPath(String path) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return jythonServer.getProjectNameForPath(path);
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return null;
	}

	@Override
	public boolean projectIsUserType(String path) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return jythonServer.projectIsUserType(path);
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return false;
	}

	@Override
	public boolean projectIsConfigType(String path) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return jythonServer.projectIsConfigType(path);
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return false;
	}

	@Override
	public boolean projectIsCoreType(String path) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return jythonServer.projectIsCoreType(path);
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				// throw new DeviceException(ex.message);
			}
		}
		return false;
	}

	@Override
	public AutoCompletion getCompletionsFor(String line, int posn) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				Any any = jythonServer.getCompletionsFor(line, posn);
				AutoCompletion ac = (AutoCompletion) any.extract_Value();
				return ac;
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object
				// primarily when the server has failed.
				break;
			} catch (CorbaDeviceException ex) {
				logger.warn("Not able to get AutoCompletion from Corba", ex);
			}
		}
		return AutoCompletion.noCompletions(line, posn);
	}

	@Override
	public void print(String text) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				jythonServer.print(text);
				return;
			} catch (COMM_FAILURE cf) {
				jythonServer = CorbaJythonHelper.narrow(netService.reconnect(name));
			} catch (org.omg.CORBA.TRANSIENT ct) {
				// This exception is thrown when the ORB failed to connect to
				// the object primarily when the server has failed.
				break;
			}
		}
	}

	@Override
	public PyObject eval(String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void exec(String s) {
		throw new UnsupportedOperationException();
	}

	/**
	 * This method is not available over CORBA and throws UnsupportedOperationException
	 */
	@Override
	public boolean runsource(String command, String JSFIdentifier, InputStream in) {
		throw new UnsupportedOperationException("It is not currently possible to specify stdin across CORBA");
	}
}
