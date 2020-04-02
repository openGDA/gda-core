package uk.ac.diamond.daq.mapping.ui.properties;

import static uk.ac.gda.client.properties.ClientPropertiesHelper.PROPERTY_FORMAT;
import static uk.ac.gda.client.properties.ClientPropertiesHelper.getConfigurationBeanProperty;
import static uk.ac.gda.client.properties.ClientPropertiesHelper.getId;
import static uk.ac.gda.client.properties.ClientPropertiesHelper.getNameProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import org.springframework.util.StringUtils;

import gda.configuration.properties.LocalProperties;
import uk.ac.gda.client.properties.DetectorProperties;

/**
 * Hides the configuration structural design. A typical configuration defining a camera would look like below
 *
 * <code>
 * client.detector.0=ws157-ML-SCAN-01
 * client.detector.0.name=Malcolm Diffraction Detector
 * client.detector.0.id=A_UNIQUE_STRING
 * </code>
 *
 * where
 *
 * <ul>
 * <li>client.detector.INDEX - represents the detector index</li>
 * <li>INDEX.name - represents the detector name. It may be either a detector bean ID or a malcom device EPICS id (like
 * in this example)</li>
 * <li>INDEX.id - is the configuration id (optional)</li>
 * </ul>
 *
 * More information are available in
 * <a href="https://confluence.diamond.ac.uk/display/DIAD/K11+GDA+Properties#K11GDAProperties-Detectors">Confluence</a>
 *
 * @author Maurizio Nagni
 *
 */
public final class DetectorHelper {

	public enum AcquisitionType {
		DIFFRACTION, TOMOGRAPHY, BEAM_SELECTOR
	};

	private static final List<DetectorProperties> detectorProperties = new ArrayList<>();
	private static final Map<String, DetectorProperties> detectorPropertiesByID = new HashMap<>();

	private static final Map<AcquisitionType, List<DetectorProperties>> acquisitionDetectors = new EnumMap<>(
			AcquisitionType.class);

	static {
		parseDetectorProperties();
		parseAcquisitionDetectors();
	}

	/**
	 * The prefix used in the property files to identify a camera configuration.
	 */
	private static final String DETECTOR_PREFIX = "client.detector";

	private DetectorHelper() {
	}

	/**
	 * Returns the {@link DetectorProperties} associated with this acquisition.
	 *
	 * @param acquisitionType
	 *            the required acquisition type
	 * @return an array of dete
	 */
	public static Optional<List<DetectorProperties>> getAcquistionDetector(AcquisitionType acquisitionType) {
		if (acquisitionDetectors.get(acquisitionType).isEmpty()) {
			return Optional.empty();
		}
		// now returns just the first but in future may returns a list of detectors
		return Optional.ofNullable(acquisitionDetectors.get(acquisitionType));
	}

	private static List<String> getConfigurationKeys(String prefix) {
		return LocalProperties.getKeysByRegexp(prefix + "\\.\\d");
	}

	private static void parseDetectorProperties() {
		IntStream.range(0, getConfigurationKeys(DETECTOR_PREFIX).size())
				.forEach(DetectorHelper::parseDetectorProperties);
		detectorProperties.sort((c1, c2) -> Integer.compare(c1.getIndex(), c2.getIndex()));
	}

	private static void parseAcquisitionDetectors() {
		Arrays.stream(AcquisitionType.values()).forEach(DetectorHelper::getAcquisitionDetectors);
	}

	private static void parseDetectorProperties(int index) {
		DetectorPropertiesBuilder builder = new DetectorPropertiesBuilder();
		builder.setIndex(index);
		builder.setId(getId(DETECTOR_PREFIX, index));
		builder.setName(getNameProperty(DETECTOR_PREFIX, index));
		builder.setDetectorBean(getConfigurationBeanProperty(DETECTOR_PREFIX, index));

		DetectorProperties cp = builder.build();
		cp.getId().ifPresent(id -> detectorPropertiesByID.putIfAbsent(id, cp));
		detectorProperties.add(cp);
	}

	private static void getAcquisitionDetectors(AcquisitionType acquisitionType) {
		String acqKey = acquisitionType.name().toLowerCase();
		List<DetectorProperties> detectorsProperties = new ArrayList<>();
		String[] detectors = LocalProperties.getStringArray(formatAcquisitionDetectorKey("client", acqKey, "detectors"));
		acquisitionDetectors.put(acquisitionType, detectorsProperties);
		Arrays.stream(detectors).filter(StringUtils::hasLength).filter(detectorPropertiesByID::containsKey)
				.map(detectorPropertiesByID::get).forEach(detectorsProperties::add);
	}

	private static String formatAcquisitionDetectorKey(String prefix, String acquisitionType, String property) {
		return String.format(PROPERTY_FORMAT, prefix, acquisitionType, property);
	}
}
