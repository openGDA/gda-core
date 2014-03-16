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
import org.eclipse.swt.layout.RowLayout;
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
import uk.ac.gda.richbeans.components.scalebox.NumberBox;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapper;
import uk.ac.gda.richbeans.components.wrappers.RadioWrapper;
import uk.ac.gda.richbeans.components.wrappers.SpinnerWrapper;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;
import uk.ac.gda.richbeans.event.ValueEvent;
import uk.ac.gda.richbeans.event.ValueListener;

import org.eclipse.swt.widgets.Group;

public final class ARPESScanBeanComposite extends Composite implements ValueListener {
	private static final Logger logger = LoggerFactory.getLogger(ARPESScanBeanComposite.class);

	private final ComboWrapper lensMode;
	private final ComboWrapper passEnergy;
	private final NumberBox startEnergy;
	private final NumberBox endEnergy;
	private final NumberBox stepEnergy;
	private final NumberBox timePerStep;
	private final SpinnerWrapper iterations;
	private final RadioWrapper sweptMode;
	private final ScaleBox centreEnergy;
	private final ScaleBox energyWidth;
	private boolean wedidit = false;
	private AnalyserCapabilties capabilities;
	private Label lblLensMode;
	private Label lblPassEnergy;
	private Label lblSweptMode;
	private Label lblStartEnergy;
	private Label lblCentreEnergy;
	private Label lblEndEnergy;
	private Label lblStepEnergy;
	private Label lblEnergyWidth;
	private Label lblTimePerStep;
	private Label lblIterations;
	private Label lblScanCommand;
	private Label lblNewLabel_1;
	private Group queueGroup;
	private Group commandGroup;
	private Label lblAlsoCopiesCommand;
	private Group grpEnergy;

	public ARPESScanBeanComposite(final Composite parent, int style, final RichBeanEditorPart editor) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		capabilities = (AnalyserCapabilties) Finder.getInstance()
				.listAllLocalObjects(AnalyserCapabilties.class.getCanonicalName()).get(0);

		Label label = new Label(this, SWT.NONE);
		label.setText("Drop file here!");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

		setDropTarget(parent, parent.getShell(), editor);

		lblLensMode = new Label(this, SWT.NONE);
		lblLensMode.setText("Lens Mode");
		this.lensMode = new ComboWrapper(this, SWT.NONE);
		this.lensMode.setItems(capabilities.getLensModes());
		GridData gd_lensMode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lensMode.widthHint = 185;
		lensMode.setLayoutData(gd_lensMode);

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

		lblSweptMode = new Label(this, SWT.NONE);
		lblSweptMode.setText("Swept Mode");
		this.sweptMode = new RadioWrapper(this, SWT.NONE, new String[] { "fixed", "swept" }) {
			@Override
			public void setValue(Object value) {
				super.setValue((Boolean) value ? "swept" : "fixed");
			}

			@Override
			public Object getValue() {
				return super.getValue().equals("swept");
			}
		};
		GridData gd_sweptMode = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
		gd_sweptMode.widthHint = 182;
		sweptMode.setLayoutData(gd_sweptMode);
		sweptMode.addValueListener(this);
		RowLayout rowLayout = new RowLayout();
		rowLayout.spacing=20;
		rowLayout.marginLeft=13;
		sweptMode.setLayout(rowLayout);
		grpEnergy = new Group(this, SWT.NONE);
		grpEnergy.setLayout(new GridLayout(6, false));
		grpEnergy.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		grpEnergy.setText("Energy");
				
		lblStartEnergy = new Label(grpEnergy, SWT.NONE);
		lblStartEnergy.setText("Start");
						
		this.startEnergy = new ScaleBox(grpEnergy, SWT.NONE);
		GridData gridData = (GridData) startEnergy.getControl().getLayoutData();
		gridData.verticalAlignment = SWT.TOP;
		gridData.horizontalAlignment = SWT.LEFT;
		gridData.grabExcessVerticalSpace = false;
		gridData.widthHint = 90;
		gridData.grabExcessHorizontalSpace = false;
		startEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		startEnergy.setUnit("eV");
		startEnergy.setDecimalPlaces(3);
		startEnergy.addValueListener(this);

		lblCentreEnergy = new Label(grpEnergy, SWT.NONE);
		lblCentreEnergy.setText("Center");
		centreEnergy = new ScaleBox(grpEnergy, SWT.NONE);
		GridData gridData_1 = (GridData) centreEnergy.getControl().getLayoutData();
		gridData_1.widthHint = 90;
		gridData_1.grabExcessHorizontalSpace = false;
		centreEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		centreEnergy.setUnit("eV");
		centreEnergy.setDecimalPlaces(3);
		centreEnergy.setFieldName("centreEnergy");
		centreEnergy.on();
		centreEnergy.addValueListener(this);

		lblEndEnergy = new Label(grpEnergy, SWT.NONE);
		lblEndEnergy.setText("End");
		this.endEnergy = new ScaleBox(grpEnergy, SWT.NONE);
		GridData gridData_2 = (GridData) endEnergy.getControl().getLayoutData();
		gridData_2.widthHint = 90;
		gridData_2.grabExcessHorizontalSpace = false;
		endEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		endEnergy.setUnit("eV");
		endEnergy.setDecimalPlaces(3);
		endEnergy.addValueListener(this);

		lblStepEnergy = new Label(grpEnergy, SWT.NONE);
		lblStepEnergy.setText("Step");
		this.stepEnergy = new ScaleBox(grpEnergy, SWT.NONE);
		GridData gridData_3 = (GridData) stepEnergy.getControl().getLayoutData();
		gridData_3.widthHint = 90;
		gridData_3.grabExcessHorizontalSpace = false;
		stepEnergy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		stepEnergy.setUnit("meV");
		stepEnergy.setDecimalPlaces(5);
		stepEnergy.setMaximum(10000);
		stepEnergy.setMinimum(0.0001);
		stepEnergy.setMinimumValid(true);
		stepEnergy.addValueListener(this);

		lblEnergyWidth = new Label(grpEnergy, SWT.NONE);
		lblEnergyWidth.setText("Width");
		energyWidth = new ScaleBox(grpEnergy, SWT.NONE);
		GridData gridData_4 = (GridData) energyWidth.getControl().getLayoutData();
		gridData_4.widthHint = 90;
		gridData_4.grabExcessHorizontalSpace = false;
		energyWidth.setUnit("eV");
		energyWidth.setDecimalPlaces(3);
		energyWidth.setFieldName("energyWidth");
		energyWidth.on();
		energyWidth.setActiveMode(ACTIVE_MODE.SET_ENABLED_AND_ACTIVE);
		
		lblPassEnergy = new Label(grpEnergy, SWT.NONE);
		lblPassEnergy.setText("Pass");
		this.passEnergy = new ComboWrapper(grpEnergy, SWT.NONE);
		GridData gd_passEnergy = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_passEnergy.widthHint = 100;
		passEnergy.setLayoutData(gd_passEnergy);
		this.passEnergy.setItems(passMap);
		this.passEnergy.addValueListener(this);
		
		energyWidth.addValueListener(this);

		lblTimePerStep = new Label(this, SWT.NONE);
		lblTimePerStep.setText("Time Per Step");
		this.timePerStep = new ScaleBox(this, SWT.NONE);
		GridData gridData_5 = (GridData) timePerStep.getControl().getLayoutData();
		gridData_5.widthHint = 90;
		gridData_5.grabExcessHorizontalSpace = false;
		timePerStep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		timePerStep.setUnit("s");
		timePerStep.addValueListener(this);

		lblIterations = new Label(this, SWT.NONE);
		lblIterations.setText("Iterations");
		this.iterations = new SpinnerWrapper(this, SWT.NONE);
		GridData gd_iterations = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_iterations.widthHint = 96;
		iterations.setLayoutData(gd_iterations);
		
		iterations.addValueListener(this);

		commandGroup = new Group(this, SWT.NONE);
		commandGroup.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		commandGroup.setLayout(new GridLayout(2, false));
						
								Button btnClipboard = new Button(commandGroup, SWT.NONE);
								GridData gd_btnClipboard = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
								gd_btnClipboard.widthHint = 160;
								btnClipboard.setLayoutData(gd_btnClipboard);
								btnClipboard.setText("Show Scan Command");
								btnClipboard
										.setToolTipText("save file and copy Jython instructions to clip board to use this defintion in scripts");
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
											String[] commands = { getOurJythonCommand(editor) };
											String command = "";
											for(int i=0;i<commands.length;i++)
												command+=commands[i];
											lblScanCommand.setText(command);
											clipboard.setContents(commands, new Transfer[] { TextTransfer.getInstance() });
											clipboard.dispose();
										} catch (Exception e1) {
											logger.error("Error sending command to the clipboard", e1);
										}
									}
								});
						
						lblAlsoCopiesCommand = new Label(commandGroup, SWT.WRAP);
						GridData gd_lblAlsoCopiesCommand = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
						gd_lblAlsoCopiesCommand.widthHint = 310;
						lblAlsoCopiesCommand.setLayoutData(gd_lblAlsoCopiesCommand);
						lblAlsoCopiesCommand.setText("Also copies command to clipboard so it can be pasted into the jython console.");
						
						lblScanCommand = new Label(commandGroup, SWT.WRAP);
						GridData gd_lblScanCommand = new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1);
						gd_lblScanCommand.heightHint = 35;
						lblScanCommand.setLayoutData(gd_lblScanCommand);
		
		queueGroup = new Group(this, SWT.NONE);
		queueGroup.setLayout(new GridLayout(2, false));
		queueGroup.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
				
						Button btnQueueExperiment = new Button(queueGroup, SWT.NONE);
						GridData gd_btnQueueExperiment = new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1);
						gd_btnQueueExperiment.widthHint = 160;
						btnQueueExperiment.setLayoutData(gd_btnQueueExperiment);
						btnQueueExperiment.setText("Queue Experiment");
						btnQueueExperiment
								.setToolTipText("save file and queue for execution (will start immediately if queue running)");
						
						lblNewLabel_1 = new Label(queueGroup, SWT.WRAP);
						GridData gd_lblNewLabel_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 2);
						gd_lblNewLabel_1.widthHint = 310;
						lblNewLabel_1.setLayoutData(gd_lblNewLabel_1);
						lblNewLabel_1.setText("Creates a scan based on the xml file that represents these parameters and queues it. Editing afterwards will change the running scan. Create a new file if you want a new scan.");

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
					energyWidth
							.setValue(capabilities.getEnergyWidthForPass(((Number) passEnergy.getValue()).intValue()));
					energyWidth.setActive(false);
					startEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue()
							- ((Number) energyWidth.getValue()).doubleValue() / 2.0);
					endEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue()
							+ ((Number) energyWidth.getValue()).doubleValue() / 2.0);

				} 
				else {
					stepEnergy.setEditable(true);
					energyWidth.setActive(true);
				}
			}

			if (e.getFieldName().equals("passEnergy")
					|| (e.getFieldName().equals("sweptMode") && (Boolean) e.getValue())) {
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
			} else {
				if (e.getFieldName().equals("startEnergy")) {
					centreEnergy.setValue(((Number) energyWidth.getValue()).doubleValue() / 2.0 + e.getDoubleValue());
					endEnergy.setValue(((Number) energyWidth.getValue()).doubleValue() + e.getDoubleValue());
				}

				if (e.getFieldName().equals("endEnergy")) {
					centreEnergy.setValue(e.getDoubleValue() - ((Number) energyWidth.getValue()).doubleValue() / 2.0);
					startEnergy.setValue(e.getDoubleValue() - ((Number) energyWidth.getValue()).doubleValue());
				}

				if (e.getFieldName().equals("energyWidth")) {
					// not allowed
				}
			}

			if (e.getFieldName().equals("centreEnergy")) {
				startEnergy.setValue(e.getDoubleValue() - ((Number) energyWidth.getValue()).doubleValue() / 2.0);
				endEnergy.setValue(e.getDoubleValue() + ((Number) energyWidth.getValue()).doubleValue() / 2.0);
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
			centreEnergy.setValue((((Number) endEnergy.getValue()).doubleValue() + ((Number) startEnergy.getValue())
					.doubleValue()) / 2.0);
			if (!isSwept()) {
				stepEnergy.setValue(capabilities.getEnergyStepForPass(((Number) passEnergy.getValue()).intValue()));
				stepEnergy.setEditable(false);
				energyWidth.setActive(false);
				energyWidth.setValue(capabilities.getEnergyWidthForPass(((Number) passEnergy.getValue()).intValue()));
				startEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue()
						- ((Number) energyWidth.getValue()).doubleValue() / 2.0);
				endEnergy.setValue(((Number) centreEnergy.getValue()).doubleValue()
						+ ((Number) energyWidth.getValue()).doubleValue() / 2.0);
			} 
			else {
				stepEnergy.setEditable(true);
				energyWidth.setActive(true);
				energyWidth.setValue(((Number) endEnergy.getValue()).doubleValue()
						- ((Number) startEnergy.getValue()).doubleValue());
			}
		} finally {
			wedidit = false;
		}
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
					((DirtyContainer) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.getActiveEditor()).setDirty(true);
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