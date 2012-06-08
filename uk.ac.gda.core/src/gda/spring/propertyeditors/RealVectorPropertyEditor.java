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

import org.apache.commons.math.linear.MatrixUtils;
import org.apache.commons.math.linear.RealVector;
import org.springframework.util.StringUtils;

/**
 * A {@link PropertyEditor} for {@link RealVector}s. Strings must be in the format <code>{1,2,3}</code>.
 */
public class RealVectorPropertyEditor extends PropertyEditorSupport {

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.hasText(text)) {
			try {
				// remove spaces
				text = text.replace(" ", "");
				
				// remove leading/trailing braces
				text = text.replace("{", "").replace("}", "");
				
				String[] valueStrings = text.split(",");
				double[] values = new double[valueStrings.length];
				for (int j=0; j<valueStrings.length; j++) {
					values[j] = Double.valueOf(valueStrings[j]);
				}
				
				RealVector vector = MatrixUtils.createRealVector(values);
				setValue(vector);
			} catch (Throwable e) {
				throw new IllegalArgumentException("Could not convert " + StringUtils.quote(text) + " to a RealVector", e);
			}
		} else {
			setValue(null);
		}
	}
	
	@Override
	public RealVector getValue() {
		return (RealVector) super.getValue();
	}
	
	@Override
	public String getAsText() {
		return getValue().toString().replace(';', ',').replace(" ", "");
	}
	
}
