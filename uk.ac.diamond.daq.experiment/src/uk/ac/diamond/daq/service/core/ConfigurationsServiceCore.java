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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import uk.ac.diamond.daq.service.CommonDocumentService;
import uk.ac.diamond.daq.service.command.receiver.CollectionCommandReceiver;
import uk.ac.diamond.daq.service.command.receiver.FilesCollectionCommandReceiver;
import uk.ac.diamond.daq.service.command.strategy.OutputStrategyFactory;
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
@Controller
public class ConfigurationsServiceCore {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationsServiceCore.class);

	@Autowired
	private CommonDocumentService documentService;
	
	public void selectDocument(UUID id, HttpServletRequest request, HttpServletResponse response) {
		CollectionCommandReceiver<Document> ccr = new FilesCollectionCommandReceiver<>(Document.class, response, request);
		try {
			documentService.selectDocument(ccr, id, OutputStrategyFactory.getJSONOutputStrategy());
		} catch (GDAServiceException e) {
			logAndWrapException("Error retrieving document", e);
		}
	}
	
	public void selectDocuments(HttpServletRequest request, HttpServletResponse response) {
		CollectionCommandReceiver<Document> ccr = new FilesCollectionCommandReceiver<>(Document.class, response, request);
		var filter = getDocumentFilter(request);
		try {
			documentService.selectDocuments(ccr, filter, OutputStrategyFactory.getJSONOutputStrategy());
		} catch (GDAServiceException e) {
			logAndWrapException("Error retrieving documents", e);
		}
	}
	
	public void insertDocument(Document document, AcquisitionConfigurationResourceType type, HttpServletRequest request, HttpServletResponse response) {
		CollectionCommandReceiver<Document> ccr = new FilesCollectionCommandReceiver<>(Document.class, response, request, type);
		try {
			documentService.insertDocument(ccr, document, OutputStrategyFactory.getJSONOutputStrategy());
		} catch (GDAServiceException e) {
			if (e.getCause() instanceof FileAlreadyExistsException) {
				logAndWrapException("File already exists", e, HttpStatus.CONFLICT);
			} else {
				logAndWrapException("Error saving document", e);
			}
		}		
	}
	
	public void deleteDocument(UUID id, HttpServletRequest request, HttpServletResponse response) {
		CollectionCommandReceiver<Document> ccr = new FilesCollectionCommandReceiver<>(Document.class, response, request);
		try {
			documentService.deleteDocument(ccr, id, OutputStrategyFactory.getJSONOutputStrategy());
		} catch (GDAServiceException e) {
			logAndWrapException("Error deleting document", e);
		}
	}
	
	private DocumentFilter getDocumentFilter(HttpServletRequest request) {
		var builder = new DocumentFilterBuilder();
		if (request.getParameterMap().containsKey("configurationType")) {
			builder.setFileExtension(request.getParameterMap().get("configurationType")[0]);
		}
		return builder.build();
	}
	
	private void logAndWrapException(String message, Exception exception) {
		logAndWrapException(message, exception, HttpStatus.PRECONDITION_FAILED);
	}
	
	private void logAndWrapException(String message, Exception exception, HttpStatus status) {
		logger.error(message, exception);
		throw new ResponseStatusException(status, message, exception);
	}
}