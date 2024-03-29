/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.jython;

import java.io.InputStream;
import java.time.Instant;

import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyJavaType;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.core.PyUnicode;
import org.python.util.InteractiveConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.commands.InputCommands;
import gda.jython.logging.PythonException;
import gda.scan.ScanInterruptedException;

/**
 * Class that overrides InteractiveConsole to allow customisation
 */
public class GDAInteractiveConsole extends InteractiveConsole {
	private static final Logger logger = LoggerFactory.getLogger(GDAInteractiveConsole.class);

	// Use the default ps1 and ps2 prompts
	private static final PyString PS1_PROMPT = new PyString(">>> ");
	private static final PyString PS2_PROMPT = new PyString("... ");

	public GDAInteractiveConsole(PyObject locals, PySystemState pySystemState) {
		super(locals, "<input>", true);
		// treat all interactive console input as UTF-8 by default
		cflags.encoding = "utf-8";
		cflags.source_is_utf8 = true;
		// Set the command prompts for interactive interpreter. Fixes a issue auto-completing on sys
		pySystemState.ps1 = PS1_PROMPT;
		pySystemState.ps2 = PS2_PROMPT;
		super.systemState = pySystemState;
		pySystemState.stdin = new GDAStdin();
	}

	/**
	 * Show syntax error messages in the Terminal This is to work around an apparent bug in jython. showexception will
	 * print to the terminal for ordinary stuff, just not PySyntaxErrors.
	 * log as error as it is an exception
	 * Only show cause as a single line on teh Jython console
	 */
	@Override
	public void showexception(PyException arg0) {
		if (arg0.match(Py.SyntaxError) || arg0.match(Py.IndentationError)) {
			if( logger.isTraceEnabled() )
				super.showexception(arg0);
			InterfaceProvider.getTerminalPrinter().print(arg0.toString());
		} else if (arg0.match(Py.KeyboardInterrupt)) {
			InterfaceProvider.getTerminalPrinter().print("KeyboardInterrupt");
		} else if (arg0.match(Py.SystemExit)) {
			// super.showexception handles SystemExit as a special case and shuts down the JVM
			blockExit();
		} else {
			InterfaceProvider.getTerminalPrinter().print("Error time: " + Instant.now());
			if (arg0.type instanceof PyJavaType) {
				// in this case the jython code is just way too verbose for users
				// we cut it down to the jython stack trace and the exception message (and class)
				if (arg0.traceback != null) {
					// Occasionally exceptions have no traceback - see DAQ-3628
					InterfaceProvider.getTerminalPrinter().print(arg0.traceback.dumpStack());
				}
				PyObject value = arg0.value;
				Throwable throwable = (Throwable) value.__tojava__(Throwable.class);
				InterfaceProvider.getTerminalPrinter().print(throwable.getClass().getName()+": "+throwable.getMessage());
			} else {
				super.showexception(arg0);
			}
		}
		//always log as error. It is up to the log config to decide how much of the exception is to be shown
		//If a PyException getMessage returns null. toString returns the stack which is not what we want as we pass the whole stack in the throwable to the
		//error method
		//Sometimes the cause is null
		String msg;
		if( arg0.getMessage() == null ){
			if( arg0.getCause() == null )
				msg = arg0.toString();
			else
				msg = arg0.getCause().getMessage();
		} else
			msg = arg0.getMessage();

		if (arg0.getCause() instanceof ScanInterruptedException) {
			logger.info("InteractiveConsole exception: " + msg);
		} else {
			logger.error("InteractiveConsole exception: " + msg, arg0);
		}
	}

	// All of our strings are unicode potentially, so create a unicode PyString
	@Override
	public PyObject eval(String s) {
		return eval(new PyUnicode(s));
	}

	@Override
	public void setIn(InputStream inStream) {
		// We need to block the default stdin handling as multiple clients share
		// this console.
	}

	/**
	 * Run the given command in the Jython interpreter after running it through the translator.
	 * <p>
	 * {@inheritDoc}
	 *
	 * @see gda.jython.translator.Translator
	 */
	@Override
	public boolean runsource(String command) {
		try {
			logger.debug("GDA command: {}", command);
			command = GDAJythonInterpreter.getTranslator().translate(command);
			logger.debug("Jython command: {}", command);
			return super.runsource(command);
		} catch (Exception e) {
			if (e instanceof PyException pe) {
				if (pe.match(Py.SystemExit)) {
					blockExit();
					return false;
				}
				logger.error("Error calling runsource for command: {}", command, PythonException.from(pe));
				showexception(pe);
			} else {
				logger.error("Error calling runsource for command: {}", command, e);
				// Most Exceptions raised within Jython code should be printed via
				// the showException method but if for any reason a command throws a
				// Java exception, ensure that the user is aware of it
				InterfaceProvider.getTerminalPrinter().print("Error running command: " + e);
			}
			return false;
		}
	}

	/**
	 * Calling <code> exit()</code> or <code>sys.exit()</code> should not shutdown the GDA server so
	 * display a message to users and return.
	 */
	private void blockExit() {
		logger.debug("Trying to exit from jython");
		write("Can't exit the Jython Interpreter. Use reset_namespace to reset\n");
	}

	/**
	 * Read a line of user input.
	 * <p>
	 * Overrides the default raw_input to allow different InputStreams to be specified when
	 * running a command
	 */
	// Don't use the builtin raw_input so that we can control how the prompt is displayed
	@Override
	public String raw_input(PyObject prompt) {
		return consoleReadline(prompt.asString());
	}

	/**
	 * Class to act as sys.stdin in Jython. Delegates to the InteractiveConsole so that the correct
	 * inputStream is used.
	 */
	// Needs to be public so Jython can reflect on it
	public class GDAStdin extends PyObject {
		public PyObject readline() {
			return new PyString(consoleReadline(""));
		}
	}

	/**
	 * Read a line from the STDIN of the process calling the command.
	 * @param prompt String to display when prompting user for input
	 * @return String input by the user
	 * @throws PyException if user cancels input before entering by either
	 * KeyboardInterrupt or EOF
	 */
	private String consoleReadline(String prompt) {
		try {
			return InputCommands.requestInput(prompt);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			throw new PyException(Py.KeyboardInterrupt);
		}
	}
}