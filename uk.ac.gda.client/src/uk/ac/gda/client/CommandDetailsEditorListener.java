/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ui.IPropertyListener;

public class CommandDetailsEditorListener implements IPropertyListener {

	private TableViewer viewer;

	public CommandDetailsEditorListener(TableViewer tableViewer) {
		viewer = tableViewer;
	}

	@Override
	public void propertyChanged(Object arg0, int arg1) {
		viewer.refresh(true);
	}

}
