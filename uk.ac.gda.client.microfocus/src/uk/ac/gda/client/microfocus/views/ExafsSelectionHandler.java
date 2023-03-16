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

import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_LOCAL_NAME;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_SCAN_ROLE;
import static org.eclipse.dawnsci.nexus.NexusConstants.NXCLASS;
import static org.eclipse.dawnsci.nexus.NexusConstants.POSITIONER;

import java.nio.file.Paths;
import java.util.Map;
import java.util.Map.Entry;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.mapping.ui.IMapClickEvent;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.IFindInTree;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.TreeUtils;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.scanning.api.device.ScanRole;
import org.eclipse.scanning.api.ui.IStageScanConfiguration;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;

/**
 * A handler for map click events that updates the Exafs selection view with the
 * location clicked in the map.
 */
public class ExafsSelectionHandler implements EventHandler {

	private static final Logger logger = LoggerFactory.getLogger(ExafsSelectionHandler.class);

	private IStageScanConfiguration stageConfigurationService;
	private ILoaderService loaderService;

	@Override
	public void handleEvent(final Event event) {
		final IMapClickEvent mapClickEvent = (IMapClickEvent) event.getProperty("event");
		// we handle only single-click events
		if (!mapClickEvent.isDoubleClick()) {
			var clickEvent = mapClickEvent.getClickEvent();
			final double xLocation = clickEvent.getxValue();
			final double yLocation = clickEvent.getyValue();

			logger.debug("Received map click event with x={}, y={}",
					clickEvent.getxValue(), clickEvent.getyValue());

			final Double zLocation = getScannablePositionInNexus(stageConfigurationService.getAssociatedAxis(), mapClickEvent.getFilePath());

			PlatformUI.getWorkbench().getDisplay().asyncExec(() -> updateExafsSelectionView(xLocation, yLocation, zLocation));
		}
	}

	private void updateExafsSelectionView(Double xLocation, Double yLocation, Double zLocation) {
		final var locationArray = new Double[] { xLocation, yLocation, zLocation };

		var exafsSelectionView = (ExafsSelectionView) EclipseUtils.getActivePage().findView(ExafsSelectionView.ID);
		if (exafsSelectionView == null) return;

		exafsSelectionView.setSelectedPoint(locationArray);
	}

	/**
	 * Finds the position of a per-scan monitor in the given NeXus file
	 * TODO This is duplicated from StageMoveHandler. See DAQ-1631/DAQ-1445.
	 * @param scannable name of the per-scan monitor
	 * @param filePath
	 * @return the position; or the current scannable position if not found in file;
	 * 		or {@code null} if either argument is {@code null}
	 */
	private Double getScannablePositionInNexus(String scannable, String filePath) {
		if (scannable == null) return null;
		if (!validFilePath(filePath)) return currentPosition(scannable);

		logger.debug("Looking for position of scannable {} in file {}", scannable, filePath);
		try {

			final IDataHolder dataHolder = loaderService.getData(filePath, null);
			var tree = dataHolder.getTree();

			IFindInTree perScanMonitorFinder = new PerScanMonitorFinder(scannable);
			Map<String, NodeLink> nodeMap = TreeUtils.treeBreadthFirstSearch(tree.getGroupNode(), perScanMonitorFinder, true, null);
			if (nodeMap.isEmpty()) {
				logger.warn("Could not find position for scannable {} in file {}. Returning its current position",
					scannable, filePath);
				return currentPosition(scannable);
			}

			Entry<String, NodeLink> entry = nodeMap.entrySet().iterator().next();

			String datasetPath = "/" + entry.getKey() + "/" + NXpositioner.NX_VALUE;
			IDataset dataset = dataHolder.getDataset(datasetPath);
			return dataset.getDouble();

		} catch (Exception e) {
			logger.error("Error searching for scannable {} in file {}. Returning its current position",
					scannable, filePath, e);
			return currentPosition(scannable);
		}

	}

	private boolean validFilePath(String filePath) {
		return filePath != null && Paths.get(filePath).toFile().exists();
	}

	private Double currentPosition(String scannableName) {
		Scannable scannable = Finder.find(scannableName);
		try {
			return (Double) scannable.getPosition();
		} catch (DeviceException deviceException) {
			logger.error("Could not read position of scannable {}", scannableName, deviceException);
			return null;
		}
	}

	/**
	 * An implementation of {@link IFindInTree} that finds the NXpositioner group for a
	 * per-scan monitor with the given name in a nexus tree.
	 * <br>
	 * TODO This is duplicated from StageMoveHandler. See DAQ-1631/DAQ-1445.
	 */
	private final class PerScanMonitorFinder implements IFindInTree {

		private final String scannableName;

		private PerScanMonitorFinder(String scannableName) {
			this.scannableName = scannableName;
		}

		@Override
		public boolean found(NodeLink node) {
			if (node.getDestination() instanceof GroupNode groupNode) {
				return attributeHasValue(groupNode, NXCLASS, POSITIONER) &&								// we are looking for an NXpositioner
						attributeHasValue(groupNode, ATTRIBUTE_NAME_LOCAL_NAME, scannableName) &&	// with the scannable name provided
						attributeHasValue(groupNode, ATTRIBUTE_NAME_SCAN_ROLE, ScanRole.MONITOR_PER_SCAN.toString()); // whose role is a per-scan monitor
			}

			return false;
		}

		private boolean attributeHasValue(GroupNode groupNode, String attrName, String expectedName) {
			var attribute = groupNode.getAttribute(attrName);
			return attribute != null && attribute.getFirstElement() != null
					&& expectedName.equals(attribute.getFirstElement());
		}
	}

	// OSGi services
	public void setStageConfiguration(IStageScanConfiguration service) {
		stageConfigurationService = service;
	}

	public void setLoaderService(ILoaderService service) {
		loaderService = service;
	}

}
