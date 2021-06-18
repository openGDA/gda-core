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

package uk.ac.gda.ui.tool.processing.keys;

import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestBuilder;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.processing.ProcessingRequestComposite;
import uk.ac.gda.ui.tool.processing.context.ProcessingRequestContext;

/**
 * Allows the GUI to build a row for a specific {@link ProcessingRequestContext}
 *
 * @param <T> the document type managed by {@code ProcessingRequestContext}
 *
 * @author Maurizio Nagni
 *
 * @see ProcessingRequestComposite
 */
public class ProcessingRequestKey<T> {

	private final Class<T> documentClass;
	private final String key;
	private final ClientMessages label;
	private final ClientMessages tooltip;
	private final ProcessingRequestBuilder<T> builder;

	ProcessingRequestKey(Class<T> documentClass, String key, ClientMessages label, ClientMessages tooltip,
			ProcessingRequestBuilder<T> builder) {
		super();
		this.documentClass = documentClass;
		this.key = key;
		this.label = label;
		this.tooltip = tooltip;
		this.builder = builder;
	}

	/**
	 * The document class managed by the {@code ProcessingRequestContext}
	 * @return the document class
	 */
	public Class<T> getDocumentClass() {
		return documentClass;
	}

	/**
	 * The processing context key {@code ProcessingRequestContext#getKey()}
	 * @return the process key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * A human readable label for the {@code ProcessingRequestContext}
	 * @return a client message
	 */
	public ClientMessages getLabel() {
		return label;
	}

	/**
	 * A human readable description for the {@code ProcessingRequestContext}
	 * @return a client message
	 */
	public ClientMessages getTooltip() {
		return tooltip;
	}

	/**
	 * Builds the represented {@code ProcessingRequestContext}
	 * @return a builder instance
	 */
	public ProcessingRequestBuilder<T> getBuilder() {
		return builder;
	}

}