/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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
 * Convenience class that maintains defaults for how classes in the ListEditor
 * use ListEditorUI Interface
 */
public class ListEditorUIAdapter implements ListEditorUI {
	
	private static ListEditorUIAdapter defaultAdapter;
	
	/**
	 * Return instance of ListEditorUI implementing the default values
	 * @return instance of ListEditorUI implementing the default values
	 */
	public static ListEditorUIAdapter getDefault() {
		if (defaultAdapter == null)
			defaultAdapter = new ListEditorUIAdapter();
		return defaultAdapter;
	}
	
	@Override
	public boolean isAddAllowed(ListEditor listEditor) {
		return true;
	}

	@Override
	public boolean isDeleteAllowed(ListEditor listEditor) {
		return true;
	}

	@Override
	public boolean isReorderAllowed(ListEditor listEditor) {
		return true;
	}

	@Override
	public void notifySelected(ListEditor listEditor) {
		// default is to do nothing on notification
	}

}
