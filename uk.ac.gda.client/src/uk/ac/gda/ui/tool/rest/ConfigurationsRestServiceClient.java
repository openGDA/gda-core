/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.tool.rest;

import static uk.ac.gda.ui.tool.rest.ClientRestService.formatURL;
import static uk.ac.gda.ui.tool.rest.ClientRestService.submitRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.gda.client.exception.GDAClientRestException;
import uk.ac.gda.common.entity.Document;
import uk.ac.gda.ui.tool.spring.ClientSpringContext;

/**
 * Provides to the GDA client access to the Configurations rest service
 *
 * <p>
 * <i>client.acquisition.service.endpoint</i> is the property which configures the URL endpoint to the service. The
 * default value is {@code http://127.0.0.1:8888/configurations}
 * </p>
 *
 * @author Maurizio Nagni
 */
@Service
public class ConfigurationsRestServiceClient {

	@Autowired
	private ClientSpringContext clientContext;

	private String getServiceEndpoint() {
		return formatURL(clientContext.getRestServiceEndpoint(), "/configurations");
	}

	public Document getDocument(String id) throws GDAClientRestException {
		String url = formatURL(getServiceEndpoint(), "/scanningAcquisitions/" + id);
		ResponseEntity<Document> response = submitRequest(url, HttpMethod.GET, null, Document.class);
		return response.getBody();
	}

	/**
	 * Retrieve a {@link Document} from an exernal service
	 * @param url the document url
	 * @return the document
	 * @throws GDAClientRestException
	 */
	public Document getDocument(URL url) throws GDAClientRestException {
		ResponseEntity<Document> response = submitRequest(url.toString(), HttpMethod.GET, null, Document.class);
		return response.getBody();
	}

	public <T extends Document> List<T> getDocuments() throws GDAClientRestException {
		String url = formatURL(getServiceEndpoint(), "/scanningAcquisitions");
		ResponseEntity<List<T>> response = submitRequest(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<T>>() {});
		return response.getBody();
	}

	public Document deleteDocument(String id) throws GDAClientRestException {
		String url = formatURL(getServiceEndpoint(), "/scanningAcquisitions/" + id);
		ResponseEntity<Document> response = submitRequest(url, HttpMethod.DELETE, null, Document.class);
		return response.getBody();
	}

	public ScanningAcquisition insertDiffraction( ScanningAcquisition acquisition) throws GDAClientRestException {
		String url = formatURL(getServiceEndpoint(), "/scanningAcquisitions/diffraction");
		HttpEntity<ScanningAcquisition> responseEntity = new HttpEntity<>(acquisition);
		ResponseEntity<ScanningAcquisition> response = submitRequest(url, HttpMethod.POST, responseEntity, ScanningAcquisition.class);
		return response.getBody();
	}

	public ScanningAcquisition insertImaging( ScanningAcquisition acquisition) throws GDAClientRestException {
		String url = formatURL(getServiceEndpoint(), "/scanningAcquisitions/tomography");
		HttpEntity<ScanningAcquisition> responseEntity = new HttpEntity<>(acquisition);
		ResponseEntity<ScanningAcquisition> response = submitRequest(url, HttpMethod.POST, responseEntity, ScanningAcquisition.class);
		return response.getBody();
	}

	/**
	 * Utility to convert a configuration document id to its explicit URL so that may be retrieved using {@link #getDocument(URL)}
	 *
	 * @param uuid the document to delete
	 * @return the document URL for the given uuid
	 * @throws GDAClientRestException
	 * @{@link Deprecated} use instead {@link #getDocumentURL(UUID)}
	 */
	public URL getDocumentURL(UUID uuid) throws GDAClientRestException {
		try {
			return new URL(formatURL(getServiceEndpoint(), "/scanningAcquisitions/" + uuid.toString()));
		} catch (MalformedURLException e) {
			throw new GDAClientRestException("Cannot create docment URL", e);
		}
	}
}