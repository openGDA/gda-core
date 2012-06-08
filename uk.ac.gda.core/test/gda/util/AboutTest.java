/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

import javax.swing.JFrame;

/**
 * Tests the {@link About} class.
 */
public class AboutTest {

	/**
	 * Main program.
	 * 
	 * @param args command-line arguments
	 * 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		JFrame frame = new JFrame();
		About about = new About(frame);
		about.setVisible(true);
		frame.dispose();
	}

}
