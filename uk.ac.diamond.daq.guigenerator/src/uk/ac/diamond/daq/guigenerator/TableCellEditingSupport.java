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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColorCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.richbeans.widgets.cell.NumberCellEditor;
import org.eclipse.richbeans.widgets.cell.SpinnerCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Table;

public class TableCellEditingSupport extends EditingSupport {
	private final Map<Class<?>, Function<String, ?>> convertors = new HashMap<>();
	private final TableColumnModel column;
	private Table table;

	public TableCellEditingSupport(ColumnViewer viewer, Table table, TableColumnModel column) {
		super(viewer);
		this.table = table;
		this.column = column;

		convertors.put(String.class, Function.<String>identity());
		convertors.put(Integer.class, Integer::valueOf);
		convertors.put(Integer.TYPE, Integer::valueOf);
		convertors.put(Long.class, Long::valueOf);
		convertors.put(Long.TYPE, Long::valueOf);
		convertors.put(Double.class, Double::valueOf);
		convertors.put(Double.TYPE, Double::valueOf);
		convertors.put(Float.class, Float::valueOf);
		convertors.put(Float.TYPE, Float::valueOf);
	}

	@Override
	public void setValue(Object element, Object value) {
		IBeanValueProperty property = BeanProperties.value(element.getClass(), column.getName());
		Object parsedValue = parseValue(value, property);
		property.setValue(element, parsedValue);
	}

	private Object parseValue(Object value, IBeanValueProperty property) {
		if (value.getClass() == property.getValueType()) return value;
		return convertors
				.get(property.getValueType())
				.apply(value.toString());
	}

	@Override
	protected Object getValue(Object element) {
		return BeanProperties.value(element.getClass(), column.getName()).getValue(element);
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		Class<?> type = (Class<?>)BeanProperties.value(element.getClass(), column.getName()).getValueType();

		if (isInteger(type)){
			SpinnerCellEditor editor = new SpinnerCellEditor(table, SWT.NONE);
			editor.setMinimum(column.getMinInt());
			editor.setMaximum(column.getMaxInt());
			return editor;
		} else if (isNumber(type)){
			return new NumberCellEditor(table, type, column.getMinDouble(), column.getMaxDouble(), null, SWT.NONE);
		} else if (isRGB(type)){
			return new ColorCellEditor(table, SWT.NONE);
		}
		return new TextCellEditor(table, SWT.NONE);
	}

	private boolean isRGB(Class<?> type) {
		return RGB.class == type;
	}

	private boolean isNumber(Class<?> type) {
		return Number.class.isAssignableFrom(type) || type.isPrimitive();
	}

	private <T> boolean isInteger(Class<T> clazz){
		return
				Integer.class.isAssignableFrom(clazz)||
				Long.class.isAssignableFrom(clazz)||
				Integer.TYPE == clazz ||
				Long.TYPE == clazz;
	}

	@Override
	public boolean canEdit(Object element) {
		Class<?> valueType = (Class<?>)BeanProperties.value(element.getClass(), column.getName()).getValueType();
		return isNumber(valueType) || String.class == valueType || isRGB(valueType);
	}
}