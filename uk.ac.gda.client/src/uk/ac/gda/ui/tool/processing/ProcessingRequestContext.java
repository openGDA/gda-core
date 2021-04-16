/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.tool.processing;

import java.net.URL;
import java.util.List;

/**
 * Describes the configuration for a {@link ProcessingRequestComposite} element.
 *
 * <p>
 * This configuration defines the behaviour a {@link ProcessingRequestRow} inside the {@link ProcessingRequestComposite}.
 * For the defined {@link ProcessingRequestKey}, the behaviour is centered around the class properties
 * <ul>
 * <li>
 * <i>configurationSource</i>: a URL defining the source for valid processing files
 * </li>
 * <li>
 * <i>defaultConfiguration</i>: a URL defining the default processing files
 * </li>
 * <li>
 * <i>mandatory</i>: appends a {@code ProcessingRequestRow} if the {@link #getDefaultConfiguration()} is not {@code null}.
 * </li>
 * </ul>
 * </p>
 * Decouple the {@link ProcessingRequestKey} definitions from where are located the associated configuration.
 * Used by {@code ProcessingRequestComposite}
 *
 * @author Maurizio Nagni
 */
public class ProcessingRequestContext {

	/**
	 * The processing request key for the client
	 */
	private final ProcessingRequestKey key;
	/**
	 * The source for the associated processing key
	 */
	private final URL configurationSource;
	/**
	 * The default processing request configuration for the associated processing key
	 */
	private final List<URL> defaultConfiguration;

	/**
	 * Indicates that this processing context has to be part of every ProcessingRequest.
	 */
	private final boolean mandatory;

	/**
	 * Creates an element for the {@code ProcessingRequestComposite} constructor.
	 * @param key The processing request key for the client
	 * @param configurationSource The source for the associated processing key
	 * @param defaultConfiguration  The default processing request configuration
	 * @param mandatory if {@code true} indicates that this processing context has to be part of every ProcessingRequest.
	 * If {@link #getDefaultConfiguration()} is {@code null} this property is ignored.
	 */
	public ProcessingRequestContext(ProcessingRequestKey key, URL configurationSource, List<URL> defaultConfiguration, boolean mandatory) {
		super();
		this.key = key;
		this.configurationSource = configurationSource;
		this.defaultConfiguration = defaultConfiguration;
		this.mandatory = mandatory;
	}

	/**
	 * The key element for the gui.
	 * @return the processing request key
	 */
	public ProcessingRequestKey getKey() {
		return key;
	}

	/**
	 * The source associated with {@link #getKey()}
	 * @return the source location
	 */
	public URL getConfigurationSource() {
		return configurationSource;
	}

	/**
	 * The default processing request configuration for the associated processing key
	 * @return The default processing request configuration, otherwise {@code null}
	 */
	public List<URL> getDefaultConfiguration() {
		return defaultConfiguration;
	}

	/**
	 * Indicates if this processing context has to be part of every ProcessingRequest.
	 * If {@link #getDefaultConfiguration()} is {@code null} this property is ignored.
	 *
	 * @return if this processing context is mandatory
	 */
	public boolean isMandatory() {
		return mandatory;
	}
}
