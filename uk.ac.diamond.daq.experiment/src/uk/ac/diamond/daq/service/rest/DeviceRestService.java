package uk.ac.diamond.daq.service.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;
import uk.ac.diamond.daq.service.core.DeviceServiceCore;
import uk.ac.diamond.daq.service.rest.exception.GDAHttpException;
import uk.ac.gda.common.entity.device.DeviceValue;

/**
 * Allows any client to communicate with GDA server side spring beans through a REST interface
 *
 * <p>
 * There are two assumptions
 * <ul>
 * <li>
 * the bean in the GDA server is named as {device}_{service}
 * </li>
 * <li>
 * any device is associated with any of the available services (still does not handle device with a subset of services)
 * </li>
 * </ul>
 * </p>
 * 
 * @author Maurizio Nagni
 */
@RestController
@RequestMapping("/device")
public class DeviceRestService {
	
	@Autowired
	private DeviceServiceCore serviceCore;

	/**
	 * Returns a {@code List<String>} of the available services
	 * @param request
	 * @param response
	 * @throws GDAHttpException
	 */
	@RequestMapping(value = "/detector/services", method = RequestMethod.GET)
	public void getServices(HttpServletRequest request, HttpServletResponse response) throws GDAHttpException {
		serviceCore.getServices(request, response);
	}

	/**
	 * Returns the properties available for a specific service.
	 *
	 * The properties are returned as a DeviceMethods instance
	 *
	 * @param service the name of the service to analyse
	 * @param request
	 * @param response
	 * @throws GDAHttpException
	 */
	@RequestMapping(value = "/detector/services/{service}/properties", method = RequestMethod.GET)
	public void getServiceProperties(@PathVariable String service, HttpServletRequest request, HttpServletResponse response) throws GDAHttpException {
		serviceCore.getServiceProperties(service, request, response);
	}

	/**
	 * Return the value for a specific detector, service and property
	 * 
	 * @param detectorName
	 * @param service
	 * @param propertyName
	 * @param request
	 * @param response
	 * @throws GDAHttpException
	 */
	@RequestMapping(value = "/detector/{detectorName}/{serviceName}/{propertyName}", method = RequestMethod.GET)
	public void getDeviceValue(@PathVariable String detectorName, @PathVariable String serviceName, @PathVariable String propertyName,
			HttpServletRequest request, HttpServletResponse response) throws GDAHttpException {
		var deviceRequest = serviceCore.createDeviceRequest(detectorName, serviceName, propertyName);
		serviceCore.getDeviceValue(deviceRequest, request, response);
	}

	/**
	 * Set the value for a specific detector, service and property.
	 * <p>
	 * The request body contains both the necessary parameters to identify detector, service and property and the value to set
	 * </p>
	 * 
	 * @param deviceValue
	 * @param request
	 * @param response
	 * @throws GDAHttpException
	 */
	@RequestMapping(value = "/detector", method = RequestMethod.POST)
	public void setDeviceValue(@RequestBody DeviceValue deviceValue, 
			HttpServletRequest request, HttpServletResponse response) throws GDAHttpException {
		var deviceRequest = serviceCore.createDeviceRequest(deviceValue);
		serviceCore.setDeviceValue(deviceRequest, request, response);
	}

	/**
	 * Handles the HTTP response for the {@link ExperimentControllerException} thrown by this rest service
	 * @param e the thrown exception
	 * @return the exception message
	 */
	@ExceptionHandler({ ExperimentControllerException.class })
    public @ResponseBody String handleException(ExperimentControllerException e) {
		return e.getMessage();
    }
}
