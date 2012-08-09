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

package gda.simplescan;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

public class DescriptionEditingSupport extends EditingSupport {
	private TableViewer viewer;
	private SimpleScan bean;
	private SimpleScanUIEditor editor;
	
	public DescriptionEditingSupport(TableViewer viewer, SimpleScan bean, SimpleScanUIEditor editor) {
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
		for (int i=0;i<bean.getDetectors().size();i++){
			if(bean.getDetectors().get(i).getDetectorName().equals(element.toString())){
				return bean.getDetectors().get(i).getDetectorDescription();
			}
		}
		return element;
	}

	@Override
	protected void setValue(Object element, Object value) {
		for (int i=0;i<bean.getDetectors().size();i++){
			if(bean.getDetectors().get(i).getDetectorName().equals(element.toString())){
				bean.getDetectors().get(i).setDetectorDescription(String.valueOf(value));
			}
		}
		viewer.refresh();
	}
	
	public SimpleScan getBean(){
		return bean;
	}
}
