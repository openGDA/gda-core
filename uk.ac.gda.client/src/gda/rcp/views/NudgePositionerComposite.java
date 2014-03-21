/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.rcp.views;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NudgePositionerComposite extends Composite{
	private static final Logger logger = LoggerFactory.getLogger(NudgePositionerComposite.class);	
	private Text increment;
	private Text position;
	private Button btnStop;
	private Scannable scannable;
	private Button btnDecrement;
	private Button btnIncrement;
	private Job updateReadbackJob;
	private double lowerLimit;
	private double upperLimit;
	private String scannableName;
	private boolean limitsSet=false;
	private String userUnits;
	
	public NudgePositionerComposite(Composite parent, int style, Scannable scannable) {
		this(parent, style, scannable, true, null);
	}
	
	public NudgePositionerComposite(Composite parent, int style, Scannable scannable, boolean showName) {
		this(parent, style, scannable, showName, null);
	}
	
	public NudgePositionerComposite(Composite parent, int style, Scannable scannable, boolean showName, String overrideName) {
		super(parent, style);
		this.scannable = scannable;
		GridLayout gridLayout = new GridLayout(4, false);
		gridLayout.horizontalSpacing = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 0;
		setLayout(gridLayout);
		scannableName = scannable.getName();
		
		GridData gd_position = new GridData(SWT.FILL, SWT.TOP, false, false, 4, 1);
		gd_position.widthHint = 85;
		
		if(showName){
			Label lblScannableName = new Label(this, SWT.CENTER);
			lblScannableName.setForeground(new Color(getDisplay(), 0, 0, 0));
			if(overrideName==null || overrideName.equals(""))
				lblScannableName.setText(scannableName);
			else
				lblScannableName.setText(overrideName);
			lblScannableName.setLayoutData(gd_position);
		}
		
		position = new Text(this, SWT.BORDER);
		position.setTextLimit(10);
		position.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.keyCode;
				if(keyCode==13){//enter pressed
					updateReadbackJob.schedule();
					double newPosition = Double.valueOf(position.getText());
					try {
						move(newPosition);
					} catch (DeviceException e1) {
						logger.error("Error while trying to move " + scannableName, e1);
					}
				}
			}
		});
		gd_position = new GridData(SWT.FILL, SWT.TOP, false, false, 4, 1);
		gd_position.widthHint = 85;
		position.setLayoutData(gd_position);
		
		btnDecrement = new Button(this, SWT.NONE);
		btnDecrement.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateReadbackJob.schedule();
				try {
					double currentPosition = Double.valueOf(NudgePositionerComposite.this.scannable.getPosition().toString());
					double decrementValue = Double.valueOf(increment.getText());
					move(currentPosition-decrementValue);
				} catch (DeviceException e1) {
					logger.error("Error while trying to move " + scannableName, e1);
				}
			}
		});
		GridData gd_btnDecrement = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnDecrement.widthHint = 30;
		btnDecrement.setLayoutData(gd_btnDecrement);
		btnDecrement.setText("-");
		
		increment = new Text(this, SWT.BORDER);
		increment.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//check whether valid
			}
		});
		increment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		btnIncrement = new Button(this, SWT.NONE);
		btnIncrement.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateReadbackJob.schedule();
				try {
					double currentPosition = Double.valueOf(NudgePositionerComposite.this.scannable.getPosition().toString());
					double incrementValue = Double.valueOf(increment.getText());
					move(currentPosition+incrementValue);
				} catch (DeviceException e1) {
					logger.error("Error while trying to move " + scannableName, e1);
				}
			}
		});
		GridData gd_btnIncrement = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnIncrement.widthHint = 30;
		btnIncrement.setLayoutData(gd_btnIncrement);
		btnIncrement.setText("+");
		
		btnStop = new Button(this, SWT.NONE);
		btnStop.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateReadbackJob.schedule();
				try {
					NudgePositionerComposite.this.scannable.stop();
				} catch (DeviceException e1) {
					logger.error("Error while stopping "+ scannableName, e);
				}
			}
		});
		btnStop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
		btnStop.setText("Stop");
		
		double defaultIncrement = 1;
		increment.setText(String.valueOf(defaultIncrement));
		
		updateReadbackJob = new Job("updateReadback") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				boolean moving = true;
				
				while (moving) {
					boolean status;
					try {
						status = NudgePositionerComposite.this.scannable.isBusy();
						if (!status)
							moving = false;
					} catch (DeviceException e1) {
						logger.error("Error while determining whether " + scannableName + " is busy", e1);
					}
					
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
					}
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							double currentPosition;
							try {
								currentPosition = Double.valueOf(NudgePositionerComposite.this.scannable.getPosition().toString());
								setPositionValue(currentPosition);
							} catch (DeviceException e) {
								logger.error("Error while getting currrent position of " + scannableName, e);
							}
							btnDecrement.setEnabled(false);
							btnIncrement.setEnabled(false);
							btnStop.setEnabled(true);
						}
					});
				}
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						double currentPosition;
						try {
							currentPosition = Double.valueOf(NudgePositionerComposite.this.scannable.getPosition().toString());
							setPositionValue(currentPosition);
						} catch (DeviceException e) {
							logger.error("Error while getting currrent position of " + scannableName, e);
						}
						btnDecrement.setEnabled(true);
						btnIncrement.setEnabled(true);
						btnStop.setEnabled(false);
					}
				});
				return Status.OK_STATUS;
			}
		};

		refresh();//get initial values
		
		final IObserver iObserver = new IObserver() {
			@Override
			public void update(final Object source, Object arg) {
				refresh();
			}
		};
		
		scannable.addIObserver(iObserver);	
		this.addDisposeListener(new DisposeListener() {		
			@Override
			public void widgetDisposed(DisposeEvent e) {
				NudgePositionerComposite.this.scannable.deleteIObserver(iObserver);
				updateReadbackJob.cancel();
			}
		});
		
		determineScannableLimits();
		determineUserUnits();
	}
	
	public void refresh() {
		updateReadbackJob.cancel();
		updateReadbackJob.schedule();
	}
	
	private void move(double position) throws DeviceException{
		boolean batonHeld = JythonServerFacade.getInstance().isBatonHeld();
		if(!batonHeld){
			MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(), "Baton not held", null,
				    "You do not hold the baton, please take the baton using the baton manager.", MessageDialog.ERROR, new String[] { "Ok" }, 0);
			dialog.open();
		}
		else if(!limitsSet || (position>=lowerLimit && position<= upperLimit))
			NudgePositionerComposite.this.scannable.asynchronousMoveTo(position);
	}
	
	private void setPositionValue(double position){
		if(userUnits==null)
			this.position.setText(String.valueOf(position));
		else if(userUnits.equals(""))
			this.position.setText(String.valueOf(position));
		else
			this.position.setText(String.valueOf(position)+userUnits);
	}
	
	private void determineUserUnits(){
		JythonServerFacade jythonServer = JythonServerFacade.getInstance();
		String command = "\'" + scannableName + "\' in globals()";
		String evaluateCommand = jythonServer.evaluateCommand(command);
		if(evaluateCommand.equals("True")){
			command = "\'getUserUnits\' in dir("+scannableName+")";
			evaluateCommand = jythonServer.evaluateCommand(command);
			if(evaluateCommand.equals("True")){
				command = scannableName + ".getUserUnits()";
				userUnits = jythonServer.evaluateCommand(command);
			}
		}
	}
		
	private void determineScannableLimits(){
		JythonServerFacade jythonServer = JythonServerFacade.getInstance();
		String command = "\'" + scannableName + "\' in globals()";
		String evaluateCommand = jythonServer.evaluateCommand(command);
		if(evaluateCommand.equals("True")){
			command = "\'getLowerInnerLimit\' in dir("+scannableName+")";
			evaluateCommand = jythonServer.evaluateCommand(command);
			if(evaluateCommand.equals("True")){
				command = scannableName + ".getLowerInnerLimit()";
				evaluateCommand = jythonServer.evaluateCommand(command);
				lowerLimit = Double.parseDouble(evaluateCommand);
				command = scannableName + ".getUpperInnerLimit()";
				evaluateCommand = jythonServer.evaluateCommand(command);
				upperLimit = Double.parseDouble(evaluateCommand);
				this.position.setToolTipText(lowerLimit + " : " + upperLimit);
				limitsSet=true;
			}
			else{
				command = "\'getLowerGdaLimits\' in dir("+scannableName+")";
				evaluateCommand = jythonServer.evaluateCommand(command);
				if(evaluateCommand.equals("True")){
					command = scannableName + ".getLowerGdaLimits()";
					evaluateCommand = jythonServer.evaluateCommand(command);
					if(!evaluateCommand.equals("None")){
						command = scannableName + ".getLowerGdaLimits()[0]";
						evaluateCommand = jythonServer.evaluateCommand(command);
						lowerLimit = Double.parseDouble(evaluateCommand);
						command = scannableName + ".getUpperGdaLimits()[0]";
						evaluateCommand = jythonServer.evaluateCommand(command);
						upperLimit = Double.parseDouble(evaluateCommand);
						this.position.setToolTipText(lowerLimit + " : " + upperLimit);
						limitsSet=true;
					}
				}
			}
			
		}
		
	}

}
