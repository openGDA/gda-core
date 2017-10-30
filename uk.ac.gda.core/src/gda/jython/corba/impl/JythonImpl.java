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

package gda.jython.corba.impl;

import gda.device.corba.CorbaDeviceException;
import gda.factory.corba.util.EventDispatcher;
import gda.factory.corba.util.EventService;
import gda.jython.Jython;
import gda.jython.UserMessage;
import gda.jython.batoncontrol.ClientDetails;
import gda.jython.commandinfo.CommandThreadEvent;
import gda.jython.commandinfo.ICommandThreadInfo;
import gda.jython.completion.AutoCompletion;
import gda.jython.corba.CorbaJythonPOA;
import gda.observable.IObserver;
import gda.scan.ScanDataPoint;
import gda.scan.ScanDataPointServer;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

import org.omg.CORBA.Any;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server side implementation for classes using the Jython interface
 */
public class JythonImpl extends CorbaJythonPOA implements IObserver {
	private static final Logger logger = LoggerFactory.getLogger(JythonImpl.class);
	private Jython jythonServer;

	private org.omg.PortableServer.POA poa;

	private EventDispatcher dispatcher;

	private String name;

	/**
	 * Create server side implementation to the CORBA package.
	 *
	 * @param jythonServer
	 *            the JythonServer implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public JythonImpl(Jython jythonServer, org.omg.PortableServer.POA poa) {
		this.jythonServer = jythonServer;
		this.poa = poa;

		name = jythonServer.getName();

		dispatcher = EventService.getInstance().getEventDispatcher();
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	/**
	 * Get the implementation object
	 *
	 * @return the JythonServer implementation object
	 */
	public Jython _delegate() {
		return jythonServer;
	}

	/**
	 * Set the implementation object.
	 *
	 * @param jythonServer
	 *            set the JythonServer implementation object
	 */
	public void _delegate(Jython jythonServer) {
		this.jythonServer = jythonServer;
	}

	@Override
	public void update(java.lang.Object o, java.lang.Object arg) {
		if (arg instanceof ScanDataPoint) {
			arg = ScanDataPointServer.getToken((ScanDataPoint) arg);
		}
		dispatcher.publish(name, arg);
	}

	@Override
	public String evaluateCommand(String command, String JSFIdentifier) throws CorbaDeviceException {
		try {
			String val = jythonServer.evaluateCommand(command, JSFIdentifier);
			if (val == null) {
				throw new IllegalArgumentException("evaluateCommand failed for " + command
						+ ". See server log for details");
			}
			return val;
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void requestFinishEarly(String JSFIdentifier) throws CorbaDeviceException {
		try {
			jythonServer.requestFinishEarly(JSFIdentifier);
			return;
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public boolean isFinishEarlyRequested() throws CorbaDeviceException {
		try {
			return jythonServer.isFinishEarlyRequested();
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void beamlineHalt(String JSFIdentifier) throws CorbaDeviceException {
		try {
			jythonServer.beamlineHalt(JSFIdentifier);
			return;
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void abortCommands(String JSFIdentifier) throws CorbaDeviceException {
		try {
			jythonServer.abortCommands(JSFIdentifier);
			return;
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void pauseCurrentScan(String JSFIdentifier) throws CorbaDeviceException {
		try {
			jythonServer.pauseCurrentScan(JSFIdentifier);
			return;
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void pauseCurrentScript(String JSFIdentifier) throws CorbaDeviceException {
		try {
			jythonServer.pauseCurrentScript(JSFIdentifier);
			return;
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void resumeCurrentScan(String JSFIdentifier) throws CorbaDeviceException {
		try {
			jythonServer.resumeCurrentScan(JSFIdentifier);
			return;
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void resumeCurrentScript(String JSFIdentifier) throws CorbaDeviceException {
		try {
			jythonServer.resumeCurrentScript(JSFIdentifier);
			return;
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void runCommand(String command, String JSFIdentifier) throws CorbaDeviceException {
		try {
			jythonServer.runCommand(command, JSFIdentifier);
			return;
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public boolean runsource(String command, String JSFIdentifier) throws CorbaDeviceException {
		try {
			return jythonServer.runsource(command, JSFIdentifier);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public int addFacade(String facadeName, String hostName, String username, String fullname, String visitID)
			throws CorbaDeviceException {
		try {
			return jythonServer.addFacade(this, facadeName, hostName, username, fullname, visitID);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public int getScanStatus(String JSFIdentifier) throws CorbaDeviceException {
		try {
			return jythonServer.getScanStatus(JSFIdentifier);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public int getScriptStatus(String JSFIdentifier) throws CorbaDeviceException {
		try {
			return jythonServer.getScriptStatus(JSFIdentifier);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void setScriptStatus(int status, String JSFIdentifier) throws CorbaDeviceException {
		try {
			jythonServer.setScriptStatus(status, JSFIdentifier);
			return;
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void placeInJythonNamespace(String objectName, Any obj, String JSFIdentifier) throws CorbaDeviceException {
		try {
			jythonServer.placeInJythonNamespace(objectName, obj.extract_Value(), JSFIdentifier);
			return;
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public Any getFromJythonNamespace(String objectName, String JSFIdentifier) throws CorbaDeviceException {
		try {
			org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
			Object o = jythonServer.getFromJythonNamespace(objectName, JSFIdentifier);
			any.insert_Value((Serializable) o);
			return any;
		} catch (NullPointerException e) {
			logger.warn("NullPointerException calling getFromJythonNamespace for " + objectName);
			org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
			any.insert_Value(null);
			return any;
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public String getRelease(String JSFIdentifier) throws CorbaDeviceException {
		try {
			return jythonServer.getRelease(JSFIdentifier);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public String getStartupOutput(String JSFIdentifier) throws CorbaDeviceException {
		try {
			return jythonServer.getStartupOutput(JSFIdentifier);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void restartCurrentScan(String JSFIdentifier) throws CorbaDeviceException {
		try {
			jythonServer.restartCurrentScan(JSFIdentifier);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public Any runScript(String command, String JSFIdentifier) throws CorbaDeviceException {
		try {
			CommandThreadEvent result = jythonServer.runScript(command, JSFIdentifier);
			org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
			any.insert_Value(result);
			return any;
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void setRawInput(String arg0, String JSFIdentifier) throws CorbaDeviceException {
		try {
			jythonServer.setRawInput(arg0, JSFIdentifier);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void addAliasedCommand(String arg0, String JSFIdentifier) throws CorbaDeviceException {
		try {
			jythonServer.addAliasedCommand(arg0, JSFIdentifier);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void addAliasedVarargCommand(String arg0, String JSFIdentifier) throws CorbaDeviceException {
		try {
			jythonServer.addAliasedVarargCommand(arg0, JSFIdentifier);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public boolean amIBatonHolder(String arg0) throws CorbaDeviceException {
		try {
			return jythonServer.amIBatonHolder(arg0);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void assignBaton(String arg0, int arg1) throws CorbaDeviceException {
		try {
			jythonServer.assignBaton(arg0, arg1);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public Any getClientInformation(String arg0) throws CorbaDeviceException {
		try {
			// get array from jython server
			ClientDetails details = jythonServer.getClientInformation(arg0);
			org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
			any.insert_Value(details);
			// send out any
			return any;
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public Any getOtherClientInformation(String arg0) throws CorbaDeviceException {
		try {
			// get array from jython server
			ClientDetails[] details = jythonServer.getOtherClientInformation(arg0);
			org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
			any.insert_Value(details);
			// send out any
			return any;
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public boolean requestBaton(String arg0) throws CorbaDeviceException {
		try {
			return jythonServer.requestBaton(arg0);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void returnBaton(String arg0) throws CorbaDeviceException {
		try {
			jythonServer.returnBaton(arg0);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public boolean isBatonHeld() throws CorbaDeviceException {
		try {
			return jythonServer.isBatonHeld();
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void removeFacade(String arg0) throws CorbaDeviceException {
		try {
			jythonServer.removeFacade(arg0);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public int getAuthorisationLevel(int arg0) throws CorbaDeviceException {
		try {
			return jythonServer.getAuthorisationLevel(arg0);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void switchUser(String arg0, String arg1, String arg2) throws CorbaDeviceException {
		try {
			jythonServer.switchUser(arg0, arg1, arg2);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void sendMessage(String arg0, String arg1) throws CorbaDeviceException {
		try {
			jythonServer.sendMessage(arg0, arg1);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public Any getMessageHistory(String myJSFIdentifier) throws CorbaDeviceException {
		try {
			List<UserMessage> messages = jythonServer.getMessageHistory(myJSFIdentifier);
			org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
			any.insert_Value((Serializable) messages);
			return any;
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public String[] getAliasedCommand(String arg0) throws CorbaDeviceException {
		try {
			Vector<String> details = jythonServer.getAliasedCommands(arg0);
			String[] a=new String[details.size()];
			return details.toArray(a);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public String[] getAliasedVarargCommand(String arg0)
			throws CorbaDeviceException {
		try {
			Vector<String> details = jythonServer.getAliasedVarargCommands(arg0);
			String[] a=new String[details.size()];
			return details.toArray(a);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public String locateScript(String scriptToRun) throws CorbaDeviceException {
		try {
			return jythonServer.locateScript(scriptToRun);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public String getDefaultScriptProjectFolder() throws CorbaDeviceException {
		try {
			return jythonServer.getDefaultScriptProjectFolder();
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public String[] getAllScriptProjectFolders() throws CorbaDeviceException {
		try {
			return jythonServer.getAllScriptProjectFolders().toArray(new String[] {});
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public String getProjectNameForPath(String path) throws CorbaDeviceException {
		try {
			return jythonServer.getProjectNameForPath(path);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public boolean projectIsUserType(String path) throws CorbaDeviceException {
		try {
			return jythonServer.projectIsUserType(path);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public boolean projectIsConfigType(String path) throws CorbaDeviceException {
		try {
			return jythonServer.projectIsConfigType(path);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public boolean projectIsCoreType(String path) throws CorbaDeviceException {
		try {
			return jythonServer.projectIsCoreType(path);
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public Any getCommandThreadInfo() throws CorbaDeviceException {
		try {
			ICommandThreadInfo[] infos = jythonServer.getCommandThreadInfo();
			org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
			any.insert_Value(infos);
			return any;
		} catch (Exception de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public Any getCompletionsFor(final String line, final int posn) throws CorbaDeviceException {
		try {
			final AutoCompletion ac = jythonServer.getCompletionsFor(line, posn);
			final org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
			any.insert_Value(ac);
			return any;
		} catch (Exception de) {
			logger.error("Error sending AutoCompletion over Corba", de);
			throw new CorbaDeviceException(de.getMessage());
		}
	}
	@Override
	public void print(String text) {
		jythonServer.print(text);
	}
}
