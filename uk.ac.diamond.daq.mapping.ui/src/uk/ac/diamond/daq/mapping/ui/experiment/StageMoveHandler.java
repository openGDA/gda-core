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
import static uk.ac.gda.ui.tool.ClientMessages.CANNOT_MOVE_STAGES;
import static uk.ac.gda.ui.tool.ClientMessages.CANNOT_MOVE_STAGES_SCAN_RUNNING_OR_PENDING;
import static uk.ac.gda.ui.tool.ClientMessagesUtility.getMessage;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
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
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.ScanRole;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.api.scan.event.IPositionerService;
import org.eclipse.scanning.api.ui.IStageScanConfiguration;
import org.eclipse.scanning.event.util.SubmissionQueueUtils;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;

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
	private IEventService eventService;
	private IPositioner positioner;

	@Override
	public void handleEvent(Event event) {
		final IMapClickEvent mapClickEvent = (IMapClickEvent) event.getProperty("event");
		// moveTo is only handled for double-clicks
		if (mapClickEvent.isDoubleClick()) {
			if (isScanRunning()) {
				final String title = getMessage(CANNOT_MOVE_STAGES);
				final String message = getMessage(CANNOT_MOVE_STAGES_SCAN_RUNNING_OR_PENDING);
				Display.getDefault().syncExec(() -> MessageDialog.openError(null, title, message));
				return;
			}
			final ClickEvent clickEvent = mapClickEvent.getClickEvent();

			String associatedAxisName = stageConfiguration.getAssociatedAxis();
			String filePath = mapClickEvent.getFilePath();

			Double associatedPosition = getScannablePositionInNexus(associatedAxisName, filePath);

			moveTo(clickEvent.getxValue(), clickEvent.getyValue(), associatedPosition);
		}
	}

	private void moveTo(final double xLocation, final double yLocation, final Double associatedLocation) {

		Map<String, Object> position = getPositionMap(xLocation, yLocation, associatedLocation);
		String moveSummary = summarise(position);

		// request confirmation to move
		boolean userConfirmedMove = confirmMove("Go here?", moveSummary);
		if (!userConfirmedMove) return;

		// Do the move
		try {
			getPositioner().setPosition(new MapPosition(position));
		} catch (ScanningException e) {
			logger.error("Could not perform stage move", e);
		} catch (InterruptedException interrupted) {
			logger.error("Stage move interrupted!", interrupted);
			Thread.currentThread().interrupt();
		}
	}

	private String summarise(Map<String, Object> position) {
		var summary = new StringBuilder("Please confirm the following move:\n");
		for (var entry : position.entrySet()) {
			summary.append(entry.getKey())
				.append(" to ")
				.append(entry.getValue().toString())
				.append("\n");
		}
		return summary.toString();
	}

	private Map<String, Object> getPositionMap(double xLocation, double yLocation, Double associatedLocation) {
		Map<String, Object> position = new HashMap<>();
		position.put(stageConfiguration.getPlotXAxisName(), xLocation);
		position.put(stageConfiguration.getPlotYAxisName(), yLocation);
		if (associatedLocation != null) {
			position.put(stageConfiguration.getAssociatedAxis(), associatedLocation);
		}
		return position;
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
	 * @return the position; or the current scannable position if not found in file;
	 * 		or {@code null} if either argument is {@code null}
	 */
	private Double getScannablePositionInNexus(String scannable, String filePath) {
		if (scannable == null) return null;
		if (!validFilePath(filePath)) return currentPosition(scannable);

		logger.debug("Looking for position of scannable {} in file {}", scannable, filePath);
		try {

			final IDataHolder dataHolder = loaderService.getData(filePath, null);
			Tree tree = dataHolder.getTree();

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
		} catch (DeviceException e) {
			logger.error("Could not read position of scannable {}", scannableName);
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

	private boolean isScanRunning() {
		return InterfaceProvider.getScanStatusHolder().isScanRunning()	// GDA 8 scans
				|| SubmissionQueueUtils.isJobRunningOrPending();	// GDA 9 scans
	}

	// Called by OSGi:
	public void setMappingStageConfiguration(IStageScanConfiguration stageConfiguration) {
		this.stageConfiguration = stageConfiguration;
	}

	public void setLoaderService(ILoaderService loaderService) {
		this.loaderService = loaderService;
	}

	public void setEventService(IEventService eventService) {
		this.eventService = eventService;
	}

	private IPositionerService getPositionerService() throws ScanningException {
		try {
			var remoteUri = new URI(LocalProperties.getActiveMQBrokerURI());
			return eventService.createRemoteService(remoteUri, IRunnableDeviceService.class);
		} catch (EventException | URISyntaxException e) {
			throw new ScanningException("Could not create remote positioner service", e);
		}
	}

	private IPositioner getPositioner() throws ScanningException {
		if (positioner == null) {
			positioner = getPositionerService().createPositioner(StageMoveHandler.class.getSimpleName());
		}
		return positioner;
	}

}
