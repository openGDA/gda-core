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

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Stroke;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

/**
 * An enum of the various line patterns allowed in SimplePlots
 */
public enum Pattern {
	/**  */
	SOLID("Solid"),
	/**  */
	DOTTED("Dotted", 2.0f, 3.0f),
	/**  */
	DASHED("Dashed", 5.0f, 5.0f),
	/**  */
	DOTDASHED("Dot-dashed", 2.0f, 4.0f, 5.0f, 4.0f);

	private float[] pattern = null;

	private String bestName;

	private Pattern(String bestName, float... pattern) {
		this.bestName = bestName;

		// For the SOLID case the passed in pattern will be an array of
		// length 0. We want to retain the value null for pattern because
		// a null dash_array works in BasicStroke but an empty dash_array
		// causes an error.
		if (pattern.length > 0) {
			this.pattern = pattern;
		}
	}

	/**
	 * Returns the actual array of floats which represents the pattern
	 * 
	 * @return the array of floats
	 */
	public float[] getPattern() {
		return pattern;
	}

	/**
	 * Returns the Stroke corresponding to the Pattern for a given lineWidth
	 * 
	 * @param lineWidth
	 *            the line width in pixels
	 * @return a Stroke of the correct pattern
	 */
	public Stroke getStroke(int lineWidth) {
		return new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0, pattern, 0);
	}

	/**
	 * Returns the display name (which may be different from the Enum name)
	 * 
	 * @return the display name
	 */
	@Override
	public String toString() {
		return bestName;
	}

	/**
	 * Creates a Pattern from the String given
	 * 
	 * @param string
	 *            the name
	 * @return the Patterb with that name (default null)
	 */
	public static Pattern fromString(String string) {
		Pattern fromString = null;
		for (Pattern ss : Pattern.values()) {
			if (ss.toString().equals(string) || ss.toString().equals(string.toUpperCase())) {
				fromString = ss;
				break;
			}
		}

		return fromString;
	}

	/**
	 * Creates a Marker from the int given
	 * 
	 * @param counter
	 * @return the Marker with that ordinal (default BOX)
	 */
	public static Pattern fromCounter(int counter) {
		Pattern fromCounter = Pattern.SOLID;
		for (Pattern m : Pattern.values()) {
			if (m.ordinal() == counter) {
				fromCounter = m;
				break;
			}
		}

		return fromCounter;
	}	
	
	/**
	 * Returns a JComboBox which can be used to choose between the Patterns
	 * 
	 * @return a suitable JComboBox
	 */
	public static JComboBox getChooser() {
		JComboBox patternsCombo = new JComboBox();
		for (Pattern p : values()) {
			patternsCombo.addItem(p);
		}

		patternsCombo.setRenderer(new ListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				JLabel component = new JLabel();
				component.setText(value.toString());
				component.setIcon(new SimpleIcon((Pattern) value, 23, 18));
				component.setVerticalTextPosition(SwingConstants.BOTTOM);
				component.setHorizontalTextPosition(SwingConstants.RIGHT);
				component.setIconTextGap(10);
				return component;
			}
		});
		return patternsCombo;
	}

}
