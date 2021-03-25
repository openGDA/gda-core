package uk.ac.diamond.daq.service.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDFileHDF5;
import gda.device.detector.areadetector.v17.NDROI;
import gda.device.detector.areadetector.v17.NDStats;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;
import uk.ac.diamond.daq.service.command.receiver.device.DeviceRequest;
import uk.ac.diamond.daq.service.core.DeviceServiceCore;
import uk.ac.diamond.daq.service.rest.exception.GDAHttpException;
import uk.ac.gda.common.entity.device.DeviceValue;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;

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
public class DeviceRestService extends DeviceServiceCore {

	private final Map<String, Class<?>> interfaces = new HashMap<>();
	
	/**
	 * Returns the list of the avilable services
	 * @param request
	 * @param response
	 * @throws GDAHttpException
	 */
	@RequestMapping(value = "/detector/services", method = RequestMethod.GET)
	public void getServices(HttpServletRequest request, HttpServletResponse response) throws GDAHttpException {
		getServices(new ArrayList<>(getInterfaces().keySet()), request, response);
	}
	
	/**
	 * Returns the properties available for a specific service.
	 * 
	 * The properties are returned as a map of (methodName, class[])
	 *  
	 * @param service the name of the service to analyse
	 * @param request
	 * @param response
	 * @throws GDAHttpException
	 */
	@RequestMapping(value = "/detector/services/{service}/properties", method = RequestMethod.GET)
	public void getServiceProperties(@PathVariable String service, HttpServletRequest request, HttpServletResponse response) throws GDAHttpException {		
		getServiceProperties(getInterfaces().getOrDefault(service, null), request, response);
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
		DeviceRequest deviceRequest = createDeviceRequest(detectorName, serviceName, propertyName);
		super.getDeviceValue(deviceRequest, request, response);
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
		DeviceRequest deviceRequest = createDeviceRequest(deviceValue);
		super.setDeviceValue(deviceRequest, request, response);
	}

	private DeviceRequest createDeviceRequest(String detectorName, String serviceName, String propertyName) {
		DeviceValue deviceValue = createDeviceValue(detectorName, serviceName, propertyName);
		return createDeviceRequest(deviceValue);
	}

	private DeviceRequest createDeviceRequest(DeviceValue deviceValue) {		
		return new DeviceRequest(getService(deviceValue.getName(), deviceValue.getServiceName()), deviceValue);
	}
	
	private DeviceValue createDeviceValue(String detectorName, String serviceName, String propertyName) {
		DeviceValue.Builder deviceValueBuilder = new DeviceValue.Builder();
		deviceValueBuilder.withServiceName(serviceName);
		deviceValueBuilder.withProperty(propertyName);
		deviceValueBuilder.withName(detectorName);
		return deviceValueBuilder.build();
	}
	
	private Object getService(String detectorName, String service) {
		return getBean(detectorName, service, getInterfaces().getOrDefault(service, null));
	}
	
	private <T> T getBean(String detectorName, String suffix, Class<T> clazz) {
		// ask the facade to retrieve the bean from the parent application context
		return SpringApplicationContextFacade.getBean(String.format("%s_%s", detectorName, suffix), clazz, true);
	}
	
	/**
	 * Handles the HTTP response for the {@link ExperimentControllerException} thrown by this rest service
	 * @param e the thrown exception
	 * @return the exeption message
	 */
	@ExceptionHandler({ ExperimentControllerException.class })
    public @ResponseBody String handleException(ExperimentControllerException e) {
		return e.getMessage();
    }

	private Map<String, Class<?>> getInterfaces() {
		if (interfaces.isEmpty()) {
			interfaces.put("adbase", ADBase.class);
			interfaces.put("array", NDArray.class);
			interfaces.put("file", NDFile.class);
			interfaces.put("fileHDF5", NDFileHDF5.class);			
			interfaces.put("roi", NDROI.class);
			interfaces.put("stat", NDStats.class);			
		}
		return interfaces;
	}
	
}
