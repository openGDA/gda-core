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

package uk.ac.gda.richbeans.components.selector;

/**
 * This interface defines optional features that can be applied to a ListEditor. 
 */
public interface ListEditorUI {

	/**
	 * @param listEditor the ListEditor making the notification
	 */
	public void notifySelected(ListEditor listEditor);
	
	/**
	 * @param listEditor the ListEditor making the request
	 * @return true/false if delete is possible
	 */
	public boolean isDeleteAllowed(ListEditor listEditor);
	
	/**
	 * @param listEditor the ListEditor making the request
	 * @return true/false if add is possible
	 */
	public boolean isAddAllowed(ListEditor listEditor);
	
	/**
	 * @param listEditor the ListEditor making the request
	 * @return true/false if reorder is possible
	 */
	public boolean isReorderAllowed(ListEditor listEditor);

}
