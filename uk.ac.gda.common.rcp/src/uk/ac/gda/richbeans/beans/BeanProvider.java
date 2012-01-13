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

/**
 * An interface to return the current bean.
 * Can be passed to UI objects which cannot see the actual bean.
 */
public interface BeanProvider {

	/**
	 * Returns the current selected bean or the bean that the 
	 * user is working on.
	 * @return the bean.
	 */
	public Object getBean();
	
	/**
	 * Returns a new bean of the current editing class 
	 * with no values in which is not linked to the system 
	 * and can be used for anything without data being affected.
	 * @return new bean
	 * @throws Exception 
	 */
	public Object getInstance() throws Exception;
}
