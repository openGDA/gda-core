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

package uk.ac.diamond.daq.persistence;

import uk.ac.diamond.daq.persistence.LocalDatabase.LocalDatabaseException;
import uk.ac.diamond.daq.persistence.entity.ObjectShelf;

import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.slf4j.LoggerFactory;

/**
 * A class used to create and access 'shelves' filled with key/value pairs. The keys are arbitrary strings such as
 * 'mode', 'mode.euler', 'a.b.1' or 'a.b.2'. Any type of serializable object can be stored which includes all Jython
 * objects. The behaviour is roughly similar to that of python's shelf, except that this implementation stores the data
 * in a database and should be thread safe. The class's static methods can be used to list and delete shelves. An entire
 * shelf can be exported to a dictionary object and later imported. More than one shelf object can talk to the same
 * underlying shelf.
 * <p>
 * To work as advertised this depends on the java.properties described in uk.ac.diamond.daq.persistence.LocalDatabase to be
 * configured.
 */
public abstract class LocalObjectShelfManager {

	public static Writer DerbyLogStream = new LoggerWriter(LoggerFactory.getLogger("derby"));

	// Only one of these for all instances or static requests
	protected static EntityManagerFactory emf = null;

	static Boolean staticEmfCreated = false;

	private static Map<String, LocalObjectShelf> openShelves = new Hashtable<String, LocalObjectShelf>();

	// This can be overridden in extending classes. This will be added to the
	// implictely added to new shelf names and calling shelves on the class will
	// only show shelves with this prefix. This allows users of the extending
	// class
	// to have their own visible shelf area. WARNING: This is not failsafe,
	// someone
	// might choose the prefix as part of a shelfname and cause (minor)
	// confusion.

	/**
	 * @return Static method to return list of all shelves that might be opened.
	 * @throws LocalDatabaseException
	 */
	synchronized public static List<String> shelves() throws LocalDatabaseException {
		return shelves("");
	}

	/**
	 * Static method to return list of shelves with a given prefix that might be opened.
	 *
	 * @param shelfNamePrefix
	 * @return List of names
	 * @throws LocalDatabaseException
	 */
	@SuppressWarnings("unchecked")
	synchronized public static List<String> shelves(String shelfNamePrefix) throws LocalDatabaseException {
		List<String> toReturn = new ArrayList<String>();

		EntityManager em = staticBeginTransaction();
		Query q = em.createQuery("select c from ObjectShelf c");
		List<ObjectShelf> shelfList = q.getResultList();
		staticEndTransaction(em);

		// recover each shelf name
		for (ObjectShelf shelf : shelfList) {
			if (shelf.getName().startsWith(shelfNamePrefix)) {
				toReturn.add(shelf.getName().substring(shelfNamePrefix.length()));
			}
		}

		return toReturn;
	}

	/**
	 * A static method to check if a certain shelf exists in the store.
	 *
	 * @param name
	 * @return true if shelf exists.
	 * @throws LocalDatabaseException
	 */
	synchronized public static Boolean hasShelf(String name) throws LocalDatabaseException {
		return hasShelf("", name);
	}

	/**
	 * A static method to check if a certain shelf with a given prefix exists in the store.
	 *
	 * @param shelfNamePrefix
	 * @param name
	 * @return true if shelf exists.
	 * @throws LocalDatabaseException
	 */
	synchronized public static Boolean hasShelf(String shelfNamePrefix, String name) throws LocalDatabaseException {
		List<String> nameList = shelves();
		return nameList.contains(shelfNamePrefix + name);
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
		delShelf("", name);
	}

	/**
	 * A static method to remove a shelf with a prefix.
	 *
	 * @param shelfNamePrefix
	 * @param name
	 *            shelf to remove
	 * @throws LocalDatabaseException
	 * @throws Exception
	 */
	synchronized public static void delShelf(String shelfNamePrefix, String name) throws LocalDatabaseException,
			Exception {
		if (!hasShelf(shelfNamePrefix + name)) {
			throw new Exception("Could not delete shelf: no shelf named " + name + " exists");
		}
		EntityManager em = staticBeginTransaction();
		ObjectShelf shelf = em.find(ObjectShelf.class, shelfNamePrefix + name);
		em.remove(shelf);
		staticEndTransaction(em);
	}

	/**
	 * Static method to return an empty dictionary of the type expected by importValues().
	 *
	 * @return An empty dictionary
	 */
	public static Dictionary<String, Serializable> getDictionaryForImport() {
		return new Hashtable<String, Serializable>();
	}

	private static EntityManager staticBeginTransaction() throws LocalDatabaseException {
		// Setup connection
		EntityManager em;
		if (emf == null) {
			emf = LocalPersistence.createPersistenceEntityManagerFactory("JythonShelfPersistenceUnit");
			staticEmfCreated = true;
		}
		em = emf.createEntityManager();
		em.getTransaction().begin();
		return em;
	}

	private static void staticEndTransaction(EntityManager em) {
		// Shutdown connection
		em.getTransaction().commit();
		em.close();
		if (staticEmfCreated) {
			emf.close();
			emf = null;
			staticEmfCreated = false;
		}
	}

	/**
	 * Returns an instance of an ObjectShelfSinglton.
	 *
	 * @param shelfName
	 * @return the object shelf singleton
	 * @throws LocalDatabaseException
	 * @throws ObjectShelfException
	 */
	@Deprecated
	public static LocalObjectShelf getLocalObjectShelf(String shelfName) throws ObjectShelfException,
			LocalDatabaseException {

		return open(shelfName);
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
		return open("", shelfName);
	}

	/**
	 * Returns an instance of an LocalObjectShelf with a given prefix.
	 *
	 * @param shelfNamePrefix
	 * @param shelfName
	 * @return the object shelf singleton
	 * @throws LocalDatabaseException
	 * @throws ObjectShelfException
	 */
	public static LocalObjectShelf open(String shelfNamePrefix, String shelfName) throws ObjectShelfException,
			LocalDatabaseException {
		// Return the already opened shelf if it exists
		if (openShelves.containsKey(shelfNamePrefix + shelfName)) {
			return openShelves.get(shelfNamePrefix + shelfName);
		}

		// else Create a new shelf object (this may in turn create an underlying
		// shelf)
		openShelves.put(shelfNamePrefix + shelfName, new LocalObjectShelf(shelfNamePrefix, shelfName));
		return openShelves.get(shelfNamePrefix + shelfName);
	}

	LocalObjectShelfManager() {
		// Not to be directly instantiated
	}

}
