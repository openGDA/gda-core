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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.epics.connection.EpicsController;
import gda.factory.Finder;
import gda.observable.IObserver;
import uk.ac.diamond.daq.devices.specs.phoibos.api.IBeamToEndstationStatus;
import uk.ac.diamond.daq.devices.specs.phoibos.api.ISpecsPhoibosAnalyser;
import uk.ac.diamond.daq.devices.specs.phoibos.api.ISpecsPhoibosAnalyserStatus;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosLiveDataUpdate;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosLiveUpdate;
import uk.ac.diamond.daq.devices.specs.phoibos.ui.ISpecsLiveDataDispatcher;

public class SpecsAlignmentView implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(SpecsAlignmentView.class);

	private ISpecsPhoibosAnalyser analyser;
	private ISpecsPhoibosAnalyserStatus status;
	private ISpecsLiveDataDispatcher dataDispatcher;

	protected final EpicsController epicsController = EpicsController.getInstance();

	private Text passEnergyText;
	private Text kineticEnergyText;
	private Text exposureText;
	private Text countsText;
	private Button startButton;
	private Button stopButton;

	private Button indicator;
	private Runnable blink;
	private ExecutorService executor;
	private boolean keepBlinking;
	private final String STOPPED_LABEL = "STOPPED";
	private final String ACTIVE_LABEL = "ACTIVE";
	private final String PHOTON_ENERGY = "pgm_energy";

	private Scannable photonEnergy;

	private IBeamToEndstationStatus beamToEndstationStatus;
	private final String APPEND_LINE = "\nClick OK to run scan anyway";

	private Integer defaultLensMode = 3;

	/**
	 * Constructor
	 */
	@Inject
	public SpecsAlignmentView(@Named("default_lens_mode") @Active @Optional String defaultLensMode) {
		// optionally inject defaultLensMode(int) via context parameters in fragment.e4xmi when needed
		if (defaultLensMode!=null) {
			try {
				this.defaultLensMode = Integer.parseInt(defaultLensMode);
			} catch (NumberFormatException e) {
				logger.warn("Failed to set default lens mode via injected parameter, leaving default value", e);
			}
		}

		executor = Executors.newSingleThreadExecutor();

		blink = ()-> {
			while(keepBlinking) {
				color(SWT.COLOR_GREEN);
				sleep(3000);
				color(SWT.COLOR_WHITE);
				sleep(3000);
			}
	    	color(SWT.COLOR_RED);
		};

		// Get analyser
		List<ISpecsPhoibosAnalyser> analysers = Finder.listLocalFindablesOfType(ISpecsPhoibosAnalyser.class);
		if (analysers.size() != 1) {
			throw new RuntimeException("No Analyser was found! (Or more than 1)");
		}
		analyser = analysers.get(0);

		// Get dispatcher
		dataDispatcher = Finder.findLocalSingleton(ISpecsLiveDataDispatcher.class);
		dataDispatcher.addIObserver(this);

		// Get status
		List<ISpecsPhoibosAnalyserStatus> analyserStatusList = Finder.listFindablesOfType(ISpecsPhoibosAnalyserStatus.class);
		if (analyserStatusList.size() != 1) {
			String msg = "No analyser status was found! (Or more than 1)";
			throw new IllegalStateException(msg);
		}
		status = analyserStatusList.get(0);
		status.addIObserver(this::updateIndicator);

		photonEnergy =  Finder.find(PHOTON_ENERGY);

		// Check if beam in endstation
		beamToEndstationStatus = Finder.find("beam_to_endstation");
	}

	@PostConstruct
	void createView(Composite parent) {

		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		ScrolledComposite scrollComp = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		Composite contents = new Composite(scrollComp, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(contents);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.BEGINNING, SWT.FILL).applyTo(contents);
		contents.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		Composite controlsArea = new Composite(contents, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(controlsArea);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.BEGINNING, SWT.FILL).applyTo(controlsArea);
		controlsArea.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		kineticEnergyText = addLabeledTextbox(controlsArea, "Ekin", "100");
		kineticEnergyText.addModifyListener(this::checkRequiredFieldsArePresent);
		kineticEnergyText.addVerifyListener(this::checkInputIsNumerical);
		passEnergyText =  addLabeledTextbox(controlsArea, "Epass", "40");
		passEnergyText.addModifyListener(this::checkRequiredFieldsArePresent);
		passEnergyText.addVerifyListener(this::checkInputIsNumerical);
		exposureText = addLabeledTextbox(controlsArea, "Dwell", "1");
		exposureText.addModifyListener(this::checkRequiredFieldsArePresent);
		exposureText.addVerifyListener(this::checkInputIsNumerical);

		Combo lensMode = addLabelledDropdown(controlsArea, "Lens mode");

		Composite buttons = new Composite(controlsArea, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(buttons);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(buttons);
		buttons.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		// Start button
		startButton = new Button(buttons, SWT.DEFAULT);
		startButton.setLayoutData(new GridData(100, SWT.DEFAULT));
		startButton.setText("Start");
		startButton.setToolTipText("Start alignment process");
		startButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				if (beamToEndstationStatus != null && !beamToEndstationStatus.beamInEndstation()) {
					int response = showBeamBlockedDialog(beamToEndstationStatus.getErrorMessage() + APPEND_LINE, contents);
					if(response == 0x100) {
						return;
					}
				}

				double centreEnergy = Double.valueOf(kineticEnergyText.getText());
				try {
					if(photonEnergy != null && !isKineticEnergyValid(centreEnergy)) {
						showEnergyValidationWarning("Cannot proceed with alignment: photon energy is smaller than or equal to kinetic energy", contents);
						return;
					}
				} catch (ClassCastException e1) {
					String msg = "Could not cast photon energy to double";
					logger.error(msg);
					showEnergyValidationWarning(msg, contents);
					return;
				} catch (DeviceException e1) {
					String msg = "Could not retrieve photon energy from device";
					logger.error(msg);
					showEnergyValidationWarning(msg, contents);
					return;
				}
				double passEnergy = Double.valueOf(passEnergyText.getText());
				double exposure = Double.valueOf(exposureText.getText());
				analyser.startAlignment(passEnergy, centreEnergy, exposure, lensMode.getText());
			}
		});

		// Stop button
		stopButton = new Button(buttons, SWT.DEFAULT);
		stopButton.setLayoutData(new GridData(100, SWT.DEFAULT));
		stopButton.setText("Stop");
		stopButton.setToolTipText("End alignment process");
		stopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				analyser.stopAcquiring();
			}
		});

		// Display counts from analyser
		Label counts = new Label(controlsArea, SWT.NONE);
		FontData fdlabel = counts.getFont().getFontData()[0];
		fdlabel.setHeight(12);
		counts.setFont(new Font(counts.getDisplay(), fdlabel));
		counts.setText("Intensity (counts)");
		countsText = new Text(controlsArea, SWT.BORDER);
		countsText.setEditable(false);
		countsText.setEnabled(false);
		FontData fdtext = countsText.getFont().getFontData()[0];
		fdtext.setHeight(25);
		countsText.setFont(new Font(countsText.getDisplay(), fdtext));
		countsText.setForeground(countsText.getDisplay().getSystemColor(SWT.COLOR_BLUE));
		GridDataFactory.swtDefaults().grab(true, false).hint(220, SWT.DEFAULT).applyTo(countsText);

		indicator = new Button(controlsArea, SWT.DEFAULT);
		indicator.setText(STOPPED_LABEL);
		FontData fdindicator = indicator.getFont().getFontData()[0];
		fdindicator.setHeight(20);
		indicator.setFont(new Font(indicator.getDisplay(), fdtext));
		GridDataFactory.swtDefaults().grab(true, false).hint(200, SWT.DEFAULT).applyTo(indicator);

		// Initialize blinking state
		String currentStatus = status.getCurrentPosition();
		if (currentStatus.equals("Acquire") || currentStatus.equals("Initializing")) {
			indicator.setText(ACTIVE_LABEL);
			keepBlinking = true;
			executor.submit(blink);
		} else {
			indicator.setBackground(SWTResourceManager.getColor(SWT.COLOR_RED));
		}

		// Setup scroll composite
		scrollComp.setContent(contents);
		scrollComp.setExpandHorizontal(true);
		scrollComp.setExpandVertical(true);
		scrollComp.setMinSize(contents.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	/**
	 * Formats numeric string in scientific notation
	 *
	 * @param reading
	 * @return A formatted string
	 */
	private String formatReading(double reading) {
		 return String.format("%8.2e", reading);
	}

	@Override
	public void update(Object source, Object arg) {
		if (!(arg instanceof SpecsPhoibosLiveDataUpdate)) {
			double[] spectrum = ((SpecsPhoibosLiveUpdate)arg).getSpectrum();
			int lastIndex = spectrum.length - 1;
			double latestValue = spectrum[lastIndex];
			Display.getDefault().asyncExec(() -> {
				countsText.setText(formatReading(latestValue));
			});
		}
	}

	public void updateIndicator(Object source, Object arg) {
		if (source == status && arg instanceof String) {
			if (arg.equals("Initializing")) {
				setIndicatorLabel(ACTIVE_LABEL);
				keepBlinking = true;
				executor.submit(blink);
			} else if (arg.equals("Acquire")) {
				//do nothing
			} else {
				keepBlinking = false;
				setIndicatorLabel(STOPPED_LABEL);
			}
		}
	}

	private void checkRequiredFieldsArePresent(ModifyEvent e) {
		if (kineticEnergyText.getText().isEmpty() || passEnergyText.getText().isEmpty() ||
				exposureText.getText().isEmpty()){
			startButton.setEnabled(false);
			stopButton.setEnabled(false);
		}else {
			startButton.setEnabled(true);
			stopButton.setEnabled(true);
		}
	}

	private void checkInputIsNumerical(VerifyEvent e) {
		if (e.character == SWT.BS || e.keyCode == SWT.ARROW_LEFT || e.keyCode == SWT.ARROW_RIGHT
				|| e.keyCode == SWT.DEL || e.keyCode == SWT.KEYPAD_DECIMAL) {
			e.doit = true;
			return;
		}
		if (!('0' <= e.character && e.character <= '9')) {
			e.doit = false;
			return;
		}
		if(((Text)e.widget).getText().length() >= 5) {
			e.doit = false;
		}
	}

	private void color(int swtColor) {
		Display.getDefault().asyncExec(() -> {
			indicator.setBackground(SWTResourceManager.getColor(swtColor));
		});
	}

	private void sleep(long sleepTime) {
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			// do nothing - never interrupted
		}
	}

	private void setIndicatorLabel(String label) {
		Display.getDefault().asyncExec(() -> {
			indicator.setText(label);
		});
	}

	private boolean isKineticEnergyValid(double userSpecifiedKineticEnergy) throws DeviceException, ClassCastException {
		return (double)photonEnergy.getPosition() > userSpecifiedKineticEnergy;
	}

	private void showEnergyValidationWarning(String msg, Composite comp) {
		MessageBox validationDialog = new MessageBox(comp.getShell(), SWT.ICON_ERROR | SWT.OK);
		validationDialog.setText("Check photon energy");
		validationDialog.setMessage(msg);
		validationDialog.open();
	}

	private int showBeamBlockedDialog(String msg, Composite comp) {
		MessageBox validationDialog = new MessageBox(comp.getShell(), SWT.ICON_QUESTION |SWT.OK |SWT.CANCEL);
		validationDialog.setText("Beam is blocked");
		validationDialog.setMessage(msg);
		int userPreference = validationDialog.open();
		return userPreference;
	}

	/**
	 * Create a labelled textbox
	 * @return only textbox
	 */
	private Text addLabeledTextbox(Composite comp, String labelName, String defaultText) {
		Composite custom = new Composite(comp, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(custom);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(custom);
		custom.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		Label label = new Label(custom, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).hint(50, SWT.DEFAULT).applyTo(label);
		label.setText(labelName);
		Text text = new Text(custom, SWT.BORDER);
		text.addModifyListener(this::checkRequiredFieldsArePresent);
		GridDataFactory.swtDefaults().grab(true, false).hint(50, SWT.DEFAULT).applyTo(text);
		text.setText(defaultText);
		return text;
	}

	private Combo addLabelledDropdown(Composite comp, String labelName) {
		Label lensModeLabel = new Label(comp, SWT.NONE);
		lensModeLabel.setText(labelName);
		Combo lensMode = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN);
		lensMode.setItems(analyser.getLensModes().toArray(new String[0]));
		lensMode.select(defaultLensMode);
		return lensMode;
	}

}
