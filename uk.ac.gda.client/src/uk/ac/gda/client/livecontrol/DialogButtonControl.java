/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package uk.ac.gda.client.livecontrol;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.util.Objects;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;


public class DialogButtonControl extends LiveControlBase {
	private static final Logger logger = LoggerFactory.getLogger(DialogButtonControl.class);

	private String buttonText;
	private String buttonTooltip = "";
	private Button button;
	private DialogFactory dialogFactory;

	@Override
	public void createControl(Composite composite) {
		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		composite.setBackgroundMode(SWT.INHERIT_FORCE);

		button = new Button(composite, SWT.NONE);
		button.setText(buttonText);
		button.setToolTipText(buttonTooltip);
		button.addSelectionListener(widgetSelectedAdapter(e -> openDialog()));
	}

	private void openDialog() {
		button.setEnabled(false);
		try {
			var currentDialog = dialogFactory.create(Display.getCurrent().getActiveShell());
			if (currentDialog != null) {
				currentDialog.create();
				currentDialog.open();
			} else {
				logger.warn("Dialog creation returned null — dialog will not be shown.");
			}
		} catch (Exception e) {
			logger.error("Unexpected error while opening dialog", e);
			MessageDialog.openError(Display.getCurrent().getActiveShell(),
			                        "Error", "An unexpected error occurred while opening the dialog.");
		} finally {
			button.setEnabled(true);
		}
		button.setEnabled(true);
	}

	public void setButtonText(String buttonText) {
		this.buttonText = buttonText;
	}

	public void setButtonTooltip(String buttonTooltip) {
		this.buttonTooltip = buttonTooltip;
	}

	public void setDialogFactory(DialogFactory factory) {
		this.dialogFactory = factory;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(buttonText, buttonTooltip);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DialogButtonControl other = (DialogButtonControl) obj;
		return Objects.equals(buttonText, other.buttonText) && Objects.equals(buttonTooltip, other.buttonTooltip);
	}
}
