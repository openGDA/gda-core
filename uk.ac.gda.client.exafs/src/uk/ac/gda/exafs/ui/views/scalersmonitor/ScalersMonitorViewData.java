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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class ScalersMonitorViewData {
	private String[] titles = { "I0", "It", "Iref", "ln(It/I0)","ln(Iref/It)", "Total Counts", "Single-element / I0" };
	private String[] formats = { "%.0f","%.0f", "%.0f", "%.4f", "%.4f", "%.0f", "%.4f" };
	private String detectorName = "Ge";
	private Table table;

	

	public void setI0(double val) {
		String txt = String.format(formats[0], val);
		table.getItem(0).setText(0, txt);
	}

	public void setIt(double val) {
		String txt = String.format(formats[1], val);
		table.getItem(0).setText(1, txt);
	}
	
	public void setIref(double val) {
		String txt = String.format(formats[2], val);
		table.getItem(0).setText(2, txt);
	}

	public void setItI0(double val) {
		String txt = String.format(formats[3], val);
		table.getItem(0).setText(3, txt);
	}
	
	public void setIrefIt(double val) {
		String txt = String.format(formats[4], val);
		table.getItem(0).setText(4, txt);
	}

	public void setFF(double val) {
		String txt = String.format(formats[5], val);
		table.getItem(0).setText(5, txt);
	}

	public void setFFI0(double val) {
		String txt = String.format(formats[6], val);
		table.getItem(0).setText(6, txt);
	}


	public ScalersMonitorViewData(Group grpCurrentCountRates) {
		table = new Table(grpCurrentCountRates, SWT.MULTI | SWT.BORDER | SWT.NO_FOCUS);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setItemCount(1);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.heightHint = -1;
		table.setLayoutData(gd);

		final TableColumn[] columns = new TableColumn[titles.length];
		for (int i = 0; i < titles.length; i++) {
			columns[i] = new TableColumn(table, SWT.NONE);
			columns[i].setText(titles[i]);
			columns[i].setAlignment(SWT.CENTER);
		}
		for (int i = 0; i < titles.length; i++) {
			table.getItem(0).setText(i, "                 "); // this string helps set the default width of the column
		}
		for (int i = 0; i < titles.length; i++) {
			table.getColumn(i).pack();
		}
		
	}

	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}

	public String getDetectorName() {
		return detectorName;
	}


}