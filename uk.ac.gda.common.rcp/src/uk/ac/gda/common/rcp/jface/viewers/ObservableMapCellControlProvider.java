/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.common.rcp.jface.viewers;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableMapCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableItem;

/**
 * A ObservableMapCellLabelProvider extended to paint a progress bar to represent the value extracted from the cell element via 
 * the IObservableMap. An ElementToProgressConverter is used to convert from the value to an integer
 * 
 */
 public class ObservableMapCellControlProvider extends ObservableMapCellLabelProvider{
	
	private static final String TABLEEDITORDATAKEY = "TABLEEDITOR";

	
	public interface ControlFactoryAndUpdater {
		Control createControl(Composite parent);
		void updateControl(Control control, Object value);
	}

	private ControlFactoryAndUpdater controlFactory;
	private String uniqueKey;
	
	public ObservableMapCellControlProvider(IObservableMap attributeMap, ControlFactoryAndUpdater controlFactory,
			String uniqueKey) {
		super(attributeMap);
		this.controlFactory =controlFactory;
		this.uniqueKey = TABLEEDITORDATAKEY + uniqueKey;
	}


	@Override
	public void update(ViewerCell cell) {
		TableItem item = (TableItem)cell.getItem();
		Control control=null;
		
		Object e = cell.getElement();
		Object itemData = item.getData(uniqueKey);
		if(itemData!= null && itemData instanceof TableEditor){
			control = ((TableEditor)itemData).getEditor();
		}
		if( control==null){
			control = controlFactory.createControl((Composite)cell.getViewerRow().getControl());
			TableEditor editor = new TableEditor(item.getParent());
			editor.grabHorizontal=true;
			editor.grabVertical = true;
			editor.setEditor(control,  item,  cell.getColumnIndex());
			editor.layout();
			//we need to dispose the TableEditor and ProgressBar when the TableItem is disposed otherwise there will be a build up
			//of TableEditor and ProgressBars being constructed as the list changes which will only
			//be disposed when the table is disposed. 
			item.setData(uniqueKey, editor);
			item.addDisposeListener(new DisposeListener() {
				
				@Override
				public void widgetDisposed(DisposeEvent e) {
					Object data = e.widget.getData(uniqueKey);
					if( data != null){
						TableEditor tableEditor = (TableEditor)data;
						tableEditor.getEditor().dispose();
						tableEditor.dispose();
					}
				}
			});
		}
		Object value = attributeMaps[0].get(e);
		controlFactory.updateControl(control, value);
	}
}

