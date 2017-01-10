/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.detectors.addetector;

import org.eclipse.scanning.api.device.IRunnableEventDevice;

import gda.factory.Findable;

/**
 * This class can be used to get a reference to an instance of a RunnableDevice from Jython.
 *
 * <P>
 *
 * RunnableDevices use the 'name' property to store the name of the device to be run, while the finder requires the
 * 'name' property to contain the name of the object, so we cannot apply the Finder interface to a RunnableDevice.
 *
 * <P>
 *
 * Example XML (@see {@link AreaDetectorRunnableDeviceProxy}):
<Pre>{@code
<bean id="pe1AreaDetectorRunnableDeviceProxy" class="uk.ac.diamond.daq.detectors.addetector.AreaDetectorRunnableDeviceProxy">
	<property name="name"        value="pe1AD"/>
</bean>

<bean id="pe1AreaDetectorRunnableDeviceProxyFinder" class="uk.ac.diamond.daq.detectors.addetector.RunnableDeviceFinder">
	<property name="name"        	value="pe1AreaDetectorRunnableDeviceProxyFinder"/>
	<property name="runnableDevice"	ref="pe1AreaDetectorRunnableDeviceProxy"/>
</bean>
}</Pre>
 * Example Jython
<Pre>
pe1AreaDetectorRunnableDeviceProxyFinder = finder.find("pe1AreaDetectorRunnableDeviceProxyFinder")
pe1AreaDetectorRunnableDeviceProxy = pe1AreaDetectorRunnableDeviceProxyFinder.getRunnableDevice()
pe1AreaDetectorRunnableDeviceProxy.setRunnableDevice(pe1JythonAreaDetectorRunnableDevice)
</Pre>
 *
 * @param <Model>
 */
public class RunnableDeviceFinder<Model> implements Findable {

	private String                      name;
	private IRunnableEventDevice<Model> runnableDevice;

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return Returns the runnableDevice.
	 */
	public IRunnableEventDevice<Model> getRunnableDevice() {
		return runnableDevice;
	}

	/**
	 * @param runnableDevice The runnableDevice to set.
	 */
	public void setRunnableDevice(IRunnableEventDevice<Model> runnableDevice) {
		this.runnableDevice = runnableDevice;
	}

}
