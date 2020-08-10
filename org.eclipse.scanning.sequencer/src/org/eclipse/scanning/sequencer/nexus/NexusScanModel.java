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

package org.eclipse.scanning.sequencer.nexus;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.dawnsci.nexus.IMultipleNexusDevice;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.builder.NexusMetadataProvider;

public class NexusScanModel {

	private String filePath;

	private final Map<ScanRole, List<INexusDevice<?>>> nexusDevices;

	private Optional<IMultipleNexusDevice> multipleNexusDevice = Optional.empty();

	private NexusScanInfo nexusScanInfo;

	private Set<String> templateFilePaths;

	private List<Collection<String>> dimensionNamesByIndex;

	private List<NexusMetadataProvider> nexusMetadataProviders;

	NexusScanModel(Map<ScanRole, List<INexusDevice<?>>> nexusDevices) {
		this.nexusDevices = nexusDevices;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Map<ScanRole, List<INexusDevice<?>>> getNexusDevices() {
		return nexusDevices;
	}

	public Optional<IMultipleNexusDevice> getMultipleNexusDevice() {
		return multipleNexusDevice;
	}

	public void setMultipleNexusDevice(Optional<IMultipleNexusDevice> multipleNexusDevice) {
		this.multipleNexusDevice = multipleNexusDevice;
	}

	public NexusScanInfo getNexusScanInfo() {
		return nexusScanInfo;
	}

	public void setNexusScanInfo(NexusScanInfo nexusScanInfo) {
		this.nexusScanInfo = nexusScanInfo;
	}

	public Set<String> getTemplateFilePaths() {
		if (templateFilePaths == null) {
			return Collections.emptySet();
		}
		return templateFilePaths;
	}

	public void setTemplateFilePaths(Set<String> templateFilePaths) {
		this.templateFilePaths = templateFilePaths;
	}

	public List<Collection<String>> getDimensionNamesByIndex() {
		return dimensionNamesByIndex;
	}

	public void setDimensionNamesByIndex(List<Collection<String>> dimensionNamesByIndex) {
		this.dimensionNamesByIndex = dimensionNamesByIndex;
	}

	public List<NexusMetadataProvider> getNexusMetadataProviders() {
		if (nexusMetadataProviders == null) {
			return Collections.emptyList();
		}

		return nexusMetadataProviders;
	}

	public void setNexusMetadataProviders(List<NexusMetadataProvider> nexusMetadataProviders) {
		this.nexusMetadataProviders = nexusMetadataProviders;
	}

}
