/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.spring.propertyeditors;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import org.springframework.util.StringUtils;

/**
 * A {@link PropertyEditor} for 2D double arrays. Strings must be in the format
 * <code>{{1,2,3},{4,5,6},{7,8,9}}</code> or <code>{{1;2;3};{4;5;6};{7;8;9}}</code>.
 */
public class TwoDDoubleArrayPropertyEditor extends PropertyEditorSupport {

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.hasText(text)) {
			try {
				// remove spaces
				text = text.replace(" ", "");

				// workaround for GDA-2492 - can't use commas in property values
				text = text.replace(";", ",");

				String[] rows = text.split("\\},\\{");
				double[][] values = new double[rows.length][];
				for (int i=0; i<rows.length; i++) {
					String row = rows[i];
					row = row.replace("{", "").replace("}", "").replace(" ", "");
					String[] valueStrings = row.split(",");
					values[i] = new double[valueStrings.length];
					for (int j=0; j<valueStrings.length; j++) {
						values[i][j] = Double.valueOf(valueStrings[j]);
					}
				}
				setValue(values);
			} catch (Throwable e) {
				throw new IllegalArgumentException("Could not convert " + StringUtils.quote(text) + " to a 2D double array", e);
			}
		} else {
			setValue(null);
		}
	}

	@Override
	public double[][] getValue() {
		return (double[][]) super.getValue();
	}

}
