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

package gda.data.generic;

import gda.configuration.properties.LocalProperties;
import gda.factory.Configurable;
import gda.factory.Findable;
import gda.jython.JythonServerFacade;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * <b>Title: </b>Class to manage generic data structures in memory.
 * </p>
 * <p>
 * <b>Description: </b>This class lets the user create, destroy and access generic data structures. It is based on an
 * interface that is CORBAised, so an instance of this class should be resident on a CORBA server accessable by other
 * parts of the GDA (or simply the main GDA CORBA object server). This implementation of
 * <code>gda.data.DataManagerInterface</code> uses a <code>java.util.HashMap</code>. So it is really just a HashMap
 * wrapper.
 * </p>
 */

public class DataManager implements Configurable, Serializable, Findable, gda.data.DataManagerInterface {
	
	private static final Logger logger = LoggerFactory.getLogger(DataManager.class);

	private HashMap<String, IGenericData> mList = new HashMap<String, IGenericData>();

	private String mName = null;

	private String mDataManProperty = "gda.data.generic.dataman";

	/**
	 * Called by the object server after instantiation to setup the object. This places this object (the DataManager
	 * object) into the Jython namespace by using <code>JythonServerFacade</code>.
	 * 
	 * @see JythonServerFacade
	 */
	@Override
	public void configure() {
		// System.err.println(((Findable)this).getName() + ":" +
		// Finder.getInstance().find("stnBase.DataManager"));
		// Put this instance in the Jython namespace.
		String dataman = LocalProperties.get(mDataManProperty);
		if (dataman != null) {
			logger.debug("Putting the DataManager into the Jython namespace with name: " + dataman);
			JythonServerFacade.getInstance().placeInJythonNamespace(dataman, this);
		} else {
			JythonServerFacade.getInstance().placeInJythonNamespace("dataman", this);
		}
	}

	/**
	 * @return Returns the name.
	 */
	@Override
	public String getName() {
		return this.mName;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	@Override
	public void setName(String name) {
		this.mName = name;
	}

	/**
	 * Create a new <code>GenericData</code> object and give it a name.
	 * 
	 * @param name
	 *            The name of the object to create.
	 * @return A reference to the object.
	 */
	@Override
	public IGenericData create(String name) {
		logger.debug("Creating a new GenericData with name " + name + " in the DataManager.");
		mList.put(name, new GenericData());
		return mList.get(name);
	}

	/**
	 * Add an object to the list
	 * 
	 * @param name
	 *            The name of the object to add.
	 * @param data
	 *            The object.
	 */
	@Override
	public void add(String name, IGenericData data) {
		logger.debug("Adding a new GenericData to the DataManager, with name " + name);
		mList.put(name, data);
	}

	/**
	 * Remove this object from the list.
	 * 
	 * @param name
	 *            The name of the object to remove.
	 */
	@Override
	public void remove(String name) {
		logger.debug("Removing the GenericData with name " + name + " from the DataManager list.");
		mList.remove(name);
	}

	/**
	 * Return a Vector of strings containing the names of all the objects in the list.
	 * 
	 * @return The names of the objects.
	 */
	@Override
	public Vector<String> list() {
		Set<String> names = mList.keySet();
		Vector<String> vectorNames = new Vector<String>();
		for (String i : names) {
			vectorNames.add(i);
		}
		return vectorNames;
	}

	/**
	 * Returns a reference to the object of this name.
	 * 
	 * @param name
	 *            The name of the object.
	 * @return The object reference.
	 */
	@Override
	public IGenericData get(String name) {
		return mList.get(name);
	}

}
