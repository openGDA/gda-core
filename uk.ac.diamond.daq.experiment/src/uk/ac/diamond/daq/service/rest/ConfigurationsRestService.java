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

package uk.ac.diamond.daq.service.rest;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionBase;
import uk.ac.diamond.daq.service.core.ConfigurationsServiceCore;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;
import uk.ac.gda.common.entity.Document;
import uk.ac.gda.common.exception.GDAServiceException;

/**
 * Exposes as REST service various acquisition configurations (diffraction, tomography, automated plans)
 *
 * @author Maurizio Nagni
 */
@RestController
@RequestMapping("/configurations")
public class ConfigurationsRestService {
	@Autowired
	private ConfigurationsServiceCore serviceCore;

	/**
	 *
	 * @return a list of AcquisitionBases with minimal informations (id, name, description)
	 * @throws GDAServiceException
	 */
	@GetMapping(value = "/scanningAcquisitions/{id}")
	public void getDocument(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) {
		try {
			var uuid = UUID.fromString(id);
			serviceCore.selectDocument(uuid, request, response);
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.PRECONDITION_FAILED);
		}
	}

	/**
	 * Retrieves documents eventually filtering them by type.
	 *
	 * Accepts {@code configurationType=[TOMO|MAP]} parameter
	 *
	 * <p>
	 * Example: {@code /scanningAcquisitions?configurationType=MAP}
	 * </p>
	 *
	 * @return a list of AcquisitionBases with minimal informations (id, name, description)
	 */
	@GetMapping(value = "/scanningAcquisitions")
	public void getDocuments(HttpServletRequest request, HttpServletResponse response) {
		serviceCore.selectDocuments(request, response);
	}

	/**
	 * Delete an existing AcquisitionBase.
	 */
	@DeleteMapping(value = "/scanningAcquisitions/{id}")
	public void deleteDocument(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) {
		serviceCore.deleteDocument(UUID.fromString(id), request, response);
	}

	/**
	 * Insert or update an existing diffraction.
	 *
	 * <p>
	 * If {@link AcquisitionBase#getUuid()} is {@code empty}, a new document is inserted, otherwise is updated.
	 * </p>
	 */
	@PostMapping(value = "/scanningAcquisitions/diffraction")
	public <T extends Document> T insertDiffraction(@RequestBody T acquisition, HttpServletRequest request, HttpServletResponse response) {
		serviceCore.insertDocument(acquisition, AcquisitionConfigurationResourceType.MAP, request , response);
		return acquisition;
	}

	/**
	 * Insert or update an existing tomography.
	 *
	 * <p>
	 * If {@link AcquisitionBase#getUuid()} is {@code empty}, a new document is inserted, otherwise is updated.
	 * </p>
	 */
	@PostMapping(value = "/scanningAcquisitions/tomography")
	public <T extends Document> T insertTomography(@RequestBody T acquisition, HttpServletRequest request, HttpServletResponse response) {
		serviceCore.insertDocument(acquisition, AcquisitionConfigurationResourceType.TOMO, request , response);
		return acquisition;
	}
}