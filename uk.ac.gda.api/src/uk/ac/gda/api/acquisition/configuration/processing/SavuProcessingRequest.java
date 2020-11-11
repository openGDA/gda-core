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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonTypeName("savuProcessingRequest")
@JsonDeserialize(builder = SavuProcessingRequest.Builder.class)
public class SavuProcessingRequest implements ProcessingRequestPair<URL>{

	private static final String key = "savu";
	private final List<URL> processingFiles;

	private SavuProcessingRequest(List<URL> processingFiles) {
		this.processingFiles = processingFiles;
	}

	/**
	 * The processing request key
	 * @return the identifier for this process
	 */
	@Override
	public String getKey() {
		return key;
	}

	/**
	 * The savu processing files
	 * @return the processing files
	 */
	@Override
	public List<URL> getValue() {
		return Collections.unmodifiableList(processingFiles);
	}

	@JsonPOJOBuilder
	public static class Builder {
		private final List<URL> processingFiles = new ArrayList<>();

	    public Builder withValue(List<URL> processingFiles) {
	    	this.processingFiles.clear();
	        this.processingFiles.addAll(processingFiles);
	        return this;
	    }

	    public SavuProcessingRequest build() {
	        return new SavuProcessingRequest(processingFiles);
	    }
	}

}
