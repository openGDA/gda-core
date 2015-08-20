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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import gda.device.DeviceException;

/**
 * To change the template for this generated type comment go to Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code
 * and Comments
 */
public class SRControlGUI extends JFrame {
	private static SRControl SRC;

	private JPanel jContentPane = null;

	private JPanel jPanel = null;

	private JButton jButton = null;

	private JButton jButton1 = null;

	private JButton jButton2 = null;

	private JPanel jPanel1 = null;

	private JLabel jLabel = null;

	private JTextField jTextField = null;

	private JLabel jLabel1 = null;

	private JTextField jTextField1 = null;

	private JLabel jLabel2 = null;

	private JTextField jTextField2 = null;

	private JPanel jPanel2 = null;

	private JLabel jLabel3 = null;

	private JLabel jLabel4 = null;

	private JTextField jTextField3 = null;

	private JTextField jTextField4 = null;

	private JButton jButton3 = null;

	private JList<?> jList = null;

	private JComboBox<String> jComboBox = null;

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
	 * This method initialises this
	 */
	private void initialize() {
		this.setSize(300, 150);
		this.setContentPane(getJContentPane());
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	/**
	 * This method initialises jContentPane @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new java.awt.BorderLayout());
			jContentPane.add(getJPanel1(), java.awt.BorderLayout.NORTH);
			jContentPane.add(getJPanel2(), java.awt.BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initialises jPanel @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.add(getJButton3(), null);
			jPanel.add(getJButton(), null);
			jPanel.add(getJButton1(), null);
			jPanel.add(getJButton2(), null);
		}
		return jPanel;
	}

	/**
	 * This method initialises jButton @return javax.swing.JButton
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
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
	 * This method initialises jButton1 @return javax.swing.JButton
	 */
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
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
	 * This method initialises jButton2 @return javax.swing.JButton
	 */
	private JButton getJButton2() {
		if (jButton2 == null) {
			jButton2 = new JButton();
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
	 * This method initialises jPanel1 @return javax.swing.JPanel
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
		}
		return jPanel1;
	}

	/**
	 * This method initialises jLabel @return javax.swing.JLabel
	 */
	private JLabel getJLabel() {
		if (jLabel == null) {
			jLabel = new JLabel();
			jLabel.setText("Parameter   ");
		}
		return jLabel;
	}

	/**
	 * This method initialises jTextField @return javax.swing.JTextField
	 */
	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
			jTextField.setPreferredSize(new java.awt.Dimension(30, 20));
			jTextField.setHorizontalAlignment(SwingConstants.CENTER);
			jTextField.setText("TST");
		}
		return jTextField;
	}

	/**
	 * This method initialises jLabel1 @return javax.swing.JLabel
	 */
	private JLabel getJLabel1() {
		if (jLabel1 == null) {
			jLabel1 = new JLabel();
			jLabel1.setText(".");
		}
		return jLabel1;
	}

	/**
	 * This method initialises jTextField1 @return javax.swing.JTextField
	 */
	private JTextField getJTextField1() {
		if (jTextField1 == null) {
			jTextField1 = new JTextField();
			jTextField1.setHorizontalAlignment(SwingConstants.CENTER);
			jTextField1.setPreferredSize(new java.awt.Dimension(50, 20));
			jTextField1.setText("PARM");
		}
		return jTextField1;
	}

	/**
	 * This method initialises jLabel2 @return javax.swing.JLabel
	 */
	private JLabel getJLabel2() {
		if (jLabel2 == null) {
			jLabel2 = new JLabel();
			jLabel2.setText(".");
		}
		return jLabel2;
	}

	/**
	 * This method initialises jTextField2 @return javax.swing.JTextField
	 */
	private JTextField getJTextField2() {
		if (jTextField2 == null) {
			jTextField2 = new JTextField();
			jTextField2.setPreferredSize(new java.awt.Dimension(25, 20));
			jTextField2.setHorizontalAlignment(SwingConstants.CENTER);
			jTextField2.setText("01");
		}
		return jTextField2;
	}

	/**
	 * This method initialises jPanel2 @return javax.swing.JPanel
	 */
	private JPanel getJPanel2() {
		if (jPanel2 == null) {
			jPanel2 = new JPanel();
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
	 * This method initialises jLabel3 @return javax.swing.JLabel
	 */
	private JLabel getJLabel3() {
		if (jLabel3 == null) {
			jLabel3 = new JLabel();
			jLabel3.setText("Property");
		}
		return jLabel3;
	}

	/**
	 * This method initialises jLabel4 @return javax.swing.JLabel
	 */
	private JLabel getJLabel4() {
		if (jLabel4 == null) {
			jLabel4 = new JLabel();
			jLabel4.setText("     Value");
		}
		return jLabel4;
	}

	/**
	 * This method initialises jTextField3 @return javax.swing.JTextField
	 */
	private JTextField getJTextField3() {
		if (jTextField3 == null) {
			jTextField3 = new JTextField();
			jTextField3.setPreferredSize(new java.awt.Dimension(40, 20));
			jTextField3.setHorizontalAlignment(SwingConstants.CENTER);
			jTextField3.setText("1");
		}
		return jTextField3;
	}

	/**
	 * This method initialises jTextField4 @return javax.swing.JTextField
	 */
	private JTextField getJTextField4() {
		if (jTextField4 == null) {
			jTextField4 = new JTextField();
			jTextField4.setPreferredSize(new java.awt.Dimension(280, 20));
		}
		return jTextField4;
	}

	/**
	 * This method initialises jButton3 @return javax.swing.JButton
	 */
	private JButton getJButton3() {
		if (jButton3 == null) {
			jButton3 = new JButton();
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
	 * This method initialises jList @return javax.swing.JList
	 */
	private JList<?> getJList() {
		if (jList == null) {
			jList = new JList<>();
		}
		return jList;
	}

	/**
	 * This method initialises jComboBox @return javax.swing.JComboBox
	 */
	private JComboBox<String> getJComboBox() {
		if (jComboBox == null) {
			jComboBox = new JComboBox<>();
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
