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

package gda.jython.scriptcontroller;

import java.util.ArrayList;
import java.util.List;

import gda.factory.FactoryException;
import gda.factory.FindableConfigurableBase;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * Concrete implementation of Scriptcontroller. The attributes defined through the script controller interface are such
 * that this class should not need to be sub-classed.
 */
@ServiceInterface(Scriptcontroller.class)
public class ScriptControllerBase extends FindableConfigurableBase implements Scriptcontroller {

	private String commandName;
	private String commandFormat = "";
	private String importCommand = null;
	private String parametersName = "";
	private List<Object> parameters = new ArrayList<>(4);

	private final ObservableComponent observable = new ObservableComponent();

	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			reconfigure();
		}
	}

	@Override
	public void reconfigure() throws FactoryException {
		doImport();
		parameters.clear();
		setConfigured(true);
	}

	private void doImport() {
		// load the script into the namespace
		this.runCommand(importCommand);
	}

	@Override
	public String getCommand() {
		String cmd = commandName;
		if (null != commandFormat && !commandFormat.isEmpty()) {
			if (!parametersName.isEmpty()) {
				parameters = readParameters(parametersName);
			}
			cmd = String.format(commandFormat, parameters.toArray(new Object[parameters.size()]));
		}
		return cmd;
	}

	@Override
	public void setCommand(String scriptName) {
		this.commandName = scriptName;
	}

	@Override
	public String getParametersName() {
		return parametersName;
	}

	@Override
	public void setParametersName(String parametersName) {
		this.parametersName = parametersName;
	}

	@Override
	public void update(@SuppressWarnings("unused") Object o, Object arg) {
		// suppress tag as this method used in Jython scripts
		observable.notifyIObservers(this, arg);
	}

	@Override
	public String getImportCommand() {
		return importCommand;
	}

	@Override
	public void setImportCommand(String command) {
		this.importCommand = command;
	}

	@Override
	public void addIObserver(IObserver observer) {
		observable.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observable.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observable.deleteIObservers();
	}

	@Override
	public void notifyIObservers(Object source, Object event) {
		observable.notifyIObservers(source, event);
	}

	public String getCommandFormat() {
		return commandFormat;
	}

	public void setCommandFormat(String format) {
		this.commandFormat = format;
	}

	@Override
	public void addParameter(Object param) {
		parameters.add(param);
	}

	/**
	 * Derive a parameter list from a given key
	 * Defines three ways to specify parameters:
	 * 1. If the key does not locate a findable object, the key string is the parameter for the command
	 * 2. If the key-findable object is a list, treat as list of parameters for the command
	 * 3. If the key-findable object is not a list, the object is a parameter usable by the scripted command
	 * @param key
	 * @return parameters as a list of Objects
	 */
	public List<Object> readParameters(String key) {
		List<Object> paramList = new ArrayList<>();
		if (null != key && !key.isEmpty()) {
			Object provider = Finder.getInstance().find(key);
			if (provider instanceof List<?>) {
				paramList = new ArrayList<>((List<?>) provider);
			} else {
				if (null == provider) {
					paramList.add(key);
				} else {
					paramList.add(provider);
				}
			}
		}
		return paramList;
	}

	public void run() {
		// run the script in the namespace
		this.runCommand(this.getCommand());
	}

	private void runCommand(String commandToRun) {
		if (null != commandToRun && !commandToRun.isEmpty()) {
			InterfaceProvider.getCommandRunner().runCommand(commandToRun);
		}
	}
}
