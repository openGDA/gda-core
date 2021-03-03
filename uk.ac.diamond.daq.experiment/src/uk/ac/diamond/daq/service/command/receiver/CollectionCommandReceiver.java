/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.service.command.receiver;

import java.util.UUID;

import uk.ac.diamond.daq.service.command.strategy.OutputStrategy;
import uk.ac.gda.common.entity.Document;
import uk.ac.gda.common.entity.filter.DocumentFilter;
import uk.ac.gda.common.exception.GDAServiceException;

/**
 * Defines a minimal set of commands for CRUD operations on documents implementing {@link Document}
 *
 * @author Maurizio Nagni
 *
 * @param <T>
 */
public interface CollectionCommandReceiver<T extends Document> {
	/**
	 * Returns the total numbers of documents.
	 *
	 * @param filter the filter to apply on the document collection
	 * @param outputStrategy the writer handling the result
	 * @throws GDAServiceException
	 */
	void count(DocumentFilter filter, OutputStrategy<T> outputStrategy) throws GDAServiceException;

	/**
	 * Returns a set of documents
	 *
	 * @param filter the filter to apply on the document collection
	 * @param outputStrategy the writer handling the result
	 * @throws GDAServiceException
	 */
	void query(DocumentFilter filter, OutputStrategy<T> outputStrategy) throws GDAServiceException;

	/**
	 * Returns a specific document.
	 *
	 * @param id the document identifier
	 * @param outputStrategy the writer handling the result
	 * @throws GDAServiceException
	 */
	void getDocument(UUID id , OutputStrategy<T> outputStrategy) throws GDAServiceException;

	/**
	 * Deletes a specific document.
	 *
	 * @param id the document identifier
	 * @param outputStrategy the writer handling the result
	 * @throws GDAServiceException
	 */
	void deleteDocument(UUID id, OutputStrategy<T> outputStrategy) throws GDAServiceException;
	
	/**
	 * Inserts a new document.
	 *
	 * @param document the document identifier
	 * @param outputStrategy the writer handling the result
	 * @throws GDAServiceException
	 */
	void insertDocument(T document, OutputStrategy<T> outputStrategy) throws GDAServiceException;	
}
