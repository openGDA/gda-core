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

package gda.swing.ncd;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;

/**
 * 
 */
public class UnitCellEditor extends JComboBox implements TableCellEditor {
	protected EventListenerList listenerList = new EventListenerList();

	protected ChangeEvent changeEvent = new ChangeEvent(this);

	/**
	 * @param itemList
	 */
	public UnitCellEditor(String[] itemList) {
		super(itemList);
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				fireEditingStopped();
			}
		});
	}

	@Override
	public void addCellEditorListener(CellEditorListener listener) {
		listenerList.add(CellEditorListener.class, listener);
	}

	@Override
	public void removeCellEditorListener(CellEditorListener listener) {
		listenerList.remove(CellEditorListener.class, listener);
	}

	protected void fireEditingStopped() {
		CellEditorListener listener;
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] == CellEditorListener.class) {
				listener = (CellEditorListener) listeners[i + 1];
				listener.editingStopped(changeEvent);
			}
		}
	}

	protected void fireEditingCanceled() {
		CellEditorListener listener;
		Object[] listeners = listenerList.getListenerList();
		for (int i = 0; i < listeners.length; i++) {
			if (listeners[i] == CellEditorListener.class) {
				listener = (CellEditorListener) listeners[i + 1];
				listener.editingCanceled(changeEvent);
			}
		}
	}

	@Override
	public void cancelCellEditing() {
		fireEditingCanceled();
	}

	@Override
	public boolean stopCellEditing() {
		fireEditingStopped();
		return true;
	}

	@Override
	public boolean isCellEditable(EventObject event) {
		return true;
	}

	@Override
	public boolean shouldSelectCell(EventObject event) {
		return true;
	}

	@Override
	public Object getCellEditorValue() {
		return getSelectedItem();
	}

	/**
	 * tells the table what component to use to edit value
	 */
	/**
	 * @param table
	 * @param value
	 * @param isSelected
	 * @param row
	 * @param column
	 * @return the cell editor
	 */
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if (value != null && value instanceof String)
			setSelectedItem(value);

		return this;
	}

}
