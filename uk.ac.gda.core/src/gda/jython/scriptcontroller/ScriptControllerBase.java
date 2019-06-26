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

import gda.factory.FactoryException;
import gda.factory.FindableConfigurableBase;
import gda.jython.ICommandRunner;
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

	private String parametersName;

	private String importCommand;

	private final ObservableComponent observable = new ObservableComponent();

	@Override
	public void configure() throws FactoryException {
		doImport();
		setConfigured(true);
	}

	@Override
	public void reconfigure() throws FactoryException {
		doImport();
	}

	private void doImport() {
		// load the script into the namespace
		if (importCommand != null && !importCommand.equals("")) {
			final ICommandRunner server = InterfaceProvider.getCommandRunner();
			server.runCommand(importCommand);
		}
	}

	@Override
	public String getCommand() {
		return commandName;
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
}
