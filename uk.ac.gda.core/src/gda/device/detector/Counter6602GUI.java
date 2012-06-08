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

package gda.device.detector;

import gda.device.Detector;
import gda.device.DeviceException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * Provides basic user interface to drive National Instruments PCI 6602 counter
 */
public class Counter6602GUI extends JFrame implements Runnable {

	private Thread runner;

	private boolean running = false;

	private javax.swing.JPanel jContentPane = null;

	private static NI6602 CT;

	private JPanel jPanel = null;

	private JButton jButton2 = null;

	private JButton jButton3 = null;

	private JButton jButton4 = null;

	private JButton jButton5 = null;

	private JButton jButton7 = null;

	private JButton jButton = null;

	private JPanel jPanel1 = null;

	private JLabel jLabel = null;

	private JTextField jTextField = null;

	private JLabel jLabel1 = null;

	private JTextField jTextField1 = null;

	private JLabel jLabel2 = null;

	private JTextField jTextField2 = null;

	private JLabel jLabel3 = null;

	private JTextField jTextField3 = null;

	private JLabel jLabel4 = null;

	private JTextField jTextField4 = null;

	private JLabel jLabel5 = null;

	private JTextField jTextField5 = null;

	private JLabel jLabel6 = null;

	private JTextField jTextField6 = null;

	private JLabel jLabel7 = null;

	private JTextField jTextField7 = null;

	private JPanel jPanel2 = null;

	private JCheckBox jCheckBox = null;

	private JLabel jLabel8 = null;

	private JTextField jTextField8 = null;

	private JTextField jTextField9 = null;

	/**
	 * Main Method.
	 * 
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		new Counter6602GUI();
		CT = new NI6602();

	}

	/**
	 * This is the default constructor
	 */
	public Counter6602GUI() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setContentPane(getJContentPane());
		this.setSize(485, 180);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);

	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new java.awt.BorderLayout());
			jContentPane.add(getJPanel(), java.awt.BorderLayout.SOUTH);
			jContentPane.add(getJPanel1(), java.awt.BorderLayout.CENTER);
			jContentPane.add(getJPanel2(), java.awt.BorderLayout.NORTH);
		}
		return jContentPane;
	}

	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.add(getJButton2(), null);
			jPanel.add(getJButton3(), null);
			jPanel.add(getJButton4(), null);
			jPanel.add(getJButton5(), null);
			jPanel.add(getJButton(), null);
			jPanel.add(getJButton7(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes jButton2
	 * 
	 * @return JButton
	 */
	private JButton getJButton2() {
		if (jButton2 == null) {
			jButton2 = new JButton();
			jButton2.setName("cmdConfigure");
			jButton2.setText("Config");
			jButton2.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					CT.setDeviceNumber(1);
					CT.setTimingChannel(1);
					CT.setTotalChans(8);
					jButton3.setEnabled(true);
					jButton2.setEnabled(false);
				}
			});
		}
		return jButton2;
	}

	/**
	 * This method initializes jButton3
	 * 
	 * @return JButton
	 */
	private JButton getJButton3() {
		if (jButton3 == null) {
			jButton3 = new JButton();
			jButton3.setName("cmdInit");
			jButton3.setText("Init");
			jButton3.setEnabled(false);
			jButton3.setRolloverEnabled(true);
			jButton3.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String s = jTextField8.getText();
					double val = Double.valueOf(s).doubleValue();
					try {
						CT.countAsync(val);
					} catch (DeviceException de) {
						jTextField9.setText(de.getMessage());
					}
					jButton4.setEnabled(true);
				}
			});
		}
		return jButton3;
	}

	/**
	 * This method initializes jButton4
	 * 
	 * @return JButton
	 */
	private JButton getJButton4() {
		if (jButton4 == null) {
			jButton4 = new JButton();
			jButton4.setName("cmdStart");
			jButton4.setText("Start");
			jButton4.setEnabled(false);
			jButton4.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						CT.start(); // Start counter running
					} catch (DeviceException de) {
						jTextField9.setText(de.getMessage());
					}
					jButton5.setEnabled(true);
					jButton4.setEnabled(false);
					jButton3.setEnabled(false);
					jTextField9.setText("Counting");
					jCheckBox.setSelected(true);
					// Create new acquisition thread
					runner = uk.ac.gda.util.ThreadManager.getThread(Counter6602GUI.this, getClass().getName());
					running = true;
					runner.start();
				}
			});
		}
		return jButton4;
	}

	@Override
	public void run() {
		int busy = Detector.IDLE;
		while (running) {
			try {
				busy = CT.getStatus();
			} catch (DeviceException de) {
				jTextField9.setText(de.getMessage());
				running = false;
			}
			if (busy == Detector.BUSY) {
				jTextField9.setText(jTextField9.getText() + ".");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			} else {
				running = false; // End thread
			}
		}
		jButton5.setEnabled(false);
		jButton4.setEnabled(false);
		jButton3.setEnabled(true);
		jCheckBox.setSelected(false);
		jTextField9.setText(jTextField9.getText() + "finished");
	}

	/**
	 * This method initializes jButton5
	 * 
	 * @return JButton
	 */
	private JButton getJButton5() {
		if (jButton5 == null) {
			jButton5 = new JButton();
			jButton5.setText("Stop");
			jButton5.setName("cmdStop");
			jButton5.setEnabled(false);
			jButton5.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						CT.stop();
					} catch (DeviceException de) {
						jTextField9.setText(de.getMessage());
					}
					jButton3.setEnabled(true);
					jButton5.setEnabled(false);
					jButton.setEnabled(true);
				}
			});
		}
		return jButton5;
	}

	/**
	 * This method initializes jButton7
	 * 
	 * @return JButton
	 */
	private JButton getJButton7() {
		if (jButton7 == null) {
			jButton7 = new JButton();
			jButton7.setName("cmdQuit");
			jButton7.setText("Quit");
			jButton7.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.exit(0);
				}
			});
		}
		return jButton7;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return JButton
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setName("cmdRead");
			jButton.setText("Read");
			jButton.setEnabled(true);
			jButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						double[] data = CT.readChans();
						jTextField.setText(Integer.toString((int) data[0]));
						jTextField1.setText(Integer.toString((int) data[1]));
						jTextField2.setText(Integer.toString((int) data[2]));
						jTextField3.setText(Integer.toString((int) data[3]));
						jTextField4.setText(Integer.toString((int) data[4]));
						jTextField5.setText(Integer.toString((int) data[5]));
						jTextField6.setText(Integer.toString((int) data[6]));
						jTextField7.setText(Integer.toString((int) data[7]));
					} catch (DeviceException de) {
						jTextField9.setText(de.getMessage());
					}

				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jPanel1
	 * 
	 * @return JPanel
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.add(getJLabel(), null);
			jPanel1.add(getJTextField(), null);
			jPanel1.add(getJLabel1(), null);
			jPanel1.add(getJTextField1(), null);
			jPanel1.add(getJLabel2(), null);
			jPanel1.add(getJTextField2(), null);
			jPanel1.add(getJLabel3(), null);
			jPanel1.add(getJTextField3(), null);
			jPanel1.add(getJLabel4(), null);
			jPanel1.add(getJTextField4(), null);
			jPanel1.add(getJLabel5(), null);
			jPanel1.add(getJTextField5(), null);
			jPanel1.add(getJLabel6(), null);
			jPanel1.add(getJTextField6(), null);
			jPanel1.add(getJLabel7(), null);
			jPanel1.add(getJTextField7(), null);
			jPanel1.add(getJTextField9(), null);
			jPanel1.setPreferredSize(new java.awt.Dimension(933, 31));
		}
		return jPanel1;
	}

	/**
	 * This method initializes jPanel2
	 * 
	 * @return JPane2
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			jPanel2 = new JPanel();
			jPanel2.setLayout(new java.awt.FlowLayout());
			jPanel2.add(getJLabel8(), null);
			jPanel2.add(getJTextField8(), null);
			jPanel2.add(getJCheckBox(), null);

		}
		return jPanel2;
	}

	/**
	 * This method initializes jLabel
	 * 
	 * @return JLabel
	 */
	private JLabel getJLabel() {
		if (jLabel == null) {
			jLabel = new JLabel();
			jLabel.setText("Channel 1");
		}
		return jLabel;
	}

	/**
	 * This method initializes jTextField
	 * 
	 * @return JTextField
	 */
	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
			jTextField.setText(" ");
			jTextField.setMinimumSize(new java.awt.Dimension(4, 21));
			jTextField.setPreferredSize(new java.awt.Dimension(50, 21));
		}
		return jTextField;
	}

	/**
	 * This method initializes jLabel1
	 * 
	 * @return JLabel
	 */
	private JLabel getJLabel1() {
		if (jLabel1 == null) {
			jLabel1 = new JLabel();
			jLabel1.setText("Channel 2");
		}
		return jLabel1;
	}

	/**
	 * This method initializes jTextField1
	 * 
	 * @return JTextField
	 */
	private JTextField getJTextField1() {
		if (jTextField1 == null) {
			jTextField1 = new JTextField();
			jTextField1.setPreferredSize(new java.awt.Dimension(50, 21));
		}
		return jTextField1;
	}

	/**
	 * This method initializes jLabel2
	 * 
	 * @return JLabel
	 */
	private JLabel getJLabel2() {
		if (jLabel2 == null) {
			jLabel2 = new JLabel();
			jLabel2.setText("Channel 3");
		}
		return jLabel2;
	}

	/**
	 * This method initializes jTextField2
	 * 
	 * @return JTextField
	 */
	private JTextField getJTextField2() {
		if (jTextField2 == null) {
			jTextField2 = new JTextField();
			jTextField2.setMinimumSize(new java.awt.Dimension(5, 21));
			jTextField2.setPreferredSize(new java.awt.Dimension(50, 21));
		}
		return jTextField2;
	}

	/**
	 * This method initializes jLabel3
	 * 
	 * @return JLabel
	 */
	private JLabel getJLabel3() {
		if (jLabel3 == null) {
			jLabel3 = new JLabel();
			jLabel3.setText("Channel 4");
		}
		return jLabel3;
	}

	/**
	 * This method initializes jTextField3
	 * 
	 * @return JTextField
	 */
	private JTextField getJTextField3() {
		if (jTextField3 == null) {
			jTextField3 = new JTextField();
			jTextField3.setPreferredSize(new java.awt.Dimension(50, 21));
		}
		return jTextField3;
	}

	/**
	 * This method initializes jLabel4
	 * 
	 * @return JLabel
	 */
	private JLabel getJLabel4() {
		if (jLabel4 == null) {
			jLabel4 = new JLabel();
			jLabel4.setText("Channel 5");
		}
		return jLabel4;
	}

	/**
	 * This method initializes jTextField4
	 * 
	 * @return JTextField
	 */
	private JTextField getJTextField4() {
		if (jTextField4 == null) {
			jTextField4 = new JTextField();
			jTextField4.setPreferredSize(new java.awt.Dimension(50, 21));
		}
		return jTextField4;
	}

	/**
	 * This method initializes jLabel5
	 * 
	 * @return JLabel
	 */
	private JLabel getJLabel5() {
		if (jLabel5 == null) {
			jLabel5 = new JLabel();
			jLabel5.setText("Channel 6");
		}
		return jLabel5;
	}

	/**
	 * This method initializes jTextField5
	 * 
	 * @return JTextField
	 */
	private JTextField getJTextField5() {
		if (jTextField5 == null) {
			jTextField5 = new JTextField();
			jTextField5.setPreferredSize(new java.awt.Dimension(50, 21));
		}
		return jTextField5;
	}

	/**
	 * This method initializes jLabel6
	 * 
	 * @return JLabel
	 */
	private JLabel getJLabel6() {
		if (jLabel6 == null) {
			jLabel6 = new JLabel();
			jLabel6.setText("Channel 7");
		}
		return jLabel6;
	}

	/**
	 * This method initializes jTextField6
	 * 
	 * @return JTextField
	 */
	private JTextField getJTextField6() {
		if (jTextField6 == null) {
			jTextField6 = new JTextField();
			jTextField6.setPreferredSize(new java.awt.Dimension(50, 21));
		}
		return jTextField6;
	}

	/**
	 * This method initializes jLabel7
	 * 
	 * @return JLabel
	 */
	private JLabel getJLabel7() {
		if (jLabel7 == null) {
			jLabel7 = new JLabel();
			jLabel7.setText("Channel 8");
		}
		return jLabel7;
	}

	/**
	 * This method initializes jTextField7
	 * 
	 * @return JTextField
	 */
	private JTextField getJTextField7() {
		if (jTextField7 == null) {
			jTextField7 = new JTextField();
			jTextField7.setMinimumSize(new java.awt.Dimension(4, 21));
			jTextField7.setPreferredSize(new java.awt.Dimension(50, 21));
		}
		return jTextField7;
	}

	/**
	 * This method initializes jCheckBox
	 * 
	 * @return JCheckBox
	 */
	private JCheckBox getJCheckBox() {
		if (jCheckBox == null) {
			jCheckBox = new JCheckBox();
			jCheckBox.setText("Counting?");
			jCheckBox.setHorizontalAlignment(SwingConstants.RIGHT);
			jCheckBox.setHorizontalTextPosition(SwingConstants.LEADING);
			jCheckBox.setVerticalAlignment(SwingConstants.CENTER);
		}
		return jCheckBox;
	}

	/**
	 * This method initializes jLabel8
	 * 
	 * @return JLabel
	 */
	private JLabel getJLabel8() {
		if (jLabel8 == null) {
			jLabel8 = new JLabel();
			jLabel8.setText("Time (secs)");
		}
		return jLabel8;
	}

	/**
	 * This method initializes jTextField8
	 * 
	 * @return JTextField
	 */
	private JTextField getJTextField8() {
		if (jTextField8 == null) {
			jTextField8 = new JTextField();
			jTextField8.setPreferredSize(new java.awt.Dimension(55, 21));
			jTextField8.setText("1");
		}
		return jTextField8;
	}

	/**
	 * This method initializes jTextField9
	 * 
	 * @return JTextField
	 */
	private JTextField getJTextField9() {
		if (jTextField9 == null) {
			jTextField9 = new JTextField();
			jTextField9.setPreferredSize(new java.awt.Dimension(470, 21));
			jTextField9.setHorizontalAlignment(SwingConstants.LEFT);
		}
		return jTextField9;
	}
} // @jve:visual-info decl-index=0 visual-constraint="7,28"
