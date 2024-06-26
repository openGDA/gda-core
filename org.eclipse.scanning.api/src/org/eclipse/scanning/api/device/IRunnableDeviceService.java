/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api.device;

import java.util.Collection;

import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionerService;

import uk.ac.diamond.osgi.services.ServiceProvider;


/**
 *
 * Anatomy of a CPU scan (non-malcolm)
 *
 *  <br>
 *&nbsp;_________<br>
 *_|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|________  collectData() Tell detector to collect<br>
 *&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;_________<br>
 *_________|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|_  readout() Tell detector to readout<br>
 *&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;_______<br>
 *_________|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|___  moveTo()  Scannables move motors to new position<br>
 * <br>
 *<br>
 * A MalcolmDevice is also an IScanner which may operate with an arbitrary model, usually driving hardware.<br>
 * <br>
 * <usage><code>
 * IParserService pservice = ...// OSGi<br>
 * <br>
 * // Parse the scan command, throws an exception<br>
 * IParserResult<StepModel> parser = pservice.createParser(...)<br>
 * // e.g. "scan x 0 5 0.1 analyser"<br>
 * <br>
 * // Now use the parser to create a generator<br>
 * IPointGeneratorService gservice = ...// OSGi<br>
 * StepModel model = parser.getModel("x");<br>
 * IPointGenerator<?>    gen = gservice.createGenerator(model)<br>
 * <br>
 * // Now scan the point iterator<br>
 * IDeviceService sservice = ...// OSGi<br>
 * IRunnableDevice<ScanModel> scanner = sservice.createScanner(...);
 * scanner.configure(model);
 * scanner.run();
 *
 * </code></usage>
 *
 * <img src="./doc/device_state.png" />
 *
 * @author Matthew Gerring
 *
 */
public interface IRunnableDeviceService extends IPositionerService {

	/**
	 * Used to register a device. This is required so that spring may create
	 * detectors and call the register method by telling the detector to register
	 * itself.
	 *
	 * @param device
	 */
	<T> void register(IRunnableDevice<T> device);

	/**
	 * Get a runnable device by name. If the device was created by spring it may need configuring
	 * before use. If the device was added to the service after a createRunnableDevice(...) call,
	 * it will already be configured.
	 *
	 * @param name
	 * @return
	 * @throws ScanningException
	 */
	<T> IRunnableDevice<T> getRunnableDevice(String name) throws ScanningException;


	/**
	 * Get a runnable device by name. If the device was created by spring it may need configuring
	 * before use. If the device was added to the service after a createRunnableDevice(...) call,
	 * it will already be configured.
	 *
	 * @param name
	 * @param publisher used for a particular run of the device. This must be set with care as only one publisher may be active on a device at a time.
	 * @return
	 * @throws ScanningException
	 */
	<T> IRunnableDevice<T> getRunnableDevice(String name, IPublisher<ScanBean> publisher) throws ScanningException;

	/**
	 * A list of the current named runnable devices which may be retrieved and configured.
	 * @return
	 * @throws ScanningException
	 */
    Collection<String> getRunnableDeviceNames() throws ScanningException;

    /**
     * Get the service being used to connect this service to the underlying hardware devices.
     * @return
     * @deprecated use {@link ServiceProvider#getService(Class)} with {@code IScannableDeviceService.class} as the argument
     */
    @Deprecated(since = "GDA 9.33", forRemoval = true)
    IScannableDeviceService getDeviceConnectorService();

    /**
     * Get the information for all the runnable devices currently created.
     * Will not get device information that is potentially held on the device if the device is not alive.
     * @return
     */
	Collection<DeviceInformation<?>> getDeviceInformation() throws ScanningException;

    /**
     * Get the information for all the runnable devices currently created with a specific role.
     * Will not get device information that is potentially held on the device if the device is not alive.
     * @return
     */
	Collection<DeviceInformation<?>> getDeviceInformation(DeviceRole role) throws ScanningException;

    /**
     * Get the information for the named runnable device.
     * @return
     */
	DeviceInformation<?> getDeviceInformation(String name) throws ScanningException;

    /**
     * Get the information for all the runnable devices currently created.
     * Will attempt to get device information that is potentially held on the device even if the device is not alive.
     * @return
     */
	Collection<DeviceInformation<?>> getDeviceInformationIncludingNonAlive() throws ScanningException;

    /**
     * This is a convenience method for getting the currently active scanner.
     * It is useful if the scan is paused and it is required to seek the scan
     * to a new location from Jython. The returned scanner will be null unless
     * a scan is currently running, therefore it is of limited usage normally.
     *
     * @return current actively scanning device.
     */
	default <T> IRunnableDevice<T> getActiveScanner() {
		throw new IllegalArgumentException("The get active scanner method is not availble!");
	}

}
