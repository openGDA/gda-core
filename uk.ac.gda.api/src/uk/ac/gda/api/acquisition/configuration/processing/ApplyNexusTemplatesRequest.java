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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Request to apply a list of Nexus Template to a scan
 *
 *  @author Maurizio Nagni
 */
@JsonTypeName("applyNexusTemplates")
@JsonDeserialize(builder = ApplyNexusTemplatesRequest.Builder.class)
public class ApplyNexusTemplatesRequest implements ProcessingRequestPair<URL>{

	private static final String KEY = "applyNexusTemplates";
	private final List<URL> nexusTemplateFiles;

	private ApplyNexusTemplatesRequest(List<URL> nexusTemplateFiles) {
		this.nexusTemplateFiles = nexusTemplateFiles;
	}

	/**
	 * The processing request key
	 * @return the identifier for this process
	 */
	@Override
	@JsonIgnore
	public String getKey() {
		return KEY;
	}

	/**
	 * The nexus template files
	 */
	@Override
	public List<URL> getValue() {
		return Collections.unmodifiableList(nexusTemplateFiles);
	}

	@JsonPOJOBuilder
	public static class Builder {
		private final List<URL> nexusTemplateFiles = new ArrayList<>();

	    /**
	     * A list containing a nexus template files urls
	     */
	    public Builder withValue(List<URL> nexusTemplateFiles) {
	    	this.nexusTemplateFiles.clear();
	        this.nexusTemplateFiles.addAll(nexusTemplateFiles);
	        return this;
	    }

	    public ApplyNexusTemplatesRequest build() {
	        return new ApplyNexusTemplatesRequest(nexusTemplateFiles);
	    }
	}
}