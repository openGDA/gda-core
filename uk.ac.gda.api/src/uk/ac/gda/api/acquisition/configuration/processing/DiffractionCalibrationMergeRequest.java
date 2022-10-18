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

import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;

/**
 * Diffraction Calibration file merge
 *
 *  @author Maurizio Nagni
 *
 *  @see <a href="https://confluence.diamond.ac.uk/display/DIAD/Diffraction+Calibration+Merge+Processing+Request">Diffraction Calibration Merge Processing Request</a>
 */
@JsonTypeName("diffractionCalibrationMerge")
@JsonDeserialize(builder = DiffractionCalibrationMergeRequest.Builder.class)
public class DiffractionCalibrationMergeRequest implements ProcessingRequestPair<URL>{

	public static final String KEY = "diffractionCalibrationMerge";
	private final List<URL> calibrationFiles;

	private DiffractionCalibrationMergeRequest(List<URL> processingFiles) {
		this.calibrationFiles = processingFiles;
	}

	/**
	 * The processing request key
	 * @return the identifier for this process
	 */
	@Override
	public String getKey() {
		return KEY;
	}

	/**
	 * The diffraction calibration file
	 */
	@Override
	public List<URL> getValue() {
		return Collections.unmodifiableList(calibrationFiles);
	}

	@JsonPOJOBuilder
	public static class Builder implements ProcessingRequestBuilder<URL> {

		private static final DeprecationLogger logger = DeprecationLogger.getLogger(DiffractionCalibrationMergeRequest.Builder.class);
		private final List<URL> calibrationFiles = new ArrayList<>();

	    /**
	     * A list containing a single calibration file url
	     */
	    @Override
		public Builder withValue(List<URL> processingFiles) {
	    	this.calibrationFiles.clear();
	        this.calibrationFiles.addAll(processingFiles);
	        return this;
	    }

	    /**
	     * @deprecated The 'key' property is currently serialised but set internally.
	     * This method is only here to satisfy the deserialiser
	     */
	    @Override
	    @Deprecated(since = "9.20")
	    public Builder withKey(String key) {
	    	// TODO: Refactor this?
	    	logger.deprecatedMethod("withKey(String)");
	    	return this;
	    }

	    @Override
		public DiffractionCalibrationMergeRequest build() {
	        return new DiffractionCalibrationMergeRequest(calibrationFiles);
	    }
	}

}
