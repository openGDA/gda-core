package uk.ac.diamond.daq.mapping.ui.properties.stages;

import static uk.ac.gda.client.properties.ClientPropertiesHelper.getConfigurationKeys;
import static uk.ac.gda.client.properties.ClientPropertiesHelper.getProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Parses a description of the available stages
 *
 * <code>
 * client.stage.0=GTS
 * client.stage.0.id=GTS
 * client.stage.0.label=GTS
 * client.stage.0.scannables.0=x
 * client.stage.0.scannables.0.scannable=simx
 * client.stage.0.scannables.0.label=X
 * client.stage.0.scannables.1.scannable=simy
 * client.stage.0.scannables.1.label=Y
 * </code>
 *
 * where
 *
 * <ul>
 * <li>client.stage.INDEX - represents the sage description index</li>
 * <li>INDEX.id - the stage unique ID</li>
 * <li>INDEX.label - the stage label to us in the client</li> *
 * <li>INDEX.scannable.index.scannable - the scannable name. We assume that it is unique among all scannables.
 * This is a reasonable assumption as the scannable names are the same as the bean representing them consequently are unique</li>
 * <li>INDEX.scannable.index.label - the scannable label to use in the client</li>
 * </ul>
 *
 * @author Maurizio Nagni
 */
public final class StagesPropertiesHelper {

	private static final String SHUTTER = "shutter";

	private static final Map<String, StagePropertiesDocument> stagesPropertiesMap = new HashMap<>();

	static {
		reloadProperties();
	}

	/**
	 * The prefix used in the property files to identify a stage configuration.
	 */
	private static final String STAGE_PROPERTIES_PREFIX = "client.stage";

	private StagesPropertiesHelper() {
	}

	private static void parseStagesProperties() {
		IntStream.range(0, getConfigurationKeys(STAGE_PROPERTIES_PREFIX).size())
				.forEach(StagesPropertiesHelper::parseStageProperties);
	}

	private static List<ScannablePropertiesDocument> parseStageScannablesProperties(int index) {
		String scannablesKey = String.format("%s.%s.%s", STAGE_PROPERTIES_PREFIX, index, "scannables");
		return IntStream.range(0, getConfigurationKeys(scannablesKey).size())
				.mapToObj(scannableIndex -> StagesPropertiesHelper.parseStageScannablesProperties(scannablesKey, scannableIndex))
				.collect(Collectors.toList());
	}

	private static void parseStageProperties(int index) {
		StagePropertiesDocument.Builder builder = new StagePropertiesDocument.Builder();
		String id = getProperty(STAGE_PROPERTIES_PREFIX, index, "id", UUID.randomUUID().toString());
		builder.withId(id);
		builder.withLabel(getProperty(STAGE_PROPERTIES_PREFIX, index, "label", "Default"));
		builder.withScannables(parseStageScannablesProperties(index));
		stagesPropertiesMap.putIfAbsent(id, builder.build());

	}

	private static ScannablePropertiesDocument parseStageScannablesProperties(String scannableKey, int scannableIndex) {
		ScannablePropertiesDocument.Builder builder = new ScannablePropertiesDocument.Builder();
		builder.withLabel(getProperty(scannableKey, scannableIndex, "label", "Default"));
		builder.withScannable(getProperty(scannableKey, scannableIndex, "scannable", null));
		return builder.build();
	}

	/**
	 * Returns a set of all the scannable composing all the stages.
	 * As each scannable composing a stage is considered as independent, this method may be useful
	 * to have a snapshot at a given time.
	 * The set is collected looking thought all the available {@link ScannablePropertiesDocument#getScannable()}
	 *
	 * @return a set of scannable identifying strings
	 */
	public static final Set<String> getScannables() {
		return stagesPropertiesMap.values().stream()
			.flatMap(stage -> stage.getScannables().stream())
			.map(ScannablePropertiesDocument::getScannable)
			.collect(Collectors.toSet());
	}

	/**
	 * Return the shutter scannable name.
	 * <p>
	 * In the actual context the shutter is considered as an essential component of a beamline.
	 * </p>
	 *
	 * @return Returns the string identifying the shutter
	 */
	public static final String getShutter() {
		if (stagesPropertiesMap.containsKey(SHUTTER) && stagesPropertiesMap.get(SHUTTER).getScannables() !=null) {
			if (stagesPropertiesMap.get(SHUTTER).getScannables().size() > 0) {
				return stagesPropertiesMap.get(SHUTTER).getScannables().get(0).getScannable();
			}
		}
		return null;
	}

	public static final void reloadProperties() {
		stagesPropertiesMap.clear();
		parseStagesProperties();
	}
}
