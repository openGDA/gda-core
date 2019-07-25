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

package uk.ac.diamond.daq.mapping.ui.experiment;

import static org.eclipse.dawnsci.nexus.NexusConstants.NXCLASS;
import static org.eclipse.dawnsci.nexus.NexusConstants.POSITIONER;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.scanning.api.device.ScanRole;
import org.eclipse.scanning.api.ui.IStageScanConfiguration;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;

/**
 * Handler for double-click events from MappedDataView. Moves the stage i.e.
 * fast and slow axes to the map coordinates where the double click occurred
 * (after receiving user confirmation).
 * <p>
 * If an associated axis is configured in the {@link IStageScanConfiguration}
 * and was included as a per-scan monitor in the active nexus file,
 * this event handler will retrieve its position and include it in this move.
 */
public class StageMoveHandler implements EventHandler {

	private static final Logger logger = LoggerFactory.getLogger(StageMoveHandler.class);

	private IStageScanConfiguration stageConfiguration;
	private ILoaderService loaderService;

	@Override
	public void handleEvent(Event event) {
		final IMapClickEvent mapClickEvent = (IMapClickEvent) event.getProperty("event");
		// moveTo is only handled for double-clicks
		if (mapClickEvent.isDoubleClick()) {
			final ClickEvent clickEvent = mapClickEvent.getClickEvent();

			String associatedAxisName = stageConfiguration.getAssociatedAxis();
			String filePath = mapClickEvent.getFilePath();

			Double associatedPosition = getScannablePositionInNexus(associatedAxisName, filePath);

			moveTo(clickEvent.getxValue(), clickEvent.getyValue(), associatedPosition);
		}
	}

	private void moveTo(final double xLocation, final double yLocation, final Double associatedLocation) {

		String fastName = stageConfiguration.getPlotXAxisName();
		String slowName = stageConfiguration.getPlotYAxisName();
		String associatedName = stageConfiguration.getAssociatedAxis();

		String moveSummary = "Please confirm the following move:\n"
				+ fastName + " to " + xLocation + "\n"
				+ slowName + " to " + yLocation
				+ (associatedLocation == null ? "" : "\n" + associatedName + " to " + associatedLocation);

		// request confirmation to move
		boolean userConfirmedMove = confirmMove("Go here?", moveSummary);
		if (!userConfirmedMove) return;

		// Do the move
		try {
			Scannable fastAxis = Finder.getInstance().find(fastName);
			fastAxis.asynchronousMoveTo(xLocation);
			Scannable slowAxis = Finder.getInstance().find(slowName);
			slowAxis.asynchronousMoveTo(yLocation);
			if (associatedLocation != null) {
				Scannable thirdAxis = Finder.getInstance().find(associatedName);
				thirdAxis.asynchronousMoveTo(associatedLocation);
			}
		} catch (DeviceException e) {
			logger.error("Error encountered while moving stage", e);
		}

	}

	private boolean confirmMove(String title, String message) {
		final AtomicBoolean moveConfirmed = new AtomicBoolean();
		Display.getDefault().syncExec(() -> moveConfirmed.set(MessageDialog.openConfirm(null, title, message)));
		return moveConfirmed.get();
	}

	/**
	 * Finds the position of a per-scan monitor in the given NeXus file
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


	// Called by OSGi:
	public void setMappingStageConfiguration(IStageScanConfiguration stageConfiguration) {
		this.stageConfiguration = stageConfiguration;
	}

	public void setLoaderService(ILoaderService loaderService) {
		this.loaderService = loaderService;
	}

}
