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

package uk.ac.gda.client.commandinfo.ui;

import gda.jython.commandinfo.CommandThreadEvent;
import gda.jython.commandinfo.ICommandThreadObserver;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;

import uk.ac.gda.client.commandinfo.CommandInfoController;

public class CommandInfoView extends ViewPart implements ICommandThreadObserver {

	public static final String ID = "uk.ac.gda.client.commandinfo.ui.CommandInfoView";
	
	private CommandInfoController controller = null;
	private CommandInfoComposite comCommandInfo = null;
	private boolean eventsEnabled = false;

	public CommandInfoView() {
	}

	private void configureController() {
		this.controller = CommandInfoController.getInstance();
		this.controller.addCommandThreadObserver(this);
	}

	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;

		int style = SWT.FILL;
		comCommandInfo = new CommandInfoComposite(container, style);
		initialiseData();

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(comCommandInfo.getViewer().getControl(), "uk.ac.gda.client.commandinfo.viewer");
		hookContextMenu();
		hookDoubleClickAction();
		contributeToolbars();
		
	}
	
	private void disableEvents() {
		eventsEnabled = false;
	}
	
	private void enableEvents() {
		eventsEnabled = true;
	}
	
	private void initialiseData() {
		disableEvents();
		if(null==controller) {
			this.configureController();
		}
		comCommandInfo.setInput(controller.getModel());
		enableEvents();
	}

	public void dispose() {
		if (null!=controller) {
			this.controller.deleteCommandThreadObserver(this);
			this.controller = null;
		}
		super.dispose();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				CommandInfoView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(comCommandInfo.getViewer().getControl());
		comCommandInfo.getViewer().getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, comCommandInfo.getViewer());
	}

	private void contributeToolbars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		//manager.add(action1);
		//manager.add(new Separator());
	}

	private void fillContextMenu(IMenuManager manager) {
		//manager.add(action1);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		//manager.add(action1);
	}


	private void hookDoubleClickAction() {
		comCommandInfo.getViewer().addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				//doubleClickAction.run();
			}
		});
	}
	
	@SuppressWarnings("unused")
	private void showMessage(String message) {
		MessageDialog.openInformation(
			comCommandInfo.getViewer().getControl().getShell(),
			"Command info View",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		comCommandInfo.getViewer().getControl().setFocus();
	}

	@Override
	public void update(Object source, Object arg) {
		if (eventsEnabled && arg instanceof CommandThreadEvent) {
			final CommandThreadEvent event = (CommandThreadEvent) arg;
			this.getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					comCommandInfo.update(null,event);
					@SuppressWarnings("unused")
					int debug = 0;
				}
			});	
		}
	}
}