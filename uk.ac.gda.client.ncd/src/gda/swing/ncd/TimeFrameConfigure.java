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

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of a panel for time-frame configuration.
 */
public class TimeFrameConfigure extends JPanel implements ChangeListener {
	private static final Logger logger = LoggerFactory.getLogger(TimeFrameConfigure.class);

	private JTabbedPane tabbedPane;

	private boolean extendedTFG = true;

	private JFileChooser jf; 

	/**
	 * Create the configure panel
	 */
	public TimeFrameConfigure() {
		tabbedPane = new JTabbedPane();
		tabbedPane.addChangeListener(this);

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		add(tabbedPane);
		add(Box.createVerticalStrut(5));
		add(createAccessPanel());
		add(Box.createVerticalStrut(5));
		add(MemoryUsage.getInstance());
		add(Box.createVerticalStrut(5));
		add(createButtonPanel());
		
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Time Frame Configure",
				TitledBorder.LEFT, TitledBorder.TOP, null, Color.black));
	}

	/**
	 * Configure the configurePanel
	 */
	public void configure() {
		TimeFrameProfile timeFrameProfile;
		if (extendedTFG)
			timeFrameProfile = new TimeFrameProfile2();
		else
			timeFrameProfile = new TimeFrameProfile1();

		tabbedPane.addTab("Profile 1", timeFrameProfile);
		
		ParameterFileController.getInstance().add(this);
	}

	private JPanel createButtonPanel() {
		JPanel panel;

		String filter[] = { "dat", "txt" };
		jf = new JFileChooser(System.getProperty("user.dir"));
		jf.addChoosableFileFilter(new SimpleFileFilter(filter, "Data (*.dat, *.txt)"));

		panel = new JPanel(new FlowLayout(FlowLayout.CENTER));

		JButton loadButton = new JButton("Load");
		loadButton.setToolTipText("Load Parameters");
		loadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				if (jf.showOpenDialog(getRootPane()) == JFileChooser.APPROVE_OPTION) {
					if (jf.getSelectedFile() != null) {
						String fileName = jf.getSelectedFile().getAbsolutePath();
						logger.debug("ParameterFileController: Load file " + fileName);
						ParameterFileController.getInstance().load(new File(fileName));
					}
				}
			}
		});

		JButton saveButton = new JButton("Save");
		saveButton.setToolTipText("Save Parameters");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				if (jf.showSaveDialog(getRootPane()) == JFileChooser.APPROVE_OPTION) {
					if (jf.getSelectedFile() != null) {
						String fileName = jf.getSelectedFile().getAbsolutePath();
						logger.debug("ParameterFileController: Save file " + fileName);
						ParameterFileController.getInstance().save(new File(fileName));
					}
				}
			}
		});

		JButton configureButton = new JButton("Configure");
		configureButton.setToolTipText("Configure Parameters");
		configureButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				configureHardware();
			}
		});

		panel.add(loadButton);
		panel.add(saveButton);
		panel.add(configureButton);

		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory
				.createEmptyBorder(1, 1, 1, 1)));

		return panel;
	}
	
	private JPanel createAccessPanel() {
		JButton addButton;
		JButton delButton;
		JButton copButton;
		JPanel panel = new JPanel();

		panel.setLayout(new FlowLayout(FlowLayout.CENTER));

		addButton = new JButton("New Profile");
		addButton.setToolTipText("Insert a new time frame profile after the current selection");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				TimeFrameProfile timeFrameProfile;
				if (extendedTFG)
					timeFrameProfile = new TimeFrameProfile2();
				else
					timeFrameProfile = new TimeFrameProfile1();

				int index = tabbedPane.getSelectedIndex() + 1;
				tabbedPane.add(timeFrameProfile, index);
				tabbedPane.setSelectedComponent(timeFrameProfile);
				reAllocateTabNames();

				logger.debug("TimeFrameConfigure: Adding profile tab at index=" + tabbedPane.getSelectedIndex()
						+ ", tabcount=" + tabbedPane.getTabCount() + ", tab place=" + tabbedPane.getTabPlacement());
			}
		});

		copButton = new JButton("Copy Profile");
		copButton.setToolTipText("Insert a copy of the selected time frame profile after it");
		copButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				int index = tabbedPane.getSelectedIndex();
				TimeFrameProfile timeFrameProfile = ((TimeFrameProfile) tabbedPane.getComponentAt(index)).copy();

				tabbedPane.add(timeFrameProfile, index + 1);
				tabbedPane.setSelectedComponent(timeFrameProfile);
				reAllocateTabNames();

				logger.debug("TimeFrameConfigure: Copying profile tab at index=" + tabbedPane.getSelectedIndex()
						+ ", tabcount=" + tabbedPane.getTabCount() + ", tab place=" + tabbedPane.getTabPlacement());
			}
		});

		delButton = new JButton("Delete Profile");
		delButton.setToolTipText("Delete the selected time frame profile");
		delButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				int count = tabbedPane.getTabCount() - 1;
				if (count > 0) {
					TimeFrameProfile tfp;
					tabbedPane.removeTabAt(tabbedPane.getSelectedIndex());
					reAllocateTabNames();
					tfp = (TimeFrameProfile) tabbedPane.getSelectedComponent();
					tfp.displayTotalFrames();
				}
			}
		});

		panel.add(addButton);
		panel.add(copButton);
		panel.add(delButton);
		return panel;
	}

	/**
	 * re-allocate the tabs names
	 */
	private void reAllocateTabNames() {
		int count = tabbedPane.getTabCount();
		for (int i = 0; i < count; i++) {
			int j = i + 1;
			String name = "Profile " + j;
			tabbedPane.setTitleAt(i, name);
		}
	}

	@Override
	public void stateChanged(ChangeEvent ev) {
		if (tabbedPane.getTabCount() > 0) {
			TimeFrameProfile tfp;
			JTabbedPane tabbedPane = (JTabbedPane) ev.getSource();
			tfp = (TimeFrameProfile) tabbedPane.getSelectedComponent();
			tfp.displayTotalFrames();
		}
	}

	/**
	 * Configure the TFG hardware
	 */
	public void configureHardware() {
		TimeFrameProfile tfp = (TimeFrameProfile) tabbedPane.getSelectedComponent();
		try {
			tfp.configureHardware();
		} catch (DeviceException e) {
			logger.error("Error in configureHardware:",e);
			JOptionPane.showOptionDialog(new JFrame("too bad"), "Something went wrong.\n\nError message is: \""+e.getMessage()+"\"\n\n",
					"TFG Config Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[] {
					"Close" }, "Close");
		}
	}

	/**
	 * Save the configured TFG parameters to an XML file
	 * 
	 * @param writer
	 */
	public void save(BufferedWriter writer) {
		try {
			writer.write("<Timing>");
			writer.newLine();
			int count = tabbedPane.getTabCount();
			for (int i = 0; i < count; i++) {
				writer.write("<Profile>" + (i + 1) + ",");
				writer.newLine();
				TimeFrameProfile tfp = (TimeFrameProfile) tabbedPane.getComponentAt(i);
				tfp.save(writer);
				writer.write("</Profile>");
				writer.newLine();
			}
			writer.write("</Timing>");
			writer.newLine();
		} catch (Exception e) {
			logger.error("TimeFrameCofigure: save: " + e);
		}
	}

	/**
	 * Load the saved TFG parameters from an xml file
	 * 
	 * @param reader
	 */
	public void load(BufferedReader reader) {
		try {
			tabbedPane.removeChangeListener(this);
			// remove all existing profiles
			tabbedPane.removeAll();

			String line;
			while ((line = reader.readLine()) != null) {
				// logger.debug(line);
				if (line.startsWith("<Profile>")) {
					TimeFrameProfile tfp;
					if (extendedTFG)
						tfp = new TimeFrameProfile2();
					else
						tfp = new TimeFrameProfile1();

					StringTokenizer parse = new StringTokenizer(line, "<>,");
					String name = parse.nextToken() + " " + parse.nextToken();
					tabbedPane.add(name, tfp);
					tfp.load(reader);
				} else if (line.startsWith("</Timing>")) {
					break;
				}
			}
			tabbedPane.addChangeListener(this);
		} catch (Exception e) {
			logger.error("TimeFrameConfigure: load: " + e);
		}
	}

	/**
	 * @param extendedTFG
	 *            The extendedTFG to set.
	 */
	public void setExtendedTFG(boolean extendedTFG) {
		this.extendedTFG = extendedTFG;
	}
}