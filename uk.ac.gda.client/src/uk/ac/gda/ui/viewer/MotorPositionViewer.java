/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.viewer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.api.event.ValueListener;
import org.eclipse.richbeans.widgets.EventListenersDelegate;
import org.eclipse.richbeans.widgets.scalebox.DemandBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.jython.InterfaceProvider;
import gda.observable.IObserver;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.ui.internal.viewer.ScannableMotionUnitsPositionSource;
import uk.ac.gda.ui.internal.viewer.ScannablePositionSource;

/**
 * A concrete viewer that displays position information about an underlying motor.
 * Users may use the viewer to change position of the motor, in which case this
 * viewer displays the target position as well as the updating position.
 * <p>
 * This class is designed to be instantiated with a pre-existing{@link Scannable}
 * which supplies the position information. The viewer registers a listener to receive
 * position updates from the underlying scannable.
 * </p>
 * <p>
 * This class is  not intended to be subclassed outside the viewer framework.
 * </p>
 */
public class MotorPositionViewer {
	private static final Logger logger = LoggerFactory.getLogger(MotorPositionViewer.class);

	private IPositionSource<Double> motor;

	private DemandBox motorBox;
	private Composite parent;

	private Scannable scannable;
	private double demandPrev;

	private String commandFormat;

	private MotorPositionDemandChangedCallback callback;

	private Job motorPositionJob;

	private boolean restoreValueWhenFocusLost;

	private IPositionVerifierDialogCreator<Double> newPositionDialog;

	private Object labelLayoutData;

	private EventListenersDelegate valueEventDelegate;


	public MotorPositionViewer(Composite parent, Scannable scannable){
		this(parent, scannable, null, false, null);
	}

	public MotorPositionViewer(Composite parent, Scannable scannable, String label){
		this(parent, scannable, label, false, null);
	}

	public MotorPositionViewer(Composite parent, Scannable scannable, String label, boolean hideLabel){
		this(parent, scannable, label, hideLabel, null);
	}

	public MotorPositionViewer(Composite parent, Scannable scannable, String label, boolean hideLabel, Object labelLayoutData){
		this.scannable = scannable;
		this.labelLayoutData = labelLayoutData;
		if( this.labelLayoutData == null){
			this.labelLayoutData = GridDataFactory.swtDefaults().create();
		}
		ScannablePositionSource positionSource=null;
		if (scannable instanceof ScannableMotionUnits) {
			positionSource = new ScannableMotionUnitsPositionSource((ScannableMotionUnits)scannable);
		} else {
			positionSource = new ScannablePositionSource(scannable);
		}
		positionSource.setLabel(label);
		positionSource.setHideLabel(hideLabel);
		motor = positionSource;
		this.parent = parent;
		createControls(parent);

		valueEventDelegate = new EventListenersDelegate();
		motorBox.addValueListener(new ValueAdapter() {

			@Override
			public void valueChangePerformed(ValueEvent e) {
				valueEventDelegate.notifyValueListeners(e);
			}
		});
	}

	/**
	 * @see EventListenersDelegate#addValueListener(ValueListener)
	 */
	public void addValueListener(final ValueListener l) {
		valueEventDelegate.addValueListener(l);
	}

	/**
	 * @see EventListenersDelegate#removeValueListener(ValueListener)
	 */
	public void removeValueListener(final ValueListener l) {
		valueEventDelegate.removeValueListener(l);
	}

	public void setCommandFormat(String commandFormat) {
		this.commandFormat = commandFormat;
	}

	public void setCallback(MotorPositionDemandChangedCallback callback) {
		this.callback = callback;
	}

	private void setDemandPrev() {
		try {
			demandPrev = motor.getPosition();
		} catch (DeviceException e1) {
			logger.error("Error setting current value of demandBox", e1);
		}
	}

	public void setPopupOnInvalidPosition(boolean popupOnInvalidPosition) {
		this.popupOnInvalidPosition = popupOnInvalidPosition;
	}

	private boolean popupOnInvalidPosition = false;

	public boolean isPopupOnInvalidPosition() {
		return popupOnInvalidPosition;
	}

	private void createControls(Composite comp){
		createReadbacksGroup(comp);
		motorBox.setUnit(motor.getDescriptor().getUnit());
		try {
			motorBox.setMaximum(motor.getDescriptor().getMaximumLimit());
			motorBox.setMinimum(motor.getDescriptor().getMinimumLimit());
		} catch (DeviceException e2) {
			logger.error("Error getting limits from motor", e2);
		}
		setDemandPrev();

		motorBox.addValueListener(new ValueAdapter("MotorPositionviewer") {

			@Override
			public void valueChangePerformed(ValueEvent e) {
				final double demand = motorBox.getNumericValue();
				setDemandPrev();
				if(Double.isNaN(demand))
				{
					motorBox.setValue(demandPrev);
					String msg = "Invalid position " + demand;
					logger.error(msg);
					if (isPopupOnInvalidPosition()) {
						UIHelper.showError(msg, "not a number");
					}
					return;
				}
				String reply = null;
				try {
					reply = scannable.checkPositionValid(demand);
				} catch (Exception e1) {
					motorBox.setValue(demandPrev);
					String msg = "Unable to check the validity of the input position, no move will be performed.";
					logger.error(msg, e1);
					if (isPopupOnInvalidPosition()) {
						UIHelper.showError(msg, String.format("%s\n\n%s", e1.getMessage(), e1.getStackTrace().toString()));
					}
					return;
				}
				if(reply != null)
				{
					motorBox.setValue(demandPrev);
					String msg = "Invalid position " + demand + "; Reason: " + reply;
					logger.error(msg);
					if (isPopupOnInvalidPosition()) {
						UIHelper.showError("Invalid position " + demand, reply);
					}
					return;
				}

				if (newPositionDialog != null) {
					Shell shell = Display.getCurrent().getActiveShell();
					if (!newPositionDialog.userAccepts(shell, demandPrev, demand)) {
						return;
					}
				}

				if (demand != demandPrev){
					motorBox.demandBegin(demandPrev);
					final String msg = "Moving " + motor.getDescriptor().getLabelText() + " to " + demand;
					Job job = new Job(msg){
						@Override
						protected void canceling() {
							try {
								MotorPositionViewer.this.scannable.stop();
							} catch (DeviceException e) {
								logger.error("Unable to stop the scannable motor", e);
							}
						}
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								if (callback != null) {
									callback.call(demand);
								}

								else if (commandFormat == null) {
									motor.setPosition(demand);
								}

								else {
									final String commandToRun = String.format(commandFormat, demand);
									InterfaceProvider.getCommandRunner().evaluateCommand(commandToRun);
								}

								demandPrev = demand;
							} catch (DeviceException e) {
								logger.error("Exception: " + msg + " " + e.getMessage(),e);
							}
							refresh();
							return Status.OK_STATUS;
						}
					};
					job.setUser(true);
					job.schedule();
				}
			}
		});
		motorBox.on();

		final IObserver iObserver = new IObserver() {
			@Override
			public void update(final Object source, Object arg) {
				refresh();
			}
		};

		scannable.addIObserver(iObserver);
		motorBox.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				scannable.deleteIObserver(iObserver);
				motorPositionJob.cancel();
			}
		});


		motorPositionJob = new Job("Motor Position " + motor.getDescriptor().getLabelText()) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					final boolean isBusy = motor.isBusy();
					final double position = motor.getPosition();
					parent.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							if (!motorBox.isDisposed()) {
								if (isBusy) {
									motorBox.demandStep(position);
								} else {
									motorBox.demandComplete(position);
								}
							}
						}
					});

					if (isBusy) {
						// wait a short while and then re-run
						schedule(250);
					}
				} catch (DeviceException e) {
					logger.error("Error getting position", e);
				}
				return Status.OK_STATUS;
			}
		};
		refresh();//get initial values
	}


	public void refresh() {
		motorPositionJob.cancel();
		motorPositionJob.schedule();
	}

	public void setFocus() {
		if (motorBox != null) motorBox.setFocus();
	}

	private void createReadbacksGroup(Composite readBacksGroup) {
		if (!motor.getDescriptor().getHideLabel()) {
			Label label = new Label(readBacksGroup, SWT.NONE);
			label.setText( motor.getDescriptor().getLabelText());
			label.setLayoutData(labelLayoutData);
		}
		motorBox = new DemandBox(readBacksGroup, SWT.NONE, 60);
		GridDataFactory.swtDefaults().hint(120, SWT.DEFAULT).applyTo(motorBox);
	}

	@Override
	public String toString() {
		return "MotorPositionViewer for " + scannable.getName();
	}

	/**
	 * Return the underlying demandBox for this viewer
	 * @return DemandBox for this viewer
	 */
	public DemandBox getDemandBox(){
		return motorBox;
	}

	public void setDecimalPlaces(int dp) {
		motorBox.setDecimalPlaces(dp);
		motorBox.setValue(demandPrev);
	}

	public void setEnabled(boolean enabled) {
		if (motorBox != null) motorBox.setEnabled(enabled);
	}
	public boolean isRestoreValueWhenFocusLost() {
		return restoreValueWhenFocusLost;
	}
	public void setRestoreValueWhenFocusLost(boolean restoreValueWhenFocusLost) {
		this.restoreValueWhenFocusLost = restoreValueWhenFocusLost;
		motorBox.setRestoreValueWhenFocusLost(restoreValueWhenFocusLost);
	}

	public IPositionVerifierDialogCreator<Double> getNewPositionDialog() {
		return newPositionDialog;
	}

	public void setNewPositionDialog(IPositionVerifierDialogCreator<Double> newPositionDialog) {
		this.newPositionDialog = newPositionDialog;
	}

	public void setPermanentlyEnabled(boolean enabled) {
		motorBox.setPermanentlyEnabled(enabled);
	}

	public void setEditable(boolean editable) {
		motorBox.setEditable(editable);
	}

	public void dispose() {
		motorBox.dispose();
	}
}
