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

import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gda.data.ServiceHolder;
import gda.data.scan.datawriter.scannablewriter.ScannableWriter;

/**
 * A class that calculates the metadata scannables for a scan from the set of detector names
 * and the set of scannable names in the scan.
 */
public class MetadataScannableCalculator {

	private enum DeviceType {
		DETECTOR,
		SCANNABLE
	}

	private final Set<String> detectorNames;

	private final Set<String> scannableNames;

	private NexusDataWriterConfiguration config = ServiceHolder.getNexusDataWriterConfiguration();

	public MetadataScannableCalculator(Collection<String> detectorNames, Collection<String> scannableNames) {
		this.detectorNames = new HashSet<>(detectorNames);
		this.scannableNames = new HashSet<>(scannableNames);
	}

	/**
	 * Calculates the metadata scannables to write into the nexus file, in the following manner:
	 * <ul>
	 * 	<li>The metadata scannables set by calling {@link NexusDataWriterConfiguration#setMetadataScannables(Set)};</li>
	 *	<li>The metadata scannables for each detector in the scan, according to the map set by
	 *			{@link NexusDataWriterConfiguration#setMetadataScannablesPerDetectorMap(Map)}</li>
	 *  <li>The prerequisite scannables names for each scannable or monitor in the given list passed-in,
	 *        according to the {@link ScannableWriter} entry in the location map for that scannable;</li>
	 *  <li>The prerequisite scannables for all the metadata scannables added so far, and their
	 *      prerequisities, and so on, until all prerequisities have been found;</li>
	 *  <li>removing any metadata scannable that is a (non-metadata) scannable or monitor in the list passed in.</li>
	 * </ul>
	 * <p>Note: The properties mentioned above are usually declared in the spring configuration for the beamline.
	 */
	public Set<String> calculateMetadataScannableNames() {
		final Set<String> metadataScannableNames = new HashSet<>();

		// add declared metadata scannables
		metadataScannableNames.addAll(config.getMetadataScannables());

		// add all dependencies of detectors
		metadataScannableNames.addAll(getAllDependencies(detectorNames, DeviceType.DETECTOR));

		// add all dependencies of scannables
		metadataScannableNames.addAll(getAllDependencies(scannableNames, DeviceType.SCANNABLE));

		// add all dependencies of new metadata scannables, repeat until no more are added
		Set<String> newMetadataScannableNames = metadataScannableNames;
		do {
			newMetadataScannableNames = getAllDependencies(newMetadataScannableNames, DeviceType.SCANNABLE);
		} while (metadataScannableNames.addAll(newMetadataScannableNames));

		// remove the names of any scannables being scanned
		metadataScannableNames.removeAll(scannableNames);

		return metadataScannableNames;
	}

	private Set<String> getAllDependencies(Collection<String> names, DeviceType deviceType) {
		return names.stream()
				.map(name -> getDependencies(name, deviceType))
				.flatMap(Collection::stream)
				.collect(toSet());
	}

	private Set<String> getDependencies(String deviceName, DeviceType deviceType) {
		switch (deviceType) {
			case DETECTOR:
				return config.getMetadataScannablesForDetector(deviceName);
			case SCANNABLE:
				return config.getMetadataScannablesForScannable(deviceName);
			default:
				throw new IllegalArgumentException("Unknown device type " + deviceType);
		}
	}

}