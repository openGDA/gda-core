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

package gda.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * ScreenIndexTest Class
 */
public class ScreenIndexTest {
	JFrame f;

	/**
	 * @param gs
	 */
	public void displayScreenIndex(GraphicsDevice[] gs) {
		for (int j = 0; j < gs.length; j++) {
			GraphicsConfiguration gc = gs[j].getDefaultConfiguration();
			f = new JFrame("Screen Index Display", gc);
			JPanel pane = new JPanel(new BorderLayout());
			JLabel displayNo = new JLabel("" + j + "");
			displayNo.setSize(gc.getBounds().width, gc.getBounds().height);
			displayNo.setVerticalAlignment(SwingConstants.CENTER);
			displayNo.setHorizontalAlignment(SwingConstants.CENTER);
			displayNo.setForeground(new Color(43 + j * 20, 105 + j * 20, 3 + j * 20));
			displayNo.setBackground(Color.BLACK);
			Font aaaFont = displayNo.getFont();
			Font font3 = aaaFont.deriveFont(500.0f);
			displayNo.setFont(font3);
			JButton exit = new JButton("exit");
			exit.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					System.exit(0);
				}
			});

			pane.add(displayNo, BorderLayout.CENTER);
			pane.add(exit, BorderLayout.SOUTH);
			f.getContentPane().setLayout(new BorderLayout());
			f.getContentPane().add(pane, BorderLayout.CENTER);
			begin(gs[j], f);
		}

	}

	/**
	 * @param device
	 * @param f
	 */
	public void begin(GraphicsDevice device, JFrame f) {
		boolean isFullScreen = device.isFullScreenSupported();
		// f.setUndecorated(false);
		f.setUndecorated(isFullScreen);
		f.setResizable(!isFullScreen);
		if (isFullScreen) {
			// Full-screen mode
			device.setFullScreenWindow(f);
			f.validate();
		} else {
			// Windowed mode
			f.pack();
			f.setVisible(true);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String args[]) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();

		ScreenIndexTest st = new ScreenIndexTest();
		st.displayScreenIndex(gs);
	}
}
