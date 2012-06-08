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

import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Provides a renderer for JTables that render cell contents left justified, rather than default right justified.
 * Overrides DefaultTableCellRenderer's getTableCellRendererComponent method.
 * 
 * @see javax.swing.table.DefaultTableCellRenderer
 */
public class LeftRenderer extends DefaultTableCellRenderer {
	DecimalFormat df;

	/**
	 * @param decimalFormat
	 *            Specifies the format of cell content rendering.
	 */
	public LeftRenderer(DecimalFormat decimalFormat) {
		df = decimalFormat;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean isFocused,
			int row, int column) {
		String newValue;
		if (value instanceof String)
			newValue = (String) value;
		else
			newValue = df.format(value);

		Component component = super.getTableCellRendererComponent(table, newValue, isSelected, isFocused, row, column);
		((JLabel) component).setHorizontalAlignment(SwingConstants.LEFT);
		return component;

	}
}
