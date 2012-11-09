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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;

/**
 * 
 */
public class BinaryCellEditor extends JPanel implements TableCellEditor {
	protected EventListenerList listenerList = new EventListenerList();

	protected ChangeEvent changeEvent = new ChangeEvent(this);

	private JPopupMenu popup;

	private JCheckBox[] jcb;

	private BinaryCellEditor bce;

	private JComponent j;

	/**
	 * @param itemList
	 */
	public BinaryCellEditor(String[] itemList) {
		bce = this;
		popup = new JPopupMenu();
		jcb = new JCheckBox[itemList.length];
		for (int i = 0; i < itemList.length; i++) {
			jcb[i] = new JCheckBox(itemList[i], false);
			popup.add(jcb[i]);
		}
		popup.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory
				.createEmptyBorder(3, 3, 3, 3)));
		popup.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent me) {
				setPopupVisible(false);
				stopCellEditing();
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
		j = (JComponent) event.getSource();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				popup.show(bce, j.getX(), j.getY());
			}
		});

		return true;
	}

	@Override
	public Object getCellEditorValue() {
		String result = "";
		for (int i = 0; i < jcb.length; i++) {
			if (jcb[i].isSelected())
				result += "1";
			else
				result += "0";
		}

		return result;
	}

	/**
	 * Tells the table what component to use to edit value {@inheritDoc}
	 * 
	 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean,
	 *      int, int)
	 */
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if (value != null && value instanceof String) {
			for (int i = 0; i < ((String) value).length(); i++) {
				if (((String) value).charAt(i) == '1')
					jcb[i].setSelected(true);
				else
					jcb[i].setSelected(false);
			}
		}
		return this;
	}

	/**
	 * @param visible
	 */
	public void setPopupVisible(boolean visible) {
		popup.setVisible(visible);
	}
}
