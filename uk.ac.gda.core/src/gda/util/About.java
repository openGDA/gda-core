/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

/*
 * @author Paul Smith <psmith@apache.org>
 */

package gda.util;

import gda.icons.GdaIcons;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * A simple About box telling people stuff about this project
 * 
 * @author Paul Smith <psmith@apache.org>
 */
public class About extends JDialog {

	private final JPanel northPane = new JPanel();

	private final JPanel southPane = new JPanel();

	private final JEditorPane editPane = new JEditorPane("text/html", "");

	private final JScrollPane scrollPane = new JScrollPane(editPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

	private final String url = About.class.getName().replace('.', '/') + ".html";

	private final Object guard = new Object();

	/**
	 * Constructor
	 * 
	 * @param parent
	 */
	public About(JFrame parent) {
		super(parent, "About GDA " + Version.getRelease(), true);
		setBackground(Color.GRAY);
		getContentPane().setLayout(new BorderLayout());

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		closeButton.setDefaultCapable(true);
		Icon image = new ImageIcon(GdaIcons.getLogo(128));
		String labelText = "<html><body><h3>Generic Data Acquisition (GDA) System -  "
				+ Version.getRelease()
				+ "</h3>"
				+ "<p>Brought to you by the GDA Developers at:<hr></p>"
				+ "<p style=\"color: blue; text-align: center; font-family: sans-serif\"> Data Acquisition and Scientific Computing Group (DASC) <br>"
				+ "Diamond Light Source Ltd (DLS)</font></p></body></html>";
		JLabel copyright = new JLabel("\u00A9 Science and Technology Facilities Council & Diamond Light Source Ltd  ");
		copyright.setBorder(BorderFactory.createEtchedBorder());
		JLabel logo = new JLabel(labelText, image, SwingConstants.CENTER);
		northPane.setLayout(new FlowLayout());
		northPane.add(logo);
		southPane.setLayout(new BorderLayout());
		southPane.add(copyright, BorderLayout.WEST);
		southPane.add(closeButton, BorderLayout.EAST);
		try {
			editPane.setPage(this.getClass().getClassLoader().getResource(url));
		} catch (Exception e) {
			throw new RuntimeException("Failed to find the About panel HTML", e);
		}
		getContentPane().add(northPane, BorderLayout.NORTH);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		getContentPane().add(southPane, BorderLayout.SOUTH);

		editPane.setEditable(false);
		editPane.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					if (Desktop.isDesktopSupported()) {
						Desktop desktop = Desktop.getDesktop();
						if (desktop.isSupported(Desktop.Action.BROWSE)) {
							try {
								desktop.browse(new URI(e.getURL().toString()));
							} catch (IOException ioe) {
								ioe.printStackTrace();
							} catch (URISyntaxException use) {
								use.printStackTrace();
							}
						}
					}
				}
			}
		});

		setSize(540, 400);
		// uk.ac.gda.util.ThreadManager.getThread(new Scroller()).start();
		scrollPane.getViewport().setViewPosition(new Point(0, 0));

		setLocationRelativeTo(parent);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		synchronized (guard) {
			guard.notifyAll();
		}
	}

	private void setLocationRelativeTo(JFrame parent) {

		int parentX = parent.getX();
		int parentY = parent.getY();
		int parentWidth = parent.getWidth();
		int parentHeight = parent.getHeight();

		int xLocation = parentX + parentWidth / 2;
		int yLocation = parentY + parentHeight / 2;

		// Place logo in centre of displaying screen
		Dimension d = getPreferredSize();
		int posx = xLocation - (d.width / 2);
		int posy = yLocation - (d.height / 2);

		setLocation(posx, posy);
	}
}
