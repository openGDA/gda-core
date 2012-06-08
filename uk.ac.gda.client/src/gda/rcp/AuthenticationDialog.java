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

package gda.rcp;

// use to open dialog box from Application~Workbench
// final AuthenticationDialog dialog = new AuthenticationDialog(PlatformUI.getWorkbench().getDisplay(), SWT.OPEN);
// dialog.open();

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import uk.ac.gda.ui.dialog.OKCancelDialog;

/**
	 *
	 */
public final class AuthenticationDialog extends OKCancelDialog {

	/**
	 * @param disp
	 * @param style
	 */
	public AuthenticationDialog(Display disp, int style) {
		super(new Shell(disp), style);
	}

	/**
	 * @param shell
	 * @param userObject
	 */
	@Override
	public void createUserUI(final Shell shell, final Object userObject) {

		// Your code goes here (widget creation, set result, etc).
		Button button = new Button(shell, SWT.PUSH);
		button.setText("Automatic");
	}
}
