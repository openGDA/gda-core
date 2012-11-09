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


import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

/**
 * A class to display the percentage of memory used
 */
public class MemoryUsage extends JPanel {
	private static MemoryUsage instance = new MemoryUsage();

	private int minUsage = 0;

	private int maxUsage = 100;

	private JProgressBar saxsMemoryUsage;

	private JProgressBar waxsMemoryUsage;

	private double saxsMemoryRatio = 0.0;

	private double waxsMemoryRatio = 0.0;

	private int timeFrameCount = 0;

	private double saxsPercentage = 0.0;

	private double waxsPercentage = 0.0;

	/**
	 * @return instance
	 */
	public static MemoryUsage getInstance() {
		return instance;
	}

	/**
	 * Private constructor to ensure singleton class
	 */
	private MemoryUsage() {
		saxsMemoryUsage = new JProgressBar();
		waxsMemoryUsage = new JProgressBar();

		saxsMemoryUsage.setMinimum(minUsage);
		saxsMemoryUsage.setMaximum(maxUsage);
		waxsMemoryUsage.setMinimum(minUsage);
		waxsMemoryUsage.setMaximum(maxUsage);
		saxsMemoryUsage.setStringPainted(true);
		waxsMemoryUsage.setStringPainted(true);

		JLabel label;
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.anchor = GridBagConstraints.EAST;
		constraints.gridx = 0;
		constraints.gridy = 0;

		label = new JLabel("SAXS  ");
		label.setForeground(Color.black);
		add(label, constraints);

		++constraints.gridx;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1.0;
		add(saxsMemoryUsage, constraints);

		constraints.gridx = 0;
		++constraints.gridy;
		constraints.fill = GridBagConstraints.NONE;
		constraints.weightx = 0.0;
		add(Box.createVerticalStrut(5), constraints);

		label = new JLabel(" WAXS  ");
		label.setForeground(Color.black);
		constraints.gridx = 0;
		++constraints.gridy;
		add(label, constraints);

		++constraints.gridx;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1.0;
		add(waxsMemoryUsage, constraints);

		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Memory Usage",
				TitledBorder.LEFT, TitledBorder.TOP, null, Color.black));

	}

	/**
	 * @param value
	 */
	public void setTimeFrameCount(int value) {
		timeFrameCount = value;

		saxsPercentage = timeFrameCount * saxsMemoryRatio * 100.0;
		setMemoryUsageValue(saxsMemoryUsage, saxsPercentage);

		waxsPercentage = timeFrameCount * waxsMemoryRatio * 100.0;
		setMemoryUsageValue(waxsMemoryUsage, waxsPercentage);
	}

	/**
	 * set ratio for SAXS or WAXS
	 * @param type SAXS or WAXS
	 * @param memoryRatio
	 */
	public void setMemoryRatio(String type, double memoryRatio) {
		if ("SAXS".equals(type)) setSaxsMemoryRatio(memoryRatio);
		if ("WAXS".equals(type)) setWaxsMemoryRatio(memoryRatio);
	}
	
	/**
	 * @param saxsMemoryRatio
	 */
	public void setSaxsMemoryRatio(double saxsMemoryRatio) {
		this.saxsMemoryRatio = saxsMemoryRatio;

		saxsPercentage = timeFrameCount * saxsMemoryRatio * 100.0;
		setMemoryUsageValue(saxsMemoryUsage, saxsPercentage);
	}

	/**
	 * @param waxsMemoryRatio
	 */
	public void setWaxsMemoryRatio(double waxsMemoryRatio) {
		this.waxsMemoryRatio = waxsMemoryRatio;

		waxsPercentage = timeFrameCount * waxsMemoryRatio * 100.0;
		setMemoryUsageValue(waxsMemoryUsage, waxsPercentage);
	}

	private void actionOnMemoryOverflow() {
		if (saxsPercentage > 100.0 || waxsPercentage > 100.0)
			ActionHelper.getInstance().disableCollection();
		else
			ActionHelper.getInstance().enableCollection();
	}

	/**
	 * Sets memory usage value in experiment
	 * 
	 * @param memoryUsage
	 *            the progress bar instance
	 * @param pcent
	 *            the new percentage complete value
	 */
	private void setMemoryUsageValue(JProgressBar memoryUsage, double pcent) {
		// Update the memory progress bar. The indicator will normally
		// be green but will display red when the memory capacity is exceeded.
		if (pcent > 100.0) {
			pcent = 100.0;
			memoryUsage.setForeground(Color.red);
		} else {
			memoryUsage.setForeground(Color.green);
		}
		memoryUsage.setValue((int) pcent);
		actionOnMemoryOverflow();
	}
}
