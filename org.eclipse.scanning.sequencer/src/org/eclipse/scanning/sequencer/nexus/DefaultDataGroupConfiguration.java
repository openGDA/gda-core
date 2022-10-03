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

package org.eclipse.scanning.sequencer.nexus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.dawnsci.nexus.scan.IDefaultDataGroupCalculator;

import uk.ac.diamond.daq.osgi.OsgiService;

@OsgiService(value=IDefaultDataGroupCalculator.class)
public class DefaultDataGroupConfiguration implements IDefaultDataGroupCalculator {

	private String defaultDataGroupName;

	private List<String> defaultDataGroupNames;

	public String getDefaultDataGroupName() {
		return defaultDataGroupName;
	}

	public void setDefaultDataGroupName(String defaultDataGroupName) {
		this.defaultDataGroupName = defaultDataGroupName;
	}

	public List<String> getDefaultDataGroupNames() {
		return defaultDataGroupNames;
	}

	public void setDefaultDataGroupNames(List<String> defaultDataGroupNames) {
		this.defaultDataGroupNames = defaultDataGroupNames;
	}

	public void addDefaultDataGroupName(String defaultDataGroupName) {
		final List<String> newList;
		if (defaultDataGroupNames == null) {
			newList = new ArrayList<>();
		} else {
			newList = new ArrayList<>(defaultDataGroupNames.size() + 1);
			newList.addAll(defaultDataGroupNames);
		}
		newList.add(defaultDataGroupName);
		defaultDataGroupNames = newList;
	}

	@Override
	public String getDefaultDataGroupName(List<String> dataGroupNames) {
		// check that either the single valued or list property must be set
		if (defaultDataGroupName == null && defaultDataGroupNames == null) {
			throw new IllegalStateException(DefaultDataGroupConfiguration.class.getSimpleName() + " is not configured, either"
					+ " defaultDataGroupName or defaultDataGroupNames (list) property must be set");
		}


		if (dataGroupNames.isEmpty()) {
			throw new IllegalArgumentException("No data groups");
		}

		final String firstDataGroupName = dataGroupNames.iterator().next();
		final Set<String> dataGroupNameSet = new HashSet<>(dataGroupNames);
		if (defaultDataGroupName != null && dataGroupNameSet.contains(defaultDataGroupName)) {
			return defaultDataGroupName;
		}

		if (defaultDataGroupNames == null) {
			return firstDataGroupName;
		}

		return defaultDataGroupNames.stream()
				.filter(dataGroupNameSet::contains)
				.findFirst()
				.orElse(firstDataGroupName);
	}

}
