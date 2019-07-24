package uk.ac.diamond.daq.experiment.ui.plan;

import static uk.ac.diamond.daq.experiment.api.Services.getExperimentService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffect;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.SelectObservableValue;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import gda.factory.Finder;
import uk.ac.diamond.daq.experiment.api.driver.DriverModel;
import uk.ac.diamond.daq.experiment.api.driver.IExperimentDriver;
import uk.ac.diamond.daq.experiment.api.plan.DriverBean;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;

public class DriverAndProfileSelectionSection extends ValidatablePart {
	
	private Map<IExperimentDriver<? extends DriverModel>, Set<String>> driversToProfiles;
	private final ExperimentPlanBean planBean;
	private final String experimentId;
	
	private Button useDriver;
	private List<Button> driverButtons;
	private ComboViewer profileCombo;
	
	private final DriverBean localDriverBean;
	private boolean consistentUiSelection = true;
	
	private DriverProfilePreview profilePlot;
	
	public DriverAndProfileSelectionSection(ExperimentPlanBean planBean, String experimentId) {
		this.planBean = planBean;
		localDriverBean = copy(planBean.getDriverBean());
		this.experimentId = experimentId;
	}
	
	@Override
	public void createPart(Composite parent) {
		
		Group driverGroup = new Group(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(driverGroup);
		GridLayoutFactory.swtDefaults().margins(20, 20).numColumns(3).equalWidth(true).applyTo(driverGroup);
		driverGroup.setText("Experiment driver");
		
		Composite controls = new Composite(driverGroup, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(controls);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(false, true).applyTo(controls);
		
		useDriver = new Button(controls, SWT.CHECK);
		useDriver.setText("Use experiment driver");
		
		new Label(controls, SWT.NONE); // space
		
		driverButtons = new ArrayList<>();
		for (Map.Entry<IExperimentDriver<? extends DriverModel>,Set<String>> experimentDriver : getDriversToProfiles().entrySet()) {
			Button button = new Button(controls, SWT.RADIO);
			String driverName = experimentDriver.getKey().getName();
			button.setText(driverName);
			driverButtons.add(button);
		}
		
		new Label(controls, SWT.NONE); // space
		
		new Label(controls, SWT.NONE).setText("Configuration");
		
		profileCombo = new ComboViewer(controls, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(profileCombo.getControl());
		profileCombo.setContentProvider(ArrayContentProvider.getInstance());
		
		Composite plotComposite = new Composite(driverGroup, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).span(2, 1).applyTo(plotComposite);
		GridLayoutFactory.swtDefaults().applyTo(plotComposite);
		
		profilePlot = new DriverProfilePreview(plotComposite);
		
		bind();
		
		updatePlot();
	}
	
	@SuppressWarnings("unchecked")
	private void bind() {
		
		DataBindingContext dbc = new DataBindingContext();
		
		final IObservableValue<Boolean> driverUsedCheck = WidgetProperties.selection().observe(useDriver);
		final SelectObservableValue<String> driverSelection = new SelectObservableValue<>();
		final IViewerObservableValue selectedProfile = ViewerProperties.singleSelection().observe(profileCombo);
		
		configureEnabledStateToggling(driverUsedCheck, dbc);
		bindDriverSelectionWithNameProperty(driverSelection, dbc);
		bindDriverSelectionWithProfileInput(driverSelection);
		bindProfileSelectionWithProfileProperty(selectedProfile, dbc);
		configureProfileSelectionSideEffects(selectedProfile);
		configureDriverToggleSideEffects(driverUsedCheck);
		configureDriverSelectionSideEffects(driverSelection);
	}

	/**
	 * binds use-driver check box to enabled state of other controls
	 */
	private void configureEnabledStateToggling(IObservableValue<Boolean> driverUsedCheck, DataBindingContext dbc) {
		final List<ISWTObservableValue> driverButtonsEnabled = driverButtons.stream().map(b -> WidgetProperties.enabled().observe(b)).collect(Collectors.toList());
		@SuppressWarnings("unchecked")
		final IObservableValue<Boolean> profileComboEnabled = WidgetProperties.enabled().observe(profileCombo.getCombo());
		
		driverButtonsEnabled.forEach(enabledObservable -> dbc.bindValue(driverUsedCheck, enabledObservable, new UpdateValueStrategy(), new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER)));
		dbc.bindValue(driverUsedCheck, profileComboEnabled, new UpdateValueStrategy(), new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER));
		
		// set the initial state
		useDriver.setSelection(planBean.isDriverUsed());
	}

	/**
	 * Binds the driver selection to the driver property in our local driver bean
	 */
	@SuppressWarnings("unchecked")
	private void bindDriverSelectionWithNameProperty(SelectObservableValue<String> driverSelection,	DataBindingContext dbc) {
		driverButtons.forEach(button -> driverSelection.addOption(button.getText(), WidgetProperties.selection().observe(button)));
		final IObservableValue<String> driverNameInBean = PojoProperties.value(DriverBean.DRIVER_PROPERTY).observe(localDriverBean);
		
		dbc.bindValue(driverSelection, driverNameInBean);
	}
	
	private void bindDriverSelectionWithProfileInput(IObservableValue<String> driverSelection) {
		ISideEffect.create(driverSelection::getValue, driverName -> {
			consistentUiSelection = false;
			Set<String> profilesForNamedDriver = getProfilesForNamedDriver(driverName);
			profileCombo.setInput(profilesForNamedDriver);
			if (profilesForNamedDriver.contains(localDriverBean.getProfile())) {
				profileCombo.setSelection(new StructuredSelection(localDriverBean.getProfile()));
			}
			profileCombo.refresh();
		});
	}
	
	/**
	 * Binds the profile selection to the profile property in our local driver bean
	 */
	private void bindProfileSelectionWithProfileProperty(IViewerObservableValue selectedProfile, DataBindingContext dbc) {
		@SuppressWarnings("unchecked")
		final IObservableValue<String> profileNameInBean = PojoProperties.value(DriverBean.PROFILE_PROPERTY).observe(localDriverBean);
		
		dbc.bindValue(selectedProfile, profileNameInBean);
	}
	
	/**
	 * Updates the model if the selection is consistent and updates the plot.
	 */
	private void configureProfileSelectionSideEffects(IViewerObservableValue selectedProfile) {
		ISideEffect.create(() -> {
			String profile = (String) selectedProfile.getValue();
			consistentUiSelection = profile != null;
			if (consistentUiSelection) {
				updateModel(true);
			}
			updatePlot();
			notifyValidationListener();
		});
	}
	
	/**
	 * We update the model if selection is consistent and the plot
	 */
	private void configureDriverToggleSideEffects(IObservableValue<Boolean> driverUsedCheck) {
		ISideEffect.create(driverUsedCheck::getValue, driverUsed -> {
			updateModel(driverUsed && consistentUiSelection);
			updatePlot();
			notifyValidationListener();
		});
	}

	/**
	 * When the driver selection changes, our local driver bean is reset and the plot is updated
	 */
	private void configureDriverSelectionSideEffects(SelectObservableValue<String> driverSelection) {
		ISideEffect.create(driverSelection::getValue, driver -> {
			if (localDriverBean.getProfile() != null && !getProfilesForNamedDriver(driver).contains(localDriverBean.getProfile())) {
				localDriverBean.setProfile(null);
			}
			updatePlot();
			notifyValidationListener();
		});	
	}

	private Set<String> getProfilesForNamedDriver(String driverName) {
		return getDriversToProfiles().entrySet().stream()
				.filter(entry -> entry.getKey().getName().equals(driverName))
				.map(Map.Entry::getValue)
				.findFirst().orElse(Collections.emptySet());	
	}
	
	private Map<IExperimentDriver<? extends DriverModel>, Set<String>> getDriversToProfiles() {
		if (driversToProfiles == null) {
			driversToProfiles = getDrivers().entrySet().stream()
					.collect(Collectors.toMap(Map.Entry::getValue,
							driver -> getExperimentService().getDriverProfileNames(driver.getKey(), experimentId)));
		}
		return driversToProfiles;
	}

	@SuppressWarnings("rawtypes")
	private Map<String, IExperimentDriver> getDrivers() {
		return Finder.getInstance().getFindablesOfType(IExperimentDriver.class);
	}

	private void updatePlot() {
		boolean driverUsed = useDriver.getSelection();
		String driver = localDriverBean.getDriver();
		String profile = localDriverBean.getProfile();
		if (driverUsed && driver != null && profile != null) {
			profilePlot.plot(getExperimentService().getDriverProfile(driver, profile, experimentId));
		} else {
			profilePlot.clear();
		}
	}
	
	@Override
	public boolean isValidSelection() {
		if (!useDriver.getSelection()) return true;
		return localDriverBean.getDriver() != null && localDriverBean.getProfile() != null;
	}
	
	private void updateModel(boolean copy) {
		if (copy) {
			planBean.setDriverBean(copy(localDriverBean));
		} else {
			planBean.setDriverBean(null);
		}
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
}
