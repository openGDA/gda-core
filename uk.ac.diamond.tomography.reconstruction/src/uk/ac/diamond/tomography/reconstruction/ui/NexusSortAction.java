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

public class NexusSortAction extends Action implements IMenuCreator {

	private Menu dropDownMenu;
	private INexusPathProvider sortProvider;

	public NexusSortAction(INexusPathProvider sortProvider) {
		setText("Nexus Sort");
		setToolTipText("Sort nexus files");
		setEnabled(true);
		setMenuCreator(this);
		this.sortProvider = sortProvider;
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
		// No sort
		ActionContributionItem itemNoSort = new ActionContributionItem(new ToggleNoNexusSortingAction(sortProvider));
		itemNoSort.fill(dropDownMenu, -1);

		// Recently used sorts
		String history[] = sortProvider.getNexusPathHistory();
		if (history != null){
			Separator separator = new Separator();
			separator.fill(dropDownMenu, -1);
			for (int i = 0; i < history.length; i++) {
				ActionContributionItem item = new ActionContributionItem(new ToggleNexusSorterAction(history[i],  sortProvider));
				item.fill(dropDownMenu, -1);
			}
		}

		// New sorter
		ActionContributionItem itemSortDialog = new ActionContributionItem(new NexusNewSortAction(sortProvider, history));
		Separator separator2 = new Separator();
		separator2.fill(dropDownMenu, -1);
		itemSortDialog.fill(dropDownMenu, -1);
	}

}
