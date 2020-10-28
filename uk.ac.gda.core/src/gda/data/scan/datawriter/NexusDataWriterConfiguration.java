/*-
 * Copyright © 2020 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.data.scan.datawriter;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import gda.data.scan.datawriter.scannablewriter.ScannableWriter;
import gda.factory.FindableBase;
import uk.ac.diamond.daq.osgi.OsgiService;

/**
 * An instance of this class holds some configuration about how to write nexus files for all scans
 * on this beamline, e.g. metadata scannables to be added to each scan, the location map to use, etc.
 */
@OsgiService(NexusDataWriterConfiguration.class)
public class NexusDataWriterConfiguration extends FindableBase {

	// note, we can't use Collections.emptySet, etc as some client code may expect these collections to be mutable
	private Set<String> metadataScannables;

	private Map<String, ScannableWriter> locationMap;

	private Map<String, Set<String>> metadataScannablesPerDetectorMap;

	private List<String> nexusTemplateFiles;

	private Map<String, String> metadata;

	public NexusDataWriterConfiguration() {
		initializeEmptyConfiguration();
	}

	private void initializeEmptyConfiguration() {
		metadataScannables = new HashSet<>();
		locationMap = new HashMap<>();
		metadataScannablesPerDetectorMap = new HashMap<>();
		nexusTemplateFiles = new ArrayList<>();
		metadata = new HashMap<>();
	}

	/**
	 * Returns the names of the configured metadata scannables. These will be added to the nexus file for all scans on this beamline.
	 * @return metadata scannables
	 */
	public Set<String> getMetadataScannables() {
		return metadataScannables;
	}

	public void setMetadataScannables(Set<String> metadataScannables) {
		requireNonNull(metadataScannables);
		this.metadataScannables = metadataScannables;
	}

	public void addMetadataScannable(String scannableName) {
		metadataScannables.add(scannableName);
	}

	public boolean removeMetadataScannable(String scannableName) {
		return metadataScannables.remove(scannableName);
	}

	/**
	 * Returns the location map, a map from scannable name to a {@link ScannableWriter} that writes the scannable with that name.
	 * The scannable writer also contains the {@link ScannableWriter#getPrerequisiteScannableNames()}.
	 * @return the location map
	 */
	public Map<String, ScannableWriter> getLocationMap() {
		return locationMap;
	}

	public void setLocationMap(Map<String, ScannableWriter> locationMap) {
		requireNonNull(locationMap);
		this.locationMap = locationMap;
	}

	/**
	 * Returns the map of scannables per detectors, i.e. these are scannables that are written as
	 * metadata scannables to the nexus file for scans that include the detector.
	 *
	 * @return scannables per detector map
	 */
	public Map<String, Set<String>> getMetadataScannablesPerDetectorMap() {
		return metadataScannablesPerDetectorMap;
	}

	public void setMetadataScannablesPerDetectorMap(Map<String, Collection<String>> metadataScannablesPerDetector) {
		requireNonNull(metadataScannablesPerDetector);
		this.metadataScannablesPerDetectorMap = metadataScannablesPerDetector.entrySet().stream().collect(
				Collectors.toMap(Map.Entry::getKey, entry -> new HashSet<String>(entry.getValue())));
	}

	public void setMetadataScannablesForDetector(String detectorName, Set<String> metadataScananbles) {
		metadataScannablesPerDetectorMap.put(detectorName, metadataScananbles);
	}

	public Set<String> getMetadataScannablesForDetector(String detectorName) {
		return metadataScannablesPerDetectorMap.getOrDefault(detectorName, emptySet());
	}

	public Set<String> getMetadataScannablesForScannable(String scannableName) {
		final ScannableWriter writer = locationMap.get(scannableName);
		return writer == null ? Collections.emptySet() : writer.getPrerequisiteScannableNames();
	}

	/**
	 * Returns the nexus template files that will applied to the nexus file as written.
	 *
	 * @return nexus template files
	 */
	public List<String> getNexusTemplateFiles() {
		return nexusTemplateFiles;
	}

	public void setNexusTemplateFiles(List<String> nexusTemplateFiles) {
		requireNonNull(nexusTemplateFiles);
		this.nexusTemplateFiles = nexusTemplateFiles;
	}

	/**
	 * Returns the metadata to be added to the nexus files.
	 * @return nexus file
	 */
	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		requireNonNull(metadata);
		this.metadata = metadata;
	}

	public void clear() {
		initializeEmptyConfiguration();
	}

}
