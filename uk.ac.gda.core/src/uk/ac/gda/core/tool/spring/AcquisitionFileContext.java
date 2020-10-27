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

package uk.ac.gda.core.tool.spring;

import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Defines the application file structure as <a href="https://confluence.diamond.ac.uk/display/DIAD/File+System">detailed in Confluence</a>
 *
 * At the moment contains only references to essential URLs. In future will contain reference to the whole beamline
 * configuration (stages, cameras, other)
 *
 * @author Maurizio Nagni
 */
@Component
public class AcquisitionFileContext {

	@Autowired
	private DiffractionFileContext diffractionContext;

	@Autowired
	private TomographyFileContext tomographyContext;

	@Autowired
	private ExperimentFileContext experimentContext;

	@Deprecated
	public enum ContextFile {
		ACQUISITION_CONFIGURATION_DIRECTORY,
		DIFFRACTION_CALIBRATION_DIRECTORY,
		DIFFRACTION_CALIBRATION,
		ACQUISITION_EXPERIMENT_DIRECTORY
	}

	/**
	 * Returns the location associated with the {@code contextFile}.
	 *
	 * @param contextFile
	 * @return the resource URL, otherwise {@code null} if nothing is found.
	 * @deprecated use instead the same method from {@link #getDiffractionContext()}, {@link #getTomographyContext()}, {@link #getExperimentContext()}
	 */
	@Deprecated
	public final URL getContextFile(ContextFile contextFile) {
		switch (contextFile) {
		case ACQUISITION_CONFIGURATION_DIRECTORY:
			return diffractionContext.getContextFile(DiffractionContextFile.DIFFRACTION_CONFIGURATION_DIRECTORY);
		case DIFFRACTION_CALIBRATION_DIRECTORY:
			return diffractionContext.getContextFile(DiffractionContextFile.DIFFRACTION_CALIBRATION_DIRECTORY);
		case DIFFRACTION_CALIBRATION:
			return diffractionContext.getContextFile(DiffractionContextFile.DIFFRACTION_DEFAULT_CALIBRATION);
		case ACQUISITION_EXPERIMENT_DIRECTORY:
			return experimentContext.getContextFile(ExperimentContextFile.EXPERIMENTS_DIRECTORY);
		default:
			return null;
		}
	}

	/**
	 * Set a {@code URL} to be used as default calibration file for any diffraction.
	 * The file can then be retrieved using {@code getContextFile(DiffractionContextFile.DIFFRACTION_DEFAULT_CALIBRATION)};
	 * @param calibrationUrl
	 * @return {@code true} if the file exists and the operation succeeds, otherwise {@code false}
	 * @deprecated use {@code getDiffractionContext().putCalibrationFile(URL)}
	 */
	@Deprecated
	public boolean putCalibrationFile(URL calibrationUrl) {
		return diffractionContext.putCalibrationInContext(calibrationUrl);
	}

	/**
	 * The diffraction file structure context
	 * @return the diffraction file context
	 */
	public DiffractionFileContext getDiffractionContext() {
		return diffractionContext;
	}

	/**
	 * The tomography file structure context
	 * @return the tomography file context
	 */
	public TomographyFileContext getTomographyContext() {
		return tomographyContext;
	}

	/**
	 * The experiment file structure context
	 * @return the experient file context
	 */
	public ExperimentFileContext getExperimentContext() {
		return experimentContext;
	}
}
