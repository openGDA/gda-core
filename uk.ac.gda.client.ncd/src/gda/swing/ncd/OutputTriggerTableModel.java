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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a way to work out the TFG2 drive and inversion settings in a user GUI
 */
public class OutputTriggerTableModel extends DefaultTableModel {

	private static final Logger logger = LoggerFactory.getLogger(OutputTriggerTableModel.class);

	private static final String[] columnNames = { "Trigger", "Inversion", "Drive" };

	private static final String[] triggers = { "USR0", "USR1", "USR2", "USR3", "USR4", "USR5", "USR6", "USR7" };

	private int inversion = 192;

	private int drive = 0;

	private TablePopup popup;

	private static final String[] inversionOptions = { "idle low", "idle high" };

	private static final String[] driveOptions = { "full drive", "50 Ω terminated" };

	private class RadioButtonRenderer implements TableCellRenderer {
		JRadioButton[] buttons;
		JPanel panel;
		
		RadioButtonRenderer(String[] str) {
			panel = new JPanel();
			panel.setFocusable(false);
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			buttons = new JRadioButton[str.length];
			ButtonGroup bg = new ButtonGroup();
			for (int i = 0; i < buttons.length; i++) {
				buttons[i] = new JRadioButton(str[i]);
				buttons[i].setFocusPainted(false);
				panel.add(buttons[i]);
				bg.add(buttons[i]);
			}
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			if (value instanceof Integer) {
				int i = ((Integer) value).intValue();
				i = (i >= 0) ? i : 0;
				buttons[i].setSelected(true);
			}
			return panel;
		}
	}
	
	private class RadioButtonEditor extends DefaultCellEditor implements ItemListener {
		JRadioButton[] buttons;
		JPanel panel;

		RadioButtonEditor(String[] str) {
			super(new JCheckBox());
			panel = new JPanel();
			panel.setFocusable(false);
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			buttons = new JRadioButton[str.length];
			ButtonGroup bg = new ButtonGroup();
			for (int i = 0; i < buttons.length; i++) {
				buttons[i] = new JRadioButton(str[i]);
				buttons[i].addItemListener(this);
				panel.add(buttons[i]);
				bg.add(buttons[i]);
			}
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			if (value instanceof Integer) {
				int i = ((Integer) value).intValue();
				i = (i >= 0) ? i : 0;
				buttons[i].setSelected(true);
			}
			return panel;
		}

		@Override
		public Object getCellEditorValue() {
			for (int i = 0; i < buttons.length; i++) {
				if (buttons[i].isSelected()) {
					return i;
				}
			}
			return 0;
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			if (((JRadioButton) e.getSource()).isSelected()) fireEditingStopped();
		}
	}

	/**
	 * default constructor
	 */
	public OutputTriggerTableModel() {
			initialiseDataVector();
	}

	private OutputTriggerTableModel(int inversion, int drive) {
		this.inversion = inversion;
		this.drive = drive;
		initialiseDataVector();
	}

	private void initialiseDataVector() {

		Object[][] dv = new Object[triggers.length][3];
		for (int i = 0; i < triggers.length; i++) {
			dv[i][0] = triggers[i];
			dv[i][1] = ((inversion & 1 << i) > 0) ? 1 : 0;
			dv[i][2] = ((drive & 1 << i) > 0) ? 1 : 0;
		}
		setDataVector(dv, columnNames);
	}
	
	private void updateData() {
		for (int i = 0; i < triggers.length; i++) {
			setValueAt(((inversion & 1 << i) > 0) ? 1 : 0, i, 1);
			setValueAt(((drive & 1 << i) > 0) ? 1 : 0, i, 2);
		}
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		switch (col) {
		case 0:
			return false;
		}
		return true;
	}

	/**
	 * @param writer
	 */
	public void save(BufferedWriter writer) {
		try {
			writer.write("<OutputTrigger>");

			writer.newLine();
			writer.write("<Inversion>");
			writer.write(String.format("%d", getInversionValue()));
			writer.write("</Inversion>");
			writer.newLine();
			writer.write("<Drive>");
			writer.write(String.format("%d", getDriveValue()));
			writer.write("</Drive>");
			writer.newLine();
			writer.write("</OutputTrigger>");
			writer.newLine();
		} catch (IOException e) {
			logger.error(this.getClass().toString() + ": error writing configuration", e);
		}
	}

	/**
	 * @param reader
	 */
	public void load(BufferedReader reader) {
		try {
			String line;
			while ((line = reader.readLine()) != null) {
				// logger.debug(line);
				if (line.startsWith("<Inversion>")) {
					StringTokenizer parse = new StringTokenizer(line, "<>, ");
					parse.nextToken(); // remove the Inversion string
				try {
						inversion = Integer.parseInt(parse.nextToken());
					} catch (NumberFormatException nfa) {
						// illegal input does not work
					}
				} else if (line.startsWith("<Drive>")) {
					StringTokenizer parse = new StringTokenizer(line, "<>, ");
					parse.nextToken(); // remove the Drive string
					try {
						drive = Integer.parseInt(parse.nextToken());
					} catch (NumberFormatException nfa) {
						// illegal input does not work
					}
				} else if (line.startsWith("</OutputTrigger>")) {
					break;
				}
			}
			updateData();
		} catch (Exception e) {
			logger.error("TimeFrameProfile: load: " + e);
		}
	}

	/**
	 * @return TriggerTableModel
	 */
	public OutputTriggerTableModel copy() {
		return new OutputTriggerTableModel(inversion, drive);
	}

	/**
	 * @return the value for the TFG2 inversion setting 
	 */
	public int getInversionValue() {
		for (int i = 0; i < triggers.length; i++) {
			if ((Integer) getValueAt(i, 1) != 0) {
				inversion |= 1 << i;
			} else {
				inversion &= ~(1 << i);
			}
		}
		logger.debug(String.format("inversion value %d", inversion & 0xFF));
		return inversion & 0xFF;
	}

	/**
	 * @return the value for the TFG2 drive setting
	 */
	public int getDriveValue() {
		for (int i = 0; i < triggers.length; i++) {
			if ((Integer) getValueAt(i, 2) != 0) {
				drive |= 1 << i;
			} else {
				drive &= ~(1 << i);
			}
		}
		return drive & 0xFF;
	}

	/**
	 * @return a popupmenu containing a table with this 
	 */
	public JPopupMenu getPopupTable() {
		if (popup == null) {
			popup = new TablePopup(this);
			RadioButtonEditor rbe;
			RadioButtonRenderer rbr;
			rbe = new RadioButtonEditor(inversionOptions);
			rbr = new RadioButtonRenderer(inversionOptions);
			popup.getTable().getColumn("Inversion").setCellRenderer(rbr);
			popup.getTable().getColumn("Inversion").setCellEditor(rbe);
			popup.getTable().getColumn("Inversion").setPreferredWidth(180);
			rbe = new RadioButtonEditor(driveOptions);
			rbr = new RadioButtonRenderer(driveOptions);
			popup.getTable().getColumn("Drive").setCellRenderer(rbr);
			popup.getTable().getColumn("Drive").setCellEditor(rbe);
			popup.getTable().getColumn("Drive").setPreferredWidth(230);
			popup.getTable().getColumn("Trigger").setPreferredWidth(70);
		}
		return popup;
	}
}
