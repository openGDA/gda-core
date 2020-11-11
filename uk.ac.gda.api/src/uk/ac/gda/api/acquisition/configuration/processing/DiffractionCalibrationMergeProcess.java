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
 * Diffraction Calibration file merge
 *
 *  @author Maurizio Nagni
 */
@JsonTypeName("diffractionCalibrationMerge")
@JsonDeserialize(builder = DiffractionCalibrationMergeProcess.Builder.class)
public class DiffractionCalibrationMergeProcess implements ProcessingRequestPair<URL>{

	private static final String KEY = "diffractionCalibrationMerge";
	private final List<URL> calibrationFiles;

	private DiffractionCalibrationMergeProcess(List<URL> processingFiles) {
		this.calibrationFiles = processingFiles;
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
	 * The diffraction calibration files
	 * @return the calibration files
	 */
	@Override
	public List<URL> getValue() {
		return Collections.unmodifiableList(calibrationFiles);
	}

	@JsonPOJOBuilder
	public static class Builder {
		private final List<URL> calibrationFiles = new ArrayList<>();

	    public Builder withValue(List<URL> processingFiles) {
	    	this.calibrationFiles.clear();
	        this.calibrationFiles.addAll(processingFiles);
	        return this;
	    }

	    public DiffractionCalibrationMergeProcess build() {
	        return new DiffractionCalibrationMergeProcess(calibrationFiles);
	    }
	}

}
