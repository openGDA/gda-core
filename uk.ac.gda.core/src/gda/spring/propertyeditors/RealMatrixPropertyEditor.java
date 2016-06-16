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
import org.apache.commons.math.linear.RealMatrix;

/**
 * A {@link PropertyEditor} for matrices. Strings must be in the format
 * <code>{{1,2,3},{4,5,6},{7,8,9}}</code> or <code>{{1;2;3};{4;5;6};{7;8;9}}</code>.
 */
public class RealMatrixPropertyEditor extends PropertyEditorSupport {

	private TwoDDoubleArrayPropertyEditor arrayPropEditor;

	/**
	 * Creates a matrix property editor.
	 */
	public RealMatrixPropertyEditor() {
		this.arrayPropEditor = new TwoDDoubleArrayPropertyEditor();
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		arrayPropEditor.setAsText(text);
		double[][] matrixValues = arrayPropEditor.getValue();
		RealMatrix matrix = MatrixUtils.createRealMatrix(matrixValues);
		setValue(matrix);
	}

	@Override
	public RealMatrix getValue() {
		return (RealMatrix) super.getValue();
	}

	@Override
	public String getAsText() {
		final String s = getValue().toString();
		return s.substring(s.indexOf('{'));
	}

}
