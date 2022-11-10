/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.rcp.ncd.calibration.views;

import static java.lang.Math.abs;
import static java.util.Collections.emptyList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.jython.InterfaceProvider;
import gda.rcp.ncd.calibration.CalibrationSet;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.server.ncd.calibration.BraggCalibrationService;
import uk.ac.gda.server.ncd.calibration.CalibrationEdge;
import uk.ac.gda.server.ncd.calibration.CalibrationUpdate;
import uk.ac.gda.server.ncd.calibration.CalibrationUpdate.Update;
import uk.ac.gda.server.ncd.calibration.ScanProgress;

public class ClientBraggCalibrationModel implements BraggCalibrationModel {
	private static final Logger logger = LoggerFactory.getLogger(ClientBraggCalibrationModel.class);

	private BraggCalibrationService server;

	private CalibrationSet selectedSet;
	private Collection<CalibrationSet> activeEdges = emptyList();

	private Collection<CalibrationSet> availableEdges;

	private Set<CalibrationListener> listeners = new HashSet<>();

	public ClientBraggCalibrationModel(BraggCalibrationService service) {
		server = service;
		server.addIObserver(this::update);
		availableEdges = server.getEdges().stream()
				.map(CalibrationSet::new)
				.toList();
	}

	@Override
	public Collection<CalibrationSet> availableEdges() {
		logger.debug("Getting edges");
		return availableEdges;
	}

	@Override
	public void scanAll(Collection<CalibrationEdge> edges) {
		if (clientHasAuthorisation()) {
			logger.debug("Requesting scan of {} edges: {}", edges.size(), edges);
			Async.submit(() -> server.runEdgeScans(edges));
		} else {
			UIHelper.showError("Invalid Authorisation", "This client cannot run scans - check baton and user authorisation level");
		}
	}

	@Override
	public void setSelectedEdge(CalibrationSet calibration) {
		logger.debug("Setting selected edge to {}", calibration);
		if (selectedSet != calibration) {
			selectedSet = calibration;
			for (var listener: listeners) {
				listener.selectedEdgeChanged(calibration);
			}
		}
	}

	@Override
	public void setActiveEdges(Collection<CalibrationSet> edges) {
		logger.debug("Setting active edges: {}", edges);
		activeEdges = edges;
		for (var listener: listeners) {
			listener.activeEdgeChanged(edges);
		}
	}

	@Override
	public void newScanData(CalibrationSet edge) {
		logger.debug("Updating edge with new scan data");
		for (var listener: listeners) {
			listener.newScanData(edge);
		}
	}

	@Override
	public CalibrationSet getSelectedEdge() {
		return selectedSet;
	}

	@Override
	public Collection<CalibrationSet> getActiveEdges() {
		return activeEdges;
	}

	@Override
	public void addListener(CalibrationListener listener) {
		logger.debug("Adding listener: {}", listener);
		listeners.add(listener);
		listener.activeEdgeChanged(activeEdges);
	}

	@Override
	public void removeListener(CalibrationListener listener) {
		logger.debug("Removing listener: {}", listener);
		listeners.remove(listener);
	}

	@Override
	public String braggDataPath() {
		return server.braggDataPath();
	}

	@Override
	public String exafsPath() {
		return server.exafsDataPath();
	}

	private void update(Object src, Object event) {
		logger.info("Update {} from {}", event, src);
		if (event instanceof CalibrationUpdate.Update) {

		} else if (event instanceof ScanProgress) {
			var prog = (ScanProgress) event;
			if (prog.getType() == Update.FINISHED) {
				availableEdges.stream()
						.filter(e -> e.getEdge().getUid().equals(prog.getEdgeId()))
						.findFirst()
						.ifPresent(e -> {
							e.setDataFile(prog.getScanFile());
							newScanData(e);
						});
			}
		}
	}

	@Override
	public void featureChanged() {
		listeners.forEach(CalibrationListener::featureChanged);
	}

	@Override
	public boolean checkInterceptValue(double intercept) {
		return abs(intercept) < server.expectedInterceptLimit();
	}

	@Override
	public void setNewInterceptValue(double intercept) {
		var details = InterfaceProvider.getBatonStateProvider().getMyDetails();
		if (details.hasBaton() && details.getAuthorisationLevel() >= 3) {
			try {
				server.setIntercept(intercept);
			} catch (DeviceException de) {
				UIHelper.showError("Could not set the bragg offset value", de);
			}
		} else {
			UIHelper.showError("Authorisation Error",
					"This client cannot run calibration - check baton and authorisation level");
			throw new IllegalAccessError("User does not have permission to run calibration - check baton and authorisation level");
		}
	}

	private boolean clientHasAuthorisation() {
		var details = InterfaceProvider.getBatonStateProvider().getMyDetails();
		return details.hasBaton() && details.getAuthorisationLevel() >= server.braggProtectionLevel();
	}
}
