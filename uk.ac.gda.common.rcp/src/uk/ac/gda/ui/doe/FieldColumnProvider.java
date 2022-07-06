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

package uk.ac.gda.ui.doe;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import uk.ac.gda.doe.RangeInfo;

public class FieldColumnProvider extends ColumnLabelProvider {

	private String[] properties;
	private String   property;
	
	FieldColumnProvider(String[] properties) {
	    this.properties = properties;
	}
	
	@Override
	public String getText(final Object element) {
		
		if (!(element instanceof RangeInfo)) return null;
		
		final RangeInfo info = (RangeInfo)element;
		return info.getColumnValue(property);
	}
	
	@Override
	public void update(ViewerCell cell) {
		if (properties!=null&&properties.length>0) {
            this.property = properties[cell.getColumnIndex()];
		}
        super.update(cell);
	}
}
