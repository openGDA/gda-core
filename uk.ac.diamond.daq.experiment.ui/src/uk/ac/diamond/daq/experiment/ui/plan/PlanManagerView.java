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
import static uk.ac.diamond.daq.experiment.api.Services.getExperimentService;
import static uk.ac.diamond.daq.experiment.api.remote.EventConstants.EXPERIMENT_PLAN_TOPIC;
import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.STRETCH;
import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.publishEvent;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.Finder;
import gda.rcp.views.AcquisitionCompositeFactoryBuilder;
import uk.ac.diamond.daq.experiment.api.ExperimentService;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanException;
import uk.ac.diamond.daq.experiment.api.plan.event.PlanStatusBean;
import uk.ac.diamond.daq.experiment.api.remote.PlanRequestHandler;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceDeleteEvent;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceEvent;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceSaveEvent;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.composites.AcquisitionCompositeButtonGroupFactoryBuilder;
import uk.ac.gda.client.composites.AcquisitionsBrowserCompositeFactory;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.spring.ClientRemoteServices;

/**
 * Through this view the user can create, edit, delete and run {@link ExperimentPlanBean}s
 * which describe a fully-automated experiment.
 * <p>
 * Note that while {@link AcquisitionConfigurationResourceEvent}s are published when configurations change,
 * they do not presently contain the resources' URLs (because they are handled by {@link ExperimentService}).
 */
public class PlanManagerView extends ViewPart {

	public static final String ID = "uk.ac.diamond.daq.experiment.ui.plan.PlanManagerView";
	private static final String EXPERIMENT_ID = "";

	private static final Logger logger = LoggerFactory.getLogger(PlanManagerView.class);

	private Composite base;

	private PlanRequestHandler handler;
	private ISubscriber<IBeanListener<PlanStatusBean>> subscriber;
	private PlanBrowser planBrowser;
	private boolean planComplete;

	private Control runButton;

	@Override
	public void createPartControl(Composite parent) {

		try {
			createSubscriber();
		} catch (Exception e) {
			String message = "Could not create subscriber";
			UIHelper.showError(message, e);
			logger.error(message, e);
			return;
		}

		base = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(base);
		STRETCH.applyTo(base);

		Map<String, Consumer<ExperimentPlanBean>> processors = new HashMap<>();
		processors.put("Edit", this::edit);
		processors.put("Delete", this::remove);

		planBrowser = new PlanBrowser(Optional.of(this::edit), Optional.of(processors));

		Composite controls = new AcquisitionCompositeFactoryBuilder()
			.addAcquisitionButtonGroupFactoryBuilder(getAcquistionButtonGroupFactoryBuilder())
			.addBottomArea(this::getBrowserComposite)
			.build().createComposite(base, SWT.NONE);
		STRETCH.applyTo(controls);
	}

	private AcquisitionCompositeButtonGroupFactoryBuilder getAcquistionButtonGroupFactoryBuilder() {
		AcquisitionCompositeButtonGroupFactoryBuilder acquisitionButtonGroup = new AcquisitionCompositeButtonGroupFactoryBuilder();
		acquisitionButtonGroup.addNewSelectionListener(widgetSelectedAdapter(event -> add()));
		acquisitionButtonGroup.addRunSelectionListener(widgetSelectedAdapter(getRunConsumer()));
		return acquisitionButtonGroup;
	}

	private Composite getBrowserComposite(Composite parent, int style) {
		Composite browser = new AcquisitionsBrowserCompositeFactory<>(planBrowser).createComposite(parent, style);
		STRETCH.applyTo(browser);
		return browser;
	}

	private Consumer<SelectionEvent> getRunConsumer() {
		return event -> {
			if (runButton == null) {
				runButton = (Control) event.widget;
			}
			updateButtons();
			run(planBrowser.getSelectedPlan());
		};
	}

	private void run(ExperimentPlanBean plan) {
		if (plan == null) {
			throw new ExperimentPlanException(
					"There is no plan selected. The UI should prevent this method from being called!");
		}
		try {
			if (handler == null) {
				handler = Finder.findSingleton(PlanRequestHandler.class);
			}
			handler.submit(plan);
		} catch (DeviceException e) {
			throw new ExperimentPlanException("Error executing experiment plan '" + plan.getPlanName() + "'", e);
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
		WizardDialog wizardDialog = new WizardDialog(base.getShell(), planWizard);
		return wizardDialog.open() == Window.OK;
	}

	private void createSubscriber() throws URISyntaxException, EventException {
		subscriber = SpringApplicationContextFacade.getBean(ClientRemoteServices.class)
						.createSubscriber(EXPERIMENT_PLAN_TOPIC);
		subscriber.addListener(event -> {
			final PlanStatusBean bean = event.getBean();
			planComplete = bean.getStatus().isFinal();
			if (planComplete) {
				Display.getDefault().syncExec(this::updateButtons);
			} // Needs to be in thread with display updates
		});
	}

	private void updateButtons() {
		final boolean planSelected = planBrowser.getSelectedPlan() != null;
		runButton.setEnabled(planSelected && planComplete);
	}

	@Override
	public void dispose() {
		if (subscriber != null) {
			try {
				subscriber.disconnect();
			} catch (EventException e) {
				logger.error("Error disconnecting subscriber", e);
			}
		}
		super.dispose();
	}

	@Override
	public void setFocus() {
		base.setFocus();
	}
}