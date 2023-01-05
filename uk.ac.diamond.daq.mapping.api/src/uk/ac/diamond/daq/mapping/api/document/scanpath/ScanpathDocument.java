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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import uk.ac.gda.api.acquisition.AcquisitionTemplateType;

/**
 * Describes a generic acquisition model. Classes extending this realise specific acquisition configuration.
 *
 * @author Maurizio Nagni
 */
@JsonDeserialize(builder = ScanpathDocument.Builder.class)
public class ScanpathDocument {

	protected final AcquisitionTemplateType modelDocument;

	protected final List<ScannableTrackDocument> scannableTrackDocuments;

	public ScanpathDocument(AcquisitionTemplateType modelDocument, List<ScannableTrackDocument> scannableTrackDocuments) {
		this.modelDocument = modelDocument;
		this.scannableTrackDocuments = scannableTrackDocuments;
	}

	public AcquisitionTemplateType getModelDocument() {
		return modelDocument;
	}

	public List<ScannableTrackDocument> getScannableTrackDocuments() {
		return Collections.unmodifiableList(Optional.ofNullable(scannableTrackDocuments).orElse(new ArrayList<>()));
	}

	@JsonPOJOBuilder
	public static class Builder {
		private AcquisitionTemplateType modelDocument;
		private List<ScannableTrackDocument> scannableTrackDocuments;

		public static Builder cloneScanpathDocument(ScanpathDocument scanpathDocument) {
			Builder builder = new Builder();
			if (Objects.isNull(scanpathDocument)) {
				return builder;
			}
			builder.withModelDocument(scanpathDocument.modelDocument);
			builder.withScannableTrackDocuments(scanpathDocument.scannableTrackDocuments);
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

		public ScanpathDocument build() {
			return new ScanpathDocument(modelDocument, scannableTrackDocuments);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(modelDocument, scannableTrackDocuments);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScanpathDocument other = (ScanpathDocument) obj;
		return modelDocument == other.modelDocument && Objects.equals(scannableTrackDocuments, other.scannableTrackDocuments);
	}

}
