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
import gda.device.EnumPositioner;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.jython.JythonServerFacade;
import gda.rcp.ncd.Activator;
import gda.rcp.ncd.NcdController;
import gda.rcp.ncd.widgets.ShutterGroup;

import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;

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
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import uk.ac.gda.util.ThreadManager;

public class NcdButtonPanelView extends ViewPart {

	private static final Logger logger = LoggerFactory.getLogger(NcdButtonPanelView.class);

	protected Combo titleEntry;
//	protected Text titleEntry;
	private String titleString;
	private Button startButton;
	private Label validationMessage;
	private Button retainTitle;
	private String[] titleList;

	public NcdButtonPanelView() {
		this.titleString = "";
		titleList = new String[] {};
	}

	/**
	 * This class validates the title to ensure no illegal characters are in there (for xml).
	 */
	class TitleValidator implements IInputValidator {
		/**
		 * Validates the String. Returns null for no error, or an error message
		 * 
		 * @param newText
		 *            the String to validate
		 * @return String
		 */
		@Override
		public String isValid(String newText) {
			for (CharSequence ch : new CharSequence[] { ">", "<", "\\" }) {
				if (newText.contains(ch)) {
					return "illegal character";
				}
			}

			// Input must be OK
			return null;
		}
	}

	//run data acquisition with configured frameset
	private SelectionListener startListener = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (retainTitle.getSelection()) {
				try {
					titleString = GDAMetadataProvider.getInstance().getMetadataValue("title");
				} catch (DeviceException e1) {
					logger.error("Could not read metadata", e1);
				}
			} else {
				titleString = "";
			}


			try {
				//only show dialog if empty title
				if (titleEntry.getText().trim().equals("")) {
					InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), "",
							"Enter scan title (e.g. sample information)", "", new TitleValidator());
					if (dlg.open() == Window.OK) {
						// User clicked OK; update the label with the input
						GDAMetadataProvider.getInstance().setMetadataValue("title", dlg.getValue().trim());
					} else {
						// cancel
						//if cancel, delete whitespace
						titleEntry.setText("");
						return;
					}
				}
			
				updateList(titleEntry.getText());
				
				//FIXME this need to capture the exceptions, but interface does not allow that
				JythonServerFacade.getInstance().runCommand(
						"gda.scan.StaticScan([" + NcdController.getInstance().getNcdDetectorSystem().getName()
								+ "]).runScan()");

			} catch (DeviceException de) {
				// Create the required Status object
				Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error Starting Scan", de);

				// Display the dialog
				ErrorDialog.openError(Display.getCurrent().getActiveShell(), "DeviceException", "Error Starting Scan",
						status);
			}
			
			retainTitle.setSelection(false);
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
			Thread thread = ThreadManager.getThread(new Runnable() {
				
				@Override
				public void run() {
					try {
						NcdController.getInstance().getTfg().stop();
					} catch (DeviceException de) {
						// Create the required Status object
						final Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error Stopping Tfg", de);
						
						Display.getDefault().asyncExec(new Runnable() {
							
							@Override
							public void run() {
								// Display the dialog
								ErrorDialog.openError(Display.getDefault().getActiveShell(), "DeviceException", "Error Stopping Tfg", status);
							}
						});
					}
					
				}
			});
			thread.start();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	};

	//Validate title input for xml
	private ModifyListener titleValidateListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			validateTitle();
		}
	};

	@SuppressWarnings("unused")
	@Override
	public void createPartControl(Composite parent) {
		GridLayout gl_parent = new GridLayout(4, false);
		gl_parent.verticalSpacing = 12;
		GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		parent.setLayoutData(gridData);
		parent.setLayout(gl_parent);

		{
			startButton = new Button(parent, SWT.NONE);
			startButton.setText("Start");
			startButton.setToolTipText("Run a data acquisition using the configured frameset");
			startButton.addSelectionListener(startListener);
		}
		{
			Button stopButton = new Button(parent, SWT.NONE);
			stopButton.setText("Stop");
			stopButton.setToolTipText("Stop detectors");
			stopButton.addSelectionListener(stopListener);
		}
		{
			Group titleGroup = new Group(parent, SWT.BORDER);
			GridData gd_titleGroup = new GridData(SWT.FILL, SWT.CENTER, true, false, 1,1);
			titleGroup.setLayoutData(gd_titleGroup);
			GridLayout gl_titleGroup = new GridLayout(2, false);
			titleGroup.setLayout(gl_titleGroup);
			titleGroup.setText("Title");
			{
				titleEntry = new Combo(titleGroup, SWT.NONE);
				GridData gd_titleEntry = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
				titleEntry.setLayoutData(gd_titleEntry);
				titleEntry.setItems(titleList);
				titleEntry.setVisibleItemCount(6);
				titleEntry.setToolTipText("Enter title or choose recent");
				
//				titleEntry = new Text(titleGroup, SWT.BORDER);
//				GridData gd_titleEntry = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
//				titleEntry.setToolTipText("Title for the scan");
//				titleEntry.setLayoutData(gd_titleEntry);
//				titleEntry.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			}
			{
				retainTitle = new Button(titleGroup, SWT.CHECK);
				GridData gd_titleCheck = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
				retainTitle.setLayoutData(gd_titleCheck);
				retainTitle.setToolTipText("Do not clear title after scan");
				retainTitle.setText("Keep?");
			}
		}
		
		ArrayList<Findable> shutters = Finder.getInstance().listAllObjects("EnumPositioner");
		for (Findable shutter : shutters) {
			new ShutterGroup(parent, SWT.NONE, (EnumPositioner) shutter);
		}
		
		try {
			titleString = GDAMetadataProvider.getInstance(true).getMetadataValue("title");
			titleEntry.setText(titleString);
		} catch (DeviceException e) {
			logger.error("Could not read title metadata", e);
		}
		{
			titleEntry.addModifyListener(titleValidateListener );
			titleEntry.addFocusListener(titleUpdateListener);
		}
		{
			validationMessage = new Label(parent, SWT.NONE);
			GridData gd_validationLabel = new GridData(SWT.FILL, SWT.RIGHT, false, false, 4, 1);
			validationMessage.setLayoutData(gd_validationLabel);
			validationMessage.setVisible(false);
			validationMessage.setText("Title cannot contain < > or \\");
		}
		
		new NcdButtonPanelUpdater(this);
	}

	protected void updateList(String text) {
		int selectionIndex = titleEntry.indexOf(text);
		if (selectionIndex != -1) {
			titleEntry.remove(selectionIndex);
		} 
		titleEntry.add(text,0);
		while (titleEntry.getItemCount() > 6) {
			titleEntry.remove(6);
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
	}

	private void validateTitle() {
		int valid = 1;
		for (CharSequence ch : new CharSequence[] { ">", "<", "\\" }) {
			if (titleEntry.getText().contains(ch)) {
				valid = 0;
			} 
		}
		if (valid != 1) {
			validationMessage.setVisible(true);
//			startButton.setEnabled(false);
			startButton.setToolTipText("Title should not contain < > or \\");
		} else {
			validationMessage.setVisible(false);
//			startButton.setEnabled(true);
			startButton.setToolTipText("Run a data acquisition using the configured frameset");
		}
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
	
}