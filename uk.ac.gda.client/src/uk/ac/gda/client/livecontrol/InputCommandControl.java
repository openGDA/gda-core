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

import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.jython.InterfaceProvider;

/**
 * Live control to create a text input that runs a command using that input value.<br>
 * Configurable command will be run when user hits enter with value parsed as String or <br>
 * double based on configurable textInput(default=false) flag.
 */
public class InputCommandControl extends LiveControlBase {
	private static final Logger logger = LoggerFactory.getLogger(InputCommandControl.class);

	private String command;
	private boolean textInput = false;
	private String displayName;


	@Override
	public void createControl(Composite composite) {
		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		composite.setBackgroundMode(SWT.INHERIT_FORCE);

		// Name label
		Label displayNameLabel = new Label(composite, SWT.NONE);
		displayNameLabel.setText(displayName);

		// Position text box
		Text positionText = new Text(composite, SWT.BORDER);
		positionText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent key) {
				if (key.character == SWT.CR) {
					String newCommand = buildCommand(positionText.getText());
					runCommand(newCommand);
				}
			}
		});
	}

	/**
	 * Builds the full command string to run, given the raw text entered by the user.
	 *
	 * @param rawText the raw, unparsed text currently in the input box
	 * @return the command string to pass to {@link #runCommand(String)}
	 */
	String buildCommand(String rawText) {
		if (!isTextInput()) {
			// input is number
			double newValue = parseNumericValue(rawText);
			return String.format("%s %f", getCommand(), newValue);
		} else {
			// input is String
			return String.format("%s %s", getCommand(), rawText);
		}
	}

	/**
	 * Parses the numeric value out of the raw text box contents.
	 *
	 * @param rawText the raw, unparsed text currently in the input box
	 * @return the parsed numeric value
	 * @throws NumberFormatException if the first token is not a valid double
	 */
	double parseNumericValue(String rawText) {
		return Double.parseDouble(rawText.trim().split(" ")[0]);
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}


	public boolean isTextInput() {
		return textInput;
	}

	public void setTextInput(boolean isText) {
		this.textInput = isText;
	}

	protected void runCommand(String command) {
		logger.debug("Running Jython command: {}", command);
		InterfaceProvider.getCommandRunner().runCommand(command);
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		InputCommandControl other = (InputCommandControl) obj;
		return Objects.equals(command, other.command) && Objects.equals(displayName, other.displayName)
				&& textInput == other.textInput;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(command, displayName, textInput);
		return result;
	}
}
