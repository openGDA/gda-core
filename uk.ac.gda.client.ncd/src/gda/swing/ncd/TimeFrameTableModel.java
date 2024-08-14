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
public class TimeFrameTableModel extends AbstractTableModel {
	private static final Logger logger = LoggerFactory.getLogger(TimeFrameTableModel.class);

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
	 * The time frame pause bit column number
	 */
	public static final int PAUSE = 6;

	/**
	 * The time frame wait period pulse bit column number
	 */
	public static final int WPULSE = 7;

	/**
	 * The time frame run period pulse bit column number
	 */
	public static final int RPULSE = 8;

	/**
	 * The possible units of time in which to program the tfg
	 */
	public static final String[] displayUnits = { "usec", "msec", "sec", "min", "hour" };

	/**
	 * The name of the pause states of the tfg
	 */
	public static final String[] displayPause = { "Wait Pause", "Run Pause" };

	/**
	 * The names of the tfg front panel output ports
	 */
	public static final String[] displayOutputs = { "Output 1", "Output 2", "Output 3", "Output 4", "Output 5",
			"Output 6", "Output 7", "Output 8", };

	private static final String[] columnNames = { "Group", "Frames", "Wait Time", "Wait Units", "Run Time",
			"Run Units", "Pause", "Wait Pulses", "Run Pulses" };

	private Vector<TimeFrameGroup> vector;

	/**
	 * Constructor
	 */
	public TimeFrameTableModel() {
		this(new Vector<TimeFrameGroup>());
		insertAfter(-1);
	}

	private TimeFrameTableModel(Vector<TimeFrameGroup> vector) {
		this.vector = vector;
	}

	/**
	 * @return a copy of the time frame model
	 */
	public TimeFrameTableModel copy() {
		Vector<TimeFrameGroup> v = new Vector<TimeFrameGroup>();

		for (TimeFrameGroup group : vector) {
			v.add(group.copy());
		}
		return new TimeFrameTableModel(v);
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

		if (col == RUNUNIT || col == WAITUNIT || col == PAUSE || col == WPULSE || col == RPULSE)
			rtrn = String.class;
		else
			rtrn = Object.class;

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
			TimeFrameGroup group = vector.elementAt(row);
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
			case PAUSE:
				rtrn = group.getPause();
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
			TimeFrameGroup group = vector.elementAt(row);
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
				case PAUSE:
					group.setPause((String) value);
					fireTableCellUpdated(row + 1, PAUSE);
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
					logger.debug("TimeFrameTableModel: Error in setValueAt, row=" + row + ", col=" + col);
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
	public Vector<TimeFrameGroup> getVector() {
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
		TimeFrameGroup group = new TimeFrameGroup(row);
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
		for (TimeFrameGroup group : vector) {
			total += group.getFrames();
		}
		return total;
	}

	/**
	 * Configure time frame data in the hardware
	 * 
	 * @param tfg
	 */
	public void configureHardware(gda.device.Timer tfg) {
		try {
			if (getTotalFrames() > tfg.getMaximumFrames()) {
				logger.error("Too many time frames requested for the time frame generator");
			} else {
				tfg.clearFrameSets();
				for (TimeFrameGroup group : vector) {
					tfg.addFrameSet(group.getFrames(), group.getActualWaitTime(), group.getActualRunTime(), group
							.getWaitPort(), group.getRunPort(), group.getWaitPause(), group.getRunPause());
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
			for (TimeFrameGroup group : vector) {
				writer.write("<Frames>" + group.getFrames() + "," + group.getWaitTime() + "," + group.getWaitUnit()
						+ "," + group.getRunTime() + "," + group.getRunUnit() + "," + group.getPause() + ","
						+ group.getWaitPulses() + "," + group.getRunPulses() + "</Frames>");
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
				logger.debug(line);
				if (line.startsWith("<Frames>")) {
					TimeFrameGroup group = new TimeFrameGroup(row + 1);

					StringTokenizer parse = new StringTokenizer(line, "<>,");
					parse.nextToken();

					group.setFrames(Integer.parseInt(parse.nextToken()));
					group.setWaitTime(Integer.parseInt(parse.nextToken()));
					group.setWaitUnit(parse.nextToken());
					group.setRunTime(Integer.parseInt(parse.nextToken()));
					group.setRunUnit(parse.nextToken());
					group.setPause(parse.nextToken());
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
