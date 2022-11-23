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
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import uk.ac.diamond.daq.devices.specs.phoibos.api.AnalyserPVProvider;
import uk.ac.diamond.daq.devices.specs.phoibos.api.IBeamToEndstationStatus;
import uk.ac.diamond.daq.devices.specs.phoibos.api.ISpecsPhoibosAnalyser;
import uk.ac.diamond.daq.devices.specs.phoibos.api.ISpecsPhoibosAnalyserStatus;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosLiveDataUpdate;

public class SpecsAlignmentView implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(SpecsAlignmentView.class);

	private ISpecsPhoibosAnalyser analyser;
	private ISpecsPhoibosAnalyserStatus status;

	protected final EpicsController epicsController = EpicsController.getInstance();
	private final AnalyserPVProvider pvProvider;
	private Channel spectrumChannel;

	private Text passEnergyText;
	private Text kineticEnergyText;
	private Text exposureText;
	private Text countsText;
	private Button startButton;
	private Button stopButton;
	private Combo lensMode;

	private Button indicator;
	private Runnable blink;
	private ExecutorService executor;
	private boolean keepBlinking;
	private final String STOPPED_LABEL = "STOPPED";
	private final String ACTIVE_LABEL = "ACTIVE";
	private final String PHOTON_ENERGY = "pgm_energy";

	private Scannable photonEnergy;

	private Composite child;

	private IBeamToEndstationStatus beamToEndstationStatus;
	private final String APPEND_LINE = "\nClick OK to run scan anyway";

	/**
	 * Constructor
	 */
	public SpecsAlignmentView() {

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
		analyser.addIObserver(this);

		// Get status
		List<ISpecsPhoibosAnalyserStatus> analyserStatusList = Finder.listFindablesOfType(ISpecsPhoibosAnalyserStatus.class);
		if (analyserStatusList.size() != 1) {
			String msg = "No analyser status was found! (Or more than 1)";
			throw new IllegalStateException(msg);
		}
		status = analyserStatusList.get(0);
		status.addIObserver(this::updateIndicator);

		photonEnergy =  Finder.find(PHOTON_ENERGY);

		pvProvider = Finder.findLocalSingleton(AnalyserPVProvider.class);
		try {
			spectrumChannel = epicsController.createChannel(pvProvider.getSpectrumPV());
		} catch (CAException | TimeoutException e) {
			logger.error("Could not create spectrum channel", e);
		}

		// Check if beam in endstation
		beamToEndstationStatus = Finder.find("beam_to_endstation");

	}

	@PostConstruct
	void createView(Composite parent) {

		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		ScrolledComposite scrollComp = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		child = new Composite(scrollComp, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(child);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(child);
		child.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		Composite controlsArea = new Composite(child, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).spacing(10, 10).applyTo(controlsArea);
		controlsArea.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		Label kineticEnergy = new Label(controlsArea, SWT.NONE);
		kineticEnergy.setText("Ekin");
		kineticEnergyText = new Text(controlsArea, SWT.BORDER);
		GridDataFactory.swtDefaults().grab(true, false).hint(50, SWT.DEFAULT).applyTo(kineticEnergyText);
		kineticEnergyText.setText("500");

		Label passEnergy = new Label(controlsArea, SWT.NONE);
		passEnergy.setText("Epass");
		passEnergyText = new Text(controlsArea, SWT.BORDER);
		GridDataFactory.swtDefaults().grab(true, false).hint(50, SWT.DEFAULT).applyTo(passEnergyText);
		passEnergyText.setText("40");

		Label exposureLabel = new Label(controlsArea, SWT.NONE);
		exposureLabel.setText("Dwell");
		exposureText = new Text(controlsArea, SWT.BORDER);
		GridDataFactory.swtDefaults().grab(true, false).hint(50, SWT.DEFAULT).applyTo(exposureText);
		exposureText.setText("1");

		Label lensModeLabel = new Label(controlsArea, SWT.NONE);
		lensModeLabel.setText("Lens mode");
		lensMode = new Combo(controlsArea, SWT.READ_ONLY | SWT.DROP_DOWN);
		lensMode.setItems(analyser.getLensModes().toArray(new String[0]));
		lensMode.select(3);

		Composite buttonsArea = new Composite(child, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).spacing(10, 10).applyTo(buttonsArea);
		buttonsArea.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		// Start button
		startButton = new Button(buttonsArea, SWT.DEFAULT);
		startButton.setLayoutData(new GridData(100, SWT.DEFAULT));
		startButton.setText("Start");
		startButton.setToolTipText("Start alignment process");
		startButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				if (beamToEndstationStatus != null && !beamToEndstationStatus.beamInEndstation()) {
					int response = showBeamBlockedDialog(beamToEndstationStatus.getErrorMessage() + APPEND_LINE);
					if(response == 0x100) {
						return;
					}
				}

				double centreEnergy = Double.valueOf(kineticEnergyText.getText());
				try {
					if(!isKineticEnergyValid(centreEnergy)) {
						showEnergyValidationWarning("Cannot proceed with alignment: photon energy is smaller than or equal to kinetic energy");
						return;
					}
				} catch (ClassCastException e1) {
					String msg = "Could not cast photon energy to double";
					logger.error(msg);
					showEnergyValidationWarning(msg);
					return;
				} catch (DeviceException e1) {
					String msg = "Could not retrieve photon energy from device";
					logger.error(msg);
					showEnergyValidationWarning(msg);
					return;
				}
				double passEnergy = Double.valueOf(passEnergyText.getText());
				double exposure = Double.valueOf(exposureText.getText());
				analyser.startAlignment(passEnergy, centreEnergy, exposure, lensMode.getText());
			}
		});

		// Stop button
		stopButton = new Button(buttonsArea, SWT.DEFAULT);
		stopButton.setLayoutData(new GridData(100, SWT.DEFAULT));
		stopButton.setText("Stop");
		stopButton.setToolTipText("End alignment process");
		stopButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				analyser.stopAcquiring();
			}
		});

		Composite displayArea = new Composite(child, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(1).spacing(10, 10).applyTo(displayArea);
		displayArea.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

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
		fdtext.setHeight(25);
		countsText.setFont(new Font(countsText.getDisplay(), fdtext));
		countsText.setForeground(countsText.getDisplay().getSystemColor(SWT.COLOR_BLUE));
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

		indicator = new Button(displayArea, SWT.DEFAULT);
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
		scrollComp.setContent(child);
		scrollComp.setExpandHorizontal(true);
		scrollComp.setExpandVertical(true);
		scrollComp.setMinSize(child.computeSize(SWT.DEFAULT, SWT.DEFAULT));
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

			double[] spectrum = null ;
			try {
				spectrum = epicsController.cagetDoubleArray(spectrumChannel, 0);
			} catch (TimeoutException | CAException | InterruptedException e) {
				logger.error("Could not get spectrum form channel", e);
			}
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

	private void showEnergyValidationWarning(String msg) {
		MessageBox validationDialog = new MessageBox(child.getShell(), SWT.ICON_ERROR | SWT.OK);
		validationDialog.setText("Check photon energy");
		validationDialog.setMessage(msg);
		validationDialog.open();
	}

	private int showBeamBlockedDialog(String msg) {
		MessageBox validationDialog = new MessageBox(child.getShell(), SWT.ICON_QUESTION |SWT.OK |SWT.CANCEL);
		validationDialog.setText("Beam is blocked");
		validationDialog.setMessage(msg);
		int userPreference = validationDialog.open();
		return userPreference;
	}


}
