package uk.ac.diamond.daq.mapping.ui.properties;

import static uk.ac.gda.client.properties.ClientPropertiesHelper.getConfigurationKeys;
import static uk.ac.gda.client.properties.ClientPropertiesHelper.getProperty;
import static uk.ac.gda.client.properties.ClientPropertiesHelper.getStringArrayProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import uk.ac.gda.api.acquisition.AcquisitionEngineDocument;
import uk.ac.gda.api.acquisition.AcquisitionEngineDocument.AcquisitionEngineType;
import uk.ac.gda.client.properties.CameraProperties;

/**
 * Parses a description of the available acquisitions types and engines
 *
 * <code>
 * client.acquisition.0=Diffraction engine
 * client.acquisition.0.type = DIFFRACTION
 * client.acquisition.0.engine.type=MALCOLM
 * client.acquisition.0.engine.id=${client.host}-ML-SCAN-01
 * client.acquisition.0.detectors=PILATUS
 * client.acquisition.0.out_of_beam=simx, simy
 * </code>
 *
 * where
 *
 * <ul>
 * <li>client.acquisition.INDEX - represents the acquisition description index</li>
 * <li>INDEX.type - represents the acquisition type name as in {@link AcquisitionsPropertiesHelper.AcquisitionPropertyType}. The type is required for</li>
 * <li>INDEX.engine.type - available acquisition engine as in {@link uk.ac.gda.api.acquisition.AcquisitionEngineDocument.AcquisitionEngineType}</li>
 * <li>INDEX.engine.id - the acquisition engine id. Depends on the engine type. See {@link AcquisitionEngineDocument}</li>
 * <li>INDEX.detectors - comma separated cameras, required for this acquisition.</li>
 * </ul>
 * More information are available in
 * <a href="https://confluence.diamond.ac.uk/display/DIAD/K11+GDA+Properties#K11GDAProperties-Detectors">Confluence</a>
 *
 * @author Maurizio Nagni
 * @see CameraProperties CameraHelper
 */
public final class AcquisitionsPropertiesHelper {

	/**
	 *
	 */
	public enum AcquisitionPropertyType {
		/**
		 * Identifies an acquisition associated with a diffraction
		 */
		DIFFRACTION,
		/**
		 * Identifies an acquisition associated with a tomography
		 */
		TOMOGRAPHY,
		/**
		 * Identifies an acquisition associated with a double detector
		 */
		BEAM_SELECTOR,
		/**
		 * Identifies an acquisition not associated with a specific type
		 */
		DEFAULT
	}

	private static final List<AcquisitionPropertiesDocument> acquisitionProperties = new ArrayList<>();
	private static final Map<AcquisitionPropertyType, List<AcquisitionPropertiesDocument>> acquisitionsMap = new EnumMap<>(
			AcquisitionPropertyType.class);

	static {
		reloadProperties();
	}



	/**
	 * The prefix used in the property files to identify a camera configuration.
	 */
	private static final String ACQUISITION_PROPERTIES_PREFIX = "client.acquisition";

	private AcquisitionsPropertiesHelper() {
	}

	/**
	 * Returns the {@link AcquisitionPropertiesDocument} associated with this acquisition.
	 *
	 * @param acquisitionPropertyType
	 *            the required acquisition type
	 * @return an list of possible acquisitions configuration engines. May return {@code null}
	 */
	public static List<AcquisitionPropertiesDocument> getAcquistionPropertiesDocument(AcquisitionPropertyType acquisitionPropertyType) {

		if (acquisitionsMap.get(acquisitionPropertyType).isEmpty()) {
			return null;
		}
		// now returns just the first but in future may returns a list of detectors
		return acquisitionsMap.get(acquisitionPropertyType);
	}

	private static void parseAcquisitionProperties() {
		IntStream.range(0, getConfigurationKeys(ACQUISITION_PROPERTIES_PREFIX).size())
				.forEach(AcquisitionsPropertiesHelper::parseDetectorProperties);
		acquisitionProperties.sort((c1, c2) -> Integer.compare(c1.getIndex(), c2.getIndex()));
	}

	private static void aggreagateByAcquisitionType() {
		Arrays.stream(AcquisitionPropertyType.values()).forEach(AcquisitionsPropertiesHelper::mapAcquisitionPropertyTypes);
	}

	private static void parseDetectorProperties(int index) {
		AcquisitionPropertiesDocument.Builder builder = new AcquisitionPropertiesDocument.Builder();

		builder.withIndex(index);
		builder.withType(AcquisitionPropertyType
				.valueOf(getProperty(ACQUISITION_PROPERTIES_PREFIX, index, "type", "DEFAULT")));

		AcquisitionEngineDocument.Builder engineBuilder = new AcquisitionEngineDocument.Builder();
		engineBuilder.withId(getProperty(ACQUISITION_PROPERTIES_PREFIX, index, "engine.id", null));
		engineBuilder.withType(AcquisitionEngineType
				.valueOf(getProperty(ACQUISITION_PROPERTIES_PREFIX, index, "engine.type", null)));
		builder.withEngine(engineBuilder.build());

		HashSet<String> cameras = new HashSet<>();
		Arrays.asList(getStringArrayProperty(ACQUISITION_PROPERTIES_PREFIX, index, "detectors")).stream().map(String::trim)
				.filter(s -> !s.isEmpty()).forEach(cameras::add);
		builder.withCameras(cameras);

		HashSet<String> outOfBeamScannables = new HashSet<>();
		Arrays.asList(getStringArrayProperty(ACQUISITION_PROPERTIES_PREFIX, index, "out_of_beam")).stream().map(String::trim)
				.filter(s -> !s.isEmpty()).forEach(outOfBeamScannables::add);
		builder.withOutOfBeamScannables(outOfBeamScannables);

		builder.withPrimaryDataset(getProperty(ACQUISITION_PROPERTIES_PREFIX, index, "dataset", null));

		AcquisitionPropertiesDocument cp = builder.build();
		acquisitionProperties.add(cp);
	}

	private static void mapAcquisitionPropertyTypes(AcquisitionPropertyType acquisitionType) {
		List<AcquisitionPropertiesDocument> detectorsProperties = new ArrayList<>();
		acquisitionProperties.stream()
			.filter(b -> acquisitionType.equals(b.getType()))
			.forEach(detectorsProperties::add);
		acquisitionsMap.putIfAbsent(acquisitionType, detectorsProperties);
	}

	public static final void reloadProperties() {
		acquisitionProperties.clear();
		acquisitionsMap.clear();
		parseAcquisitionProperties();
		aggreagateByAcquisitionType();
	}
}
