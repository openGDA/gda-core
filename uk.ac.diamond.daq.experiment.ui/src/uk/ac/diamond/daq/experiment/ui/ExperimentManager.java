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

import static uk.ac.gda.ui.tool.ClientSWTElements.createClientButton;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGroup;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientLabel;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientText;
import static uk.ac.gda.ui.tool.rest.ClientRestServices.getExperimentController;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.WidgetUtilities;
import uk.ac.gda.ui.tool.images.ClientImages;

/**
 * Lets the user start and stop their experiment
 */
public class ExperimentManager implements CompositeFactory {

	private Text experimentName;
	private Button toggleExperiment;

	@Override
	public Composite createComposite(Composite parent, int style) {
		Composite composite = createClientCompositeWithGridLayout(parent, style, 1);
		ClientSWTElements.createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(composite);

		Group group = createClientGroup(parent, style, 2, ClientMessages.EXPERIMENT);
		ClientSWTElements.createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(group);

		Label name = createClientLabel(group, style, ClientMessages.NAME);
		ClientSWTElements.createClientGridDataFactory().indent(5, SWT.DEFAULT).applyTo(name);

		experimentName = createClientText(group, style, ClientMessages.EMPTY_MESSAGE, (VerifyListener) null);
		ClientSWTElements.createClientGridDataFactory().hint(ClientSWTElements.DEFAULT_TEXT_SIZE)
				.applyTo(experimentName);

		toggleExperiment = createClientButton(group, SWT.TOGGLE, ClientMessages.START, ClientMessages.START_EXPERIMENT, ClientImages.START);
		ClientSWTElements.createClientGridDataFactory().indent(5, SWT.DEFAULT).applyTo(toggleExperiment);

		WidgetUtilities.addWidgetDisposableListener(toggleExperiment, SWT.Selection, toggleExperimentListener);

		if (getExperimentController().isExperimentInProgress()) {
			experimentName.setText(getExperimentController().getExperimentName());
			updateWidgets();
		}

		return composite;
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
			} catch (ExperimentControllerException e) {
				UIHelper.showError("Cannot stop the Experiment", e);
			}
		} else {
			// the button is ready to start an experiment
			try {
				getExperimentController().startExperiment(experimentName.getText());
			} catch (ExperimentControllerException e) {
				UIHelper.showError("Cannot start the Experiment", e);
			}
		}
		Display.getDefault().syncExec(this::updateWidgets);
	};
}
