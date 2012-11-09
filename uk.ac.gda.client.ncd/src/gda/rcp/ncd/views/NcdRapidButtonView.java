/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

import gda.data.metadata.GDAMetadataProvider;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.TimerStatus;
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;
import gda.rcp.ncd.NcdController;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NcdRapidButtonView extends ViewPart implements IObserver {
	public static final String ID = "gda.rcp.ncd.views.NcdRapidButtonView"; //$NON-NLS-1$
	private static final Logger logger = LoggerFactory.getLogger(NcdRapidButtonView.class);
    private Button startButton;
    private Button clearButton;
	private Button haltButton;
	private Button restartButton;
	private Button outputButton;
	private NcdController ncdController = NcdController.getInstance();
	protected static boolean saved = true;
	protected static boolean cleared = false;
	private SaveDataDialog saveDataDialog;
	private ClearDataDialog clearDataDialog;
	private Shell shell;
	private Boolean oldIdle = null;
	private Timer tfg = null;

    public NcdRapidButtonView() {
        super();
		tfg = NcdController.getInstance().getTfg();
		if (tfg != null) {
			tfg.addIObserver(this);
		}
    }

    @Override
	public void createPartControl(Composite parent) {
    	shell = parent.getShell();
    	saveDataDialog = new SaveDataDialog(shell);
    	clearDataDialog = new ClearDataDialog(shell);
    	
		FormLayout parentLayout = new FormLayout();
		parent.setLayout(parentLayout);

		startButton = new Button(parent, SWT.PUSH | SWT.CENTER);
		{
			FormData formData = new FormData();
			formData.top = new FormAttachment(0, 15);
			formData.left = new FormAttachment(0, 20);
			startButton.setLayoutData(formData);
		}
		startButton.setText("Start");
		startButton.setToolTipText("Start data collection");
		startButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				if (!saved) {
					saveDataDialog.open();
					if (saveDataDialog.isSaveRequired()) {
						outputClick();
					} else {
						if (saveDataDialog.isClearRequired()) {
							try {
								logger.info("Clearing memory");
								ncdController.getNcdDetectorSystem().clear();
								cleared = true;
							} catch (DeviceException de) {
								logger.error("DeviceException " + de);
								MessageDialog.openError(shell, "DeviceException", de.getMessage());
							}
						} else {
							cleared = true;
						}
					}
				}
				if (!cleared) {
					clearDataDialog.open();
					if (clearDataDialog.isClearRequired()) {
						try {
							logger.info("Clearing memory");
							ncdController.getNcdDetectorSystem().clear();
							cleared = true;
						} catch (DeviceException de) {
							logger.error("DeviceException " + de);
							MessageDialog.openError(shell, "DeviceException", de.getMessage());
						}
					}
				}	
				saved = false;

				try {
					ncdController.getNcdDetectorSystem().start();
					enableCollection();
					cleared = false;
				} catch (DeviceException de) {
					logger.error("DeviceException " + de); //$NON-NLS-1$
					MessageDialog.openError(shell, "DeviceException", de.getMessage());
				}
			}
		});

		clearButton = new Button(parent, SWT.PUSH | SWT.CENTER);
		{
			FormData formData = new FormData();
			formData.top = new FormAttachment(startButton, 0, SWT.TOP);
			formData.left = new FormAttachment(startButton, 15);
			clearButton.setLayoutData(formData);
		}
		clearButton.setText("Clear");
		clearButton.setToolTipText("Clear Memory");
		clearButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				if (!saved) {
					saveDataDialog.open();
					if (saveDataDialog.isSaveRequired()) {
						outputClick();
					}
				}
				try {
					ncdController.getNcdDetectorSystem().clear();
				} catch (DeviceException de) {
					logger.error("DeviceException " + de); //$NON-NLS-1$
					MessageDialog.openError(shell, "DeviceException", de.getMessage());
				}
				saved = true;
			}
		});

		haltButton = new Button(parent, SWT.PUSH | SWT.CENTER);
		{
			FormData formData = new FormData();
			formData.top = new FormAttachment(clearButton, 0, SWT.TOP);
			formData.left = new FormAttachment(clearButton, 15);
			haltButton.setLayoutData(formData);
		}
		haltButton.setText("Halt");
		haltButton.setToolTipText("Halt data collection");
		haltButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				try {
					ncdController.getNcdDetectorSystem().stop();
				} catch (DeviceException de) {
					logger.error("DeviceException " + de); //$NON-NLS-1$
					MessageDialog.openError(shell, "DeviceException", de.getMessage());
				}
			}
		});

		restartButton = new Button(parent, SWT.PUSH | SWT.CENTER);
		{
			FormData formData = new FormData();
			formData.top = new FormAttachment(haltButton, 0, SWT.TOP);
			formData.left = new FormAttachment(haltButton, 15);
			restartButton.setLayoutData(formData);
		}
		restartButton.setText("Restart");
		restartButton.setToolTipText("Restart data collection");
		restartButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				try {
					tfg.restart();
				} catch (DeviceException de) {
					logger.error("DeviceException during readout ", de); //$NON-NLS-1$
					MessageDialog.openError(shell, "DeviceException", de.getMessage());
				}
			}
		});

		outputButton = new Button(parent, SWT.PUSH | SWT.CENTER);
		{
			FormData formData = new FormData();
			formData.top = new FormAttachment(restartButton, 0, SWT.TOP);
			formData.left = new FormAttachment(restartButton, 15);
			outputButton.setLayoutData(formData);
		}
		outputButton.setText("Output");
		outputButton.setToolTipText("Output data to file");
		outputButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				outputClick();
			}
		});
    }

    @Override
	public void setFocus() {
    }
    
    @Override
	public void dispose() {
        super.dispose();
    }
    
	/**
	 * Disable control panel
	 */
	public void disableCollection() {
		startButton.setEnabled(false);
		clearButton.setEnabled(false);
		outputButton.setEnabled(false);
		haltButton.setEnabled(false);
		restartButton.setEnabled(false);
	}

	/**
	 * Change control panel appropriate for data collection complete
	 */
	public void collectionComplete() {
		startButton.setEnabled(true);
		clearButton.setEnabled(true);
		outputButton.setEnabled(true);
		haltButton.setEnabled(false);
		restartButton.setEnabled(false);
	}

	/**
	 * Change control panel appropriate for data collection active
	 */
	public void enableCollection() {
		startButton.setEnabled(false);
		clearButton.setEnabled(false);
		outputButton.setEnabled(false);
		haltButton.setEnabled(true);
		restartButton.setEnabled(true);
	}

	/**
	 * Change control panel appropriate for data output complete
	 */
	public void outputComplete() {
		startButton.setEnabled(true);
		clearButton.setEnabled(true);
		outputButton.setEnabled(true);
		haltButton.setEnabled(false);
		restartButton.setEnabled(true);
	}

	/**
	 * Change control panel appropriate for data output
	 */
	public void outputStart() {
		startButton.setEnabled(false);
		clearButton.setEnabled(false);
		outputButton.setEnabled(false);
		haltButton.setEnabled(false);
		restartButton.setEnabled(false);
	}
	
	private void outputClick() {
		String title = null;
		outputStart();
		InputDialog inputDialog = new InputDialog(shell, "Enter Title", "Title: ", "", null);
		// check if user pressed cancel
		if (inputDialog.open() == IDialogConstants.OK_ID) {
			if ((title = inputDialog.getValue()) != null) {
				// get rid of the worst characters, there may be more...
				title = title.replace("'", " ");
				title = title.replace("\\", " ");

				try {					
					GDAMetadataProvider.getInstance(true).setMetadataValue("title", title.trim());
					JythonServerFacade.getInstance().runCommand("gda.scan.StaticScanNoCollection(["+ncdController.getNcdDetectorSystem().getName()+"]).runScan()");
				} catch (DeviceException de) {
					logger.error("DeviceException during readout ", de); //$NON-NLS-1$
					MessageDialog.openError(shell, "DeviceException", de.getMessage());
				}
				saved = true;
			}
		}
		outputComplete();
	}

	@Override
	public void update(Object iObservable, Object arg) {
		if (arg != null && arg instanceof TimerStatus) {
			Boolean newIdle = ((TimerStatus) arg).getCurrentStatus().equals("IDLE");
			if (!newIdle.equals(oldIdle)) {
				if (newIdle) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							Display.getCurrent().beep();
							collectionComplete();

						}
					});
				} else {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							enableCollection();

						}
					});
				}
				oldIdle = newIdle;
			}
		}
	}

}
