/*-
 * Copyright © 2026 Diamond Light Source Ltd.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import gda.jython.ICommandRunner;
import gda.jython.InterfaceProvider;

/**
 * Unit tests for {@link InputCommandControl}.
 */
public class InputCommandControlTest {

	private InputCommandControl control;

	@Before
	public void setUp() {
		control = new InputCommandControl();
	}

	// --- Getters/setters ---

	@Test
	public void defaultsToNumericInput() {
		// Documents current default behavior rather than asserting invented validation
		assertFalse(control.isTextInput());
	}

	@Test
	public void commandGetterSetterRoundTrips() {
		control.setCommand("pos x");
		assertEquals("pos x", control.getCommand());
	}

	@Test
	public void displayNameGetterSetterRoundTrips() {
		control.setDisplayName("X Position");
		assertEquals("X Position", control.getDisplayName());
	}

	@Test
	public void textInputGetterSetterRoundTrips() {
		control.setTextInput(true);
		assertTrue(control.isTextInput());
	}

	@Test
	public void nullCommandAndDisplayNameAreTolerated() {
		// Edge case: documents that nulls are accepted, not rejected
		control.setCommand(null);
		control.setDisplayName(null);
		assertNull(control.getCommand());
		assertNull(control.getDisplayName());
	}

	// --- equals() / hashCode() ---

	@Test
	public void equalsIsReflexive() {
		control.setCommand("pos x");
		control.setDisplayName("X");
		control.setTextInput(true);
		assertEquals(control, control);
	}

	@Test
	public void equalObjectsHaveEqualFieldsAndHash() {
		InputCommandControl a = build("pos x", "X", true);
		InputCommandControl b = build("pos x", "X", true);

		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	public void differingCommandBreaksEquality() {
		InputCommandControl a = build("pos x", "X", true);
		InputCommandControl b = build("pos y", "X", true);

		assertNotEquals(a, b);
	}

	@Test
	public void differingTextInputBreaksEquality() {
		InputCommandControl a = build("pos x", "X", true);
		InputCommandControl b = build("pos x", "X", false);

		assertNotEquals(a, b);
	}

	@Test
	public void differingDisplayNameBreaksEquality() {
		InputCommandControl a = build("pos x", "X", true);
		InputCommandControl b = build("pos x", "Y", true);

		assertNotEquals(a, b);
	}

	@Test
	public void notEqualToNullOrDifferentType() {
		InputCommandControl a = build("pos x", "X", true);
		assertNotEquals(a, null);
		assertNotEquals(a, "some string");
	}

	private InputCommandControl build(String command, String displayName, boolean textInput) {
		InputCommandControl c = new InputCommandControl();
		c.setCommand(command);
		c.setDisplayName(displayName);
		c.setTextInput(textInput);
		return c;
	}

	// --- buildCommand() / parseNumericValue() ---

	@Test
	public void buildCommandFormatsNumericInputWithCommandPrefix() {
		control.setCommand("pos x");
		control.setTextInput(false);

		String result = control.buildCommand("10.0");

		assertEquals("pos x 10.000000", result);
	}

	@Test
	public void buildCommandFormatsTextInputWithCommandPrefix() {
		control.setCommand("pos x");
		control.setTextInput(true);

		String result = control.buildCommand("home");

		assertEquals("pos x home", result);
	}

	@Test
	public void buildCommandTrimsWhitespaceFromNumericInput() {
		control.setCommand("pos x");
		control.setTextInput(false);

		String result = control.buildCommand("  10.0  ");

		assertEquals("pos x 10.000000", result);
	}

	@Test
	public void buildCommandDoesNotTrimTextInput() {
		// Documents current behavior: unlike numeric input, text input is passed through as-is
		control.setCommand("pos x");
		control.setTextInput(true);

		String result = control.buildCommand("  home  ");

		assertEquals("pos x   home  ", result);
	}

	@Test
	public void parseNumericValueTakesFirstWhitespaceSeparatedToken() {
		// Documents current behavior: trailing tokens (e.g. units) are ignored
		assertEquals(10.0, control.parseNumericValue("10.0 mm"), 0.0001);
	}

	@Test
	public void parseNumericValueParsesPlainNumber() {
		assertEquals(-3.5, control.parseNumericValue("-3.5"), 0.0001);
	}

	@Test
	public void parseNumericValueThrowsOnInvalidNumber() {
		assertThrows(NumberFormatException.class, () -> control.parseNumericValue("abc"));
	}

	@Test
	public void parseNumericValueThrowsOnEmptyString() {
		assertThrows(NumberFormatException.class, () -> control.parseNumericValue(""));
	}

	// --- runCommand() delegation ---

	@Test
	public void runCommandDelegatesToInterfaceProviderCommandRunner() {
		try (MockedStatic<InterfaceProvider> mockedProvider = mockStatic(InterfaceProvider.class)) {
			ICommandRunner mockRunner = mock(ICommandRunner.class);
			mockedProvider.when(InterfaceProvider::getCommandRunner).thenReturn(mockRunner);

			control.runCommand("pos x 10.000000");

			verify(mockRunner).runCommand("pos x 10.000000");
		}
	}

	@Test
	public void buildCommandThenRunCommandEndToEndForNumericInput() {
		control.setCommand("pos x");
		control.setTextInput(false);

		try (MockedStatic<InterfaceProvider> mockedProvider = mockStatic(InterfaceProvider.class)) {
			ICommandRunner mockRunner = mock(ICommandRunner.class);
			mockedProvider.when(InterfaceProvider::getCommandRunner).thenReturn(mockRunner);

			String command = control.buildCommand("10.0");
			control.runCommand(command);

			verify(mockRunner).runCommand("pos x 10.000000");
		}
	}
}