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

import java.util.EventObject;


/**
 *
 */
public class BeanSelectionEvent extends EventObject {

	private Object selectedBean;
	private int    selectionIndex;

	/**
	 * @param source
	 * @param selectionIndex
	 */
	public BeanSelectionEvent(Object source, final int selectionIndex, final Object selectedBean) {
		super(source);
		this.selectionIndex = selectionIndex;
		this.selectedBean   = selectedBean;
	}

	/**
	 * @return Returns the selectedBean.
	 */
	public Object getSelectedBean() {
		return selectedBean;
	}

	/**
	 * @param selectedBean The selectedBean to set.
	 */
	public void setSelectedBean(Object selectedBean) {
		this.selectedBean = selectedBean;
	}

	/**
	 * @return Returns the selectionIndex.
	 */
	public int getSelectionIndex() {
		return selectionIndex;
	}

	/**
	 * @param selectionIndex The selectionIndex to set.
	 */
	public void setSelectionIndex(int selectionIndex) {
		this.selectionIndex = selectionIndex;
	}

}
