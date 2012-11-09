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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

import uk.ac.gda.util.ThreadManager;

public class NcdButtonPanelView extends ViewPart {

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

	private SelectionListener startListener = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			try {
				String text = GDAMetadataProvider.getInstance(true).getMetadataValue("title");
				InputDialog dlg = new InputDialog(Display.getCurrent().getActiveShell(), "",
						"Enter scan title (e.g. sample information)", text, new TitleValidator());
				if (dlg.open() == Window.OK) {
					// User clicked OK; update the label with the input
					GDAMetadataProvider.getInstance().setMetadataValue("title", dlg.getValue().trim());
				} else {
					// cancel
					return;
				}
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
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	};

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

	public NcdButtonPanelView() {
	}

	@SuppressWarnings("unused")
	@Override
	public void createPartControl(Composite parent) {
		RowLayout rl_parent = new RowLayout(SWT.HORIZONTAL);
		rl_parent.spacing = 5;
		rl_parent.marginTop = 5;
		rl_parent.marginRight = 5;
		rl_parent.marginLeft = 5;
		rl_parent.marginBottom = 5;
		rl_parent.pack = false;
		rl_parent.fill = true;
		rl_parent.center = true;
		parent.setLayout(rl_parent);

		Button startButton = new Button(parent, SWT.NONE);
		startButton.setText("Start");
		startButton.setToolTipText("Run a data acquisition using the configured frameset");
		startButton.addSelectionListener(startListener);

		Button stopButton = new Button(parent, SWT.NONE);
		stopButton.setText("Stop");
		stopButton.setToolTipText("Stop detectors");
		stopButton.addSelectionListener(stopListener);

		ArrayList<Findable> shutters = Finder.getInstance().listAllObjects("EnumPositioner");
		for (Findable shutter : shutters) {
			new ShutterGroup(parent, SWT.NONE, (EnumPositioner) shutter);
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
	}
}