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
package uk.ac.diamond.daq.service.core;

import java.nio.file.FileAlreadyExistsException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.diamond.daq.service.CommonDocumentService;
import uk.ac.diamond.daq.service.command.receiver.CollectionCommandReceiver;
import uk.ac.diamond.daq.service.command.receiver.FilesCollectionCommandReceiver;
import uk.ac.diamond.daq.service.command.strategy.OutputStrategyFactory;
import uk.ac.diamond.daq.service.rest.exception.GDAHttpException;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;
import uk.ac.gda.common.entity.Document;
import uk.ac.gda.common.entity.filter.DocumentFilter;
import uk.ac.gda.common.entity.filter.DocumentFilterBuilder;
import uk.ac.gda.common.exception.GDAServiceException;
import uk.ac.gda.core.tool.spring.AcquisitionFileContext;

/**
 * Implements the {@link CommonDocumentService} based on the filesytem exposed by the {@link AcquisitionFileContext}
 * 
 * @author Maurizio Nagni
 *
 */
public class ConfigurationsServiceCore extends CommonDocumentService {

	protected void selectDocument(UUID id, HttpServletRequest request, HttpServletResponse response) throws GDAHttpException {
		CollectionCommandReceiver<Document> ccr = new FilesCollectionCommandReceiver<>(Document.class, response, request);
		try {
			selectDocument(ccr, id, OutputStrategyFactory.getJSONOutputStrategy());
		} catch (GDAServiceException e) {
			throw new GDAHttpException(e.getMessage(), HttpServletResponse.SC_PRECONDITION_FAILED);
		}
	}
	
	protected void selectDocuments(HttpServletRequest request, HttpServletResponse response) throws GDAHttpException {
		CollectionCommandReceiver<Document> ccr = new FilesCollectionCommandReceiver<>(Document.class, response, request);
		DocumentFilter filter = getDocumentFilter(request);
		try {
			selectDocuments(ccr, filter, OutputStrategyFactory.getJSONOutputStrategy());
		} catch (GDAServiceException e) {
			throw new GDAHttpException(e.getMessage(), HttpServletResponse.SC_PRECONDITION_FAILED);
		}
	}
	
	protected void insertDocument(Document document, AcquisitionConfigurationResourceType type, HttpServletRequest request, HttpServletResponse response) throws GDAHttpException {
		CollectionCommandReceiver<Document> ccr = new FilesCollectionCommandReceiver<>(Document.class, response, request, type);
		try {
			insertDocument(ccr, document, OutputStrategyFactory.getJSONOutputStrategy());
		} catch (GDAServiceException e) {
			if (e.getCause() instanceof FileAlreadyExistsException) {
				throw new GDAHttpException(e.getMessage(), HttpServletResponse.SC_CONFLICT);	
			}
			throw new GDAHttpException(e.getMessage(), HttpServletResponse.SC_PRECONDITION_FAILED);
		}		
	}
	
	protected void deleteDocument(UUID id, HttpServletRequest request, HttpServletResponse response) throws GDAHttpException {
		CollectionCommandReceiver<Document> ccr = new FilesCollectionCommandReceiver<>(Document.class, response, request);
		try {
			super.deleteDocument(ccr, id, OutputStrategyFactory.getJSONOutputStrategy());
		} catch (GDAServiceException e) {
			throw new GDAHttpException(e.getMessage(), HttpServletResponse.SC_PRECONDITION_FAILED);
		}
	}
	
	private DocumentFilter getDocumentFilter(HttpServletRequest request) {
		DocumentFilterBuilder builder = new DocumentFilterBuilder();
		if (request.getParameterMap().containsKey("configurationType")) {
			builder.setFileExtension(request.getParameterMap().get("configurationType")[0]);
		}
		return builder.build();
	}
	
	protected UUID getUUID(String id) throws GDAServiceException {
		try {
			return UUID.fromString(id);
		} catch (IllegalArgumentException e) {
			throw new GDAServiceException("Cannot convert id: " + id);
		}
	}
}