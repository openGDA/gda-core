/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.rcp.ncd.views;

import java.util.List;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.omg.CORBA.SystemException;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.syringepump.Syringe;
import gda.factory.Finder;
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;
import gda.rcp.ncd.Activator;
import uk.ac.gda.client.UIHelper;

public class SyringeControlView extends ViewPart implements IObserver {
	public static final String ID = "gda.rcp.ncd.views.SyringeControlView";

	private static final Logger logger = LoggerFactory.getLogger(SyringeControlView.class);
	private List<Syringe> pumps;
	private Syringe current;
	private ProgressBar bar;
	private Combo pumpChoice;

	private Label statusLabel;

	private Button stopButton;

	private Label volumeRemainingLabel;
	private Text volumeRemaining;

	private Label timeToRunLabel;
	private Text timeToRun;

	private Text infuseVolume;
	private Button infuse;

	private String stopAction;

	private Runnable updater = new Runnable() {
		@Override
		public void run() {
			update();
		}
	};
	private SelectionListener pumpSelection = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			update();
		}
	};
	private SelectionListener stopListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (current != null) {
				try {
					current.stop();
					if (stopAction != null) {
						String command = stopAction;
						command = command.replace("{syringe}", current.getName());
						try {
							JythonServerFacade.getInstance().runCommand(command);
						} catch (SystemException se) {
							UIHelper.showError("Error running stop command\n" + command, se.getMessage());
							logger.error("Error running stop command - {}", se.getMessage());
						}
					}
				} catch (Throwable t) {
					logger.error("Error stopping the syringe", t);
					UIHelper.showError("Error stopping the syringe", t.getMessage());
				}

			}
		}
	};

	private SelectionListener postStopActionSelection = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			InputDialog newStopAction = new InputDialog(Display.getDefault().getShells()[0], "Stop Action",
					"Enter new stop action (syringe will always be stopped first)", stopAction, null);
			newStopAction.open();
			if (newStopAction.getReturnCode() == Window.OK) {
				stopAction = newStopAction.getValue();
				Preferences preferences = InstanceScope.INSTANCE.getNode(ID);
				Preferences pref = preferences.node("stopCommand");
				pref.put("stopCommand", stopAction);
				try {
					pref.flush();
					preferences.flush();
				} catch (BackingStoreException bse) {
				}
			}
		}
	};

	private SelectionListener infuseListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (current != null) {
				try {
					final double volume = Double.valueOf(infuseVolume.getText());
					SafeRunner.run(new ISafeRunnable() {
						@Override
						public void run() {
							try {
								logger.debug("Infusing {} from syringe {}", volume, current.getName());
								current.infuse(volume);
							} catch (DeviceException de) {
								logger.error("Can't infuse from syringe '{}'", current.getName(), de);
								UIHelper.showError("Can't infuse from syringe '" + current.getName() + "'", de.getMessage());
							}
						}

						@Override
						public void handleException(Throwable exception) {
							logger.error("Can't infuse from syringe '{}'", current.getName(), exception);
							UIHelper.showError("Can't infuse from syringe '" + current.getName() + "'", exception.getMessage());
						}
					});
				} catch (NumberFormatException nfe) {
					logger.error("Can't infuse volume '{}'", infuseVolume.getText());
					UIHelper.showError("Can't infuse", infuseVolume.getText() + " is not a valid volume");
				}
			} else {
				UIHelper.showError("Can't infuse", "No syringe selected");
			}
		}
	};

	public SyringeControlView() {
		pumps = Finder.getInstance().listFindablesOfType(Syringe.class);
	}

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(new GridLayout(5, false));
		statusLabel = new Label(parent, SWT.NONE);
		statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		bar = new ProgressBar(parent, SWT.SMOOTH | SWT.HORIZONTAL);
		bar.setMaximum(100);
		bar.setMinimum(0);
		bar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		pumpChoice = new Combo(parent, SWT.DROP_DOWN | SWT.SINGLE | SWT.READ_ONLY);
		pumpChoice.addSelectionListener(pumpSelection);
		pumpChoice.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));

		volumeRemainingLabel = new Label(parent, SWT.NONE);
		volumeRemainingLabel.setText("Volume remaining");
		volumeRemaining = new Text(parent, SWT.SINGLE | SWT.READ_ONLY);
		volumeRemaining.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 2, 1));

		infuseVolume = new Text(parent, SWT.SINGLE | SWT.BORDER);
		infuseVolume.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));

		timeToRunLabel = new Label(parent, SWT.NONE);
		timeToRunLabel.setText("Time remaining");
		timeToRun = new Text(parent, SWT.SINGLE | SWT.READ_ONLY);
		timeToRun.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 2, 1));

		infuse = new Button(parent, SWT.PUSH);
		infuse.setText("Infuse");
		infuse.addSelectionListener(infuseListener);
		infuse.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 1, 1));

		stopButton = new Button(parent, SWT.PUSH);
		stopButton.setToolTipText("Stop syringe and run post stop action");
		stopButton.setImage(Activator.getImageDescriptor("icons/stop.png").createImage());
		stopButton.addSelectionListener(stopListener);

		if (pumps.isEmpty()) {
			pumpChoice.setText("No syringe pumps found");
			pumpChoice.setEnabled(false);
		} else {
			for (Syringe sp : pumps) {
				pumpChoice.add(sp.getName());
			}
			pumpChoice.select(0);
			update();
		}
		Preferences preferences = InstanceScope.INSTANCE.getNode(ID);
		Preferences pref = preferences.node("stopCommand");
		stopAction = pref.get("stopCommand", "");
	}

	private void update() {
		if (current != null) {
			current.deleteIObserver(this);
		}
		Syringe sp = pumps.get(pumpChoice.getSelectionIndex());
		current = sp;
		current.addIObserver(this);
		if (current.isEnabled()) {
			bar.setEnabled(true);
			bar.setMaximum((int) sp.getCapacity());
			try {
				double vol = current.getVolume();
				volumeRemaining.setText(String.format("%.4f", vol));
				timeToRun.setText(String.format("%.4f", current.getRemainingTime()));
				bar.setSelection((int) vol);
				infuse.setEnabled(true);
				stopButton.setEnabled(true);
				infuseVolume.setEnabled(true);
				statusLabel.setText("");
			} catch (DeviceException e) {
				// c'est la vie
			}
		} else {
			bar.setEnabled(false);
			volumeRemaining.setText("n/a");
			timeToRun.setText("n/a");
			bar.setSelection(0);
			infuse.setEnabled(false);
			stopButton.setEnabled(false);
			infuseVolume.setEnabled(false);
			statusLabel.setText("Syringe is not enabled");
		}
	}

	public void refill() {
		InputDialog newVolume = new InputDialog(Display.getDefault().getShells()[0], "New Volume", "Enter new volume", "", null);
		newVolume.open();
		if (newVolume.getReturnCode() == Window.OK) {
			try {
				current.setVolume(Double.valueOf(newVolume.getValue()));
				update();
			} catch (NumberFormatException nfe) {
				UIHelper.showError("Invalid volume", "Volume must be numeric value");
			} catch (DeviceException de) {
				UIHelper.showError("Could not set volume", de.getMessage());
				logger.error("Could not set volume", de);
			}
		}
	}

	public void setStop() {
		postStopActionSelection.widgetSelected(null);
	}

	@Override
	public void setFocus() {
		if (!pumps.isEmpty() && current != null) {
			pumpChoice.select(pumps.indexOf(current));
		}
	}

	@Override
	public void update(Object source, Object arg) {
		if (source == current) {
			Display.getDefault().asyncExec(updater);
		}
	}

	@Override
	public void dispose() {
		if (current != null) {
			current.deleteIObserver(this);
		}
		super.dispose();
	}

}
