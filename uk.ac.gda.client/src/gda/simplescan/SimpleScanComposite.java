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

package gda.simplescan;

import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gda.rcp.GDAClientActivator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.ComboWrapperWithGetCombo;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

/**
 *
 */
public final class SimpleScanComposite extends Composite {

	private ComboWrapperWithGetCombo scannableName;
	private ScaleBox fromPos;
	private ScaleBox toPos;
	private ScaleBox stepSize;
	private ScaleBox acqTime;
	private Group grpScannable;
	private Composite composite;
	private Label lblScannable;
	private Label lblFrom;
	private Label lblTo;
	private Label lblStep;
	private SimpleScan bean;
	private Composite detComposite;
	private Label lblAcqTime_1;
	protected TableViewer viewer;
	private Image CHECKED;
	private Image UNCHECKED;
	private DescriptionEditingSupport des;
	private EnabledEditingSupport detEnabled;
	private List<ScannableManagerBean> scannables;
	private Job scanStatusJob;
	private Button scan;
	private Button stop;
//	private String scannable;
	
	public SimpleScanComposite(Composite parent, int style, Object editingBean) {
		super(parent, style);

		CHECKED = GDAClientActivator.getImageDescriptor("icons/checked.gif").createImage();
		UNCHECKED = GDAClientActivator.getImageDescriptor("icons/unchecked.gif").createImage();

		bean = (SimpleScan) editingBean;

		grpScannable = new Group(this, SWT.NONE);
		grpScannable.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		grpScannable.setText("Scan");
		GridLayout gl_grpScannable = new GridLayout(1, false);
		gl_grpScannable.verticalSpacing = 0;
		gl_grpScannable.marginWidth = 0;
		gl_grpScannable.marginHeight = 0;
		gl_grpScannable.horizontalSpacing = 0;
		grpScannable.setLayout(gl_grpScannable);

		composite = new Composite(grpScannable, SWT.NONE);
		GridData gd_composite = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_composite.widthHint = 559;
		composite.setLayoutData(gd_composite);
		GridLayout gl_composite = new GridLayout(9, false);
		gl_composite.horizontalSpacing = 2;
		gl_composite.marginHeight = 2;
		gl_composite.verticalSpacing = 2;
		composite.setLayout(gl_composite);

		lblScannable = new Label(composite, SWT.NONE);
		GridData gd_lblScannable = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblScannable.widthHint = 75;
		lblScannable.setLayoutData(gd_lblScannable);
		lblScannable.setText("Scannable");

		createScannables(composite);

		updateScannables();

		lblFrom = new Label(composite, SWT.NONE);
		lblFrom.setText("From");

		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		setLayout(gridLayout);
		this.fromPos = new ScaleBox(composite, SWT.NONE);
		((GridData) fromPos.getControl().getLayoutData()).widthHint = 45;
		fromPos.getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				bean.setScannableName(scannableName.getItem(scannableName.getSelectionIndex()));
				try {
					setMotorLimits(bean.getScannableName(), fromPos);
				} catch (Exception e1) {
				}
			}
		});
		fromPos.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblTo = new Label(composite, SWT.NONE);
		lblTo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTo.setText("To");
		this.toPos = new ScaleBox(composite, SWT.NONE);
		((GridData) toPos.getControl().getLayoutData()).widthHint = 46;
		toPos.getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				bean.setScannableName(scannableName.getItem(scannableName.getSelectionIndex()));
				try {
					setMotorLimits(bean.getScannableName(), toPos);
				} catch (Exception e1) {
				}
			}
		});
		toPos.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		try {
			setMotorLimits(bean.getScannableName(), fromPos);
			setMotorLimits(bean.getScannableName(), toPos);
		} catch (Exception e1) {
		}

		lblStep = new Label(composite, SWT.NONE);
		lblStep.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblStep.setText("Step");
		this.stepSize = new ScaleBox(composite, SWT.NONE);
		((GridData) stepSize.getControl().getLayoutData()).widthHint = 41;
		stepSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		@SuppressWarnings("unused")
		Label lbl = new Label(composite, SWT.NONE);

		detComposite = new Composite(grpScannable, SWT.NONE);
		GridLayout gl_detComposite = new GridLayout(1, false);
		gl_detComposite.marginHeight = 0;
		gl_detComposite.verticalSpacing = 0;
		gl_detComposite.marginWidth = 0;
		gl_detComposite.horizontalSpacing = 0;
		detComposite.setLayout(gl_detComposite);
		GridData gd_detComposite = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		gd_detComposite.heightHint = 218;
		gd_detComposite.widthHint = 568;
		detComposite.setLayoutData(gd_detComposite);

		createDetectors(detComposite);

		updateDetectors();

//		createScanButton(this);

		this.fromPos.setValue(bean.getFromPos());
		lbl = new Label(fromPos, SWT.NONE);
		this.toPos.setValue(bean.getToPos());
		lbl = new Label(toPos, SWT.NONE);
		this.stepSize.setValue(bean.getStepSize());
		lbl = new Label(stepSize, SWT.NONE);
		this.acqTime.setValue(bean.getAcqTime());
		lbl = new Label(acqTime, SWT.NONE);

		scannables = bean.getScannables();
		boolean found = false;
		for (int i = 0; i < scannables.size(); i++) {
			if (scannables.get(i).getScannableName().equals(bean.getScannableName())) {
				this.scannableName.select(i + 1);
				found = true;
			}
		}
		if (!found)
			this.scannableName.select(0);
		
		scanStatusJob = new Job("updateScanStatus") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				boolean moving = true;
				
				while (moving) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
					}
					if (JythonServerFacade.getInstance().getScanStatus() != Jython.RUNNING)
						moving = false;
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							scan.setEnabled(false);
							stop.setEnabled(true);
						}
					});
				}
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						scan.setEnabled(true);
						stop.setEnabled(false);
					}
				});
				return Status.OK_STATUS;
			}
		};
	}

	public void updateScannables() {
		List<String> names = new ArrayList<String>(bean.getScannables().size());
		String[] comboNames = new String[bean.getScannables().size() + 1];
		comboNames[0] = "";
		for (int i = 1; i < bean.getScannables().size() + 1; i++) {
			names.add(bean.getScannables().get(i - 1).getScannableName());
			comboNames[i] = bean.getScannables().get(i - 1).getScannableName();
		}
		scannableName.setItems(comboNames);

		List<ScannableManagerBean> scannables = bean.getScannables();
		boolean found = false;

		for (int i = 0; i < scannables.size(); i++) {
			if (scannables.get(i).getScannableName().equals(bean.getScannableName())) {
				scannableName.select(i + 1);
				found = true;
			}
		}
		if (!found)
			scannableName.select(0);
	}

	public void updateDetectors() {
		List<String> names = new ArrayList<String>(bean.getDetectors().size());
		String[] comboNames = new String[bean.getDetectors().size()];
		for (int i = 0; i < bean.getDetectors().size(); i++) {
			names.add(bean.getDetectors().get(i).getDetectorName());
			comboNames[i] = bean.getDetectors().get(i).getDetectorName();
		}
		if (names.size() > 0) {
			viewer.setInput(names);
			viewer.refresh();
		}
	}

//	public void createScanButton(Composite comp) {
//	}

	private void performScan() {

		String scannable_name = scannableName.getItem(scannableName.getSelectionIndex());
		double from = fromPos.getNumericValue();
		double to = toPos.getNumericValue();
		double step = stepSize.getNumericValue();
		Double acq = acqTime.getNumericValue();

		if (bean.getDetectors().size() == 0) {
			String command = "scan " + scannable_name + " " + from + " " + to + " " + step;
			JythonServerFacade.getInstance().runCommand(command);
		} else {
			List<DetectorManagerBean> detectors = bean.getDetectors();
			String detList = "";
			for (int i = 0; i < detectors.size(); i++) {
				if (detectors.get(i).isEnabled())
					detList += detectors.get(i).getDetectorName() + " ";
			}
			if (!acq.isNaN() && !acqTime.getValue().toString().equals(""))
				detList += acq;
			String command = "scan " + scannable_name + " " + from + " " + to + " " + step + " " + detList;
			JythonServerFacade.getInstance().runCommand(command);
		}
	}

	public void setMotorLimits(String motorName, ScaleBox box) throws Exception {
		double lowerLimit = Double.parseDouble(JythonServerFacade.getInstance().evaluateCommand(motorName + ".getLowerInnerLimit()"));
		double upperLimit = Double.parseDouble(JythonServerFacade.getInstance().evaluateCommand(motorName + ".getUpperInnerLimit()"));
		
		if(lowerLimit<-1000000)
			lowerLimit=-1000000;
		if(upperLimit>1000000)
			upperLimit=1000000;
		
		BigDecimal bdLowerLimit = new BigDecimal(lowerLimit).setScale(6, RoundingMode.HALF_EVEN);
		BigDecimal bdUpperLimit = new BigDecimal(upperLimit).setScale(6, RoundingMode.HALF_EVEN);
		
		box.setMinimum(bdLowerLimit.doubleValue());
		box.setMaximum(bdUpperLimit.doubleValue());
	}

	public void createScannables(Composite comp) {
		this.scannableName = new ComboWrapperWithGetCombo(comp, SWT.NONE);
		GridData gd_scannableName = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_scannableName.widthHint = 161;
		scannableName.setLayoutData(gd_scannableName);	
		scannableName.getCombo().addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					bean.setScannableName(scannableName.getItem(scannableName.getSelectionIndex()));
					setMotorLimits(bean.getScannableName(), fromPos);
					setMotorLimits(bean.getScannableName(), toPos);
				} catch (Exception e1) {
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}

	public void createDetectors(Composite comp) {

		Composite composite_2 = new Composite(comp, SWT.NONE);
		GridData gd_composite_2 = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
		gd_composite_2.heightHint = 216;
		composite_2.setLayoutData(gd_composite_2);
		GridLayout gl_composite_2 = new GridLayout(2, false);
		gl_composite_2.horizontalSpacing = 0;
		gl_composite_2.marginWidth = 0;
		gl_composite_2.marginHeight = 0;
		gl_composite_2.verticalSpacing = 0;
		composite_2.setLayout(gl_composite_2);

		Composite composite_3 = new Composite(composite_2, SWT.NONE);
		GridLayout gl_composite_3 = new GridLayout(1, false);
		gl_composite_3.horizontalSpacing = 2;
		gl_composite_3.verticalSpacing = 2;
		gl_composite_3.marginWidth = 2;
		gl_composite_3.marginHeight = 2;
		composite_3.setLayout(gl_composite_3);
		GridData gd_composite_3 = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
		gd_composite_3.heightHint = 229;
		composite_3.setLayoutData(gd_composite_3);

		Composite composite_4 = new Composite(composite_2, SWT.NONE);
		composite_4.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));

		lblAcqTime_1 = new Label(composite_3, SWT.CENTER);
		GridData gd_lblAcqTime_1 = new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1);
		gd_lblAcqTime_1.widthHint = 71;
		lblAcqTime_1.setLayoutData(gd_lblAcqTime_1);
		lblAcqTime_1.setText("Acq Time");

		acqTime = new ScaleBox(composite_3, SWT.NONE);
		((GridData) acqTime.getControl().getLayoutData()).widthHint = 71;
		acqTime.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
				@SuppressWarnings("unused")
				Label lbl = new Label(composite_3, SWT.NONE);
		
				scan = new Button(composite_3, SWT.NONE);
				scan.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true, 1, 1));
				scan.setText("Scan");
				scan.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						performScan();
//						scannable = scannableName.getItem(scannableName.getSelectionIndex());
						scanStatusJob.schedule();
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) {
					}
				});
		
		stop = new Button(composite_3, SWT.NONE);
		stop.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		stop.setText("Stop");
		stop.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				performStop();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		stop.setEnabled(false);
		GridLayout gl_composite_4 = new GridLayout(1, false);
		gl_composite_4.marginHeight = 0;
		gl_composite_4.verticalSpacing = 0;
		gl_composite_4.marginWidth = 0;
		gl_composite_4.horizontalSpacing = 0;
		composite_4.setLayout(gl_composite_4);
		
		viewer = new TableViewer(composite_4, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		final TableViewerColumn enabledCol = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn enabledColumn = enabledCol.getColumn();
		enabledColumn.setText("Enabled");
		enabledColumn.setWidth(65);
		enabledColumn.setResizable(true);
		enabledColumn.setMoveable(true);

		detEnabled = new EnabledEditingSupport(viewer, bean);

		enabledCol.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				bean = detEnabled.getBean();
				for (int i = 0; i < bean.getDetectors().size(); i++) {
					if (bean.getDetectors().get(i).getDetectorName().equals(cell.getItem().getData().toString())) {
						if (bean.getDetectors().get(i).isEnabled())
							cell.setImage(CHECKED);
						else
							cell.setImage(UNCHECKED);
					}
				}

			}
		});
		enabledCol.setEditingSupport(detEnabled);

		final TableViewerColumn nameCol = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn nameColumn = nameCol.getColumn();
		nameColumn.setText("Detector Name");
		nameColumn.setWidth(150);
		nameColumn.setResizable(true);
		nameColumn.setMoveable(true);
		nameCol.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				cell.setText(cell.getItem().getData().toString());
			}
		});

		final TableViewerColumn descriptionCol = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn descriptionColumn = descriptionCol.getColumn();
		descriptionColumn.setText("Detector Description");
		descriptionColumn.setWidth(150);
		descriptionColumn.setResizable(true);
		descriptionColumn.setMoveable(true);
		des = new DescriptionEditingSupport(viewer, bean);

		descriptionCol.setLabelProvider(new CellLabelProvider() {
			@Override
			public void update(ViewerCell cell) {
				bean = des.getBean();
				for (int i = 0; i < bean.getDetectors().size(); i++) {
					if (bean.getDetectors().get(i).getDetectorName().equals(cell.getItem().getData().toString())) {
						cell.setText(bean.getDetectors().get(i).getDetectorDescription());
					}
				}
			}
		});
		descriptionCol.setEditingSupport(des);

		final Table table = viewer.getTable();
		table.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 1, 1));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		viewer.setContentProvider(new ArrayContentProvider());
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = false;
		gridData.heightHint = 200;
		gridData.widthHint = 465;
		viewer.getControl().setLayoutData(gridData);
	}

	private void performStop() {
		JythonServerFacade.getInstance().haltCurrentScan();
	}
	
	public SimpleScan getBean() {
		return bean;
	}

	public ScaleBox getFromPos() {
		return fromPos;
	}

	public ScaleBox getToPos() {
		return toPos;
	}

	public ScaleBox getStepSize() {
		return stepSize;
	}

	public ScaleBox getAcqTime() {
		return acqTime;
	}

	public FieldComposite getScannableName() {
		return scannableName;
	}
	
	public void setBean(SimpleScan bean) {
		this.bean = bean;
	}
}