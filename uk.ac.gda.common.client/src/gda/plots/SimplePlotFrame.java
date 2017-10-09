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

package gda.plots;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;

/**
 * Simple JFrame that displays a SimplePlot.
 */
public class SimplePlotFrame extends JFrame {
	/**
	 *
	 */
	public final SimplePlot simplePlot;

	/**
	 * Constructor - creates a LINECHART with autoRange = true
	 */
	public SimplePlotFrame() {
		this(SimplePlot.LINECHART, true);
	}
	/**
	 * Constructor
	 * @param type SimplePlot.LINECHART or SimplePlot.BARCHART
	 * @param autoRange
	 */
	SimplePlotFrame(int type, boolean autoRange) {
		super();
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = c.gridy = 0;
		c.weightx = c.weighty = 1;
		add(simplePlot = new SimplePlot(type, autoRange, true), c);
		simplePlot.setTitle("");
	}
}
