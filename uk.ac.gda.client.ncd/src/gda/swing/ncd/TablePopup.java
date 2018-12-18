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

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;

/**
 * Takes a table model and creates a popup table 
 */
public class TablePopup extends JPopupMenu {
	private JTable table;

	/**
	 * @param ttm TableModel to use
	 */
	public TablePopup(TableModel ttm) {
		JPanel panel = new JPanel();
		
		table = new JTable(ttm);
		table.setGridColor(Color.blue);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(
				new Dimension(table.getPreferredScrollableViewportSize().width,
					(1 + table.getRowCount()) * (table.getRowHeight() + 1)));
		panel.add(scrollPane);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		add(panel);
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory
				.createEmptyBorder(3, 3, 3, 3)));
	}
	
	/**
	 * gives access to the JTable to allow some customisation if required
	 * @return JTable
	 */
	public JTable getTable() {
		return table;
	}
}
