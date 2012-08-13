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

package uk.ac.gda.exafs.ui.views.scalersmonitor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class XmapI1MonitorViewData {
	private String[] titles = { "I1", "Total Counts", "FF / I1", "FF Rate", "Dead Time" };
	private String[] formats = { "%.0f", "%.0f", "%.4f", "%.4f", "%.4f" };
	private Table table;

	public void setI1(double val) {
		String txt = String.format(formats[0], val);
		table.getItem(0).setText(0, txt);
	}

	public void setTotalCounts(double val) {
		String txt = String.format(formats[1], val);
		table.getItem(0).setText(1, txt);
	}

	public void setFFI1(double val) {
		String txt = String.format(formats[2], val);
		table.getItem(0).setText(2, txt);
	}

	public void setRate(double val) {
		String txt = String.format(formats[3], val);
		table.getItem(0).setText(3, txt);
	}

	public void setDeadTime(double val) {
		String txt = String.format(formats[4], val);
		table.getItem(0).setText(4, txt);
	}

	public XmapI1MonitorViewData(Composite parent) {
		table = new Table(parent, SWT.BORDER);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setItemCount(1);

		TableLayout layout = new TableLayout();
		final int columnWeight = Math.round(100 / titles.length);
		for (String title : titles) {
			layout.addColumnData(new ColumnWeightData(columnWeight, true));
			TableColumn column1 = new TableColumn(table, SWT.CENTER);
			column1.setText(title);
			column1.setAlignment(SWT.CENTER);
		}
		for (int i = 0; i < titles.length; i++) {
			table.getItem(0).setText(i, "                   "); // this string helps set the default width of the column
		}
		table.setLayout(layout);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(table);
	}
}