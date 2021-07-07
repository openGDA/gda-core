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

package uk.ac.gda.ui.tool.processing.context;

import java.util.List;

import uk.ac.gda.ui.tool.processing.ProcessingRequestComposite;
import uk.ac.gda.ui.tool.processing.keys.ProcessingRequestKey;

/**
 * Describes the configuration for a {@link ProcessingRequestComposite} element.
 *
 *
 * @author Maurizio Nagni
 */
public class ProcessingRequestContext<T> {

	/**
	 * The processing request key for the client
	 */
	private final ProcessingRequestKey<T> key;
	/**
	 * The source for the associated processing key
	 */
	private final T configurationSource;
	/**
	 * The default processing request configuration for the associated processing key
	 */
	private final List<T> defaultConfiguration;

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
	public ProcessingRequestContext(ProcessingRequestKey<T> key, T configurationSource, List<T> defaultConfiguration, boolean mandatory) {
		super();
		this.key = key;
		this.configurationSource = configurationSource;
		this.defaultConfiguration = defaultConfiguration;
		this.mandatory = mandatory;
	}

	/**
	 * Creates an element from an existing instance.
	 *
	 * @param defaultConfiguration  The default processing request configuration
	 * If {@link #getDefaultConfiguration()} is {@code null} this property is ignored.
	 */
	public ProcessingRequestContext(ProcessingRequestContext<T> processingRequest, List<T> defaultConfiguration) {
		super();
		this.key = processingRequest.getKey();
		this.configurationSource = processingRequest.getConfigurationSource();
		this.defaultConfiguration = defaultConfiguration;
		this.mandatory = processingRequest.isMandatory();
	}

	/**
	 * The key element for the gui.
	 * @return the processing request key
	 */
	public ProcessingRequestKey<T> getKey() {
		return key;
	}

	/**
	 * The source associated with {@link #getKey()}
	 * @return the source location
	 */
	public T getConfigurationSource() {
		return configurationSource;
	}

	/**
	 * The default processing request configuration for the associated processing key
	 * @return The default processing request configuration, otherwise {@code null}
	 */
	public List<T> getDefaultConfiguration() {
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
