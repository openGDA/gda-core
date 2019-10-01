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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.persistence.implementation.data.SearchResult;
import uk.ac.diamond.daq.persistence.implementation.json.JsonSerialisationFactory;
import uk.ac.diamond.daq.persistence.implementation.service.ClassLoaderService;
import uk.ac.diamond.daq.persistence.implementation.service.PersistenceException;
import uk.ac.diamond.daq.persistence.implementation.service.PersistenceService;
import uk.ac.diamond.daq.persistence.implementation.service.VisitService;
import uk.ac.diamond.daq.persistence.implementation.service.impl.InMemoryJsonPersistenceService;

public class PersistenceManager implements PersistenceService {
	private static final Logger logger = LoggerFactory.getLogger(PersistenceManager.class);

	private PersistenceService persistenceService;

	public PersistenceManager(JsonSerialisationFactory jsonSerialisationFactory, ClassLoaderService classLoaderService, VisitService visitService) {
		logger.info("Creating persistence service");
		persistenceService = new InMemoryJsonPersistenceService(jsonSerialisationFactory, classLoaderService, visitService);
	}

	//-------------------------------------------------------------------------------------
	// PersistenceService implementation - delegates to InMemoryJsonPersistenceService
	//-------------------------------------------------------------------------------------

	@Override
	public <T> void save(T item) throws PersistenceException {
		persistenceService.save(item);
	}

	@Override
	public boolean delete(long persistenceId) throws PersistenceException {
		return persistenceService.delete(persistenceId);
	}

	@Override
	public <T> SearchResult get(Class<T> clazz) throws PersistenceException {
		return persistenceService.get(clazz);
	}

	@Override
	public <T> SearchResult get(Map<String, Object> searchParameters, Class<T> clazz) throws PersistenceException {
		return persistenceService.get(searchParameters, clazz);
	}

	@Override
	public <T> T get(long persistenceId, Class<T> clazz) throws PersistenceException {
		return persistenceService.get(persistenceId, clazz);
	}

	@Override
	public List<Long> getVersions(long persistenceId) throws PersistenceException {
		return persistenceService.getVersions(persistenceId);
	}

	@Override
	public <T> T getArchive(long persistenceId, long version, Class<T> clazz) throws PersistenceException {
		return persistenceService.getArchive(persistenceId, version, clazz);
	}
}
