/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.ui.plan;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.diamond.daq.experiment.api.EventConstants.EXPERIMENT_CONTROLLER_TOPIC;
import static uk.ac.diamond.daq.experiment.api.EventConstants.EXPERIMENT_PLAN_TOPIC;
import static uk.ac.diamond.daq.experiment.api.Services.getExperimentService;
import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.publishEvent;
import static uk.ac.gda.ui.tool.ClientSWTElements.composite;
import static uk.ac.gda.ui.tool.ClientSWTElements.getImage;
import static uk.ac.gda.ui.tool.rest.ClientRestServices.getExperimentController;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.Finder;
import gda.rcp.views.Browser;
import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.experiment.api.ExperimentService;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanException;
import uk.ac.diamond.daq.experiment.api.plan.event.PlanStatusBean;
import uk.ac.diamond.daq.experiment.api.remote.PlanRequestHandler;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentEvent;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceDeleteEvent;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceEvent;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceSaveEvent;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.AcquisitionConfigurationView;
import uk.ac.gda.ui.tool.images.ClientImages;
import uk.ac.gda.ui.tool.spring.ClientRemoteServices;

/**
 * Through this view the user can create, edit, delete and run {@link ExperimentPlanBean}s
 * which describe a fully-automated experiment.
 * <p>
 * Note that while {@link AcquisitionConfigurationResourceEvent}s are published when configurations change,
 * they do not presently contain the resources' URLs (because they are handled by {@link ExperimentService}).
 */
public class PlanManagerView extends AcquisitionConfigurationView {

	public static final String ID = "uk.ac.diamond.daq.experiment.ui.plan.PlanManagerView";
	private static final String EXPERIMENT_ID = "";

	private static final Logger logger = LoggerFactory.getLogger(PlanManagerView.class);

	private Supplier<Composite> compositeForButtons;

	private PlanRequestHandler handler;
	private ISubscriber<IBeanListener<PlanStatusBean>> planListener;
	private ISubscriber<IBeanListener<ExperimentEvent>> experimentControllerListener;
	private PlanBrowser planBrowser;
	private boolean planRunning = false;

	private Button runButton;

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		try {
			createSubscribers();
		} catch (Exception e) {
			String message = "Could not create subscriber";
			UIHelper.showError(message, e);
			logger.error(message, e);
		}
		drawButtons(compositeForButtons.get());
	}

	@Override
	protected CompositeFactory getTopArea(Supplier<Composite> controlButtonsContainerSupplier) {
		compositeForButtons = controlButtonsContainerSupplier;
		return null;
	}

	private void drawButtons(Composite parent) {
		var composite = composite(parent, 2, false);

		var newButton = addButton(composite, "New", getImage(ClientImages.ADD));
		newButton.addSelectionListener(widgetSelectedAdapter(selection -> add()));

		runButton = addButton(composite, "Run", getImage(ClientImages.RUN));
		runButton.addSelectionListener(widgetSelectedAdapter(selection -> run(planBrowser.getSelectedPlan()) ));

		updateButtons();
	}

	private Button addButton(Composite parent, String text, Image icon) {
		var button = new Button(parent, SWT.PUSH);
		button.setText(text);
		button.setImage(icon);
		button.addDisposeListener(dispose -> icon.dispose());
		return button;
	}

	@Override
	protected Browser<?> getBrowser() {
		Map<String, Consumer<ExperimentPlanBean>> processors = new HashMap<>();
		processors.put("Edit", this::edit);
		processors.put("Delete", this::remove);

		planBrowser = new PlanBrowser(Optional.of(this::edit), Optional.of(processors));
		planBrowser.addIObserver((source, argument) -> updateButtons());
		return planBrowser;
	}

	private PlanRequestHandler getPlanRequestHandler() {
		if (handler == null) {
			handler = Finder.findSingleton(PlanRequestHandler.class);
		}
		return handler;
	}

	private void run(ExperimentPlanBean plan) {
		if (plan == null) {
			throw new ExperimentPlanException(
					"There is no plan selected. The UI should prevent this method from being called!");
		}
		try {
			getPlanRequestHandler().submit(plan);
		} catch (DeviceException e) {
			logger.error("Error starting experiment plan", e);
		}
	}

	private void add() {
		ExperimentPlanBean bean = new ExperimentPlanBean();
		if (openWizard(bean)) {
			getExperimentService().saveExperimentPlan(bean);
			publishEvent(new AcquisitionConfigurationResourceSaveEvent(this, bean.getUuid()));
		}
	}

	private void edit(ExperimentPlanBean bean) {
		final String originalName = bean.getPlanName(); // the edit could change this
		if (openWizard(bean)) {
			if (!bean.getPlanName().equals(originalName)) {
				getExperimentService().deleteExperimentPlan(originalName);
			}
			getExperimentService().saveExperimentPlan(bean);
			publishEvent(new AcquisitionConfigurationResourceSaveEvent(this, bean.getUuid()));
		}
	}

	private void remove(ExperimentPlanBean bean) {
		if (UIHelper.showConfirm("Do you want to delete experiment plan '" + bean.getPlanName() + "'?")) {
			getExperimentService().deleteExperimentPlan(bean.getPlanName());
			publishEvent(new AcquisitionConfigurationResourceDeleteEvent(this, bean.getUuid()));
		}
	}

	private boolean openWizard(ExperimentPlanBean planBean) {
		PlanSetupWizard planWizard = new PlanSetupWizard(EXPERIMENT_ID, planBean);
		WizardDialog wizardDialog = new WizardDialog(compositeForButtons.get().getShell(), planWizard);
		return wizardDialog.open() == Window.OK;
	}

	private void createSubscribers() throws URISyntaxException, EventException {
		planListener = SpringApplicationContextFacade.getBean(ClientRemoteServices.class)
						.createSubscriber(EXPERIMENT_PLAN_TOPIC);
		planListener.addListener(event -> {
			final PlanStatusBean bean = event.getBean();
			planRunning = bean.getStatus().isActive();
			Display.getDefault().syncExec(this::updateButtons);
		});

		experimentControllerListener = SpringApplicationContextFacade.getBean(ClientRemoteServices.class)
				.createSubscriber(EXPERIMENT_CONTROLLER_TOPIC);

		experimentControllerListener.addListener(event -> Display.getDefault().asyncExec(this::updateButtons));
	}

	private void updateButtons() {
		var somethingRunning = getExperimentController().isExperimentInProgress();
		final boolean planSelected = planBrowser.getSelectedPlan() != null;
		runButton.setEnabled(!somethingRunning && planSelected && !planRunning);
	}

	@Override
	public void dispose() {
		disconnect(planListener);
		disconnect(experimentControllerListener);
		super.dispose();
	}

	private void disconnect(ISubscriber<?> subscriber) {
		if (subscriber != null) {
			try {
				subscriber.disconnect();
			} catch (EventException e) {
				logger.error("Error disconnecting subscriber on topic {}", subscriber.getTopicName(), e);
			}
		}
	}

	@Override
	public void setFocus() {
		compositeForButtons.get().setFocus();
	}


}