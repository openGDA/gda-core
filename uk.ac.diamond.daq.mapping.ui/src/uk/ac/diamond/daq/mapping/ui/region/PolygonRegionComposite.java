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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import uk.ac.diamond.daq.mapping.region.MutablePoint;
import uk.ac.diamond.daq.mapping.region.PolygonMappingRegion;
import uk.ac.diamond.daq.mapping.ui.experiment.AbstractRegionAndPathComposite;

public class PolygonRegionComposite extends AbstractRegionAndPathComposite {

	private TableViewer polygonTableViewer;
	private PolygonMappingRegion polygonRegion;
	private enum Axis {X, Y}
	private List<MutablePoint> polyPoints = new ArrayList<>(10);
	private Pattern pattern = Pattern.compile("-?\\d+\\.?\\d*");
	private PropertyChangeListener regionPointsListener = evt -> {
		this.polyPoints = ((PolygonMappingRegion) evt.getSource()).getPoints();
		polygonTableViewer.setInput(this.polyPoints);
		polygonTableViewer.refresh();
	};

	public PolygonRegionComposite(Composite parent, PolygonMappingRegion region) {
		super(parent, SWT.NONE);

		this.polygonRegion = region;

		// Set the layout of the main composite area
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER)
		.applyTo(this);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);

		Composite tableComposite = new Composite(this, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(SWT.DEFAULT, 110)
		.applyTo(tableComposite);

		// Create the table
		polygonTableViewer = new TableViewer(tableComposite,
				SWT.MULTI | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
		.applyTo(polygonTableViewer.getControl());

		createColumns(polygonTableViewer);

		// make lines and header visible
		final Table table = polygonTableViewer.getTable();
		TableColumnLayout tableLayout = new TableColumnLayout();
		tableComposite.setLayout(tableLayout);
		for (TableColumn tc : table.getColumns()) {
			tableLayout.setColumnData(tc, new ColumnWeightData(50));
		}
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		polygonTableViewer.setContentProvider(new ArrayContentProvider());
		polygonTableViewer.setInput(region.getPoints());
		region.addPropertyChangeListener(regionPointsListener);

	}

	private void createColumns(TableViewer polygonTableViewer) {

		// X column
		TableViewerColumn xCol = createTableViewerColumn(polygonTableViewer, "X (mm)");
		xCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				MutablePoint p = (MutablePoint) element;
				return String.valueOf(round(p.getX(), 4));
			}
		});

		xCol.setEditingSupport(new PointEditingSupport(polygonTableViewer, Axis.X));
		xCol.getColumn().setToolTipText("Edit X position of vertex");

		// Y column
		TableViewerColumn yCol = createTableViewerColumn(polygonTableViewer, "Y (mm)");
		yCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				MutablePoint p = (MutablePoint) element;
				return String.valueOf(round(p.getY(), 4));
			}
		});

		yCol.setEditingSupport(new PointEditingSupport(polygonTableViewer, Axis.Y));
		yCol.getColumn().setToolTipText("Edit Y position of vertex");

	}

	private TableViewerColumn createTableViewerColumn(final TableViewer viewer, final String title) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.CENTER);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setResizable(true);
		return viewerColumn;
	}

	private class PointEditingSupport extends EditingSupport {

		private CellEditor cellEditor;
		private Axis axis;

		public PointEditingSupport(TableViewer viewer, Axis axis) {
			super(viewer);
			this.axis = axis;
			cellEditor = new TextCellEditor((Composite) viewer.getControl());
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
			// polyPoints is empty if user has switched region types, so replenish with fresh points
			if (polyPoints.isEmpty()) {
				polyPoints = polygonRegion.getPoints();
			}

			if (axis == Axis.X) {
				((MutablePoint) element).setX(Double.valueOf((String) value));
			} else {
				((MutablePoint) element).setY(Double.valueOf((String) value));
			}

			polygonRegion.setPoints(polyPoints);
			polygonTableViewer.refresh();
		}
	}

	private double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

	@Override
	public void dispose() {
		polygonRegion.removePropertyChangeListener(regionPointsListener);
		super.dispose();
	}

}
