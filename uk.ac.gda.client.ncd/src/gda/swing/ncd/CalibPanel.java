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

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.factory.Finder;
import gda.rcp.ncd.NcdController;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.server.ncd.detectorsystem.NcdDetectorSystem;
import uk.ac.gda.server.ncd.subdetector.INcdSubDetector;
import uk.ac.gda.server.ncd.subdetector.NcdSubDetector;
import uk.ac.gda.server.ncd.subdetector.NcdWireDetector;

/**
 * An implementation of a calibration channel panel. It only records labels for the RAPID scaler inputs. The whole
 * calibDetector framework is not used currently.
 */
public class CalibPanel extends JPanel implements TableModelListener {
	private static final Logger logger = LoggerFactory.getLogger(CalibPanel.class);

	private int calibSlots;

	private String[] calibLabels;

	private INcdSubDetector calibDetector;

	private static String calLabelFileName = LocalProperties.getVarDir() + "caliblabels.dat";

	private class calTableModel extends AbstractTableModel {

		// table header
		private String[] calTitle = { "Channel", "Source" };

		@Override
		public int getColumnCount() {
			return calTitle.length;
		}

		@Override
		public String getColumnName(int column) {
			return calTitle[column];
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return false;
			}
			if (rowIndex == 0) {
				return false;
			}
			return true;
		}

		@Override
		public int getRowCount() {
			return calibSlots;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				if (rowIndex == 0) {
					return rowIndex + 1;
				}
				return (rowIndex + 1) + "  (vtof " + (rowIndex - 1) + ")";
			}
			if (columnIndex == 1) {
				return calibLabels[rowIndex];
			}
			return null;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			String foo;

			if (columnIndex == 1) {
				foo = (String) aValue;
				// remove the strangest characters
				foo = foo.replace("'", " ");
				foo = foo.replace("\\", " ");
				calibLabels[rowIndex] = foo.trim();

				fireTableDataChanged(); // no need to be specific
			}
		}
	}

	/**
	 * empty constructor
	 */
	public CalibPanel() {
	}

	/**
	 * Configure the panel
	 * @throws DeviceException 
	 */
	public void configure() throws DeviceException {
		JPanel panel = new JPanel();

		String name = NcdController.getInstance().getDetectorName(NcdDetectorSystem.CALIBRATION_DETECTOR);
		calibDetector = (INcdSubDetector) Finder.getInstance().find(name);

		int[] dims = calibDetector.getDataDimensions();
		
		calibSlots = dims[0] * dims[1];
		
		calibLabels = new String[calibSlots];
		
		load();

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		TableModel tmod = new calTableModel();
		tmod.addTableModelListener(this);
		JTable gpanel = new JTable(tmod);

		panel.add(gpanel.getTableHeader(), BorderLayout.NORTH);
		panel.add(gpanel, BorderLayout.CENTER);

		panel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);
		setBorder(BorderFactory.createEtchedBorder());

	}

	@Override
	public void tableChanged(TableModelEvent e) {
		// set extra header lines when user finished editing
		try {
			String description = StringUtils.join(calibLabels, "\r\n");
			calibDetector.setAttribute(NcdWireDetector.descriptionLabel, description);
		} catch (DeviceException e1) {
			logger.error("could not set ExtraHeaders: ", e1.getMessage());
		}
		autoSave();
	}

	/**
	 * load the calibration labels to have persistence over sessions
	 */
	private void load() {
		try {
			BufferedReader reader;

			int index = 0;
			String line;
			reader = new BufferedReader(new FileReader(calLabelFileName));
			while ((line = reader.readLine()) != null) {
				calibLabels[index] = line.trim();
				index++;
				if (index > calibLabels.length) {
					break;
				}
			}
			reader.close();
		} catch (Exception e) {
			logger.debug("exception loading file (no need to worry usually):", e.getMessage());
		}
		calibLabels[0]="Timer (10ns)";
		tableChanged(null); // set/update ExtraHeaders
	}

	/**
	 * Save the parameters used to create the x axis
	 */
	private void autoSave() {
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(calLabelFileName));
			for (String calibLabel : calibLabels) {
				if (calibLabel != null) {
					writer.write(calibLabel);
				}
				writer.newLine();
			}
			writer.close();
		} catch (Exception e) {
			logger.error("autoSave: ", e.getMessage());
		}
	}
}