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

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.metadata.GDAMetadataProvider;
import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.Scannable;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.jython.JythonServerFacade;
import gda.rcp.ncd.Activator;
import gda.rcp.ncd.NcdController;
import gda.rcp.ncd.widgets.ShutterGroup;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.gda.client.UIHelper;

public class NcdButtonPanelView extends ViewPart {

	private static final Logger logger = LoggerFactory.getLogger(NcdButtonPanelView.class);

	private static final String THICKNESS_METADATA = "sample_thickness";

	protected Combo titleEntry;
	private final int historyLength = 6;
	private String titleString;
	protected Text thicknessPositionLabel;
	private Button startButton;
	protected Button retainMetadata;
	private String[] titleList;

	protected Scannable thicknessScannable;

	protected String thicknessString;

	public NcdButtonPanelView() {
		this.titleString = "";
		titleList = new String[] {};
		Findable find = Finder.getInstance().find(THICKNESS_METADATA);
		if (find instanceof ScannableAdapter) {
			thicknessScannable = ((Scannable) find);
		} else {
			logger.error("NCD Acquisition panel could not find thickness scannable");
		}
	}

	//run data acquisition with configured frameset
	private SelectionListener startListener = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			logger.info("Scan start requested from NcdButtonPanelView");
			if (retainMetadata.getSelection()) {
				try {
					thicknessString = thicknessPositionLabel.getText();
					titleString = GDAMetadataProvider.getInstance().getMetadataValue("title");
				} catch (DeviceException e1) {
					logger.error("Could not read metadata", e1);
				}
			} else {
				titleString = "";
			}

//			try {
				//only show dialog if empty title
			boolean needTitle = titleEntry.getText().trim().equals("");
			boolean needThickness = true;
			try {
				needThickness = thicknessScannable.isAt(Double.NaN);
			} catch (DeviceException e1) {
				logger.error("Could not read thickness scannable position - assuming not set", e1);
			}
				if (needTitle || needThickness) {
					Dialog cDlg = new ConfirmTitleAndThicknessDialog(Display.getCurrent().getActiveShell(), needTitle, needThickness);
					cDlg.open();
					int ret = cDlg.getReturnCode();
					if (ret != Window.OK) {
						return;
					}
				} else {
					updateList(titleEntry.getText());
				}

				//FIXME this need to capture the exceptions, but interface does not allow that
				JythonServerFacade.getInstance().runCommand(
						"gda.scan.StaticScan([" + NcdController.getInstance().getNcdDetectorSystem().getName()
								+ "]).runScan()");
		}
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	};

	//Update title metadata when focus is lost
	private FocusListener titleUpdateListener = new FocusListener() {
		@Override
		public void focusGained(FocusEvent e) {
		}

		@Override
		public void focusLost(FocusEvent e) {
			try {
				GDAMetadataProvider.getInstance(true).setMetadataValue("title", titleEntry.getText().trim());
			} catch (DeviceException e1) {
				logger.error("Could not set scan title from NcdButtonPanel", e1);
			}
		}


	};

	//Stop running script
	private SelectionListener stopListener = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			logger.info("Stop requested from NCD Button Panel");
			Async.execute(() -> {
					try {
						NcdController.getInstance().getNcdDetectorSystem().stop();
					} catch (DeviceException de) {
						// Create the required Status object
						final Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error Stopping Tfg", de);

						Display.getDefault().asyncExec(() -> {
								// Display the dialog
								ErrorDialog.openError(Display.getDefault().getActiveShell(), "DeviceException", "Error Stopping Tfg", status);
						});
					}
			});
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	};

	@SuppressWarnings("unused")
	@Override
	public void createPartControl(Composite parent) {
		GridLayout gl_parent = new GridLayout(3, false);
		gl_parent.verticalSpacing = 12;
		GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 2);
		parent.setLayoutData(gridData);
		parent.setLayout(gl_parent);

		{
			startButton = new Button(parent, SWT.NONE);
			startButton.setText("Start");
			startButton.setToolTipText("Run a data acquisition using the configured frameset");
			startButton.addSelectionListener(startListener);
		}
		{
			Composite metadata = new Composite(parent, SWT.NONE);
			metadata.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1,3));
			metadata.setLayout(new GridLayout(2, false));

			{
				Label titleLabel = new Label(metadata, SWT.NONE);
				titleLabel.setText("Title");

				titleEntry = new Combo(metadata, SWT.NONE);
				GridData gd_titleEntry = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
				titleEntry.setLayoutData(gd_titleEntry);
				titleEntry.setItems(titleList);
				titleEntry.setVisibleItemCount(6);
				titleEntry.setToolTipText("Enter title or choose recent");
				titleEntry.addFocusListener(titleUpdateListener);
			}
			{
				Label thicknessLabel = new Label(metadata, SWT.NONE);
				thicknessLabel.setText("Thickness (mm)");
				try {
					GridData gd_thicknessEntry = new GridData(SWT.FILL, SWT.CENTER, true, false, 1,1);
					thicknessPositionLabel = new Text(metadata, SWT.SINGLE | SWT.BORDER);
					thicknessPositionLabel.setLayoutData(gd_thicknessEntry);
					thicknessPositionLabel.setText(String.valueOf(thicknessScannable.getPosition()));
					thicknessPositionLabel.addKeyListener(new KeyListener() {

						@Override
						public void keyPressed(KeyEvent e) {
							if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
								String newPosition = thicknessPositionLabel.getText();
								try {
									if (newPosition.isEmpty()) {
										thicknessScannable.moveTo(null);
										return;
									}
									Double doublePosition = Double.valueOf(newPosition);
									thicknessScannable.moveTo(doublePosition);
								} catch (NumberFormatException e1) {
									try {
										thicknessScannable.moveTo(newPosition);
									} catch (DeviceException e2) {
										thicknessPositionLabel.setText(newPosition);
									}
								} catch (DeviceException e1) {
									try {
										thicknessPositionLabel.setText(String.valueOf(thicknessScannable.getPosition()));
									} catch (DeviceException e2) {
									}
								}
								thicknessPositionLabel.setForeground(Display.getDefault().getSystemColor(0));
							} else {
								thicknessPositionLabel.setForeground(Display.getDefault().getSystemColor(3));
							}

						}
						@Override
						public void keyReleased(KeyEvent e) {
						}
					});
				} catch (Exception e) {
					logger.error("Could not create thickness editor", e);
				}
			}
			{
				retainMetadata = new Button(metadata, SWT.CHECK);
				GridData gd_titleCheck = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 2);
				retainMetadata.setLayoutData(gd_titleCheck);
				retainMetadata.setToolTipText("Do not clear title/thickness after scan");
				retainMetadata.setText("Keep?");
			}
		}

		Composite shutterComp = new Composite(parent, SWT.NONE);
		shutterComp.setLayout(new GridLayout(1, false));
		shutterComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1,2));
		List<Findable> shutters = Finder.getInstance().listAllObjects("EnumPositioner");
		for (Findable shutter : shutters) {
			new ShutterGroup(shutterComp, SWT.NONE, (EnumPositioner) shutter);
		}

		{
			Button stopButton = new Button(parent, SWT.NONE);
			stopButton.setText("Stop");
			stopButton.setToolTipText("Stop detectors");
			stopButton.addSelectionListener(stopListener);
		}
		try {
			titleString = GDAMetadataProvider.getInstance(true).getMetadataValue("title");
			titleEntry.setText(titleString);
		} catch (DeviceException e) {
			logger.error("Could not read title metadata", e);
		}

		new NcdButtonPanelUpdater(this);
	}

	protected void updateList(String text) {
		if (!text.equals("")) {
			int selectionIndex = titleEntry.indexOf(text);
			if (selectionIndex != -1) {
				titleEntry.remove(selectionIndex);
			}
			titleEntry.add(text,0);
			while (titleEntry.getItemCount() > historyLength) {
				titleEntry.remove(historyLength);
			}
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
	}

	public String getTitleString() {
		return titleString;
	}

	public void setTitleString(String title) {
		this.titleString = title;
	}

	public boolean hasTitleString() {
		if (titleString.equals("")) {
			return false;
		}
		return true;
	}

	private class ConfirmTitleAndThicknessDialog extends Dialog {
		private boolean title, thickness;
		private Text titleText;
		private Text thicknessText;
		protected ConfirmTitleAndThicknessDialog(Shell parentShell, boolean showTitle, boolean showThickness) {
			super(parentShell);
			title = showTitle;
			thickness = showThickness;
		}
		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("Enter title/sample thickness");
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Composite comp = (Composite) super.createDialogArea(parent);
			GridLayout layout = (GridLayout)comp.getLayout();
			layout.numColumns = 2;

			if (title) {
				Label titleLabel = new Label(comp, SWT.NONE);
				titleLabel.setText("Scan Title");
				titleText = new Text(comp,  SWT.SINGLE | SWT.BORDER);
				titleText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
			}
			if (thickness) {
				Label thicknessLabel = new Label(comp, SWT.NONE);
				thicknessLabel.setText("Sample Thickness");
				thicknessText = new Text(comp,  SWT.SINGLE | SWT.BORDER);
				thicknessText.setText(thicknessPositionLabel.getText());
				thicknessText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
			}
			return comp;
		}
		@Override
		protected void okPressed() {
			if (title) {
				try {
					GDAMetadataProvider.getInstance(true).setMetadataValue("title", titleText.getText().trim());
				} catch (DeviceException e) {
					UIHelper.showError("Could not set title metadata", e.getLocalizedMessage());
					logger.error("Could not set title metadata", e);
				}
			}
			if (thickness) {
				try {
					thicknessScannable.moveTo(Double.valueOf(thicknessText.getText()));
				} catch (DeviceException e) {
					UIHelper.showError("Could not set Sample Thickness", e.getLocalizedMessage());
					throw new IllegalStateException();
				}
			}
			super.okPressed();
		}
	}
}
