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

package uk.ac.gda.ui.modifiers;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 *
 */
public abstract class DoubleClickModifier implements ICellModifier {

	protected boolean enabled;
	
	protected DoubleClickModifier(final ColumnViewer viewer) {
		viewer.getControl().addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				setEnabled(false);
			}
		});
		viewer.getControl().addListener(SWT.MouseDoubleClick, new Listener() {
			@Override
			public void handleEvent(Event event) {
				IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
				if (selection.toList().size() != 1) {
					return;
				}
				final ViewerCell cell = viewer.getCell(new Point(event.x, event.y));
				setEnabled(true);
				viewer.editElement(selection.getFirstElement(),cell!=null?cell.getColumnIndex():0);
			}
		});

	}
	
	/**
	 * The editor can be disabled, useful for double click table editing
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	@Override
	public boolean canModify(Object element, String property) {
		return enabled;
	}

}
