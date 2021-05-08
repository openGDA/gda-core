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

package uk.ac.gda.api.acquisition.configuration.processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import uk.ac.gda.api.acquisition.parameters.DetectorDocument;

@JsonTypeName("frameCaptureRequest")
@JsonDeserialize(builder = FrameCaptureRequest.Builder.class)
public class FrameCaptureRequest implements ProcessingRequestPair<DetectorDocument> {

	public static final String KEY = "frameCapture";
	private final List<DetectorDocument> detectorDocuments;

	private FrameCaptureRequest(List<DetectorDocument> processingFiles) {
		this.detectorDocuments = processingFiles;
	}

	/**
	 * The processing request key
	 * @return the identifier for this process
	 */
	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public List<DetectorDocument> getValue() {
		return Collections.unmodifiableList(detectorDocuments);
	}

	@JsonPOJOBuilder
	public static class Builder implements ProcessingRequestBuilder<DetectorDocument> {
		private final List<DetectorDocument> detectorDocuments = new ArrayList<>();

	    @Override
		public Builder withValue(List<DetectorDocument> processingFiles) {
	    	this.detectorDocuments.clear();
	        this.detectorDocuments.addAll(processingFiles);
	        return this;
	    }

	    /**
	     * @deprecated The 'key' property is currently serialised but set internally.
	     * This method is only here to satisfy the deserialiser
	     */
	    @Override
	    @Deprecated(since = "9.20")
	    public Builder withKey(String key) {
	    	return this;
	    }

	    @Override
		public FrameCaptureRequest build() {
	        return new FrameCaptureRequest(Objects.requireNonNull(detectorDocuments));
	    }
	}

}
