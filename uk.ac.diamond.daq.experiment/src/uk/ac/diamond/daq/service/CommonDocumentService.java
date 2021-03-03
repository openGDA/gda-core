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

package uk.ac.diamond.daq.service;

import java.util.UUID;

import uk.ac.diamond.daq.service.command.DeleteDocumentCollectionCommand;
import uk.ac.diamond.daq.service.command.GetDocumentCollectionCommand;
import uk.ac.diamond.daq.service.command.InsertDocumentCollectionCommand;
import uk.ac.diamond.daq.service.command.QueryDocumentCollectionCommand;
import uk.ac.diamond.daq.service.command.receiver.CollectionCommandReceiver;
import uk.ac.diamond.daq.service.command.strategy.CollectionOutputStrategy;
import uk.ac.gda.common.command.ExecuteCommand;
import uk.ac.gda.common.entity.Document;
import uk.ac.gda.common.entity.filter.DocumentFilter;
import uk.ac.gda.common.exception.GDAServiceException;

/**
 * Implements the essential CRUD over the existing documents 
 * 
 * @author Maurizio Nagni
 *
 */
public class CommonDocumentService {

	/**
	 * Retrieves a single document
	 * 
	 * @param <T> a subclass of {@link Document}
	 * @param ccr defines how collect the document
	 * @param id the document identifier 
	 * @param outputStrategy defines how write the output
	 * @throws GDAServiceException
	 */
	protected <T extends Document> void selectDocument(CollectionCommandReceiver<T> ccr, UUID id, CollectionOutputStrategy<T> outputStrategy) throws GDAServiceException {
		ExecuteCommand cc = new GetDocumentCollectionCommand<>(ccr, id, outputStrategy);
		cc.execute();
	}

	/**
	 * Retrieves multiple documents 
	 * 
	 * @param <T> a subclass of {@link Document}
	 * @param ccr defines how collect the document
	 * @param filter filter the document collections  
	 * @param outputStrategy defines how write the output
	 * @throws GDAServiceException
	 */
	protected <T extends Document> void selectDocuments(CollectionCommandReceiver<T> ccr, DocumentFilter filter, CollectionOutputStrategy<T> outputStrategy) throws GDAServiceException {
		ExecuteCommand cc = new QueryDocumentCollectionCommand<>(ccr, filter, outputStrategy);
		cc.execute();
	}
	
	/**
	 * Inserts a document 
	 * 
	 * @param <T> a subclass of {@link Document}
	 * @param ccr defines how collect the document
	 * @param document the document to insert  
	 * @param outputStrategy defines how write the output
	 * @throws GDAServiceException
	 */	
	protected <T extends Document> void insertDocument(CollectionCommandReceiver<T> ccr, T document, CollectionOutputStrategy<T> outputStrategy) throws GDAServiceException {
		ExecuteCommand cc = new InsertDocumentCollectionCommand<>(ccr, document, outputStrategy);
		cc.execute();
	}
	
	/**
	 * Deletes a document 
	 * 
	 * @param <T> a subclass of {@link Document}
	 * @param ccr defines how collect the document
	 * @param id the document identifier   
	 * @param outputStrategy defines how write the output
	 * @throws GDAServiceException
	 */	
	protected <T extends Document> void deleteDocument(CollectionCommandReceiver<T> ccr, UUID id, CollectionOutputStrategy<T> outputStrategy) throws GDAServiceException {
		ExecuteCommand cc = new DeleteDocumentCollectionCommand<>(ccr, id, outputStrategy);
		cc.execute();
	}
}
