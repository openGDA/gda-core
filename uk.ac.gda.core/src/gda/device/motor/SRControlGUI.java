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

import gda.device.DeviceException;

import javax.swing.JFrame;
import javax.swing.SwingConstants;

/**
 * To change the template for this generated type comment go to Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code
 * and Comments
 */
public class SRControlGUI extends JFrame {
	private static SRControl SRC;

	private javax.swing.JPanel jContentPane = null;

	private javax.swing.JPanel jPanel = null;

	private javax.swing.JButton jButton = null;

	private javax.swing.JButton jButton1 = null;

	private javax.swing.JButton jButton2 = null;

	private javax.swing.JPanel jPanel1 = null;

	private javax.swing.JLabel jLabel = null;

	private javax.swing.JTextField jTextField = null;

	private javax.swing.JLabel jLabel1 = null;

	private javax.swing.JTextField jTextField1 = null;

	private javax.swing.JLabel jLabel2 = null;

	private javax.swing.JTextField jTextField2 = null;

	private javax.swing.JPanel jPanel2 = null;

	private javax.swing.JLabel jLabel3 = null;

	private javax.swing.JLabel jLabel4 = null;

	private javax.swing.JTextField jTextField3 = null;

	private javax.swing.JTextField jTextField4 = null;

	private javax.swing.JButton jButton3 = null;

	private javax.swing.JList jList = null;

	private javax.swing.JComboBox jComboBox = null;

	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		new SRControlGUI();
		SRC = new SRControl();
	}

	/**
	 * This is the default constructor
	 */
	public SRControlGUI() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 */
	private void initialize() {
		this.setSize(300, 150);
		this.setContentPane(getJContentPane());
		this.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new java.awt.BorderLayout());
			jContentPane.add(getJPanel1(), java.awt.BorderLayout.NORTH);
			jContentPane.add(getJPanel2(), java.awt.BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new javax.swing.JPanel();
			jPanel.add(getJButton3(), null);
			jPanel.add(getJButton(), null);
			jPanel.add(getJButton1(), null);
			jPanel.add(getJButton2(), null);
		}
		return jPanel;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private javax.swing.JButton getJButton() {
		if (jButton == null) {
			jButton = new javax.swing.JButton();
			jButton.setText("Read");
			jButton.setEnabled(true);
			jButton.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String parameter = jTextField.getText() + "." + jTextField1.getText();
					if (!jTextField2.getText().equals(""))
						parameter = parameter + "." + jTextField2.getText();
					String property = "";
					int i = jComboBox.getSelectedIndex();
					if (i == 0)
						property = SRControlInterface.GET_VALUE;
					else if (i == 1)
						property = SRControlInterface.GET_STATUS;
					else if (i == 2)
						property = SRControlInterface.GET_MINVALUE;
					else if (i == 3)
						property = SRControlInterface.GET_MAXVALUE;
					else if (i == 4)
						property = SRControlInterface.GET_INTERLOCKS;
					double[] val = new double[1];
					try {
						SRC.getValue(parameter, property, val);
						jTextField4.setText(parameter + ":" + property + " = " + val[0]);
						if (i == 1) // get status string
						{
							StringBuffer status = new StringBuffer("");

							try {
								SRC.getStatusString(parameter, val[0], status, 16);
								jTextField4.setText(jTextField4.getText() + " ,Status string = " + status);
							} catch (DeviceException de) {
								jTextField4.setText(de.getMessage());
							}
						} else if (i != 4) // Not interlocks or status
						{
							StringBuffer units = new StringBuffer("");

							try {
								SRC.getUnitsString(parameter, units, 16);
								jTextField4.setText(jTextField4.getText() + " " + units);
							} catch (DeviceException de) {
								jTextField4.setText(de.getMessage());
							}

						}
					} catch (DeviceException de) {
						jTextField4.setText(de.getMessage());
					}
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jButton1
	 * 
	 * @return javax.swing.JButton
	 */
	private javax.swing.JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new javax.swing.JButton();
			jButton1.setText("Write");
			jButton1.setEnabled(true);
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String parameter = jTextField.getText() + "." + jTextField1.getText();
					if (!jTextField2.getText().equals(""))
						parameter = parameter + "." + jTextField2.getText();

					int i = jComboBox.getSelectedIndex();
					String property = "";
					if (i == 0)
						property = SRControlInterface.SET_VALUE;
					else if (i == 1)
						property = SRControlInterface.SET_STATUS;
					else if (i == 2)
						property = SRControlInterface.SET_MINVALUE;
					else if (i == 3)
						property = SRControlInterface.SET_MAXVALUE;
					String value = jTextField3.getText();
					double[] val = new double[1];
					double d = Double.valueOf(value).doubleValue();
					val[0] = d;
					try {
						SRC.setValue(parameter, property, val);
						jTextField4.setText(parameter + ":" + property + " = " + val[0]);
					} catch (DeviceException de) {
						jTextField4.setText(de.getMessage());
					}

				}
			});
		}
		return jButton1;
	}

	/**
	 * This method initializes jButton2
	 * 
	 * @return javax.swing.JButton
	 */
	private javax.swing.JButton getJButton2() {
		if (jButton2 == null) {
			jButton2 = new javax.swing.JButton();
			jButton2.setText("Quit");
			jButton2.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.exit(0);
				}
			});
		}
		return jButton2;
	}

	/**
	 * This method initializes jPanel1
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new javax.swing.JPanel();
			jPanel1.add(getJLabel(), null);
			jPanel1.add(getJTextField(), null);
			jPanel1.add(getJLabel1(), null);
			jPanel1.add(getJTextField1(), null);
			jPanel1.add(getJLabel2(), null);
			jPanel1.add(getJTextField2(), null);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jLabel
	 * 
	 * @return javax.swing.JLabel
	 */
	private javax.swing.JLabel getJLabel() {
		if (jLabel == null) {
			jLabel = new javax.swing.JLabel();
			jLabel.setText("Parameter   ");
		}
		return jLabel;
	}

	/**
	 * This method initializes jTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private javax.swing.JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new javax.swing.JTextField();
			jTextField.setPreferredSize(new java.awt.Dimension(30, 20));
			jTextField.setHorizontalAlignment(SwingConstants.CENTER);
			jTextField.setText("TST");
		}
		return jTextField;
	}

	/**
	 * This method initializes jLabel1
	 * 
	 * @return javax.swing.JLabel
	 */
	private javax.swing.JLabel getJLabel1() {
		if (jLabel1 == null) {
			jLabel1 = new javax.swing.JLabel();
			jLabel1.setText(".");
		}
		return jLabel1;
	}

	/**
	 * This method initializes jTextField1
	 * 
	 * @return javax.swing.JTextField
	 */
	private javax.swing.JTextField getJTextField1() {
		if (jTextField1 == null) {
			jTextField1 = new javax.swing.JTextField();
			jTextField1.setHorizontalAlignment(SwingConstants.CENTER);
			jTextField1.setPreferredSize(new java.awt.Dimension(50, 20));
			jTextField1.setText("PARM");
		}
		return jTextField1;
	}

	/**
	 * This method initializes jLabel2
	 * 
	 * @return javax.swing.JLabel
	 */
	private javax.swing.JLabel getJLabel2() {
		if (jLabel2 == null) {
			jLabel2 = new javax.swing.JLabel();
			jLabel2.setText(".");
		}
		return jLabel2;
	}

	/**
	 * This method initializes jTextField2
	 * 
	 * @return javax.swing.JTextField
	 */
	private javax.swing.JTextField getJTextField2() {
		if (jTextField2 == null) {
			jTextField2 = new javax.swing.JTextField();
			jTextField2.setPreferredSize(new java.awt.Dimension(25, 20));
			jTextField2.setHorizontalAlignment(SwingConstants.CENTER);
			jTextField2.setText("01");
		}
		return jTextField2;
	}

	/**
	 * This method initializes jPanel2
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJPanel2() {
		if (jPanel2 == null) {
			jPanel2 = new javax.swing.JPanel();
			jPanel2.add(getJLabel3(), null);
			jPanel2.add(getJComboBox(), null);
			jPanel2.add(getJList(), null);
			jPanel2.add(getJLabel4(), null);
			jPanel2.add(getJTextField3(), null);
			jPanel2.add(getJTextField4(), null);
			jPanel2.add(getJPanel(), null);
			jPanel2.add(getJPanel(), null);
		}
		return jPanel2;
	}

	/**
	 * This method initializes jLabel3
	 * 
	 * @return javax.swing.JLabel
	 */
	private javax.swing.JLabel getJLabel3() {
		if (jLabel3 == null) {
			jLabel3 = new javax.swing.JLabel();
			jLabel3.setText("Property");
		}
		return jLabel3;
	}

	/**
	 * This method initializes jLabel4
	 * 
	 * @return javax.swing.JLabel
	 */
	private javax.swing.JLabel getJLabel4() {
		if (jLabel4 == null) {
			jLabel4 = new javax.swing.JLabel();
			jLabel4.setText("     Value");
		}
		return jLabel4;
	}

	/**
	 * This method initializes jTextField3
	 * 
	 * @return javax.swing.JTextField
	 */
	private javax.swing.JTextField getJTextField3() {
		if (jTextField3 == null) {
			jTextField3 = new javax.swing.JTextField();
			jTextField3.setPreferredSize(new java.awt.Dimension(40, 20));
			jTextField3.setHorizontalAlignment(SwingConstants.CENTER);
			jTextField3.setText("1");
		}
		return jTextField3;
	}

	/**
	 * This method initializes jTextField4
	 * 
	 * @return javax.swing.JTextField
	 */
	private javax.swing.JTextField getJTextField4() {
		if (jTextField4 == null) {
			jTextField4 = new javax.swing.JTextField();
			jTextField4.setPreferredSize(new java.awt.Dimension(280, 20));
		}
		return jTextField4;
	}

	/**
	 * This method initializes jButton3
	 * 
	 * @return javax.swing.JButton
	 */
	private javax.swing.JButton getJButton3() {
		if (jButton3 == null) {
			jButton3 = new javax.swing.JButton();
			jButton3.setText("Init");
			jButton3.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					StringBuffer buff = new StringBuffer("");
					try {

						// byte j = SRC.getDecimalPlaces("TST.PARM.01");
						// jTextField4.setText("" + j);
						SRC.initialise();
						jTextField4.setText("" + buff);

					} catch (DeviceException de) {
						jTextField4.setText(de.getMessage());
					}
				}
			});
		}
		return jButton3;
	}

	/**
	 * This method initializes jList
	 * 
	 * @return javax.swing.JList
	 */
	private javax.swing.JList getJList() {
		if (jList == null) {
			jList = new javax.swing.JList();
		}
		return jList;
	}

	/**
	 * This method initializes jComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private javax.swing.JComboBox getJComboBox() {
		if (jComboBox == null) {
			jComboBox = new javax.swing.JComboBox();
			jComboBox.setComponentOrientation(java.awt.ComponentOrientation.LEFT_TO_RIGHT);
			jComboBox.setMinimumSize(new java.awt.Dimension(100, 25));
			jComboBox.setPreferredSize(new java.awt.Dimension(70, 20));
			jComboBox.addItem("Value");
			jComboBox.addItem("Status");
			jComboBox.addItem("Min");
			jComboBox.addItem("Max");
			jComboBox.addItem("Intlks");
		}
		return jComboBox;
	}
} // @jve:visual-info decl-index=0 visual-constraint="10,10"
