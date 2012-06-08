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

package gda.rcp.views;

import gda.jython.JythonServerFacade;
import gda.rcp.GDAClientActivator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.swtdesigner.ResourceManager;

public class ScriptScanControllerView extends ViewPart {

	public static final String ID = "gda.rcp.ScriptScanController"; //$NON-NLS-1$

	/**
	 * Create contents of the view part
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		container.setLayout(gridLayout);

		final Button pauseButton = new Button(container, SWT.NONE);
		pauseButton.setImage(ResourceManager.getPluginImage(GDAClientActivator.getDefault(), "icons/control_pause_blue.png"));
		pauseButton.setText("Pause");
		pauseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				JythonServerFacade.getInstance().pauseCurrentScan();
			}
		});

		final Button resumeButton = new Button(container, SWT.NONE);
		resumeButton.setImage(ResourceManager.getPluginImage(GDAClientActivator.getDefault(), "icons/control_play_blue.png"));
		resumeButton.setText("Resume");

		final Button haltButton = new Button(container, SWT.NONE);
		haltButton.setImage(ResourceManager.getPluginImage(GDAClientActivator.getDefault(), "icons/delete.png"));
		haltButton.setText("Halt");

		final Button stopButton = new Button(container, SWT.NONE);
		stopButton.setImage(ResourceManager.getPluginImage(GDAClientActivator.getDefault(), "icons/control_stop_blue.png"));
		stopButton.setText("Stop");
		//
		createActions();
		initializeToolBar();
		initializeMenu();
	}

	/**
	 * Create the actions
	 */
	private void createActions() {
	}

	/**
	 * Initialize the toolbar
	 */
	private void initializeToolBar() {
		getViewSite().getActionBars().getToolBarManager();
	}

	/**
	 * Initialize the menu
	 */
	private void initializeMenu() {
		getViewSite().getActionBars().getMenuManager();
	}

	@Override
	public void setFocus() {
	}

}
