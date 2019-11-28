/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.persistence.manager;

import java.util.List;
import java.util.Map;

import uk.ac.diamond.daq.application.persistence.data.SearchResult;
import uk.ac.diamond.daq.application.persistence.factory.PersistenceServiceFactory;
import uk.ac.diamond.daq.application.persistence.service.PersistenceException;
import uk.ac.diamond.daq.application.persistence.service.PersistenceService;
import uk.ac.diamond.daq.osgi.OsgiService;

/**
* Loads and saves items from an abstracted data store.
* <p>
* The classes of stored items must have the {@link uk.ac.diamond.daq.application.persistence.annotation.Persistable}
* annotation.  Also theses classes must also contain an id and version members which are both logs with the
* {@link uk.ac.diamond.daq.application.persistence.annotation.Id} and
* {@link uk.ac.diamond.daq.application.persistence.annotation.Version} annotations.
* <p>
* The service uses Jackson behind the seen to serialise the objects to json to be stored.  Therefore any class that is
* serialisable by Jackson will be able to be persisted by the service.  If the class contains members that cannot be
* serialised by Jackson the {@link uk.ac.diamond.daq.application.persistence.annotation.CustomPersistable} annotation
* can be used to specify a custom serialiser.
* <p>
* If the item's class contains other classes that are
* {@link uk.ac.diamond.daq.application.persistence.annotation.Persistable} then these are saved independently to the
* containing item.  Should the nested item be updated by another process the latest version of the nested item will be
* returned with the containing item.
* <p>
* Members of the class that have the {@link uk.ac.diamond.daq.application.persistence.annotation.Listable} annotation
* will be returned as part of the object search, and can be used as parameters for specific searches.  If a
* {@link uk.ac.diamond.daq.application.persistence.annotation.Listable} item with the attribute primary is true then
* any update to that member will cause a new item to be created.
* <p>
* An archive of all persisted item is retained.
*/
@OsgiService(PersistenceServiceWrapper.class)
public class PersistenceServiceWrapper implements PersistenceService {
	private PersistenceService persistenceService;

	/**
	 * Create the persistence wrapper from a factory
	 * @param persistenceServiceFactory
	 */
	public PersistenceServiceWrapper(PersistenceServiceFactory persistenceServiceFactory) {
		this.persistenceService = persistenceServiceFactory.getPersistenceService();
	}

    /**
     * Saves the an item that is persistable, and archived copy of the item is also stored
     *
     * @param item persistable item
     * @param <T> class
     * @throws PersistenceException if the storage system fails to save the item
     */
	@Override
	public <T> void save(T item) throws PersistenceException {
		persistenceService.save(item);
	}

    /**
     * Deletes an item with the specified ID note that archived items are not deleted
     *
     * @param persistenceId id of the item
     * @return whether the item was found and deleted
     * @throws PersistenceException if the there is an issue with the storage system
     */
	@Override
	public boolean delete(long persistenceId) throws PersistenceException {
		return persistenceService.delete(persistenceId);
	}

    /**
     * Finds all items stored of a type.  Items inherited from the class are also returned.  The results are only a
     * summary of each item based of the {@link uk.ac.diamond.daq.application.persistence.annotation.Listable}
     * annotations applied to the method and fields of each items class i.e. different classes may return differing
     * summaries that are contained in the same {@link SearchResult}
     *
     * @param clazz class or interface to be found
     * @param <T> class or interface to be found
     * @return results of the
     * @throws PersistenceException if error in storage layer
     */
	@Override
	public <T> SearchResult get(Class<T> clazz) throws PersistenceException {
		return persistenceService.get(clazz);
	}

    /**
     * Finds all items stored of a type using search parameters
     *
     * The search parameters are made from key value pairs of
     * {@link uk.ac.diamond.daq.application.persistence.annotation.Listable} value against the string value of the
     * {@link uk.ac.diamond.daq.application.persistence.annotation.Listable} field.
     *
     * @param searchParameters map of @Listable field name to {@link String} value of the field
     * @param clazz base class of the items to be returned
     * @param <T> class
     * @return matching items
     * @throws PersistenceException if searchParameters is null or storage system error
     */
	@Override
	public <T> SearchResult get(Map<String, Object> searchParameters, Class<T> clazz) throws PersistenceException {
		return persistenceService.get(searchParameters, clazz);
	}

    /**
     * Get the current version of item
     *
     * @param persistenceId id of the item
     * @param clazz base class of the item
     * @param <T> base class of the item
     * @return instance of that item
     * @throws PersistenceException if id not found, unable to cast to class, or storage system failure
     */
	@Override
	public <T> T get(long persistenceId, Class<T> clazz) throws PersistenceException {
		return persistenceService.get(persistenceId, clazz);
	}

    /**
     * List the versions available of an item.
     *
     * @param persistenceId id of the item
     * @return version available from the archive system
     * @throws PersistenceException if id not found, or storage system failure
     */
	@Override
	public List<Long> getVersions(long persistenceId) throws PersistenceException {
		return persistenceService.getVersions(persistenceId);
	}

    /**
     * Get a specific version of an item from the archive
     *
     * @param persistenceId id of the item
     * @param version version of the item
     * @param clazz base class of the item
     * @param <T> base class of the item
     * @return instance of that item
     * @throws PersistenceException if id/version not found, unable to cast to class, or storage system failure
     */
	@Override
	public <T> T getArchive(long persistenceId, long version, Class<T> clazz) throws PersistenceException {
		return persistenceService.getArchive(persistenceId, version, clazz);
	}
}
