/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

import java.io.Serializable;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.scanning.api.AbstractScannable;
import org.eclipse.scanning.api.annotation.scan.PrepareScan;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * Class to allow a snapshot of latest detector data to be recorded in the Nexus file as metadata at the start of a scan.
 */
public class DetectorCollectionScannable extends AbstractScannable<Object>implements INexusDevice<NXcollection>, Configurable {

	private static final Logger logger = LoggerFactory.getLogger(DetectorCollectionScannable.class);

	/** Name of the detector object to collect data from */
	private Detector detector;

	/** The name (path) of the group in the Nexus file where the data should be written to */
	private String groupName = "";

	/**
	 * Register with NexusDeviceService so NexusDataWriter can access it and use it to write metadata.
	 */
	@Override
	public void configure() {
		ServiceProvider.getService(INexusDeviceService.class).register(this);
	}

	/**
	 * This is invoked shortly before the main scan is configured - If no detector is set then throw an exception here
	 * to prevent issues when the snapshot is attempted to be taken
	 */
	@PrepareScan
	public void configureAndCollect() {
		if (detector == null) {
			throw new IllegalArgumentException("Cannot use "+getName()+" in scan - detector object has not been set");
		}
	}

	@Override
	public NexusObjectProvider<NXcollection> getNexusProvider(NexusScanInfo info) throws NexusException {
		logger.info("Collecting data from {} detector", detector.getName());
		final NXcollection nodeCollection = NexusNodeFactory.createNXcollection();
		try {
			Object detectorData = detector.readout();
			if (detectorData instanceof NexusTreeProvider provider) {
				extractData(nodeCollection, provider.getNexusTree());
			} else {
				logger.warn("Cannot add data for {} - it's 'readout' method does not return a NexusTreeProvider", detector.getName());
			}
		} catch (DeviceException e) {
			throw new NexusException("Problem collecting data from "+getName()+" at start of scan");
		}

		String nodeName = StringUtils.defaultIfEmpty(groupName, detector.getName());
		final NexusObjectWrapper<NXcollection> nexusObjectWrapper =
				new NexusObjectWrapper<>(nodeName, nodeCollection);
		nexusObjectWrapper.setCategory(NexusBaseClass.NX_INSTRUMENT);
		return nexusObjectWrapper;
	}

	private void extractData(final NXcollection nodeCollection, INexusTree nexusTree) {
		var parentNode = nexusTree.getChildNode(0);
		int numNodes = parentNode.getNumberOfChildNodes();
		for(int i=0; i<numNodes; i++) {
			var childNode = parentNode.getChildNode(i);
			logger.debug("Adding data from NeuxTree node {}", childNode.getName());

			// Extract the dataset and attributes
			Map<String, Serializable> attrData = childNode.getAttributes();
			var dataset = childNode.getData().toDataset();
			dataset.setName(childNode.getName());

			// Create DataNode
			var dataNode = createDataNode(dataset, attrData);

			// Add to the collection of nodes
			nodeCollection.addDataNode(dataset.getName(), dataNode);
		}
	}

	private DataNode createDataNode(final Dataset detectorData, Map<String, Serializable> attributes) {
		final DataNode dataNode = NexusNodeFactory.createDataNode();
		dataNode.setDataset(detectorData);
		// Add any attributes
		if (attributes != null) {
			for(var attr : attributes.entrySet()) {
				dataNode.addAttribute(TreeFactory.createAttribute(attr.getKey(), attr.getValue()));
			}
		}
		return dataNode;
	}

	@Override
	public Object getPosition() throws ScanningException {
		return null;
	}

	@Override
	public Object setPosition(Object value, IPosition position) throws ScanningException {
		return null;
	}

	@Override
	public void abort() throws ScanningException, InterruptedException {
	}

	@Override
	public boolean isConfigured() {
		return false;
	}

	@Override
	public void reconfigure() throws FactoryException {
	}

	@Override
	public boolean isConfigureAtStartup() {
		return true;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Detector getDetector() {
		return detector;
	}

	public void setDetector(Detector detector) {
		this.detector = detector;
	}


}
