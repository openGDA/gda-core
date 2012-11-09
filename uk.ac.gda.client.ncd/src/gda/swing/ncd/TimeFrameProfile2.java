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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
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
public class TimeFrameProfile2 extends TimeFrameProfile implements TableModelListener {
	private static final Logger logger = LoggerFactory.getLogger(TimeFrameProfile2.class);

	private Timer timer = null;

	private JTable table;
	private TimeFrameTableModel2 tableModel;
	private TriggerTableModel ttm;
	private OutputTriggerTableModel ottm;

	private JTextField cycleField;
	private JTextField frameField;
	private JTextField timeField;

	private JCheckBox extInhibit;
	private JButton startMethod;
	private StartMenu menu;
	private JButton triggerMethod;
	private JPopupMenu triggerMenu;
	private JButton oTriggerMethod;
	private JPopupMenu oTriggerMenu;

	/**
	 * Constructor
	 */
	public TimeFrameProfile2() {
		this(new TimeFrameTableModel2(), new TriggerTableModel(), new OutputTriggerTableModel());
	}

	private TimeFrameProfile2(TimeFrameTableModel2 tableModel, TriggerTableModel ttm, OutputTriggerTableModel ottm) {
		this.tableModel = tableModel;
		this.ttm = ttm;
		this.ottm = ottm;

		table = new JTable(this.tableModel);
		table.setGridColor(Color.blue);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableModel.addTableModelListener(this);

		TableColumnModel tcm = table.getColumnModel();
		TableColumn tc = tcm.getColumn(TimeFrameTableModel2.WAITUNIT);
		tc.setCellEditor(new UnitCellEditor(TimeFrameTableModel2.displayUnits));

		TableColumn tc1 = tcm.getColumn(TimeFrameTableModel2.RUNUNIT);
		tc1.setCellEditor(new UnitCellEditor(TimeFrameTableModel2.displayUnits));

		TableColumn tc2 = tcm.getColumn(TimeFrameTableModel2.WAITPAUSE);
		tc2.setCellEditor(new PauseCellEditor(TimeFrameTableModel2.displayPause));

		TableColumn tc3 = tcm.getColumn(TimeFrameTableModel2.RUNPAUSE);
		tc3.setCellEditor(new PauseCellEditor(TimeFrameTableModel2.displayPause));

		TableColumn tc4 = tcm.getColumn(TimeFrameTableModel2.WPULSE);
		tc4.setCellEditor(new BinaryCellEditor(TimeFrameTableModel2.displayOutputs));

		TableColumn tc5 = tcm.getColumn(TimeFrameTableModel2.RPULSE);
		tc5.setCellEditor(new BinaryCellEditor(TimeFrameTableModel2.displayOutputs));

		JScrollPane scrollPane = new JScrollPane(table);

		table.setPreferredScrollableViewportSize(new Dimension(table.getPreferredScrollableViewportSize().width,
				6 * (table.getRowHeight() + 1)));

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
	public TimeFrameProfile copy() {
		return new TimeFrameProfile2(tableModel.copy(), ttm.copy(), ottm.copy());
	}

	private JPanel createDisplayPanel() {
		JPanel panel = new JPanel();

		panel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets(1, 1, 5, 1);

		constraints.fill = GridBagConstraints.NONE;
		panel.add(createLabel("Start Method"), constraints);

		++constraints.gridx;
		menu = new StartMenu();
		startMethod = new JButton(menu.getText());
		startMethod.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				menu.show(startMethod, 0, startMethod.getHeight());
			}
		});
		panel.add(startMethod, constraints);

		++constraints.gridx;
		extInhibit = new JCheckBox("External Inhibit", false);
		panel.add(extInhibit, constraints);

		++constraints.gridx;
		++constraints.gridx;
		triggerMenu = ttm.getPopupTable();
		triggerMethod = new JButton("Input Trigger Details");
		triggerMethod.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				triggerMenu.show(triggerMethod, 0, triggerMethod.getHeight());
			}
		});
		panel.add(triggerMethod, constraints);

		++constraints.gridx;
		oTriggerMenu = ottm.getPopupTable();
		oTriggerMethod = new JButton("Output Trigger Details");
		oTriggerMethod.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				oTriggerMenu.show(oTriggerMethod, 0, oTriggerMethod.getHeight());
			}
		});
		panel.add(oTriggerMethod, constraints);

		constraints.gridx = 0;
		++constraints.gridy;
		constraints.fill = GridBagConstraints.NONE;
		panel.add(createLabel("No. of Cycles  "), constraints);

		++constraints.gridx;
		constraints.fill = GridBagConstraints.NONE;
		cycleField = new JTextField("1", 5);
		cycleField.setForeground(ColorSetting.NORMALCOLOR);
		cycleField.setToolTipText("Type in No. of cycles");
		panel.add(cycleField, constraints);

		++constraints.gridx;
		constraints.fill = GridBagConstraints.NONE;
		panel.add(createLabel("Total Frames  "), constraints);

		++constraints.gridx;
		constraints.fill = GridBagConstraints.NONE;
		frameField = new JTextField(5);
		frameField.setForeground(ColorSetting.NORMALCOLOR);
		frameField.setBackground(Color.white);
		frameField.setEditable(false);
		frameField.setToolTipText("Display total frames number");
		panel.add(frameField, constraints);

		++constraints.gridx;
		constraints.fill = GridBagConstraints.NONE;
		panel.add(createLabel("Total Time  "), constraints);

		++constraints.gridx;
		constraints.fill = GridBagConstraints.NONE;
		timeField = new JTextField(15);
		timeField.setForeground(ColorSetting.NORMALCOLOR);
		timeField.setBackground(Color.white);
		timeField.setEditable(false);
		panel.add(timeField, constraints);

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

		double tt = tableModel.getTotalTime();
		text = formatDuration(tt);
		timeField.setText(text);

		MemoryUsage.getInstance().setTimeFrameCount(frames);
		ExptDataModel.getInstance().setTotalFrames(frames);
	}

	private static String formatDuration(double dur) {
		StringBuilder sb = new StringBuilder("");

		int min = (int) (dur / 60);
		
		if (min > 0) {
			sb.append(String.format("%d min ", min));
			dur -= min *60;
		}
		sb.append(String.format("%5.3f sec", dur % 60));

		return sb.toString();
	}

	private JLabel createLabel(String name) {
		JLabel label = new JLabel(name);
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setForeground(Color.black);
		return label;
	}

	@Override
	public void tableChanged(TableModelEvent ev) {
		displayTotalFrames();
	}

	@Override
	public void configureHardware() throws DeviceException {
		if (timer == null) {
			logger.debug("TimeFrameConfigure: Finding TimeFrameGenerator");
			timer = NcdController.getInstance().getTfg();
		}
		if (timer == null) {
			throw new DeviceException("No timer found!");
		}
		if (timer.getStatus() != gda.device.Timer.IDLE) {
			throw new DeviceException("Cannot configure while TFG running!");
		}
		int cycleCount = 1;

		timer.setAttribute("Debounce", ttm.getDebounceValues());
		timer.setAttribute("Threshold", ttm.getThresholdValues());
		timer.setAttribute("Inversion", ottm.getInversionValue());
		timer.setAttribute("Drive", ottm.getDriveValue());
		timer.setAttribute("Start-Method", new Integer(menu.getValue()));
		timer.setAttribute("Ext-Inhibit", new Boolean(extInhibit.isSelected()));
		try {
			cycleCount = Integer.parseInt(cycleField.getText());
		} catch (NumberFormatException nfe) {
			// seems to default to 1 in that case
		}
		timer.setCycles(cycleCount);
		tableModel.configureHardware(timer);
	}

	@Override
	public void save(BufferedWriter writer) {
		try {
			writer.write("Cycles=" + cycleField.getText() + ",Start-Method=" + menu.getValue() + ",Ext-Inhibit="
					+ extInhibit.isSelected());

			writer.newLine();
			ttm.save(writer);
			ottm.save(writer);
			tableModel.save(writer);
		} catch (Exception e) {
			logger.error("save: " + e);
		}
	}

	@Override
	public void load(BufferedReader reader) {
		try {
			String line = reader.readLine();
			// logger.debug(line);
			StringTokenizer parse = new StringTokenizer(line, "<>,=");
			while (parse.hasMoreTokens()) {
				String token = parse.nextToken();
				if (token.equals("Cycles")) {
					cycleField.setText(parse.nextToken());
				} else if (token.equals("Start-Method")) {
					menu.setValue(Integer.valueOf(parse.nextToken()).intValue());
				} else if (token.equals("Ext-Inhibit")) {
					extInhibit.setSelected(Boolean.valueOf(parse.nextToken()).booleanValue());
				}
			}
			ttm.load(reader);
			ottm.load(reader);
			tableModel.load(reader);
		} catch (Exception e) {
			logger.error("TimeFrameProfile: load: " + e);
		}
	}

	/**
	 * A class to control all option selectable start methods of TFG2.
	 */
	public class StartMenu extends JPopupMenu {
		private JMenuItem subMenu, subMenu2;

		private String[] iconName = { "leadingEdge.gif", "fallingEdge.gif" };

		private String[] startLabelList = { "Software", "\u2191 BM Trigger", "\u2191 ADC chan 0", "\u2191 ADC chan 1",
				"\u2191 ADC chan 2", "\u2191 ADC chan 3", "\u2191 ADC chan 4", "\u2191 ADC chan 5",
				"\u2191 TTL trig 0", "\u2191 TTL trig 1", "\u2191 TTL trig 2", "\u2191 TTL trig 3",
				"\u2191 LVDS Lemo ", "\u2191 TFG cable 1", "\u2191 TFG cable 2", "\u2191 TFG cable 3",
				"\u2191 Var thrshhld", "\u2193 BM Trigger", "\u2193 ADC chan 0", "\u2193 ADC chan 1",
				"\u2193 ADC chan 2", "\u2193 ADC chan 3", "\u2193 ADC chan 4", "\u2193 ADC chan 5",
				"\u2193 TTL trig 0", "\u2193 TTL trig 1", "\u2193 TTL trig 2", "\u2193 TTL trig 3",
				"\u2193 LVDS Lemo ", "\u2193 TFG cable 1", "\u2193 TFG cable 2", "\u2193 TFG cable 3",
				"\u2193 Var thrshhld" };

		private String selectedText;

		private ButtonGroup group;

		/**
		 * Create Start menu comprising submenu of all possible trigger sources.
		 */
		public StartMenu() {
			group = new ButtonGroup();
			selectedText = startLabelList[0];

			JRadioButtonMenuItem rbMenuItem1 = new JRadioButtonMenuItem(startLabelList[0], true);
			rbMenuItem1.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					JMenuItem mi = (JMenuItem) e.getSource();
					selectedText = mi.getText();
					startMethod.setText(selectedText);
				}
			});
			group.add(rbMenuItem1);
			add(rbMenuItem1);

			subMenu = new JMenu("Signal");
			subMenu.setIcon(new ImageIcon(getClass().getResource(iconName[0])));

			for (int i = 1; i < 17; i++) {
				JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem(startLabelList[i], false);
				rbMenuItem.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						JMenuItem mi = (JMenuItem) e.getSource();
						selectedText = mi.getText();
						startMethod.setText(selectedText);
					}
				});
				subMenu.add(rbMenuItem);
				group.add(rbMenuItem);
			}

			add(subMenu);

			subMenu2 = new JMenu("Signal");
			subMenu2.setIcon(new ImageIcon(getClass().getResource(iconName[1])));
			for (int i = 17; i < 33; i++) {
				JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem(startLabelList[i], false);
				rbMenuItem.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						JMenuItem mi = (JMenuItem) e.getSource();
						selectedText = mi.getText();
						startMethod.setText(selectedText);
					}
				});
				subMenu2.add(rbMenuItem);
				group.add(rbMenuItem);
			}
			add(subMenu2);
		}

		/**
		 * Get the text representing the selected menu item.
		 * 
		 * @return the text representing the selected menu item
		 */
		public String getText() {
			return selectedText;
		}

		/**
		 * Get an integer representing the selected menu item.
		 * 
		 * @return the value representing the selected menu item
		 */
		public int getValue() {
			int i;
			for (i = 0; i < startLabelList.length; i++) {
				if (startLabelList[i].equals(selectedText)) {
					break;
				}
			}
			return i;
		}

		/**
		 * @param i
		 */
		public void setValue(int i) {
			selectedText = startLabelList[i];
			startMethod.setText(selectedText);
		}
	}
}