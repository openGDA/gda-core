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

package gda.swing.ncd;

import gda.jython.JythonServerFacade;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WatchPanel Class
 */
public class WatchPanel extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(WatchPanel.class);

	private JTable tblWatches = new JTable();

	private DefaultTableModel tblWatchesModel;

	private JPanel pnlSetFrequency = new JPanel();

	private JLabel lblUpdateFrequency = new JLabel();

	private JComboBox cmbFreqChoice = new JComboBox();

	private JButton btnAddWatch = new JButton();

	private JButton btnRemoveWatch = new JButton();

	private JButton btnHideWatch = new JButton();

	private boolean autohide = true;

	private JythonServerFacade commandserver = null;

	private boolean configured = false;

	/**
	 * This creates a Panel that
	 */

	public WatchPanel() {
		tblWatchesModel = new DefaultTableModel() {

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}

		};
		setBorder(new TitledBorder(BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151)), "Watches"));
		setLayout(new GridBagLayout());
	}

	/**
	 * 
	 */
	public void configure() {
		if (configured) {
			return;
		}
		commandserver = JythonServerFacade.getInstance();
		lblUpdateFrequency.setDoubleBuffered(false);
		lblUpdateFrequency.setMaximumSize(new Dimension(100, 15));
		lblUpdateFrequency.setMinimumSize(new Dimension(20, 15));
		lblUpdateFrequency.setPreferredSize(new Dimension(100, 15));
		lblUpdateFrequency.setText("Update frequency");

		/*
		 * Adds a watch to the watch list and displays the watch panel if it is hidden @param e ActionEvent
		 */
		btnAddWatch.setText("Add watch...");
		btnAddWatch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				// open a dialog box to add a row to tblWatches
				String inputValue = JOptionPane.showInputDialog("Please input a value");
				if (inputValue != null && inputValue.compareTo("") != 0) {
					addWatch(inputValue);
				}
			}
		});

		/*
		 * Removes a watch from the list of watches @param e ActionEvent
		 */
		btnRemoveWatch.setText("Remove watch");
		btnRemoveWatch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				int[] rowIndices = tblWatches.getSelectedRows();
				for (int element : rowIndices) {
					tblWatchesModel.removeRow(element);
				}
			}
		});

		btnHideWatch.setText("Hide");
		btnHideWatch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				setVisible(false);
			}
		});

		tblWatches.setModel(tblWatchesModel);
		tblWatchesModel.addColumn("name");
		tblWatchesModel.addColumn("value");
		 tblWatches.getColumnModel().getColumn(0).setHeaderValue("name");
		 tblWatches.getColumnModel().getColumn(1).setHeaderValue("value");
		 tblWatches.getTableHeader().resizeAndRepaint();

		cmbFreqChoice.addItem(new Double(10));
		cmbFreqChoice.addItem(new Double(5));
		cmbFreqChoice.addItem(new Double(2));
		cmbFreqChoice.addItem(new Double(1));
		cmbFreqChoice.addItem(new Double(0.5));
		cmbFreqChoice.setSelectedIndex(cmbFreqChoice.getItemCount() / 2);

		pnlSetFrequency.add(lblUpdateFrequency, null);
		pnlSetFrequency.add(cmbFreqChoice, null);
		pnlSetFrequency.add(btnAddWatch, null);
		pnlSetFrequency.add(btnRemoveWatch, null);
		if (autohide) {
			setVisible(false);
			pnlSetFrequency.add(btnHideWatch, null);

		} else {
			setVisible(true);
		}

		add(tblWatches, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		add(pnlSetFrequency, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		// start a thread which will constantly updates this panel
		Thread updateWatches = uk.ac.gda.util.ThreadManager.getThread(new UpdateWatches(this));
		updateWatches.setPriority(Thread.MIN_PRIORITY);
		updateWatches.start();
		configured = true;
	}

	/**
	 * Actually sets two things: If set to true this panel will hide when there is no more device watched and there will
	 * be a button to hide this panel as well. Defaults to true as for the JythonTerminal which has its own means to
	 * unhide this panel. Other users might want to set it to false prior to the configure() call. If done after that
	 * you will have the 'Hide' button already.
	 * 
	 * @param value
	 */
	public void setAutoHide(boolean value) {
		this.autohide = value;
	}

	/**
	 * Check if autohide is enabled
	 * 
	 * @return autohide
	 */
	public boolean isAutoHide() {
		return autohide;
	}

	/**
	 * Removes the Device identified by watch from the WatchList
	 * 
	 * @param watch
	 *            String
	 */
	public void removeWatch(String watch) {
		// check if watch is already listed
		for (int i = 0; i < tblWatchesModel.getRowCount(); ++i) {
			if (tblWatchesModel.getValueAt(i, 0).toString().compareTo(watch) == 0) {
				tblWatchesModel.removeRow(i);
				if (autohide && tblWatchesModel.getRowCount() == 0) {
					setVisible(false);
				}
				return;
			}
		}
	}

	/**
	 * Adds an object to the list of objects whose positions are displayed in a subpanel on the JythonTerminal
	 * interface. If the subpanel is not visible, then this method also makes it visible.
	 * 
	 * @param watch
	 *            String
	 */
	public void addWatch(String watch) {
		// check if watch is already listed
		for (int i = 0; i < tblWatchesModel.getRowCount(); ++i) {
			if (tblWatchesModel.getValueAt(i, 0).toString().compareTo(watch) == 0) {
				setVisible(true);
				return;
			}
		}

		// then check if watch is viable
		Object objectFromJythonNamespace = commandserver.evaluateCommand(watch + ".getPosition()");
		if (objectFromJythonNamespace != null) {
			// then make sure the watch panel is visible
			setVisible(true);
			// then add the watch
			tblWatchesModel.addRow(new Object[] { watch, null });
			this.updateUI();
		} else {
			JOptionPane.showMessageDialog(null, "Cannot get value for object " + watch, "Cannot access object",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private class UpdateWatches implements Runnable {
		JPanel master;
		JLabel label = new JLabel();

		/**
		 * @param master
		 */
		public UpdateWatches(JPanel master) {
			this.master = master;
		}

		@Override
		public void run() {
			while (true) {

				try {
				if (tblWatchesModel.getRowCount() > 0 && master.isVisible()) {
					// loop through all the items in the first column (0)
					// and update the second column
					for (int i = 0; i < tblWatchesModel.getRowCount(); ++i) {

						String s = tblWatchesModel.getValueAt(i, 0).toString();
						String value = commandserver
								.evaluateCommand("gda.device.scannable.ScannableUtils.getHTMLFormattedCurrentPosition("
										+ s + ")");
						tblWatchesModel.setValueAt(value, i, 1);
						// it would be nicer to use the genuine table widgets, but that does not work
						label.setText(value);
						int height = label.getPreferredSize().height;
						if (height > 1 ) tblWatches.setRowHeight(i, height);
					}
				}
				} catch (Exception e) {
					// this exists to keep the update thread running even if there is an intermediate 
					// error talking to the command server
					logger.warn("Caught exception while trying to update: ", e);
				}
				try { // pause
					Thread.sleep(new Double(((Double) cmbFreqChoice.getSelectedItem()).doubleValue() * 1000.0)
							.longValue());
				} catch (InterruptedException ex) {
					// we don't care
				}
			}
		}
	}
}