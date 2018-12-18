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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.EventObject;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellEditor;

/**
 * 
 */
public class PauseCellEditor extends JPanel implements TableCellEditor {
	protected EventListenerList listenerList = new EventListenerList();

	protected ChangeEvent changeEvent = new ChangeEvent(this);

	private PauseMenu popup;

	private PauseCellEditor pce;

	private JComponent j;

	/**
	 * @param itemList
	 */
	public PauseCellEditor(String[] itemList) {
		pce = this;
		popup = new PauseMenu(itemList);
		putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
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
				popup.show(pce, j.getX(), j.getY());
			}
		});

		return true;
	}

	@Override
	public Object getCellEditorValue() {
		return popup.getText();
	}

	/**
	 * Tells the table what component to use to edit value {@inheritDoc}
	 * 
	 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean,
	 *      int, int)
	 */
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		return this;
	}

	/**
	 * @param visible
	 */
	public void setPopupVisible(boolean visible) {
		popup.setVisible(visible);
	}

	/**
	 * A class to control all option selectable for the pause bits of TFG2.
	 */
	public class PauseMenu extends JPopupMenu {
		private JMenuItem subMenu, subMenu2;

		private String[] iconName = { "leadingEdge.gif", "fallingEdge.gif" };

		private String selectedText;

		private ButtonGroup group;

		/**
		 * Create Pause menu comprising submenu of all possible trigger sources.
		 * 
		 * @param pauseLabelList
		 */
		public PauseMenu(String[] pauseLabelList) {
			group = new ButtonGroup();
			selectedText = pauseLabelList[0];

			JRadioButtonMenuItem rbMenuItem1 = new JRadioButtonMenuItem(pauseLabelList[0], true);
			rbMenuItem1.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					JMenuItem mi = (JMenuItem) e.getSource();
					selectedText = mi.getText();
					if (mi.isSelected())
						stopCellEditing();
				}
			});
			group.add(rbMenuItem1);
			add(rbMenuItem1);

			JRadioButtonMenuItem rbMenuItem2 = new JRadioButtonMenuItem(pauseLabelList[1], false);
			rbMenuItem2.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					JMenuItem mi = (JMenuItem) e.getSource();
					selectedText = mi.getText();
					if (mi.isSelected())
						stopCellEditing();
				}
			});
			group.add(rbMenuItem2);
			add(rbMenuItem2);

			subMenu = new JMenu("Signal");
			subMenu.setIcon(new ImageIcon(getClass().getResource(iconName[0])));

			for (int i = 2; i < 18; i++) {
				JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem(pauseLabelList[i], false);
				rbMenuItem.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						JMenuItem mi = (JMenuItem) e.getSource();
						selectedText = mi.getText();
						if (mi.isSelected())
							stopCellEditing();
					}
				});
				subMenu.add(rbMenuItem);
				group.add(rbMenuItem);
			}

			add(subMenu);

			subMenu2 = new JMenu("Signal");
			subMenu2.setIcon(new ImageIcon(getClass().getResource(iconName[1])));
			for (int i = 18; i < 34; i++) {
				JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem(pauseLabelList[i], false);
				rbMenuItem.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						JMenuItem mi = (JMenuItem) e.getSource();
						selectedText = mi.getText();
						if (mi.isSelected())
							stopCellEditing();
					}
				});
				subMenu2.add(rbMenuItem);
				group.add(rbMenuItem);
			}
			add(subMenu2);

			setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory
					.createEmptyBorder(3, 3, 3, 3)));
		}

		/**
		 * Get the text representing the selected menu item.
		 * 
		 * @return the text represrepresenting the selected menu item
		 */
		public String getText() {
			return selectedText;
		}
	}
}
