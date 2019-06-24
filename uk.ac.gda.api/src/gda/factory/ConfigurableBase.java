/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class giving a default implementation of the functions in {@link gda.factory.Configurable}
 * <p>
 * A default implementation of {@link #configure()} is provided for objects that do not require explicit configuration
 * but which implement Configurable or (more often) an interface derived from Configurable, for example dummy devices
 * which must implement the same interface as their live equivalents.
 *
 * @since GDA 9.8
 */
public abstract class ConfigurableBase implements Configurable {
	private static final Logger logger = LoggerFactory.getLogger(ConfigurableBase.class);

	private boolean configured = false;

	/**
	 * Set the configured state of the object
	 * <p>
	 * Subclasses will typically call this function with parameter <code>true</code> at the end of their implementation
	 * of {@link #configure()}, and with <code>false</code> when closing the connection to a device and/or when starting
	 * to reconfigure it.
	 *
	 * @param configured
	 *            The configured state of the object (see comment above).
	 */
	protected void setConfigured(boolean configured) {
		this.configured = configured;
	}

	/**
	 * Default implementation for classes that do not have to do any specific configuration.<br>
	 * Classes that do their own configuration should *not* call this superclass function, as it may cause the object to
	 * appear configured before it really is.
	 */
	@Override
	public void configure() throws FactoryException {
		configured = true;
	}

	@Override
	public boolean isConfigured() {
		return configured;
	}

	@Override
	public void reconfigure() throws FactoryException {
		logger.debug("Empty reconfigure() called");
	}

	@Override
	public boolean isConfigureAtStartup() {
		return true;
	}

}
