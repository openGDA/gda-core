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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Represents a document identifier.
 *
 * @author Maurizio Nagni
 */
@JsonDeserialize(builder = DocumentBase.Builder.class)
public class DocumentBase implements Document {

	private final UUID uuid;
	private final String name;
	private final String description;

	/**
	 * A constructor used only by the inner builder class
	 * @param uuid
	 * @param name
	 * @param description
	 */
	private DocumentBase(UUID uuid, String name, String description) {
		super();
		this.uuid = uuid;
		this.name = name;
		this.description = description;
	}

	/**
	 * This constructor enable any class extending this to still create a new immutable object
	 *
	 * @param documentBase
	 *
	 * @see uk.ac.gda.common.entity.device.DeviceValue.Builder#build()
	 */
	protected DocumentBase(DocumentBase documentBase) {
		this.uuid = documentBase.getUuid();
		this.name = documentBase.getName();
		this.description = documentBase.getDescription();
	}

	@Override
	public UUID getUuid() {
		return uuid;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@JsonPOJOBuilder
    public static class Builder {
    	private UUID uuid;
    	private String name;
    	private String description;

    	public final Builder withUuid(UUID uuid) {
	        this.uuid = uuid;
	        return this;
	    }

    	public final Builder withName(String name) {
	        this.name = name;
	        return this;
	    }

    	public final Builder withDescription(String description) {
	        this.description = description;
	        return this;
	    }

		public DocumentBase build() {
			return new DocumentBase(uuid, name, description);
		}
    }
}
