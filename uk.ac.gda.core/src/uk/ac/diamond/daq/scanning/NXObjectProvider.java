/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.scanning;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;

public class NXObjectProvider<T extends NXobject> implements NexusObjectProvider<T> {

	private String name;
	private T sampleNode;

	public NXObjectProvider(String name, T sampleNode) {
		this.name = name;
		this.sampleNode = sampleNode;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public NexusBaseClass getNexusBaseClass() {
		return NexusBaseClass.NX_SAMPLE;
	}

	@Override
	public T getNexusObject() {
		return sampleNode;
	}

	@Override
	public NexusBaseClass getCategory() {
		return null;
	}

	@Override
	public String getCollectionName() {
		return null;
	}

	@Override
	public Set<String> getExternalFileNames() {
		return Collections.EMPTY_SET;
	}

	@Override
	public int getExternalDatasetRank(String fieldName) {
		return 0;
	}

	@Override
	public List<String> getAxisDataFieldNames() {
		return null;
	}

	@Override
	public String getPrimaryDataFieldName() {
		return null;
	}

	@Override
	public List<String> getAdditionalPrimaryDataFieldNames() {
		return null;
	}

	@Override
	public List<String> getAxisDataFieldsForPrimaryDataField(String primaryDataFieldName) {
		return null;
	}

	@Override
	public String getDefaultAxisDataFieldName() {
		return null;
	}

	@Override
	public Integer getDefaultAxisDimension(String primaryDataFieldName, String axisDataFieldName) {
		return null;
	}

	@Override
	public int[] getDimensionMappings(String primaryDataFieldName, String axisDataFieldName) {
		return null;
	}

	@Override
	public Boolean getUseDeviceNameInNXdata() {
		return null;
	}

	@Override
	public Object getPropertyValue(String propertyName) {
		return null;
	}
}
