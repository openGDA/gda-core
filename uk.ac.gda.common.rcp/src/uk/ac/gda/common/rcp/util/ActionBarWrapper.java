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

package uk.ac.gda.common.rcp.util;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.SubActionBars2;

public class ActionBarWrapper extends SubActionBars2 {


	private IToolBarManager    alternativeToolbarManager;
	private IMenuManager       alternativeMenuManager;
	private IStatusLineManager alternativeStatusManager;

	/**
	 * alternatives may be null.
	 * @param alternativeToolbarManager
	 * @param alternativeMenuManager
	 * @param alternativeStatusManager
	 * @param parent
	 */
	public ActionBarWrapper(final IToolBarManager    alternativeToolbarManager,
			                final IMenuManager       alternativeMenuManager,
			                final IStatusLineManager alternativeStatusManager,
			                IActionBars2 parent) {
		super(parent);
		this.alternativeToolbarManager = alternativeToolbarManager;
		this.alternativeMenuManager    = alternativeMenuManager;
		this.alternativeStatusManager  = alternativeStatusManager;
	}

	@Override
	public IMenuManager getMenuManager() {
		if (alternativeMenuManager!=null) return alternativeMenuManager;
		return super.getMenuManager();
	}

	/**
	 * Returns the status line manager. If items are added or removed from the
	 * manager be sure to call <code>updateActionBars</code>.
	 * 
	 * @return the status line manager
	 */
	@Override
	public IStatusLineManager getStatusLineManager() {
		if (alternativeStatusManager!=null) return alternativeStatusManager;
		return super.getStatusLineManager();
	}

	/**
	 * Returns the tool bar manager. If items are added or removed from the
	 * manager be sure to call <code>updateActionBars</code>.
	 * 
	 * @return the tool bar manager
	 */
	@Override
	public IToolBarManager getToolBarManager() {
		if (alternativeToolbarManager!=null) return alternativeToolbarManager;
		return super.getToolBarManager();
	}

}
