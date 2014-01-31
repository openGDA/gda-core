/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.diamond.tomography.reconstruction.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import uk.ac.diamond.tomography.reconstruction.INexusPathProvider;

public class NexusFilterAction extends Action implements IMenuCreator {

	private Menu dropDownMenu;
	private INexusPathProvider filterPathProvider;
	
	public NexusFilterAction(INexusPathProvider filterPathProvider) {
		setText("Nexus Filter");
		setToolTipText("Filter nexus files");
		setEnabled(true);
		setMenuCreator(this);
		this.filterPathProvider = filterPathProvider;
	}
	@Override
	public void dispose() {
		if (dropDownMenu != null) {
			dropDownMenu.dispose();
			dropDownMenu = null;
		}
	}

	@Override
	public Menu getMenu(Control parent) {
		if (dropDownMenu != null) {
			dropDownMenu.dispose();
		}
		dropDownMenu = new Menu(parent);
		addActionsToMenu();
		return dropDownMenu;
	}

	@Override
	public Menu getMenu(Menu parent) {
		if (dropDownMenu != null) {
			dropDownMenu.dispose();
		}
		dropDownMenu = new Menu(parent);
		addActionsToMenu();
		return dropDownMenu;
	}
	
	private void addActionsToMenu() {
		// Filters off
		ActionContributionItem itemNoFilter = new ActionContributionItem(new ToggleNoNexusFiltersAction(filterPathProvider));
		itemNoFilter.fill(dropDownMenu, -1);
		
		// Recently used filters
		String history[] = filterPathProvider.getNexusPathHistory();
		if (history != null){
			Separator separator = new Separator();
			separator.fill(dropDownMenu, -1);
			for (int i = 0; i < history.length; i++) {
				ActionContributionItem item = new ActionContributionItem(new ToggleNexusFilterAction(history[i],  filterPathProvider));
				item.fill(dropDownMenu, -1);	
			}
		}

		// New filter
		ActionContributionItem itemSortDialog = new ActionContributionItem(new NexusNewFilterAction(filterPathProvider, history));
		Separator separator2 = new Separator();
		separator2.fill(dropDownMenu, -1);
		itemSortDialog.fill(dropDownMenu, -1);
	}
}
