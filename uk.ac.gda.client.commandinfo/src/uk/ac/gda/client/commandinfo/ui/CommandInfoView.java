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

public class CommandInfoView extends ViewPart {

	public static final String ID = "uk.ac.gda.client.commandinfo.ui.CommandInfoView";
	
	private CommandInfoComposite comCommandInfo = null;

	public CommandInfoView() {
	}

	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;

		int style = SWT.FILL;
		comCommandInfo = new CommandInfoComposite(container, style);
		comCommandInfo.configure();
		comCommandInfo.loadInput();

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(comCommandInfo.getViewer().getControl(), "uk.ac.gda.client.commandinfo.viewer");
		hookContextMenu();
		hookDoubleClickAction();
		contributeToolbars();
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
}