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

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import gda.device.ODCCD;
import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;

/**
 * <p>
 * <b>Title: </b>Standalone GUI control/monitor application for the Oxford Diffraction CCDs.
 * </p>
 * <p>
 * <b>Description: </b>This standalone GUI can be used to control/monitor the Oxford Diffraction CCDs independantly of
 * the GDA if needed. A local instance of ODCCDController is created in this GUI.
 * </p>
 */
@Deprecated(since="GDA 9.31", forRemoval=true)
public class ODCCDGui extends JFrame {
	private static final DeprecationLogger logger = DeprecationLogger.getLogger(ODCCDGui.class);

	/** Private reference to the control panel. */
	private ODCCDControlPanel mControlPanel = null;

	/** Private reference to the CCD control object. */
	private ODCCD mCCD = null;

	/**
	 * Private constructor.
	 */
	private ODCCDGui() {
		super("Oxford Diffraction CCD");
		logger.deprecatedClass("9.33", "nothing, Crysalis/ODCCD is no longer supported in GDA");

		// Make a local reference to the CCD object
		mCCD = new ODCCDController();

		mControlPanel = new ODCCDControlPanel(mCCD);

		Container cp = this.getContentPane();

		this.setSize(900, 600);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new DisconnectOnClose());

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Control", mControlPanel);

		// Add tabbed panel to content frame
		cp.add(tabbedPane);
	}

	/**
	 * Static method to create the GUI in a thread safe manner using <code>EventQueue.invokeLater()</code>
	 *
	 * @return ODCCDControlPanel
	 */
	static ODCCDGui createGui() {

		final ODCCDGui gui = new ODCCDGui();

		// Pack everything and display it.
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				// gui.pack();
				gui.setVisible(true);
			}
		});
		return gui;
	}

	/**
	 * When closing main frame, show dialog box to conform CCD disconnection.
	 */
	private class DisconnectOnClose extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent e) {
			if (!mControlPanel.isConnected()) {
				e.getWindow().dispose();
				System.exit(0);
			} else {
				int response = JOptionPane.showConfirmDialog(null, "Confirm CCD disconnection and exit.", " ",
						JOptionPane.YES_NO_OPTION);
				if (response == 0) {
					logger.info("Disconnecting from " + mControlPanel.getHostname());
					mControlPanel.disconnectCCD();
					e.getWindow().dispose();
					System.exit(0);
				}
			}
		}
	}

}
