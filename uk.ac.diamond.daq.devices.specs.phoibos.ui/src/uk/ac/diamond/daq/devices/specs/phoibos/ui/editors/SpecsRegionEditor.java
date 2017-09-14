
package uk.ac.diamond.daq.devices.specs.phoibos.ui.editors;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.SelectObservableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.factory.Finder;
import uk.ac.diamond.daq.devices.specs.phoibos.api.ISpecsPhoibosAnalyser;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosRegion;
import uk.ac.diamond.daq.devices.specs.phoibos.ui.SpecsUiConstants;

public class SpecsRegionEditor {
	private static final Logger logger = LoggerFactory.getLogger(SpecsRegionEditor.class);

	private static final UpdateValueStrategy POLICY_UPDATE = new UpdateValueStrategy(false,
			UpdateValueStrategy.POLICY_UPDATE);
	private static final UpdateValueStrategy POLICY_NEVER = new UpdateValueStrategy(false,
			UpdateValueStrategy.POLICY_NEVER);

	private Composite parent;

	private ISpecsPhoibosAnalyser analyser;

	// Initialise dbc to prevent NPE
	private DataBindingContext dbc = new DataBindingContext();

	// UI controls
	private ComboViewer lensModeCombo;
	private Text nameText;
	private ComboViewer acquisitionModeCombo;
	private ComboViewer psuModeCombo;
	private Text passEnergyText;
	private Button keButton;
	private Button beButton;
	private Text startEnergyText;
	private Text endEnergyText;
	private Text stepEnergyText;
	private Spinner iterationsSpinner;
	private Text exposureTimeText;
	private Text centreEnergyText;
	private Text widthEnergyText;
	private Text estimatedTimeText;

	private Spinner slicesSpinner;

	@Inject
	public SpecsRegionEditor() {
		logger.trace("Constructor called");

		// Get an analyser
		List<ISpecsPhoibosAnalyser> analysers = Finder.getInstance()
				.listLocalFindablesOfType(ISpecsPhoibosAnalyser.class);
		if (analysers.size() != 1) {
			String msg = "No Analyser was found! (Or more than 1)";
			logger.error(msg);
			throw new RuntimeException(msg);
		}
		analyser = analysers.get(0);
		logger.debug("Connected to analyser {}", analyser);
	}

	@PostConstruct
	public void postConstruct(Composite parent) {
		logger.trace("postConstruct called");
		this.parent = parent;

		GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(parent);
		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		Label nameLabel = new Label(parent, SWT.NONE);
		nameLabel.setText("Region name");
		nameText = new Text(parent, SWT.BORDER);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(nameText);

		Label acquisitionModeLabel = new Label(parent, SWT.NONE);
		acquisitionModeLabel.setText("Acquisition mode");
		acquisitionModeCombo = new ComboViewer(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		// Remove the modes which we don't support yet
		List<String> acquisitionsModes = new ArrayList<>(analyser.getAcquisitionModes());
		acquisitionsModes.remove("Fixed Retarding Ratio");
		acquisitionsModes.remove("Fixed Energy");
		acquisitionModeCombo.add(acquisitionsModes.toArray(new String[] {}));
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.FILL)
				.applyTo(acquisitionModeCombo.getControl());

		Label lensModeLabel = new Label(parent, SWT.NONE);
		lensModeLabel.setText("Lens mode");
		lensModeCombo = new ComboViewer(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		lensModeCombo.add(analyser.getLensModes().toArray(new String[] {}));
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(lensModeCombo.getControl());

		Label psuModeLabel = new Label(parent, SWT.NONE);
		psuModeLabel.setText("PSU mode");
		psuModeCombo = new ComboViewer(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		psuModeCombo.add(analyser.getPsuModes().toArray(new String[] {}));
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(psuModeCombo.getControl());

		Group energyRange = new Group(parent, SWT.SHADOW_NONE);
		energyRange.setLayout(GridLayoutFactory.swtDefaults().numColumns(4).create());
		energyRange.setText("Energy Range (eV)");
		energyRange.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		GridDataFactory.swtDefaults().span(2, 1).grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(energyRange);

		Group energyModeGroup = new Group(energyRange, SWT.SHADOW_NONE);
		energyModeGroup.setLayout(GridLayoutFactory.swtDefaults().numColumns(4).create());
		energyModeGroup.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		energyModeGroup.setText("Energy mode");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).span(4, 1).applyTo(energyModeGroup);
		keButton = new Button(energyModeGroup, SWT.RADIO);
		keButton.setText("Kinetic");
		keButton.setSelection(true);
		beButton = new Button(energyModeGroup, SWT.RADIO);
		beButton.setText("Binding");

		Label startEnergyLabel = new Label(energyRange, SWT.NONE);
		startEnergyLabel.setText("Start");
		startEnergyText = new Text(energyRange, SWT.BORDER);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(startEnergyText);

		Label centreEnergyLabel = new Label(energyRange, SWT.NONE);
		centreEnergyLabel.setText("Centre");
		centreEnergyText = new Text(energyRange, SWT.BORDER);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(centreEnergyText);

		Label endEnergyLabel = new Label(energyRange, SWT.NONE);
		endEnergyLabel.setText("End");
		endEnergyText = new Text(energyRange, SWT.BORDER);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(endEnergyText);

		Label widthEnergyLabel = new Label(energyRange, SWT.NONE);
		widthEnergyLabel.setText("Width");
		widthEnergyText = new Text(energyRange, SWT.BORDER);
		widthEnergyText.setEnabled(false);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(widthEnergyText);

		Label passEnergyLabel = new Label(energyRange, SWT.NONE);
		passEnergyLabel.setText("Pass energy");
		passEnergyText = new Text(energyRange, SWT.BORDER);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(passEnergyText);

		Label stepEnergyLabel = new Label(energyRange, SWT.NONE);
		stepEnergyLabel.setText("Step");
		stepEnergyText = new Text(energyRange, SWT.BORDER);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(stepEnergyText);

		energyModeGroup.layout(true);

		Label exposureTimeLabel = new Label(parent, SWT.NONE);
		exposureTimeLabel.setText("Exposure time (sec)");
		exposureTimeText = new Text(parent, SWT.BORDER);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(exposureTimeText);

		Label iterationsLabel = new Label(parent, SWT.NONE);
		iterationsLabel.setText("Iterations");
		iterationsSpinner = new Spinner(parent, SWT.BORDER);
		iterationsSpinner.setMinimum(1);
		iterationsSpinner.setMaximum(1000); // This is arbitrary but not expecting more that 1000 needed.
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(iterationsSpinner);

		Label slicesLabel = new Label(parent, SWT.NONE);
		slicesLabel.setText("Slices");
		slicesSpinner = new Spinner(parent, SWT.BORDER);
		slicesSpinner.setMinimum(1);
		slicesSpinner.setMaximum(1000); // Should be the detector width in Y, for now hard code to 1000.
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(slicesSpinner);

		Label estimatedTimeLabel = new Label(parent, SWT.NONE);
		estimatedTimeLabel.setText("Estimated time");
		estimatedTimeText = new Text(parent, SWT.NONE);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(estimatedTimeText);

		logger.trace("Finished building composite");
	}

	@Focus
	public void onFocus() {
		parent.setFocus();
	}

	@Optional
	@Inject
	private void selectedRegionChanged(
			@UIEventTopic(SpecsUiConstants.REGION_SELECTED_EVENT) SpecsPhoibosRegion region) {
		logger.trace("selectedRegionChanged called with {}", region);

		if (region == null) { // Can happen when a sequence is loaded.
			parent.setVisible(false); // Hide the UI as its not bound
			return;
		}

		// Get the wrapper for editing support
		SpecsPhoibosRegionEditingWrapper regionEditingWrapper = new SpecsPhoibosRegionEditingWrapper(region,
				analyser.getDetectorEnergyWidth());

		// Setup the data binding

		// Remove existing bindings as the region has changed
		dbc.dispose();
		dbc = new DataBindingContext();

		// Region name
		IObservableValue regionNameTarget = WidgetProperties.text(SWT.Modify).observe(nameText);
		IObservableValue regionNameModel = BeanProperties.value("name").observe(regionEditingWrapper);
		dbc.bindValue(regionNameTarget, regionNameModel);

		IViewerObservableValue acquisitionModeTarget = ViewerProperties.singleSelection().observe(acquisitionModeCombo);
		IObservableValue acquisitionModeModel = BeanProperties.value("acquisitionMode").observe(regionEditingWrapper);
		dbc.bindValue(acquisitionModeTarget, acquisitionModeModel);

		// Lens mode
		IViewerObservableValue lensModeTarget = ViewerProperties.singleSelection().observe(lensModeCombo);
		IObservableValue lensModeModel = BeanProperties.value("lensMode").observe(regionEditingWrapper);
		dbc.bindValue(lensModeTarget, lensModeModel);

		// PSU mode
		IViewerObservableValue psuModeTarget = ViewerProperties.singleSelection().observe(psuModeCombo);
		IObservableValue psuModeModel = BeanProperties.value("psuMode").observe(regionEditingWrapper);
		dbc.bindValue(psuModeTarget, psuModeModel);

		// Pass Energy
		IObservableValue passEnergyTarget = WidgetProperties.text(SWT.Modify).observe(passEnergyText);
		IObservableValue passEnergyModel = BeanProperties.value("passEnergy").observe(regionEditingWrapper);
		UpdateValueStrategy passEnergyStrategy = new UpdateValueStrategy();
		passEnergyStrategy.setBeforeSetValidator(value -> {
			try {
				double temp = Double.parseDouble(String.valueOf(value));
				if (temp <= 0) {
					return ValidationStatus.error("Pass energy must be positive");
				}
				return ValidationStatus.ok();
			} catch (NumberFormatException e) {
				return ValidationStatus.error("Pass energy must be a number");
			}
		});
		Binding passEnergyBinding = dbc.bindValue(passEnergyTarget, passEnergyModel, passEnergyStrategy,
				new UpdateValueStrategy());
		ControlDecorationSupport.create(passEnergyBinding, SWT.TOP | SWT.LEFT);

		// Start Energy
		IObservableValue startEnergyTarget = WidgetProperties.text(SWT.Modify).observe(startEnergyText);
		IObservableValue startEnergyModel = BeanProperties.value("startEnergy").observe(regionEditingWrapper);
		dbc.bindValue(startEnergyTarget, startEnergyModel);

		// End Energy
		IObservableValue endEnergyTarget = WidgetProperties.text(SWT.Modify).observe(endEnergyText);
		IObservableValue endEnergyModel = BeanProperties.value("endEnergy").observe(regionEditingWrapper);
		dbc.bindValue(endEnergyTarget, endEnergyModel);

		// Step Energy
		IObservableValue stepEnergyTarget = WidgetProperties.text(SWT.Modify).observe(stepEnergyText);
		IObservableValue stepEnergyModel = BeanProperties.value("stepEnergy").observe(regionEditingWrapper);
		dbc.bindValue(stepEnergyTarget, stepEnergyModel);

		// Iterations
		IObservableValue iterationsTarget = WidgetProperties.selection().observe(iterationsSpinner);
		IObservableValue iterationsModel = BeanProperties.value("iterations").observe(regionEditingWrapper);
		dbc.bindValue(iterationsTarget, iterationsModel);

		// Slices
		IObservableValue slicesTarget = WidgetProperties.selection().observe(slicesSpinner);
		IObservableValue slicesModel = BeanProperties.value("slices").observe(regionEditingWrapper);
		dbc.bindValue(slicesTarget, slicesModel);

		// Exposure Time
		IObservableValue exposureTimeTarget = WidgetProperties.text(SWT.Modify).observe(exposureTimeText);
		IObservableValue exposureTimeModel = BeanProperties.value("exposureTime").observe(regionEditingWrapper);
		dbc.bindValue(exposureTimeTarget, exposureTimeModel);

		// Centre Energy
		IObservableValue centreEnergyTarget = WidgetProperties.text(SWT.Modify).observe(centreEnergyText);
		IObservableValue centreEnergyComputed = BeanProperties.value("centreEnergy").observe(regionEditingWrapper);
		dbc.bindValue(centreEnergyTarget, centreEnergyComputed);

		// Energy Width
		IObservableValue widthEnergyTarget = WidgetProperties.text(SWT.Modify).observe(widthEnergyText);
		IObservableValue widthEnergyModel = BeanProperties.value("energyWidth").observe(regionEditingWrapper);
		dbc.bindValue(widthEnergyTarget, widthEnergyModel);

		// Energy Mode
		SelectObservableValue energyModeTarget = new SelectObservableValue();
		energyModeTarget.addOption(true, WidgetProperties.selection().observe(beButton));
		energyModeTarget.addOption(false, WidgetProperties.selection().observe(keButton));
		IObservableValue energyModeModel = BeanProperties.value("bindingEnergy").observe(regionEditingWrapper);
		dbc.bindValue(energyModeTarget, energyModeModel);

		// Enable fields depending on the acquisition mode
		// Also disable start and end they could be set but require care if we allow it.
		// If the mode is snapshot then you can't set the step energy so disable it
		IObservableValue isSnapshotMode = BeanProperties.value("snapshotMode").observe(regionEditingWrapper);
		IObservableValue isNotSnapshotMode = BeanProperties.value("notSnapshotMode").observe(regionEditingWrapper);

		IObservableValue centreEnergyEnabled = WidgetProperties.enabled().observe(centreEnergyText);
		IObservableValue stepEnergyEnabled = WidgetProperties.enabled().observe(stepEnergyText);
		IObservableValue startEnergyEnabled = WidgetProperties.enabled().observe(startEnergyText);
		IObservableValue endEnergyEnabled = WidgetProperties.enabled().observe(endEnergyText);

		dbc.bindValue(centreEnergyEnabled, isSnapshotMode, POLICY_NEVER, POLICY_UPDATE);
		dbc.bindValue(stepEnergyEnabled, isNotSnapshotMode, POLICY_NEVER, POLICY_UPDATE);
		dbc.bindValue(startEnergyEnabled, isNotSnapshotMode, POLICY_NEVER, POLICY_UPDATE);
		dbc.bindValue(endEnergyEnabled, isNotSnapshotMode, POLICY_NEVER, POLICY_UPDATE);

		// Estimated time
		IObservableValue estimatedTimeTarget = WidgetProperties.text(SWT.Modify).observe(estimatedTimeText);
		IObservableValue estimatedTimeModel = BeanProperties.value("estimatedTime").observe(regionEditingWrapper);
		dbc.bindValue(estimatedTimeTarget, estimatedTimeModel, POLICY_NEVER, POLICY_UPDATE);

		// Update the widgets from the model
		dbc.updateTargets();

		// Bindings are setup make the controls visible
		parent.setVisible(true);

		logger.trace("Finished bindings");
	}

}