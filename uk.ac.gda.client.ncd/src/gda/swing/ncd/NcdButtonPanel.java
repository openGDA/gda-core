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

import gda.data.metadata.GDAMetadataProvider;
import gda.device.DeviceException;
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;
import gda.rcp.ncd.NcdController;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class for all NCD panels except InstrumentPanels Class members have the start, clear, stop, output and restart
 * buttons at their bottom and their respective stuff above
 */
public class NcdButtonPanel extends JPanel {
	
	private static final Logger logger = LoggerFactory.getLogger(NcdButtonPanel.class);
	
	private JButton startButton;
	private JButton clearButton;
	private JButton haltButton;
	private JButton outputButton;
	private ShutterPanel shutterPanel;

	private static String shutterName;

	private static boolean saved = true;

	private SaveDataDialog saveDataDialog;

	private NcdController ncdController = NcdController.getInstance();
	private ActionHelper actionHelper = ActionHelper.getInstance();

	public NcdButtonPanel() {
		setLayout(new BorderLayout());

		add(createControlPanel());

		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory
				.createEmptyBorder(0, 0, 0, 0)));

		saveDataDialog = new SaveDataDialog();
		actionHelper.register(this);
	}

	public void configure() {
		if (shutterName != null) {
			shutterPanel.setShutterName(shutterName);
			shutterPanel.configure();
		}
	}

	/**
	 * @return the control panel instance
	 */
	public JPanel createControlPanel() {
		JPanel panel = new JPanel(new FlowLayout());

		panel.add(Box.createHorizontalGlue());

		startButton = new JButton("Start");
		startButton.setToolTipText("Start Experiment");
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				if (!saved) {
					saveDataDialog.showDialog();
					if (saveDataDialog.isSaveRequired()) {
						outputButton.doClick();
					}
					if (saveDataDialog.isClearRequired()) {
						try {
							ncdController.getNcdDetectorSystem().clear();
						} catch (DeviceException de) {
							logger.error("DeviceException " + de);
							JOptionPane.showMessageDialog(null, de.getMessage(), "DeviceException",
									JOptionPane.ERROR_MESSAGE);
						}
					}

				}
				saved = false;

				try {
					ncdController.getNcdDetectorSystem().start();
				} catch (DeviceException de) {
					logger.error("DeviceException " + de);
					JOptionPane.showMessageDialog(null, de.getMessage(), "DeviceException", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		clearButton = new JButton("Clear");
		clearButton.setToolTipText("Clear Data");
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				if (!saved) {
					saveDataDialog.showDialog();
					if (saveDataDialog.isSaveRequired()) {
						outputButton.doClick();
					}
				}
				try {
					ncdController.getNcdDetectorSystem().clear();
				} catch (DeviceException de) {
					logger.error("DeviceException " + de);
					JOptionPane.showMessageDialog(null, de.getMessage(), "DeviceException", JOptionPane.ERROR_MESSAGE);
				}
				saved = true;
			}
		});

		haltButton = new JButton("Halt");
		haltButton.setToolTipText("Halt Experiment");
		haltButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				try {
					ncdController.getNcdDetectorSystem().stop();
				} catch (DeviceException de) {
					logger.error("DeviceException " + de);
					JOptionPane.showMessageDialog(null, de.getMessage(), "DeviceException", JOptionPane.ERROR_MESSAGE);

				}
			}
		});

		outputButton = new JButton("Output");
		outputButton.setToolTipText("Output Experimental Data");
		outputButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				actionHelper.outputStart();

				String title = JOptionPane.showInputDialog(getTopLevelAncestor(), "Enter Title", "");
				// if null user pressed cancel
				if (title != null) {
					// get rid of the worst characters, there may be more...
					title = title.replace("'", " ");
					title = title.replace("\\", " ");

					try {
						GDAMetadataProvider.getInstance(true).setMetadataValue("title", title.trim());
						JythonServerFacade.getInstance().evaluateCommand("gda.scan.StaticScanNoCollection(["+ncdController.getNcdDetectorSystem().getName()+"]).runScan()");
						saved = true;
					} catch (Exception de) {
						logger.error("Exception during readout ", de);
						JOptionPane.showMessageDialog(null, de.getMessage(), "Exception",
								JOptionPane.ERROR_MESSAGE);
					}
				}

				actionHelper.outputComplete();
			}
		});

		panel.add(startButton);
		panel.add(haltButton);
		panel.add(clearButton);
		panel.add(outputButton);

		panel.add(Box.createHorizontalGlue());

		shutterPanel = new ShutterPanel();
		panel.add(shutterPanel);

		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory
				.createEmptyBorder(5, 3, 5, 3)));

		return panel;
	}

	/**
	 * Disable control panel
	 */
	public void disableCollection() {
		startButton.setEnabled(false);
		clearButton.setEnabled(false);
		outputButton.setEnabled(false);
		haltButton.setEnabled(false);
	}

	/**
	 * Change control panel appropriate for data collection complete
	 */
	public void collectionComplete() {
		startButton.setEnabled(true);
		clearButton.setEnabled(true);
		outputButton.setEnabled(true);
		haltButton.setEnabled(false);
	}

	/**
	 * Change control panel appropriate for data collection active
	 */
	public void collectionStart() {
		startButton.setEnabled(false);
		clearButton.setEnabled(false);
		outputButton.setEnabled(false);
		haltButton.setEnabled(true);
	}

	/**
	 * Change control panel appropriate for data output complete
	 */
	public void outputComplete() {
		startButton.setEnabled(true);
		clearButton.setEnabled(true);
		outputButton.setEnabled(true);
		haltButton.setEnabled(false);
	}

	/**
	 * Change control panel appropriate for data output
	 */
	public void outputStart() {
		startButton.setEnabled(false);
		clearButton.setEnabled(false);
		outputButton.setEnabled(false);
		haltButton.setEnabled(false);
	}

	/**
	 * Weirdly named and implemented observer pattern
	 * 
	 * @param observer
	 */
	public void addObservability(@SuppressWarnings("unused") IObserver observer) {
	}

	/**
	 * get the name of the shutter configured
	 * 
	 * @return the name of the shutter configured
	 */
	public String getShutterName() {
		return shutterName;
	}

	/**
	 * set the name of the shutter
	 * 
	 * @param newShutterName
	 */
	public void setShutterName(String newShutterName) {
		shutterName = newShutterName;
	}
}