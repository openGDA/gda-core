package uk.ac.diamond.daq.service.rest;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionBase;
import uk.ac.diamond.daq.service.core.ConfigurationsServiceCore;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;
import uk.ac.gda.common.entity.Document;
import uk.ac.gda.common.exception.GDAServiceException;
import uk.ac.gda.core.tool.GDAHttpException;

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
	@RequestMapping(value = "/scanningAcquisitions/{id}", method = RequestMethod.GET)
	public void getDocument(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) throws GDAHttpException {
		UUID uuid;
		try {
			uuid = serviceCore.getUUID(id);
		} catch (GDAServiceException e) {
			throw new GDAHttpException("Cannot convert the document", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		serviceCore.selectDocument(uuid, request, response);
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
	 * @throws GDAServiceException 
	 */
	@RequestMapping(value = "/scanningAcquisitions", method = RequestMethod.GET)
	public void getDocuments(HttpServletRequest request, HttpServletResponse response) throws GDAHttpException {
		serviceCore.selectDocuments(request, response);
	}

	/**
	 * Delete an existing AcquisitionBase.
	 *
	 * @return
	 * @throws GDAHttpException 
	 */
	@RequestMapping(value = "/scanningAcquisitions/{id}", method = RequestMethod.DELETE)
	public void deleteDocument(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) throws GDAHttpException {
		UUID uuid;
		try {
			uuid = serviceCore.getUUID(id);
		} catch (GDAServiceException e) {
			throw new GDAHttpException("Cannot convert the document", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		serviceCore.deleteDocument(uuid, request, response);
	}

	/**
	 * Insert or update an existing diffraction.
	 *
	 * <p>
	 * If {@link AcquisitionBase#getUuid()} is {@code empty}, a new document is inserted, otherwise is updated.
	 * </p> 
	 * @return
	 */
	@RequestMapping(value = "/scanningAcquisitions/diffraction", method = RequestMethod.POST)
	public <T extends Document> T insertDiffraction(@RequestBody T acquisition, HttpServletRequest request, HttpServletResponse response) throws GDAHttpException {
		serviceCore.insertDocument(acquisition, AcquisitionConfigurationResourceType.MAP, request , response);
		return acquisition;
	}

	/**
	 * Insert or update an existing tomography.
	 *
	 * <p>
	 * If {@link AcquisitionBase#getUuid()} is {@code empty}, a new document is inserted, otherwise is updated.
	 * </p> 
	 * @return
	 */
	@RequestMapping(value = "/scanningAcquisitions/tomography", method = RequestMethod.POST)
	public <T extends Document> T insertTomography(@RequestBody T acquisition, HttpServletRequest request, HttpServletResponse response) throws GDAHttpException {
		serviceCore.insertDocument(acquisition, AcquisitionConfigurationResourceType.TOMO, request , response);
		return acquisition;
	}

	@ExceptionHandler(GDAHttpException.class)
	public ResponseEntity<GDAHttpException> exceptionHandler(GDAHttpException exc) {
		return new ResponseEntity<>(exc, HttpStatus.valueOf(exc.getStatus()));
	}
}