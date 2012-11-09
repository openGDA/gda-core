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
import gda.rcp.ncd.ExptDataModel;
import gda.rcp.ncd.NcdController;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to implement a time frame profile
 */
public class TimeFrameProfile1 extends TimeFrameProfile implements TableModelListener {
	private static final Logger logger = LoggerFactory.getLogger(TimeFrameProfile1.class);

	private JTable table;

	private TimeFrameTableModel tableModel;

	private Timer timer = null;

	private JTextField frameField;

	private JTextField cycleField;

	private JRadioButton softwareStart;

	private JRadioButton hardwareStart;

	private JRadioButton softStartHardTrigger;

	private JCheckBox extInhibit;

	/**
	 * Constructor
	 */
	public TimeFrameProfile1() {
		this(new TimeFrameTableModel());
	}

	private TimeFrameProfile1(TimeFrameTableModel tableModel) {
		this.tableModel = tableModel;

		table = new JTable(this.tableModel);
		table.setGridColor(Color.blue);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableModel.addTableModelListener(this);
		// table.addMouseListener(this);

		TableColumnModel tcm = table.getColumnModel();
		TableColumn tc = tcm.getColumn(TimeFrameTableModel.WAITUNIT);
		tc.setCellEditor(new UnitCellEditor(TimeFrameTableModel.displayUnits));

		TableColumn tc2 = tcm.getColumn(TimeFrameTableModel.RUNUNIT);
		tc2.setCellEditor(new UnitCellEditor(TimeFrameTableModel.displayUnits));

		TableColumn tc3 = tcm.getColumn(TimeFrameTableModel.PAUSE);
		tc3.setCellEditor(new BinaryCellEditor(TimeFrameTableModel.displayPause));
		// tc3.setCellEditor(new
		// PauseCellEditor(TimeFrameTableModel.displayPause));

		TableColumn tc4 = tcm.getColumn(TimeFrameTableModel.WPULSE);
		tc4.setCellEditor(new BinaryCellEditor(TimeFrameTableModel.displayOutputs));

		TableColumn tc5 = tcm.getColumn(TimeFrameTableModel.RPULSE);
		tc5.setCellEditor(new BinaryCellEditor(TimeFrameTableModel.displayOutputs));

		JScrollPane scrollPane = new JScrollPane(table);

		table.setPreferredScrollableViewportSize(new Dimension(table.getPreferredScrollableViewportSize().width,
				5 * (table.getRowHeight() + 1)));

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(scrollPane);

		JPanel displayPanel = createDisplayPanel();
		JPanel accessPanel = createAccessPanel();

		add(Box.createVerticalStrut(5));
		add(displayPanel);
		add(Box.createVerticalStrut(5));
		add(accessPanel);

		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(), BorderFactory
				.createEmptyBorder(3, 3, 3, 3)));

		// force update of total frame count when creating/copying profiles
		tableModel.fireTableRowsInserted(0, 0);
	}

	@Override
	public TimeFrameProfile1 copy() {
		return new TimeFrameProfile1(tableModel.copy());
	}

	private JPanel createDisplayPanel() {
		JPanel panel = new JPanel();
		ButtonGroup buttonGroup = new ButtonGroup();

		panel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 0;
		constraints.gridy = 0;
		panel.add(createLabel("Total Frames  "), constraints);

		frameField = new JTextField(5);
		frameField.setForeground(ColorSetting.NORMALCOLOR);
		frameField.setBackground(Color.white);
		frameField.setEditable(false);
		frameField.setToolTipText("Display total frames number");
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.NONE;
		++constraints.gridx;
		panel.add(frameField, constraints);

		++constraints.gridx;
		softwareStart = new JRadioButton("Software Start", true);
		panel.add(softwareStart, constraints);
		buttonGroup.add(softwareStart);

		++constraints.gridx;
		constraints.fill = GridBagConstraints.NONE;
		constraints.weightx = 0.0;

		hardwareStart = new JRadioButton("Hardware trigger start", false);
		panel.add(hardwareStart, constraints);
		buttonGroup.add(hardwareStart);

		++constraints.gridx;
		constraints.fill = GridBagConstraints.NONE;
		constraints.weightx = 0.0;

		softStartHardTrigger = new JRadioButton("Software start + hardware trigger", false);
		panel.add(softStartHardTrigger, constraints);
		buttonGroup.add(softStartHardTrigger);

		constraints.weightx = 0.0;
		++constraints.gridy;
		constraints.gridx = 0;
		constraints.fill = GridBagConstraints.NONE;
		panel.add(createLabel("No. of Cycles  "), constraints);

		cycleField = new JTextField("1", 5);
		cycleField.setForeground(ColorSetting.NORMALCOLOR);
		cycleField.setToolTipText("Type in No. of cycles");
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.NONE;
		++constraints.gridx;
		panel.add(cycleField, constraints);

		++constraints.gridx;
		constraints.fill = GridBagConstraints.NONE;
		constraints.weightx = 0.0;

		extInhibit = new JCheckBox("External Inhibit", false);
		panel.add(extInhibit, constraints);

		return panel;
	}

	private JPanel createAccessPanel() {
		JButton addButton, deleteButton;
		JPanel panel = new JPanel();

		panel.setLayout(new FlowLayout(FlowLayout.CENTER));

		addButton = new JButton("Insert Group");
		addButton.setToolTipText("Insert a new group next current group on time frame profile");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				tableModel.insertAfter(table.getSelectedRow());
			}
		});

		deleteButton = new JButton("Delete Group");
		deleteButton.setToolTipText("Delete the current group from time frame profile");
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				tableModel.delete(table.getSelectedRow());
			}
		});

		panel.add(addButton);
		panel.add(deleteButton);
		return panel;
	}

	@Override
	public void displayTotalFrames() {
		int frames = tableModel.getTotalFrames();
		String text = String.valueOf(frames);
		frameField.setText(text);
		MemoryUsage.getInstance().setTimeFrameCount(frames);
		ExptDataModel.getInstance().setTotalFrames(frames);
	}

	private JLabel createLabel(String name) {
		JLabel label = new JLabel(name);
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setForeground(Color.black);
		return label;
	}

	@Override
	public void tableChanged(TableModelEvent ev) {
		if (ev.getType() == TableModelEvent.DELETE || ev.getType() == TableModelEvent.INSERT
				|| ev.getColumn() == TimeFrameTableModel.NFRAME) {
			displayTotalFrames();
		}
	}

	@Override
	public void configureHardware() {
		if (timer == null) {
			logger.debug("TimeFrameConfigure: Finding TimeFrameGenerator");
			timer = NcdController.getInstance().getTfg();
		}
		if (timer != null) {
			try {
				if (timer.getStatus() == gda.device.Timer.IDLE) {
					int cycleCount = 1;

					timer.setAttribute("Ext-Start", new Boolean(!softwareStart.isSelected()));
					timer.setAttribute("VME-Start", new Boolean(!hardwareStart.isSelected()));
					timer.setAttribute("Ext-Inhibit", new Boolean(extInhibit.isSelected()));
					try {
						cycleCount = Integer.parseInt(cycleField.getText());
					} catch (NumberFormatException nfe) {
					}
					timer.setCycles(cycleCount);
					tableModel.configureHardware(timer);
				} else {
					logger.debug("Timer is not idle");
				}
			} catch (DeviceException e) {
				logger.error("TimeFrameProfile: configureHardware: " + e);
			}
		} else {
			logger.error("TimeFrameProfile: configureHardware: No TFG");
		}

	}

	@Override
	public void save(BufferedWriter writer) {
		try {
			writer.write("Cycles=" + cycleField.getText() + ",SoftwareStart=" + softwareStart.isSelected()
					+ ",HardwareTrigger=" + hardwareStart.isSelected() + ",SoftStartWithHardtrigger="
					+ softStartHardTrigger.isSelected() + ",Ext-Inhibit=" + extInhibit.isSelected());
			writer.newLine();
			tableModel.save(writer);
		} catch (Exception e) {
			logger.error("save: " + e);
		}
	}

	@Override
	public void load(BufferedReader reader) {
		try {
			String line = reader.readLine();
			logger.debug(line);
			StringTokenizer parse = new StringTokenizer(line, "<>,=");
			while (parse.hasMoreTokens()) {
				String token = parse.nextToken();
				if (token.equals("Cycles")) {
					cycleField.setText(parse.nextToken());
				} else if (token.equals("SoftwareStart")) {
					softwareStart.setSelected(Boolean.valueOf(parse.nextToken()).booleanValue());
				} else if (token.equals("HardwareTrigger")) {
					hardwareStart.setSelected(Boolean.valueOf(parse.nextToken()).booleanValue());
				} else if (token.equals("SoftStartWithHardtrigger")) {
					softStartHardTrigger.setSelected(Boolean.valueOf(parse.nextToken()).booleanValue());
				} else if (token.equals("Ext-Inhibit")) {
					extInhibit.setSelected(Boolean.valueOf(parse.nextToken()).booleanValue());
				}
			}
			tableModel.load(reader);
		} catch (Exception e) {
			logger.error("TimeFrameProfile: load: " + e);
		}
	}
}
