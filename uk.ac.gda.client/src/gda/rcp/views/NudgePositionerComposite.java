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
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A class which provides a GUI composite to allow easy control of a scannable. It provides the current
 * position which can be edited and moved and buttons to allow incremental moves. It also provides a
 * stop button to abort moves.
 */
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
	private boolean moveEnabled;
	private boolean positionOnly;
	private boolean showStop;
	private double defaultIncrement = 1;

	/**Simple constructor for a NudgePositionerComposite only requires the specification of minimal
	 * parameters. 
	 * @param parent
	 * @param style (Typically SWT.NONE)
	 * @param scannable Scannable to be controlled
	 */
	public NudgePositionerComposite(Composite parent, int style, Scannable scannable) {
		this(parent, style, scannable, true, null, false, true, true);
	}
	
	/**Constructor for a NudgePositionerComposite which allows additional control over the GUI
	 * @param parent
	 * @param style (Typically SWT.NONE)
	 * @param scannable Scannable to be controlled
	 * @param showName If false name will not be shown
	 */
	public NudgePositionerComposite(Composite parent, int style, Scannable scannable, boolean showName) {
		this(parent, style, scannable, showName, null, false, true);
	}

	/**Constructor for a NudgePositionerComposite which allows additional control over the GUI
	 * @param parent
	 * @param style (Typically SWT.NONE)
	 * @param scannable Scannable to be controlled
	 * @param overrideName Specifies a name which will be used for the GUI or null to use the scannable name
	 * @param showStop If false no stop button will be displayed (Can be used when scannable can't be stopped)
	 */
	public NudgePositionerComposite(Composite parent, int style, Scannable scannable, String overrideName, boolean showStop) {
		this(parent, style, scannable, true, overrideName, false, true, showStop);
	}
	
	public NudgePositionerComposite(Composite parent, int style, Scannable scannable, final boolean showName, String overrideName, final boolean positionOnly, boolean moveEnabled) {
		this(parent, style, scannable, showName, overrideName, positionOnly, moveEnabled, true);
	}

	/**Constructor for a NudgePositionerComposite which allows full control over the GUI
	 * @param parent
	 * @param style (Typically SWT.NONE)
	 * @param scannable Scannable to be controlled
	 * @param showName If false name will not be shown
	 * @param overrideName Specifies a name which will be used for the GUI or null to use the scannable name
	 * @param positionOnly If true only the position box will be shown (No nudging is possible)
	 * @param moveEnabled If false movement of the scannable will be blocked
	 * @param showStop If false no stop button will be displayed (Can be used when scannable can't be stopped)
	 */
	public NudgePositionerComposite(Composite parent, int style, Scannable scannable, final boolean showName, String overrideName, final boolean positionOnly, boolean moveEnabled, final boolean showStop) {
		super(parent, style);
		this.scannable = scannable;
		GridLayout gridLayout = new GridLayout(4, false);
		gridLayout.horizontalSpacing = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 0;
		setLayout(gridLayout);
		scannableName = scannable.getName();
		this.moveEnabled = moveEnabled;
		this.positionOnly = positionOnly;
		this.showStop = showStop;

		GridData gd_position = new GridData(SWT.FILL, SWT.TOP, false, false, 4, 1);
		gd_position.widthHint = 85;

		if (showName) {
			Label lblScannableName = new Label(this, SWT.CENTER);
			if (overrideName == null || overrideName.equals(""))
				lblScannableName.setText(scannableName);
			else
				lblScannableName.setText(overrideName);
			lblScannableName.setLayoutData(gd_position);
		}
		
		position = new Text(this, SWT.BORDER);
		position.setTextLimit(10);
		position.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent key) {
				//Get the keyCode
				int keyCode = key.keyCode;
				//If enter was pressed move to new position
				if(keyCode==13 || keyCode == 16777296){ //enter or numpad enter pressed
					double newPosition = Double.valueOf(position.getText().split(" ")[0]);
					move(newPosition);
				}
				if (!positionOnly) {
					//If up was pressed increment position and move
					if (keyCode == 16777217) { //up arrow pressed 
						double newPosition = Double.valueOf(position.getText().split(" ")[0]) + Double.valueOf(increment.getText());
						move(newPosition);
					}
					//If down was pressed decrement position and move
					if (keyCode == 16777218) { //down arrow pressed 
						double newPosition = Double.valueOf(position.getText().split(" ")[0]) - Double.valueOf(increment.getText());
						move(newPosition);
					}
				}
			}
		});
		position.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				// Update to ensure current position is shown when focus is lost
				updateReadbackJob.schedule();
			}

			@Override
			public void focusGained(FocusEvent e) {
				// Don't do anything.
			}
		});

		gd_position = new GridData(SWT.FILL, SWT.TOP, false, false, 4, 1);
		gd_position.widthHint = 85;
		position.setLayoutData(gd_position);
		position.setEditable(moveEnabled);

		if(!positionOnly){
			btnDecrement = new Button(this, SWT.NONE);
			btnDecrement.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					double currentPosition = Double.valueOf(position.getText().split(" ")[0]);
					double decrementValue = Double.valueOf(increment.getText());
					move(currentPosition - decrementValue);
				}
			});
			GridData gd_btnDecrement = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_btnDecrement.widthHint = 30;
			btnDecrement.setLayoutData(gd_btnDecrement);
			btnDecrement.setText("-");

			increment = new Text(this, SWT.BORDER);
			increment.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			increment.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent key) {
					//Get the keyCode
					int keyCode = key.keyCode;
					//If enter was pressed switch focus to position box to allow up down tapping.
					if(keyCode==13 || keyCode == 16777296){ //enter or numpad enter pressed 
						position.setFocus();
					}
				}
			});

			btnIncrement = new Button(this, SWT.NONE);
			btnIncrement.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					double currentPosition = Double.valueOf(position.getText().split(" ")[0]);
					double incrementValue = Double.valueOf(increment.getText());
					move(currentPosition + incrementValue);
				}
			});
			GridData gd_btnIncrement = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gd_btnIncrement.widthHint = 30;
			btnIncrement.setLayoutData(gd_btnIncrement);
			btnIncrement.setText("+");

			// Stop button
			
			if (showStop) {
				btnStop = new Button(this, SWT.NONE);
				btnStop.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						try {
							NudgePositionerComposite.this.scannable.stop();
						} catch (DeviceException e1) {
							logger.error("Error while stopping " + scannableName, e);
						}
					}
				});
				btnStop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
				btnStop.setText("Stop");
			}
			increment.setText(String.valueOf(defaultIncrement));
		}

		// This is the job which handles updating of the composite. It need to be scheduled when a move is
		// started after which it will continue to run until the move finishes.
		updateReadbackJob = new Job("Update " + scannableName + " nudge positioner readback value") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				boolean moving = true;
				while (moving) { // Loop which runs while scannable is moving
					boolean status;
					try {
						status = NudgePositionerComposite.this.scannable.isBusy();
						if (!status) {
							moving = false;
						}
					} catch (DeviceException e1) {
						logger.error("Error while determining whether " + scannableName + " is busy", e1);
					}

					try {
						Thread.sleep(100); // Pause to stop loop running to fast. ~ 10 Hz
					} catch (InterruptedException e) { // Do nothing
					}
					// Update the GUI
					updateGui(getCurrentPosition(), moving);
				}
				return Status.OK_STATUS;
			}
		};

		// Add an observer to the scannable when an event occurs such as starting to move
		// start the updateReadbackJob. If the job is already running a maximum of one extra will
		// be scheduled.
		final IObserver iObserver = new IObserver() {
			@Override
			public void update(final Object source, Object arg) {
				// Start the updateReadbackJob
				updateReadbackJob.schedule();
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
		updateReadbackJob.schedule(); // Get initial values
	}

	/**
	 * Moves the scannable to a new position by calling {@link Scannable} asynchronousMoveTo(position).
	 * Checks if the position is within limits and if the scannable is busy before moving
	 * @param position The demanded position
	 */
	private void move(double position) {
		boolean batonHeld = JythonServerFacade.getInstance().isBatonHeld();
		if(!batonHeld){
			MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(), "Baton not held", null,
				    "You do not hold the baton, please take the baton using the baton manager.", MessageDialog.ERROR, new String[] { "Ok" }, 0);
			dialog.open();
		}
		else if(!limitsSet || (position>=lowerLimit && position<= upperLimit) && moveEnabled)
			try {
				if(!NudgePositionerComposite.this.scannable.isBusy()){
					NudgePositionerComposite.this.scannable.asynchronousMoveTo(position);
				}
			} catch (DeviceException e) {
				logger.error("Error while trying to move " + scannableName, e);
			}
	}

	/**
	 * This is used to update the GUI. Only this method should be used to update the GUI to ensure
	 * display is consistent. The position is updated and the controls enabled/disabled as appropriate
	 * @param currentPositionString The newest position to display typically from {@link #getCurrentPosition()}
	 * @param moving Flag showing if the scannable is moving
	 */
	private void updateGui(final String currentPositionString, final boolean moving) {
		// Update the GUI in the UI thread
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				// Update the position
				if (currentPositionString == null) {
					NudgePositionerComposite.this.position.setText("null");
				} else if (userUnits == null || userUnits.equals("")) {
					NudgePositionerComposite.this.position.setText(currentPositionString);
				} else {
					NudgePositionerComposite.this.position.setText(currentPositionString + " " + userUnits);
				}
				// Update the controls enabled/disabled
				if (!positionOnly) { // If positionOnly=true buttons won't exist.
					btnDecrement.setEnabled(!moving);
					btnIncrement.setEnabled(!moving);
					position.setEditable(!moving);
					if (showStop) {
						btnStop.setEnabled(moving);
					}
				}
			}
		});
	}

	/**
	 * Calls {@link Scannable} getPosition() method and parses it into a String using getOutputFormat()
	 * If the scannable returns an array the first element is used.
	 * @return The current position of the scannable
	 */
	private String getCurrentPosition() {
		Double currentPosition = null;
		try {
			Object getPosition = this.scannable.getPosition();

			if (getPosition.getClass().isArray())
				// The scannable returns an array assume the relevant value is the first and its a Double
				currentPosition = ((Double) ((Object[]) getPosition)[0]).doubleValue();
			else if (getPosition instanceof Double) {
				currentPosition = ((Double) getPosition).doubleValue();
			}
			else {
				logger.error("Error while parsing currrent position of " + scannableName);
			}
		} catch (DeviceException e) {
			logger.error("Error while getting currrent position of " + scannableName, e);
		}

		return String.format(scannable.getOutputFormat()[0], currentPosition).trim();
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