package uk.ac.diamond.daq.mapping.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;

import uk.ac.diamond.daq.mapping.api.IBeamlineConfiguration;
import uk.ac.diamond.daq.mapping.api.IDetectorModelWrapper;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.ISampleMetadata;
import uk.ac.diamond.daq.mapping.api.IScanDefinition;
import uk.ac.diamond.daq.mapping.api.IScanPathModelWrapper;

public class ExampleMappingExperimentBean implements IMappingExperimentBean {

	private ExampleSampleMetadata sampleMetadata;
	private List<IDetectorModelWrapper> detectorParameters;
	private ExampleBeamlineConfigurationImpl beamlineConfiguration;
	private IScanDefinition scanDefinition;

	public ExampleMappingExperimentBean() {
		// FIXME This is currently hardcoded for each detector and extra scan axis required. This needs to be changed to
		// be configurable on a per beamline basis

		detectorParameters = new ArrayList<>();
		MandelbrotModel mandelbrotModel = new MandelbrotModel();
		detectorParameters.add(new DetectorModelWrapper("mandelbrot_detector", mandelbrotModel));

		sampleMetadata = new ExampleSampleMetadata();
		beamlineConfiguration = new ExampleBeamlineConfigurationImpl();

		scanDefinition = new ExampleScanDefinition();
		IScanPathModelWrapper energyAxis = new ScanPathModelWrapper("energy", null, false);
		scanDefinition.setOuterScannables(Arrays.asList(energyAxis));
	}

	@Override
	public ExampleSampleMetadata getSampleMetadata() {
		return sampleMetadata;
	}

	@Override
	public void setSampleMetadata(ISampleMetadata sampleMetadata) {
		this.sampleMetadata = (ExampleSampleMetadata) sampleMetadata;
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
	public ExampleBeamlineConfigurationImpl getBeamlineConfiguration() {
		return beamlineConfiguration;
	}

	@Override
	public void setBeamlineConfiguration(IBeamlineConfiguration beamlineConfiguration) {
		this.beamlineConfiguration = (ExampleBeamlineConfigurationImpl) beamlineConfiguration;
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
}
