/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.ui.plan.setup;

import static uk.ac.gda.ui.tool.ClientSWTElements.STRETCH;
import static uk.ac.gda.ui.tool.ClientSWTElements.composite;
import static uk.ac.gda.ui.tool.ClientSWTElements.label;

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffect;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import gda.factory.Finder;
import uk.ac.diamond.daq.experiment.api.ExperimentService;
import uk.ac.diamond.daq.experiment.api.driver.DriverSignal;
import uk.ac.diamond.daq.experiment.api.driver.ExperimentDriver;
import uk.ac.diamond.daq.experiment.api.plan.DriverBean;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;
import uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils;
import uk.ac.diamond.daq.experiment.ui.driver.ExperimentDriverDialog;
import uk.ac.diamond.daq.experiment.ui.plan.preview.PlanPreviewer;
import uk.ac.diamond.daq.experiment.ui.plan.preview.PlotControllerImpl;
import uk.ac.diamond.daq.experiment.ui.plan.setup.tree.PlanValidator;

public class PlanSetupDialog extends Dialog {

	private GridDataFactory greedy = STRETCH.copy().align(SWT.FILL, SWT.FILL).grab(true, true);

	private ExperimentPlanBean bean;

	private DriverBean cachedDriverBean;

	private PlanParametersControls parametersControls;

	private DataBindingContext dbc = new DataBindingContext();

	private PlanValidator validator = new PlanValidator();

	public PlanSetupDialog(Shell parentShell, ExperimentPlanBean bean) {
		super(parentShell);
		this.bean = bean;
		cachedDriverBean = copy(bean.getDriverBean());
		parametersControls = new PlanParametersControls(bean);
	}

	/**
	 * @return a field-by-field copy of the given bean, or an empty instance if {@code bean == null}
	 */
	private DriverBean copy(DriverBean bean) {
		if (bean == null) return new DriverBean();
		DriverBean copy = new DriverBean();
		copy.setDriver(bean.getDriver());
		copy.setProfile(bean.getProfile());
		return copy;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Automated experiment plan configuration");
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Point getInitialSize() {
		return new Point(1300, 800);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		var dialogArea = (Composite) super.createDialogArea(parent);
		GridLayoutFactory.swtDefaults().applyTo(dialogArea);

		experimentMetadata(dialogArea);
		experimentParameters(dialogArea);

		return dialogArea;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);

		PropertyChangeListener toggleOkButton = event -> {
			var ok = getButton(IDialogConstants.OK_ID);
			ok.setEnabled(validator.validate(bean));
		};


		bean.addPropertyChangeListener(toggleOkButton);
		parent.addDisposeListener(dispose -> bean.removePropertyChangeListener(toggleOkButton));

		toggleOkButton.propertyChange(null);
	}

	private void experimentMetadata(Composite parent) {
		var head = composite(parent, 2);
		nameAndDescription(head);
		experimentDriver(head);
	}

	private void nameAndDescription(Composite parent) {
		var section = section(parent, "Experiment details", false);

		var composite = composite(section, 2, false);
		greedy.applyTo(composite);

		label(composite, "Experiment name");
		var experimentName = new Text(composite, SWT.BORDER);
		STRETCH.applyTo(experimentName);

		var descriptionLabel = label(composite, "Description");
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.TOP).applyTo(descriptionLabel);

		var description = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		STRETCH.copy().grab(true, true).align(SWT.FILL, SWT.FILL).hint(SWT.DEFAULT, 75).applyTo(description);

		IObservableValue<String> nameInWidget = WidgetProperties.text(SWT.Modify).observe(experimentName);
		IObservableValue<String> nameInBean = BeanProperties.value("planName", String.class).observe(bean);
		dbc.bindValue(nameInWidget, nameInBean);

		IObservableValue<String> descriptionInWidget = WidgetProperties.text(SWT.Modify).observe(description);
		IObservableValue<String> descriptionInBean = BeanProperties.value("planDescription", String.class).observe(bean);
		dbc.bindValue(descriptionInWidget, descriptionInBean);
	}

	private Composite section(Composite parent, String title, boolean verticalStretch) {
		var section = composite(parent, 1);
		if (verticalStretch) {
			greedy.applyTo(section);
		} else {
			STRETCH.copy().align(SWT.FILL, SWT.TOP).grab(true, true).applyTo(section);
		}

		var dividor = composite(section, 2, false);
		label(dividor, title);
		STRETCH.applyTo(new Label(dividor, SWT.SEPARATOR | SWT.HORIZONTAL));
		return section;
	}

	private void experimentDriver(Composite parent) {

		var section = section(parent, "Experiment driver", false);

		var composite = composite(section, 2, false);
		var includeButton = new Button(composite, SWT.CHECK);
		includeButton.setText("Use experiment driver");

		var configure = new Button(composite, SWT.PUSH);
		configure.setText("Configure...");
		var image = ExperimentUiUtils.getImage(ExperimentUiUtils.CONFIGURE_ICON);
		configure.setImage(image);
		configure.addDisposeListener(dispose -> image.dispose());
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.TOP).applyTo(configure);

		label(composite, "Driver");
		var driverCombo = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		driverCombo.setContentProvider(ArrayContentProvider.getInstance());
		STRETCH.applyTo(driverCombo.getControl());

		label(composite, "Profile");
		var profileCombo = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		profileCombo.setContentProvider(ArrayContentProvider.getInstance());
		STRETCH.applyTo(profileCombo.getControl());

		var profilesPerDriver = getDriversToProfiles();

		driverCombo.setInput(profilesPerDriver.keySet().stream().map(ExperimentDriver::getName).toList());

		bindExperimentDriverControls(includeButton, configure, driverCombo, profileCombo);

	}

	private void bindExperimentDriverControls(Button includeInPlan, Button configure, ComboViewer driverCombo, ComboViewer profileCombo) {
		// set initial state of checkbox manually (we don't want the driver bean to be null whenever user unchecks!
		includeInPlan.setSelection(bean.isDriverUsed());

		// Populate profiles based on driver selection

		// tracks driver selection
		IViewerObservableValue<String> driverSelectionObservable = ViewerProperties.singleSelection(String.class).observe(driverCombo);

		Consumer<String> profilesUpdater = driver -> {
			var profiles = getDriversToProfiles().getOrDefault(getDrivers().get(driver), Collections.emptySet());
			profileCombo.setInput(profiles);
			if (profiles.isEmpty()) {
				// prevent inconsistent driver/profile selection in bean
				cachedDriverBean.setProfile(null);
			} else {
				profileCombo.setSelection(new StructuredSelection(profiles.iterator().next()));
			}
		};

		var updateProfilesWhenDriverChanged = ISideEffect.create(driverSelectionObservable::getValue, profilesUpdater);

		configure.addListener(SWT.Selection, selection -> openDriverConfigurationDialog(driverCombo, profileCombo, updateProfilesWhenDriverChanged));

		// tracks profile selection
		IViewerObservableValue<String> selectedProfile = ViewerProperties.singleSelection(String.class).observe(profileCombo);

		// Bind driver/profile to driver bean
		dbc.bindValue(driverSelectionObservable, PojoProperties.value(DriverBean.DRIVER_PROPERTY, String.class).observe(cachedDriverBean));
		dbc.bindValue(selectedProfile, PojoProperties.value(DriverBean.PROFILE_PROPERTY, String.class).observe(cachedDriverBean));

		// cache/restore driver bean when check button toggled
		includeInPlan.addListener(SWT.Selection, selection -> {
			if (includeInPlan.getSelection()) {
				bean.setDriverBean(cachedDriverBean);
				refreshDriverSignals(cachedDriverBean.getDriver());
			} else {
				bean.setDriverBean(null);
			}
		});

		// set driver readouts to segment/trigger controls
		ISideEffect.create(driverSelectionObservable::getValue, driverName -> {
			if (parametersControls != null) {
				refreshDriverSignals(driverName);
			}
		});
	}

	/**
	 * Opens {@link ExperimentDriverDialog} with initial selection matching ours.
	 * If user OK's, our selection will be changed according to what they selected in the driver dialog.
	 */
	private void openDriverConfigurationDialog(ComboViewer driverCombo, ComboViewer profileCombo, ISideEffect driverSelectionSideEffect) {
		String experimentId = null;

		var driverSelection = driverCombo.getSelection();
		var profileSelection = profileCombo.getSelection();

		String selectedDriver = driverSelection.isEmpty() ? null : driverCombo.getStructuredSelection().getFirstElement().toString();
		String selectedProfile = profileSelection.isEmpty() ? null : profileCombo.getStructuredSelection().getFirstElement().toString();


		var driverDialog = new ExperimentDriverDialog(driverCombo.getControl().getShell(), experimentId, selectedDriver, selectedProfile);

		if (driverDialog.open() == Window.OK) {
			// We will update our selections sensibly

			ISelection emptySelection = () -> true; // the answer to 'is the selection empty?'

			if (!driverSelection.isEmpty()) {

				// profiles may have modified, or been added or deleted, so force a refresh:
				driverCombo.setSelection(emptySelection);
				driverCombo.setSelection(driverSelection);

				// let profile update run first
				driverSelectionSideEffect.runIfDirty();

				if (!profileSelection.isEmpty()) {
					// profile was selected before, reselect it
					// (results in empty selection if profile no longer exists)
					profileCombo.setSelection(profileSelection);

				} else if (driverDialog.getProfileSelection() != null) {
					// no profile was selected, but there was a selection in the driver dialog
					// so we make that selection here
					profileCombo.setSelection(new StructuredSelection(driverDialog.getProfileSelection()));
				}

			} else if (driverDialog.getDriverSelection() != null) {
				// no driver selected before hand, one selected in the driver dialog
				// so we select it here
				driverCombo.setSelection(new StructuredSelection(driverDialog.getDriverSelection()));
				driverSelectionSideEffect.runIfDirty();

				// copy over profile selection (if any)
				var profile = driverDialog.getProfileSelection();
				var selection = profile == null ? emptySelection : new StructuredSelection(profile);
				profileCombo.setSelection(selection);
			}
		}
	}

	private void refreshDriverSignals(String driverName) {
		ExperimentDriver driver = getDrivers().get(driverName);
		if (driver != null) {
			parametersControls.setPrioritySignals(driver.getDriverSignals().stream().map(DriverSignal::signalName).collect(Collectors.toSet()));
		}
	}


	private Map<ExperimentDriver, Set<String>> getDriversToProfiles() {
		return getDrivers().entrySet().stream()
			.collect(Collectors.toMap(Map.Entry::getValue,
					driver -> Finder.findSingleton(ExperimentService.class).getDriverProfileNames(driver.getKey(), null)));
	}

	private Map<String, ExperimentDriver> getDrivers() {
		return Finder.getFindablesOfType(ExperimentDriver.class);
	}

	private void experimentParameters(Composite parent) {
		var section = section(parent, "Parameters", true);

		var composite = composite(section, 3);
		greedy.applyTo(composite);

		var treeAndContextControls = composite(composite, 2);
		greedy.copy().span(2, 1).applyTo(treeAndContextControls);

		parametersControls.create(treeAndContextControls);

		var previewComposite = composite(composite, 1);
		greedy.applyTo(previewComposite);
		var preview = new PlanPreviewer(bean, new PlotControllerImpl(previewComposite));
		preview.update();

		PropertyChangeListener previewUpdater = event -> preview.update();
		bean.addPropertyChangeListener(previewUpdater);
		previewComposite.addDisposeListener(dispose -> bean.removePropertyChangeListener(previewUpdater));

	}

}
