package uk.ac.diamond.daq.mapping.ui.properties.stages;

import static uk.ac.gda.client.properties.ClientPropertiesHelper.getConfigurationKeys;
import static uk.ac.gda.client.properties.ClientPropertiesHelper.getProperty;
import static uk.ac.gda.client.properties.ClientPropertiesHelper.getStringArrayProperty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import uk.ac.diamond.daq.mapping.ui.services.position.DevicePositionDocumentService;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument.ValueType;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;

/**
 * Parses the available scannableGroups from the properties. The properties format follows a structure like
 *
 * <pre>
 * client.scannableGroup.0=GTS
 * client.scannableGroup.0.id=GTS
 * client.scannableGroup.0.label=GTS
 * client.scannableGroup.0.scannable.0=x
 * client.scannableGroup.0.scannable.0.id=Y
 * client.scannableGroup.0.scannable.0.scannable=simx
 * client.scannableGroup.0.scannable.0.label=X Axis
 * client.scannableGroup.0.scannable.1=y
 * client.scannableGroup.0.scannable.1.id=Y
 * client.scannableGroup.0.scannable.1.scannable=simy
 * client.scannableGroup.0.scannable.1.label=Y Axis
 * client.scannableGroup.0.scannable.1.enums=CLOSED:right position,OPEN:left position
 * </pre>
 *
 * where
 *
 * <ul>
 * <li>client.scannables.INDEX - represents the sage description index</li>
 * <li>INDEX.id - the stage group unique ID</li>
 * <li>INDEX.label - the stage label to us in the client</li>
 * <li>INDEX.scannable.index.scannable - the scannable name. We assume that it is unique among all scannables. This is a
 * reasonable assumption as the scannable names are the same as the bean representing them consequently are unique</li>
 * <li>INDEX.scannable.index.label - the scannable label to use in the client</li>
 * <li>INDEX.scannable.index.enum - when the scannable is of type enumPositioner, this comma separated property allows to map the internal key with the scannable real position enum </li>
 * </ul>
 *
 * In a real application
 * </p>
 * <ol>
 * <li>{@link ScannablesPropertiesHelper} parses the existing scannableGroups from the properties</li>
 * <li>the external component can retrieve the required a ManagedScannable using
 * {@link #getManagedScannable(String, String, Class)};</li>
 * </ol>
 * </p>
 *
 * @author Maurizio Nagni
 */
public final class ScannablesPropertiesHelper {

	private static final String SHUTTER = "shutter";

	private static final Map<String, ScannableGroupPropertiesDocument> scannablesGroupPropertiesMap = new HashMap<>();
	private static final Map<ScannablePropertiesDocument, ManagedScannable<?>> managedScannableMap = new HashMap<>();

	static {
		reloadProperties();
	}

	/**
	 * The prefix used in the property files to identify a stage configuration.
	 */
	private static final String STAGE_PROPERTIES_PREFIX = "client.scannableGroup";

	private ScannablesPropertiesHelper() {
	}

	private static void parseScannableGroupsProperties() {
		IntStream.range(0, getConfigurationKeys(STAGE_PROPERTIES_PREFIX).size())
				.forEach(ScannablesPropertiesHelper::parseScannableProperties);
	}

	private static List<ScannablePropertiesDocument> parseScannablesGroupProperties(int index) {
		String scannablesKey = String.format("%s.%s.%s", STAGE_PROPERTIES_PREFIX, index, "scannable");
		return IntStream.range(0, getConfigurationKeys(scannablesKey).size())
				.mapToObj(scannableIndex -> ScannablesPropertiesHelper.parseStageScannablesProperties(scannablesKey,
						scannableIndex))
				.collect(Collectors.toList());
	}

	private static void parseScannableProperties(int index) {
		ScannableGroupPropertiesDocument.Builder builder = new ScannableGroupPropertiesDocument.Builder();
		String id = getProperty(STAGE_PROPERTIES_PREFIX, index, "id", UUID.randomUUID().toString());
		builder.withId(id);
		builder.withLabel(getProperty(STAGE_PROPERTIES_PREFIX, index, "label", "Default"));
		builder.withScannables(parseScannablesGroupProperties(index));
		scannablesGroupPropertiesMap.putIfAbsent(id, builder.build());
	}

	private static ScannablePropertiesDocument parseStageScannablesProperties(String scannableKey, int scannableIndex) {
		ScannablePropertiesDocument.Builder builder = new ScannablePropertiesDocument.Builder();
		builder.withId(getProperty(scannableKey, scannableIndex, "id", Integer.toString(scannableIndex)));
		builder.withLabel(getProperty(scannableKey, scannableIndex, "label", "Default"));
		builder.withScannable(getProperty(scannableKey, scannableIndex, "scannable", null));
		builder.withEnumsMap(parseScannableEnums(scannableKey, scannableIndex));
		return builder.build();
	}

	private static Map<String, String> parseScannableEnums(String scannableKey, int scannableIndex) {
		Map<String, String> enumsMap = new HashMap<>();
		String[] enums = getStringArrayProperty(scannableKey, scannableIndex, "enums");
		if (enums != null) {
			Arrays.stream(enums)
					.forEach(m -> {
						String[] mapping = m.split(":");
						enumsMap.put(mapping[0], mapping[1]);
					});
		}
		return enumsMap;
	}

	/**
	 * Returns a set of all the scannable composing all the stages. As each scannable composing a stage is considered as
	 * independent, this method may be useful to have a snapshot at a given time. The set is collected looking thought
	 * all the available {@link ScannablePropertiesDocument#getScannable()}
	 *
	 * @return a set of scannable identifying strings
	 */
	public static final Set<String> getScannables() {
		return scannablesGroupPropertiesMap.values().stream()
				.flatMap(stage -> stage.getScannables().stream())
				.map(ScannablePropertiesDocument::getScannable)
				.collect(Collectors.toSet());
	}

	/**
	 * Returns a {@code ScannableGroupPropertiesDocument} for a specific group ID
	 *
	 * @param scannableGroupID
	 * @return an existing document, otherwise {@code null}
	 */
	public static final ScannableGroupPropertiesDocument getScannableGroupPropertiesDocument(String scannableGroupID) {
		return scannablesGroupPropertiesMap.get(scannableGroupID);
	}

	/**
	 * Returns a {@code ScannablePropertiesDocument} for a specific scannable id (not group ID)
	 *
	 * @param scannableGroupID
	 * @param scannableID
	 * @return an existing document, otherwise {@code null}
	 */
	private static final ScannablePropertiesDocument getScannablePropertiesDocument(String scannableGroupID,
			String scannableID) {
		return Optional.ofNullable(getScannableGroupPropertiesDocument(scannableGroupID))
				.map(ScannableGroupPropertiesDocument::getScannables)
				.map(s -> getScannablePropertiesDocument(s, scannableID))
				.orElseGet(() -> null);
	}

	/**
	 * @param <T> The expected scannable movement type: {@code String} for {@code EnumPositioner} or {@code Double} for {@code IScannableMotor}
	 * @param scannableGroupID the scannable group ID where the scannable belong to
	 * @param scannableID the scannable ID inside the scannable group
	 * @param clazz the expected scannable movement type
	 * @return a managed scannable or {@code null} if the pair (groupID, scannableID) is not available from the client properties
	 */
	public static final <T> ManagedScannable<T> getManagedScannable(String scannableGroupID, String scannableID,
			Class<T> clazz) {
		ScannablePropertiesDocument document = getScannablePropertiesDocument(scannableGroupID, scannableID);
		if (document == null)
			return null;
		if (!managedScannableMap.containsKey(document)) {
			ValueType type = getDevicePositionDocumentService().devicePositionType(document.getScannable());
			ManagedScannable<?> managedScanable = null;
			switch (type) {
			case NUMERIC:
				if (!clazz.isAssignableFrom(Double.class)) {
					return null;
				}
				managedScanable = Optional.ofNullable(document)
						.map(ManagedScannable<Double>::new)
						.orElseGet(() -> null);
				break;
			case LABELLED:
				if (!clazz.isAssignableFrom(String.class)) {
					return null;
				}
				managedScanable = Optional.ofNullable(document)
						.map(ManagedScannable<String>::new)
						.orElseGet(() -> null);
				break;
			default:
				break;
			}
			managedScannableMap.put(document, managedScanable);
		}
		return (ManagedScannable<T>) managedScannableMap.get(document);
	}

	private static ScannablePropertiesDocument getScannablePropertiesDocument(
			List<ScannablePropertiesDocument> scannables, String scannableID) {
		return scannables.stream()
				.filter(s -> s.getId().equals(scannableID))
				.findFirst()
				.orElseGet(() -> null);
	}

	/**
	 * Return the shutter scannable name.
	 * <p>
	 * In the actual context the shutter is considered as an essential component of a beamline.
	 * </p>
	 *
	 * @return Returns the string identifying the shutter
	 * @deprecated Use {@link #getManagedScannable(String, String, Class)}
	 */
	@Deprecated
	public static final String getShutter() {
		if (isStageMapped(SHUTTER) && !hasStageScannable(SHUTTER)) {
			return scannablesGroupPropertiesMap.get(SHUTTER).getScannables().get(0).getScannable();
		}
		return null;
	}

	public static final void reloadProperties() {
		scannablesGroupPropertiesMap.clear();
		parseScannableGroupsProperties();
	}

	private static boolean isStageMapped(String stageName) {
		return scannablesGroupPropertiesMap.containsKey(stageName)
				&& scannablesGroupPropertiesMap.get(stageName).getScannables() != null;
	}

	private static boolean hasStageScannable(String stageName) {
		return scannablesGroupPropertiesMap.get(stageName).getScannables().isEmpty();
	}

	private static DevicePositionDocumentService getDevicePositionDocumentService() {
		return SpringApplicationContextFacade.getBean(DevicePositionDocumentService.class);
	}
}
