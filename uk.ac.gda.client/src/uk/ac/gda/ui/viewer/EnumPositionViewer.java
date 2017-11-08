/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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
import org.eclipse.richbeans.widgets.wrappers.ComboWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.jython.InterfaceProvider;
import gda.observable.IObserver;
import uk.ac.gda.ui.internal.viewer.EnumPositionerSource;

/**
 * MotorPositionViewer for EnumPositioner scannables
 * <p>
 *
 * @see MotorPositionViewer
 */
public class EnumPositionViewer {
	private static final Logger logger = LoggerFactory.getLogger(MotorPositionViewer.class);

	private EnumPositionerSource motor;

	private ComboWrapper motorBox;
	private Composite parent;

	private EnumPositioner scannable;
	private volatile String demandPrev;

	private String commandFormat;

	private Job motorPositionJob;

	private IPositionVerifierDialogCreator<String> newPositionDialog;

	private Object labelLayoutData;

	protected Job job;

	private boolean controlEnabled = true;

	public EnumPositionViewer(Composite parent, EnumPositioner scannable) {
		this(parent, scannable, null, false, null);
	}

	public EnumPositionViewer(Composite parent, EnumPositioner scannable, String label) {
		this(parent, scannable, label, false, null);
	}

	public EnumPositionViewer(Composite parent, EnumPositioner scannable, String label, boolean hideLabel) {
		this(parent, scannable, label, hideLabel, null);
	}

	public EnumPositionViewer(Composite parent, EnumPositioner scannable, String label, boolean hideLabel,
			Object labelLayoutData) {
		this.scannable = scannable;
		this.labelLayoutData = labelLayoutData;
		if (this.labelLayoutData == null) {
			this.labelLayoutData = GridDataFactory.swtDefaults().create();
		}
		EnumPositionerSource positionSource = new EnumPositionerSource(scannable);

		positionSource.setLabel(label);
		positionSource.setHideLabel(hideLabel);
		motor = positionSource;
		this.parent = parent;
		createControls(parent);
	}

	public void setCommandFormat(String commandFormat) {
		this.commandFormat = commandFormat;
	}

	private void setDemandPrev() {
		try {
			demandPrev = motor.getPosition();
		} catch (DeviceException e1) {
			logger.error("Error setting current value of demandBox", e1);
		}
	}

	private void createControls(Composite comp) {
		createReadbacksGroup(comp);
		setDemandPrev();
		fillCombo();

		motorBox.addValueListener(new ValueAdapter("MotorPositionviewer") {

			@Override
			public void valueChangePerformed(ValueEvent e) {
				final int demandIndex = motorBox.getSelectionIndex();
				final String demandString = motorBox.getItems()[demandIndex];
				setDemandPrev();

				String reply = null;
				try {
					reply = scannable.checkPositionValid(demandString);
				} catch (DeviceException e1) {
					logger.error("Unable to check the validity of the input position , no move will be performed", e1);
					motorBox.setValue(demandPrev);
					return;
				}
				if (reply != null) {
					logger.error("Invalid position " + reply);
					motorBox.setValue(demandPrev);
					return;
				}

				if (newPositionDialog != null) {
					Shell shell = Display.getCurrent().getActiveShell();
					if (!newPositionDialog.userAccepts(shell, demandPrev, demandString)) {
						return;
					}
				}

				if (!demandString.equals(demandPrev) && (job == null || job.getState() == Job.NONE)) {
					demandPrev = demandString;
					final String msg = "Moving " + motor.getDescriptor().getLabelText() + " to " + demandString;
					job = new Job(msg) {
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
									@Override
									public void run() {
										motorBox.setEnabled(false);
									}
								});

								if (commandFormat == null) {
									motor.setPosition(demandString);
								}

								else {
									final String commandToRun = String.format(commandFormat, demandString);
									InterfaceProvider.getCommandRunner().evaluateCommand(commandToRun);
								}

								return Status.OK_STATUS;
							} catch (DeviceException e) {
								logger.error(msg, e);
								return new Status(IStatus.ERROR, "uk.ac.gda.client", e.getMessage());
							} finally {
								PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
									@Override
									public void run() {
										motorBox.setEnabled(controlEnabled);
									}
								});
								refresh();
							}
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

		motorPositionJob = new Job("Combo Updater") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					final boolean isBusy = motor.isBusy();
					final String position = motor.getPosition();
					parent.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							if (!motorBox.isDisposed()) {
								if (isBusy) {
									motorBox.demandStep();
								} else {
									motorBox.demandComplete(position);
									motorBox.setEnabled(controlEnabled);
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
				} finally {
					parent.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							motorBox.setEnabled(controlEnabled);
						}
					});
				}
				return Status.OK_STATUS;
			}
		};
		refresh();// get initial values
	}

	private void fillCombo() {
		try {
			motorBox.setItems(motor.getPositions());
		} catch (DeviceException e) {
			logger.error("Could not get positions from {}", motor.getUnit(), e);
			motorBox.setItems(new String[] { "Not connected!" });
		}
	}

	public void refresh() {
		motorPositionJob.cancel();
		motorPositionJob.schedule();
	}

	public void setFocus() {
		if (motorBox != null)
			motorBox.setFocus();
	}

	private void createReadbacksGroup(Composite readBacksGroup) {
		if (!motor.getDescriptor().getHideLabel()) {
			Label label = new Label(readBacksGroup, SWT.NONE);
			label.setText(motor.getDescriptor().getLabelText());
			label.setLayoutData(labelLayoutData);
		}
		motorBox = new ComboWrapper(readBacksGroup, SWT.NONE);
		GridDataFactory.swtDefaults().hint(120, SWT.DEFAULT).applyTo(motorBox);
	}

	@Override
	public String toString() {
		return "MotorPositionViewer for " + scannable.getName();
	}

	/**
	 * Return the underlying demandBox for this viewer
	 *
	 * @return DemandBox for this viewer
	 */
	public ComboWrapper getComboWrapper() {
		return motorBox;
	}

	public void setEnabled(boolean enabled) {
		controlEnabled = enabled;
		if (motorBox != null)
			motorBox.setEnabled(enabled);
	}

	public IPositionVerifierDialogCreator<String> getNewPositionDialog() {
		return newPositionDialog;
	}

	public void setNewPositionDialog(IPositionVerifierDialogCreator<String> newPositionDialog) {
		this.newPositionDialog = newPositionDialog;
	}
}
