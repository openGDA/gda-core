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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import gda.jython.ICommandRunner;
import gda.jython.InterfaceProvider;

public class JythonCommandControl implements LiveControl {

	private String name;
	private String group;
	private String buttonText;
	private String jythonCommand;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getButtonText() {
		return buttonText;
	}

	public void setButtonText(String buttonText) {
		this.buttonText = buttonText;
	}

	public String getJythonCommand() {
		return jythonCommand;
	}

	public void setJythonCommand(String jythonCommand) {
		this.jythonCommand = jythonCommand;
	}

	@Override
	public void createControl(Composite composite) {
		final ICommandRunner commandRunner = InterfaceProvider.getCommandRunner();
		final Button button = new Button(composite, SWT.NONE);
		button.setText(buttonText);

		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				commandRunner.runCommand(jythonCommand);
			}
		});

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((buttonText == null) ? 0 : buttonText.hashCode());
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((jythonCommand == null) ? 0 : jythonCommand.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JythonCommandControl other = (JythonCommandControl) obj;
		if (buttonText == null) {
			if (other.buttonText != null)
				return false;
		} else if (!buttonText.equals(other.buttonText))
			return false;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (jythonCommand == null) {
			if (other.jythonCommand != null)
				return false;
		} else if (!jythonCommand.equals(other.jythonCommand))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
