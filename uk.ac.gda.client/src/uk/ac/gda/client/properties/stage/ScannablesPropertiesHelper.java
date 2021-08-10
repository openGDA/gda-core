package uk.ac.gda.client.properties.stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument.ValueType;
import uk.ac.gda.client.properties.stage.services.DevicePositionDocumentService;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
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
 * client.scannableGroup.0.scannable.1.enums.OPEN:left position
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
 * {@link #getManagedScannable(String, String)};</li>
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

	public ScannableProperties getScannablePropertiesDocument(DefaultManagedScannable scannableDefinition) {
		return getScannablePropertiesDocument(scannableDefinition.groupId, scannableDefinition.getScannableId());
	}

	public <T> ManagedScannable<T> getManagedScannable(DefaultManagedScannable scannableDefinition) {
		return getManagedScannable(scannableDefinition.groupId, scannableDefinition.getScannableId());
	}

	/**
	 * @param <T> The expected scannable movement type: {@code String} for {@code EnumPositioner} or {@code Double} for {@code IScannableMotor}
	 * @param scannableGroupID the scannable group ID where the scannable belong to
	 * @param scannableID the scannable ID inside the scannable group
	 * @return a managed scannable or {@code null} if the pair (groupID, scannableID) is not available from the client properties
	 */
	public <T> ManagedScannable<T> getManagedScannable(String scannableGroupID, String scannableID) {
		var document = getScannablePropertiesDocument(scannableGroupID, scannableID);
		if (document == null)
			return null;
		if (!managedScannableMap.containsKey(document)) {
			ValueType type = getDevicePositionDocumentService().devicePositionType(document.getScannable());
			ManagedScannable<?> managedScanable = null;
			switch (type) {
			case NUMERIC:
				managedScanable = Optional.ofNullable(document)
						.map(ManagedScannable<Double>::new)
						.orElseGet(() -> null);
				break;
			case LABELLED:
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

	private static ScannableProperties getScannablePropertiesDocument(
			List<ScannableProperties> scannables, String scannableID) {
		return scannables.stream()
				.filter(s -> s.getId().equals(scannableID))
				.findFirst()
				.orElseGet(() -> null);
	}

	private static DevicePositionDocumentService getDevicePositionDocumentService() {
		return SpringApplicationContextFacade.getBean(DevicePositionDocumentService.class);
	}
}