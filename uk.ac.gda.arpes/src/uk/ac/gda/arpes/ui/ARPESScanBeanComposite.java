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

import gda.commandqueue.JythonCommandCommandProvider;
import gda.commandqueue.Queue;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
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

import uk.ac.gda.arpes.beans.ARPESScanBean;
import uk.ac.gda.arpes.beans.ScanBeanFromNeXusFile;
import uk.ac.gda.client.CommandQueueViewFactory;
import uk.ac.gda.devices.vgscienta.AnalyserCapabilties;
import uk.ac.gda.richbeans.beans.IFieldWidget;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.components.scalebox.IntegerBox;
import uk.ac.gda.richbeans.components.scalebox.NumberBox;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.BooleanWrapper;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
import uk.ac.gda.richbeans.components.wrappers.RadioWrapper;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;
import uk.ac.gda.richbeans.event.ValueEvent;
import uk.ac.gda.richbeans.event.ValueListener;

public final class ARPESScanBeanComposite extends Composite implements ValueListener {
	private static final Logger logger = LoggerFactory.getLogger(ARPESScanBeanComposite.class);
	
	private AnalyserCapabilties capabilities;
	private Label lblpsuMode;
	private Label psuMode;
	private Label lblLensMode;
	private final ComboWrapper lensMode;
	private Label lblPassEnergy;
	private final ComboWrapper passEnergy;
	private Label lblStartEnergy;
	private final NumberBox startEnergy;
	private Label lblEndEnergy;
	private final NumberBox endEnergy;
	private Label lblStepEnergy;
	private final NumberBox stepEnergy;
	private Label lblTimePerStep;
	private final NumberBox timePerStep;
	private Label lblIterations;
	private final NumberBox iterations;
	private Label lblSweptMode;
	private final RadioWrapper sweptMode;
	private Label lblCenterEnergy;
	private final NumberBox centreEnergy;
	private Label lblEnergyWidth;
	private final NumberBox energyWidth;
	private Label lblConfigureOnly;
	private final BooleanWrapper configureOnly;
	
	private Label lblEstimatedTime;	
	private Label estimatedTime;


	public ARPESScanBeanComposite(final Composite parent, int style, final RichBeanEditorPart editor) {
		super(parent, style);

		// Load the analyser capabilities
		capabilities = (AnalyserCapabilties) Finder.getInstance()
				.listAllLocalObjects(AnalyserCapabilties.class.getCanonicalName()).get(0);

		// Make a 2 column grid layout
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.horizontalSpacing = 10;
		gridLayout.verticalSpacing = 6;
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
		btnQueueExperiment.setText("Queue Experiment");
		btnQueueExperiment
				.setToolTipText("Save file and queue for execution (will start immediately if queue running)");
		btnQueueExperiment.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				try {
					IProgressMonitor monitor = new NullProgressMonitor();
					editor.doSave(monitor);
					if (monitor.isCanceled()) {
						return;
					}
					Queue queue = CommandQueueViewFactory.getQueue();
					boolean batonHeld = JythonServerFacade.getInstance().isBatonHeld();
					if (!batonHeld) {
						MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(),
								"Baton not held", null,
								"You do not hold the baton, please take the baton using the baton manager.",
								MessageDialog.ERROR, new String[] { "Ok" }, 0);
						dialog.open();
					} else if (queue != null) {
						queue.addToTail(new JythonCommandCommandProvider(getOurJythonCommand(editor),
								editor.getTitle(), editor.getPath()));
					} else {
						logger.warn("No queue received from CommandQueueViewFactory");
					}
				} catch (Exception e1) {
					logger.error("Error adding command to the queue", e1);
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
		updatePsuMode(); // Call to get current PSU mode as panel is building

		// Lens Mode
		lblLensMode = new Label(this, SWT.NONE);
		lblLensMode.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblLensMode.setText("Lens Mode");
		lensMode = new ComboWrapper(this, SWT.NONE);
		GridData gd_lensMode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lensMode.widthHint = 200;
		lensMode.setLayoutData(gd_lensMode);
		lensMode.setItems(capabilities.getLensModes());
		lensMode.addValueListener(this);

		// Pass energy
		lblPassEnergy = new Label(this, SWT.NONE);
		lblPassEnergy.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPassEnergy.setText("Pass Energy");
		passEnergy = new ComboWrapper(this, SWT.NONE);
		GridData gd_passEnergy = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_passEnergy.widthHint = 200;
		passEnergy.setLayoutData(gd_passEnergy);
		updatePassEnergy();
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
		lblCenterEnergy.setText("Center Energy");
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
	}

	private void updatePsuMode() {
		psuMode.setText(InterfaceProvider.getCommandRunner().evaluateCommand("psu_mode()"));

	}

	// Sets the available pass energies from the analyser capabilities + PSU mode
	private void updatePassEnergy() {
		Comparator<String> passEComparator = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return Integer.valueOf(o1.substring(0, o1.lastIndexOf(" "))).compareTo(Integer.valueOf(o2.substring(0, o2.lastIndexOf(" "))));
			}
		};
		Map<String, Short> passMap = new TreeMap<String, Short>(passEComparator);
		for (short s : capabilities.getPassEnergies()) {
			passMap.put(String.format("%d eV", s), s);
		}
		// Remove Pass energies from drop down not possible with current psu_mode
		updatePsuMode();
		if (psuMode.equals("High Pass (XPS)")) {
			passMap.remove("1 eV"); // PE=1 eV not possible in High Pass
			passMap.remove("2 eV"); // PE=2 eV not possible in High Pass
		}
		if (psuMode.equals("Low Pass (UPS)")) {
			passMap.remove("50 eV"); // PE=50 eV not possible in Low Pass
			passMap.remove("100 eV"); // PE=100 eV not possible in Low Pass
		}
		passEnergy.setItems(passMap);
	}

	// DetEnN is number of detector point in swept mode and could be calculated from sweptModeRegion, currently
	// DetEnN=(905-55)
	// DetEnStep is a minimum energy step per pixel and is a function of a pass Energy,
	private double determineMinimmumStepEnergy(double passEnergy) {
		// This casts passEnergy to an int might be a problem for non-int passEnergies
		return capabilities.getEnergyStepForPass(determinePassEnergyIndex((int) (passEnergy)));
	}

	private void updateMinimumStepEnergy() {
		double passEnergyVal = Double.parseDouble(passEnergy.getValue().toString());
		stepEnergy.setMinimum(determineMinimmumStepEnergy(passEnergyVal));
	}

	// This calculates the acquisition time excluding dead time (i.e. shortest possible)
	private void updateEstimatedTime() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {

				long estimatedTimeMs = 0L;
				int numberOfIterations = iterations.getIntegerValue();
				double stepTime = timePerStep.getNumericValue();
				boolean isSweptMode = (Boolean) sweptMode.getValue();

				if (isSweptMode) {
					double startEnergyVal = startEnergy.getNumericValue();
					double endEnergyVal = endEnergy.getNumericValue();
					double stepEnergyVal = stepEnergy.getNumericValue();
					double passEnergyVal = Double.parseDouble(passEnergy.getValue().toString());
					estimatedTimeMs = calculateSweptTime(stepTime, startEnergyVal, endEnergyVal, stepEnergyVal,	passEnergyVal) * numberOfIterations;
				} else { // Fixed mode
					estimatedTimeMs = (long) (stepTime * numberOfIterations * 1000);
				}

				String time = msToString(estimatedTimeMs);
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

	private long calculateSweptTime(double stepTime, double startEn, double endEn, double stepEnergyVal,
			double passEnergyVal) {
		double minStepEnergyVal = determineMinimmumStepEnergy(passEnergyVal);
		// DetEnN is number of detector point in swept mode and could be calculated from sweptModeRegion, currently
		// DetEnN=(905-55)
		double numberOfDetectorPoints = 905 - 55; // This is hard coded, not good it specifies the energy points from the swept ROI
		long sweptTime = (long) ((stepTime * (numberOfDetectorPoints * minStepEnergyVal / 1000 + Math.abs(startEn - endEn)) * 1000 / stepEnergyVal) * 1000);
		return sweptTime;
	}

	protected String getOurJythonCommand(final RichBeanEditorPart editor) {
		return String.format("import arpes; arpes.ARPESRun(\"%s\").run()", editor.getPath());
	}

	public FieldComposite getLensMode() {
		return lensMode;
	}

	public FieldComposite getPassEnergy() {
		return passEnergy;
	}

	public FieldComposite getStartEnergy() {
		return startEnergy;
	}

	public FieldComposite getEndEnergy() {
		return endEnergy;
	}

	public FieldComposite getStepEnergy() {
		return stepEnergy;
	}

	public FieldComposite getTimePerStep() {
		return timePerStep;
	}

	public FieldComposite getIterations() {
		return iterations;
	}

	public FieldComposite getConfigureOnly() {
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
				stepEnergy.setValue(capabilities.getEnergyStepForPass(((Number) passEnergy.getValue()).intValue()));
				energyWidth.setValue(capabilities.getEnergyWidthForPass(((Number) passEnergy.getValue()).intValue()));
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
				energyWidth.setValue(capabilities.getEnergyWidthForPass(((Number) passEnergy.getValue()).intValue()));
				startEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue()
						- ((Number) energyWidth.getValue()).doubleValue() / 2.0);
				endEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue()
						+ ((Number) energyWidth.getValue()).doubleValue() / 2.0);
				stepEnergy.setValue(capabilities.getEnergyStepForPass(((Number) passEnergy.getValue()).intValue()));
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
		int lens = lensMode.getSelectionIndex();
		int passEnergyVal = Integer.parseInt(passEnergy.getValue().toString());

		//String psu_mode = InterfaceProvider.getCommandRunner().evaluateCommand("psu_mode()");
		String psu_mode = psuMode.getText();
		int[] energyRange;
		if (psu_mode.equals("High Pass (XPS)")) {
			energyRange = getHighPassEnergyRange(lens, passEnergyVal);
		} else { // Low Pass (UPS) or unknown!
			energyRange = getLowPassEnergyRange(lens, passEnergyVal);
		}
		
		int min = energyRange[0];
		int max = energyRange[1];

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
			energyWidth.setValue(capabilities.getEnergyWidthForPass(((Number) passEnergy.getValue()).intValue()));
			stepEnergy.setValue(capabilities.getEnergyStepForPass(((Number) passEnergy.getValue()).intValue()));

		}
		updatePsuMode();
		updateEnergyLimits();
		updateEstimatedTime();
	}

	private int determinePassEnergyIndex(int passEnergy) {
		int passEnergyIndex = 0;
		switch (passEnergy) {
		case 1:
			passEnergyIndex = 0;
			break;
		case 2:
			passEnergyIndex = 1;
			break;
		case 5:
			passEnergyIndex = 2;
			break;
		case 10:
			passEnergyIndex = 3;
			break;
		case 20:
			passEnergyIndex = 4;
			break;
		case 50:
			passEnergyIndex = 5;
			break;
		case 100:
			passEnergyIndex = 6;
			break;
		}
		return passEnergyIndex;
	}

	// -1 represents none
	// index 0,1,2,3,4,5,6 relates to pass energies 1,2,5,10,20,50,100
	// lens modes: 0:transmission
	// 1:angular7NF
	// 2:angular14
	// 3:angular14SmallSpot
	// 4:angular30
	// 5:angular30SmallSpot

	private int[] getLowPassEnergyRange(int lens, int passEnergy) {
		int[] minEnergies = new int[7];
		int[] maxEnegies = new int[7];
		switch (lens) {
		case 0: // trans correct 23-2-15 JJM
			minEnergies = new int[] { 1, 0, 1, 2, 5, -1, -1 };
			maxEnegies = new int[] { 32, 64, 116, 121, 131, -1, -1 };
			break;
		case 1: // ang 7 correct 23-2-15 JJM
			minEnergies = new int[] { 1, 1, 3, 12, 12, -1, -1 };
			maxEnegies = new int[] { 38, 76, 116, 121, 131, -1, -1 };
			break;
		case 2: // ang 14 correct 23-2-15 JJM
			minEnergies = new int[] { 1, 1, 2, 5, 10, -1, -1 };
			maxEnegies = new int[] { 38, 76, 116, 121, 131, -1, -1 };
			break;
		case 3: // ang 14 small correct 23-2-15 JJM
			minEnergies = new int[] { 1, 1, 1, 2, 2, -1, -1 };
			maxEnegies = new int[] { 10, 19, 48, 95, 67, -1, -1 };
			break;
		case 4: // ang 30 correct 23-2-15 JJM
			minEnergies = new int[] { 1, 1, 2, 4, 7, -1, -1 };
			maxEnegies = new int[] { 23, 45, 113, 121, 131, -1, -1 };
			break;
		case 5: // ang 30 small correct 23-2-15 JJM
			minEnergies = new int[] { 1, 1, 1, 1, 1, -1, -1 };
			maxEnegies = new int[] { 8, 24, 60, 120, 88, -1, -1 };
			break;
		}
		int passEnergyIndex = determinePassEnergyIndex(passEnergy);
		return new int[] { minEnergies[passEnergyIndex], maxEnegies[passEnergyIndex] };
	}

	// -1 represents none
	// index 0,1,2,3,4,5,6 relates to pass energies 1,2,5,10,20,50,100

	// lens modes: 0:transmission
	// 1:angular7NF
	// 2:angular14
	// 3:angular14SmallSpot
	// 4:angular30
	// 5:angular30SmallSpot

	private int[] getHighPassEnergyRange(int lens, int passEnergy) {
		int[] minEnergies = new int[7];
		int[] maxEnegies = new int[7];
		switch (lens) {
		case 0: // transmission correct 23-2-15 JJM
			minEnergies = new int[] { -1, -1, 1, 2, 5, 12, 25 };
			maxEnegies = new int[] { -1, -1, 160, 320, 640, 1407, 1305 };
			break;
		case 1: // ang 7 correct 23-2-15 JJM
			minEnergies = new int[] { -1, -1, 3, 12, 12, 30, 59 };
			maxEnegies = new int[] { -1, -1, 190, 761, 761, 369, 216 };
			break;
		case 2: // ang 14 correct 23-2-15 JJM
			minEnergies = new int[] { -1, -1, 2, 5, 10, 25, 50 };
			maxEnegies = new int[] { -1, -1, 190, 381, 761, 1234, 1467 };
			break;
		case 3: // ang 14 small correct 23-2-15 JJM
			minEnergies = new int[] { -1, -1, 1, 2, 2, 6, 12 };
			maxEnegies = new int[] { -1, -1, 48, 95, 190, 182, 149 };
			break;
		case 4: // ang 30 correct 23-2-15 JJM
			minEnergies = new int[] { -1, -1, 2, 4, 7, 18, 35 };
			maxEnegies = new int[] { -1, -1, 113, 226, 453, 800, 1037 };
			break;
		case 5: // ang 30 small correct 23-2-15 JJM
			minEnergies = new int[] { -1, -1, 1, 1, 1, 3, 6 };
			maxEnegies = new int[] { -1, -1, 60, 120, 240, 248, 172 };
			break;
		}
		int passEnergyIndex = determinePassEnergyIndex(passEnergy);
		return new int[] { minEnergies[passEnergyIndex], maxEnegies[passEnergyIndex] };
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