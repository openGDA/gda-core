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

package uk.ac.gda.client.plotting;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

public class DataPlotterCheckedTreeViewer extends ContainerCheckedTreeViewer {

	public void updateCheckSelection(Object data, boolean isChecked) {
		updateCheckSelection(data, isChecked, true);
	}

	/**
	 * Update checked status in tree for supplied object with optional firing of CheckStateChangedEvent
	 *
	 * @param data
	 * @param isChecked
	 * @param fireEvent
	 * @since 28/9/2016
	 */
	public void updateCheckSelection(Object data, boolean isChecked, boolean fireEvent) {
		Widget widget = this.findItem(data);
		if (widget instanceof TreeItem) {
			((TreeItem) widget).setChecked(isChecked);
			if (fireEvent)
				this.fireCheckStateChanged(new CheckStateChangedEvent(this, data, ((TreeItem) widget).getChecked()));
		}
	}

	public DataPlotterCheckedTreeViewer(Composite parent, int style) {
		super(parent, style);
	}

	public DataPlotterCheckedTreeViewer(Composite parent) {
		super(parent);
	}

}
