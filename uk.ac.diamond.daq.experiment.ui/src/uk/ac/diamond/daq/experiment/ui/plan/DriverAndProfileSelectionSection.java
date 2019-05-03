package uk.ac.diamond.daq.experiment.ui.plan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffect;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.SelectObservableValue;
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

import uk.ac.diamond.daq.experiment.api.ExperimentService;
import uk.ac.diamond.daq.experiment.api.driver.DriverProfileSection;
import uk.ac.diamond.daq.experiment.api.driver.IExperimentDriver;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;

public class DriverAndProfileSelectionSection {
	
	private final Map<IExperimentDriver, Set<String>> driversToProfiles;
	private final ExperimentPlanBean planBean;
	private final ExperimentService experimentService;
	private final String experimentId;
	
	private Button useDriver;
	private List<Button> driverButtons;
	private ComboViewer profileCombo;
	
	private DriverProfilePreview profilePlot;
	
	public DriverAndProfileSelectionSection(ExperimentPlanBean planBean, Map<IExperimentDriver, Set<String>> driversToProfiles,
											ExperimentService experimentService, String experimentId) {
		this.planBean = planBean;
		this.driversToProfiles = driversToProfiles;
		this.experimentService = experimentService;
		this.experimentId = experimentId;
	}
	
	public void createSection(Composite parent) {
		
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
		for (Map.Entry<IExperimentDriver,Set<String>> experimentDriver : driversToProfiles.entrySet()) {
			Button button = new Button(controls, SWT.RADIO);
			String driverName = experimentDriver.getKey().getName();
			button.setText(driverName);
			driverButtons.add(button);
		}
		
		new Label(controls, SWT.NONE); // space
		
		new Label(controls, SWT.NONE).setText("Configuration");
		
		profileCombo = new ComboViewer(controls, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(profileCombo.getControl());
		
		Composite plotComposite = new Composite(driverGroup, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).span(2, 1).applyTo(plotComposite);
		GridLayoutFactory.swtDefaults().applyTo(plotComposite);
		
		profilePlot = new DriverProfilePreview(plotComposite);
		
		bind();
	}
	
	@SuppressWarnings("unchecked") // Blame JFace
	private void bind() {
		
		DataBindingContext dbc = new DataBindingContext();
		
		// bind use-driver check box to enabled state of other controls
		final IObservableValue<Boolean> driverUsedCheck = WidgetProperties.selection().observe(useDriver);
		final List<IObservableValue<Boolean>> driverButtonsEnabled = driverButtons.stream().map(b -> WidgetProperties.enabled().observe(b)).collect(Collectors.toList());
		final IObservableValue<Boolean> profileComboEnabled = WidgetProperties.enabled().observe(profileCombo.getCombo());
		
		driverButtonsEnabled.forEach(enabledObservable -> dbc.bindValue(driverUsedCheck, enabledObservable));
		dbc.bindValue(driverUsedCheck, profileComboEnabled);
		
		// bind use-driver check box to driver used property in bean
		final IObservableValue<Boolean> driverUsedInBean = BeanProperties.value("driverUsed").observe(planBean);
		
		dbc.bindValue(driverUsedCheck, driverUsedInBean);
		
		// bind driver radio button with driver name property in bean
		final SelectObservableValue<String> driverSelection = new SelectObservableValue<>();
		driverButtons.forEach(button -> driverSelection.addOption(button.getText(), WidgetProperties.selection().observe(button)));
		final IObservableValue<String> driverNameInBean = BeanProperties.value("experimentDriverName").observe(planBean);
		
		dbc.bindValue(driverSelection, driverNameInBean);
		profileCombo.setContentProvider(ArrayContentProvider.getInstance());
		
		// bind driver radio button selection with profile combo input
		ISideEffect.create(driverSelection::getValue, driverName -> {
			Set<String> profilesForNamedDriver = getProfilesForNamedDriver(driverName);
			profileCombo.setInput(profilesForNamedDriver);
			if (profilesForNamedDriver.contains(planBean.getExperimentDriverProfile())) {
				profileCombo.setSelection(new StructuredSelection(planBean.getExperimentDriverProfile()));
			}
			profileCombo.refresh();
		});
		
		// bind profile combo selection to profile name in bean
		final IViewerObservableValue selectedProfile = ViewerProperties.singleSelection().observe(profileCombo);
		final IObservableValue<String> profileNameInBean = BeanProperties.value("experimentDriverProfile").observe(planBean);
		
		dbc.bindValue(selectedProfile, profileNameInBean);
		
		// plot driver profile:
		// 1) on selection of a different profile
		ISideEffect.create(() -> (String) selectedProfile.getValue(), this::plot);
		
		// 2) on use-driver check box toggle
		ISideEffect.create(driverUsedCheck::getValue, driverUsed -> this.plot(planBean.getExperimentDriverProfile()));
		
		// 3) on changing drivers
		ISideEffect.create(driverNameInBean::getValue, driverName -> this.plot(planBean.getExperimentDriverProfile()));
		
	}
	
	private Set<String> getProfilesForNamedDriver(String driverName) {
		return driversToProfiles.entrySet().stream()
				.filter(entry -> entry.getKey().getName().equals(driverName))
				.map(Map.Entry::getValue)
				.findFirst().orElse(Collections.emptySet());	
	}
	
	private void plot(String profileName) {
		List<DriverProfileSection> profile;
		if (!planBean.isDriverUsed() || profileName == null || profileName.isEmpty()) {
			profile = Collections.emptyList();
		} else {
			try {
				profile = experimentService.getDriverProfile(planBean.getExperimentDriverName(),
															profileName, experimentId).getProfile();
			} catch (Exception e) {
				profile = Collections.emptyList();
			}
		}
		profilePlot.plot(profile);
	}
	
	public boolean validSelection() {
		return !planBean.isDriverUsed() || // no driver is OK
				// otherwise driver name and driver profile must exist and map to something useful
				(planBean.getExperimentDriverName() != null &&
				planBean.getExperimentDriverProfile() != null && 
				experimentService.getDriverProfile(planBean.getExperimentDriverName(),
						planBean.getExperimentDriverProfile(), experimentId) != null);
	}
}
