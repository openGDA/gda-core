/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.detector.addetectorprovisional.plugin;

import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.addetectorprovisional.ADDetectorPlugin;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataChildNodeAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class ADMetadataPlugin implements ADDetectorPlugin {

	private boolean firstReadoutInScan;
	
	private NexusTreeProvider metaDataProvider;

	public ADMetadataPlugin(NexusTreeProvider metaDataProvider) {
		this.setMetaDataProvider(metaDataProvider);
	}
	
	public void setMetaDataProvider(NexusTreeProvider metaDataProvider) {
		this.metaDataProvider = metaDataProvider;
	}

	public NexusTreeProvider getMetaDataProvider() {
		return metaDataProvider;
	}
	
	//
	
	@Override
	public String getName() {
		return "metadata";
	}

	@Override
	public boolean willRequireCallbacks() {
		return false;
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection) throws Exception {
		firstReadoutInScan = true;
	}

	@Override
	public void prepareForLine() throws Exception {
	}

	@Override
	public void completeLine() throws Exception {
	}

	@Override
	public void completeCollection() throws Exception {
	}

	@Override
	public void atCommandFailure() throws Exception {
	}

	@Override
	public void stop() throws Exception {
	}
	
	@Override
	public List<String> getInputStreamExtraNames() {
		return Arrays.asList();
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList();
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		List<NXDetectorDataAppender> appenders = new ArrayList<NXDetectorDataAppender>();
		if (firstReadoutInScan) {
			INexusTree treeToAppend = getMetaDataProvider().getNexusTree();
			appenders.add(new NXDetectorDataChildNodeAppender(treeToAppend));
		} else {
			appenders.add(new NXDetectorDataNullAppender());
		}
		firstReadoutInScan = false;
		return appenders;
	}

}
