/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device;

import java.util.Hashtable;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.ConditionallyConfigurable;
import gda.factory.FactoryException;
import gda.factory.Localizable;
import gda.jython.accesscontrol.MethodAccessProtected;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

/**
 * A base implementation for all devices
 */
public abstract class DeviceBase implements Device, ConditionallyConfigurable, Localizable {

	private static final Logger logger = LoggerFactory.getLogger(DeviceBase.class);

	private String name;

	private int protectionLevel = 1;

	private boolean local = false;

	private boolean configureAtStartup = true;

	private final ObservableComponent observableComponent = new ObservableComponent();

	private final Map<String, Object> attributes = new Hashtable<>();

	protected boolean configured = false;

	public DeviceBase() {
	}

	/**
	 * Get the name of the device
	 *
	 * @return Returns the name.
	 */
	@Override
	public String getName() {
		if (configured && (name == null || name.isEmpty())){
			logger.warn("getName() called on a device when the name has not been set. This may cause problems in the system and should be fixed.");
		}
		return name;
	}

	/**
	 * Set the name of the device
	 *
	 * @param name
	 *            The name to set.
	 */
	@Override
	@MethodAccessProtected(isProtected=true)
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Checks to see if the created object should be local to the server or whether a corba impl should be instantiated
	 * and placed on the name server.
	 *
	 * @return true for local only objects
	 */
	@Override
	public boolean isLocal() {
		return local;
	}

	@Override
	public int getProtectionLevel() throws DeviceException{
		return protectionLevel;
	}

	@Override
	public void setProtectionLevel(int permissionLevel) throws DeviceException {
		this.protectionLevel = permissionLevel;
	}

	/**
	 * Sets a flag to inform the server that the created object should be local to itself or whether a corba impl should
	 * be instantiated and placed on the name server.
	 *
	 * @param local
	 *            true if a local only implementation.
	 */
	@Override
	@MethodAccessProtected(isProtected=true)
	public void setLocal(boolean local) {
		this.local = local;
	}

	@Override
	public boolean isConfigureAtStartup() {
		return configureAtStartup;
	}

	/**
	 * Set a flag to inform the server whether the configure method should be called at startup.
	 *
	 * @param configureAtStartup
	 *            true to configure at startup.
	 */
	public void setConfigureAtStartup(boolean configureAtStartup) {
		this.configureAtStartup = configureAtStartup;
	}

	@Override
	public void reconfigure() throws FactoryException {
		// do nothing. its up to the sub-classes
	}

	/**
	 * Checks to see if the object is already configured.
	 *
	 * @return boolean value of whether the device has been configured or not
	 */
	public boolean isConfigured() {
		return configured;
	}

	/**
	 * @param configured
	 */
	public void setConfigured(boolean configured) {
		this.configured = configured;
	}

	@Override
	public void close() throws DeviceException {
		// do nothing. its up to the sub-classes
	}

	@Override
	@MethodAccessProtected(isProtected=true)
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		attributes.put(attributeName, value);
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		Object val = attributes.get(attributeName);
		if (val == null) {
			logger.debug("{}.getAttribute - unable to get value for '{}'", getName(), attributeName);
		}
		return val;
	}

	@Override
	public void addIObserver(IObserver observer) {
		observableComponent.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observableComponent.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	protected boolean isBeingObserved() {
		return observableComponent.IsBeingObserved();
	}
	/**
	 * Notify all observers on the list of the requested change.
	 *
	 * @param source the observed component
	 * @param arg the data to be sent to the observer.
	 */
	public void notifyIObservers(Object source, Object arg) {
		observableComponent.notifyIObservers(source, arg);
	}

}
