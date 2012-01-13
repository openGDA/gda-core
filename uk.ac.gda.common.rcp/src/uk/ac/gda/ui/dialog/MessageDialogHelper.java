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

package uk.ac.gda.ui.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * This class adds to the static methods already provided by MessageDialog
 */
public class MessageDialogHelper {

	/**
	 * Possible answers from message dialogs.
	 */
	public static enum Answer {
		YES, NO, YES_TO_ALL, NO_TO_ALL, OK, CANCEL, DEFAULT;
	}

	/**
	 * Convenience method to open a simple Yes/YesToAll/No/NoToAll question dialog.
	 * 
	 * @param parent
	 *            the parent shell of the dialog, or <code>null</code> if none
	 * @param title
	 *            the dialog's title, or <code>null</code> if none
	 * @param message
	 *            the message
	 * @return <code>Answer.YES</code>, <code>Answer.YES_TO_ALL</code>, <code>Answer.NO</code>,
	 *         <code>Answer.NO_TO_ALL</code> corresponding to the pressed button. Or <code>Answer.DEFAULT</code>
	 *         if dialog.open() returns <code>SWT.DEFAULT</code>
	 */
	public static Answer openYesNoToAll(Shell parent, String title, String message) {
		String[] buttonlabels = new String[] { IDialogConstants.YES_LABEL, IDialogConstants.YES_TO_ALL_LABEL,
				IDialogConstants.NO_LABEL, IDialogConstants.NO_TO_ALL_LABEL };
		MessageDialog dialog = new MessageDialog(parent, title, null, message, MessageDialog.QUESTION, buttonlabels, 0);
		switch (dialog.open()) {
		case 0:
			return Answer.YES;
		case 1:
			return Answer.YES_TO_ALL;
		case 2:
			return Answer.NO;
		case 3:
			return Answer.NO_TO_ALL;
		default:
			return Answer.DEFAULT;
		}

	}
}
