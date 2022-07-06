/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

/**
 * Simple action which will have other actions in a drop down menu.
 */
public class MenuAction extends Action implements IMenuCreator {
	
	private Menu fMenu;
	private List<IAction> actions;

	public MenuAction(final String text) {
		super(text, IAction.AS_DROP_DOWN_MENU);
		setMenuCreator(this);
		this.actions = new ArrayList<IAction>(7);
	}


	@Override
	public void dispose() {
		if (fMenu != null)  {
			fMenu.dispose();
			fMenu= null;
		}
	}


	@Override
	public Menu getMenu(Menu parent) {
		return null;
	}

	public void add(final IAction action) {
		actions.add(action);
	}

	@Override
	public Menu getMenu(Control parent) {
		if (fMenu != null) fMenu.dispose();

		fMenu= new Menu(parent);

		for (IAction action : actions) {
			addActionToMenu(fMenu, action);
		}

		return fMenu;
	}


	protected void addActionToMenu(Menu parent, IAction action) {
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	@Override
	public void run() {

	}


	/**
	 * Get's rid of the menu, because the menu hangs on to * the searches, etc.
	 */
	void clear() {
		dispose();
	}
}