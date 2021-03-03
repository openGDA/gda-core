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

package uk.ac.gda.common.entity;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Represents a document identifier.
 *
 * @author Maurizio Nagni
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        defaultImpl = DocumentBase.class
)
public interface Document {

	/**
	 * An universal unique identifier to discriminate between different documents
	 *
	 * @return the document unique identifier
	 */
	UUID getUuid();

	/**
	 * A human readable name for the document.
	 *
	 * @return the document name
	 */
	String getName();

	/**
	 * A short description for the document.
	 *
	 * @return the document description
	 */
	String getDescription();
}
