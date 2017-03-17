package uk.ac.diamond.daq.mapping.impl;

import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;

import uk.ac.diamond.daq.mapping.api.IClusterProcessingModelWrapper;
import uk.ac.diamond.daq.mapping.api.IDetectorModelWrapper;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.ISampleMetadata;
import uk.ac.diamond.daq.mapping.api.IScanDefinition;
import uk.ac.diamond.daq.mapping.api.IScriptFiles;

public class MappingExperimentBean implements IMappingExperimentBean {

	private SimpleSampleMetadata sampleMetadata;
	private List<IDetectorModelWrapper> detectorParameters = null;
	private List<IClusterProcessingModelWrapper> clusterProcessingConfiguration = null;
	private Map<String, Object> beamlineConfiguration = null;
	private IScanDefinition scanDefinition;
	private IScriptFiles scriptFiles = null;

	public MappingExperimentBean() {
		sampleMetadata = new SimpleSampleMetadata();
		scanDefinition = new MappingScanDefinition();
	}

	@Override
	public SimpleSampleMetadata getSampleMetadata() {
		return sampleMetadata;
	}

	@Override
	public void setSampleMetadata(ISampleMetadata sampleMetadata) {
		this.sampleMetadata = (SimpleSampleMetadata) sampleMetadata;
	}

	@Override
	public IScanDefinition getScanDefinition() {
		return scanDefinition;
	}

	@Override
	public void setScanDefinition(IScanDefinition scanDefinition) {
		this.scanDefinition = scanDefinition;
	}

	@Override
	public List<IDetectorModelWrapper> getDetectorParameters() {
		return detectorParameters;
	}

	@Override
	public void setDetectorParameters(List<IDetectorModelWrapper> detectorParameters) {
		this.detectorParameters = detectorParameters;
	}

	@Override
	public Map<String, Object> getBeamlineConfiguration() {
		return beamlineConfiguration;
	}

	@Override
	public void setBeamlineConfiguration(Map<String, Object> beamlineConfiguration) {
		this.beamlineConfiguration = beamlineConfiguration;
	}

	@Override
	public List<IClusterProcessingModelWrapper> getClusterProcessingConfiguration() {
		return clusterProcessingConfiguration;
	}

	@Override
	public void setClusterProcessingConfiguration(List<IClusterProcessingModelWrapper> clusterProcessingConfiguration) {
		this.clusterProcessingConfiguration = clusterProcessingConfiguration;
	}

	@Override
	public List<IOperationModel> getPostProcessingConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPostProcessingConfiguration(List<IOperationModel> postProcessingConfiguration) {
		// TODO Auto-generated method stub
	}

	@Override
	public IScriptFiles getScriptFiles() {
		return scriptFiles;
	}

	@Override
	public void setScriptFiles(IScriptFiles scriptFiles) {
		this.scriptFiles = scriptFiles;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((beamlineConfiguration == null) ? 0 : beamlineConfiguration.hashCode());
		result = prime * result
				+ ((clusterProcessingConfiguration == null) ? 0 : clusterProcessingConfiguration.hashCode());
		result = prime * result + ((detectorParameters == null) ? 0 : detectorParameters.hashCode());
		result = prime * result + ((sampleMetadata == null) ? 0 : sampleMetadata.hashCode());
		result = prime * result + ((scanDefinition == null) ? 0 : scanDefinition.hashCode());
		result = prime * result + ((scriptFiles == null) ? 0 : scriptFiles.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MappingExperimentBean other = (MappingExperimentBean) obj;
		if (beamlineConfiguration == null) {
			if (other.beamlineConfiguration != null)
				return false;
		} else if (!beamlineConfiguration.equals(other.beamlineConfiguration))
			return false;
		if (clusterProcessingConfiguration == null) {
			if (other.clusterProcessingConfiguration != null)
				return false;
		} else if (!clusterProcessingConfiguration.equals(other.clusterProcessingConfiguration))
			return false;
		if (detectorParameters == null) {
			if (other.detectorParameters != null)
				return false;
		} else if (!detectorParameters.equals(other.detectorParameters))
			return false;
		if (sampleMetadata == null) {
			if (other.sampleMetadata != null)
				return false;
		} else if (!sampleMetadata.equals(other.sampleMetadata))
			return false;
		if (scanDefinition == null) {
			if (other.scanDefinition != null)
				return false;
		} else if (!scanDefinition.equals(other.scanDefinition))
			return false;
		if (scriptFiles == null) {
			if (other.scriptFiles != null)
				return false;
		} else if (!scriptFiles.equals(other.scriptFiles))
			return false;
		return true;
	}

}
