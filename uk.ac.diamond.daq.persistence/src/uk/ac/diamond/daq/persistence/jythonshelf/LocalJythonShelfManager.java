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

package uk.ac.diamond.daq.persistence.jythonshelf;

import java.util.List;

import uk.ac.diamond.daq.persistence.jythonshelf.LocalDatabase.LocalDatabaseException;

/**
 *
 */
public abstract class LocalJythonShelfManager extends LocalObjectShelfManager {

	private static String prefix = "_jython_";

	private LocalJythonShelfManager() {
		// Not to be directly instantiated
	}

	/**
	 * @param name
	 * @return boolean
	 * @throws LocalDatabaseException
	 */
	synchronized public static Boolean hasShelf(String name) throws LocalDatabaseException {
		return hasShelf(prefix, name);
	}

	/**
	 * A static method to remove a shelf.
	 *
	 * @param name
	 *            shelf to remove
	 * @throws LocalDatabaseException
	 * @throws Exception
	 */
	synchronized public static void delShelf(String name) throws LocalDatabaseException, Exception {
		delShelf(prefix, name);
	}

	/**
	 * @return Static method to return list of all shelves that might be opened.
	 * @throws LocalDatabaseException
	 */
	synchronized public static List<String> shelves() throws LocalDatabaseException {
		return shelves(prefix);
	}

	/**
	 * Returns an instance of an LocalObjectShelf.
	 *
	 * @param shelfName
	 * @return the object shelf singleton
	 * @throws LocalDatabaseException
	 * @throws ObjectShelfException
	 */
	public static LocalObjectShelf open(String shelfName) throws ObjectShelfException, LocalDatabaseException {
		return open(prefix, shelfName);
	}
}