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

package uk.ac.gda.richbeans.beans;

import org.eclipse.swt.widgets.Control;

/**
 * An interface used to mark a widget as providing expression evaluation.
 * 
 * A sub-set of all IFieldWidgets will also be IExpressionWidget
 */
public interface IExpressionWidget extends IFieldWidget {

	/**
	 * Set the manager for expressions.
	 * @param man
	 */
	public void setExpressionManager(IExpressionManager man);
	
	/**
	 * Sets the displayed value for the expression. Used as short
	 * cut for updating the value, saves expensive recalculation
	 * cycle in RichBeanEditor. Usually called from IExpressionManager
	 * when precedents change.
	 * 
	 * @param value
	 */
	public void setExpressionValue(double value);
	
	/**
	 * Called to return the main control used by the widget.
	 * 
	 * This control will have content proposals added to it if the control
	 * is a type that the IExpressionManager recognises as possible to have
	 * content proposals.
	 */
	public Control getControl();
	
	/**
	 * This method returns false if the string entered is definitely a number.
	 * For instance a double value or a double value and a unit.
	 * 
	 * Otherwise it returns true.
	 * 
	 * @param value
	 * @return false if number
	 */
	public boolean isExpressionParseRequired(final String value);
	
	/**
	 * Returns false if the box does not currently allow expressions.
	 */
	public boolean isExpressionAllowed();

}
