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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import uk.ac.diamond.daq.mapping.region.PolygonMappingRegion;
import uk.ac.diamond.daq.mapping.ui.experiment.AbstractRegionAndPathComposite;

public class PolygonRegionComposite extends AbstractRegionAndPathComposite {

	private final int columnWidth = 130;
	private TableViewer polygonTableViewer;
	private PolygonMappingRegion polygonRegion;
	private PropertyChangeListener listener = (evt ->
	{polygonTableViewer.setInput(((PolygonMappingRegion) evt.getSource()).getPoints());
		polygonTableViewer.refresh();});

	public PolygonRegionComposite(Composite parent, PolygonMappingRegion region) {
		super(parent, SWT.NONE);

		this.polygonRegion = region;

		// Set the layout of the main composite area
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).hint(SWT.DEFAULT, 200)
				.applyTo(this);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(this);

		// Create the table
		polygonTableViewer = new TableViewer(this,
				SWT.MULTI | SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
				.hint(SWT.DEFAULT, 110).applyTo(polygonTableViewer.getControl());

		createColumns(polygonTableViewer);

		// make lines and header visible
		final Table table = polygonTableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		polygonTableViewer.setContentProvider(new ArrayContentProvider());
		polygonTableViewer.setInput(region.getPoints());
		region.addPropertyChangeListener(listener);

	}

	private void createColumns(TableViewer polygonTableViewer) {

		// X column
		TableViewerColumn xCol = createTableViewerColumn(polygonTableViewer, "X (mm)", columnWidth);
		xCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Point p = (Point) element;
				return String.valueOf(round(p.getX(), 4));
			}
		});


		// Y column
		TableViewerColumn yCol = createTableViewerColumn(polygonTableViewer, "Y (mm)", columnWidth);
		yCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Point p = (Point) element;
				return String.valueOf(round(p.getY(), 4));
			}
		});

	}

	private TableViewerColumn createTableViewerColumn(final TableViewer viewer, final String title, final int width) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.CENTER);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(width);
		column.setResizable(true);
		return viewerColumn;
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
		polygonRegion.removePropertyChangeListener(listener);
		super.dispose();
	}

}
