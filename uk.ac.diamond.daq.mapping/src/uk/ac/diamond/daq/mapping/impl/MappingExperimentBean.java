package uk.ac.diamond.daq.mapping.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.ui.IStageScanConfiguration;

import uk.ac.diamond.daq.mapping.api.ConfigWrapper;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.ISampleMetadata;
import uk.ac.diamond.daq.mapping.api.IScanDefinition;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.api.IScriptFiles;
import uk.ac.diamond.daq.mapping.api.TemplateFileWrapper;

public class MappingExperimentBean implements IMappingExperimentBean {

	private long id = INVALID_ID;
	private String displayName;
	private SimpleSampleMetadata sampleMetadata;
	private List<IScanModelWrapper<IDetectorModel>> detectorParameters = null;
	private Map<String, Object> beamlineConfiguration = null;
	private IScanDefinition scanDefinition;
	private IScriptFiles scriptFiles = null;
	private Set<String> perScanMonitorNames = null;
	private Set<String> perPointMonitorNames = null;
	private MappingStageInfo stageInfoSnapshot;
	private List<ConfigWrapper> processingConfigs;
	private List<TemplateFileWrapper> templateFiles;
	private boolean useAlternativeDirectory = false;
	private String alternativeDirectory = "";

	/**
	 * Units for elements of the mapping region (e.g. xCentre, xRange) that the user has changed from the defaults.
	 * <p>
	 * Note that these are the units that are used for display purposes only. The values stored elsewhere in this class
	 * are in the canonical units of the relevant scannables.
	 */
	private Map<String, String> mappingRegionUnits;

	public MappingExperimentBean() {
		sampleMetadata = new SimpleSampleMetadata();
		scanDefinition = new MappingScanDefinition();
		processingConfigs = new ArrayList<>();
		templateFiles = new ArrayList<>();
		mappingRegionUnits = new HashMap<>();
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public void setId(long id) {
		this.id = id;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
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
	public Map<String, Collection<Object>> getProcessingRequest() {

		Map<String, Collection<Object>> request = new HashMap<>();

		for (ConfigWrapper w : processingConfigs) {

			if (!w.isActive()) continue;

			if (request.containsKey(w.getAppName())) {
				request.get(w.getAppName()).add(w.getConfigObject());
			} else {
				List<Object> l = new ArrayList<>(Arrays.asList(w.getConfigObject()));
				request.put(w.getAppName(), l);
			}
		}

		return request;
	}

	@Override
	public List<TemplateFileWrapper> getTemplateFiles() {
		return templateFiles;
	}

	@Override
	public void setTemplateFiles(List<TemplateFileWrapper> templateFiles) {
		this.templateFiles = templateFiles;
	}

	@Override
	public void addTemplateFile(TemplateFileWrapper templateFile) {
		if (templateFile != null) {
			templateFiles.add(templateFile);
		}
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

	@Override
	public boolean isUseAlternativeDirectory() {
		return useAlternativeDirectory;
	}

	@Override
	public void setUseAlternativeDirectory(boolean use) {
		useAlternativeDirectory = use;
	}

	@Override
	public String getAlternativeDirectory() {
		return alternativeDirectory;
	}

	@Override
	public void setAlternativeDirectory(String directory) {
		alternativeDirectory = directory;
	}

	@Override
	public Map<String, String> getMappingRegionUnits() {
		return mappingRegionUnits;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alternativeDirectory == null) ? 0 : alternativeDirectory.hashCode());
		result = prime * result + ((beamlineConfiguration == null) ? 0 : beamlineConfiguration.hashCode());
		result = prime * result + ((detectorParameters == null) ? 0 : detectorParameters.hashCode());
		result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((mappingRegionUnits == null) ? 0 : mappingRegionUnits.hashCode());
		result = prime * result + ((perPointMonitorNames == null) ? 0 : perPointMonitorNames.hashCode());
		result = prime * result + ((perScanMonitorNames == null) ? 0 : perScanMonitorNames.hashCode());
		result = prime * result + ((processingConfigs == null) ? 0 : processingConfigs.hashCode());
		result = prime * result + ((sampleMetadata == null) ? 0 : sampleMetadata.hashCode());
		result = prime * result + ((scanDefinition == null) ? 0 : scanDefinition.hashCode());
		result = prime * result + ((scriptFiles == null) ? 0 : scriptFiles.hashCode());
		result = prime * result + ((stageInfoSnapshot == null) ? 0 : stageInfoSnapshot.hashCode());
		result = prime * result + ((templateFiles == null) ? 0 : templateFiles.hashCode());
		result = prime * result + (useAlternativeDirectory ? 1231 : 1237);
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
		if (alternativeDirectory == null) {
			if (other.alternativeDirectory != null)
				return false;
		} else if (!alternativeDirectory.equals(other.alternativeDirectory))
			return false;
		if (beamlineConfiguration == null) {
			if (other.beamlineConfiguration != null)
				return false;
		} else if (!beamlineConfiguration.equals(other.beamlineConfiguration))
			return false;
		if (detectorParameters == null) {
			if (other.detectorParameters != null)
				return false;
		} else if (!detectorParameters.equals(other.detectorParameters))
			return false;
		if (displayName == null) {
			if (other.displayName != null)
				return false;
		} else if (!displayName.equals(other.displayName))
			return false;
		if (id != other.id)
			return false;
		if (mappingRegionUnits == null) {
			if (other.mappingRegionUnits != null)
				return false;
		} else if (!mappingRegionUnits.equals(other.mappingRegionUnits))
			return false;
		if (perPointMonitorNames == null) {
			if (other.perPointMonitorNames != null)
				return false;
		} else if (!perPointMonitorNames.equals(other.perPointMonitorNames))
			return false;
		if (perScanMonitorNames == null) {
			if (other.perScanMonitorNames != null)
				return false;
		} else if (!perScanMonitorNames.equals(other.perScanMonitorNames))
			return false;
		if (processingConfigs == null) {
			if (other.processingConfigs != null)
				return false;
		} else if (!processingConfigs.equals(other.processingConfigs))
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
		if (stageInfoSnapshot == null) {
			if (other.stageInfoSnapshot != null)
				return false;
		} else if (!stageInfoSnapshot.equals(other.stageInfoSnapshot))
			return false;
		if (templateFiles == null) {
			if (other.templateFiles != null)
				return false;
		} else if (!templateFiles.equals(other.templateFiles))
			return false;
		if (useAlternativeDirectory != other.useAlternativeDirectory)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MappingExperimentBean [id=" + id + ", displayName=" + displayName + ", sampleMetadata=" + sampleMetadata
				+ ", detectorParameters=" + detectorParameters + ", beamlineConfiguration=" + beamlineConfiguration
				+ ", scanDefinition=" + scanDefinition + ", scriptFiles=" + scriptFiles + ", perScanMonitorNames="
				+ perScanMonitorNames + ", perPointMonitorNames=" + perPointMonitorNames + ", stageInfoSnapshot="
				+ stageInfoSnapshot + ", processingConfigs=" + processingConfigs + ", templateFiles=" + templateFiles
				+ ", useAlternativeDirectory=" + useAlternativeDirectory + ", alternativeDirectory="
				+ alternativeDirectory + ", mappingRegionUnits=" + mappingRegionUnits + "]";
	}
}
