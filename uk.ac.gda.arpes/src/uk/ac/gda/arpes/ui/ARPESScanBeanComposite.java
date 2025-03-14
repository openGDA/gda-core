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
import java.util.Optional;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import gda.jython.JythonServerFacade;
import uk.ac.diamond.daq.pes.api.AcquisitionMode;
import uk.ac.diamond.daq.pes.api.AnalyserDeflectorRangeConfiguration;
import uk.ac.diamond.daq.pes.api.AnalyserEnergyRangeConfiguration;
import uk.ac.diamond.daq.pes.api.IDeflector;
import uk.ac.diamond.daq.pes.api.IDitherScanning;
import uk.ac.diamond.daq.pes.api.IElectronAnalyser;
import uk.ac.gda.arpes.beans.ARPESScanBean;
import uk.ac.gda.arpes.beans.ScanBeanFromNeXusFile;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public final class ARPESScanBeanComposite extends Composite implements ValueListener {

	private static final Logger logger = LoggerFactory.getLogger(ARPESScanBeanComposite.class);

	// Information from the analyser to make the GUI responsive
	private final IElectronAnalyser analyser;
	private final AnalyserEnergyRangeConfiguration energyRange;
	private  AnalyserDeflectorRangeConfiguration deflectorRangeConfig = null;
	private final double energyStepPerPixel;
	private final double maxKE;
	private final int fixedModeEnergyChannels;
	private final int sweptModeEnergyChannels;
	private final int maxNumberOfSteps;

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
	private final Label lblNumberOfSteps;
	private final Text numberOfSteps;
	private final Label lblTimePerStep;
	private final NumberBox timePerStep;
	private final Label lblIterations;
	private final NumberBox iterations;
	private final Label lblEnergyMode;
	private final RadioWrapper acquisitionMode;
	private final Label lblCenterEnergy;
	private final NumberBox centreEnergy;
	private final Label lblEnergyWidth;
	private final NumberBox energyWidth;
	private final Label lblConfigureOnly;
	private final BooleanWrapper configureOnly;
	private final String[] lensModes;
	private final Label lblEstimatedTime;
	private final Label estimatedTime;
	private final Label lblDeflectorX;
	private final NumberBox deflectorX;

	private AcquisitionMode lastSelectedAcquisitionMode;
	private Optional<Double> cachedFixedModeCentreEnergy = Optional.empty();
	private Optional<Double> cachedDitherModeCentreEnergy = Optional.empty();
	private Optional<Double> cachedSweptModeStartEnergy = Optional.empty();
	private Optional<Double> cachedSweptModeEndEnergy = Optional.empty();

	public ARPESScanBeanComposite(final Composite parent, int style, final RichBeanEditorPart editor) {
		super(parent, style);

		//Switch off undoing as it doesn't work when box values are programmatically updated
		editor.setUndoStackActive(false);

		// Should be local as its already imported by Spring
		final List<IElectronAnalyser> analyserRmiList = Finder.listLocalFindablesOfType(IElectronAnalyser.class);
		if (analyserRmiList.isEmpty()) {
			throw new RuntimeException("No analyser was found over RMI");
		}
		// TODO Might actually want to handle the case where more than on
		analyser = analyserRmiList.get(0);

		// Get the energy range from the analyser this will now be local don't need to keep making calls over RMI
		energyRange = analyser.getEnergyRange();
		// Get deflector range if present
		if (analyser instanceof IDeflector) {
			deflectorRangeConfig = ((IDeflector)analyser).getDeflectorRangeConfiguration();
		}
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
		// Get the maximum number of steps allowed in a step scan
		maxNumberOfSteps = analyser.getMaximumNumberOfSteps();

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

		// Acquisition mode radio box
		lblEnergyMode = new Label(this, SWT.NONE);
		lblEnergyMode.setLayoutData(labelLayoutData());
		lblEnergyMode.setText("Acquisition Mode");

		String[] supportedAcquisitionModes = analyser.getSupportedAcquisitionModes()
				.stream()
				.map(AcquisitionMode::getLabel)
				.toArray(String[]::new);

		acquisitionMode = new RadioWrapper(this, SWT.NONE, supportedAcquisitionModes) {
			@Override
			public void setValue(Object value) {
				if (value instanceof AcquisitionMode) {
					super.setValue(((AcquisitionMode)value).getLabel());
				}
			}

			@Override
			public Object getValue() {
				var value = super.getValue();
				if (value == null) {
					return AcquisitionMode.FIXED;
				}
				return AcquisitionMode.valueOfLabel((String)value);
			}
		};
		acquisitionMode.addValueListener(this);
		acquisitionMode.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

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
		stepEnergy.setFieldName("stepEnergy");

		// Number of steps
		lblNumberOfSteps = new Label(this, SWT.NONE);
		lblNumberOfSteps.setLayoutData(labelLayoutData());
		lblNumberOfSteps.setText("Number of Steps");
		numberOfSteps = new Text(this, SWT.BORDER);
		numberOfSteps.setLayoutData(controlGridData());
		numberOfSteps.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		numberOfSteps.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
		numberOfSteps.setEditable(false);

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

		// DeflectorX
		lblDeflectorX = new Label(this, SWT.NONE);
		lblDeflectorX.setLayoutData(labelLayoutData());
		lblDeflectorX.setText("Deflector X");
		deflectorX = new ScaleBox(this, SWT.NONE);
		deflectorX.setLayoutData(controlGridData());
		deflectorX.setDecimalPlaces(3);
		deflectorX.setFieldName("deflectorX");

		if (!(analyser instanceof IDeflector)) {
			deflectorX.setEnabled(false);
			deflectorX.setToolTipText("The current electron analyser does not have a deflector.");
		}

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
		Optional<Scannable> psuModeScannable = Finder.findOptionalOfType("psu_mode", Scannable.class);

		// Add an observer that updates the PSU mode when fired
		psuModeScannable.ifPresent(s -> s.addIObserver((source, arg) -> Display.getDefault().asyncExec(this::updatePsuMode)));

		lastSelectedAcquisitionMode = getSelectedAcquisitionMode();
		cacheEnergyValues();
		updateDeflectorLimits();
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

	private void updateDeflectorLimits() {
		if (deflectorRangeConfig == null) return;

		var deflectorRange = deflectorRangeConfig.getDeflectorRangeForLensMode(getSelectedLensMode());

		if (deflectorRange.hasDeflectorEnabled()) {
			deflectorX.setEnabled(true);
			deflectorX.setToolTipText("");
			deflectorX.setMinimum(deflectorRange.getDeflectorXMinimum());
			deflectorX.setMaximum(deflectorRange.getDeflectorXMaximum());
		} else {
			deflectorX.setValue(0);
			deflectorX.setEnabled(false);
			deflectorX.setToolTipText("Deflector is not available in this lens mode.");
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
			String passEnergyString = passEnergy.getValue().toString();
			if (passEnergyString.isEmpty()) {
				return 0; // Will be the case if a new editor is being opened
			}
			return Integer.parseInt(passEnergyString);
		} catch (NumberFormatException | NullPointerException e) {
			logger.error("Couldn't determine the pass energy", e);
			return 0; // If we couldn't determine the PE return 0
		}
	}

	private AcquisitionMode getSelectedAcquisitionMode() {
		return (AcquisitionMode)acquisitionMode.getValue();
	}

	private String getSelectedLensMode() {
		return lensMode.getValue().toString();
	}

	// This calculates the acquisition time excluding dead time (i.e. shortest possible)
	private void updateEstimatedTime() {
		Display.getDefault().asyncExec(() -> {

			switch (getSelectedAcquisitionMode()) {
			case FIXED:
				estimatedTime.setText(secondsToString(estimateFixedTimeInSeconds()));
				break;
			case SWEPT:
				estimatedTime.setText(secondsToString(estimateSweptTimeInSeconds()));
				break;
			case DITHER:
				estimatedTime.setText(secondsToString(estimateDitherTimeInSeconds()));
				break;
			default:
				estimatedTime.setText("Unknown acquisition mode. Unable to calculate." );

			}
		});
	}

	private String secondsToString(long totalSeconds) {
		long hours = totalSeconds / 3600;
		long mins = (totalSeconds / 60) % 60;
		long secs = totalSeconds % 60;
		String minsString = (mins == 0) ? "00" : ((mins < 10) ? "0" + mins : Long.toString(mins));
		String secsString = (secs == 0) ? "00" : ((secs < 10) ? "0" + secs : Long.toString(secs));
		return hours + ":" + minsString + ":" + secsString + " (hh:mm:ss)";
	}

	private long estimateFixedTimeInSeconds() {
		var numberOfIterations = iterations.getIntegerValue();
		double stepTime = timePerStep.getNumericValue();

		return (long)(numberOfIterations * stepTime);
	}

	private long estimateSweptTimeInSeconds() {
		int passEnergyVal = getSelectedPassEnergy();
		double stepEnergyVal = stepEnergy.getNumericValue();
		double minStepEnergyValEv = determineMinimumStepEnergy(passEnergyVal) / 1000; // convert from meV to eV
		double stepEnergyValEv = stepEnergyVal / 1000;                                // convert from meV to eV

		double startEnergyVal = startEnergy.getNumericValue();
		double endEnergyVal = endEnergy.getNumericValue();
		double energyRangeEv   = Math.abs(startEnergyVal - endEnergyVal);
		double stepTime = timePerStep.getNumericValue();
		// use this to adjust step time since this class is shared with nano branch
		if (analyser instanceof IDitherScanning) {
			stepTime += 0.016;
		}

		var numberOfIterations = iterations.getIntegerValue();

		return (long)(numberOfIterations * (stepTime * (sweptModeEnergyChannels * minStepEnergyValEv + energyRangeEv) / stepEnergyValEv));
	}

	private long estimateDitherTimeInSeconds() {
		var numberOfIterations = iterations.getIntegerValue();
		double stepTime = timePerStep.getNumericValue();

		int numberOfDitherSteps = 1;

		if (analyser instanceof IDitherScanning) {
			try {
				numberOfDitherSteps = ((IDitherScanning) analyser).getNumberOfDitherSteps();
			} catch (DeviceException exception) {
				logger.error("Error while retrieving current number of dither steps", exception);
			}
		}
		return (long)(numberOfIterations * stepTime * (numberOfDitherSteps + 4));
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

	public IFieldWidget getAcquisitionMode() {
		return acquisitionMode;
	}

	public IFieldWidget getDeflectorX() {
		return deflectorX;
	}

	private boolean isSweptMode() {
		return getSelectedAcquisitionMode() == AcquisitionMode.SWEPT;
	}

	@Override
	public void valueChangePerformed(ValueEvent e) {

		if ("acquisitionMode".equals(e.getFieldName())) {
			cacheEnergyValues();
			updateNumberOfSteps();
		}

		if ("lensMode".equals(e.getFieldName())) {
			// Changed lens mode so different pass energies might be available
			updatePassEnergy();
			updateDeflectorLimits();
		}

		if ("passEnergy".equals(e.getFieldName()) && !isSweptMode()) { // Fixed or dither mode
			energyWidth.setValue(determineFixedModeEnergyWidth(getSelectedPassEnergy()));
			startEnergy.setValue(getValue(centreEnergy)	- getValue(energyWidth) / 2.0);
			endEnergy.setValue(getValue(centreEnergy) + getValue(energyWidth) / 2.0);
			stepEnergy.setMinimum(determineMinimumStepEnergy(getSelectedPassEnergy()));
		}

		if ("startEnergy".equals(e.getFieldName())) {
			// If you change startEnergy must be in swept or dither therefore calculate centre and width
			centreEnergy.setValue((getValue(startEnergy) + getValue(endEnergy)) / 2.0);
			energyWidth.setValue(getValue(endEnergy) - getValue(startEnergy));
			updateNumberOfSteps();
		}

		if ("centreEnergy".equals(e.getFieldName())) {
			// If you change centreEnergy must be in fixed mode therefore calculate start and end
			startEnergy.setValue(getValue(centreEnergy) - getValue(energyWidth) / 2.0);
			endEnergy.setValue(getValue(centreEnergy) + getValue(energyWidth) / 2.0);
		}

		if ("endEnergy".equals(e.getFieldName())) {
			// If you change stopEnergy must be in swept or dither therefore calculate centre and width
			centreEnergy.setValue((getValue(startEnergy) + getValue(endEnergy)) / 2.0);
			energyWidth.setValue(getValue(endEnergy) - getValue(startEnergy));
			updateNumberOfSteps();
		}

		if ("stepEnergy".equals(e.getFieldName())) {
			updateNumberOfSteps();
		}

		updateEnergyLimits();
		updateAcquisitionMode();

		if ("acquisitionMode".equals(e.getFieldName())) {
			restoreCachedEnergyValues();
			updateNumberOfSteps();
		}

		updateEstimatedTime();
	}

	private double getValue(NumberBox numberBox) {
		return ((Number) numberBox.getValue()).doubleValue();
	}

	private Optional<Double> getOptionalValue(NumberBox numberBox) {
		Object value = numberBox.getValue();
		if (value != null) {
			return Optional.of(((Number) value).doubleValue());
		}
		return Optional.empty();
	}

	/**
	 * This handles adding and removing listeners and setting controls enabled and disabled depending on the selected mode.
	 */
	private void updateAcquisitionMode() {

		switch (getSelectedAcquisitionMode()) {
		case FIXED:
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
			stepEnergy.setMinimum(determineMinimumStepEnergy(getSelectedPassEnergy()));
			break;

		case SWEPT:
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
			break;

		case DITHER :
			startEnergy.setEditable(false);
			centreEnergy.setEditable(true);
			endEnergy.setEditable(false);
			stepEnergy.setEditable(false);
			startEnergy.removeValueListener(this);
			centreEnergy.addValueListener(this);
			endEnergy.removeValueListener(this);
			stepEnergy.removeValueListener(this);
			energyWidth.setValue(determineFixedModeEnergyWidth(getSelectedPassEnergy()));
			startEnergy.setValue(getValue(centreEnergy) - getValue(energyWidth) / 2.0);
			endEnergy.setValue(getValue(centreEnergy) + getValue(energyWidth) / 2.0);
			stepEnergy.setMinimum(determineMinimumStepEnergy(getSelectedPassEnergy()));
			break;

		default:
			// Nothing. This is here to satisfy the linter
		}
	}

	private void updateEnergyLimits() {
		String psuModeString = psuMode.getText();
		String lensModeString = lensModes[lensMode.getSelectionIndex()];
		int passEnergyInt = getSelectedPassEnergy();

		double min = 0;
		double max = maxKE;
		try {
			//ToDo - There needs to be a better way to do this as this method doesn't take into account that there could be
			//multiple energy limits. For now, just use the first {@code EnergyRange} found.
			min = energyRange.getMinKEs(psuModeString, lensModeString, passEnergyInt).get(0);
			max = energyRange.getMaxKEs(psuModeString, lensModeString, passEnergyInt).get(0);
		}
		catch (IllegalArgumentException e) {
			logger.error("Error calculating energy limits. Setting defualts min: {} max {}", min, max, e);
		}

		if (isSweptMode()) { // Swept mode
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

	private void cacheEnergyValues() {
		switch (lastSelectedAcquisitionMode) {
		case FIXED:
			cachedFixedModeCentreEnergy = getOptionalValue(centreEnergy);
			break;
		case SWEPT:
			cachedSweptModeStartEnergy = getOptionalValue(startEnergy);
			cachedSweptModeEndEnergy = getOptionalValue(endEnergy);
			break;
		case DITHER:
			cachedDitherModeCentreEnergy = getOptionalValue(centreEnergy);
			break;
		default:
			// No-op
		}

		lastSelectedAcquisitionMode = getSelectedAcquisitionMode();
	}

	private void restoreCachedEnergyValues() {
		switch (getSelectedAcquisitionMode()) {
		case FIXED:
			cachedFixedModeCentreEnergy.ifPresent(centreEnergy::setValue);
			break;
		case SWEPT:
			cachedSweptModeStartEnergy.ifPresent(startEnergy::setValue);
			cachedSweptModeEndEnergy.ifPresent(endEnergy::setValue);
			break;
		case DITHER:
			cachedDitherModeCentreEnergy.ifPresent(centreEnergy::setValue);
			break;
		default:
			// No-op
		}
	}

	private void updateNumberOfSteps() {

		if (getSelectedAcquisitionMode() == AcquisitionMode.SWEPT) {

			int numberofSteps = calculateNumberOfSteps();
			numberOfSteps.setText(String.valueOf(numberofSteps));

			if (numberofSteps > maxNumberOfSteps) {
				numberOfSteps.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
				numberOfSteps.setToolTipText(String.format("Number of steps is too high. Max is %d. Please change step size, start energy, or end energy", maxNumberOfSteps));
			} else {
				numberOfSteps.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
				numberOfSteps.setToolTipText("");
			}
		} else {
			numberOfSteps.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
			numberOfSteps.setText("N/A");
			numberOfSteps.setToolTipText("");
		}
	}

	private int calculateNumberOfSteps() {
		var width = getValue(endEnergy) - getValue(startEnergy);
		var step = getValue(stepEnergy) / 1000;
		return (int)Math.abs(Math.ceil(width / step));
	}

	@Override
	public String getValueListenerName() {
		return null;
	}

	public void beanUpdated() {
		// Centre energy is not saved in the XML so need to be calculated from start and stop
		centreEnergy.setValue((getValue(startEnergy) + getValue(endEnergy)) / 2.0);

		lastSelectedAcquisitionMode = getSelectedAcquisitionMode();
		updateAcquisitionMode();
		updateEnergyLimits();
		updateEstimatedTime();
		updateNumberOfSteps();
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
