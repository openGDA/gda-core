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
import org.eclipse.jface.action.IAction;

import uk.ac.diamond.tomography.reconstruction.INexusFilterDescriptor;
import uk.ac.diamond.tomography.reconstruction.INexusFilterInfoProvider;

/**
 * An action for toggling a nexus filter on or off
 */
public class ToggleNexusFilterAction extends Action {

	private INexusFilterInfoProvider filterInfoProvider;
	private INexusFilterDescriptor filterDescriptor;

	/**
	 * Create a new filter action
	 * @param thisDescriptor the filter descriptor this action toggles
	 * @param filterInfoProvider the filter info provider to get and set the filter. Must not be <code>null</code>
	 */
	public ToggleNexusFilterAction(INexusFilterDescriptor thisDescriptor, INexusFilterInfoProvider filterInfoProvider) {
		super(thisDescriptor.toString() +'@', IAction.AS_CHECK_BOX); //$NON-NLS-1$
		this.filterDescriptor = thisDescriptor;
		this.filterInfoProvider = filterInfoProvider;
		if (filterDescriptor != null){
			setChecked(filterDescriptor.equals(filterInfoProvider.getFilterDescriptor()));
		}
	}

	@Override
	public void run() {
		if (isChecked()) filterInfoProvider.setFilterDescriptor(filterDescriptor);
	}
}
