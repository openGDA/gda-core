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

package gda.jython.authoriser;

import gda.configuration.properties.LocalProperties;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

public class EditPermissions extends javax.swing.JFrame implements ActionListener {

	private JTable permissionsTable;

	private FileAuthoriser fileAuthoriser = new FileAuthoriser();
	private Vector<String> deletedEntries = new Vector<String>();
	/**
	 * Auto-generated main method to display this JFrame
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				EditPermissions inst = new EditPermissions();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}

	/**
	 * Constructor.
	 */
	public EditPermissions() {
		super();
		initGUI();
	}

	private void initGUI() {
		try {
			setTitle("Permissions Editor");
			JScrollPane jScrollPane1 = new JScrollPane();
			jScrollPane1.setViewportView(getPermissionsTable());			
			getContentPane().add(jScrollPane1, BorderLayout.CENTER);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setSize(400, 300);
			{
				JMenuBar menuBar = new JMenuBar();
				setJMenuBar(menuBar);
				{
					JMenu fileMenu = new JMenu();
					menuBar.add(fileMenu);
					fileMenu.setText("File");
					{
						JMenuItem addMenuItem = new JMenuItem();
						fileMenu.add(addMenuItem);
						addMenuItem.setText("Add user");
						addMenuItem.addActionListener(this);
					}
					{
						JMenuItem deleteMenuItem = new JMenuItem();
						fileMenu.add(deleteMenuItem);
						deleteMenuItem.setText("Delete selected");
						deleteMenuItem.addActionListener(this);
					}
					{
						JMenuItem saveMenuItem = new JMenuItem();
						fileMenu.add(saveMenuItem);
						saveMenuItem.setText("Save");
						saveMenuItem.addActionListener(this);
					}
					fileMenu.add(new JSeparator());
					{
						JMenuItem openFileMenuItem = new JMenuItem();
						fileMenu.add(openFileMenuItem);
						openFileMenuItem.setText("Undo Changes");
						openFileMenuItem.addActionListener(this);
					}
					fileMenu.add(new JSeparator());
					{
						JMenuItem exitMenuItem = new JMenuItem();
						fileMenu.add(exitMenuItem);
						exitMenuItem.setText("Exit");
						exitMenuItem.addActionListener(this);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JTable getPermissionsTable() {
		if (permissionsTable == null) {
			PermissionsTableModel jTable1Model = new PermissionsTableModel();
			permissionsTable = new JTable();
			permissionsTable.setModel(jTable1Model);
			permissionsTable.setAutoCreateRowSorter(true);
			permissionsTable.setColumnSelectionAllowed(false);
			permissionsTable.setRowSelectionAllowed(true);
			permissionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			populateTable();

		}
		return permissionsTable;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getActionCommand().equals("Undo Changes")) {
			// populate the table
			deletedEntries.clear();
			populateTable();
		} else if (e.getActionCommand().equals("Save")) {
			// write out the table contents
			saveTable();
		} else if (e.getActionCommand().equals("Delete selected")) {
			// remove selected users
			deleteSelectedUsers();
		} else if (e.getActionCommand().equals("Add user")) {
			// add a line to the table
			((PermissionsTableModel) permissionsTable.getModel()).addRow();
			permissionsTable.addNotify();
		} else if (e.getActionCommand().equals("Exit")) {
			// close the program
			System.exit(0);
		}
	}

	private void deleteSelectedUsers() {
		PermissionsTableModel jTable1Model = (PermissionsTableModel) permissionsTable.getModel();
		int[] selected = permissionsTable.getSelectedRows();

		for (int row : selected) {
			deletedEntries.add((String)jTable1Model.getValueAt(row, 0));
			jTable1Model.removeRow(row);
			
		}
		permissionsTable.addNotify();
	}

	private void populateTable() {
		PermissionsTableModel jTable1Model = new PermissionsTableModel();
		UserEntry[] entries = fileAuthoriser.getEntries();

		for (int i = 0; i < entries.length; i++) {
			jTable1Model.addRow();
			jTable1Model.setValueAt(entries[i].getUserName(), i, 0);
			jTable1Model.setValueAt(entries[i].getAuthorisationLevel(), i, 1);
			jTable1Model.setValueAt(entries[i].getStaff(), i, 2);
		}

		permissionsTable.setModel(jTable1Model);

	}

	private void saveTable() {
		if(deletedEntries.size() != 0)
		{
			for (String s : deletedEntries)
			{
				fileAuthoriser.deleteEntry(s);
			}
		}
		HashMap<String, UserEntry> data = validateTable();
		if (data != null) {
			for (String fedid : data.keySet()) {
				fileAuthoriser.addEntry(fedid.toString(), data.get(fedid).getAuthorisationLevel(), data.get(fedid)
						.getStaff());
			}
			JOptionPane.showMessageDialog(this, "Save complete.");
		}
	}

	private HashMap<String, UserEntry> validateTable() {

		HashMap<String, UserEntry> validatedData = new HashMap<String, UserEntry>();

		for (int i = 0; i < permissionsTable.getModel().getRowCount(); i++) {

			// check this row first for a valid entry. If any row is incorrect then ignore that row and carry on.
			String fedid = permissionsTable.getValueAt(i, 0).toString();
			Object level = permissionsTable.getValueAt(i, 1);
			Boolean staff = (Boolean) permissionsTable.getValueAt(i, 2);

			if (fedid.equals("")) {
				System.out.println("Missing username - will skip this row.");
			} else {
				try {
					int levelValue = Integer.parseInt(level.toString());

					if (validatedData.containsKey(fedid)) {
						System.out.println("Duplicate entry for " + fedid + ". Save failed.");
						JOptionPane.showMessageDialog(this, "Duplicate entry for " + fedid + ". Save failed.");
						return null;
					}
					validatedData.put(fedid, new UserEntry(fedid, levelValue, staff));

				} catch (NumberFormatException e) {
					System.out.println("Invalid level for " + fedid + ". Skipping.");
				}
			}
		}

		return validatedData;

	}

	private class PermissionsTableModel extends AbstractTableModel {

		private Vector<UserEntry> data = new Vector<UserEntry>(1);

		/**
		 * Constructor.
		 */
		public PermissionsTableModel() {
			super();
		}

		public void removeRow(int row) {
			data.remove(row);
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return true;
		}

		@Override
		public String getColumnName(int i) {
			if (i == 0) {
				return "User Name";
			} else if (i == 1) {
				return "Authorisation Level";
			}
			return "isStaff";

		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public int getRowCount() {
			if (data == null) {
				return 0;
			}
			return data.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex > 2 || rowIndex > data.size()) {
				return null;
			}

			UserEntry row = data.get(rowIndex);

			if (columnIndex == 0) {
				return row.getUserName();
			} else if (columnIndex == 1) {
				return row.getAuthorisationLevel();
			}
			return row.getStaff();
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

			if (rowIndex < getRowCount()) {

				UserEntry editedUser = data.get(rowIndex);

				try {
					if (columnIndex == 0) {
						editedUser.setUserName((String) aValue);
					} else if (columnIndex == 1 && aValue instanceof String) {
						editedUser.setAuthorisationLevel(Integer.parseInt((String) aValue));
					} else if (columnIndex == 1) {
						editedUser.setAuthorisationLevel((Integer) aValue);
					} else if (columnIndex == 2) {
						editedUser.setStaff((Boolean) aValue);
					}
					data.set(rowIndex, editedUser);
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(EditPermissions.this, "Invalid authorisation level");
				}
			}

		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Class getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return String.class;
			case 1:
				return Integer.class;
			case 2:
				return Boolean.class;
			}
			return null;
		}

		/**
		 * Add an extra row
		 */
		public void addRow() {
			UserEntry newUser = new UserEntry("", LocalProperties.getInt(FileAuthoriser.DEFAULTLEVELPROPERTY, 1), false);
			data.add(newUser);
		}
	}
}