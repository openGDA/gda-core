/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.region;

import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import uk.ac.diamond.daq.mapping.region.PolygonMappingRegion;
import uk.ac.diamond.daq.mapping.region.PolygonMappingRegion.MutablePoint;

public class PolygonRegionEditor extends AbstractRegionEditor {

	private TableViewer polygonTableViewer;
	private enum Axis {X, Y}
	private Pattern pattern = Pattern.compile("-?\\d+\\.?\\d*");
	private PropertyChangeListener tableInputChanged = event -> polygonTableViewer.refresh();

	@Override
	public Composite createEditorPart(Composite parent) {

		final Composite composite = super.createEditorPart(parent);
		GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 110).applyTo(composite);

		createTable(composite);

		// Force a TableViewer refresh when the model changes
		getModel().addPropertyChangeListener(tableInputChanged);

		return composite;
	}

	private void createTable(Composite tableComposite) {
		polygonTableViewer = new TableViewer(tableComposite, SWT.MULTI | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		grabHorizontalSpace.applyTo(polygonTableViewer.getControl());

		// content and label providers
		polygonTableViewer.setContentProvider(new ArrayContentProvider());

		TableViewerColumn xCol = createTableViewerColumn(polygonTableViewer, getFastAxisName() + " (mm)");
		xCol.setLabelProvider(new MutablePointLabelProvider(Axis.X));
		xCol.setEditingSupport(new PointEditingSupport(polygonTableViewer, Axis.X));
		xCol.getColumn().setToolTipText("Edit X position of vertex");

		TableViewerColumn yCol = createTableViewerColumn(polygonTableViewer, getSlowAxisName() + " (mm)");
		yCol.setLabelProvider(new MutablePointLabelProvider(Axis.Y));
		yCol.setEditingSupport(new PointEditingSupport(polygonTableViewer, Axis.Y));
		yCol.getColumn().setToolTipText("Edit Y position of vertex");

		final Table table = polygonTableViewer.getTable();
		TableColumnLayout tableLayout = new TableColumnLayout();
		tableComposite.setLayout(tableLayout);
		for (TableColumn tc : table.getColumns()) {
			tableLayout.setColumnData(tc, new ColumnWeightData(50));
		}

		// make lines and header visible
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// TableViewer input
		IListProperty listProperty = Properties.selfList(MutablePoint.class);
		IObservableList input = listProperty.observe(((PolygonMappingRegion) getModel()).getPoints());
		polygonTableViewer.setInput(input);
	}

	private TableViewerColumn createTableViewerColumn(final TableViewer viewer, final String title) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.CENTER);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setResizable(true);
		return viewerColumn;
	}

	private class PointEditingSupport extends EditingSupport {

		private TextCellEditor cellEditor;
		private Axis axis;

		public PointEditingSupport(TableViewer viewer, Axis axis) {
			super(viewer);
			this.axis = axis;
			cellEditor = new TextCellEditor((Composite) viewer.getControl());

			createValidation();
		}

		private void createValidation() {
			String scannableName = axis==Axis.X ? getFastAxisName() :  getSlowAxisName();
			double lowerLimit = getLowerLimit(scannableName);
			double upperLimit = getUpperLimit(scannableName);

			IObservableValue observableText = WidgetProperties.text(SWT.Modify).observe(cellEditor.getControl());
			MultiValidator validator = new MultiValidator() {
				@Override
				protected IStatus validate() {
					double position = Double.parseDouble((String) observableText.getValue());
					if (position < lowerLimit || position > upperLimit) return getLimitsError(lowerLimit, upperLimit);
					return ValidationStatus.ok();
				}
			};

			// Position of decoration depends on the column number
			// so that it is always inside the table
			int decorationPosition = SWT.TOP | (axis == Axis.X ? SWT.RIGHT : SWT.LEFT);
			ControlDecorationSupport.create(validator, decorationPosition);
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return cellEditor;
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}

		@Override
		protected Object getValue(Object element) {
			if (axis == Axis.X) {
				return Double.toString(round(((MutablePoint) element).getX(), 4));
			} else {
				return Double.toString(round(((MutablePoint) element).getY(), 4));
			}
		}

		@Override
		protected void setValue(Object element, Object value) {
			// Ensure only numbers are entered as values
			if (!pattern.matcher((String) value).matches())
				return;

			if (axis == Axis.X) {
				((MutablePoint) element).setX(Double.valueOf((String) value));
			} else {
				((MutablePoint) element).setY(Double.valueOf((String) value));
			}
		}
	}

	@Override
	public void dispose() {
		getModel().removePropertyChangeListener(tableInputChanged);
		super.dispose();
	}

	private double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	private static final Color RED = Display.getDefault().getSystemColor(SWT.COLOR_RED);
	private class MutablePointLabelProvider extends ColumnLabelProvider {

		private final Axis axis;
		private final double lowerLimit;
		private final double upperLimit;

		public MutablePointLabelProvider(Axis axis) {
			this.axis = axis;
			String scannableName = axis == Axis.X ? getFastAxisName() : getSlowAxisName();
			lowerLimit = getLowerLimit(scannableName);
			upperLimit = getUpperLimit(scannableName);
		}

		@Override
		public String getText(Object element) {
			MutablePoint p = (MutablePoint) element;
			double pos = axis == Axis.X ? p.getX() : p.getY();
			return String.valueOf(round(pos, 4));
		}

		@Override
		public Color getForeground(Object element) {
			if (!isWithinLimits(element)) return RED;
			return super.getForeground(element);
		}

		private boolean isWithinLimits(Object element) {
			double position = Double.parseDouble(getText(element));
			return position >= lowerLimit && position <= upperLimit;
		}
	}

}
