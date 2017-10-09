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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

/**
 * Creates a JComboBox for choosing one of the values in an Enum.
 */
class EnumChooser extends JComboBox<Object> implements ListCellRenderer<Object> {

	/**
	 * Constructor - not quite right yet!
	 *
	 * @param c
	 *            the Class should be forced to be an Enum but isn't yet.
	 */
	EnumChooser(Class<?> c) {

		Method m = null;
		try {
			m = c.getMethod("values", new Class[] {});
		} catch (SecurityException e2) {
			e2.printStackTrace();
		} catch (NoSuchMethodException e2) {
			e2.printStackTrace();
		}

		if (m != null) {
			try {
				for (Object o : (Object[]) m.invoke(null, new Object[] {})) {
					addItem(o);
				}
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
			}
		}
		setRenderer(this);
	}

	/**
	 * Implements the ListCellRenderer interface to provide a suitable component to display the value.
	 *
	 * @param list
	 *            The JList we're painting.
	 * @param value
	 *            The value returned by list.getModel().getElementAt(index).
	 * @param index
	 *            The cells index.
	 * @param isSelected
	 *            True if the specified cell was selected.
	 * @param cellHasFocus
	 *            True if the specified cell has the focus.
	 * @return A component whose paint() method will render the specified value.
	 */
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {
		JLabel component = new JLabel();
		component.setText(value.toString());
		component
				.setIcon(new SimpleIcon((Enum<?>) value, 20, component.getFontMetrics(component.getFont()).getHeight()));
		component.setVerticalTextPosition(SwingConstants.CENTER);
		component.setHorizontalTextPosition(SwingConstants.RIGHT);
		component.setIconTextGap(15);
		return component;
	}
}
