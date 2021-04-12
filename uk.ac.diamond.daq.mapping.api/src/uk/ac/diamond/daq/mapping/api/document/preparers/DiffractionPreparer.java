/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api.document.preparers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.event.scan.ScanRequest;

import gda.configuration.properties.LocalProperties;

/**
 * FIXME this is a temporary solution for adding a radiograph collector monitor
 * to a K11 diffraction scan. A generic mechanism for injecting monitors to a particular scan
 * (e.g. via properties) is preferable.
 */
public class DiffractionPreparer implements ScanRequestPreparer {

	@Override
	public void prepare(ScanRequest scanRequest) {
		String monitorName = LocalProperties.get("diffraction.radiograph.decorator");
		if (monitorName == null) return;
		List<String> monitors = new ArrayList<>(scanRequest.getMonitorNamesPerPoint());
		monitors.add(monitorName);
		scanRequest.setMonitorNamesPerScan(monitors);
	}

}
