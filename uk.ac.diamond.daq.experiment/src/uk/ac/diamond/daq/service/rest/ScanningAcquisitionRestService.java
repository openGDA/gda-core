package uk.ac.diamond.daq.service.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionBase;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionConfigurationBase;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionParametersBase;
import uk.ac.diamond.daq.mapping.api.document.exception.ScanningAcquisitionServiceException;
import uk.ac.diamond.daq.service.ScanningAcquisitionService;
import uk.ac.diamond.daq.service.core.AcquisitionServiceCore;
import uk.ac.gda.api.acquisition.request.MscanRequest;
import uk.ac.gda.api.acquisition.response.RunAcquisitionResponse;

/**
 * Provides access to a {@link ScanningAcquisitionService}
 * 
 * <p> 
 * For documentation about the API aee <a href="https://confluence.diamond.ac.uk/display/DIAD/Scanning+Acquisition+Service">Acquisition service</a>
 * </p> 
 * 
 * @author Maurizio Nagni
 */
@RestController
@RequestMapping("/acquisition")
public class ScanningAcquisitionRestService {

	@Autowired
	private AcquisitionServiceCore serviceCore;
	
	
	/**
	 * Receives request for acquisitions.
	 * @param acquisition
	 * @return
	 * @throws ScanningAcquisitionServiceException
	 */
	@PostMapping(value = "/run")
	public @ResponseBody ResponseEntity<RunAcquisitionResponse> runScan(
			@RequestBody AcquisitionBase<? extends AcquisitionConfigurationBase<? extends AcquisitionParametersBase>> acquisition) {
		return serviceCore.runAcquisition(acquisition);
	}



	/**
	 * Receives request for acquisitions.
	 * @param acquisition
	 * @return
	 * @throws ScanningAcquisitionServiceException
	 */
	@PostMapping(value = "/mscan")
	public @ResponseBody ResponseEntity<RunAcquisitionResponse> run(
			@RequestBody MscanRequest request) {
		return serviceCore.runMScan(request);
	}

}
