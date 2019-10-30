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
package org.eclipse.scanning.api.malcolm;

import java.util.List;

import org.eclipse.scanning.api.IValidator;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.malcolm.event.IMalcolmEventListener;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.scan.ScanningException;


/**
 * This device talks to the middleware layer state machine for controlling
 * the scan. If working inside Diamond there is a wiki here:
 * http://confluence.diamond.ac.uk/display/MAP/WP4+Middlelayer+Design+Specification
 *
 * A version of this design is also in Malcolm.pdf in this package.
 *
 * The name malcolm is chosen for the middleware layer between the Java OSGi server and
 * the hardware - inspired by the sitcom 'Malcolm in the Middle'
 * https://en.wikipedia.org/wiki/Malcolm_in_the_Middle
 *
 * This interface attempts to mirror how the hardware in the python looks, hence the
 * fact that it does not look very 'Java'. So instead of setPaused(boolean) we have
 * pause() and resume().
 *
 * Usage:
 * <code>
 <p>
        IRunnableDeviceService service = ... // OSGi service<br>
        IMalcolmConnection        connection = service.createConnection("tcp://127.0.0.1:7800");<br>
<br>
		IMalcolmDevice zebra =  service.getRunnableDevice("zebra");<br>
	    Map<String, Object> config = new HashMap<String,Object>(2);<br>
		config.put("PC_BIT_CAP", 1);<br>
		config.put("PC_TSPRE", "ms");<br>
		<br>
		zebra.configure(config);<br>
		zebra.run(); // blocks until finished<br>
		<br>
		final State state = zebra.getState();<br>
        // ... We did something!<br>
</p>
</code>

 * <img src="./doc/device_state.png" />

 * @author Matthew Gerring
 * @param <T> the type of the model for this malcolm device
 *
 */
public interface IMalcolmDevice extends IRunnableEventDevice<IMalcolmModel>, IValidator<IMalcolmModel> {

	/**
	 * Initializes the connection to the actual malcolm device. This methods must be called before
	 * the malcolm device can be used.
	 * @throws MalcolmDeviceException if the malcolm device could not be initialized
	 */
	public void initialize() throws MalcolmDeviceException;

	/**
	 * Attempts to determine if the device is locked doing something like a configure or a run.
	 *
	 * @return true if not in locked state, otherwise false.
	 */
	public boolean isLocked() throws MalcolmDeviceException;

	/**
	 * Returns the axes that this malcolm device can move.
	 * @return the axes this malcolm device can move, never <code>null</code>
	 * @throws ScanningException
	 */
	public List<String> getAvailableAxes() throws ScanningException;

	/**
	 * Set the point generator for the malcolm device.
	 * @param pointGenerator point generator
	 */
	public void setPointGenerator(IPointGenerator<?> pointGenerator);

	/**
	 * Get the point generator that this malcolm device has been configured with
	 * @return
	 */
	public IPointGenerator<?> getPointGenerator();

	/**
	 * Set the directory where malcolm will write its h5 files to. The directory should exist at
	 * the point that the malcolm device is configured, malcolm is not responsible for creating it.
	 */
	public void setOutputDir(String fileDir);

	/**
	 * Get the directory where malcolm will write its h5 files to.
	 */
	public String getOutputDir();

	/**
	 * Add a listener to malcolm changes.
	 *
	 * @param listener
	 */
	public void addMalcolmListener(IMalcolmEventListener listener);

	/**
	 * Remove a listener to malcolm changes.
     *
	 * @param listener
	 */
	public void removeMalcolmListener(IMalcolmEventListener listener);

	/**
	 * Returns a {@link MalcolmTable} object describing the datasets that this malcolm device is configured to create
	 * during a scan. This method should only be called after {@link #configure(Object)} has been called
	 * on this malcolm device.
	 * @return a table of the datasets created by this malcolm device
	 * @throws MalcolmDeviceException if the datasets table could not be returned for any reason
	 */
	public MalcolmTable getDatasets() throws MalcolmDeviceException;

	/**
	 * Disposes of the malcolm device causing it to disconnect from the underlying malcolm device.
	 *
	 * @throws MalcolmDeviceException
	 */
	public void dispose() throws MalcolmDeviceException;

}
