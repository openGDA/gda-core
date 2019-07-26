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

import static org.eclipse.dawnsci.nexus.NexusConstants.NXCLASS;
import static org.eclipse.dawnsci.nexus.NexusConstants.POSITIONER;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.mapping.ui.IMapClickEvent;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.IFindInTree;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.api.tree.TreeUtils;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.plotting.api.axis.ClickEvent;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.scanning.api.device.ScanRole;
import org.eclipse.scanning.api.ui.IStageScanConfiguration;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			ClickEvent clickEvent = mapClickEvent.getClickEvent();
			final double xLocation = clickEvent.getxValue();
			final double yLocation = clickEvent.getyValue();

			logger.debug("Received map click event with x={}, y={}",
					clickEvent.getxValue(), clickEvent.getyValue());

			final Double zLocation = getScannablePositionInNexus(stageConfigurationService.getAssociatedAxis(), mapClickEvent.getFilePath());

			PlatformUI.getWorkbench().getDisplay().asyncExec(() -> updateExafsSelectionView(xLocation, yLocation, zLocation));
		}
	}

	private void updateExafsSelectionView(Double xLocation, Double yLocation, Double zLocation) {
		final Double[] locationArray = new Double[] { xLocation, yLocation, zLocation };

		final String sampleStagePrefix = stageConfigurationService.getPlotXAxisName().substring(0, 2);
		boolean validPrefix = Arrays.asList(stageConfigurationService.getPlotXAxisName(),
											stageConfigurationService.getPlotYAxisName(),
											stageConfigurationService.getAssociatedAxis()).stream()
											.allMatch(axis -> axis.startsWith(sampleStagePrefix));

		ExafsSelectionView exafsSelectionView = (ExafsSelectionView) EclipseUtils.getActivePage().findView(ExafsSelectionView.ID);
		if (exafsSelectionView == null) return;

		exafsSelectionView.setSelectedPoint(locationArray);
		if (validPrefix) {
			exafsSelectionView.setSampleStagePrefix(sampleStagePrefix);
		} else {
			logger.warn("Inconsistent axes selected");
			exafsSelectionView.setSampleStagePrefix(null);
		}
	}

	/**
	 * Finds the position of a per-scan monitor in the given NeXus file
	 * <br>
	 * TODO This is duplicated from StageMoveHandler. See DAQ-1631/DAQ-1445.
	 * @param scannable name of the per-scan monitor
	 * @param filePath
	 * @return the position; or {@code null} if either argument is {@code null}, or the position is not found.
	 */
	private Double getScannablePositionInNexus(String scannable, String filePath) {
		if (scannable == null || filePath == null || filePath.isEmpty()) return null;
		logger.debug("Looking for position of scannable {} in file {}", scannable, filePath);
		try {

			final IDataHolder dataHolder = loaderService.getData(filePath, null);
			Tree tree = dataHolder.getTree();

			IFindInTree perScanMonitorFinder = new PerScanMonitorFinder(scannable);
			Map<String, NodeLink> nodeMap = TreeUtils.treeBreadthFirstSearch(tree.getGroupNode(), perScanMonitorFinder, true, null);
			Entry<String, NodeLink> entry = nodeMap.entrySet().iterator().next();

			String datasetPath = "/" + entry.getKey() + "/" + NXpositioner.NX_VALUE;
			IDataset dataset = dataHolder.getDataset(datasetPath);
			return dataset.getDouble();

		} catch (Exception e) {
			logger.error("Could not find position for scannable {} in file {}", scannable, filePath, e);
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

		private static final String ATTR_NAME_GDA_SCANNABLE_NAME = "gda_scannable_name";
		private static final String ATTR_NAME_GDA_SCAN_ROLE = "gda_scan_role";

		private final String scannableName;

		private PerScanMonitorFinder(String scannableName) {
			this.scannableName = scannableName;
		}

		@Override
		public boolean found(NodeLink node) {
			if (node.getDestination() instanceof GroupNode) {
				GroupNode groupNode = (GroupNode) node.getDestination();
				return attributeHasValue(groupNode, NXCLASS, POSITIONER) &&								// we are looking for an NXpositioner
						attributeHasValue(groupNode, ATTR_NAME_GDA_SCANNABLE_NAME, scannableName) &&	// with the scannable name provided
						attributeHasValue(groupNode, ATTR_NAME_GDA_SCAN_ROLE,							// whose role is a per-scan monitor
								ScanRole.MONITOR_PER_SCAN.toString().toLowerCase());
			}

			return false;
		}

		private boolean attributeHasValue(GroupNode groupNode, String attrName, String expectedName) {
			Attribute attribute = groupNode.getAttribute(attrName);
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
