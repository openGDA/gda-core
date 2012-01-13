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

import java.util.Collection;

/**
 * An interface to encapsulate expression support.
 */
public interface IExpressionManager {
	
	/**
	 * 
	 * @return true if expression is valid.
	 */
	public boolean isExpressionValid();
	
	/**
	 * Called to get the value entered by the user as a double. Must call
	 * 
	 * @return the evaluated value or an exception or Double.NaN if the expression is null.
	 */
	public double getExpressionValue();
	
	/**
	 * Sets the expression.
	 * @param expression
	 * 
	 */
    public void setExpression(final String expression);
    
	/**
	 * Returns the current string expression, last called by 
	 * @return expression
	 */
	public String getExpression();
	
	/**
	 * Returns a list of the symbols that can be used in the expression.
	 * This then can be shown in a drop down for the widget for instance.
	 * @return list of symbols.
	 */
	public Collection<String> getAllowedSymbols() throws Exception;
	
	/**
	 * Call to set symbol set which can be used in expressions.
	 * @param symbols
	 * @throws Exception
	 */
	public void setAllowedSymbols(Collection<String> symbols) throws Exception;
}
