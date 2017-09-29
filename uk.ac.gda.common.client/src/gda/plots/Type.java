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

import java.awt.Component;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

/**
 * An enum of the three line types allowed in SimplePlots.
 */
public enum Type {
	/** */
	LINEONLY(true, false, "Line only"),
	/** */
	POINTSONLY(false, true, "Points only"),
	/** */
	LINEANDPOINTS(true, true, "Line and Points");

	private boolean drawLine;

	private boolean drawPoints;

	private String bestName;

	private Type(boolean drawLine, boolean drawPoints, String bestName) {
		this.drawLine = drawLine;
		this.drawPoints = drawPoints;
		this.bestName = bestName;
	}

	/**
	 * Returns whether or not this Type requires the points to be drawn
	 *
	 * @return true if the points should be drawn
	 */
	public boolean getDrawPoints() {
		return drawPoints;
	}

	/**
	 * Returns whether or not this Type requires the line to be drawn
	 *
	 * @return true if the line should be drawn
	 */
	public boolean getDrawLine() {
		return drawLine;
	}

	/**
	 * Returns the display name of this type (which may not be the same as the enum name).
	 *
	 * @return the display name
	 */
	@Override
	public String toString() {
		return bestName;
	}

	/**
	 * Creates a Type from a String (defaults to LINEONLY).
	 *
	 * @param string
	 *            the String
	 * @return the Type
	 */
	static Type fromString(String string) {
		Type fromString = LINEONLY;
		for (Type t : Type.values()) {
			if ((t.toString().toUpperCase()).equals(string.toUpperCase())) {
				fromString = t;
				break;
			}
		}

		return fromString;
	}

	/**
	 * Returns a JComboBox suitable for choosing a line type.
	 *
	 * @return the JComboBox
	 */
	public static JComboBox<Type> getChooser() {
		JComboBox<Type> typesCombo = new JComboBox<>();
		for (Type t : values()) {
			typesCombo.addItem(t);
		}

		typesCombo.setRenderer(new TypeCellRenderer());
		return typesCombo;
	}
}

class TypeCellRenderer implements ListCellRenderer<Object>
{
	private HashMap<Type, JLabel> comps = new HashMap<>(); //http://www.blogjava.net/rednight/archive/2007/01/16/94089.html
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		Type type = Type.class.cast(value);
		JLabel component = comps.get(Type.class.cast(value));
		if(component == null){
			component = new JLabel();
			component.setText(value.toString());
			component.setIcon(new SimpleIcon(Type.class.cast(value), 23, 18));
			component.setVerticalTextPosition(SwingConstants.BOTTOM);
			component.setHorizontalTextPosition(SwingConstants.RIGHT);
			component.setIconTextGap(20);
			comps.put(type, component);
		}
		return component;
	}
}