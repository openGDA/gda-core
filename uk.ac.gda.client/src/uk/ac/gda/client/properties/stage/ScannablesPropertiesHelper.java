package uk.ac.gda.client.properties.stage;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.gda.client.properties.stage.position.Position;
import uk.ac.gda.client.properties.stage.position.PositionScannableKeys;
import uk.ac.gda.client.properties.stage.position.ScannableKeys;
import uk.ac.gda.ui.tool.spring.ClientSpringProperties;

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
 * client.scannableGroup.0.scannable.1.enums.CLOSED=right position
 * client.scannableGroup.0.scannable.1.enums.OPEN=left position
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
 * {@link #getManagedScannable(ScannableKeys)};</li>
 * </ol>
 * </p>
 *
 * @author Maurizio Nagni
 */
@Component
public class ScannablesPropertiesHelper {

	@Autowired
	private ClientSpringProperties clientProperties;

	private static final Map<ScannableProperties, ManagedScannable<?>> managedScannableMap = new HashMap<>();

	/**
	 * Returns a set of all the scannable composing all the stages. As each scannable composing a stage is considered as
	 * independent, this method may be useful to have a snapshot at a given time. The set is collected looking thought
	 * all the available {@link ScannableProperties#getScannable()}
	 *
	 * @return a set of scannable identifying strings
	 */
	public Set<String> getScannables() {
		return clientProperties.getScannableGroups().stream()
				.flatMap(stage -> stage.getScannables().stream())
				.map(ScannableProperties::getScannable)
				.collect(Collectors.toSet());
	}

	/**
	 * Returns a {@code ScannableGroupPropertiesDocument} for a specific group ID
	 *
	 * @param scannableGroupID
	 * @return an existing document, otherwise {@code null}
	 */
	public ScannableGroupProperties getScannableGroupPropertiesDocument(String scannableGroupID) {
		return clientProperties.getScannableGroups().stream()
			.filter(g -> Objects.equals(g.getId(), scannableGroupID))
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
	}

	/**
	 * Returns a {@code ScannablePropertiesDocument} for a specific scannable id (not group ID)
	 *
	 * @param scannableGroupID
	 * @param scannableID
	 * @return an existing document, otherwise {@code null}
	 */
	private ScannableProperties getScannablePropertiesDocument(String scannableGroupID,
			String scannableID) {
		return Optional.ofNullable(getScannableGroupPropertiesDocument(scannableGroupID))
				.map(ScannableGroupProperties::getScannables)
				.map(s -> getScannablePropertiesDocument(s, scannableID))
				.orElseGet(() -> null);
	}

	public ScannableProperties getScannablePropertiesDocument(ScannableKeys scannableKeys) {
		if (scannableKeys.getGroupId() == null && scannableKeys.getScannableId() == null)
			return null;
		return getScannablePropertiesDocument(scannableKeys.getGroupId(), scannableKeys.getScannableId());
	}

	public ManagedScannable<Object> getManagedScannable(DefaultManagedScannable scannableDefinition) {
		return getManagedScannable(scannableDefinition.getScannableKey());
	}

	/**
	 * @param scannableKeys the keys identifying the scannable
	 * @return a managed scannable or {@code null} if the pair (groupID, scannableID) is not available from the client properties
	 */
	@SuppressWarnings("unchecked")
	public ManagedScannable<Object> getManagedScannable(ScannableKeys scannableKeys) {
		var document = getScannablePropertiesDocument(scannableKeys);
		if (document == null)
			return null;
		return (ManagedScannable<Object>) managedScannableMap.computeIfAbsent(document, ManagedScannable<Object>::new);
	}

	private static ScannableProperties getScannablePropertiesDocument(
			List<ScannableProperties> scannables, String scannableID) {
		return scannables.stream()
				.filter(s -> s.getId().equals(scannableID))
				.findFirst()
				.orElseGet(() -> null);
	}

	public Optional<PositionScannableKeys> getPositionScannableKeys(Position position) {
		return clientProperties.getPositions().stream()
			.filter(p -> position.equals(p.getPosition()))
			.findFirst();
	}

	public List<ScannableProperties> getPositionScannableProperties(Position position) {
		Optional<PositionScannableKeys> positionKeys = getPositionScannableKeys(position);
		if (positionKeys.isPresent()) {
			return positionKeys.get().getKeys().stream()
				.map(this::getScannablePropertiesDocument)
				.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}
}