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

package uk.ac.gda.client;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;

public class XYPlotviewMenu extends ContributionItem {

	public XYPlotviewMenu() {
		// TODO Auto-generated constructor stub
	}

	public XYPlotviewMenu(String id) {
		super(id);
	}
	private boolean state;
	@Override
	public void fill(ToolBar parent, int index) {
		super.fill(parent, index);
		final MenuItem menuItem = new MenuItem(parent.getMenu(),SWT.CHECK,index);
		menuItem.setText("Disconnect");
		menuItem.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				state = !state;
				menuItem.setSelection(state);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		menuItem.getMenu().addMenuListener(new MenuAdapter(){

			@Override
			public void menuShown(MenuEvent e) {
				super.menuShown(e);
				menuItem.setSelection(state);
			}
			
		});
	}

	
}
