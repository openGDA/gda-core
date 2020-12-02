package uk.ac.diamond.daq.mapping.ui.properties;

import static uk.ac.gda.client.properties.ClientPropertiesHelper.getProperty;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.api.document.AcquisitionTemplateType;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.gda.ui.tool.selectable.SelectableContainedCompositeFactory;

/**
 * Hides the configuration structural design.
 *
 * <p>
 * Defining an AcquisitionTemplateType is necessary to define which scannable is associated with each
 * {@link ScannableTrackDocument}. As such the client has to configure the possible acquisitions in the property file.
 *
 * <p>
 * A typical acquisition property line format looks like below <br>
 * <p>
 * {@code acquisition.[acquisitionType].[acquisitionTemplateType].[otherProperties]}
 * </p>
 *
 * where
 * <ul>
 * <li>
 * {@code acquisitionType} represents a group of acquisitions, diffraction, tomography or others.
 * This element is a handy way to aggregate acquisitions of the same kind which usually can be thought as similar
 * so usually may represents the acquisition types available in a perspective.
 * </li>
 * <li>
 * {@code acquisitionTemplateType} represents a specific acquisition template. Examples can be found
 * <a href="https://confluence.diamond.ac.uk/display/DIAD/Acquisition+Template+Types">here</a>
 * </li>
 * </ul>
 * </p>
 * A configuration element may be look like below
 *
 * <pre>
 * {@code
 *	acquisition.diffraction.two_dimension_point.0.scannable = simx
 *  acquisition.diffraction.two_dimension_point.0.axis = x
 *	acquisition.diffraction.two_dimension_point.1.scannable = simy
 *	acquisition.diffraction.two_dimension_point.1.axis = y
 *
 * 	acquisition.diffraction.two_dimension_line.0.scannable = simx
 *  acquisition.diffraction.two_dimension_line.0.axis = x
 * 	acquisition.diffraction.two_dimension_line.1.scannable = simy
 * 	acquisition.diffraction.two_dimension_line.1.axis = y
 *
 * 	acquisition.diffraction.two_dimension_grid.0.scannable = simx
 *  acquisition.diffraction.two_dimension_grid.0.axis = x
 * 	acquisition.diffraction.two_dimension_grid.1.scannable = simy
 *  acquisition.diffraction.two_dimension_grid.1.axis = y
 * }
 * </pre>
 *
 * where the fields meaning represent
 *
 * <ul>
 * <li><i>acquisition.diffraction</i>
 * <ul>
 * <li>the acquisition prefix</li>
 * </ul>
 * </li>
 * <li><i>two_dimension_point | two_dimension_line | two_dimension_grid</i>
 * <ul>
 * <li>a specific AcquisitionTemplateType</li>
 * </ul>
 * </li>
 * <li><i>INDEX</i>
 * <ul>
 * <li>an indexed list of axis</li>
 * </ul>
 * <li><i>INDEX.axis</i>
 * <ul>
 * <li>the name of the axis </li>
 * </ul>
 * </li>
 * <li><i>INDEX.scannable</i>
 * <ul>
 * <li>the name of the scannable for this axis</li>
 * </ul>
 * </li>
 * </ul>
 * <p>
 * The class parses the properties and group them by {@code acquisitionType} making them available through {@link #getAcquisitionProperties(String)}.
 * </p>
 *
 * @author Maurizio Nagni
 *
 * @see SelectableContainedCompositeFactory
 * @see AcquisitionTemplateType
 */
public final class AcquisitionTypeProperties {

	private static final  Map<String, AcquisitionTypeProperties> acquisitionProperties = new HashMap<>();

	/**
	 * Map each acquisition type to a pre-build list of scannableTrack documents
	 */
	private final Map<AcquisitionTemplateType, List<ScannableTrackDocument>> acquisitionTemplateTypeScanables = new EnumMap<>(AcquisitionTemplateType.class);
	private final String acquisitionType;

	/**
	 * Return an {@code AcquisitionTypeProperties} parsing the available properties or return a copy of it if already available.
	 * @param acquisitionType the required type
	 * @return an {@code AcquisitionTypeProperties} instance, eventually empty.
	 */
	public static AcquisitionTypeProperties getAcquisitionProperties(String acquisitionType) {
		// Does already exist?
		if (acquisitionProperties.containsKey(acquisitionType)) {
			return acquisitionProperties.get(acquisitionType);
		}
		AcquisitionTypeProperties newProperties = new AcquisitionTypeProperties(acquisitionType);
		// Add to cache only if not empty
		if (!newProperties.getAvailableAcquisitionTemplateTypes().isEmpty()) {
			acquisitionProperties.put(acquisitionType, newProperties);
		}
		return newProperties;
	}

	/**
	 * Used internally to check whether this instance has no associated {code AcquisitionTemplateType}
	 * @return the available {code AcquisitionTemplateType}
	 */
	public Set<AcquisitionTemplateType> getAvailableAcquisitionTemplateTypes() {
		return Collections.unmodifiableSet(acquisitionTemplateTypeScanables.keySet());
	}

	/**
	 * Create a new {@link uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument.Builder} for the specified acquisition template type
	 * according to the parsed {@link #getAcquisitionType()}
	 *
	 * @param acquisitonTemplateType
	 *            the acquisition type
	 * @return a list of predefined scannableTrackDocument
	 */
	public ScanpathDocument.Builder buildScanpathBuilder(AcquisitionTemplateType acquisitonTemplateType) {
		List<ScannableTrackDocument> scannableTrackDocuments = getScannableTrackDocument(acquisitonTemplateType);
		ScanpathDocument.Builder builder = new ScanpathDocument.Builder();
		builder.withModelDocument(acquisitonTemplateType);
		builder.withScannableTrackDocuments(scannableTrackDocuments);
		return builder;
	}

	public String getAcquisitionType() {
		return acquisitionType;
	}

	private AcquisitionTypeProperties(String acquisitionType) {
		this.acquisitionType = acquisitionType;
		parseAcquisitionTypeScannableProperties();
	}

	private String getAcquisitionTypePrefix() {
		return "acquisition." + getAcquisitionType();
	}

	private List<String> getAcquisitionTemplateTypeKeys(String acquisitionTemplateType) {
		return LocalProperties.getKeysByRegexp(
				String.format("%s\\.%s\\.\\d.*", getAcquisitionTypePrefix(), acquisitionTemplateType));
	}

	private void parseAcquisitionTypeScannableProperties() {
		Arrays.stream(AcquisitionTemplateType.values())
			.forEach(this::parseAcquisitionTemplateType);
	}

	private void parseAcquisitionTemplateType(AcquisitionTemplateType acquisitonTemplateType) {
		String name = acquisitonTemplateType.name().toLowerCase();
		List<String> elements = getAcquisitionTemplateTypeKeys(name);

		List<ScannableTrackDocument> scannableTrackDocuments = IntStream.range(0, elements.size()/2)
				.mapToObj(index -> 	parseAcquisitionTemplateType(index, name))
				.collect(Collectors.toList());
		acquisitionTemplateTypeScanables.put(acquisitonTemplateType, scannableTrackDocuments);
	}

	private ScannableTrackDocument parseAcquisitionTemplateType(int index, String key) {
		ScannableTrackDocument.Builder builder = new ScannableTrackDocument.Builder();
		String prefix = String.format("%s.%s", getAcquisitionTypePrefix(), key);
		 builder.withAxis(getAcquisitionTemplateAxis(index, prefix));
		 builder.withScannable(getAcquisitionTemplateScannable(index, prefix));
		return builder.build();
	}

	private static String getAcquisitionTemplateAxis(int index, String prefix) {
		return getProperty(prefix, index, "axis", null);
	}

	private static String getAcquisitionTemplateScannable(int index, String prefix) {
		return getProperty(prefix, index, "scannable", null);
	}

	/**
	 * Builds a set of ScannableTrackDocument exactly how specified for the DiffractionAcquisitionTypeProperties
	 * associated with the AcquisitionTemplateType
	 *
	 * @param acquisitonTemplateType
	 * @return a list of scannable track builders
	 */
	private List<ScannableTrackDocument> createScannableTrackDocuments(AcquisitionTemplateType acquisitonTemplateType) {
		return acquisitionTemplateTypeScanables
				.get(acquisitonTemplateType).stream().map(e -> {
					ScannableTrackDocument.Builder builder = new ScannableTrackDocument.Builder();
					builder.withAxis(e.getAxis());
					builder.withScannable(e.getScannable());
					return builder;
				})
				.map(ScannableTrackDocument.Builder::build)
				.collect(Collectors.toList());
	}

	private List<ScannableTrackDocument> getScannableTrackDocument(AcquisitionTemplateType acquisitonTemplateType) {
		return createScannableTrackDocuments(acquisitonTemplateType).stream()
				.map(ScannableTrackDocument.Builder::new)
				.map(builder -> {
					builder.withPoints(1);
					builder.withStart(0);
					builder.withStop(180.0);
				return builder;
				})
				.map(ScannableTrackDocument.Builder::build)
				.collect(Collectors.toList());
	}
}