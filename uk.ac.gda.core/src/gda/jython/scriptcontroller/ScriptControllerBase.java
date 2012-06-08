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

import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Localizable;
import gda.jython.ICommandRunner;
import gda.jython.InterfaceProvider;
import gda.observable.ObservableComponent;

/**
 * Concrete implementation of Scriptcontroller. The attributes defined through the script controller interface are such
 * that this class should not need to be sub-classed.
 */
public class ScriptControllerBase extends ObservableComponent implements Scriptcontroller, Configurable, Localizable {

	String name;

	String commandName;

	String parametersName;

	String importCommand;

	ICommandRunner server;

	boolean local = false; // for distribution by default

	@Override
	public void configure() throws FactoryException {
		doImport();
	}
	
	@Override
	public void reconfigure() throws FactoryException {
		doImport();
	}
	
	private void doImport() {
		server = InterfaceProvider.getCommandRunner();

		// load the script into the namespace
		if (importCommand != null && !importCommand.equals("")) {
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
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Updates observers by distributing arg to them.
	 * 
	 * @param o
	 * @param arg
	 */
	public void update(@SuppressWarnings("unused") Object o, Object arg) {
		// suppress tag as this method used in Jython scripts
		notifyIObservers(this, arg);
	}

	@Override
	public String getImportCommand() {
		return importCommand;
	}

	@Override
	public void setImportCommand(String command) {
		this.importCommand = command;
	}

	/**
	 * Sets if this object is for local access only.
	 * 
	 * @param local
	 */
	@Override
	public void setLocal(boolean local) {
		this.local = local;
	}

	/**
	 * Return if this object is for local access only.
	 * 
	 * @return true if the object is local (not distributed)
	 */
	@Override
	public boolean isLocal() {
		return local;
	}

}
