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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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

import com.swtdesigner.SWTResourceManager;

import gda.device.Scannable;
import gda.factory.Finder;
import gda.jython.JythonServerFacade;
import uk.ac.gda.arpes.beans.ARPESScanBean;
import uk.ac.gda.arpes.beans.ScanBeanFromNeXusFile;
import uk.ac.gda.devices.vgscienta.IVGScientaAnalyserRMI;
import uk.ac.gda.devices.vgscienta.VGScientaAnalyserEnergyRange;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public final class ARPESScanBeanComposite extends Composite implements ValueListener {

	private static final Logger logger = LoggerFactory.getLogger(ARPESScanBeanComposite.class);

	private static final String FIXED_MODE = "fixed";
	private static final String SWEPT_MODE = "swept";

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

		// Should be local as its already imported by Spring
		final List<IVGScientaAnalyserRMI> analyserRmiList = Finder.getInstance().listLocalFindablesOfType(IVGScientaAnalyserRMI.class);
		if (analyserRmiList.isEmpty()) {
			throw new RuntimeException("No analyser was found over RMI");
		}
		// TODO Might actually want to handle the case where more than on
		analyser = analyserRmiList.get(0);

		// Get the energy range from the analyser this will now be local don't need to keep making calls over RMI
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

		setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));
		setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		setBackgroundMode(SWT.INHERIT_FORCE);

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
		btnQueueExperiment.setToolTipText("Save file and Acquire immediately");
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
		lblpsuMode.setLayoutData(labelLayoutData());
		lblpsuMode.setText("PSU Mode");
		psuMode = new Label(this, SWT.NONE);
		psuMode.setLayoutData(controlGridData());
		psuMode.setText("Not detected!");

		// Lens Mode
		lblLensMode = new Label(this, SWT.NONE);
		lblLensMode.setLayoutData(labelLayoutData());
		lblLensMode.setText("Lens Mode");
		lensMode = new ComboWrapper(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		lensMode.setLayoutData(controlGridData());
		lensMode.setItems(lensModes);
		lensMode.select(0); // Select the first option so when the GUI draws it can be looked up
		lensMode.addValueListener(this);

		// Pass energy
		lblPassEnergy = new Label(this, SWT.NONE);
		lblPassEnergy.setLayoutData(labelLayoutData());
		lblPassEnergy.setText("Pass Energy");
		passEnergy = new ComboWrapper(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		passEnergy.setLayoutData(controlGridData());
		passEnergy.addValueListener(this);

		// Fixed or swept radio box
		lblSweptMode = new Label(this, SWT.NONE);
		lblSweptMode.setLayoutData(labelLayoutData());
		lblSweptMode.setText("Swept Mode");
		sweptMode = new RadioWrapper(this, SWT.NONE, new String[] { FIXED_MODE, SWEPT_MODE }) {
			@Override
			public void setValue(Object value) {
				super.setValue((Boolean) value ? SWEPT_MODE : FIXED_MODE);
			}

			@Override
			public Object getValue() {
				return SWEPT_MODE.equals(super.getValue());
			}
		};
		sweptMode.addValueListener(this);
		sweptMode.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		// Estimated time
		lblEstimatedTime = new Label(this, SWT.NONE);
		lblEstimatedTime.setLayoutData(labelLayoutData());
		lblEstimatedTime.setText("Estimated Time");
		estimatedTime = new Label(this, SWT.NONE);
		estimatedTime.setLayoutData(controlGridData());

		// Start Energy
		lblStartEnergy = new Label(this, SWT.NONE);
		lblStartEnergy.setLayoutData(labelLayoutData());
		lblStartEnergy.setText("Start Energy");
		startEnergy = new ScaleBox(this, SWT.NONE);
		startEnergy.setLayoutData(controlGridData());
		startEnergy.setUnit("eV");
		startEnergy.setDecimalPlaces(3);

		// Centre energy
		lblCenterEnergy = new Label(this, SWT.NONE);
		lblCenterEnergy.setLayoutData(labelLayoutData());
		lblCenterEnergy.setText("Centre Energy"); //use UK spelling to match scannable name centre_energy
		centreEnergy = new ScaleBox(this, SWT.NONE);
		centreEnergy.setLayoutData(controlGridData());
		centreEnergy.setUnit("eV");
		centreEnergy.setDecimalPlaces(3);
		centreEnergy.setFieldName("centreEnergy");
		centreEnergy.on();

		// End Energy
		lblEndEnergy = new Label(this, SWT.NONE);
		lblEndEnergy.setLayoutData(labelLayoutData());
		lblEndEnergy.setText("End Energy");
		endEnergy = new ScaleBox(this, SWT.NONE);
		endEnergy.setLayoutData(controlGridData());
		endEnergy.setUnit("eV");
		endEnergy.setDecimalPlaces(3);

		// Step energy
		lblStepEnergy = new Label(this, SWT.NONE);
		lblStepEnergy.setLayoutData(labelLayoutData());
		lblStepEnergy.setText("Step Energy");
		stepEnergy = new ScaleBox(this, SWT.NONE);
		stepEnergy.setLayoutData(controlGridData());
		stepEnergy.setUnit("meV");
		stepEnergy.setDecimalPlaces(5);
		stepEnergy.setMaximum(10000);

		// Energy width
		lblEnergyWidth = new Label(this, SWT.NONE);
		lblEnergyWidth.setLayoutData(labelLayoutData());
		lblEnergyWidth.setText("Energy Width");
		energyWidth = new ScaleBox(this, SWT.NONE);
		energyWidth.setLayoutData(controlGridData());
		energyWidth.setUnit("eV");
		energyWidth.setDecimalPlaces(3);
		energyWidth.setFieldName("energyWidth");
		energyWidth.setEditable(false);

		// Time per step
		lblTimePerStep = new Label(this, SWT.NONE);
		lblTimePerStep.setLayoutData(labelLayoutData());
		lblTimePerStep.setText("Time Per Step");
		timePerStep = new ScaleBox(this, SWT.NONE);
		timePerStep.setLayoutData(controlGridData());
		timePerStep.setUnit("s");
		timePerStep.addValueListener(this);

		// Iterations
		lblIterations = new Label(this, SWT.NONE);
		lblIterations.setLayoutData(labelLayoutData());
		lblIterations.setText("Iterations");
		iterations = new IntegerBox(this, SWT.NONE);
		iterations.setLayoutData(controlGridData());
		iterations.addValueListener(this);

		// Configure only
		lblConfigureOnly = new Label(this, SWT.NONE);
		lblConfigureOnly.setLayoutData(labelLayoutData());
		lblConfigureOnly.setText("Configure Only");
		configureOnly = new BooleanWrapper(this, SWT.NONE);
		// Use special grid data here to make selection box the right size
		configureOnly.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

		this.layout(true);

		//Call to get current PSU mode as panel is building
		//Only call after creating all GUI objects
		updatePsuMode();

		// This is to allow dynamic updates of PSU mode from EPICS
		// Create a scannable to allow an observer to be added
		Scannable psuModeScannable = Finder.getInstance().find("psu_mode");

		// Add an observer that updates the PSU mode when fired
		psuModeScannable.addIObserver((source, arg) -> Display.getDefault().asyncExec(this::updatePsuMode));
	}

	private GridData controlGridData() {
		return new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
	}

	private GridData labelLayoutData() {
		return new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
	}

	private void updatePsuMode() {
		try {
			String newPsuMode = analyser.getPsuMode();
			if (!newPsuMode.equals(psuMode.getText())) {
				// If PSU mode has changed update GUI
				logger.info("PSU mode change detected! New PSU mode = " + newPsuMode);
				psuMode.setText(newPsuMode);
				updatePassEnergy();
			}
		} catch (Exception e) {
			logger.error("Error getting the PSU mode", e);
		}
	}

	// Sets the available pass energies from the analyser capabilities + PSU mode
	private void updatePassEnergy() {

		// Store current pass energy to reselect it, if still available
		final int selectedPassEnergy = getSelectedPassEnergy();

		final int i = lensMode.getSelectionIndex();
		Set<Integer> passEnergies = null;
		try {
			passEnergies = energyRange.getPassEnergies(psuMode.getText(), lensModes[i]);
		}
		catch (IllegalArgumentException e) {
			logger.error("Error in providing pass energies. Not all pass energies available are valid!", e);
			// Just show all available pass energies
			passEnergies = energyRange.getAllPassEnergies();
		}

		final Map<String, Integer> passMap = new LinkedHashMap<>();
		for (Integer passEnergy : passEnergies) {
			passMap.put(passEnergy.toString() + " eV", passEnergy);
		}
		passEnergy.setItems(passMap);

		// Try to restore pass energy if its still available
		if (passEnergies.contains(selectedPassEnergy)) {
			// Need to determine the index here there might be a nicer way...
			passEnergy.select((new ArrayList<Integer>(passEnergies)).indexOf(selectedPassEnergy));
		}
		// If the old PE is not available any more just select the first option
		else {
			passEnergy.select(0);
		}
	}

	private double determineMinimumStepEnergy(int passEnergy) {
		return energyStepPerPixel * passEnergy;
	}

	private void updateMinimumStepEnergy() {
		int passEnergyInt = getSelectedPassEnergy();
		stepEnergy.setMinimum(determineMinimumStepEnergy(passEnergyInt));
	}

	private double determineFixedModeEnergyWidth(int passEnergy) {
		// convert meV to eV
		return determineMinimumStepEnergy(passEnergy) * fixedModeEnergyChannels / 1000;
	}

	private int getSelectedPassEnergy(){
		try {
			return Integer.parseInt(passEnergy.getValue().toString());
		} catch (NumberFormatException | NullPointerException e) {
			logger.error("Couldn't determine the pass energy", e);
			return 0; // If we couldn't determine the PE return 0
		}
	}

	// This calculates the acquisition time excluding dead time (i.e. shortest possible)
	private void updateEstimatedTime() {
		Display.getDefault().asyncExec(() -> {

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
		});
	}

	public String msToString(long ms) {
		long totalSecs = ms / 1000;
		long hours = totalSecs / 3600;
		long mins = (totalSecs / 60) % 60;
		long secs = totalSecs % 60;
		String minsString = (mins == 0) ? "00" : ((mins < 10) ? "0" + mins : Long.toString(mins));
		String secsString = (secs == 0) ? "00" : ((secs < 10) ? "0" + secs : Long.toString(secs));
		return hours + ":" + minsString + ":" + secsString;
	}

	private double calculateSweptTime(double stepTime, double startEn, double endEn, double stepEnergyVal, int passEnergyVal) {
		// returns sweptTime estimate in milliseconds
		double minStepEnergyValEv = determineMinimumStepEnergy(passEnergyVal) / 1000; // convert from meV to eV
		double stepEnergyValEv = stepEnergyVal / 1000;                                // convert from meV to eV
		double energyRangeEv   = Math.abs(startEn - endEn);
		return stepTime * (sweptModeEnergyChannels * minStepEnergyValEv + energyRangeEv) / stepEnergyValEv;
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

		if ("lensMode".equals(e.getFieldName())) {
			// Changed lens mode so different pass energies might be available
			updatePassEnergy();
		}

		if ("passEnergy".equals(e.getFieldName()) && !isSwept()) { // Fixed mode
			energyWidth.setValue(determineFixedModeEnergyWidth(getSelectedPassEnergy()));
			startEnergy.setValue(getValue(centreEnergy)	- getValue(energyWidth) / 2.0);
			endEnergy.setValue(getValue(centreEnergy) + getValue(energyWidth) / 2.0);
			stepEnergy.setValue(determineMinimumStepEnergy(getSelectedPassEnergy()));
		}

		if ("startEnergy".equals(e.getFieldName())) {
			// If you change startEnergy must be in swept therefore calculate centre and width
			centreEnergy.setValue((getValue(startEnergy) + getValue(endEnergy)) / 2.0);
			energyWidth.setValue(getValue(endEnergy) - getValue(startEnergy));
		}

		if ("centreEnergy".equals(e.getFieldName())) {
			// If you change centreEnergy must be in fixed mode therefore calculate start and end
			startEnergy.setValue(getValue(centreEnergy) - getValue(energyWidth) / 2.0);
			endEnergy.setValue(getValue(centreEnergy) + getValue(energyWidth) / 2.0);
		}

		if ("endEnergy".equals(e.getFieldName())) {
			// If you change stopEnergy must be in swept therefore calculate centre and width
			centreEnergy.setValue((getValue(startEnergy) + getValue(endEnergy)) / 2.0);
			energyWidth.setValue(getValue(endEnergy) - getValue(startEnergy));
		}

		updateEnergyLimits();
		updateFixedSweptMode();
		updateEstimatedTime();
	}

	private double getValue(NumberBox numberBox) {
		return ((Number) numberBox.getValue()).doubleValue();
	}

	/**
	 * This handles adding and removing listeners and setting controls enabled and disabled depending on the selected mode.
	 */
	private void updateFixedSweptMode() {
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
			energyWidth.setValue(getValue(endEnergy) - getValue(startEnergy));

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
			startEnergy.setValue(getValue(centreEnergy) - getValue(energyWidth) / 2.0);
			endEnergy.setValue(getValue(centreEnergy) + getValue(energyWidth) / 2.0);
			stepEnergy.setValue(determineMinimumStepEnergy(getSelectedPassEnergy()));
		}
	}

	private void updateEnergyLimits() {
		String psuModeString = psuMode.getText();
		String lensModeString = lensModes[lensMode.getSelectionIndex()];
		int passEnergyInt = getSelectedPassEnergy();

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
		centreEnergy.setValue((getValue(startEnergy) + getValue(endEnergy)) / 2.0);

		updateFixedSweptMode();
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
