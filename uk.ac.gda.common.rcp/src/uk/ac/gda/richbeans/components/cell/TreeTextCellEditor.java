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

package uk.ac.gda.richbeans.components.cell;

import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;

/**
 * @author fcp94556
 *
 */
public class TreeTextCellEditor extends TextCellEditor {

	/**
	 * @param parent
	 * @param flags
	 */
	public TreeTextCellEditor(Composite parent, int flags) {
		super(parent,flags);
	}

	/**
	 * @param listener
	 */
	public void addVerifyListener(final VerifyListener listener) {
		text.addVerifyListener(listener);
	}
	/**
	 * @param listener
	 */
	public void removeVerifyListener(final VerifyListener listener) {
		text.removeVerifyListener(listener);
	}

}

	