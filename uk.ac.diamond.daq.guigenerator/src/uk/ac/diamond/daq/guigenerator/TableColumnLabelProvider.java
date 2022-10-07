/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.guigenerator;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class TableColumnLabelProvider extends ColumnLabelProvider{
	private final String column;

	public TableColumnLabelProvider(String column){
		this.column = column;
	}

	@Override
	public String getText(Object element) {
		Object value = BeanProperties.value(element.getClass(), column).getValue(element);
		if (value instanceof RGB){
			return null;
		}
		return value.toString();
	}

	@Override
	public Color getBackground(Object element) {
		Object value = BeanProperties.value(element.getClass(), column).getValue(element);
		if (value instanceof RGB){
			return new Color(Display.getCurrent(), (RGB)value);
		}
		return null;
	}


}
