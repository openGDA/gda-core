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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JPopupMenu;
import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class TriggerTableModel extends AbstractTableModel {

	private static final Logger logger = LoggerFactory.getLogger(TriggerTableModel.class);

	private static final String[] columnNames = { "Trigger", "Threshhold", "Debounce" };

	private static final String[] triggers = { "BM Trigger", "ADC chan 0", "ADC chan 1", "ADC chan 2", "ADC chan 3",
			"ADC chan 4", "ADC chan 5", "TTL trig 0", "TTL trig 1", "TTL trig 2", "TTL trig 3", "LVDS Lemo ",
			"TFG cable 1", "TFG cable 2", "TFG cable 3", "Var thrshhld" };

	private Double[] threshholds = { Double.NaN, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.NaN, Double.NaN, Double.NaN,
			Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, 0.0 };

	private Double[] debounce = { Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN,
			0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };

	private JPopupMenu popup;

	/**
	 * 
	 */
	public TriggerTableModel() {
	}

	private TriggerTableModel(Double[] debounce, Double[] thresholds) {
		this.debounce = debounce.clone();
		this.threshholds = thresholds.clone();
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		switch (col) {
		case 0:
			return false;
		case 1:
			return (!threshholds[row].isNaN());
		case 2:
			return (!debounce[row].isNaN());
		}
		return false;
	}

	/**
	 * Gets the table column count
	 * 
	 * @return table column count
	 */
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	/**
	 * Gets the table row count
	 * 
	 * @return table row count
	 */
	@Override
	public int getRowCount() {
		return triggers.length;
	}

	/**
	 * Gets the chosen table column name {@inheritDoc}
	 * 
	 * @param col
	 *            chosen table column
	 * @return table column name
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	/**
	 * Gets object from the chosen table cell
	 * 
	 * @param row
	 *            chosen table row
	 * @param col
	 *            chosen table column
	 * @return object from the chosen table cell
	 */
	@Override
	public Object getValueAt(int row, int col) {
		Object value = null;

		switch (col) {
		case 0:
			value = triggers[row];
			break;
		case 1:
			value = (threshholds[row].isNaN()) ? new String("--") : threshholds[row];
			break;
		case 2:
			value = (debounce[row].isNaN()) ? new String("--") : debounce[row];
			break;
		}
		return value;
	}

	@Override
	public Class<? extends Object> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		try {
			Double dvalue = Double.parseDouble(value.toString());
			if (dvalue.isNaN())
				return;
			switch (col) {
			case 0:
				break;
			case 1:
				logger.debug("row " + row + " col " + col + " value " + value);
				threshholds[row] = dvalue;
				break;
			case 2:
				debounce[row] = dvalue;
				break;
			}
			fireTableCellUpdated(row, col);
		} catch (NumberFormatException nfa) {
			// just ignore illegal input
		}
	}

	/**
	 * @return an array of debounce values
	 */
	public ArrayList<Double> getDebounceValues() {
		ArrayList<Double> values = new ArrayList<Double>();
		for (double d : debounce) {
			values.add(d);
		}
		return values;
	}

	/**
	 * @return an array of threshold values
	 */
	public ArrayList<Double> getThresholdValues() {
		ArrayList<Double> values = new ArrayList<Double>();
		for (double d : threshholds) {
			values.add(d);
		}
		return values;
	}

	/**
	 * @param writer
	 */
	public void save(BufferedWriter writer) {
		try {
			writer.write("<Trigger>");

			writer.newLine();
			writer.write("<Debounce>");
			for (Double d : debounce) {
				writer.write(d.toString() + " ");
			}
			writer.write("</Debounce>");
			writer.newLine();
			writer.write("<Threshold>");
			for (Double d : threshholds) {
				writer.write(d.toString() + " ");
			}
			writer.write("</Threshold>");
			writer.newLine();
			writer.write("</Trigger>");
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
				if (line.startsWith("<Debounce>")) {
					StringTokenizer parse = new StringTokenizer(line, "<>, ");
					parse.nextToken(); // remove the Debounce string
					for (int i = 0; i < debounce.length; i++) {
						if (!parse.hasMoreTokens())
							break;
						try {
							Double dvalue = Double.parseDouble(parse.nextToken());
							if (!dvalue.isNaN() && !debounce[i].isNaN())
								debounce[i] = dvalue;
						} catch (NumberFormatException nfa) {
							// break on illegal input
							break;
						}
					}
				} else if (line.startsWith("<Threshold>")) {
					StringTokenizer parse = new StringTokenizer(line, "<>, ");
					parse.nextToken(); // remove the Threshold string
					for (int i = 0; i < threshholds.length; i++) {
						if (!parse.hasMoreTokens())
							break;
						try {
							Double dvalue = Double.parseDouble(parse.nextToken());
							if (!dvalue.isNaN() && !threshholds[i].isNaN())
								threshholds[i] = dvalue;
						} catch (NumberFormatException nfa) {
							// break on illegal input
							break;
						}
					}
				} else if (line.startsWith("</Trigger>")) {
					break;
				}
			}
		} catch (Exception e) {
			logger.error("TimeFrameProfile: load: " + e);
		}

	}

	/**
	 * Return a copy of this 
	 * @return TriggerTableModel
	 */
	public TriggerTableModel copy() {
		return new TriggerTableModel(debounce, threshholds);
	}
	
	/**
	 * Creates a JPopupMenu that displays and allows editing this TableModel
	 * 
	 * @return JPopupMenu
	 */
	public JPopupMenu getPopupTable() {
		if (popup == null) popup = new TablePopup(this);
		return popup;
	}
}