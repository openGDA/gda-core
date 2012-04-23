/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.composites;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.ui.IWorkbenchPartSite;

import uk.ac.gda.beans.exafs.XanesRegionParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.exafs.ui.XanesScanParametersUIEditor;

public class TimeEditingSupport extends EditingSupport {
	private TableViewer viewer;
	private XanesScanParameters bean;
	private IWorkbenchPartSite site;
	XanesScanParametersUIEditor editor;
	
	public TimeEditingSupport(TableViewer viewer, XanesScanParameters bean, IWorkbenchPartSite site, XanesScanParametersUIEditor editor) {
		super(viewer);
		this.viewer = viewer;
		this.bean = bean;
		this.site = site;
		this.editor = editor;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return new TextCellEditor(viewer.getTable());
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		double time = ((XanesRegionParameters) element).getTime();
		return String.valueOf(time);
	}

	@Override
	protected void setValue(Object element, Object value) {
		((XanesRegionParameters) element).setTime(Double.parseDouble(value.toString()));
		int region = ((XanesRegionParameters) element).getRegion()-1;
		bean.getRegions().get(region).setTime(Double.parseDouble(value.toString()));
		viewer.refresh();
		try {
			editor.setDirty(true);
		} catch (Exception e) {
		}
	}
}
