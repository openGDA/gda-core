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

package uk.ac.diamond.daq.experiment.ui;

import static uk.ac.diamond.daq.experiment.api.EventConstants.EXPERIMENT_CONTROLLER_TOPIC;
import static uk.ac.gda.ui.tool.ClientSWTElements.STRETCH;
import static uk.ac.gda.ui.tool.ClientSWTElements.composite;
import static uk.ac.gda.ui.tool.ClientSWTElements.label;
import static uk.ac.gda.ui.tool.rest.ClientRestServices.getExperimentController;

import java.net.URISyntaxException;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentEvent;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentEvent.Transition;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.GDAClientRestException;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.images.ClientImages;
import uk.ac.gda.ui.tool.spring.ClientRemoteServices;

/**
 * Lets the user start and stop their experiment
 */
public class ExperimentManager implements CompositeFactory {

	private static final Logger logger = LoggerFactory.getLogger(ExperimentManager.class);

	private Text experimentName;
	private Button toggleExperiment;

	private ISubscriber<IBeanListener<ExperimentEvent>> experimentListener;

	@Override
	public Composite createComposite(Composite parent, int style) {

		var composite = composite(parent, 3, false);
		label(composite, "Experiment");

		experimentName = new Text(composite, SWT.BORDER);
		STRETCH.applyTo(experimentName);

		toggleExperiment = new Button(composite, SWT.PUSH);
		toggleExperiment.addListener(SWT.Selection, toggleExperimentListener);

		if (getExperimentController().isExperimentInProgress()) {
			experimentName.setText(getExperimentController().getExperimentName());
		}

		updateWidgets();

		try {
			connectExperimentListener();
		} catch (Exception e) {
			logger.error("Error subscribing to experiment controller messages", e);
		}

		parent.addDisposeListener(dispose -> disconnectExperimentListener());

		return composite;
	}

	private void connectExperimentListener() throws URISyntaxException, EventException {
		experimentListener = SpringApplicationContextFacade.getBean(ClientRemoteServices.class)
				.createSubscriber(EXPERIMENT_CONTROLLER_TOPIC);
		experimentListener.addListener(event -> processEvent(event.getBean()));
	}

	private void disconnectExperimentListener() {
		try {
			experimentListener.disconnect();
		} catch (EventException e) {
			logger.error("Error disconnecting experiment controller listener", e);
		}
	}

	private void processEvent(ExperimentEvent event) {
		var name = event.getTransition() == Transition.STARTED ? event.getExperimentName() : "";
		Display.getDefault().syncExec(() -> {
			experimentName.setText(name);
			updateWidgets();
		});
	}

	private void updateWidgets() {
		boolean running = getExperimentController().isExperimentInProgress();
		if (running) {
			ClientSWTElements.updateButton(toggleExperiment, ClientMessages.STOP, ClientMessages.STOP_EXPERIMENT, ClientImages.STOP);
		} else {
			ClientSWTElements.updateButton(toggleExperiment, ClientMessages.START, ClientMessages.START_EXPERIMENT, ClientImages.START);
		}
		experimentName.setEnabled(!running);
	}

	private Listener toggleExperimentListener = event -> {
		if (getExperimentController().isExperimentInProgress()) {
			// the button is ready to stop an experiment
			try {
				getExperimentController().stopExperiment();
			} catch (GDAClientRestException e) {
				UIHelper.showError("Cannot stop the Experiment", e);
			}
		} else {
			// the button is ready to start an experiment
			try {
				getExperimentController().startExperiment(experimentName.getText());
			} catch (GDAClientRestException e) {
				UIHelper.showError("Cannot start the Experiment", e);
			}
		}
		Display.getDefault().syncExec(this::updateWidgets);
	};
}
