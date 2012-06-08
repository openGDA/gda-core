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
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ListProperties Class
 */
public class ListProperties {
	private static final Logger logger = LoggerFactory.getLogger(ListProperties.class);

	private static JFrame frame = new JFrame();

	private static JTabbedPane pane = new JTabbedPane();

	private static JPanel panel = new JPanel(new BorderLayout());

	private static void initialiseGUI() {
		frame.setTitle("GDA Java Properties");
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent ev) {
				System.exit(0);
			}
		});
		frame.getContentPane().add(panel);
		panel.add(pane);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Class<?> c;
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		Class<?>[] interfaces;
		Enumeration<JarEntry> enumeration;
		String name;

		if (args.length < 1) {
			logger.debug("Usage: ListProperties JarFile");
			System.exit(1);
		}

		try {
			initialiseGUI();

			enumeration = (new JarFile(args[0])).entries();
			while (enumeration.hasMoreElements()) {
				name = enumeration.nextElement().toString();
				if (name.indexOf(".class") >= 0 && name.indexOf("$") < 0) {
					name = name.replace('/', '.');
					name = name.substring(0, name.length() - 6);
					c = Class.forName(name, false, classLoader);
					interfaces = c.getInterfaces();
					for (int i = 0; i < interfaces.length; i++) {
						if (interfaces[i].getName().indexOf("gda.util.UsesProperties") > -1) {
							JScrollPane scrollPane = new JScrollPane();
							JTextArea textArea = new JTextArea(20, 80);
							scrollPane.setViewportView(textArea);
							pane.addTab(c.getName(), scrollPane);
							Method method = c.getMethod("getProperties", new Class[] {});
							Object o = method.invoke(null, new Object[] {});
							if (o instanceof Vector) {
								Vector<?> v = (Vector<?>) o;
								for (int j = 0; j < v.size(); j++) {
									textArea.append(v.elementAt(j).toString());
								}
							}
						}
					}
				}
			}

			frame.pack();
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			frame.setLocation((screenSize.width - frame.getWidth()) / 2, (screenSize.height - frame.getHeight()) / 2);
			frame.setVisible(true);
		} catch (Exception e) {
			logger.debug(e.getStackTrace().toString());
		}
	}
}