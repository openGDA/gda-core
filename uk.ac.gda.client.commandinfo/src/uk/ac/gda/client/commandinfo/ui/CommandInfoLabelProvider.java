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

package uk.ac.gda.client.commandinfo.ui;

import gda.jython.commandinfo.ICommandThreadInfo;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import uk.ac.gda.client.commandinfo.ui.CommandInfoComposite.CiColumn;

public class CommandInfoLabelProvider implements ITableLabelProvider {

	@Override
	public void addListener(ILabelProviderListener listener) {}

	@Override
	public void dispose() {}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		ICommandThreadInfo record;
		if (element instanceof ICommandThreadInfo) {
			record = (ICommandThreadInfo) element;
		} else {
			return "-";
		}
		CiColumn column = CiColumn.getColumnByIndex(columnIndex);
		String label = "-";
		switch (column) {
		case _id : // ID
			label = String.valueOf(record.getId());
			break;
		case _threadType : // thread type
			label = record.getCommandThreadType();
			break;
		case _state : // state
			label = record.getState();
			break;
		case _command : // Command
			label = record.getCommand();
			break;
		case _priority : // Priority
			label = String.valueOf(record.getPriority());
			break;
		default : break;
		}
		return label;
	}
}
