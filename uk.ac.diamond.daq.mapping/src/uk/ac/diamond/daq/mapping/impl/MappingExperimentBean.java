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

}
