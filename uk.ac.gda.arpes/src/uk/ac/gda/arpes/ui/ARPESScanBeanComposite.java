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
import uk.ac.gda.richbeans.ACTIVE_MODE;
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
	private final ComboWrapper lensMode;
	private final ComboWrapper passEnergy;
	private final NumberBox startEnergy;
	private final NumberBox endEnergy;
	private final NumberBox stepEnergy;
	private final NumberBox timePerStep;
	private final NumberBox iterations;
	private final RadioWrapper sweptMode;
	private final ScaleBox centreEnergy;
	private final ScaleBox energyWidth;
	private final BooleanWrapper configureOnly;
	private boolean wedidit = false;
	private AnalyserCapabilties capabilities;
	private Label lblEstimatedTime;
	private Label estimatedTime;
	private Label lblStartEnergy;
	private Label lblCenterEnergy;
	private Label lblEndEnergy;
	private Label lblStepEnergy;
	private Label lblEnergyWidth;
	private Label lblTimePerStep;
	private Label lblIterations;
	private Label lblConfigureOnly;
	private Label lblSweptMode;
	private Label lblPassEnergy;
	private Label lblLensMode;

	public ARPESScanBeanComposite(final Composite parent, int style, final RichBeanEditorPart editor) {
		super(parent, style);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.horizontalSpacing = 10;
		setLayout(gridLayout);

		capabilities = (AnalyserCapabilties) Finder.getInstance().listAllLocalObjects(AnalyserCapabilties.class.getCanonicalName()).get(0);

		Label label = new Label(this, SWT.NONE);
		label.setText("Drop file here!");
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		setDropTarget(parent, parent.getShell(), editor);

		Composite btnComp = new Composite(this, SWT.NONE);
		btnComp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));

		btnComp.setLayout(new GridLayout(2, false));

		Button btnClipboard = new Button(btnComp, SWT.NONE);
		btnClipboard.setText("Jython to Clipboard");
		btnClipboard.setToolTipText("save file and copy Jython instructions to clip board to use this defintion in scripts");
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
		btnQueueExperiment.setToolTipText("save file and queue for execution (will start immediately if queue running)");
		btnQueueExperiment.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				try {
					IProgressMonitor monitor = new NullProgressMonitor();
					editor.doSave(monitor);
					if (monitor.isCanceled())
						return;
					Queue queue = CommandQueueViewFactory.getQueue();
					boolean batonHeld = JythonServerFacade.getInstance().isBatonHeld();
					if(!batonHeld){
						MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(), "Baton not held", null,
							    "You do not hold the baton, please take the baton using the baton manager.", MessageDialog.ERROR, new String[] { "Ok" }, 0);
						dialog.open();
					}
					else if (queue != null) {
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

		lblLensMode = new Label(this, SWT.NONE);
		lblLensMode.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblLensMode.setText("Lens Mode");
		lensMode = new ComboWrapper(this, SWT.NONE);
		GridData gd_lensMode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lensMode.widthHint = 200;
		lensMode.setLayoutData(gd_lensMode);
		lensMode.setItems(capabilities.getLensModes());

		Comparator<String> passEComparator = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return Integer.valueOf(o1.substring(0, o1.lastIndexOf(" "))).compareTo(
						Integer.valueOf(o2.substring(0, o2.lastIndexOf(" "))));
			}
		};
		Map<String, Short> passMap = new TreeMap<String, Short>(passEComparator);
		for (short s : capabilities.getPassEnergies()) {
			passMap.put(String.format("%d eV", s), s);
		}
		lblPassEnergy = new Label(this, SWT.NONE);
		lblPassEnergy.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPassEnergy.setText("Pass Energy");
		passEnergy = new ComboWrapper(this, SWT.NONE);
		GridData gd_passEnergy = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_passEnergy.widthHint = 200;
		passEnergy.setLayoutData(gd_passEnergy);
		passEnergy.setItems(passMap);
		passEnergy.addValueListener(this);

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

		lblEstimatedTime = new Label(this, SWT.NONE);
		lblEstimatedTime.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		lblEstimatedTime.setText("Estimated Time");

		estimatedTime = new Label(this, SWT.NONE);
		GridData gd_estimatedTime = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_estimatedTime.widthHint = 200;
		estimatedTime.setLayoutData(gd_estimatedTime);

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
		startEnergy.addValueListener(this);

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
		centreEnergy.addValueListener(this);

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
		endEnergy.addValueListener(this);

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
		stepEnergy.setMinimum(0.0001);
		stepEnergy.setMinimumValid(true);
		stepEnergy.addValueListener(this);

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
		energyWidth.on();
		energyWidth.setActiveMode(ACTIVE_MODE.SET_ENABLED_AND_ACTIVE);
		energyWidth.addValueListener(this);

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

		lblConfigureOnly = new Label(this, SWT.NONE);
		lblConfigureOnly.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblConfigureOnly.setText("Configure Only");
		configureOnly = new BooleanWrapper(this, SWT.NONE);
		configureOnly.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	}

	//DetEnN is number of detector point in swept mode and could be calculated from sweptModeRegion, currently DetEnN=(905-55)
	//DetEnStep is a minimum energy step per pixel and is a function of a pass Energy,
	private double determineMinimmumStepEnergy(double passEnergy){
		if(passEnergy==20)
			return 1.6119;
		else if(passEnergy==10)
			return 0.80595;
		else if(passEnergy==5)
			return 0.402975;
		return -1;
	}

	private void updateEstimatedTime(){
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				int numberOfIterations= iterations.getIntegerValue();
				double stepTime = timePerStep.getNumericValue();
				double startEnergyVal = startEnergy.getNumericValue();
				double endEnergyVal = endEnergy.getNumericValue();
				double stepEnergyVal = stepEnergy.getNumericValue();
				double passEnergyVal = Double.parseDouble(passEnergy.getValue().toString());
				sweptMode.getValue().toString();
				boolean isSweptMode = (Boolean) sweptMode.getValue();
				long estimatedTimeMs = 0L;
				if(isSweptMode)
					estimatedTimeMs = calculateSweptTime(stepTime, startEnergyVal, endEnergyVal, stepEnergyVal, passEnergyVal) * numberOfIterations;
				else{
					double energyRange = endEnergyVal - startEnergyVal;
					double numberOfSteps = energyRange/stepEnergyVal;
					estimatedTimeMs = (long) (numberOfSteps * (stepTime*1000)) * numberOfIterations;
				}
				String time = msToString(estimatedTimeMs);
				estimatedTime.setText(time + " (hh:mm:ss)");
			}
		});
	}

    public String msToString(long ms) {
        long totalSecs = ms/1000;
        long hours = (totalSecs / 3600);
        long mins = (totalSecs / 60) % 60;
        long secs = totalSecs % 60;
        String minsString = (mins == 0)
            ? "00"
            : ((mins < 10)
               ? "0" + mins
               : "" + mins);
        String secsString = (secs == 0)
            ? "00"
            : ((secs < 10)
               ? "0" + secs
               : "" + secs);
        return hours + ":" + minsString + ":" + secsString;
    }

	private long calculateSweptTime(double stepTime, double startEn, double endEn, double stepEnergyVal, double passEnergyVal){
		double minStepEnergyVal = determineMinimmumStepEnergy(passEnergyVal);
		//DetEnN is number of detectort point in swept mode and could be calculated from sweptModeRegion, currently DetEnN=(905-55)
		double numberOfDetectorPoints = 905-55;
		long sweptTime = (long)((stepTime*(numberOfDetectorPoints*minStepEnergyVal/1000+Math.abs(startEn-endEn))*1000/stepEnergyVal)*1000);
		return sweptTime;
	}

	protected String getOurJythonCommand(final RichBeanEditorPart editor) {
		return String.format("import arpes; arpes.APRESRun(\"%s\").run()", editor.getPath());
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
		if (Double.isNaN(e.getDoubleValue()))
			return;
		if (wedidit)
			return;
		wedidit = true;

		try {
			if (e.getFieldName().equals("sweptMode")) {
				stepEnergy.setMinimum(capabilities.getEnergyStepForPass(((Number) passEnergy.getValue()).intValue()));
				if (!isSwept()) {
					stepEnergy.setValue(capabilities.getEnergyStepForPass(((Number) passEnergy.getValue()).intValue()));
					stepEnergy.setEditable(false);
					energyWidth.setValue(capabilities.getEnergyWidthForPass(((Number) passEnergy.getValue()).intValue()));
					energyWidth.setActive(false);
					startEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue() - ((Number) energyWidth.getValue()).doubleValue() / 2.0);
					endEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue() + ((Number) energyWidth.getValue()).doubleValue() / 2.0);

				} else {
					stepEnergy.setEditable(true);
					energyWidth.setActive(true);
				}
			}

			if (e.getFieldName().equals("passEnergy") || (e.getFieldName().equals("sweptMode") && (Boolean) e.getValue())) {
				stepEnergy.setMinimum(capabilities.getEnergyStepForPass(((Number) passEnergy.getValue()).intValue()));
				if (!isSwept()) {
					stepEnergy.setValue(capabilities.getEnergyStepForPass(((Number) passEnergy.getValue()).intValue()));
					double width = capabilities.getEnergyWidthForPass(((Number) passEnergy.getValue()).intValue());
					energyWidth.setValue(width);
					startEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue() - width / 2.0);
					endEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue() + width / 2.0);
				}
			}

			if (isSwept()) {
				if (e.getFieldName().equals("startEnergy")) {
					centreEnergy.setValue((((Number) endEnergy.getValue()).doubleValue() + e.getDoubleValue()) / 2.0);
					energyWidth.setValue(((Number) endEnergy.getValue()).doubleValue() - e.getDoubleValue());
				}
				if (e.getFieldName().equals("endEnergy")) {
					centreEnergy.setValue((((Number) startEnergy.getValue()).doubleValue() + e.getDoubleValue()) / 2.0);
					energyWidth.setValue(-1 * ((Number) startEnergy.getValue()).doubleValue() + e.getDoubleValue());
				}
				if (e.getFieldName().equals("energyWidth")) {
					startEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue() - e.getDoubleValue() / 2.0);
					endEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue() + e.getDoubleValue() / 2.0);
				}
			}
			else {
				if (e.getFieldName().equals("startEnergy")) {
					centreEnergy.setValue(((Number) energyWidth.getValue()).doubleValue() / 2.0 + e.getDoubleValue());
					endEnergy.setValue(((Number) energyWidth.getValue()).doubleValue() + e.getDoubleValue());
				}
				if (e.getFieldName().equals("endEnergy")) {
					centreEnergy.setValue(e.getDoubleValue() - ((Number) energyWidth.getValue()).doubleValue() / 2.0);
					startEnergy.setValue(e.getDoubleValue() - ((Number) energyWidth.getValue()).doubleValue());
				}
			}

			if (e.getFieldName().equals("centreEnergy")) {
				startEnergy.setValue(e.getDoubleValue() - ((Number) energyWidth.getValue()).doubleValue() / 2.0);
				endEnergy.setValue(e.getDoubleValue() + ((Number) energyWidth.getValue()).doubleValue() / 2.0);
			}

		} finally {
			wedidit = false;
		}
		updateEstimatedTime();
		updateEnergyLimits();
	}

	private void updateEnergyLimits(){
		int lens = lensMode.getSelectionIndex();
		int passEnergyVal = Integer.parseInt(passEnergy.getValue().toString());
		int[] lowPassEnergyRange = getLowPassEnergyRange(lens, passEnergyVal);
		int min = lowPassEnergyRange[0];
		int max = lowPassEnergyRange[1];
		if(min!=-1 && max!=-1){
			startEnergy.setEnabled(true);
			centreEnergy.setEnabled(true);
			endEnergy.setEnabled(true);
			startEnergy.setMinimum(min);
			startEnergy.setMaximum(max);
			centreEnergy.setMinimum(min);
			centreEnergy.setMaximum(max);
			endEnergy.setMinimum(min);
			endEnergy.setMaximum(max);
		}
		else{
			startEnergy.setEnabled(false);
			centreEnergy.setEnabled(false);
			endEnergy.setEnabled(false);
		}
	}

	@Override
	public String getValueListenerName() {
		return null;
	}

	public void beanUpdated() {
		wedidit = true;
		try {
			stepEnergy.setMinimum(capabilities.getEnergyStepForPass(((Number) passEnergy.getValue()).intValue()));
			centreEnergy.setValue((((Number) endEnergy.getValue()).doubleValue() + ((Number) startEnergy.getValue()).doubleValue()) / 2.0);
			if (!isSwept()) {
				stepEnergy.setValue(capabilities.getEnergyStepForPass(((Number) passEnergy.getValue()).intValue()));
				stepEnergy.setEditable(false);
				energyWidth.setActive(false);
				energyWidth.setValue(capabilities.getEnergyWidthForPass(((Number) passEnergy.getValue()).intValue()));
				startEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue() - ((Number) energyWidth.getValue()).doubleValue() / 2.0);
				endEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue() + ((Number) energyWidth.getValue()).doubleValue() / 2.0);
			}
			else {
				stepEnergy.setEditable(true);
				energyWidth.setActive(true);
				energyWidth.setValue(((Number) endEnergy.getValue()).doubleValue() - ((Number) startEnergy.getValue()).doubleValue());
			}
		} finally {
			wedidit = false;
		}
		updateEstimatedTime();
	}

	private int determinePassEnergyIndex(int passEnergy){
		int passEnergyIndex = 0;
		switch (passEnergy) {
			case 1:	passEnergyIndex = 0;
					break;
			case 2:	passEnergyIndex = 1;
					break;
			case 5:	passEnergyIndex = 2;
					break;
			case 10:passEnergyIndex = 3;
					break;
			case 20:passEnergyIndex = 4;
					break;
			case 50:passEnergyIndex = 5;
					break;
			case 100:passEnergyIndex = 6;
					break;
		}
		return passEnergyIndex;
	}

	//-1 represents none
	//index 0,1,2,3,4,5,6 relates to pass energies 1,2,5,10,20,50,100
	//lens modes:	0:transmission
	//				1:angular7NF
	//				2:angular14
	//				3:angular14SmallSpot
	//				4:angular30
	//				5:angular30SmallSpot

	private int[] getLowPassEnergyRange(int lens, int passEnergy){
		int[] minEnergies = new int[7];
		int[] maxEnegies = new int[7];
		switch (lens) {
    		case 0: //trans correct
    			minEnergies = new int[]{1,0,1,2,5,-1,-1};
    			maxEnegies = new int[]{32,64,116,121,131,-1,-1};
                        break;
    		case 1: //ang 7 correct
    			minEnergies = new int[]{1,1,3,12,12,-1,-1};
    			maxEnegies = new int[]{38,76,116,121,131,-1,-1};
    			break;
    		case 2: //ang 14 correct
    			minEnergies = new int[]{1,1,2,5,10,-1,-1};
    			maxEnegies = new int[]{38,76,116,121,131,-1,-1};
			break;
    		case 3: // ang 14 small correct
    			minEnergies = new int[]{1,1,1,1,1,-1,-1};
    			maxEnegies = new int[]{8,24,60,120,88,-1,-1};
    			break;
    		case 4: // ang 30 correct
    			minEnergies = new int[]{1,1,2,4,7,-1,-1};
    			maxEnegies = new int[]{23,45,113,121,131,-1,-1};
    			break;
    		case 5: // ang 30 small correct
    			minEnergies = new int[]{1,1,1,1,1,-1,-1};
    			maxEnegies = new int[]{8,24,60,120,88,-1,-1};
			break;
		}
		int passEnergyIndex = determinePassEnergyIndex(passEnergy);
		return new int[]{minEnergies[passEnergyIndex],maxEnegies[passEnergyIndex]};
	}

	//-1 represents none
	//index 0,1,2,3,4,5,6 relates to pass energies 1,2,5,10,20,50,100

	//lens modes:	0:transmission
	//				1:angular7NF
	//				2:angular14
	//				3:angular14SmallSpot
	//				4:angular30
	//				5:angular30SmallSpot

	private int[] getHighPassEnergyRange(int lens, int passEnergy){
		int[] minEnergies = new int[7];
		int[] maxEnegies = new int[7];
		switch (lens) {
    		case 0: //transmission correct
    			minEnergies = new int[]{-1,-1,1,2,5,12,25};
			maxEnegies = new int[]{-1,-1,160,320,640,1407,1305};
			break;
    		case 1: //ang 7 correct
    			minEnergies = new int[]{-1,-1,3,12,12,30,59};
    			maxEnegies = new int[]{-1,-1,190,761,761,369,216};
    			break;
    		case 2: //ang 14 correct
    			minEnergies = new int[]{-1,-1,2,5,10,25,50};
			maxEnegies = new int[]{-1,-1,190,381,761,1234,1467};
			break;
    		case 3: //ang 14 small correct
    			minEnergies = new int[]{-1,-1,1,2,2,6,12};
    			maxEnegies = new int[]{-1,-1,48,95,190,182,149};
    			break;
    		case 4: //ang 30 correct
    			minEnergies = new int[]{-1,-1,2,4,7,18,35};
    			maxEnegies = new int[]{-1,-1,113,226,453,800,1037};
    			break;
    		case 5: //ang 30 small correct
    			minEnergies = new int[]{-1,-1,1,1,1,3,6};
			maxEnegies = new int[]{-1,-1,60,120,240,248,172};
			break;
		}
		int passEnergyIndex = determinePassEnergyIndex(passEnergy);
		return new int[]{minEnergies[passEnergyIndex],maxEnegies[passEnergyIndex]};
	}

	public static void setDropTarget(final Composite parent, final Shell shell, final RichBeanEditorPart editor) {
		int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
		DropTarget target = new DropTarget(parent, operations);
		target.setTransfer(new Transfer[] { FileTransfer.getInstance() });
		target.addDropListener(new DropTargetAdapter() {
			@Override
			public void dragEnter(DropTargetEvent e) {
				if (e.detail == DND.DROP_NONE)
					e.detail = DND.DROP_LINK;
			}

			@Override
			public void dragOperationChanged(DropTargetEvent e) {
				if (e.detail == DND.DROP_NONE)
					e.detail = DND.DROP_LINK;
			}

			@Override
			public void drop(DropTargetEvent event) {
				if (event.data == null) {
					event.detail = DND.DROP_NONE;
					return;
				}
				String[] filenames = (String[]) event.data;
				if (filenames.length > 1) {
					MessageDialog.openError(shell, "too many files", "Please drop one file only in here.\nI cannot copy settings from multiple sources.");
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
					MessageDialog.openError(shell, "error reading nexus file for settings", "Analyser settings from that file could not be read.");
					return;
				}
				// TODO message for non-analyser parameters (exit slit, entrance slit, photon energy)
				// TODO deal with multi-dim files

				MessageDialog dialog = new MessageDialog(shell, "Save imported settings", null,
						"We would suggest saving this experiment under a new name now.", MessageDialog.QUESTION,
						new String[] { "Save as...", "Keep existing name and don't save yet" }, 0);
				int result = dialog.open();
				if (result == 0)
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().doSaveAs();
			}
		});
	}
}