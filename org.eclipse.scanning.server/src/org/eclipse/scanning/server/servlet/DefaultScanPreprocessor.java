/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package org.eclipse.scanning.server.servlet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.scan.process.IPreprocessor;
import org.eclipse.scanning.api.scan.process.ProcessingException;
import org.eclipse.scanning.server.application.Activator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This pre-processor merges the monitors defined in its {@link DefaultScanConfiguration} into a {@link ScanRequest}.
 */
public class DefaultScanPreprocessor implements IPreprocessor {
	private static Logger logger = LoggerFactory.getLogger(DefaultScanPreprocessor.class);

	private DefaultScanConfiguration defaultScanConfiguration;

	@Override
	public String getName() {
		return "Default scan preprocessor";
	}

	public DefaultScanConfiguration getDefaultScanConfiguration() {
		return defaultScanConfiguration;
	}

	public void setDefaultScanConfiguration(DefaultScanConfiguration defaultScanConfiguration) {
		this.defaultScanConfiguration = defaultScanConfiguration;
	}

	@Override
	public ScanRequest preprocess(ScanRequest scanRequest) throws ProcessingException {
		if (defaultScanConfiguration == null) {
			// try getting the configuration object from the OSGi bundle context
			defaultScanConfiguration = Activator.getService(DefaultScanConfiguration.class);
		}

		if (defaultScanConfiguration != null) {
			// add default per point monitor names to the scan request
			final Set<String> perPointMonitorNames = new HashSet<>(defaultScanConfiguration.getPerPointMonitorNames());
			Optional.ofNullable(scanRequest.getMonitorNamesPerPoint()).ifPresent(perPointMonitorNames::addAll);
			scanRequest.setMonitorNamesPerPoint(perPointMonitorNames);

			// add default per scan monitor names to the scan request
			final Set<String> perScanMonitorNames = new HashSet<>(defaultScanConfiguration.getPerScanMonitorNames());
			Optional.ofNullable(scanRequest.getMonitorNamesPerScan()).ifPresent(perScanMonitorNames::addAll);
			scanRequest.setMonitorNamesPerScan(perScanMonitorNames);

			final Set<String> templateFilePaths = new HashSet<>(defaultScanConfiguration.getTemplateFilePaths());
			Optional.ofNullable(scanRequest.getTemplateFilePaths()).ifPresent(templateFilePaths::addAll);
			scanRequest.setTemplateFilePaths(templateFilePaths);

			final MapPosition startPosition = defaultScanConfiguration.getStartPosition();
			if (startPosition != null) {
				scanRequest.setStartPosition(mergePositions(startPosition, scanRequest.getStartPosition(), "startPosition"));
			}

			final MapPosition endPosition = defaultScanConfiguration.getEndPosition();
			if (endPosition != null) {
				scanRequest.setEnd(mergePositions(endPosition, scanRequest.getEndPosition(), "endPosition"));
			}
		}

		return scanRequest;
	}

	/**
	 * Merge a position into an existing position<br>
	 * If the same scannable is present in both positions, the existing value will be preserved.
	 *
	 * @param position
	 *            position to be merged
	 * @param mergeInto
	 *            existing position to merge into
	 * @param fieldName
	 *            name of the field containing this position (for messaging purposes)
	 */
	private IPosition mergePositions(MapPosition position, IPosition mergeInto, String fieldName) {
		// We have to create a new map, as the one in mergeInto may be immutable
		final Map<String, Object> result = (mergeInto == null) ? new HashMap<>() : new HashMap<>(mergeInto.getValues());

		// Merge in positions for variables that are not already present
		for (String scannableName : position.getNames()) {
			if (result.get(scannableName) == null) {
				result.put(scannableName, position.get(scannableName));
			} else {
				logger.warn("'{}' already exists in {}: cannot merge in another value", scannableName, fieldName);
			}
		}
		return new MapPosition(result);
	}

}
