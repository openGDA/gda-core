/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;

/**
 * This interface should be implemented by any class which can represent a mapping type experiment
 */
public interface IMappingExperimentBean {

	/**
	 * Gets the sample metadata for the mapping scan e.g. name, chemical formula ...
	 *
	 * @return sampleMetadata
	 */
	public ISampleMetadata getSampleMetadata();

	/**
	 * Sets the sample metadata for the mapping scan e.g. name, chemical formula ...
	 *
	 * @param sampleMetadata
	 */
	public void setSampleMetadata(ISampleMetadata sampleMetadata);

	/**
	 * Gets the scan definition including the mapping scan region and any additional experimental parameters to be changed as part of the scan e.g. temperature,
	 * energy, pressure ...
	 *
	 * @return scanDefinition
	 */
	public IScanDefinition getScanDefinition();

	/**
	 * Sets the scan definition including the mapping scan region and any additional experimental parameters to be changed as part of the scan e.g. temperature,
	 * energy, pressure ...
	 *
	 * @param scanDefinition
	 */
	public void setScanDefinition(IScanDefinition scanDefinition);

	/**
	 * Gets the detector parameters to be used in the mapping scan. A <code>List</code> including elements for each of the enabled detectors. The elements will
	 * contain the individual detectors parameters e.g. exposure time, gain ...
	 *
	 * @return detectorParameters
	 */
	public List<IDetectorModelWrapper> getDetectorParameters();

	/**
	 * Sets the detector parameters to be used in the mapping scan. A <code>List</code> including elements for each of the enabled detectors. The elements will
	 * contain the individual detectors parameters e.g. exposure time, gain ...
	 *
	 * @param detectorParameters
	 */
	public void setDetectorParameters(List<IDetectorModelWrapper> detectorParameters);

	/**
	 * Gets the beamline configuration to be used in the mapping scan. e.g. monochromator energy, slit size, attenuation, ID gap, mirror positions ...
	 *
	 * @return beamlineConfiguration
	 */
	public IBeamlineConfiguration getBeamlineConfiguration();

	/**
	 * Sets the beamline configuration to be used in the mapping scan. e.g. monochromator energy, slit size, attenuation, ID gap, mirror positions ...
	 *
	 * @param beamlineConfiguration
	 */
	public void setBeamlineConfiguration(IBeamlineConfiguration beamlineConfiguration);

	/**
	 * Gets the post processing steps to be run live as part of the mapping scan (online analysis). A <code>List</code> to allow multiple processing steps to be
	 * defined. Can be null indicating no processing should be performed.
	 *
	 * @return postProcessingConfiguration
	 */
	public List<IOperationModel> getPostProcessingConfiguration();

	/**
	 * Sets the post processing steps to be run live as part of the mapping scan (online analysis). A <code>List</code> to allow multiple processing steps to be
	 * defined. Can be null indicating no processing should be performed.
	 *
	 * @param postProcessingConfiguration
	 */
	public void setPostProcessingConfiguration(List<IOperationModel> postProcessingConfiguration);

	/**
	 * Gets the script files to run before and after the mapping scan. Can be <code>null</code>
	 * to indicate no scripts should be run.
	 * @return script files
	 */
	public IScriptFiles getScriptFiles();

	/**
	 * Sets the script files to run before and after the mapping scan. Can be <code>null</code>
	 * to indicate no scripts should be run.
	 * @param scriptFiles script files
	 */
	public void setScriptFiles(IScriptFiles scriptFiles);

}
