package org.opengda.detector.electronanalyser.api;

import java.util.List;
import java.util.Map;

import uk.ac.diamond.daq.osgi.OsgiService;
import uk.ac.gda.api.remoting.ServiceInterface;

@OsgiService(SESSettingsService.class)
@ServiceInterface(SESSettingsService.class)
public class SESSettings implements SESSettingsService {

	private String name;
	private Map<String, List<Double>> legacyConversionExcitationEnergySourceForSESRegion;
	private String defaultExcitationEnergySourceForSESRegion = "source1";
	private String legacyFileExtensionForSESSequenceJSONHanlder = "GDA_9.36";
	boolean legacyFileFormatOverwrittenForSESSequenceJSONHanlder = false;
	private List<SESConfigExcitationEnergySource> excitationEnergyConfigList = null;

	private SESSettings() {}

	@Override
	public String getDefaultExcitationEnergySourceForSESRegion() {
		return defaultExcitationEnergySourceForSESRegion;
	}

	@Override
	public void setDefaultExcitationEnergySourceForSESRegion(String defaultExcitationEnergySourceForSESRegion) {
		this.defaultExcitationEnergySourceForSESRegion = defaultExcitationEnergySourceForSESRegion;
	}

	@Override
	public String getLegacyFileExtensionForSESSequenceJSONHanlder() {
		return legacyFileExtensionForSESSequenceJSONHanlder;
	}

	@Override
	public void setLegacyFileExtensionForSESSequenceJSONHanlder(String legacyFileExtensionForSESSequenceJSONHanlder) {
		this.legacyFileExtensionForSESSequenceJSONHanlder = legacyFileExtensionForSESSequenceJSONHanlder;
	}

	@Override
	public Map<String, List<Double>> getLegacyConversionExcitationEnergyForSESRegion() {
		return legacyConversionExcitationEnergySourceForSESRegion;
	}

	@Override
	public void setLegacyConversionExcitationEnergyForSESRegion(Map<String, List<Double>> legacyConversionExcitationEnergySourceForSESRegion) {
		this.legacyConversionExcitationEnergySourceForSESRegion = legacyConversionExcitationEnergySourceForSESRegion;
	}

	@Override
	public boolean isLegacyFileFormatOverwrittenForSESSequenceJSONHanlder() {
		return legacyFileFormatOverwrittenForSESSequenceJSONHanlder;
	}

	@Override
	public void setLegacyFileFormatOverwrittenForSESSequenceJSONHanlder(boolean legacyFileFormatOverwrittenForSESSequenceJSONHanlder) {
		this.legacyFileFormatOverwrittenForSESSequenceJSONHanlder = legacyFileFormatOverwrittenForSESSequenceJSONHanlder;
	}

	@Override
	public List<SESConfigExcitationEnergySource> getSESConfigExcitationEnergySourceList() {
		return excitationEnergyConfigList;
	}

	@Override
	public void setSESConfigExcitationEnergySourceList(List<SESConfigExcitationEnergySource> excitationEnergyConfigList) {
		this.excitationEnergyConfigList = excitationEnergyConfigList;
	}

	@Override
	public List<SESExcitationEnergySource> getSESExcitationEnergySourceList() {
		final List<SESConfigExcitationEnergySource> excitationEnergySourceConfig = getSESConfigExcitationEnergySourceList();
		if (excitationEnergySourceConfig == null || excitationEnergySourceConfig.isEmpty()) throw new RuntimeException("excitationEnergySourceConfig cannot be null or empty!");
		return excitationEnergySourceConfig.stream().map(config -> new SESExcitationEnergySource(config.getName(), config.getScannableName())).toList();
	}

	@Override
	public boolean isExcitationEnergySourceSelectable() {
		return getSESConfigExcitationEnergySourceList().size() > 1;
	}

	@Override
	public String convertLegacyExcitationEnergyToExcitationEnergySourceName(final double excitationEnergy) {
		final Map<String, List<Double>> excitationEnergySourceToLimits = getLegacyConversionExcitationEnergyForSESRegion();
		if (excitationEnergySourceToLimits != null) {
			for (final Map.Entry<String, List<Double>> entry : excitationEnergySourceToLimits.entrySet()) {
				final String configuredExcitationEnergySource = entry.getKey();
				final List<Double> limits = entry.getValue();
				final Double lowLimit = limits.get(0);
				final Double highLimit = limits.get(1);
				if (excitationEnergy >= lowLimit && excitationEnergy < highLimit) {
					return configuredExcitationEnergySource;
				}
			}
		}
		//Return default case as it isn't configured.
		return getDefaultExcitationEnergySourceForSESRegion();
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}