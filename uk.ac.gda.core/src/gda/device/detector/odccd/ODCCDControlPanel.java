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

package gda.device.detector.odccd;

import gda.device.ODCCD;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * <b>Title: </b>Control panel GUI for the Oxford Diffraction CCDs.
 * </p>
 * <p>
 * <b>Description: </b>This GUI panel can be used to connect to and control Oxford Diffraction CCDs. Use it to send
 * script commands, read simple parameters and connect/disconnect from the IS software.
 * </p>
 */

public class ODCCDControlPanel extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(ODCCDControlPanel.class);

	private ODCCD mCCD = null;

	private String mHostname = null;

	private JTextField mHostNameField = null;

	private JButton mConnectButton = null;

	private JButton mDisconnectButton = null;

	private JButton mTempButton = null;

	private JButton mWaterTempButton = null;

	private JButton mShutterStatusButton = null;

	private JButton mShutterOpenButton = null;

	private JButton mShutterCloseButton = null;

	private JTextArea mOutputArea = null;

	private JTextField mRunScriptField = null;

	private JButton mRunScriptButton = null;

	// private JButton mISDBDataButton = null;
	private JTextField mISDBDataField = null;

	/**
	 * Contructor. This takes in a reference to the CCD control object.
	 * 
	 * @param ccd
	 */
	public ODCCDControlPanel(ODCCD ccd) {
		super();

		// Set the CCD control object, required in this constructor.
		mCCD = ccd;

		mConnectButton = new JButton("Connect");
		mConnectButton.addActionListener(new ConnectAction());

		mDisconnectButton = new JButton("Disconnect");
		mDisconnectButton.addActionListener(new DisconnectAction());

		mTempButton = new JButton("Temperature");
		mTempButton.addActionListener(new TemperatureAction());

		mWaterTempButton = new JButton("Water Temperature");
		mWaterTempButton.addActionListener(new WaterTemperatureAction());

		mShutterStatusButton = new JButton("Shutter status");
		mShutterStatusButton.addActionListener(new ShutterStatusAction());

		mShutterOpenButton = new JButton("Open shutter");
		mShutterOpenButton.addActionListener(new ShutterOpenAction());

		mShutterCloseButton = new JButton("Close shutter");
		mShutterCloseButton.addActionListener(new ShutterCloseAction());

		JLabel setHostNameLabel = new JLabel("Hostname");
		mHostNameField = new JTextField("diamrz0001.diamond.ac.uk", 25);

		JLabel sendCommandLabel = new JLabel("Script");
		mRunScriptField = new JTextField("call dark_cor 10.0 2 \"//root\"", 25);
		mRunScriptButton = new JButton("Run script");
		mRunScriptButton.addActionListener(new RunScriptAction());

		// JLabel dataDirectoryLabel = new JLabel("IS DB Data");
		// mISDBDataField = new JTextField("Type IS DB directory here", 25);
		// mISDBDataButton = new JButton("Read Data");
		// mISDBDataButton.addActionListener(new ReadISDBDataAction());

		// mDarkCurrentButton = new JButton("1 sec Dark Current");
		// mDarkCurrentButton.addActionListener(new DarkCurrentAction());

		// BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 0, 0);
		// c.fill = GridBagConstraints.VERTICAL;
		this.setLayout(layout);

		// Now add hostname components
		c.gridx = 1;
		c.gridy = 0;
		this.add(setHostNameLabel, c);
		c.gridx = 2;
		c.gridy = 0;
		this.add(mHostNameField, c);
		c.gridx = 3;
		c.gridy = 0;
		this.add(mConnectButton, c);
		this.add(mDisconnectButton, c);
		// If the remote CCD object is already connected then only display the
		// 'disconnect' button.
		if (mCCD.isConnected()) {
			mDisconnectButton.setVisible(true);
			mConnectButton.setVisible(false);
		} else {
			mDisconnectButton.setVisible(false);
			mConnectButton.setVisible(true);
		}

		// Now add send generic command components
		c.gridx = 1;
		c.gridy = 1;
		this.add(sendCommandLabel, c);
		c.gridx = 2;
		c.gridy = 1;
		this.add(mRunScriptField, c);
		c.gridx = 3;
		c.gridy = 1;
		this.add(mRunScriptButton, c);

		// Now add read IS DB data components
		/*
		 * c.gridx = 1; c.gridy = 2; this.add(dataDirectoryLabel, c); c.gridx = 2; c.gridy = 2; this.add(mISDBDataField,
		 * c); c.gridx = 3; c.gridy = 2; this.add(mISDBDataButton, c);
		 */

		// Now add specific command buttons
		c.gridx = 0;
		c.gridy = 0;
		this.add(mTempButton, c);
		c.gridx = 0;
		c.gridy = 1;
		this.add(mWaterTempButton, c);
		c.gridx = 0;
		c.gridy = 2;
		this.add(mShutterStatusButton, c);
		c.gridx = 0;
		c.gridy = 3;
		this.add(mShutterOpenButton, c);
		c.gridx = 0;
		c.gridy = 4;
		this.add(mShutterCloseButton, c);
		// c.gridx = 0;
		// c.gridy = 5;
		// this.add(mDarkCurrentButton, c);

		// Now add the text area at the bottom
		mOutputArea = new JTextArea(20, 70);
		mOutputArea.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(mOutputArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		c.gridx = 0;
		c.gridy = 6;
		c.gridwidth = 4;
		this.add(scrollPane, c);
	}

	/**
	 * Write a message to the output text area.
	 * 
	 * @param msg
	 *            The message to write
	 */
	private void write(String msg) {
		mOutputArea.append(msg);
	}

	/**
	 * Action to connect to the CCD.
	 */
	private class ConnectAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			mHostname = mHostNameField.getText();
			write("Connecting to " + mHostname + "....\n");
			// Try to connect to IS
			try {
				mCCD.connect(mHostname);
				mDisconnectButton.setVisible(true);
				mConnectButton.setVisible(false);
			} catch (IOException e) {
				logger.error("ERROR: Failed to connect to " + mHostname);
			}
		}
	}

	/**
	 * Action to disconnect from the CCD.
	 */
	private class DisconnectAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			int response = JOptionPane.showConfirmDialog(null, "Confirm CCD disconnection", " ",
					JOptionPane.YES_NO_OPTION);
			if (response == 0) {
				if (mCCD.isConnected()) {
					write("Disconnecting from " + mHostname + "....\n");
					mCCD.disconnect();
					mDisconnectButton.setVisible(false);
					mConnectButton.setVisible(true);
				}
			}
		}
	}

	/**
	 * Action to read the temperature from the CCD.
	 */
	private class TemperatureAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (mCCD.isConnected()) {
				write("CCD Temperature: " + mCCD.temperature() + "\n");
			}
		}
	}

	/**
	 * Action to read the water temperature from the CCD.
	 */
	private class WaterTemperatureAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (mCCD.isConnected()) {
				write("Water Temperature: " + mCCD.waterTemperature() + "\n");
			}
		}
	}

	/**
	 * Action to read the shutter status from the CCD.
	 */
	private class ShutterStatusAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (mCCD.isConnected()) {
				write("Shutter Status: " + mCCD.shutter() + "\n");
			}
		}
	}

	/**
	 * Action to close the shutter.
	 */
	private class ShutterCloseAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (mCCD.isConnected()) {
				write("Closing shutter...\n");
				mCCD.closeShutter();
				write("Shutter Status: " + mCCD.shutter() + "\n");
			}
		}
	}

	/**
	 * Action to open the shutter.
	 */
	private class ShutterOpenAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (mCCD.isConnected()) {
				write("Opening shutter...\n");
				mCCD.openShutter();
				write("Shutter Status: " + mCCD.shutter() + "\n");
			}
		}
	}

	/**
	 * Action to run a script on IS.
	 */
	private class RunScriptAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (mCCD.isConnected()) {
				write("Sending command: " + mRunScriptField.getText() + "\n");
				mCCD.runScript(mRunScriptField.getText());
			}
		}
	}

	/**
	 * Action to read a IS data base image file into memory.
	 */
	@SuppressWarnings("unused")
	private class ReadISDBDataAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (mCCD.isConnected()) {
				write("Reading data from IS data base location " + mISDBDataField.getText() + "\n");
				ODCCDImage image = mCCD.readDataFromISDataBase(mISDBDataField.getText());

				// save image
				image.writeToFile("C:/imageFile.img");

			}
		}
	}

	/**
	 * Is the CCD object connected to the CCD?
	 * 
	 * @return true or false
	 */
	public boolean isConnected() {
		return mCCD.isConnected();
	}

	/**
	 * Disconnect the CCD object from the CCD.
	 */
	public void disconnectCCD() {
		mCCD.disconnect();
	}

	/**
	 * Returns the hostname that the CCD object is connected to.
	 * 
	 * @return hostname
	 */
	public String getHostname() {
		return mHostname;
	}

	/**
	 * Set the object reference to the CCD.
	 * 
	 * @param ccd
	 *            The object reference.
	 */
	public void setCCD(ODCCD ccd) {
		mCCD = ccd;
	}

	/*
	 * public ODCCD getCCD() { return mCCD; }
	 */

}
