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

import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyJavaType;
import org.python.core.PyObject;
import org.python.core.PySyntaxError;
import org.python.core.PySystemState;
import org.python.core.PyUnicode;
import org.python.util.InteractiveConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that overrides InteractiveConsole to allow customisation
 */
public class GDAInteractiveConsole extends InteractiveConsole {
	private static final Logger logger = LoggerFactory.getLogger(GDAInteractiveConsole.class);

	public GDAInteractiveConsole() {
		// treat all interactive console input as UTF-8 by default
		cflags.encoding = "utf-8";
		cflags.source_is_utf8 = true;
	}
	
	/**
	 * Custom displayhook to print unicode values in unicode
	 * @see <a href="http://www.python.org/dev/peps/pep-0217/">PEP-217 for more details on displayhook</a>
	 * @see PySystemState#displayhook
	 * @param o the object to print and store in <code>_</code>
	 */
    public static void displayhook(PyObject o) {
        /* Print value except if None */
        /* After printing, also assign to '_' */
        /* Before, set '_' to None to avoid recursion */
        if (o == Py.None)
             return;

        PyObject currentBuiltins = Py.getSystemState().getBuiltins();
        currentBuiltins.__setitem__("_", Py.None);
        if (o instanceof PyUnicode) {
        	PyUnicode u = (PyUnicode)o;
			Py.println(u.__str__());
		} else {
			Py.println(o.__repr__());
		}
        currentBuiltins.__setitem__("_", o);
    }

	
	/**
	 * Show syntax error messages in the Terminal This is to work around an apparent bug in jython. showexception will
	 * print to the terminal for ordinary stuff, just not PySyntaxErrors.
	 * log as error as it is an exception
	 * Only show cause as a single line on teh Jython console
	 */
	@Override
	public void showexception(PyException arg0) {
		if (arg0 instanceof PySyntaxError) {
			System.err.println("*** Handling PySyntaxError");
			if( logger.isTraceEnabled() )
				super.showexception(arg0);
			InterfaceProvider.getTerminalPrinter().print(arg0.toString());
		} else {
			if (arg0.type instanceof PyJavaType) {
				System.err.printf("*** Handling PyException with type == PyJavaType - value is |%s|%n", arg0.value);
				// in this case the jython code is just way too verbose for users
				// we cut it down to the jython stack trace and the exception message (and class)
				InterfaceProvider.getTerminalPrinter().print(arg0.traceback.dumpStack());
				PyObject value = arg0.value;
				Throwable throwable = (Throwable) value.__tojava__(Throwable.class);
				InterfaceProvider.getTerminalPrinter().print(throwable.getClass().getName()+": "+throwable.getMessage());
			} else {
				System.err.printf("*** Handling PyException with type == %s - value is |%s|%n", arg0.type, arg0.value);
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

		logger.error("InteractiveConsole exception: " + msg, arg0);
	}
	
	// All of our strings are unicode potentially, so create a unicode PyString
	@Override
	public PyObject eval(String s) {
		return eval(new PyUnicode(s));
	}
	
}