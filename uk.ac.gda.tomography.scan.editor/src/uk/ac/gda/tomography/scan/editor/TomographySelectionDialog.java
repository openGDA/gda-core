/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.scan.editor;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import uk.ac.gda.tomography.scan.editor.view.TomographyMessages;
import uk.ac.gda.tomography.scan.editor.view.TomographyMessagesUtility;

/**
 * Allows the user to select an item from a list.
 *
 * @author Maurizio Nagni
 */
public class TomographySelectionDialog extends ElementListSelectionDialog {

	public TomographySelectionDialog(Shell parentShell, String[] items, TomographyMessages dialogTitle) {
		super(parentShell, new LabelProvider());
		setElements(items);
		setTitle(TomographyMessagesUtility.getMessage(dialogTitle));
	}

}
