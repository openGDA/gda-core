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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.naming.directory.InvalidAttributesException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import uk.ac.diamond.daq.mapping.api.document.DocumentMapper;
import uk.ac.diamond.daq.service.ScanningAcquisitionFileService;
import uk.ac.diamond.daq.service.ServiceUtils;
import uk.ac.diamond.daq.service.command.strategy.OutputStrategy;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;
import uk.ac.gda.common.entity.Document;
import uk.ac.gda.common.entity.filter.DocumentFilter;
import uk.ac.gda.common.entity.filter.DocumentFilterBuilder;
import uk.ac.gda.common.exception.GDAException;
import uk.ac.gda.common.exception.GDAServiceException;
import uk.ac.gda.core.tool.spring.AcquisitionFileContext;
import uk.ac.gda.core.tool.spring.DiffractionContextFile;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.core.tool.spring.TomographyContextFile;

/**
 * An implementation of {@link CollectionCommandReceiver} which persists on a file system
 * 
 * @author Maurizio Nagni
 *
 * @param <T>
 */
public class FilesCollectionCommandReceiver<T extends Document> implements CollectionCommandReceiver<T> {

	private static final Logger logger = LoggerFactory.getLogger(FilesCollectionCommandReceiver.class);

	private final Class<T> documentClass;
	private final OutputStream response;
	private AcquisitionConfigurationResourceType type;

	// To use it call getFileContext()
	private AcquisitionFileContext fileContext;
	// To use it call getFileService()
	private ScanningAcquisitionFileService fileService;
	// To use it call getFileService()
	private DocumentMapper documentMapper;
	// To use it call getServiceUtils()
	private ServiceUtils serviceUtils;
	
	public FilesCollectionCommandReceiver(Class<T> documentClass, OutputStream response) {
		this(documentClass, response, null);
	}

	public FilesCollectionCommandReceiver(Class<T> documentClass, OutputStream response, AcquisitionConfigurationResourceType type) {
		this.documentClass = documentClass;
		this.response = response;
		this.type = type;
	}
	
	@Override
	public void count(DocumentFilter filter, OutputStrategy<T> outputStrategy) throws GDAServiceException {
		// TBD	
	}

	@Override
	public void query(DocumentFilter filter, OutputStrategy<T> outputStrategy) throws GDAServiceException {	
		List<T> document = getFiles(filter).stream()
				.map(this::readDocument)
				.filter(Objects::nonNull)
				.map(this::parseDocument)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		getServiceUtils().writeOutput(new TypeReference<List<T>>() {}, document, outputStrategy, response);
	}

	@Override
	public void getDocument(UUID id, OutputStrategy<T> outputStrategy) throws GDAServiceException {
		T document = getFiles().stream()
			.map(this::readDocument)
			.filter(Objects::nonNull)
			.map(this::parseDocument)
			.filter(Objects::nonNull)
			.filter(d -> d.getUuid().equals(id))
			.findFirst()
			.orElse(null);

		getServiceUtils().writeOutput(document, outputStrategy, response);
	}
	
	/**
	 * Insert a new document. If this 
	 */
	@Override
	public void insertDocument(T document, OutputStrategy<T> outputStrategy) throws GDAServiceException {
		if (document.getUuid() == null) {
			insertId(document);	
		} else {
			//Override existing document
			deleteDocument(document.getUuid());
		}
		try {
			getFileService().saveTextDocument(getDocumentMapper().convertToJSON(document), formatConfigurationFileName(document.getName()),
					getType(), false);
		} catch (InvalidAttributesException | IOException | GDAException e) {
			throw new GDAServiceException(e.getMessage(), e);
		}	
		getServiceUtils().writeOutput(document, outputStrategy, response);
	}

	@Override
	public void deleteDocument(UUID id, OutputStrategy<T> outputStrategy) throws GDAServiceException {
		T document = deleteDocument(id);
		if (document != null) 
			getServiceUtils().writeOutput(document, outputStrategy, response);
	}
	
	private T deleteDocument(UUID id) throws GDAServiceException {
		for (File file : getFiles()) {
			T document = Optional.ofNullable(readDocument(file))
					.map(this::parseDocument)
					.orElse(null);
			
			if (document == null || !document.getUuid().equals(id))
				continue;
			
			try {
				Files.deleteIfExists(file.toPath());
				return document;
			} catch (IOException e) {
				throw new GDAServiceException("Cannot delete document", e);
			}		
		}
		return null;
	}
	
	private void insertId(T document) {
		try {
			PropertyAccessor documentAccessor = PropertyAccessorFactory.forDirectFieldAccess(document);
			documentAccessor.setPropertyValue("uuid", UUID.randomUUID());
		}  catch (Exception e) {
			logger.error("Cannot insert uuid in a new document", e);
		}
	}
	
	private String readDocument(File document) {
		try {
			return FileUtils.readFileToString(document, Charset.defaultCharset());
		} catch (IOException e) {
			logger.error("Cannot read the document", e);
			return null;
		}
	}
	
	private T parseDocument(String document) {
		try {
			return getDocumentMapper().convertFromJSON(document, documentClass);	
		} catch (GDAException e) {
			logger.error("Cannot parse the document", e);
			return null;
		}
	}

	private Collection<File> getFiles(DocumentFilter filter) throws GDAServiceException {
		return getDocuments(filter);
	}
	
	private Collection<File> getFiles() throws GDAServiceException {
		return getFiles(new DocumentFilterBuilder().build());
	}
	
	private Collection<File> getDocuments(DocumentFilter filter) throws GDAServiceException {
		Set<File> files = new HashSet<>();
		files.addAll(getDocuments(getFileContext().getDiffractionContext().getContextFile(DiffractionContextFile.DIFFRACTION_CONFIGURATION_DIRECTORY)));
		files.addAll(getDocuments(getFileContext().getTomographyContext().getContextFile(TomographyContextFile.TOMOGRAPHY_CONFIGURATION_DIRECTORY)));
		Optional.ofNullable(filter.getFileExtension())
			.ifPresent(s -> files.removeIf(f -> {
				return !FilenameUtils.getExtension(f.getName()).equals(s);	
			}));
		return files;
	}
	
	private Collection<File> getDocuments(URL dirConf) throws GDAServiceException {
		File directory;
		try {
			directory = new File(dirConf.toURI());
			if (directory.isFile())
				directory = directory.getParentFile();
		} catch (URISyntaxException e) {
			throw new GDAServiceException("No configuration directory " + dirConf);
		}
		return FileUtils.listFiles(directory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
	}	
	
	private String formatConfigurationFileName(String fileName) {
		return Optional.ofNullable(fileName)
			.map(n -> fileName.replaceAll("\\s", ""))
			.filter(n -> n.length() > 0)
			.orElseGet(() -> "noNameConfiguration");
	}
	
	private AcquisitionFileContext getFileContext() {		
		if (fileContext == null) {
			fileContext = SpringApplicationContextFacade.getBean(AcquisitionFileContext.class);
		}
		return fileContext;
	}

	private ScanningAcquisitionFileService getFileService() {		
		if (fileService == null) {
			fileService = SpringApplicationContextFacade.getBean(ScanningAcquisitionFileService.class);
		}
		return fileService;
	}
	
	private DocumentMapper getDocumentMapper() {		
		if (documentMapper == null) {
			documentMapper = SpringApplicationContextFacade.getBean(DocumentMapper.class);
		}
		return documentMapper;
	}

	private ServiceUtils getServiceUtils() {		
		if (serviceUtils == null) {
			serviceUtils = SpringApplicationContextFacade.getBean(ServiceUtils.class);
		}
		return serviceUtils;
	}
	
	private AcquisitionConfigurationResourceType getType() {
		return Optional.ofNullable(type)
				.orElseGet(() -> AcquisitionConfigurationResourceType.DEFAULT);
			
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(documentClass, response, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FilesCollectionCommandReceiver<?> other = (FilesCollectionCommandReceiver<?>) obj;
		return Objects.equals(documentClass, other.documentClass)
				&& Objects.equals(response, other.response) && type == other.type;
	}
}