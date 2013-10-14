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

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
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

	private AnalyserCapabilties capabilities;

	public ARPESScanBeanComposite(final Composite parent, int style, final RichBeanEditorPart editor) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		capabilities = (AnalyserCapabilties) Finder.getInstance().listAllLocalObjects(AnalyserCapabilties.class.getCanonicalName()).get(0);
		
		Label label = new Label(this, SWT.NONE);
		label.setText("Drop file here!");
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		
		setDropTarget(parent, parent.getShell(), editor);
		
		Button btnQueueExperiment = new Button(this, SWT.NONE);
		GridData layoutData = new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1);
		btnQueueExperiment.setLayoutData(layoutData);
		btnQueueExperiment.setText("Queue Experiment");
		btnQueueExperiment.setToolTipText("save file and queue for execution (will start immediately if queue running");
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
					if (queue != null) {
						queue.addToTail(new JythonCommandCommandProvider(String.format("import arpes; arpes.APRESRun(\"%s\").run()", editor.getPath()), editor.getTitle(), editor.getPath()));
					} else {
						logger.warn("No queue received from CommandQueueViewFactory");
					}
				} catch (Exception e1) {
					logger.error("Error adding command to the queue", e1);
				}
			}
		});

		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("lensMode");
		this.lensMode = new ComboWrapper(this, SWT.NONE);
		this.lensMode.setItems(capabilities.getLensModes());
		lensMode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Comparator<String> passEComparator = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return Integer.valueOf(o1.substring(0, o1.lastIndexOf(" "))).compareTo(Integer.valueOf(o2.substring(0, o2.lastIndexOf(" "))));
			}
		};
		Map<String, Short> passMap = 	new TreeMap<String, Short>(passEComparator);
		for (short s: capabilities.getPassEnergies()) {
			passMap.put(String.format("%d eV", s), s);
		}
		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("passEnergy");
		this.passEnergy = new ComboWrapper(this, SWT.NONE);
		this.passEnergy.setItems(passMap);
		this.passEnergy.addValueListener(this);
		passEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("sweptMode");
		this.sweptMode = new RadioWrapper(this, SWT.NONE, new String[] { "fixed", "swept"}) {
			@Override
			public void setValue(Object value) {
				super.setValue((Boolean) value ? "swept" : "fixed");
			}
			@Override
			public Object getValue() {
				return super.getValue().equals("swept");
			}
		};
		sweptMode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		sweptMode.addValueListener(this);
		
		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("startEnergy");
		this.startEnergy = new ScaleBox(this, SWT.NONE);
		startEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		startEnergy.setUnit("eV");
		startEnergy.setDecimalPlaces(3);
		startEnergy.addValueListener(this);
		
		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("centreEnergy");
		centreEnergy = new ScaleBox(this, SWT.NONE);
		centreEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		centreEnergy.setUnit("eV");
		centreEnergy.setDecimalPlaces(3);
		centreEnergy.setFieldName("centreEnergy");
		centreEnergy.on();
		centreEnergy.addValueListener(this);
		
		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("endEnergy");
		this.endEnergy = new ScaleBox(this, SWT.NONE);
		endEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		endEnergy.setUnit("eV");
		endEnergy.setDecimalPlaces(3);
		endEnergy.addValueListener(this);
		
		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("stepEnergy");
		this.stepEnergy = new ScaleBox(this, SWT.NONE);
		stepEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		stepEnergy.setUnit("meV");
		stepEnergy.setDecimalPlaces(5);
		stepEnergy.setMaximum(10000);
		stepEnergy.setMinimum(0.0001);
		stepEnergy.setMinimumValid(true);
		stepEnergy.addValueListener(this);
		
		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("energyWidth");
		energyWidth = new ScaleBox(this, SWT.NONE);
		energyWidth.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		energyWidth.setUnit("eV");
		energyWidth.setDecimalPlaces(3);
		energyWidth.setFieldName("energyWidth");
		energyWidth.on();
		energyWidth.setActiveMode(ACTIVE_MODE.SET_ENABLED_AND_ACTIVE);
		energyWidth.addValueListener(this);
		
		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("timePerStep");
		this.timePerStep = new ScaleBox(this, SWT.NONE);
		timePerStep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		timePerStep.setUnit("s");
		timePerStep.addValueListener(this);
		
		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("iterations");
		this.iterations = new IntegerBox(this, SWT.NONE);
		iterations.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		iterations.addValueListener(this);
		
		label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		label.setText("configureOnly");
		this.configureOnly = new BooleanWrapper(this, SWT.NONE);
		configureOnly.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
//		configureOnly.addValueListener(this);
		configureOnly.setToolTipText("Do not run experiment, just set up analyser accordingly.");
		
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

	boolean wedidit = false;
	
	private boolean isSwept() {
		return (Boolean) sweptMode.getValue();
	}
	
	@Override
	public void valueChangePerformed(ValueEvent e) {
	
		if (Double.isNaN(e.getDoubleValue())) 
			return; 
		
		if (wedidit) return;
		
		wedidit = true;
		
		try {
			if (e.getFieldName().equals("sweptMode")) {
				stepEnergy.setMinimum(capabilities.getEnergyStepForPass(((Number) passEnergy.getValue()).intValue()));
				if (!isSwept()) {
					stepEnergy.setValue(capabilities.getEnergyStepForPass(((Number) passEnergy.getValue()).intValue()));
					stepEnergy.setEditable(false);
					energyWidth.setValue(capabilities.getEnergyWidthForPass(((Number) passEnergy.getValue()).intValue()));
					energyWidth.setActive(false);
					startEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue() - ((Number) energyWidth.getValue()).doubleValue()/2.0);
					endEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue() + ((Number) energyWidth.getValue()).doubleValue()/2.0);

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
					startEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue() - width/2.0);
					endEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue() + width/2.0);
				}
			}
		
			if (isSwept()) {
				if (e.getFieldName().equals("startEnergy")) {
					centreEnergy.setValue((((Number) endEnergy.getValue()).doubleValue() + e.getDoubleValue())/2.0);
					energyWidth.setValue(((Number) endEnergy.getValue()).doubleValue() - e.getDoubleValue());
				}
				
				if (e.getFieldName().equals("endEnergy")) {
					centreEnergy.setValue((((Number) startEnergy.getValue()).doubleValue() + e.getDoubleValue())/2.0);
					energyWidth.setValue(-1 *((Number) startEnergy.getValue()).doubleValue() + e.getDoubleValue());
				}
				
				if (e.getFieldName().equals("energyWidth")) {
					startEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue() - e.getDoubleValue()/2.0);
					endEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue() + e.getDoubleValue()/2.0);
				}
			} else {
				if (e.getFieldName().equals("startEnergy")) {
					centreEnergy.setValue(((Number) energyWidth.getValue()).doubleValue()/2.0 + e.getDoubleValue());
					endEnergy.setValue(((Number) energyWidth.getValue()).doubleValue() + e.getDoubleValue());
				}
				
				if (e.getFieldName().equals("endEnergy")) {
					centreEnergy.setValue(e.getDoubleValue() - ((Number) energyWidth.getValue()).doubleValue()/2.0);
					startEnergy.setValue(e.getDoubleValue() - ((Number) energyWidth.getValue()).doubleValue());
				}
				
				if (e.getFieldName().equals("energyWidth")) {
					// not allowed
				}
			}

			if (e.getFieldName().equals("centreEnergy")) {
				startEnergy.setValue(e.getDoubleValue() - ((Number) energyWidth.getValue()).doubleValue()/2.0);
				endEnergy.setValue(e.getDoubleValue() + ((Number) energyWidth.getValue()).doubleValue()/2.0);
			}
				
		} finally {
			wedidit = false;
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
			centreEnergy.setValue((((Number) endEnergy.getValue()).doubleValue() + ((Number) startEnergy.getValue()).doubleValue())/2.0);
			if (!isSwept()) {
				stepEnergy.setValue(capabilities.getEnergyStepForPass(((Number) passEnergy.getValue()).intValue()));
				stepEnergy.setEditable(false);
				energyWidth.setActive(false);
				energyWidth.setValue(capabilities.getEnergyWidthForPass(((Number) passEnergy.getValue()).intValue()));
				startEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue() - ((Number) energyWidth.getValue()).doubleValue()/2.0);
				endEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue() + ((Number) energyWidth.getValue()).doubleValue()/2.0);

			} else {
				stepEnergy.setEditable(true);
				energyWidth.setActive(true);
				energyWidth.setValue(((Number) endEnergy.getValue()).doubleValue() - ((Number) startEnergy.getValue()).doubleValue());
			}
		} finally {
			wedidit = false;
		}
	}
	
	public static void setDropTarget (final Composite parent, final Shell shell, final RichBeanEditorPart editor) {
		int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
		DropTarget target = new DropTarget(parent, operations);
		target.setTransfer(new Transfer[] {FileTransfer.getInstance()});
		target.addDropListener (new DropTargetAdapter() {
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

				MessageDialog dialog = new MessageDialog(shell, "Save imported settings", null, "We would suggest saving this experiment under a new name now.", 
						MessageDialog.QUESTION, new String[] { "Save as...", "Keep existing name and don't save yet" }, 0);
				int result = dialog.open();
				if (result == 0)	
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().doSaveAs();
			}
		});
	}
}