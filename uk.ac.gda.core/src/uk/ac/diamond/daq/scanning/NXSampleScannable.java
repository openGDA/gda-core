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

package uk.ac.diamond.daq.scanning;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXsample;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;

public class NXSampleScannable implements IScannable<Object>, INexusDevice<NXsample> {


	private String scannableName;
	private NXSampleProvider provider;

	public NXSampleScannable(String scannableName, String sampleName, NXsample sampleNode) {
		this.scannableName = scannableName;
		provider = new NXSampleProvider(sampleName,sampleNode);
	}

	public void updateNode(String sampleName, NXsample sampleNode) {
		provider = new NXSampleProvider(sampleName,sampleNode);
	}

	@Override
	public void setLevel(int level) {
		//level does nothing here
	}

	@Override
	public int getLevel() {
		return 0;
	}

	@Override
	public String getName() {
		return scannableName;
	}

	@Override
	public void setName(String name) {
		this.scannableName = name;

	}

	@Override
	public NexusObjectProvider<NXsample> getNexusProvider(NexusScanInfo info) throws NexusException {
		return provider;
	}

	@Override
	public Object getPosition() throws ScanningException {
		return null;
	}

	@Override
	public Object setPosition(Object value, IPosition position) throws ScanningException {
		return null;
	}

	private class NXSampleProvider implements NexusObjectProvider<NXsample>{

		private String name;
		private NXsample nxsample;

		public NXSampleProvider(String name, NXsample sampleNode) {
			this.name = name;
			this.nxsample = sampleNode;
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
		public NXsample getNexusObject() {
			return nxsample;
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
}
