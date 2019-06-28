package uk.ac.diamond.daq.mapping.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scanning.api.device.models.ClusterProcessingModel;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.ui.IStageScanConfiguration;

import uk.ac.diamond.daq.mapping.api.ConfigWrapper;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.ISampleMetadata;
import uk.ac.diamond.daq.mapping.api.IScanDefinition;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.api.IScriptFiles;

public class MappingExperimentBean implements IMappingExperimentBean {

	private SimpleSampleMetadata sampleMetadata;
	private List<IScanModelWrapper<IDetectorModel>> detectorParameters = null;
	private List<IScanModelWrapper<ClusterProcessingModel>> clusterProcessingConfiguration = null;
	private Map<String, Object> beamlineConfiguration = null;
	private IScanDefinition scanDefinition;
	private IScriptFiles scriptFiles = null;
	private Set<String> perScanMonitorNames = null;
	private Set<String> perPointMonitorNames = null;
	private MappingStageInfo stageInfoSnapshot;
	private List<ConfigWrapper> processingConfigs;

	public MappingExperimentBean() {
		sampleMetadata = new SimpleSampleMetadata();
		scanDefinition = new MappingScanDefinition();
		processingConfigs = new ArrayList<ConfigWrapper>();
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
	public List<IScanModelWrapper<IDetectorModel>> getDetectorParameters() {
		return detectorParameters;
	}

	@Override
	public void setDetectorParameters(List<IScanModelWrapper<IDetectorModel>> detectorParameters) {
		this.detectorParameters = detectorParameters;
	}

	@Override
	public Set<String> getPerScanMonitorNames() {
		return perScanMonitorNames;
	}

	@Override
	public void setPerScanMonitorNames(Set<String> perScanMonitorNames) {
		this.perScanMonitorNames = perScanMonitorNames;
	}

	@Override
	public Set<String> getPerPointMonitorNames() {
		return this.perPointMonitorNames;
	}

	@Override
	public void setPerPointMonitorNames(Set<String> perPointMonitorNames) {
		this.perPointMonitorNames = perPointMonitorNames;
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
	public IScriptFiles getScriptFiles() {
		return scriptFiles;
	}

	@Override
	public void setScriptFiles(IScriptFiles scriptFiles) {
		this.scriptFiles = scriptFiles;
	}

	@Override
	public IStageScanConfiguration getStageInfoSnapshot() {
		if (stageInfoSnapshot == null) {
			stageInfoSnapshot = new MappingStageInfo();
		}
		return stageInfoSnapshot;
	}

	@Override
	public Map<String,Object> getProcessingRequest() {

		Map<String,Object> request = new HashMap<String, Object>();

		for (ConfigWrapper w : processingConfigs) {

			if (!w.isActive()) continue;

			if (request.containsKey(w.getAppName())) {
				((List<String>)request.get(w.getAppName())).add(w.getPathToConfig());
			} else {
				List<String> l = new ArrayList<String>(Arrays.asList(w.getPathToConfig()));
				request.put(w.getAppName(), l);
			}
		}

		return request;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((beamlineConfiguration == null) ? 0 : beamlineConfiguration.hashCode());
		result = prime * result
				+ ((clusterProcessingConfiguration == null) ? 0 : clusterProcessingConfiguration.hashCode());
		result = prime * result + ((detectorParameters == null) ? 0 : detectorParameters.hashCode());
		result = prime * result + ((perPointMonitorNames == null) ? 0 : perPointMonitorNames.hashCode());
		result = prime * result + ((perScanMonitorNames == null) ? 0 : perScanMonitorNames.hashCode());
		result = prime * result + ((sampleMetadata == null) ? 0 : sampleMetadata.hashCode());
		result = prime * result + ((scanDefinition == null) ? 0 : scanDefinition.hashCode());
		result = prime * result + ((scriptFiles == null) ? 0 : scriptFiles.hashCode());
		result = prime * result + ((stageInfoSnapshot == null) ? 0 : stageInfoSnapshot.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MappingExperimentBean other = (MappingExperimentBean) obj;
		if (beamlineConfiguration == null) {
			if (other.beamlineConfiguration != null) {
				return false;
			}
		} else if (!beamlineConfiguration.equals(other.beamlineConfiguration)) {
			return false;
		}
		if (clusterProcessingConfiguration == null) {
			if (other.clusterProcessingConfiguration != null) {
				return false;
			}
		} else if (!clusterProcessingConfiguration.equals(other.clusterProcessingConfiguration)) {
			return false;
		}
		if (detectorParameters == null) {
			if (other.detectorParameters != null) {
				return false;
			}
		} else if (!detectorParameters.equals(other.detectorParameters)) {
			return false;
		}
		if (perPointMonitorNames == null) {
			if (other.perPointMonitorNames != null) {
				return false;
			}
		} else if (!perPointMonitorNames.equals(other.perPointMonitorNames)) {
			return false;
		}
		if (perScanMonitorNames == null) {
			if (other.perScanMonitorNames != null) {
				return false;
			}
		} else if (!perScanMonitorNames.equals(other.perScanMonitorNames)) {
			return false;
		}
		if (sampleMetadata == null) {
			if (other.sampleMetadata != null) {
				return false;
			}
		} else if (!sampleMetadata.equals(other.sampleMetadata)) {
			return false;
		}
		if (scanDefinition == null) {
			if (other.scanDefinition != null) {
				return false;
			}
		} else if (!scanDefinition.equals(other.scanDefinition)) {
			return false;
		}
		if (scriptFiles == null) {
			if (other.scriptFiles != null) {
				return false;
			}
		} else if (!scriptFiles.equals(other.scriptFiles)) {
			return false;
		}
		if (stageInfoSnapshot == null) {
			if (other.stageInfoSnapshot != null) {
				return false;
			}
		} else if (!stageInfoSnapshot.equals(other.stageInfoSnapshot)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "MappingExperimentBean [sampleMetadata=" + sampleMetadata + ", detectorParameters=" + detectorParameters
				+ ", clusterProcessingConfiguration=" + clusterProcessingConfiguration + ", beamlineConfiguration="
				+ beamlineConfiguration + ", scanDefinition=" + scanDefinition + ", scriptFiles=" + scriptFiles
				+ ", perScanMonitorNames=" + perScanMonitorNames + ", perPointMonitorNames=" + perPointMonitorNames
				+ "]";
	}

	@Override
	public List<ConfigWrapper> getProcessingConfigs() {
		return processingConfigs;
	}

	@Override
	public void setProcessingConfigs(List<ConfigWrapper> processingConfigs) {
		this.processingConfigs =processingConfigs;
	}

	@Override
	public void addProcessingRequest(ConfigWrapper wrapper) {
		if (wrapper == null) return;
		processingConfigs.add(wrapper);
	}
}
