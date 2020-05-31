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

package uk.ac.diamond.daq.mapping.api.document.scanpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import gda.mscan.element.Mutator;
import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplateType;
import uk.ac.diamond.daq.mapping.api.document.deserializer.MutatorDeserializer;

/**
 * Describes a generic acquisition model. Classes extending this realise specific acquisition configuration.
 *
 * @author Maurizio Nagni
 */
@JsonDeserialize(builder = ScanpathDocument.Builder.class)
public class ScanpathDocument {

	protected final AcquisitionTemplateType modelDocument;

	@JsonDeserialize(keyUsing = MutatorDeserializer.class)
	protected final Map<Mutator, List<Number>> mutators;

	protected final List<ScannableTrackDocument> scannableTrackDocuments;

	public ScanpathDocument(AcquisitionTemplateType modelDocument, List<ScannableTrackDocument> scannableTrackDocuments,
			Map<Mutator, List<Number>> mutators) {
		this.modelDocument = modelDocument;
		this.scannableTrackDocuments = scannableTrackDocuments;
		this.mutators = mutators;
	}

	public AcquisitionTemplateType getModelDocument() {
		return modelDocument;
	}

	public List<ScannableTrackDocument> getScannableTrackDocuments() {
		return Collections.unmodifiableList(Optional.ofNullable(scannableTrackDocuments).orElse(new ArrayList<>()));
	}

	public Map<Mutator, List<Number>> getMutators() {
		return Collections.unmodifiableMap(Optional.ofNullable(mutators).orElse(new EnumMap<>(Mutator.class)));
	}

	@JsonPOJOBuilder
	public static class Builder {
		private AcquisitionTemplateType modelDocument;
		private List<ScannableTrackDocument> scannableTrackDocuments;
		@JsonDeserialize(keyUsing = MutatorDeserializer.class)
		private Map<Mutator, List<Number>> mutators;

		public static Builder cloneScanpathDocument(ScanpathDocument scanpathDocument) {
			Builder builder = new Builder();
			if (Objects.isNull(scanpathDocument)) {
				return builder;
			}
			builder.withModelDocument(scanpathDocument.modelDocument);
			builder.withScannableTrackDocuments(scanpathDocument.scannableTrackDocuments);
			builder.withMutators(scanpathDocument.mutators);
			return builder;
		}

		public Builder withModelDocument(AcquisitionTemplateType modelDocument) {
			this.modelDocument = modelDocument;
			return this;
		}

		public Builder withScannableTrackDocuments(List<ScannableTrackDocument> scannableTrackDocuments) {
			this.scannableTrackDocuments = scannableTrackDocuments;
			return this;
		}

		public Builder withMutators(Map<Mutator, List<Number>> mutators) {
			this.mutators = mutators;
			return this;
		}

		public Builder addMutator(Mutator key, List<Number> value) {
			if (mutators == null) {
				withMutators(new EnumMap<>(Mutator.class));
			}
			mutators.put(key, value);
			return this;
		}

		public Builder removeMutator(Mutator key) {
			Optional.ofNullable(mutators).ifPresent(m -> m.remove(key));
			return this;
		}

		public ScanpathDocument build() {
			return new ScanpathDocument(modelDocument, scannableTrackDocuments, mutators);
		}
	}
}
