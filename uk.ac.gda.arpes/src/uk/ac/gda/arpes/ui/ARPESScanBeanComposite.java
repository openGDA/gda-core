/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.arpes.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.api.event.ValueListener;
import org.eclipse.richbeans.api.widget.IFieldWidget;
import org.eclipse.richbeans.widgets.scalebox.IntegerBox;
import org.eclipse.richbeans.widgets.scalebox.NumberBox;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper;
import org.eclipse.richbeans.widgets.wrappers.ComboWrapper;
import org.eclipse.richbeans.widgets.wrappers.RadioWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Scannable;
import gda.factory.Finder;
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;
import uk.ac.gda.arpes.beans.ARPESScanBean;
import uk.ac.gda.arpes.beans.ScanBeanFromNeXusFile;
import uk.ac.gda.devices.vgscienta.IVGScientaAnalyserRMI;
import uk.ac.gda.devices.vgscienta.VGScientaAnalyserEnergyRange;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public final class ARPESScanBeanComposite extends Composite implements ValueListener {
	private static final Logger logger = LoggerFactory.getLogger(ARPESScanBeanComposite.class);

	// Information from the analyser to make the GUI responsive
	private final IVGScientaAnalyserRMI analyser;
	private final VGScientaAnalyserEnergyRange energyRange;
	private final double energyStepPerPixel;
	private final double maxKE;
	private final int fixedModeEnergyChannels;
	private final int sweptModeEnergyChannels;

	// GUI Items
	private final Label lblpsuMode;
	private final Label psuMode;
	private final Label lblLensMode;
	private final ComboWrapper lensMode;
	private final Label lblPassEnergy;
	private final ComboWrapper passEnergy;
	private final Label lblStartEnergy;
	private final NumberBox startEnergy;
	private final Label lblEndEnergy;
	private final NumberBox endEnergy;
	private final Label lblStepEnergy;
	private final NumberBox stepEnergy;
	private final Label lblTimePerStep;
	private final NumberBox timePerStep;
	private final Label lblIterations;
	private final NumberBox iterations;
	private final Label lblSweptMode;
	private final RadioWrapper sweptMode;
	private final Label lblCenterEnergy;
	private final NumberBox centreEnergy;
	private final Label lblEnergyWidth;
	private final NumberBox energyWidth;
	private final Label lblConfigureOnly;
	private final BooleanWrapper configureOnly;
	private final String[] lensModes;
	private final Label lblEstimatedTime;
	private final Label estimatedTime;

	public ARPESScanBeanComposite(final Composite parent, int style, final RichBeanEditorPart editor) {
		super(parent, style);

		//Switch off undoing as it doesn't work when box values are programmatically updated
		editor.setUndoStackActive(false);

		// Get the energy range from the analyser this will now be local don't need to keep making calls over RMI
		analyser = Finder.getInstance().find("analyserRmi");
		energyRange = analyser.getEnergyRange();
		// Find all the lens modes
		lensModes = energyRange.getAllLensModes().toArray(new String[0]);
		// Get the energy step per pixel
		energyStepPerPixel = analyser.getEnergyStepPerPixel();
		// Get the fall-back max KE
		maxKE = analyser.getMaxKE();
		// Get the number of energy channels in fixed mode
		fixedModeEnergyChannels = analyser.getFixedModeEnergyChannels();
		// Get the number of energy channels in swept mode
		sweptModeEnergyChannels = analyser.getSweptModeEnergyChannels();


		// Make a 2 column grid layout
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.horizontalSpacing = 10;
		setLayout(gridLayout);

		// First row
		Label label = new Label(this, SWT.NONE);
		label.setText("Drop file here!");
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		setDropTarget(parent, parent.getShell(), editor);

		Composite btnComp = new Composite(this, SWT.NONE);
		btnComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

		btnComp.setLayout(new GridLayout(2, false));

		Button btnClipboard = new Button(btnComp, SWT.NONE);
		btnClipboard.setText("Jython to Clipboard");
		btnClipboard
				.setToolTipText("Save file and copy Jython instructions to clip board to use this defintion in scripts");
		btnClipboard.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				try {
					IProgressMonitor monitor = new NullProgressMonitor();
					editor.doSave(monitor);
					if (monitor.isCanceled()) {
						return;
					}
					Display display = Display.getCurrent();
					Clipboard clipboard = new org.eclipse.swt.dnd.Clipboard(display);
					String[] data = { getOurJythonCommand(editor) };
					clipboard.setContents(data, new Transfer[] { TextTransfer.getInstance() });
					clipboard.dispose();
				} catch (Exception e1) {
					logger.error("Error sending command to the clipboard", e1);
				}
			}
		});

		Button btnQueueExperiment = new Button(btnComp, SWT.NONE);
		btnQueueExperiment.setText("Acquire Now!");
		btnQueueExperiment
				.setToolTipText("Save file and Acquire immediately");
		btnQueueExperiment.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				IProgressMonitor monitor = new NullProgressMonitor();
				editor.doSave(monitor);
				if (monitor.isCanceled()) {
					return;
				}
				// Check if baton is held
				boolean batonHeld = JythonServerFacade.getInstance().isBatonHeld();
				if (!batonHeld) {
					MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(), "Baton not held",
							null, "You do not hold the baton, please take the baton using the baton manager.",
							MessageDialog.ERROR, new String[] { "Ok" }, 0);
					dialog.open();
				} else { // Baton is held run the scan
					JythonServerFacade.getInstance().runCommand(
							String.format("arpes.ARPESRun(\"%s\").run()", editor.getPath()));
				}
			}

		});

		// PSU mode
		lblpsuMode = new Label(this, SWT.NONE);
		lblpsuMode.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		lblpsuMode.setText("PSU Mode");
		psuMode = new Label(this, SWT.NONE);
		GridData gd_psuMode = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_psuMode.widthHint = 200;
		psuMode.setLayoutData(gd_psuMode);
		psuMode.setText("Not detected!");

		// Lens Mode
		lblLensMode = new Label(this, SWT.NONE);
		lblLensMode.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblLensMode.setText("Lens Mode");
		lensMode = new ComboWrapper(this, SWT.NONE);
		GridData gd_lensMode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lensMode.widthHint = 200;
		lensMode.setLayoutData(gd_lensMode);
		lensMode.setItems(lensModes);
		lensMode.select(0); // Select the first option so when the GUI draws it can be looked up
		lensMode.addValueListener(this);

		// Pass energy
		lblPassEnergy = new Label(this, SWT.NONE);
		lblPassEnergy.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPassEnergy.setText("Pass Energy");
		passEnergy = new ComboWrapper(this, SWT.NONE);
		GridData gd_passEnergy = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_passEnergy.widthHint = 200;
		passEnergy.setLayoutData(gd_passEnergy);
		passEnergy.addValueListener(this);

		// Fixed or swept radio box
		lblSweptMode = new Label(this, SWT.NONE);
		lblSweptMode.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblSweptMode.setText("Swept Mode");
		sweptMode = new RadioWrapper(this, SWT.NONE, new String[] { "fixed", "swept" }) {
			@Override
			public void setValue(Object value) {
				super.setValue((Boolean) value ? "swept" : "fixed");
			}

			@Override
			public Object getValue() {
				return super.getValue().equals("swept");
			}
		};
		sweptMode.addValueListener(this);

		// Estimated time
		lblEstimatedTime = new Label(this, SWT.NONE);
		lblEstimatedTime.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		lblEstimatedTime.setText("Estimated Time");
		estimatedTime = new Label(this, SWT.NONE);
		GridData gd_estimatedTime = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_estimatedTime.widthHint = 200;
		estimatedTime.setLayoutData(gd_estimatedTime);

		// Start Energy
		lblStartEnergy = new Label(this, SWT.NONE);
		lblStartEnergy.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblStartEnergy.setText("Start Energy");
		startEnergy = new ScaleBox(this, SWT.NONE);
		GridData gridData = (GridData) startEnergy.getControl().getLayoutData();
		gridData.widthHint = 200;
		gridData.horizontalAlignment = SWT.LEFT;
		gridData.grabExcessHorizontalSpace = false;
		startEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		startEnergy.setUnit("eV");
		startEnergy.setDecimalPlaces(3);

		// Centre energy
		lblCenterEnergy = new Label(this, SWT.NONE);
		lblCenterEnergy.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCenterEnergy.setText("Centre Energy"); //use UK spelling to match scannable name centre_energy
		centreEnergy = new ScaleBox(this, SWT.NONE);
		GridData gridData_1 = (GridData) centreEnergy.getControl().getLayoutData();
		gridData_1.widthHint = 200;
		gridData_1.horizontalAlignment = SWT.LEFT;
		gridData_1.grabExcessHorizontalSpace = false;
		centreEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		centreEnergy.setUnit("eV");
		centreEnergy.setDecimalPlaces(3);
		centreEnergy.setFieldName("centreEnergy");
		centreEnergy.on();

		// End Energy
		lblEndEnergy = new Label(this, SWT.NONE);
		lblEndEnergy.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblEndEnergy.setText("End Energy");
		endEnergy = new ScaleBox(this, SWT.NONE);
		GridData gridData_2 = (GridData) endEnergy.getControl().getLayoutData();
		gridData_2.widthHint = 200;
		gridData_2.horizontalAlignment = SWT.LEFT;
		gridData_2.grabExcessHorizontalSpace = false;
		endEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		endEnergy.setUnit("eV");
		endEnergy.setDecimalPlaces(3);

		// Step energy
		lblStepEnergy = new Label(this, SWT.NONE);
		lblStepEnergy.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblStepEnergy.setText("Step Energy");
		stepEnergy = new ScaleBox(this, SWT.NONE);
		GridData gridData_3 = (GridData) stepEnergy.getControl().getLayoutData();
		gridData_3.widthHint = 200;
		gridData_3.horizontalAlignment = SWT.LEFT;
		gridData_3.grabExcessHorizontalSpace = false;
		stepEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		stepEnergy.setUnit("meV");
		stepEnergy.setDecimalPlaces(5);
		stepEnergy.setMaximum(10000);

		// Energy width
		lblEnergyWidth = new Label(this, SWT.NONE);
		lblEnergyWidth.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblEnergyWidth.setText("Energy Width");
		energyWidth = new ScaleBox(this, SWT.NONE);
		GridData gridData_4 = (GridData) energyWidth.getControl().getLayoutData();
		gridData_4.widthHint = 200;
		gridData_4.horizontalAlignment = SWT.LEFT;
		gridData_4.grabExcessHorizontalSpace = false;
		energyWidth.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		energyWidth.setUnit("eV");
		energyWidth.setDecimalPlaces(3);
		energyWidth.setFieldName("energyWidth");
		energyWidth.setEditable(false);

		// Time per step
		lblTimePerStep = new Label(this, SWT.NONE);
		lblTimePerStep.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTimePerStep.setText("Time Per Step");
		timePerStep = new ScaleBox(this, SWT.NONE);
		GridData gridData_5 = (GridData) timePerStep.getControl().getLayoutData();
		gridData_5.widthHint = 200;
		gridData_5.horizontalAlignment = SWT.LEFT;
		gridData_5.grabExcessHorizontalSpace = false;
		timePerStep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		timePerStep.setUnit("s");
		timePerStep.addValueListener(this);

		// Iterations
		lblIterations = new Label(this, SWT.NONE);
		lblIterations.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblIterations.setText("Iterations");
		iterations = new IntegerBox(this, SWT.NONE);
		GridData gridData_6 = (GridData) iterations.getControl().getLayoutData();
		gridData_6.widthHint = 200;
		gridData_6.horizontalAlignment = SWT.LEFT;
		gridData_6.grabExcessHorizontalSpace = false;
		iterations.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		iterations.addValueListener(this);

		// Configure only
		lblConfigureOnly = new Label(this, SWT.NONE);
		lblConfigureOnly.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblConfigureOnly.setText("Configure Only");
		configureOnly = new BooleanWrapper(this, SWT.NONE);
		configureOnly.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		//Call to get current PSU mode as panel is building
		//Only call after creating all GUI objects
		updatePsuMode();

		// This is to allow dynamic updates of PSU mode from EPICS
		// Create a scannable to allow an observer to be added
		Scannable psuModeScannable = (Scannable) (Finder.getInstance().find("psu_mode"));

		// Create an observer that updates the PSU mode when fired
		final IObserver psuModeObserver = new IObserver() {
			@Override
			public void update(Object source, Object arg) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						updatePsuMode();
					}
				});
			}
		};

		// Connect observer to scannable.
		psuModeScannable.addIObserver(psuModeObserver);
	}

	private void updatePsuMode() {
		try {
		String newPsuMode = analyser.getPsuMode();
		if (!newPsuMode.equals(psuMode.getText())){
			//If PSU mode has changed update GUI
			logger.info("PSU mode change detected! New PSU mode = " + newPsuMode);
			psuMode.setText(newPsuMode);
			updatePassEnergy();
		}
		}
		catch (Exception e) {
			logger.error("Error getting the PSU mode", e);
		}
	}

	// Sets the available pass energies from the analyser capabilities + PSU mode
	private void updatePassEnergy() {
		int i = lensMode.getSelectionIndex();

		Set<Integer> passEnergies = null;
		try {
			passEnergies = energyRange.getPassEnergies(psuMode.getText(), lensModes[i]);
		}
		catch (IllegalArgumentException e) {
			logger.error("Error in providing pass energies. Not all pass energies available are valid!", e);
			// Just show all available pass energies
			passEnergies = energyRange.getAllPassEnergies();
		}

		Map<String, Integer> passMap = new LinkedHashMap<String, Integer>();
		for (Integer passEnergy : passEnergies) {
			passMap.put(passEnergy.toString() + " eV", passEnergy);
		}
		passEnergy.setItems(passMap);
	}

	private double determineMinimumStepEnergy(int passEnergy) {
		return energyStepPerPixel * passEnergy;
	}

	private void updateMinimumStepEnergy() {
		int passEnergyInt = Integer.parseInt(passEnergy.getValue().toString());
		stepEnergy.setMinimum(determineMinimumStepEnergy(passEnergyInt));
	}

	private double determineFixedModeEnergyWidth(int passEnergy) {
		// convert meV to eV
		return determineMinimumStepEnergy(passEnergy) * fixedModeEnergyChannels / 1000;
	}

	private int getSelectedPassEnergy(){
		return Integer.parseInt(passEnergy.getValue().toString());
	}

	// This calculates the acquisition time excluding dead time (i.e. shortest possible)
	private void updateEstimatedTime() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {

				double estimatedTimeSecs = 0.0;
				int numberOfIterations = iterations.getIntegerValue();
				double stepTime = timePerStep.getNumericValue();
				boolean isSweptMode = (Boolean) sweptMode.getValue();

				if (isSweptMode) {
					double startEnergyVal = startEnergy.getNumericValue();
					double endEnergyVal = endEnergy.getNumericValue();
					double stepEnergyVal = stepEnergy.getNumericValue();
					int passEnergyVal = getSelectedPassEnergy();
					estimatedTimeSecs = numberOfIterations * calculateSweptTime(stepTime, startEnergyVal, endEnergyVal, stepEnergyVal, passEnergyVal);
				} else { // Fixed mode
					estimatedTimeSecs = numberOfIterations * stepTime;
				}

				String time = msToString((long) (estimatedTimeSecs * 1000));
				estimatedTime.setText(time + " (hh:mm:ss)");
			}
		});
	}

	public String msToString(long ms) {
		long totalSecs = ms / 1000;
		long hours = (totalSecs / 3600);
		long mins = (totalSecs / 60) % 60;
		long secs = totalSecs % 60;
		String minsString = (mins == 0) ? "00" : ((mins < 10) ? "0" + mins : "" + mins);
		String secsString = (secs == 0) ? "00" : ((secs < 10) ? "0" + secs : "" + secs);
		return hours + ":" + minsString + ":" + secsString;
	}

	private double calculateSweptTime(double stepTime, double startEn, double endEn, double stepEnergyVal, int passEnergyVal) {
		// returns sweptTime estimate in milliseconds
		double minStepEnergyValEv = determineMinimumStepEnergy(passEnergyVal) / 1000; // convert from meV to eV
		double stepEnergyValEv = stepEnergyVal / 1000;                                // convert from meV to eV
		double energyRangeEv   = Math.abs(startEn - endEn);
		return  (stepTime * (sweptModeEnergyChannels * minStepEnergyValEv + energyRangeEv) / stepEnergyValEv);
	}

	protected String getOurJythonCommand(final RichBeanEditorPart editor) {
		return String.format("import arpes; arpes.ARPESRun(\"%s\").run()", editor.getPath());
	}

	public IFieldWidget getLensMode() {
		return lensMode;
	}

	public IFieldWidget getPassEnergy() {
		return passEnergy;
	}

	public IFieldWidget getStartEnergy() {
		return startEnergy;
	}

	public IFieldWidget getEndEnergy() {
		return endEnergy;
	}

	public IFieldWidget getStepEnergy() {
		return stepEnergy;
	}

	public IFieldWidget getTimePerStep() {
		return timePerStep;
	}

	public IFieldWidget getIterations() {
		return iterations;
	}

	public IFieldWidget getConfigureOnly() {
		return configureOnly;
	}

	public IFieldWidget getSweptMode() {
		return sweptMode;
	}

	private boolean isSwept() {
		return (Boolean) sweptMode.getValue();
	}

	@Override
	public void valueChangePerformed(ValueEvent e) {

		if (e.getFieldName().equals("lensMode")) {
			updateEnergyLimits();
		}

		if (e.getFieldName().equals("passEnergy")) {
			if (!isSwept()) {// Fixed mode
				energyWidth.setValue(determineFixedModeEnergyWidth(getSelectedPassEnergy()));
				startEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue()
						- ((Number) energyWidth.getValue()).doubleValue() / 2.0);
				endEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue()
						+ ((Number) energyWidth.getValue()).doubleValue() / 2.0);
				stepEnergy.setValue(determineMinimumStepEnergy(getSelectedPassEnergy()));
			}
		}

		if (e.getFieldName().equals("sweptMode")) {
			if (isSwept()) {
				// In swept edit start, stop and step, not centre
				startEnergy.setEditable(true);
				centreEnergy.setEditable(false);
				endEnergy.setEditable(true);
				stepEnergy.setEditable(true);
				// Stop watching for changes in centre energy as they are programmatic
				startEnergy.addValueListener(this);
				centreEnergy.removeValueListener(this);
				endEnergy.addValueListener(this);
				stepEnergy.addValueListener(this);

			} else { // Fixed mode
				// In fixed edit centre only
				startEnergy.setEditable(false);
				centreEnergy.setEditable(true);
				endEnergy.setEditable(false);
				stepEnergy.setEditable(false);
				// Only watch for changes in centreEnergy in fixed mode
				startEnergy.removeValueListener(this);
				centreEnergy.addValueListener(this);
				endEnergy.removeValueListener(this);
				stepEnergy.removeValueListener(this);
				// Update values as appropriate for fixed mode
				energyWidth.setValue(determineFixedModeEnergyWidth(getSelectedPassEnergy()));
				startEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue()
						- ((Number) energyWidth.getValue()).doubleValue() / 2.0);
				endEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue()
						+ ((Number) energyWidth.getValue()).doubleValue() / 2.0);
				stepEnergy.setValue(determineMinimumStepEnergy(getSelectedPassEnergy()));
			}
		}

		if (e.getFieldName().equals("startEnergy")) {
			// If you change startEnergy must be in swept therefore calculate centre and width
			centreEnergy.setValue((((Number) startEnergy.getValue()).floatValue() + ((Number) endEnergy.getValue()).floatValue()) / 2.0);
			energyWidth.setValue(((Number) endEnergy.getValue()).floatValue() - ((Number) startEnergy.getValue()).floatValue());
		}

		if (e.getFieldName().equals("centreEnergy")) {
			// If you change centreEnergy must be in fixed mode therefore calculate start and end
			startEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue() - ((Number) energyWidth.getValue()).doubleValue() / 2.0);
			endEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue() + ((Number) energyWidth.getValue()).doubleValue() / 2.0);
		}

		if (e.getFieldName().equals("endEnergy")) {
			// If you change stopEnergy must be in swept therefore calculate centre and width
			centreEnergy.setValue((((Number) startEnergy.getValue()).floatValue() + ((Number) endEnergy.getValue()).floatValue()) / 2.0);
			energyWidth.setValue(((Number) endEnergy.getValue()).floatValue() - ((Number) startEnergy.getValue()).floatValue());
		}
		updatePsuMode();
		updateEnergyLimits();
		updateEstimatedTime();
	}

	private void updateEnergyLimits() {
		String psuModeString = psuMode.getText();
		String lensModeString = lensModes[lensMode.getSelectionIndex()];
		int passEnergyInt = Integer.parseInt(passEnergy.getValue().toString());

		double min = 0;
		double max = maxKE;
		try {
			min = energyRange.getMinKE(psuModeString, lensModeString, passEnergyInt);
			max = energyRange.getMaxKE(psuModeString, lensModeString, passEnergyInt);
		}
		catch (IllegalArgumentException e) {
			logger.error("Error calculating energy limits. Setting defualts min: {} max {}", min, max, e);
		}

		if (isSwept()) { // Swept mode
			startEnergy.setMinimum(min);
			startEnergy.setMaximum(max);
			centreEnergy.setMinimum(-1);
			centreEnergy.setMaximum(10000);
			endEnergy.setMinimum(min);
			endEnergy.setMaximum(max);
			updateMinimumStepEnergy();
		} else { // Fixed mode
			startEnergy.setMinimum(-1);
			startEnergy.setMaximum(10000);
			centreEnergy.setMinimum(min);
			centreEnergy.setMaximum(max);
			endEnergy.setMinimum(-1);
			endEnergy.setMaximum(10000);
		}
	}

	@Override
	public String getValueListenerName() {
		return null;
	}

	public void beanUpdated() {
		// Centre energy is not saved in the XML so need to be calculated from start and stop
		centreEnergy.setValue((((Number) startEnergy.getValue()).floatValue() + ((Number) endEnergy.getValue()).floatValue()) / 2.0);

		if (isSwept()) {
			// In swept edit start, stop and step, not centre
			startEnergy.setEditable(true);
			centreEnergy.setEditable(false);
			endEnergy.setEditable(true);
			stepEnergy.setEditable(true);
			// Stop watching for changes in centre energy as they are programmatic
			startEnergy.addValueListener(this);
			centreEnergy.removeValueListener(this);
			endEnergy.addValueListener(this);
			stepEnergy.addValueListener(this);
			// Calculate values to rebuild the editor fully these fields should not be listened to otherwise will fire
			// another updates
			energyWidth.setValue(((Number) endEnergy.getValue()).floatValue() - ((Number) startEnergy.getValue()).floatValue());

		} else { // Fixed mode
			// In fixed edit centre only
			startEnergy.setEditable(false);
			centreEnergy.setEditable(true);
			endEnergy.setEditable(false);
			stepEnergy.setEditable(false);
			// Only watch for changes in centreEnergy in fixed mode
			startEnergy.removeValueListener(this);
			centreEnergy.addValueListener(this);
			endEnergy.removeValueListener(this);
			stepEnergy.removeValueListener(this);
			// Calculate values to rebuild the editor fully these fields should not be listened to otherwise will fire
			// another updates
			energyWidth.setValue(determineFixedModeEnergyWidth(getSelectedPassEnergy()));
			startEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue()
					- ((Number) energyWidth.getValue()).doubleValue() / 2.0);
			endEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue()
					+ ((Number) energyWidth.getValue()).doubleValue() / 2.0);
			stepEnergy.setValue(determineMinimumStepEnergy(getSelectedPassEnergy()));

		}
		updatePsuMode();
		updateEnergyLimits();
		updateEstimatedTime();
	}

	public static void setDropTarget(final Composite parent, final Shell shell, final RichBeanEditorPart editor) {
		int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
		DropTarget target = new DropTarget(parent, operations);
		target.setTransfer(new Transfer[] { FileTransfer.getInstance() });
		target.addDropListener(new DropTargetAdapter() {
			@Override
			public void dragEnter(DropTargetEvent e) {
				if (e.detail == DND.DROP_NONE) {
					e.detail = DND.DROP_LINK;
				}
			}

			@Override
			public void dragOperationChanged(DropTargetEvent e) {
				if (e.detail == DND.DROP_NONE) {
					e.detail = DND.DROP_LINK;
				}
			}

			@Override
			public void drop(DropTargetEvent event) {
				if (event.data == null) {
					event.detail = DND.DROP_NONE;
					return;
				}
				String[] filenames = (String[]) event.data;
				if (filenames.length > 1) {
					MessageDialog.openError(shell, "too many files",
							"Please drop one file only in here.\nI cannot copy settings from multiple sources.");
					return;
				}
				ARPESScanBean bean;
				try {
					bean = ScanBeanFromNeXusFile.read(filenames[0]);
					((ARPESScanBeanUIEditor) editor).replaceBean(bean);
					((DirtyContainer) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor()).setDirty(true);
				} catch (Exception e) {
					logger.error("error converting nexus file to bean", e);
					// TODO better messages for frequent cases (no analyser in file)
					MessageDialog.openError(shell, "error reading nexus file for settings",
							"Analyser settings from that file could not be read.");
					return;
				}
				// TODO message for non-analyser parameters (exit slit, entrance slit, photon energy)
				// TODO deal with multi-dim files

				MessageDialog dialog = new MessageDialog(shell, "Save imported settings", null,
						"We would suggest saving this experiment under a new name now.", MessageDialog.QUESTION,
						new String[] { "Save as...", "Keep existing name and don't save yet" }, 0);
				int result = dialog.open();
				if (result == 0) {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().doSaveAs();
				}
			}
		});
	}
}
