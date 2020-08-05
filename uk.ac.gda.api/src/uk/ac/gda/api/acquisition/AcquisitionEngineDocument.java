/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.api.acquisition;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Identifies an acquisition engine.
 *
 * <p>
 * The acquisition engine is where the acquisition is interpreted and executed. The {@link AcquisitionEngineType} is not
 * strictly necessary because any compliant acquisition engine should be able to parse the {@link Acquisition}. The
 * {@link AcquisitionEngineType} allows parsers to identify an adapter for compatibility with the existing engines.
 * </p>
 *
 * @author Maurizio Nagni
 */
@JsonDeserialize(builder = AcquisitionEngineDocument.Builder.class)
public class AcquisitionEngineDocument {
	/**
	 * Classifies an acquisition engine
	 */
	public enum AcquisitionEngineType {
		/**
		 * Identifies a Malcolm engine. The {@link AcquisitionEngineDocument#getId()} returns the PV where Malcom is
		 * available
		 */
		MALCOLM,
		/**
		 * Identifies a script engine. The {@link AcquisitionEngineDocument#getId()} returns the path to the python script which
		 * takes this {@code Acquisition} as input
		 */
		SCRIPT,
		/**
		 * Identifies a service engine. The {@link AcquisitionEngineDocument#getId()} return the endpoint, as URL, where
		 * {@code POST} this {@code Acquisition}
		 */
		SERVICE
	}

	private final AcquisitionEngineType type;

	private final String id;

	private AcquisitionEngineDocument(String id, AcquisitionEngineType type) {
		this.type = type;
		this.id = id;
	}

	/**
	 * Clones an existing {@code AcquisitionEngine}
	 * @param acquisitionEngine
	 */
	public AcquisitionEngineDocument(AcquisitionEngineDocument acquisitionEngine) {
		this(acquisitionEngine.getId(), acquisitionEngine.getType());
	}

	/**
	 * The Acquisition engine type associated with this acquisition
	 *
	 * @return an engine {@code type}, otherwise {@code null} if the selection of a complaint acquisition engine should
	 *         delegated to an external controller
	 */
	public AcquisitionEngineType getType() {
		return type;
	}

	/**
	 * Identifies the acquisition id. Its meaning depends on the specific {@link AcquisitionEngineType}
	 *
	 * @return an acquisition {@code id}, otherwise {@code null} if {@link #getType()} returns {@code null}
	 */
	public String getId() {
		return id;
	}

	@JsonPOJOBuilder
	public static class Builder {
		private AcquisitionEngineType type;
		private String id;

		public Builder withType(AcquisitionEngineType type) {
			this.type = type;
			return this;
		}

		public Builder withId(String id) {
			this.id = id;
			return this;
		}

		public AcquisitionEngineDocument build() {
			if (type == null) {
				id = null;
			}
			return new AcquisitionEngineDocument(id, type);
		}
	}
}
