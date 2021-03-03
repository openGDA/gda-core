package uk.ac.diamond.daq.service.command.receiver.device;

import uk.ac.gda.common.entity.device.DeviceValue;

/**
 * A utility class to move data for the device rest operations
 * 
 * @author Maurizio Nagni
 *
 */
public class DeviceRequest {

	/**
	 * The instance of the service providing the property to get/set 
	 */
	private final Object device;
	
	/**
	 * The client request document
	 */
	private final DeviceValue deviceValue;

	public DeviceRequest(Object device, DeviceValue deviceValue) {
		super();
		this.device = device;
		this.deviceValue = deviceValue;
	}

	public Object getDevice() {
		return device;
	}

	public DeviceValue getDeviceValue() {
		return deviceValue;
	}
}
