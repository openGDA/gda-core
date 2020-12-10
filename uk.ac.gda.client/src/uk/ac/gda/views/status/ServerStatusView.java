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

package uk.ac.gda.views.status;

import static gda.configuration.properties.LocalProperties.GDA_SERVER_HOST;
import static gda.configuration.properties.LocalProperties.GDA_SERVER_STATUS_PORT;
import static gda.configuration.properties.LocalProperties.GDA_SERVER_STATUS_PORT_DEFAULT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import gda.beamline.health.BeamlineHealthComponentResult;
import gda.beamline.health.BeamlineHealthMonitor;
import gda.beamline.health.BeamlineHealthResult;
import gda.beamline.health.BeamlineHealthState;
import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.concurrent.Async.ListeningScheduledFuture;
import uk.ac.gda.client.viewer.FourStateDisplay;

/**
 * Show the current status of the GDA server
 * <p>
 * This view polls the server status port for the server status, as configured in the {@link BeamlineHealthMonitor} for
 * the beamline.
 */
public class ServerStatusView {
	private static final Logger logger = LoggerFactory.getLogger(ServerStatusView.class);

	/** The location of the server status port */
	private static final String SERVER_HOST = LocalProperties.get(GDA_SERVER_HOST);
	/** Server status port */
	private static final int SERVER_STATUS_PORT = LocalProperties.getAsInt(GDA_SERVER_STATUS_PORT, GDA_SERVER_STATUS_PORT_DEFAULT);

	/** Time between attempts to get server status in sec */
	private static final long POLLING_INTERVAL_SEC = 2;

	/** Shows the time the status was last updated */
	private Label lastUpdateTime;

	/** Overall beamline state */
	private FourStateDisplay beamlineStatusDisplay;

	/** Managers for each configured component */
	private Map<String, ComponentIndicatorManager> componentIndicatorManagers;

	private SimpleDateFormat timeFormatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");

	private ListeningScheduledFuture<?> pollingFuture;

	@PostConstruct
	public void createView(Composite parent) {
		logger.debug("Creating server status view");

		parent.setLayout(new FillLayout());
		parent.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		parent.setBackgroundMode(SWT.INHERIT_FORCE);

		final ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayout(new FillLayout());
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		final Composite content = new Composite(scrolledComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(content);

		// Show the time the status was last updated
		final Composite lastUpdateComposite = new Composite(content, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(lastUpdateComposite);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(lastUpdateComposite);

		final Label lastUpdateLabel = new Label(lastUpdateComposite, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(lastUpdateLabel);
		lastUpdateLabel.setText("Last updated:");

		lastUpdateTime = new Label(lastUpdateComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(lastUpdateTime);

		// Show status of beamline
		final Composite statusComposite = new Composite(content, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(statusComposite);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(statusComposite);

		final Label beamlineStatusLabel = new Label(statusComposite, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(beamlineStatusLabel);
		beamlineStatusLabel.setText("Beamline:");

		beamlineStatusDisplay = new FourStateDisplay(statusComposite, null, null, null, "condition disabled");

		// Show status of each configured component
		try {
			createComponentIndicators(content);
		} catch (Exception e) {
			final String message = "Error getting server status";
			lastUpdateTime.setText(message);
			logger.error(message, e);
			return;
		}

		scrolledComposite.setContent(content);
		scrolledComposite.setMinSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		// Start polling for server status
		pollingFuture = Async.scheduleWithFixedDelay(this::showServerStatus, 0, POLLING_INTERVAL_SEC, TimeUnit.SECONDS, "Server status");
	}

	// Show status of components
	private void createComponentIndicators(Composite parent) throws IOException {
		final BeamlineHealthResult beamlineHealthResult = getServerStatus();
		final List<BeamlineHealthComponentResult> componentResults = beamlineHealthResult.getComponentResults();
		componentIndicatorManagers = new HashMap<>(componentResults.size());

		final Composite indicatorsComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(indicatorsComposite);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(indicatorsComposite);

		// For each component show name, position & state relative to the configured condition
		for (BeamlineHealthComponentResult componentResult : componentResults) {
			final String componentName = componentResult.getComponentName();
			createLabel(indicatorsComposite, SWT.DEFAULT, componentName);
			final Label positionLabel = createLabel(indicatorsComposite, 100, componentResult.getCurrentState());
			final FourStateDisplay statusIndicator = new FourStateDisplay(indicatorsComposite, "", "", "", "");
			componentIndicatorManagers.put(componentName, new ComponentIndicatorManager(positionLabel, statusIndicator));
		}
	}

	/**
	 * Display the overall status of the beamline, and the status of each relevant component.
	 */
	private void showServerStatus() {
		try {
			final BeamlineHealthResult beamlinehealthResult = getServerStatus();
			Display.getDefault().asyncExec(() -> updateStatusDisplay(beamlinehealthResult));
		} catch (Exception e) {
			logger.error("Error getting server input stream", e);
		}
	}

	/**
	 * Get beamline status from the server
	 *
	 * @return beamline status from the server status socket
	 * @throws IOException
	 */
	private BeamlineHealthResult getServerStatus() throws IOException {
		try (final Socket statusSocket = new Socket(SERVER_HOST, SERVER_STATUS_PORT);
				final PrintWriter out = new PrintWriter(statusSocket.getOutputStream(), true);
				final BufferedReader in = new BufferedReader(new InputStreamReader(statusSocket.getInputStream()))) {
			out.println(BeamlineHealthResult.COMMAND);
			return new ObjectMapper().readValue(in.readLine(), BeamlineHealthResult.class);
		}
	}

	/**
	 * Update status of the beamline and each component from server status result
	 *
	 * @param beamlineHealthResult
	 *            status as returned by server
	 */
	private void updateStatusDisplay(BeamlineHealthResult beamlineHealthResult) {
		// Update the overall server status
		final BeamlineHealthState healthState = beamlineHealthResult.getBeamlineHealthState();
		final String message = beamlineHealthResult.getMessage();
		if (healthState == BeamlineHealthState.OK) {
			beamlineStatusDisplay.setGreen(message);
		} else if (healthState == BeamlineHealthState.WARNING) {
			beamlineStatusDisplay.setYellow(message);
		} else {
			beamlineStatusDisplay.setRed(message);
		}

		// Update the status of the individual components
		for (BeamlineHealthComponentResult componentResult : beamlineHealthResult.getComponentResults()) {
			final String componentName = componentResult.getComponentName();
			final ComponentIndicatorManager indicatorManager = componentIndicatorManagers.get(componentName);
			if (indicatorManager == null) {
				logger.warn("No indicator found for {}", componentName);
			} else {
				indicatorManager.updateComponentDisplay(componentResult);
			}
		}

		lastUpdateTime.setText(timeFormatter.format(System.currentTimeMillis()));
	}

	private static Label createLabel(Composite parent, int widthHint, String message) {
		final Label label = new Label(parent, SWT.NONE);
		GridDataFactory.swtDefaults().hint(widthHint, SWT.DEFAULT).applyTo(label);
		label.setText(message);
		return label;
	}

	@PreDestroy
	public void onDispose() {
		if (pollingFuture != null) {
			pollingFuture.cancel(true);
		}
	}

	/**
	 * Class to manage the GUI for a single component<br>
	 * This allows us to update the GUI elements every time the server is polled.
	 */
	private static class ComponentIndicatorManager {
		private final Label positionLabel;
		private final FourStateDisplay statusIndicator;

		public ComponentIndicatorManager(Label positionLabel, FourStateDisplay statusIndicator) {
			this.positionLabel = positionLabel;
			this.statusIndicator = statusIndicator;
		}

		public void updateComponentDisplay(BeamlineHealthComponentResult componentResult) {
			final BeamlineHealthState healthState = componentResult.getComponentHealthState();
			if (healthState == BeamlineHealthState.OK) {
				statusIndicator.setGreen();
			} else if (healthState == BeamlineHealthState.WARNING) {
				statusIndicator.setYellow(componentResult.getErrorMessage());
			} else if (healthState == BeamlineHealthState.ERROR) {
				statusIndicator.setRed(componentResult.getErrorMessage());
			} else {
				statusIndicator.setGrey();
			}
			positionLabel.setText(componentResult.getCurrentState());
		}
	}
}
