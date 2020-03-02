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

package uk.ac.diamond.daq.devices.specs.phoibos.ui.control;

import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.factory.Finder;
import gda.observable.IObserver;
import uk.ac.diamond.daq.devices.specs.phoibos.api.ISpecsPhoibosAnalyser;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosRegion;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSpectrumUpdate;

public class SpecsAlignmentView implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(SpecsAlignmentView.class);

	private ISpecsPhoibosAnalyser analyser;

	private Text passEnergyText;
	private Text kineticEnergyText;
	private Text valuesText;
	private Text exposureText;
	private Text countsText;
	private Button setButton;
	private Button startButton;
	private Button stopButton;

	@PostConstruct
	void createView(Composite parent) {

		List<ISpecsPhoibosAnalyser> analysers = Finder.getInstance()
				.listLocalFindablesOfType(ISpecsPhoibosAnalyser.class);
		if (analysers.size() != 1) {
			throw new RuntimeException("No Analyser was found! (Or more than 1)");
		}
		analyser = analysers.get(0);
		analyser.addIObserver(this);

		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		Composite controlsArea = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).spacing(10, 10).applyTo(controlsArea);

		Label kineticEnergy = new Label(controlsArea, SWT.NONE);
		kineticEnergy.setText("Ekin");
		kineticEnergyText = new Text(controlsArea, SWT.BORDER);
		GridDataFactory.swtDefaults().grab(true, false).hint(50, SWT.DEFAULT).applyTo(kineticEnergyText);

		Label passEnergy = new Label(controlsArea, SWT.NONE);
		passEnergy.setText("Epass");
		passEnergyText = new Text(controlsArea, SWT.BORDER);
		GridDataFactory.swtDefaults().grab(true, false).hint(50, SWT.DEFAULT).applyTo(passEnergyText);

		Label valuesLabel = new Label(controlsArea, SWT.NONE);
		valuesLabel.setText("Values");
		valuesText = new Text(controlsArea, SWT.BORDER);
		GridDataFactory.swtDefaults().grab(true, false).hint(50, SWT.DEFAULT).applyTo(valuesText);

		Label exposureLabel = new Label(controlsArea, SWT.NONE);
		exposureLabel.setText("Dwell");
		exposureText = new Text(controlsArea, SWT.BORDER);
		GridDataFactory.swtDefaults().grab(true, false).hint(50, SWT.DEFAULT).applyTo(exposureText);

		// Start button
		startButton = new Button(controlsArea, SWT.DEFAULT);
		startButton.setLayoutData(new GridData(100, SWT.DEFAULT));
		startButton.setText("Start");
		startButton.setToolTipText("Start alignment process");
		startButton.setEnabled(false);
		startButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
					// Start Acquiring
					analyser.startContinuous();
				}
			});

		// Set button
		setButton = new Button(controlsArea, SWT.DEFAULT);
		setButton.setLayoutData(new GridData(100, SWT.DEFAULT));
		setButton.setText("Set");
		setButton.setToolTipText("Set Analyser");
		setButton.setEnabled(false);
		setButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Use values set by user
				SpecsPhoibosRegion customRegion = new SpecsPhoibosRegion();
				customRegion.setAcquisitionMode("Fixed Energy");
				customRegion.setLensMode("LargeArea");
				customRegion.setCentreEnergy(Double.valueOf(kineticEnergyText.getText()));
				customRegion.setPassEnergy(Double.valueOf(passEnergyText.getText()));
				customRegion.setExposureTime(Double.valueOf(exposureText.getText()));
				customRegion.setValues(Integer.parseInt(valuesText.getText()));
				analyser.setRegion(customRegion);
			}
		});

		// Stop button
		stopButton = new Button(controlsArea, SWT.DEFAULT);
		stopButton.setLayoutData(new GridData(100, SWT.DEFAULT));
		stopButton.setText("Stop");
		stopButton.setToolTipText("End alignment process");
		stopButton.setEnabled(false);
		stopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				analyser.stopAcquiring();
				}
			});

		Composite displayArea = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(1).spacing(10, 10).applyTo(displayArea);

		// Display counts from analyser
		Label counts = new Label(displayArea, SWT.NONE);
		FontData fdlabel = counts.getFont().getFontData()[0];
		fdlabel.setHeight(12);
		counts.setFont(new Font(counts.getDisplay(), fdlabel));
		counts.setText("Intensity (counts)");
		countsText = new Text(displayArea, SWT.BORDER);
		countsText.setEditable(false);
		countsText.setEnabled(false);
		FontData fdtext = countsText.getFont().getFontData()[0];
		fdtext.setHeight(20);
		countsText.setFont(new Font(countsText.getDisplay(), fdtext));
		GridDataFactory.swtDefaults().grab(true, false).hint(220, SWT.DEFAULT).applyTo(countsText);

		// Check all fields have been completed
		for(Object control : controlsArea.getChildren()) {
			if(control instanceof Text) {
				Text textControl = (Text) control;
				textControl.addModifyListener(this::checkRequiredFieldsArePresent);
			}
		}

		// Check fields contain numbers only
		for(Object control : controlsArea.getChildren()) {
			if(control instanceof Text) {
				Text textControl = (Text) control;
				textControl.addVerifyListener(this::checkInputIsNumerical);
			}
		}
	}

	/**
	 * Creates a formatted numeric string if input double is larger or equal to 1000.
	 * For input which is smaller than 1000 the number is simply converted to String
	 *
	 * @param reading
	 * @return A formatted string
	 */
	private String formatReading(double reading) {
		if(reading >= 1000) {
			return String.valueOf(reading/1000 + "\u22C5"+"10"+"\u00B3");
		}
		return String.valueOf(reading);
	}

	@Override
	public void update(Object source, Object arg) {
		if (arg instanceof SpecsPhoibosSpectrumUpdate) {
			SpecsPhoibosSpectrumUpdate event = (SpecsPhoibosSpectrumUpdate) arg;
			Display.getDefault().asyncExec(() -> {
				double[] data = event.getSpectrum();
				int lastIndex = event.getDataLength()-1;
				countsText.setText(formatReading(data[lastIndex]));
			});
		}
	}

	private void checkRequiredFieldsArePresent(ModifyEvent e) {
		if (kineticEnergyText.getText().isEmpty() || passEnergyText.getText().isEmpty() ||
				valuesText.getText().isEmpty() || exposureText.getText().isEmpty()){
			setButton.setEnabled(false);
			startButton.setEnabled(false);
			stopButton.setEnabled(false);
		}else {
			setButton.setEnabled(true);
			startButton.setEnabled(true);
			stopButton.setEnabled(true);
		}
	}

	private void checkInputIsNumerical(VerifyEvent e) {
		if (e.character == SWT.BS || e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT
				|| e.keyCode == SWT.DEL) {
			e.doit = true;
			return;
		}
		if (!('0' <= e.character && e.character <= '9')) {
			e.doit = false;
		}
		if(((Text)e.widget).getText().length() >= 5) {
			e.doit = false;
		}
	}

}
