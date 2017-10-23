/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.motor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

/**
 * MotorPositionFileReader Class
 */
public class MotorPositionFileReader extends JFrame {
	/**
	 *
	 */
	public static class MotposGuiPanel extends JPanel {

		private JButton buttonClear;

		private JButton buttonGet;

		private JLabel labelFilePath;

		private JLabel labelFileName;

		private JLabel labelStoredPosition;

		private JPanel panelStoredValues;

		private JPanel panelFileParameters;

		private JTextField fieldFilePath;

		private JTextField fieldFileName;

		private JTextField fieldStoredPosition;

		private double positionValue = 0.0;

		private String fileName = null;

		private String filePath = null;

		private String separator = System.getProperty("file.separator");

		/**
		 * Constructor
		 */
		public MotposGuiPanel() {
			buttonClear = new JButton("Clear");
			buttonGet = new JButton("Get");

			labelFileName = new JLabel("File Name:");
			labelFilePath = new JLabel("File Path:");

			labelStoredPosition = new JLabel("Motor Position (steps)");

			fieldFileName = new JTextField(30);
			fieldFilePath = new JTextField(30);

			fieldStoredPosition = new JTextField(20);
			fieldStoredPosition.setEditable(false);

			buttonClear.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					clear();
				}
			});

			buttonGet.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					get();
				}
			});

			setLayout(new BorderLayout());
			add(createFileParametersPanel(), BorderLayout.NORTH);
			add(createStoredValuesPanel(), BorderLayout.CENTER);
		}

		/**
		 * This method clears the text boxes in the panel for stored values.
		 */
		private void clear() {
			fieldStoredPosition.setText("");
		}

		/**
		 * Creates and sets layout for the file parameters panel
		 *
		 * @return panelFileParameters
		 */
		private JPanel createFileParametersPanel() {
			// Set layout of file parameters panel
			panelFileParameters = new JPanel();
			panelFileParameters.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();

			int oldfill = c.fill;

			c.gridx = GridBagConstraints.RELATIVE;
			c.gridy = GridBagConstraints.RELATIVE;

			c.gridwidth = 1;
			panelFileParameters.add(labelFilePath, c);

			c.gridwidth = GridBagConstraints.REMAINDER;
			c.fill = GridBagConstraints.HORIZONTAL;
			panelFileParameters.add(fieldFilePath, c);
			c.fill = oldfill;

			c.gridwidth = 1;
			panelFileParameters.add(labelFileName, c);

			c.gridwidth = GridBagConstraints.REMAINDER;
			c.fill = GridBagConstraints.HORIZONTAL;
			panelFileParameters.add(fieldFileName, c);
			c.fill = oldfill;

			return panelFileParameters;
		}

		/**
		 * Creates and sets out the panel for the stored values to be displayed.
		 *
		 * @return panelStoredValues
		 */
		private JPanel createStoredValuesPanel() {
			panelStoredValues = new JPanel();
			panelStoredValues.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
					"Get Stored Values", TitledBorder.LEFT, TitledBorder.TOP, null, Color.black));
			panelStoredValues.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();

			int oldfill = c.fill;

			c.gridx = GridBagConstraints.RELATIVE;
			c.gridy = GridBagConstraints.RELATIVE;

			c.gridwidth = 1;
			panelStoredValues.add(labelStoredPosition, c);

			c.gridwidth = GridBagConstraints.REMAINDER;
			c.fill = GridBagConstraints.HORIZONTAL;
			panelStoredValues.add(fieldStoredPosition, c);
			c.fill = oldfill;

			c.gridwidth = 1;
			panelStoredValues.add(buttonGet, c);

			c.gridwidth = 1;
			panelStoredValues.add(buttonClear, c);

			return panelStoredValues;
		}

		/**
		 * Gets and checks values exist in the file and directory path fields. Opens a binary file for reading and reads
		 * the values. These values are then displayed on the GUI
		 */
		protected void get() {
			if (validateFileFields()) {
				String limitStore = filePath + separator + fileName;

				ObjectInputStream in = null;
				try {
					in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(limitStore)));

					positionValue = in.readDouble();
					in.close();
				} catch (IOException ioe) {
					JOptionPane.showMessageDialog(panelFileParameters,
							"An exception occurred while trying to read the file. " + ioe.getMessage());
				} catch (Exception e) {
					JOptionPane.showMessageDialog(panelFileParameters, "Object not found within the file. "
							+ e.getMessage());
				}

				try {
					fieldStoredPosition.setText(Double.toString(positionValue));
				} catch (Exception e) {
					JOptionPane.showMessageDialog(panelFileParameters,
							"An exception occurred while setting the values to the Object. " + e.getMessage());
				}
			}
		}

		/**
		 * Checks to whether text fields have content and prompts for action if not.
		 *
		 * @return valid if content is present in both fields
		 */
		private boolean validateFileFields() {
			boolean valid = false;
			fileName = fieldFileName.getText();
			filePath = fieldFilePath.getText();
			if (fileName == null || fileName == "") {
				JOptionPane.showMessageDialog(panelStoredValues, "Please enter a file name.");
			} else if (filePath == null || filePath == "") {
				JOptionPane.showMessageDialog(panelStoredValues, "Please enter a file path.");
			} else {
				valid = true;
			}
			return valid;
		}
	}

	/**
	 *
	 */
	public MotorPositionFileReader() {
		super("Motor Position Reader");
		MotposGuiPanel motposGuiPanel = new MotposGuiPanel();
		getContentPane().add(motposGuiPanel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(500, 150);
		setVisible(true);
	}

	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		new MotorPositionFileReader();

	}

}
