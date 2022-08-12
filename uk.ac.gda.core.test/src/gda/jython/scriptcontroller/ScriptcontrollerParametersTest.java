/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.jython.scriptcontroller;

import static org.junit.Assert.assertEquals;

import java.util.MissingFormatArgumentException;

import org.junit.jupiter.api.Test;

public class ScriptcontrollerParametersTest {

	public final String COMMAND = "command";
	public final String PARAM_ONE = "one";
	public final String PARAM_TWO = "two";
	public final String PARAM_THREE = "three";
	public final String PARAMS_NAME = "parameters";

	public final String FORMAT_ONE = COMMAND + "(%s)";
	public final String FORMAT_ALL = COMMAND + "(%s, %s, %s)";
	public final String FORMAT_NAMED = COMMAND + "(ONE=%s, TWO=%s)";

	// Expected results
	public final String RESULT_RAW = COMMAND;
	public final String RESULT_CALL = COMMAND + "()";
	public final String RESULT_PARAMETER_NAME = COMMAND + "(" + PARAMS_NAME + ")";
	public final String RESULT_PARAMETER_ONE = COMMAND + "(" + PARAM_ONE + ")";
	public final String RESULT_PARAMETER_ALL = COMMAND + "(" + PARAM_ONE + ", " + PARAM_TWO + ", " + PARAM_THREE + ")";
	public final String RESULT_NAMED_PARAMETER = COMMAND + "(ONE=" + PARAM_ONE + ", TWO=" + PARAM_TWO + ")";

	@Test
	public void testFormatParametersRaw() {
		ScriptControllerBase controller = new ScriptControllerBase();
		controller.setCommand(COMMAND);
		assertEquals(RESULT_RAW, controller.getCommand());
	}

	@Test
	public void testFormatParametersCall() {
		ScriptControllerBase controller = new ScriptControllerBase();
		controller.setCommand(COMMAND);
		controller.setCommandFormat(RESULT_CALL);
		assertEquals(RESULT_CALL, controller.getCommand());
	}

	@Test
	public void testFormatParametersName() {
		ScriptControllerBase controller = new ScriptControllerBase();
		controller.setCommand(COMMAND);
		controller.setCommandFormat(FORMAT_ONE);
		controller.setParametersName(PARAMS_NAME);
		assertEquals(RESULT_PARAMETER_NAME, controller.getCommand());
	}

	@Test
	public void testFormatParametersOne() {
		ScriptControllerBase controller = new ScriptControllerBase();
		controller.setCommand(COMMAND);
		controller.setCommandFormat(FORMAT_ONE);
		controller.addParameter(PARAM_ONE);
		assertEquals(RESULT_PARAMETER_ONE, controller.getCommand());

		// Test that additional parameters are ignored
		controller.addParameter(PARAM_TWO);
		controller.addParameter(PARAM_THREE);
		assertEquals(RESULT_PARAMETER_ONE, controller.getCommand());
	}

	@Test
	public void testFormatParametersAll() {
		ScriptControllerBase controller = new ScriptControllerBase();
		controller.setCommand(COMMAND);
		controller.setCommandFormat(FORMAT_ALL);
		controller.addParameter(PARAM_ONE);
		controller.addParameter(PARAM_TWO);
		controller.addParameter(PARAM_THREE);
		assertEquals(RESULT_PARAMETER_ALL, controller.getCommand());
	}

	@Test(expected = MissingFormatArgumentException.class)
	public void testMissingParameters() {
		ScriptControllerBase controller = new ScriptControllerBase();
		controller.setCommand(COMMAND);
		controller.setCommandFormat(FORMAT_ALL);
		controller.addParameter(PARAM_ONE);
		controller.getCommand();
	}

	@Test
	public void testFormatNamedParameter() {
		ScriptControllerBase controller = new ScriptControllerBase();
		controller.setCommand(COMMAND);
		controller.setCommandFormat(FORMAT_NAMED);
		controller.addParameter(PARAM_ONE);
		controller.addParameter(PARAM_TWO);
		assertEquals(RESULT_NAMED_PARAMETER, controller.getCommand());
	}
}
