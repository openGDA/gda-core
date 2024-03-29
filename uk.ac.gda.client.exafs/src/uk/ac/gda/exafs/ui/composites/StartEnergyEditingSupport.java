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

import uk.ac.gda.beans.exafs.XanesRegionParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.exafs.ui.XanesScanParametersUIEditor;

public class StartEnergyEditingSupport extends EditingSupport {

	private TableViewer viewer;
	private XanesScanParameters bean;
	XanesScanParametersUIEditor editor;

	public StartEnergyEditingSupport(TableViewer viewer, XanesScanParameters bean, XanesScanParametersUIEditor editor) {
		super(viewer);
		this.viewer = viewer;
		this.bean = bean;
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
		double startEnergy = ((XanesRegionParameters) element).getStartEnergy();
		return String.valueOf(startEnergy);
	}

	@Override
	protected void setValue(Object element, Object value) {
		((XanesRegionParameters) element).setStartEnergy(Double.parseDouble(value.toString()));
		int region = ((XanesRegionParameters) element).getRegion() - 1;
		bean.getRegions().get(region).setEnergy(Double.parseDouble(value.toString()));
		try {
			editor.updatePlottedPoints();
			editor.setDirty(true);
		} catch (Exception e) {
		}
		viewer.refresh();
	}
}