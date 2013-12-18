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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableMapCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.TableItem;

/**
 * A ObservableMapCellLabelProvider extended to paint a progress bar to represent the value extracted from the cell element via 
 * the IObservableMap. An ElementToProgressConverter is used to convert from the value to an integer
 * 
 */
 public class ObservableMapProgressBarProvider extends ObservableMapCellLabelProvider{
	
	public interface ElementToProgressConverter {

		int convertToProgress(Object value);
	}
	
	
	public ObservableMapProgressBarProvider(IObservableMap attributeMap, ElementToProgressConverter converter) {
		super(attributeMap);
		this.converter = converter;
	}

	int max=100;
	
	
	public int getMax() {
		return max;
	}

	/**
	 * 
	 * @param max applied to ProgressBar
	 */
	public void setMax(int max) {
		this.max = max;
	}

	final Map<Object, ProgressBar> progressBars = new HashMap<Object, ProgressBar>();
	private ElementToProgressConverter converter;

	@Override
	public void update(ViewerCell cell) {
		TableItem item = (TableItem)cell.getItem();
		ProgressBar bar;
		Object e = cell.getElement();
		if( progressBars.containsKey(e)){
			bar = progressBars.get(e);
		}
		else{
			bar = new ProgressBar((Composite)cell.getViewerRow().getControl(), SWT.NONE);
			bar.setMaximum(max);
			progressBars.put(e, bar);
		}
		TableEditor editor = new TableEditor(item.getParent());
		editor.grabHorizontal=true;
		editor.grabVertical = true;
		Object value = attributeMaps[0].get(e);
		bar.setSelection(converter.convertToProgress(value));
		editor.setEditor(bar,  item,  cell.getColumnIndex());
		editor.layout();
		
	}
}

