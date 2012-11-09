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

import gda.device.DeviceException;
import gda.device.Timer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class TimeFrameTableModel2 extends AbstractTableModel {
	private static final Logger logger = LoggerFactory.getLogger(TimeFrameTableModel2.class);

	/**
	 * The time frame group column number
	 */
	public static final int GROUP = 0;

	/**
	 * The time frame count column number
	 */
	public static final int NFRAME = 1;

	/**
	 * The time frame wait time column number
	 */
	public static final int WAITTIME = 2;

	/**
	 * The time frame wait unit column number
	 */
	public static final int WAITUNIT = 3;

	/**
	 * The time frame run time column number
	 */
	public static final int RUNTIME = 4;

	/**
	 * The time frame run unit column number
	 */
	public static final int RUNUNIT = 5;

	/**
	 * The time frame wait pause bit column number
	 */
	public static final int WAITPAUSE = 6;

	/**
	 * The time frame run pause bit column number
	 */
	public static final int RUNPAUSE = 7;

	/**
	 * The time frame wait period pulse bit column number
	 */
	public static final int WPULSE = 8;

	/**
	 * The time frame run period pulse bit column number
	 */
	public static final int RPULSE = 9;

	/**
	 * The possible units of time in which to program the tfg
	 */
	public static final String[] displayUnits = { "ns", "usec", "msec", "sec", "min", "hour" };

	/**
	 * The name of the pause states of the tfg
	 */
	public static final String[] displayPause = { "Software", "No Pause", "\u2191 BM Trigger", "\u2191 ADC chan 0",
			"\u2191 ADC chan 1", "\u2191 ADC chan 2", "\u2191 ADC chan 3", "\u2191 ADC chan 4", "\u2191 ADC chan 5",
			"\u2191 TTL trig 0", "\u2191 TTL trig 1", "\u2191 TTL trig 2", "\u2191 TTL trig 3", "\u2191 LVDS Lemo ",
			"\u2191 TFG cable 1", "\u2191 TFG cable 2", "\u2191 TFG cable 3", "\u2191 Var thrshhld",
			"\u2193 BM Trigger", "\u2193 ADC chan 0", "\u2193 ADC chan 1", "\u2193 ADC chan 2", "\u2193 ADC chan 3",
			"\u2193 ADC chan 4", "\u2193 ADC chan 5", "\u2193 TTL trig 0", "\u2193 TTL trig 1", "\u2193 TTL trig 2",
			"\u2193 TTL trig 3", "\u2193 LVDS Lemo ", "\u2193 TFG cable 1", "\u2193 TFG cable 2", "\u2193 TFG cable 3",
			"\u2193 Var thrshhld" };

	/**
	 * The names of the tfg front panel output ports
	 */
	public static final String[] displayOutputs = { "Output 0", "Output 1", "Output 2", "Output 3", "Output 4",
			"Output 5", "Output 6", "Output 7" };

	private static final String[] columnNames = { "Group", "Frames", "Wait Time", "Wait Units", "Run Time",
			"Run Units", "Wait Pause", "Run Pause", "Wait Pulses", "Run Pulses" };

	private Vector<TimeFrameGroup2> vector;

	/**
	 * Constructor
	 */
	public TimeFrameTableModel2() {
		this(new Vector<TimeFrameGroup2>());
		insertAfter(-1);
	}

	private TimeFrameTableModel2(Vector<TimeFrameGroup2> vector) {
		this.vector = vector;
	}

	/**
	 * @return a copy of the time frame model
	 */
	public TimeFrameTableModel2 copy() {
		Vector<TimeFrameGroup2> v = new Vector<TimeFrameGroup2>();

		for (TimeFrameGroup2 group : vector) {
			v.add(group.copy());
		}
		return new TimeFrameTableModel2(v);
	}

	/**
	 * Returns the class of component appropriate to the column. This is used by JTable when choosing editors and
	 * renderers for particular classes. {@inheritDoc}
	 * 
	 * @param col
	 *            the column in question
	 * @return the class to use
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<? extends Object> getColumnClass(int col) {
		Class<? extends Object> rtrn;

		if (col == RUNUNIT || col == WAITUNIT || col == WAITPAUSE || col == RUNPAUSE || col == WPULSE || col == RPULSE) {
			rtrn = String.class;
		} else {
			rtrn = Object.class;
		}

		return rtrn;
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
		return vector == null ? 0 : vector.size();
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
		Object rtrn = null;

		if (row >= 0 && row < getRowCount()) {
			TimeFrameGroup2 group = vector.elementAt(row);
			switch (col) {
			case GROUP:
				rtrn = new Integer(group.getGroup());
				break;
			case NFRAME:
				rtrn = new Integer(group.getFrames());
				break;
			case WAITTIME:
				rtrn = new Integer(group.getWaitTime());
				break;
			case WAITUNIT:
				rtrn = group.getWaitUnit();
				break;
			case RUNTIME:
				rtrn = new Integer(group.getRunTime());
				break;
			case RUNUNIT:
				rtrn = group.getRunUnit();
				break;
			case WAITPAUSE:
				rtrn = group.getWaitPause();
				break;
			case RUNPAUSE:
				rtrn = group.getRunPause();
				break;
			case WPULSE:
				rtrn = group.getWaitPulses();
				break;
			case RPULSE:
				rtrn = group.getRunPulses();
				break;
			default:
				logger.debug("TimeFrameTableModel: error in getValueAt, row=" + row + ", col=" + col);
			}
		}
		return rtrn;
	}

	/**
	 * Checks the table cell is editable or not {@inheritDoc}
	 * 
	 * @param row
	 *            chosen table row
	 * @param col
	 *            chosen table column
	 * @return true if the table cell is editable, else false
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int row, int col) {
		return (col > 0);
	}

	/**
	 * Sets an object to the fixed table cell {@inheritDoc}
	 * 
	 * @param value
	 *            an object to be set to the fixed table cell
	 * @param row
	 *            chosen table row
	 * @param col
	 *            chosen table column
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
	public void setValueAt(Object value, int row, int col) {
		if (row >= 0 && row < getRowCount()) {
			TimeFrameGroup2 group = vector.elementAt(row);
			try {
				switch (col) {
				case NFRAME:
					group.setFrames(Integer.parseInt((String) value));
					fireTableCellUpdated(row + 1, NFRAME);
					break;
				case WAITTIME:
					group.setWaitTime(Integer.parseInt((String) value));
					fireTableCellUpdated(row + 1, WAITTIME);
					break;
				case WAITUNIT:
					group.setWaitUnit((String) value);
					fireTableCellUpdated(row + 1, WAITUNIT);
					break;
				case RUNTIME:
					group.setRunTime(Integer.parseInt((String) value));
					fireTableCellUpdated(row + 1, RUNTIME);
					break;
				case RUNUNIT:
					group.setRunUnit((String) value);
					fireTableCellUpdated(row + 1, RUNUNIT);
					break;
				case WAITPAUSE:
					group.setWaitPause((String) value);
					fireTableCellUpdated(row + 1, WAITPAUSE);
					break;
				case RUNPAUSE:
					group.setRunPause((String) value);
					fireTableCellUpdated(row + 1, RUNPAUSE);
					break;
				case WPULSE:
					group.setWaitPulses((String) value);
					fireTableCellUpdated(row + 1, WPULSE);
					break;
				case RPULSE:
					group.setRunPulses((String) value);
					fireTableCellUpdated(row + 1, RPULSE);
					break;
				default:
					logger.debug("Error in setValueAt, row=" + row + ", col=" + col);
					break;
				}
			} catch (NumberFormatException nfE) {
			}
		}
	}

	/**
	 * Gets rows as vector
	 * 
	 * @return rows saved in vector
	 */
	public Vector<TimeFrameGroup2> getVector() {
		return vector;
	}

	/**
	 * Inserts a new row to the table
	 * 
	 * @param row
	 *            insert new table row after specified row
	 */
	public void insertAfter(int row) {
		row += 1;
		logger.debug("Inserting row " + row);
		TimeFrameGroup2 group = new TimeFrameGroup2(row);
		vector.add(row, group);
		reAllocateGroups();
		fireTableRowsInserted(row, row);
	}

	/**
	 * Deletes a table row
	 * 
	 * @param row
	 *            table row to be deleted
	 * @return true if the table row can be deleted, else false
	 */
	public boolean delete(int row) {
		if (row >= 0 && row < vector.size()) {
			vector.remove(row);
			reAllocateGroups();
			fireTableRowsDeleted(row, row);
			return true;
		}
		return false;
	}

	/**
	 * Get the total number of frames declared for all groups in the profile.
	 * 
	 * @return the total number of frames declared for all groups
	 */
	public int getTotalFrames() {
		int total = 0;
		for (TimeFrameGroup2 group : vector) {
			if (group.getRunTime() != 0) {
				total += group.getFrames();
			}
		}
		return total;
	}

	/**
	 * Get the total time declared for all groups in the profile in seconds
	 * 
	 * @return the total time
	 */
	public double getTotalTime() {
		double total = 0;
		for (TimeFrameGroup2 group : vector) {
				total += (group.getActualRunTime() + group.getActualWaitTime()) * group.getFrames() / 1000;
		}
		return total;
	}
	

	/**
	 * Configure time frame data in the hardware
	 * 
	 * @param tfg
	 */
	public void configureHardware(Timer tfg) {
		try {
			if (getTotalFrames() > tfg.getMaximumFrames()) {
				logger.error("Too many time frames requested for the time frame generator");
			} else {
				tfg.clearFrameSets();
				for (TimeFrameGroup2 group : vector) {
					tfg.addFrameSet(group.getFrames(), group.getActualWaitTime(), group.getActualRunTime(), group
							.getWaitPort(), group.getRunPort(), group.getWaitPauseValue(), group.getRunPauseValue());
				}
				tfg.loadFrameSets();
			}
		} catch (DeviceException e) {
			logger.error("TimeFrameTableModel: configureHardware: " + e);
		}
	}

	/*
	 * re-allocate the groups
	 */
	private void reAllocateGroups() {
		for (int i = 0; i < vector.size(); i++) {
			vector.elementAt(i).setGroup(i + 1);
		}
	}

	/**
	 * Save time frame data in xml formatted file
	 * 
	 * @param writer
	 */
	public void save(BufferedWriter writer) {
		try {
			for (TimeFrameGroup2 group : vector) {
				writer.write("<Frames>" + group.getFrames() + "," + group.getWaitTime() + "," + group.getWaitUnit()
						+ "," + group.getRunTime() + "," + group.getRunUnit() + "," + group.getWaitPause() + ","
						+ group.getRunPause() + "," + group.getWaitPulses() + "," + group.getRunPulses() + "</Frames>");
				writer.newLine();
			}
		} catch (Exception e) {
			logger.error("TimeFrameTableModel: save: " + e);
		}
	}

	/**
	 * Load time frame data from xml formatted data file.
	 * 
	 * @param reader
	 */
	public void load(BufferedReader reader) {
		try {
			// remove all exisitng groups of existing and update
			// the timeframe table model
			int size = vector.size();
			vector.clear();
			fireTableRowsDeleted(0, size);

			String line;
			int row = 0;
			while ((line = reader.readLine()) != null) {
				// logger.debug(line);
				if (line.startsWith("<Frames>")) {
					TimeFrameGroup2 group = new TimeFrameGroup2(row + 1);

					StringTokenizer parse = new StringTokenizer(line, "<>,");
					parse.nextToken();

					group.setFrames(Integer.parseInt(parse.nextToken()));
					group.setWaitTime(Integer.parseInt(parse.nextToken()));
					group.setWaitUnit(parse.nextToken());
					group.setRunTime(Integer.parseInt(parse.nextToken()));
					group.setRunUnit(parse.nextToken());
					group.setWaitPause(parse.nextToken());
					group.setRunPause(parse.nextToken());
					group.setWaitPulses(parse.nextToken());
					group.setRunPulses(parse.nextToken());

					vector.add(row, group);
					fireTableRowsInserted(row, row);
					row++;
				} else if (line.startsWith("</Profile>")) {
					break;
				}
			}
		} catch (Exception e) {
			logger.error("TimeFrameProfile: load: " + e);
		}
	}
}
