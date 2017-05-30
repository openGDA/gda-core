/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos.ui.control;

import javax.annotation.PostConstruct;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.daq.devices.specs.phoibos.ui.SpecsFixedRegionWrapper;

/**
 * A E4 style POJO view object for allowing live control over a SPECS Phoibos electron analyser.
 *
 * @author James Mudd
 *
 */
public class SpecsLiveAnalyserControlsView {

	@PostConstruct
	void createView(Composite parent) {

		// Get the controller and the model
		SpecsLiveAnalyserControlController controller = new SpecsLiveAnalyserControlController();
		SpecsFixedRegionWrapper model = controller.getModel();

		// Setup the data binding context
		DataBindingContext dbc = new DataBindingContext();

		GridLayoutFactory.swtDefaults().numColumns(3).spacing(10, 0).applyTo(parent);

		Composite psuAndLensModeComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).spacing(10, 10).applyTo(psuAndLensModeComposite);

		Label psuModeLabel = new Label(psuAndLensModeComposite, SWT.NONE);
		psuModeLabel.setText("PSU Mode");
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).grab(false, true).applyTo(psuModeLabel);

		// PSU Mode
		Combo psuModeCombo = new Combo(psuAndLensModeComposite, SWT.NONE);
		GridDataFactory.swtDefaults().grab(true, false).applyTo(psuModeCombo);
		// Setup lens modes and select currently selected one
		psuModeCombo.setItems(controller.getPsuModes());
		// Setup the data binding
		IObservableValue psuModeTarget = WidgetProperties.text().observe(psuModeCombo);
		IObservableValue psuModeModel = BeanProperties.value(SpecsFixedRegionWrapper.class, "psuMode").observe(model);
		dbc.bindValue(psuModeTarget, psuModeModel, new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST),
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST));

		// Lens Mode
		Label lensModeLabel = new Label(psuAndLensModeComposite, SWT.NONE);
		lensModeLabel.setText("Lens Mode");
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).grab(false, true).applyTo(lensModeLabel);

		Combo lensModeCombo = new Combo(psuAndLensModeComposite, SWT.NONE);
		GridDataFactory.swtDefaults().grab(true, false).applyTo(lensModeCombo);
		// Setup lens modes and select currently selected one
		lensModeCombo.setItems(controller.getLensModes());
		// Setup the data binding
		IObservableValue lensModeTarget = WidgetProperties.text().observe(lensModeCombo);
		IObservableValue lensModeModel = BeanProperties.value(SpecsFixedRegionWrapper.class, "lensMode").observe(model);
		dbc.bindValue(lensModeTarget, lensModeModel, new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST),
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST));

		Composite centreAndPassEnergyComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).spacing(10, 5).applyTo(centreAndPassEnergyComposite);

		// Centre Energy
		Label centreEnergyLabel = new Label(centreAndPassEnergyComposite, SWT.NONE);
		centreEnergyLabel.setText("Centre Energy (eV)");
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).grab(false, true).applyTo(centreEnergyLabel);

		Text centreEnergyText = new Text(centreAndPassEnergyComposite, SWT.BORDER);
		GridDataFactory.swtDefaults().grab(true, false).hint(50, SWT.DEFAULT).applyTo(centreEnergyText);
		// Setup the data binding
		IObservableValue centreEnergyTarget = WidgetProperties.text().observe(centreEnergyText);
		IObservableValue centreEnergyModel = BeanProperties.value(SpecsFixedRegionWrapper.class, "centreEnergy")
				.observe(model);
		dbc.bindValue(centreEnergyTarget, centreEnergyModel,
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST),
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST));

		// Pass Energy
		Label passEnergyLabel = new Label(centreAndPassEnergyComposite, SWT.NONE);
		passEnergyLabel.setText("Pass Energy (eV)");
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).grab(false, true).applyTo(passEnergyLabel);

		Text passEnergyText = new Text(centreAndPassEnergyComposite, SWT.BORDER);
		GridDataFactory.swtDefaults().grab(true, false).hint(50, SWT.DEFAULT).applyTo(passEnergyText);
		// Setup the data binding
		IObservableValue passEnergyTarget = WidgetProperties.text().observe(passEnergyText);
		IObservableValue passEnergyModel = BeanProperties.value(SpecsFixedRegionWrapper.class, "passEnergy")
				.observe(model);
		dbc.bindValue(passEnergyTarget, passEnergyModel, new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST),
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST));

		// Exposure time
		Label exposureTimeLabel = new Label(centreAndPassEnergyComposite, SWT.NONE);
		exposureTimeLabel.setText("Exposure Time (sec)");
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).grab(false, true).applyTo(exposureTimeLabel);

		Text exposureTimeText = new Text(centreAndPassEnergyComposite, SWT.BORDER);
		GridDataFactory.swtDefaults().grab(true, false).hint(50, SWT.DEFAULT).applyTo(exposureTimeText);
		// Setup the data binding
		IObservableValue exposureTimeTarget = WidgetProperties.text().observe(exposureTimeText);
		IObservableValue exposureTimeModel = BeanProperties.value(SpecsFixedRegionWrapper.class, "exposureTime")
				.observe(model);
		dbc.bindValue(exposureTimeTarget, exposureTimeModel,
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST),
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST));

		Composite startAndStopComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(1).spacing(10, 10).applyTo(startAndStopComposite);

		// Start button
		Button startButton = new Button(startAndStopComposite, SWT.DEFAULT);
		startButton.setLayoutData(new GridData(100, SWT.DEFAULT));
		startButton.setText("Start");
		startButton.setToolTipText("Apply voltages and start acquiring");
		startButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Update the model with the current settings
				dbc.updateModels();
				// Start Acquiring
				controller.start();
				// Disable the GUI
				disable();
			}
		});

		// Stop button
		Button stopButton = new Button(startAndStopComposite, SWT.DEFAULT);
		stopButton.setLayoutData(new GridData(100, SWT.DEFAULT));
		stopButton.setText("Stop");
		stopButton.setToolTipText("Stop acquiring");
		stopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				controller.stop();
				enable();
			}
		});

		// Set GUI to have the current model values
		dbc.updateTargets();

		// TODO Add support for validation to only allow valid settings

		// Exposure Box
		// Label exposureLabel = new Label(composite, SWT.NONE);
		// exposureLabel.setText("Exposure Time (sec)");
		// GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).grab(false, true).applyTo(exposureLabel);
		//
		// Text exposureText = new Text(composite, SWT.BORDER);
		// exposureText.setText(Double.toString(analyser.getDwellTime()));
		// GridDataFactory.swtDefaults().grab(true, false).applyTo(exposureText);
		// exposureText.addSelectionListener(new SelectionAdapter() {
		// @Override
		// public void widgetDefaultSelected(SelectionEvent e) {
		// double newExposure = Double.parseDouble(exposureText.getText());
		// logger.info("Changing exposure to: {}", newExposure);
		// analyser.setDwellTime(newExposure);
		// }
		// });
		//
		// ISWTObservableValue textObservable = WidgetProperties.text(SWT.Modify).observe(exposureText);
		//
		// UpdateValueStrategy strategy = new UpdateValueStrategy();
		// strategy.setAfterGetValidator(text -> {
		// double value = Double.parseDouble((String) text);
		// if (value < 0) {
		// return ValidationStatus.error("Exposure must be greater than 0");
		// }
		// if (value > 30) {
		// return ValidationStatus.error("The max exposure is 30 sec");
		// }
		// return ValidationStatus.ok();
		// });
		//
		// /// bind value
		// Binding bindValue = new DataBindingContext().bindValue(textObservable, new ExposureObservable(analyser),
		// strategy,
		// new UpdateValueStrategy());
		//
		// // Add Jface decorator
		// ControlDecorationSupport.create(bindValue, SWT.LEFT);

	}

	private void enable() {
		// TODO Auto-generated method stub

	}

	private void disable() {
		// TODO Auto-generated method stub

	}

}
