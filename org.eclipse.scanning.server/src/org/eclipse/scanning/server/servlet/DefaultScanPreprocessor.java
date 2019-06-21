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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.scan.process.IPreprocessor;
import org.eclipse.scanning.api.scan.process.ProcessingException;
import org.eclipse.scanning.server.application.Activator;

/**
 * This pre-processor merges the monitors defined in its {@link DefaultScanConfiguration} into a {@link ScanRequest}.
 */
public class DefaultScanPreprocessor implements IPreprocessor {

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
	public <T> ScanRequest<T> preprocess(ScanRequest<T> scanRequest) throws ProcessingException {
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
		}

		return scanRequest;
	}

}
