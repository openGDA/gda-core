/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.swtdesigner.SWTResourceManager;

/**
 * Live control to create a button that runs a command.<br>
 * As a command could be run in different ways (e.g. in Jython or in a Bash shell), this is left to subclasses.
 */
abstract class CommandControl extends LiveControlBase {

	private String buttonText;
	private String buttonTooltip = "";
	private String command;

	@Override
	public void createControl(Composite composite) {
		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		composite.setBackgroundMode(SWT.INHERIT_FORCE);
		final Button button = new Button(composite, SWT.NONE);
		button.setText(buttonText);
		button.setToolTipText(buttonTooltip);
		button.addSelectionListener(widgetSelectedAdapter(e -> runCommand(command)));
	}

	protected abstract void runCommand(String command);

	public void setButtonText(String buttonText) {
		this.buttonText = buttonText;
	}

	public void setButtonTooltip(String buttonTooltip) {
		this.buttonTooltip = buttonTooltip;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((buttonText == null) ? 0 : buttonText.hashCode());
		result = prime * result + ((buttonTooltip == null) ? 0 : buttonTooltip.hashCode());
		result = prime * result + ((command == null) ? 0 : command.hashCode());
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
		CommandControl other = (CommandControl) obj;
		if (buttonText == null) {
			if (other.buttonText != null)
				return false;
		} else if (!buttonText.equals(other.buttonText))
			return false;
		if (buttonTooltip == null) {
			if (other.buttonTooltip != null)
				return false;
		} else if (!buttonTooltip.equals(other.buttonTooltip))
			return false;
		if (command == null) {
			if (other.command != null)
				return false;
		} else if (!command.equals(other.command))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CommandControl [buttonText=" + buttonText + ", buttonTooltip=" + buttonTooltip + ", command=" + command
				+ "]";
	}
}
