/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.persistence.jythonshelf.LocalDatabase.LocalDatabaseException;
import uk.ac.diamond.daq.persistence.jythonshelf.entity.ObjectShelf;
import uk.ac.diamond.daq.persistence.jythonshelf.entity.ObjectShelfEntry;

/**
 * LocalObjectShelf Class
 */
public class LocalObjectShelf {
	private static final Logger logger = LoggerFactory.getLogger(LocalObjectShelf.class);

	EntityManager em;

	private String shelfName;

	private String shelfNamePrefix;

	/**
	 * Creates an object to access a shelf entity and its entries. Will create the underlying shelf entity if it does
	 * not exists. More than one object may safely connect to the same underlying shelf.
	 *
	 * @param _shelfPrefix
	 * @param _shelfName
	 * @throws ObjectShelfException
	 * @throws LocalDatabaseException
	 */
	public LocalObjectShelf(String _shelfPrefix, String _shelfName) throws ObjectShelfException, LocalDatabaseException {
		// Create the persistence entity factory Persister if needed
		if (LocalObjectShelfManager.emf == null) {
			LocalObjectShelfManager.emf = LocalPersistence
					.createPersistenceEntityManagerFactory("JythonShelfPersistenceUnit");
		}

		// Get the entity manager
		LocalObjectShelfManager.emf.createEntityManager();
		// Set this shelf name
		shelfName = _shelfName;
		shelfNamePrefix = _shelfPrefix;
		// See if shelfName exists in persistence store
		if (!LocalObjectShelfManager.hasShelf(shelfNamePrefix + shelfName)) {
			// Create it if not
			addShelf(shelfName); // note: will add the prefix itself

		}

		// Fill the cache

		// Shelf is ready to go
		logger.info("LocalObjectShelf {}{} ready", shelfNamePrefix, shelfName);
	}

	private void addShelf(String name) throws ObjectShelfException, LocalDatabaseException {

		EntityManager em;
		if (!LocalObjectShelfManager.hasShelf(shelfNamePrefix + name)) {
			ObjectShelf newshelf = new ObjectShelf();
			em = beginTransaction();
			newshelf.setName(shelfNamePrefix + name);
			em.persist(newshelf);
			commitTransaction(em);
			logger.info("Created new LocalJythonShelf {}", name);
		} else {
			throw new ObjectShelfException("Could not make new shelf: a shelf with name " + shelfNamePrefix + name
					+ " exists already");
		}
	}

	/**
	 * Returns the shelf's name.
	 *
	 * @return shelf name
	 */
	public String getName() {
		return shelfName;
	}

	/**
	 * Checks if an entry with key keyName exists.
	 *
	 * @param keyName
	 * @return true if key exists.
	 */
	@Deprecated
	synchronized public Boolean hasKey(String keyName) {

		return has_key(keyName);

	}

	/**
	 * Checks if an entry with key keyName exists. (Jython convention)
	 *
	 * @param keyName
	 * @return true if key exists.
	 */
	synchronized public Boolean has_key(String keyName) {

		List<String> keyList = keys();
		if (keyList != null) {
			return keyList.contains(keyName);
		}
		return false;
	}

	/**
	 * Returns a list of all keys.
	 *
	 * @return a list of key names.
	 */
	synchronized public List<String> keys() {
		List<String> toReturn = new ArrayList<>();
		EntityManager em;
		ObjectShelf shelf;

		// i) find shelf
		em = beginTransaction();
		shelf = em.find(ObjectShelf.class, shelfNamePrefix + shelfName);
		commitTransaction(em);
		if (shelf == null) {
			return null;
		}

		// ii) find entries
		em = beginTransaction();
		Collection<ObjectShelfEntry> entryList = shelf.getEntries();

		// KLUDGE:
		// recover each shelf name
		for (ObjectShelfEntry entry : entryList) {
			toReturn.add(entry.getKeyName());
		}

		return toReturn;
	}

	/**
	 * Changes the entry for keyName to value. Will not create a new key/value pair if this one does not exist.
	 *
	 * @param keyName
	 * @param data
	 *            Any serializable object. All Jython objects are serializable.
	 * @throws ObjectShelfException
	 *             If key does not exist already (or for any number of lower level problems)
	 */
	synchronized public void setValue(String keyName, Serializable data) throws ObjectShelfException {
		if (hasKey(keyName)) {
			throw new ObjectShelfException("Could not change shelf entry: an entry with key " + keyName
					+ " does not exist. Use addValue() if to both change or create a value");
		}
		addValue(keyName, data);
	}

	/**
	 * Sets a value, creating it if necessary.
	 *
	 * @param keyName
	 * @param data
	 * @throws ObjectShelfException
	 */
	synchronized public void addValue(String keyName, Serializable data) throws ObjectShelfException {
		EntityManager em;
		ObjectShelf shelf;
		ObjectShelfEntry newEntry;
		boolean keyDidNotExist;

		byte[] toStore = null;
		try {
			toStore = toByteArray(data);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ObjectShelfException("Could not serialize input data object", e);
		}

		// * Add the empty the configuration *
		em = beginTransaction();

		// i) get the mode entity
		shelf = em.find(ObjectShelf.class, shelfNamePrefix + this.shelfName);

		if (hasKey(keyName)) { // change the data
			keyDidNotExist = false;
			Collection<ObjectShelfEntry> entryList = shelf.getEntries();

			for (ObjectShelfEntry entry : entryList) {
				String entryKeyName = entry.getKeyName();
				//TODO: an entry should not have a null keyname, but it can happen
				if (entryKeyName != null && entryKeyName.equals(keyName)) {
					entry.setData(toStore);
					em.merge(entry);
					em.persist(entry);
				}
			}
		} else { // Create the key
			//
			keyDidNotExist = true;
			newEntry = new ObjectShelfEntry();
			newEntry.setShelf(shelf);
			newEntry.setKeyName(keyName);
			newEntry.setData(toStore);
			shelf.getEntries().add(newEntry);
			em.persist(newEntry);
		}

		em.persist(shelf);

		commitTransaction(em);

		if (keyDidNotExist) {
			logger.info("Added new entry={} to shelf={}.", keyName, this.shelfName);
		}
	}

	/**
	 * Gets a value, returning a specified default if it does not exist.
	 *
	 * @param keyName
	 * @param defaultVal
	 * @return An object.
	 * @throws ObjectShelfException
	 */
	synchronized public Serializable getValue(String keyName, Serializable defaultVal) throws ObjectShelfException {
		if (!hasKey(keyName)) {
			return defaultVal;
		}
		// implicit else
		return getValue(keyName);
	}

	/**
	 * Gets a value, (Jython named)
	 *
	 * @param keyName
	 * @return Object
	 * @throws ObjectShelfException
	 */
	synchronized public Serializable get(String keyName) throws ObjectShelfException {
		return getValue(keyName);
	}

	/**
	 * Included for compatability with anydbm and shelve only. Does nothing.
	 */
	void sync() {
		// Flushes to file in somy anydbm implementations. Not needed here but
		// incuded
		// for compatability with anydbm and shelve.
	}

	/**
	 * Retrieves the value of the entry with key keyName.
	 *
	 * @param keyName
	 * @return The stored object.
	 * @throws ObjectShelfException
	 * @throws ObjectShelfException
	 *             If the key does not exist.
	 */
	synchronized public Serializable getValue(String keyName) throws ObjectShelfException {
		//TODO: code duplicated across set and get methods
		EntityManager em;
		ObjectShelf shelf;
		ObjectShelfEntry entry = null;

		// check key exists
		if (!has_key(keyName)) {
			throw new ObjectShelfException("Could not read shelf entry: an entry with key " + keyName
					+ " does not exist.");
		}

		// i) find shelf
		em = beginTransaction();
		shelf = em.find(ObjectShelf.class, shelfNamePrefix + shelfName);
		commitTransaction(em);

		// ii) find entry
		em = beginTransaction();
		Collection<ObjectShelfEntry> entryList = shelf.getEntries();

		// KLUDGE:
		for (ObjectShelfEntry anEntry : entryList) {
			String entryKeyName = anEntry.getKeyName();
			if (entryKeyName != null && entryKeyName.equals(keyName)) {
				entry = anEntry;
			}
		}
		if (entry == null) {
			throw new ObjectShelfException("Unexpected error reading shelf entry with key " + keyName);
		}
		try {
			return fromByteArray(entry.getData());
		} catch (Exception e) {
			e.printStackTrace();
			throw new ObjectShelfException("Could not un-serialize stored data back to an object", e);
		}
	}

	/**
	 * Deletes the entry with key keyName.
	 *
	 * @param keyName
	 * @throws ObjectShelfException
	 */
	synchronized public void delValue(String keyName) throws ObjectShelfException {
		EntityManager em;
		ObjectShelf shelf;
		ObjectShelfEntry toDelete = null;
		if (!hasKey(keyName)) {
			throw new ObjectShelfException("Could not delete shelf entry: no entry with key " + keyName + " exists");
		}

		// i) find shelf
		em = beginTransaction();
		shelf = em.find(ObjectShelf.class, this.shelfName);

		// ii) find entries
		Collection<ObjectShelfEntry> entryList = shelf.getEntries();

		// KLUDGE:
		// delete shelf
		for (ObjectShelfEntry entry : entryList) {
			if (entry.getKeyName().equals(keyName)) {
				toDelete = entry;
			}
		}
		shelf.getEntries().remove(toDelete);
		em.remove(toDelete);
		commitTransaction(em);
	}

	/**
	 * Deletes all the keys in shelf, but not the shelf itself.
	 *
	 * @throws ObjectShelfException
	 */
	synchronized public void clearShelf() throws ObjectShelfException {
		List<String> keyList = keys();
		for (String key : keyList) {
			delValue(key);
		}
	}

	/**
	 * Exports the entire shelf as a dictionary. This can then be re-imorted, or even saved in another shelf for a rainy
	 * day. (Jython name)
	 *
	 * @return A dictionary of key/object pairs.
	 * @throws ObjectShelfException
	 */
	synchronized public Dictionary<String, Serializable> dict() throws ObjectShelfException {
		Dictionary<String, Serializable> toReturn = new Hashtable<String, Serializable>();
		List<String> keyList = keys();
		for (String key : keyList) {
			toReturn.put(key, getValue(key));
		}
		return toReturn;
	}

	/**
	 * Exports the entire shelf as a dictionary. This can then be re-imorted, or even saved in another shelf for a rainy
	 * day.
	 *
	 * @return A dictionary of key/object pairs.
	 * @throws ObjectShelfException
	 */
	@Deprecated
	synchronized public Dictionary<String, Serializable> exportValues() throws ObjectShelfException {
		return dict();
	}

	/**
	 * Imports a dictionary of key/object pairs into the shelf. The pre-existing values are all cleared.
	 *
	 * @param dict
	 *            Expects a java dictionary, not a Jython one. A full one can be obtained using exportValues(), or an
	 *            empty on with getDictionaryForImport(). These dictionaries can be used in Jython just like standard
	 *            Jython ones.
	 * @throws ObjectShelfException
	 */
	synchronized public void importValues(Dictionary<String, Serializable> dict) throws ObjectShelfException {
		clearShelf();
		Enumeration<String> keyList = dict.keys();
		while (keyList.hasMoreElements()) {
			String key = keyList.nextElement();
			addValue(key.toString(), dict.get(key));
		}
	}

	/**
	 * Jython collection method.
	 *
	 * @param key
	 * @return true if key exists.
	 */
	public Boolean __contains__(String key) {
		return hasKey(key);
	}

	/**
	 * Jython collection method.
	 *
	 * @param key
	 * @throws ObjectShelfException
	 */
	public void __delitem__(String key) throws ObjectShelfException {
		delValue(key);
	}

	/**
	 * Jython collection method.
	 *
	 * @param key
	 * @throws ObjectShelfException
	 */
	public void __del__(String key) throws ObjectShelfException {
		__delitem__(key);
	}

	/**
	 * Jython collection method.
	 *
	 * @param key
	 * @return An object
	 * @throws ObjectShelfException
	 */
	public Object __getitem__(String key) throws ObjectShelfException {
		return getValue(key);
	}

	/**
	 * Jython collection method.
	 *
	 * @return number of elements.
	 */
	public Integer __len__() {
		return keys().size();
	}

	/**
	 * Jython collection method.
	 *
	 * @param key
	 * @param item
	 * @throws ObjectShelfException
	 */
	public void __setitem__(String key, Serializable item) throws ObjectShelfException {
		addValue(key, item);
	}

	@Override
	public String toString() {
		List<String> keyList = keys();
		String toReturn = "";
		if (keyList != null) {
			if (keyList.size() > 0) {
				for (String key : keyList) {
					try {
						toReturn += "   '" + key + "' : " + getValue(key).toString() + "\n";
					} catch (ObjectShelfException e) {
						toReturn = "Instance of LocalJythonShelf";
					}
				}
				return "\n {" + toReturn.substring(2, toReturn.length() - 1) + " }\n";
			}
		}

		return "Empty LocalObjectShelf";

	}

	/**
	 * Jython collection method.
	 *
	 * @return a string representation of object
	 */
	public String __str__() {
		return toString();
	}

	/**
	 * Jython collection method.
	 *
	 * @return a string representation of shelf
	 */
	public String __repr__() {
		return toString();
	}

	private byte[] toByteArray(Serializable obj) throws Exception {

		if (obj != null) {
			byte[] toReturn;
			ByteArrayOutputStream regStore = new ByteArrayOutputStream();

			ObjectOutputStream regObjectStream = new ObjectOutputStream(regStore);
			regObjectStream.writeObject(obj);
			toReturn = regStore.toByteArray();
			regObjectStream.close();
			regStore.close();
			return toReturn;
		}
		// implicit else

		return null;
	}

	private Serializable fromByteArray(byte[] regBytes) throws Exception {
		ByteArrayInputStream regArrayStream = new ByteArrayInputStream(regBytes);
		ObjectInputStream regObjectStream = new ObjectInputStream(regArrayStream);
		return (Serializable) regObjectStream.readObject();
	}

	/**
	 * Short cut method to get an entity manager and begin a transaction.
	 *
	 * @return the entity manager
	 */
	private EntityManager beginTransaction() {
		// Create a new EntityManager
		EntityManager em = LocalObjectShelfManager.emf.createEntityManager();

		// Begin transaction
		em.getTransaction().begin();
		return em;
	}

	/**
	 * Short cut method to commit a transaction and close the entity manager..
	 *
	 * @param em
	 */
	private void commitTransaction(EntityManager em) {
		// Commit the transaction
		em.getTransaction().commit();

		// Close this EntityManager
		em.close();
	}
}
