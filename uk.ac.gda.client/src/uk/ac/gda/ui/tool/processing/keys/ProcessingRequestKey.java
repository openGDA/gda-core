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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((builder == null) ? 0 : builder.hashCode());
		result = prime * result + ((documentClass == null) ? 0 : documentClass.hashCode());
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((tooltip == null) ? 0 : tooltip.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProcessingRequestKey<?> other = (ProcessingRequestKey<?>) obj;
		if (builder == null) {
			if (other.builder != null)
				return false;
		} else if (!builder.equals(other.builder))
			return false;
		if (documentClass == null) {
			if (other.documentClass != null)
				return false;
		} else if (!documentClass.equals(other.documentClass))
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (label != other.label)
			return false;
		if (tooltip != other.tooltip)
			return false;
		return true;
	}

}