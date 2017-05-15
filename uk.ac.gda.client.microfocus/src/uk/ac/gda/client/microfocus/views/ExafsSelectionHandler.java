/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.client.microfocus.views;

import static org.eclipse.dawnsci.nexus.NexusBaseClass.NX_POSITIONER;

import java.util.Map;
import java.util.Map.Entry;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.mapping.ui.IMapClickEvent;
import org.eclipse.dawnsci.analysis.api.io.IRemoteDatasetService;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.IFindInTree;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.api.tree.TreeUtils;
import org.eclipse.dawnsci.analysis.tree.TreeToMapUtils;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.plotting.api.axis.ClickEvent;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IRemoteData;
import org.eclipse.scanning.api.ui.IStageScanConfiguration;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;

/**
 * A handler for map click events that updates the Exafs selection view with the
 * location clicked in the map.
 */
public class ExafsSelectionHandler implements EventHandler {

	/**
	 * An implementation of {@link IFindInTree} that finds the NXpositioner group for a
	 * metadata scannable with the given name in a nexus tree.
	 */
	private final class MetadataScannableFinder implements IFindInTree {
		private final String scannableName;

		private MetadataScannableFinder(String scannableName) {
			this.scannableName = scannableName;
		}

		@Override
		public boolean found(NodeLink node) {
			if (node.getDestination() instanceof GroupNode) {
				GroupNode groupNode = (GroupNode) node.getDestination();
				if (attributeHasValue(groupNode, ATTR_NAME_NX_CLASS, NX_POSITIONER.toString()) &&
						attributeHasValue(groupNode, ATTR_NAME_GDA_SCANNABLE_NAME, scannableName) &&
						attributeHasValue(groupNode, ATTR_NAME_GDA_SCAN_ROLE,
								ScanRole.MONITOR_PER_SCAN.toString().toLowerCase())) {
					return true;
				}
			}

			return false;
		}

		private boolean attributeHasValue(GroupNode groupNode, String attrName, String expectedName) {
			Attribute attribute = groupNode.getAttribute(attrName);
			return attribute != null && attribute.getFirstElement() != null &&
					expectedName.equals(attribute.getFirstElement());
		}
	}

	private static final String ATTR_NAME_GDA_SCANNABLE_NAME = "gda_scannable_name";

	private static final String ATTR_NAME_GDA_SCAN_ROLE = "gda_scan_role";

	private static final String ATTR_NAME_NX_CLASS = "NX_class";

	private static final Logger logger = LoggerFactory.getLogger(ExafsSelectionHandler.class);

	private IStageScanConfiguration stageConfiguration = null;

	private IRemoteDatasetService remoteDatasetService = null;

	public void setStageConfiguration(IStageScanConfiguration stageConfiguration) {
		this.stageConfiguration = stageConfiguration;
	}

	public IStageScanConfiguration getStageConfiguration() {
		return stageConfiguration;
	}

	public void setRemoteDatasetService(IRemoteDatasetService remoteDatasetService) {
		this.remoteDatasetService = remoteDatasetService;
	}

	@Override
	public void handleEvent(final Event event) {
		final IMapClickEvent mapClickEvent = (IMapClickEvent) event.getProperty("event");
		// we handle only single-click events
		if (!mapClickEvent.isDoubleClick()) {
			ClickEvent clickEvent = mapClickEvent.getClickEvent();
			final double xLocation = clickEvent.getxValue();
			final double yLocation = clickEvent.getyValue();

			logger.debug("Received map click event with x={}, y={}",
					clickEvent.getxValue(), clickEvent.getyValue());

			final Double zLocation = getZLocation(mapClickEvent.getFilePath());

			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					updateExafsSelectionView(xLocation, yLocation, zLocation);
				}
			});
		}
	}

	private void updateExafsSelectionView(Double xLocation, Double yLocation, Double zLocation) {
		final Double[] locationArray = new Double[] { xLocation, yLocation, zLocation };

		ExafsSelectionView exafsSelectionView = (ExafsSelectionView) EclipseUtils.getActivePage()
				.findView(ExafsSelectionView.ID);
		if (exafsSelectionView != null) {
			exafsSelectionView.setSelectedPoint(locationArray);
		}
	}


	/**
	 * Get the z position from the nexus file if possible.
	 * @param outputFilePath the path of the nexus file for the map
	 * @return
	 */
	private Double getZLocation(String outputFilePath) {
		IStageScanConfiguration stageConfig = getStageConfiguration();
		if (stageConfig == null) {
			// Mapping stage should be configured in spring using gda.util.osgi.OSGiServiceRegister
			logger.warn("No mapping stage configured. Using 0.0 as z position");
			return 0.0;
		}

		// Get the name of the z scannable with
		final String stageZName = stageConfig.getAssociatedAxis();
		if (stageZName == null) {
			logger.warn("No z-axis configured for mapping stage, using 0.0 as z position.");
			return 0.0;
		}

		Scannable zScannable = Finder.getInstance().findNoWarn(stageZName);
		if (zScannable == null) {
			logger.warn("Could not find scannable: " + stageZName);
			return 0.0;
		}

		if (outputFilePath == null) {
			// The output path doesn't exist, use current position of z scannable.
			logger.warn("No ouput path defined for current map. Using current position of scannable " + stageZName);
			try {
				Object pos = zScannable.getPosition();
				if (pos instanceof Double) {
					return (Double) pos;
				} else {
					logger.error("Could not get position of scannable " + stageZName + ". Value was not a double: " + pos);
					return 0.0;
				}
			} catch (DeviceException e) {
				logger.error("Could not get position of scannable " + stageZName, e);
				return 0.0;
			}
		}

		// Use the remote dataset service to get the z-position from the nexus file
		// get the hostname and port
		String hostname = LocalProperties.get(LocalProperties.GDA_DATASERVER_HOST);
		int port = Integer.parseInt(LocalProperties.get(LocalProperties.GDA_DATASERVER_PORT));
		try {
			// Find the NXpositioner group for the z scannable in the nexus tree
			// First get the tree from the remote dataset service
			IRemoteData remoteData = remoteDatasetService.createRemoteData(hostname, port);
			remoteData.setPath(outputFilePath);
			Map<String, Object> map = remoteData.getTree();
			Tree tree = TreeToMapUtils.mapToTree(map, outputFilePath);

			// Find the NXpositioner group for the z scannable
			IFindInTree zScannableFinder = new MetadataScannableFinder(stageZName);
			Map<String, NodeLink> nodeMap = TreeUtils.treeBreadthFirstSearch(tree.getGroupNode(), zScannableFinder, true, null);
			if (nodeMap.isEmpty()) throw new Exception(); // drop to catch block
			Entry<String, NodeLink> entry = nodeMap.entrySet().iterator().next();

			// Check the "value" data node is present
			GroupNode scannableGroup = (GroupNode) entry.getValue().getDestination();
			DataNode dataNode = scannableGroup.getDataNode(NXpositioner.NX_VALUE);
			if (dataNode == null || dataNode.getRank() > 0) throw new Exception(); // drop to catch block

			// Use the remote dataset service to load the dataset
			String datasetPath = "/" + entry.getKey() + "/" + NXpositioner.NX_VALUE;
			final IDatasetConnector data = remoteDatasetService.createRemoteDataset(hostname, port);
			data.setPath(outputFilePath);
			data.setDatasetName(datasetPath);
			data.connect();

			// Get the value from the dataset
			ILazyDataset dataset = data.getDataset();
			IDataset slice = dataset.getSlice();
			return slice.getDouble();
		} catch (Exception e) {
			logger.error("Could not get position of scannable " + stageZName + " from from output file " + outputFilePath);
			return 0.0;
		}
	}

}
