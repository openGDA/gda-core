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

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import uk.ac.diamond.tomography.reconstruction.INexusFilterDescriptor;
import uk.ac.diamond.tomography.reconstruction.INexusFilterInfoProvider;

public class NexusFilterAction extends Action implements IMenuCreator {

	private Menu dropDownMenu;
	private INexusFilterInfoProvider filterPathProvider;

	public NexusFilterAction(INexusFilterInfoProvider filterPathProvider) {
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

		INexusFilterDescriptor[] pastDescriptors = filterPathProvider.getFilterDescriptorHistory();
		// Recently used filters
		if (pastDescriptors != null){
			Separator separator = new Separator();
			separator.fill(dropDownMenu, -1);
			for (int i = 0; i < pastDescriptors.length; i++) {
				ActionContributionItem item = new ActionContributionItem(new ToggleNexusFilterAction(pastDescriptors[i],  filterPathProvider));
				item.fill(dropDownMenu, -1);
			}
		}

		// New filter
		if (pastDescriptors != null){
			ArrayList<String> historyList = new ArrayList<String>(pastDescriptors.length);
			for (int i = 0; i < pastDescriptors.length; i++) {
				if (!historyList.contains(pastDescriptors[i].getNexusFilterPath())){
					historyList.add(pastDescriptors[i].getNexusFilterPath());
				}
			}
			String pathHistory[] = historyList.toArray(new String[historyList.size()]);
			ActionContributionItem itemSortDialog = new ActionContributionItem(new NexusNewFilterAction(filterPathProvider, pathHistory));
			Separator separator2 = new Separator();
			separator2.fill(dropDownMenu, -1);
			itemSortDialog.fill(dropDownMenu, -1);
		}
	}
}
