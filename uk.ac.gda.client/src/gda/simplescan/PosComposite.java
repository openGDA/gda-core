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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.swtdesigner.SWTResourceManager;

import gda.configuration.properties.LocalProperties;
import gda.jython.JythonServerFacade;


public class PosComposite extends Composite {

	Combo scannableName;
	SimpleScan bean;
	Text incrementVal;
	ScaleBox textTo;
	Button btnIncrement;
	Button btnDecrement;
	Label lblReadbackVal;
	Job updateReadbackJob;
	String scannable;
	Text setVal;
	Button btnSet;

	public PosComposite(Composite parent, int style, Object editingBean) {
		super(parent, style);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		setLayout(gridLayout);
		bean = (SimpleScan) editingBean;

		Group grpPos = new Group(this, SWT.NONE);
		GridData gd_grpPos = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_grpPos.widthHint = 258;
		grpPos.setLayoutData(gd_grpPos);
		grpPos.setText("Pos");
		GridLayout gl_grpPos = new GridLayout(1, false);
		gl_grpPos.marginHeight = 2;
		gl_grpPos.marginWidth = 2;
		gl_grpPos.verticalSpacing = 2;
		gl_grpPos.horizontalSpacing = 2;
		grpPos.setLayout(gl_grpPos);

		Composite posComposite = new Composite(grpPos, SWT.NONE);
		GridData gd_posComposite = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_posComposite.widthHint = 218;
		posComposite.setLayoutData(gd_posComposite);
		GridLayout gl_posComposite = new GridLayout(2, false);
		gl_posComposite.marginWidth = 2;
		gl_posComposite.verticalSpacing = 2;
		gl_posComposite.horizontalSpacing = 2;
		gl_posComposite.marginHeight = 0;
		posComposite.setLayout(gl_posComposite);

		Label lblScannable = new Label(posComposite, SWT.NONE);
		lblScannable.setText("Scannable");

		createScannables(posComposite);
		Composite composite_2 = new Composite(grpPos, SWT.NONE);
		GridLayout gl_composite_2 = new GridLayout(5, false);
		gl_composite_2.verticalSpacing = 2;
		gl_composite_2.marginWidth = 2;
		gl_composite_2.marginHeight = 0;
		gl_composite_2.horizontalSpacing = 2;
		composite_2.setLayout(gl_composite_2);

		Label lblTo = new Label(composite_2, SWT.NONE);
		lblTo.setText("Go to");
		createEmptyLabel(composite_2);
		textTo = new ScaleBox(composite_2, SWT.NONE);

		textTo.getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 13 || e.keyCode == 16777296)
					performPos();
				else {
					bean.setScannableName(scannableName.getItem(scannableName.getSelectionIndex()));
					try {
						setMotorLimits(bean.getScannableName(), textTo);
					} catch (Exception e1) {
					}
				}
			}
		});
		textTo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		createEmptyLabel(textTo);
		createEmptyLabel(composite_2);

		Button btnGo = new Button(composite_2, SWT.NONE);
		btnGo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnGo.setToolTipText("Moves scannable to new position");
		btnGo.setText("Go");

		btnGo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				performPos();
			}
		});
		btnGo.setEnabled(true);


		Label lblReadback = new Label(composite_2, SWT.NONE);
		lblReadback.setText("Readback");
		createEmptyLabel(composite_2);

		Composite incrementNuggeComp = new Composite(composite_2, SWT.NONE);
		GridData gd_incrementNuggeComp = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_incrementNuggeComp.widthHint = 117;
		incrementNuggeComp.setLayoutData(gd_incrementNuggeComp);
		GridLayout gl_incrementNuggeComp = new GridLayout(3, false);
		gl_incrementNuggeComp.verticalSpacing = 0;
		gl_incrementNuggeComp.marginHeight = 0;
		gl_incrementNuggeComp.marginWidth = 0;
		gl_incrementNuggeComp.horizontalSpacing = 0;
		incrementNuggeComp.setLayout(gl_incrementNuggeComp);

		btnDecrement = new Button(incrementNuggeComp, SWT.NONE);
		GridData gd_btnDecrement = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnDecrement.widthHint = 25;
		btnDecrement.setLayoutData(gd_btnDecrement);
		btnDecrement.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				performDecrement();
			}
		});
		btnDecrement.setText("-");
		btnDecrement.setFont(SWTResourceManager.getFont("Sans", 12, SWT.BOLD));
		lblReadbackVal = new Label(incrementNuggeComp, SWT.NONE);
		lblReadbackVal.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblReadbackVal.setText("               ");

		btnIncrement = new Button(incrementNuggeComp, SWT.NONE);
		GridData gd_btnIncrement = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnIncrement.widthHint = 25;
		btnIncrement.setLayoutData(gd_btnIncrement);
		btnIncrement.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				performIncrement();
			}
		});
		btnIncrement.setText("+");
		btnIncrement.setFont(SWTResourceManager.getFont("Sans", 12, SWT.BOLD));
		createEmptyLabel(composite_2);

		final Button btnStop = new Button(composite_2, SWT.NONE);
		GridData gd_btnStop = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnStop.widthHint = 55;
		btnStop.setLayoutData(gd_btnStop);
		btnStop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				performStop();
			}
		});
		btnStop.setText("Stop");
		btnStop.setEnabled(false);

		Label lblIncrement = new Label(composite_2, SWT.NONE);
		lblIncrement.setText("Increment");
		createEmptyLabel(composite_2);

		incrementVal = new Text(composite_2, SWT.BORDER);
		incrementVal.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		incrementVal.setText("1");
		createEmptyLabel(composite_2);

		updateScannables();

		final Label lblStatus = new Label(composite_2, SWT.NONE);
		GridData gd_lblStatus = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_lblStatus.widthHint = 55;
		lblStatus.setLayoutData(gd_lblStatus);
		lblStatus.setText("     Idle     ");

		if (LocalProperties.check("gda.simplescan.allowSet", false)) {
			Label lblSet = new Label(composite_2, SWT.NONE);
			lblSet.setText("Set");
			createEmptyLabel(composite_2);

			setVal = new Text(composite_2, SWT.BORDER);
			setVal.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			setVal.setText("");
			createEmptyLabel(composite_2);

			Button btnSet = new Button(composite_2, SWT.NONE);
			btnSet.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			btnSet.setText("Set");
			btnSet.setToolTipText("Set scannable position (does not move!)");

			btnSet.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					performSet();
				}
			});
			btnSet.setEnabled(true);
		}

		try {
			setMotorLimits(bean.getScannableName(), textTo);
		} catch (Exception e2) {
		}
		updateReadback();

		scannableName.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					bean.setScannableName(scannableName.getItem(scannableName.getSelectionIndex()));
					setMotorLimits(bean.getScannableName(), textTo);
					updateReadback();
				} catch (Exception e1) {
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		updateReadbackJob = new Job("updateReadback") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				boolean moving = true;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
				while (moving) {
					String status = JythonServerFacade.getInstance().evaluateCommand(
							scannable + ".getMotor().isMoving()");
					if (status.equals("False"))
						moving = false;
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
					}
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							updateReadback();
							lblStatus.setText(" Moving  ");
							btnDecrement.setEnabled(false);
							btnIncrement.setEnabled(false);
							btnStop.setEnabled(true);
						}
					});
				}
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						updateReadback();
						lblStatus.setText("     Idle     ");
						btnDecrement.setEnabled(true);
						btnIncrement.setEnabled(true);
						btnStop.setEnabled(false);
					}
				});
				return Status.OK_STATUS;
			}
		};
	}

	private void performPos() {
		scannable = scannableName.getItem(scannableName.getSelectionIndex());
		String demand = textTo.getValue().toString();
		String command = scannable + "(" + demand + ")";
		JythonServerFacade.getInstance().runCommand(command);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
		updateReadbackJob.schedule();
	}

	private void performIncrement() {
		scannable = scannableName.getItem(scannableName.getSelectionIndex());
		double increment = Double.parseDouble(incrementVal.getText());
		String command = scannable + "(" + scannable + "()+"+ increment + ")";
		JythonServerFacade.getInstance().runCommand(command);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
		updateReadbackJob.schedule();
	}

	private void performDecrement() {
		scannable = scannableName.getItem(scannableName.getSelectionIndex());
		double decrement = Double.parseDouble(incrementVal.getText());
		String command = scannable + "(" + scannable + "()+-"+ decrement + ")";
		JythonServerFacade.getInstance().runCommand(command);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
		updateReadbackJob.schedule();
	}

	private void performStop() {
		scannable = scannableName.getItem(scannableName.getSelectionIndex());
		String command = scannable + ".getMotor().stop()";
		JythonServerFacade.getInstance().runCommand(command);
		command = scannable + ".stop()";
		JythonServerFacade.getInstance().runCommand(command);
	}

	private void performSet() {
		scannable = scannableName.getItem(scannableName.getSelectionIndex());
		double pos = Double.parseDouble(setVal.getText());
		String command = scannable + ".setPosition(" + pos + ")";
		JythonServerFacade.getInstance().runCommand(command);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
		updateReadbackJob.schedule();
	}

	public void createScannables(Composite comp) {
		scannableName = new Combo(comp, SWT.NONE);
		GridData gd_scannableName = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_scannableName.widthHint = 136;
		scannableName.setLayoutData(gd_scannableName);
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
				updateReadback();
			}
		}
		if (!found)
			scannableName.select(0);

	}

	public void updateReadback() {
		String command = "java.lang.String.format("+ bean.getScannableName()+".getOutputFormat()[0],"+ bean.getScannableName()+"())";
		String formattedPos = JythonServerFacade.getInstance().evaluateCommand(command);
		lblReadbackVal.setText(formattedPos);
	}

	public void setMotorLimits(String motorName, ScaleBox box) throws Exception {
		double lowerLimit = Double.parseDouble(JythonServerFacade.getInstance().evaluateCommand(
				motorName + ".getLowerInnerLimit()"));
		double upperLimit = Double.parseDouble(JythonServerFacade.getInstance().evaluateCommand(
				motorName + ".getUpperInnerLimit()"));

		if (lowerLimit < -1000000)
			lowerLimit = -1000000;
		if (upperLimit > 1000000)
			upperLimit = 1000000;

		BigDecimal bdLowerLimit = new BigDecimal(lowerLimit).setScale(6, RoundingMode.HALF_EVEN);
		BigDecimal bdUpperLimit = new BigDecimal(upperLimit).setScale(6, RoundingMode.HALF_EVEN);

		box.setMinimum(bdLowerLimit.doubleValue());
		box.setMaximum(bdUpperLimit.doubleValue());
	}

	public void setBean(SimpleScan bean) {
		this.bean = bean;
	}

	@SuppressWarnings("unused")
	private void createEmptyLabel(Composite composite){
		new Label(composite, SWT.NONE);
	}

}
