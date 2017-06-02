/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.rcp.ncd.widgets;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.jython.JythonServerFacade;
import gda.rcp.ncd.Activator;
import gda.rcp.ncd.NcdController;
import uk.ac.gda.util.ThreadManager;

public class NcdScanControlComposite extends Composite {

	protected static final Logger logger = LoggerFactory.getLogger(NcdScanControlComposite.class);
	private Button start;
	private Button stop;

	public NcdScanControlComposite(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new GridLayout(2, true));

		start = new Button(this, SWT.PUSH);
		start.setText("Start");
		start.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		start.addSelectionListener(startListener);
		stop = new Button(this, SWT.PUSH);
		stop.setText("Stop");
		stop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		stop.addSelectionListener(stopListener);
	}

	//Stop running script
	private SelectionListener stopListener = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			logger.info("Stop requested from NCD Button Panel");
			Thread thread = ThreadManager.getThread(NcdScanControlComposite.this::stopScan);
			thread.start();
		}
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	};

	private SelectionListener startListener = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			logger.info("Scan start requested from NcdButtonPanelView");
			startScan();
		}
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
		private void startScan() {
//		FIXME this need to capture the exceptions, but interface does not allow that
			JythonServerFacade.getInstance().runCommand(
					"gda.scan.StaticScan([" + NcdController.getInstance().getNcdDetectorSystem().getName()
					+ "]).runScan()");
		}
	};


	private void stopScan() {
		try {
			NcdController.getInstance().getNcdDetectorSystem().stop();
		} catch (DeviceException de) {
			// Create the required Status object
			final Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error Stopping Tfg", de);
			Display.getDefault().asyncExec(() ->
					ErrorDialog.openError(Display.getDefault().getActiveShell(), "DeviceException", "Error Stopping Tfg", status));
		}
	}
}
