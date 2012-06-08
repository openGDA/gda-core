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

import java.awt.Dimension;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import org.springframework.util.StringUtils;

/**
 * A {@link PropertyEditor} for {@link Dimension} objects. Strings must be in the format {@code "x, y"}; for example,
 * {@code "640, 480"}.
 */
public class DimensionPropertyEditor extends PropertyEditorSupport {

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.hasText(text)) {
			try {
				// remove spaces
				text = text.replace(" ", "");
				
				String[] numbers = text.split(",");
				
				if (numbers.length != 2) {
					throw new IllegalArgumentException("Could not convert " + StringUtils.quote(text) + " to a Dimension; it does not consist of two numbers");
				}
				
				Dimension d = new Dimension();
				d.width = Integer.parseInt(numbers[0]);
				d.height = Integer.parseInt(numbers[1]);
				
				setValue(d);
			} catch (Throwable e) {
				throw new IllegalArgumentException("Could not convert " + StringUtils.quote(text) + " to a Dimension", e);
			}
		} else {
			setValue(null);
		}
	}

	@Override
	public Dimension getValue() {
		return (Dimension) super.getValue();
	}

}
